package com.rudra.hisab.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.ProductEntity
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
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class DashboardState(
    val shopName: String = "",
    val todaySales: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val totalDues: Double = 0.0,
    val lowStockCount: Int = 0,
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val todaySaleCount: Int = 0,
    val todayPurchases: Double = 0.0,
    val todayCreditGiven: Double = 0.0,
    val quickActions: List<String> = listOf("sale", "stock", "expense", "customer"),
    val isLoading: Boolean = true,
    val dateLabel: String = "",
    val isBangla: Boolean = true
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
                val formatter = DateTimeFormatter.ofPattern(
                    if (settings.isBangla) "EEEE, dd MMMM yyyy" else "EEEE, MMMM dd, yyyy",
                    if (settings.isBangla) Locale.forLanguageTag("bn") else Locale.ENGLISH
                )
                val actions = settings.quickActions.split(",").map { it.trim() }
                _state.value = _state.value.copy(
                    shopName = settings.shopName,
                    quickActions = actions,
                    isBangla = settings.isBangla,
                    dateLabel = now.format(formatter)
                )
            }
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val salesFlow = transactionRepository.getTodaySalesFlow(startOfDay, endOfDay)
            val expensesFlow = expenseRepository.getTodayExpensesFlow(startOfDay, endOfDay)
            val duesFlow = customerRepository.getTotalDues()
            val lowStockFlow = productRepository.getLowStockProducts()
            val purchasesFlow = transactionRepository.getTodayPurchasesFlow(startOfDay, endOfDay)
            val creditFlow = transactionRepository.getTodayCreditFlow(startOfDay, endOfDay)
            val saleCount = transactionRepository.getTodaySaleCount(startOfDay, endOfDay)

            combine(
                salesFlow, expensesFlow, duesFlow, lowStockFlow, purchasesFlow, creditFlow
            ) { args: Array<Any?> ->
                val sales = args[0] as Double
                val expenses = args[1] as Double
                val dues = args[2] as Double?
                @Suppress("UNCHECKED_CAST")
                val lowStock = args[3] as List<ProductEntity>
                val purchases = args[4] as Double
                val credit = args[5] as Double
                _state.value.copy(
                    todaySales = sales,
                    todayExpenses = expenses,
                    totalDues = dues ?: 0.0,
                    lowStockCount = lowStock.size,
                    lowStockProducts = lowStock,
                    todayPurchases = purchases,
                    todayCreditGiven = credit,
                    todaySaleCount = saleCount,
                    isLoading = false
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }
}
