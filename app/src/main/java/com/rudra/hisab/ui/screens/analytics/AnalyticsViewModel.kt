package com.rudra.hisab.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.dao.ExpenseBreakdown
import com.rudra.hisab.data.local.dao.ProductSalesSummary
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.DailySnapshotEntity
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.DailySnapshotRepository
import com.rudra.hisab.data.repository.ExpenseRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import com.rudra.hisab.util.ReportData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AnalyticsState(
    val weeklyProfit: List<Double> = emptyList(),
    val topProducts: List<ProductSalesSummary> = emptyList(),
    val topProductsDetails: List<ProductEntity> = emptyList(),
    val expenseBreakdown: List<ExpenseBreakdown> = emptyList(),
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val slowMovers: List<ProductEntity> = emptyList(),
    val dueCustomers: List<Pair<String, Long>> = emptyList(),
    val isLoading: Boolean = true,
    val customerOutstanding: List<CustomerEntity> = emptyList(),
    val productProfit: List<Pair<ProductEntity, Double>> = emptyList(),
    val monthlyGrowth: List<Pair<String, Double>> = emptyList(),
    val cashFlow: List<Pair<String, Double>> = emptyList(),
    val customStartDate: LocalDate = LocalDate.now().minusDays(30),
    val customEndDate: LocalDate = LocalDate.now(),
    val showDatePicker: Boolean = false
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val expenseRepository: ExpenseRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val dailySnapshotRepository: DailySnapshotRepository,
    private val appPreferences: AppPreferences
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

            val weekAgo = now.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val todayEnd = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            dailySnapshotRepository.getSnapshotsByRange(weekAgo, todayEnd).collect { snapshots ->
                val profits = snapshots.sortedBy { it.date }.map { it.netProfit }
                _state.value = _state.value.copy(weeklyProfit = profits)
            }

            transactionRepository.getTopProducts(thirtyDaysAgo).collect { top ->
                val details = top.mapNotNull { s -> productRepository.getProductById(s.productId) }
                _state.value = _state.value.copy(topProducts = top, topProductsDetails = details)
            }

            expenseRepository.getExpenseBreakdown(sevenDaysAgo, todayEnd).collect { breakdown ->
                _state.value = _state.value.copy(expenseBreakdown = breakdown)
            }

            productRepository.getLowStockProducts().collect { low ->
                _state.value = _state.value.copy(lowStockProducts = low)
            }

            val allProducts = productRepository.getAllProducts().first()
            val slow = mutableListOf<ProductEntity>()
            for (product in allProducts) {
                val lastSale = transactionRepository.getLastSaleDate(product.id)
                if (lastSale == null || lastSale < fourteenDaysAgo) {
                    slow.add(product)
                }
            }
            _state.value = _state.value.copy(slowMovers = slow)

            customerRepository.getAllCustomers().collect { customers ->
                val aging = customers.filter { it.totalDue > 0 }.map {
                    val daysSince = it.lastTransactionAt?.let { time ->
                        val txDate = java.time.Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate()
                        java.time.temporal.ChronoUnit.DAYS.between(txDate, now)
                    } ?: 0L
                    val bracket = when { daysSince <= 7 -> "0-7 days"; daysSince <= 30 -> "7-30 days"; else -> "30+ days" }
                    Pair(bracket, 1L)
                }
                _state.value = _state.value.copy(
                    dueCustomers = aging,
                    customerOutstanding = customers.filter { it.totalDue > 0 }.sortedByDescending { it.totalDue },
                    isLoading = false
                )
            }

            loadProductProfit(thirtyDaysAgo)
            loadMonthlyGrowth()
            loadCashFlow(thirtyDaysAgo, todayEnd)
        }
    }

    private suspend fun loadProductProfit(since: Long) {
        val products = productRepository.getAllProducts().first()
        val profitList = products.map { product ->
            val qty = transactionRepository.getProductSalesQuantity(product.id, since)
            val profit = (product.sellPrice - product.buyPrice) * qty
            product to profit
        }.sortedByDescending { it.second }
        _state.value = _state.value.copy(productProfit = profitList)
    }

    private suspend fun loadMonthlyGrowth() {
        val now = LocalDate.now()
        val growth = mutableListOf<Pair<String, Double>>()
        for (i in 0 until 6) {
            val monthStart = now.minusMonths(i.toLong()).withDayOfMonth(1)
            val monthEnd = monthStart.plusMonths(1).minusDays(1)
            val start = monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = monthEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val snapshots = dailySnapshotRepository.getSnapshotsByRange(start, end).first()
            val total = snapshots.sumOf { it.netProfit }
            val label = SimpleDateFormat("MMM yy", Locale.forLanguageTag("bn")).format(Date(start))
            growth.add(label to total)
        }
        _state.value = _state.value.copy(monthlyGrowth = growth.reversed())
    }

    private suspend fun loadCashFlow(start: Long, end: Long) {
        val snapshots = dailySnapshotRepository.getSnapshotsByRange(start, end).first()
        val flow = snapshots.sortedBy { it.date }.map { snap ->
            val date = SimpleDateFormat("dd MMM", Locale.forLanguageTag("bn")).format(Date(snap.date))
            date to (snap.totalSales - snap.totalExpenses)
        }
        _state.value = _state.value.copy(cashFlow = flow)
    }

    fun setCustomStartDate(date: LocalDate) {
        _state.value = _state.value.copy(customStartDate = date)
    }

    fun setCustomEndDate(date: LocalDate) {
        _state.value = _state.value.copy(customEndDate = date)
    }

    fun refreshWithCustomRange() {
        viewModelScope.launch {
            val start = _state.value.customStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = _state.value.customEndDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            loadAnalytics()
        }
    }

    fun showDatePicker() { _state.value = _state.value.copy(showDatePicker = true) }
    fun hideDatePicker() { _state.value = _state.value.copy(showDatePicker = false) }

    suspend fun buildReportData(): ReportData {
        val settings = appPreferences.settings.first()
        val startDate = _state.value.customStartDate
        val endDate = _state.value.customEndDate
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        val totalSales = transactionRepository.getTodaySalesTotal(startMillis, endMillis)
        val totalPurchases = transactionRepository.getTodayPurchasesTotal(startMillis, endMillis)
        val totalExpenses = expenseRepository.getTodayExpensesTotal(startMillis, endMillis)
        val snapshots = dailySnapshotRepository.getSnapshotsByRange(startMillis, endMillis).first()
        val topProducts = transactionRepository.getTopProducts(startMillis).first()
        val topProductsDetails = topProducts.mapNotNull { productRepository.getProductById(it.productId) }

        return ReportData(
            shopName = settings.shopName,
            startDate = dateFormat.format(Date(startMillis)),
            endDate = dateFormat.format(Date(endMillis)),
            totalSales = totalSales,
            totalExpenses = totalExpenses,
            totalPurchases = totalPurchases,
            netProfit = totalSales - totalExpenses,
            cashReceived = totalSales - transactionRepository.getTodayCreditGiven(startMillis, endMillis),
            creditGiven = transactionRepository.getTodayCreditGiven(startMillis, endMillis),
            saleCount = transactionRepository.getTodaySaleCount(startMillis, endMillis),
            topProducts = topProductsDetails.zip(topProducts).map { (p, s) -> p.nameBangla to s.revenue },
            dailyBreakdown = snapshots
        )
    }
}
