package com.rudra.hisab.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.ExpenseRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class DashboardState(
    val shopName: String = "",
    val todaySales: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val totalDues: Double = 0.0,
    val lowStockCount: Int = 0,
    val todaySaleCount: Int = 0,
    val isLoading: Boolean = true
) {
    val netProfit: Double get() = todaySales - todayExpenses
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val transactionRepository: TransactionRepository,
    private val expenseRepository: ExpenseRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        val now = LocalDate.now()
        val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            appPreferences.settings.collect { settings ->
                _state.value = _state.value.copy(shopName = settings.shopName)
            }
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val salesFlow = transactionRepository.getTodaySalesFlow(startOfDay, endOfDay)
            val expensesFlow = expenseRepository.getTodayExpensesFlow(startOfDay, endOfDay)
            val duesFlow = customerRepository.getTotalDues()
            val lowStockFlow = productRepository.getLowStockProducts()

            combine(salesFlow, expensesFlow, duesFlow, lowStockFlow) { sales, expenses, dues, lowStock ->
                _state.value.copy(
                    todaySales = sales,
                    todayExpenses = expenses,
                    totalDues = dues ?: 0.0,
                    lowStockCount = lowStock.size,
                    isLoading = false
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }
}
