package com.rudra.hisab.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.DailySnapshotRepository
import com.rudra.hisab.data.repository.ExpenseRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import com.rudra.hisab.util.ExportFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SalesReportData(
    val totalSales: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalDue: Double = 0.0,
    val saleCount: Int = 0,
    val avgSaleValue: Double = 0.0
)

data class ExpenseReportData(
    val totalExpenses: Double = 0.0,
    val breakdown: Map<ExpenseCategory, Double> = emptyMap()
)

data class ProfitLossData(
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMargin: Double = 0.0
)

data class ReportsUiState(
    val startDate: Long = 0L,
    val endDate: Long = System.currentTimeMillis(),
    val salesReport: SalesReportData = SalesReportData(),
    val expenseReport: ExpenseReportData = ExpenseReportData(),
    val profitLoss: ProfitLossData = ProfitLossData(),
    val totalDues: Double = 0.0,
    val dueCustomerCount: Int = 0,
    val lowStockCount: Int = 0,
    val totalStockValue: Double = 0.0,
    val isLoading: Boolean = false,
    val selectedFormat: ExportFormat = ExportFormat.CSV,
    val isExporting: Boolean = false,
    val isBangla: Boolean = true
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val expenseRepository: ExpenseRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val dailySnapshotRepository: DailySnapshotRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = appPreferences.settings.first()
            _uiState.value = _uiState.value.copy(isBangla = settings.isBangla)
        }
    }

    fun setDateRange(startDate: Long, endDate: Long) {
        _uiState.value = _uiState.value.copy(startDate = startDate, endDate = endDate)
        loadReports()
    }

    private fun loadReports() {
        val start = _uiState.value.startDate
        val end = _uiState.value.endDate
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            // Sales — suspend snapshot from transactions table
            val totalSales = transactionRepository.getTodaySalesTotal(start, end)
            val saleCount = transactionRepository.getTodaySaleCount(start, end)
            val totalDueInRange = transactionRepository.getTotalDueInRange(start, end)

            // Expenses — collect once
            val expenseList = expenseRepository.getExpensesByDateRange(start, end)
                .first()   // take the latest emitted list, then return
            val totalExpenses = expenseList.sumOf { it.amount }
            val breakdown = expenseList.groupBy { it.categoryId }
                .mapValues { (_, list) -> list.sumOf { it.amount } }

            val netProfit = totalSales - totalExpenses
            val margin = if (totalSales > 0) (netProfit / totalSales) * 100 else 0.0

            // Customer dues — suspend snapshot
            val allCustomers = customerRepository.getAllCustomers().first()
            val dues = allCustomers.sumOf { it.totalDue }
            val dueCount = allCustomers.count { it.totalDue > 0 }

            // Low stock — suspend snapshot
            val lowStock = productRepository.getLowStockProductsOnce()
            val stockValue = productRepository.getTotalStockValue().first() ?: 0.0

            _uiState.value = _uiState.value.copy(
                salesReport = SalesReportData(
                    totalSales = totalSales,
                    totalPaid = totalSales - totalDueInRange,
                    totalDue = totalDueInRange,
                    saleCount = saleCount,
                    avgSaleValue = if (saleCount > 0) totalSales / saleCount else 0.0
                ),
                expenseReport = ExpenseReportData(
                    totalExpenses = totalExpenses,
                    breakdown = breakdown
                ),
                profitLoss = ProfitLossData(
                    totalRevenue = totalSales,
                    totalExpenses = totalExpenses,
                    netProfit = netProfit,
                    profitMargin = margin
                ),
                totalDues = dues,
                dueCustomerCount = dueCount,
                lowStockCount = lowStock.size,
                totalStockValue = stockValue,
                isLoading = false
            )
        }
    }

    fun setFormat(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    fun exportData() {
        // TODO: Implement export functionality
    }
}
