package com.rudra.hisab.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.dao.ExpenseBreakdown
import com.rudra.hisab.data.local.dao.ProductSalesSummary
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.DailySnapshotRepository
import com.rudra.hisab.data.repository.ExpenseRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class AnalyticsState(
    val weeklyProfit: List<Double> = emptyList(),
    val topProducts: List<ProductSalesSummary> = emptyList(),
    val topProductsDetails: List<ProductEntity> = emptyList(),
    val expenseBreakdown: List<ExpenseBreakdown> = emptyList(),
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val slowMovers: List<ProductEntity> = emptyList(),
    val dueCustomers: List<Pair<String, Long>> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val expenseRepository: ExpenseRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val dailySnapshotRepository: DailySnapshotRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val now = LocalDate.now()
            val sevenDaysAgo = now.minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val thirtyDaysAgo = now.minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val fourteenDaysAgo = now.minusDays(14).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // Weekly profit from snapshots
            val weekAgo = now.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val todayEnd = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            dailySnapshotRepository.getSnapshotsByRange(weekAgo, todayEnd).collect { snapshots ->
                val profits = snapshots.sortedBy { it.date }.map { it.netProfit }
                _state.value = _state.value.copy(weeklyProfit = profits)
            }

            // Top products
            transactionRepository.getTopProducts(thirtyDaysAgo).collect { top ->
                val details = top.mapNotNull { s ->
                    productRepository.getProductById(s.productId)
                }
                _state.value = _state.value.copy(topProducts = top, topProductsDetails = details)
            }

            // Expense breakdown
            expenseRepository.getExpenseBreakdown(sevenDaysAgo, todayEnd).collect { breakdown ->
                _state.value = _state.value.copy(expenseBreakdown = breakdown)
            }

            // Low stock
            productRepository.getLowStockProducts().collect { low ->
                _state.value = _state.value.copy(lowStockProducts = low)
            }

            // Slow movers
            val allProducts = productRepository.getAllProducts().first()
            val slow = mutableListOf<ProductEntity>()
            for (product in allProducts) {
                val lastSale = transactionRepository.getLastSaleDate(product.id)
                if (lastSale == null || lastSale < fourteenDaysAgo) {
                    slow.add(product)
                }
            }
            _state.value = _state.value.copy(slowMovers = slow)

            // Due aging
            customerRepository.getAllCustomers().collect { customers ->
                val aging = customers.filter { it.totalDue > 0 }.map {
                    val daysSince = it.lastTransactionAt?.let { time ->
                        val txDate = java.time.Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate()
                        java.time.temporal.ChronoUnit.DAYS.between(txDate, now)
                    } ?: 0L

                    val bracket = when {
                        daysSince <= 7 -> "0-7 days"
                        daysSince <= 30 -> "7-30 days"
                        else -> "30+ days"
                    }
                    Pair(bracket, 1L)
                }
                _state.value = _state.value.copy(dueCustomers = aging, isLoading = false)
            }
        }
    }
}
