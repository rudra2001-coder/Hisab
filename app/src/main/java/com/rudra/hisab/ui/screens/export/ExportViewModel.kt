package com.rudra.hisab.ui.screens.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.DailySnapshotRepository
import com.rudra.hisab.data.repository.ExpenseRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.SaleRepository
import com.rudra.hisab.util.ExportManager
import com.rudra.hisab.util.ExportFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExportUiState(
    val startDate: Long = 0L,
    val endDate: Long = System.currentTimeMillis(),
    val includeSales: Boolean = true,
    val includeInventory: Boolean = true,
    val includeCustomers: Boolean = true,
    val includeExpenses: Boolean = true,
    val selectedFormat: ExportFormat = ExportFormat.CSV,
    val isExporting: Boolean = false,
    val exportResult: String? = null,
    val exportError: String? = null
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val expenseRepository: ExpenseRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val dailySnapshotRepository: DailySnapshotRepository,
    private val exportManager: ExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun setDateRange(startDate: Long, endDate: Long) {
        _uiState.value = _uiState.value.copy(startDate = startDate, endDate = endDate)
    }

    fun toggleIncludeSales() {
        _uiState.value = _uiState.value.copy(includeSales = !_uiState.value.includeSales)
    }

    fun toggleIncludeInventory() {
        _uiState.value = _uiState.value.copy(includeInventory = !_uiState.value.includeInventory)
    }

    fun toggleIncludeCustomers() {
        _uiState.value = _uiState.value.copy(includeCustomers = !_uiState.value.includeCustomers)
    }

    fun toggleIncludeExpenses() {
        _uiState.value = _uiState.value.copy(includeExpenses = !_uiState.value.includeExpenses)
    }

    fun setFormat(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    fun exportData() {
        val state = _uiState.value
        if (state.isExporting) return

        _uiState.value = state.copy(isExporting = true, exportResult = null, exportError = null)

        viewModelScope.launch {
            try {
                val result = exportManager.generateReport(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    includeSales = state.includeSales,
                    includeInventory = state.includeInventory,
                    includeCustomers = state.includeCustomers,
                    includeExpenses = state.includeExpenses,
                    format = state.selectedFormat,
                    saleRepository = saleRepository,
                    expenseRepository = expenseRepository,
                    customerRepository = customerRepository,
                    productRepository = productRepository,
                    dailySnapshotRepository = dailySnapshotRepository
                )
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportResult = result,
                    exportError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportResult = null,
                    exportError = e.message ?: "Export failed"
                )
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(exportResult = null, exportError = null)
    }
}
