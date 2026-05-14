package com.rudra.hisab.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.ExpenseRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
)

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

    fun refresh() {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val now = LocalDate.now()
            val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val settings = appPreferences.settings.value

            val todaySales = transactionRepository.getTodaySalesTotal(startOfDay, endOfDay)
            val todayExpenses = expenseRepository.getTodayExpensesTotal(startOfDay, endOfDay)
            val todayPurchases = transactionRepository.getTodayPurchasesTotal(startOfDay, endOfDay)
            val todaySalesCount = transactionRepository.getTodaySaleCount(startOfDay, endOfDay)

            appPreferences.settings.collect { s ->
                _state.value = _state.value.copy(shopName = s.shopName)
            }

            customerRepository.getTotalDues().collect { dues ->
                val lowStock = productRepository.getLowStockProducts().value
                _state.value = _state.value.copy(
                    shopName = settings.shopName,
                    todaySales = todaySales,
                    todayExpenses = todayExpenses + todayPurchases,
                    totalDues = dues ?: 0.0,
                    lowStockCount = lowStock.size,
                    todaySaleCount = todaySalesCount,
                    isLoading = false
                )
            }
        }
    }

    fun getNetProfit(): Double {
        val s = _state.value
        return s.todaySales - s.todayExpenses
    }
}
