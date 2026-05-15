package com.rudra.hisab.ui.screens.dailyclose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.DailySnapshotEntity
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
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class DailyCloseState(
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val cashReceived: Double = 0.0,
    val creditGiven: Double = 0.0,
    val netProfit: Double = 0.0,
    val stockValue: Double = 0.0,
    val isClosing: Boolean = false,
    val isClosed: Boolean = false,
    val alreadyClosed: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class DailyCloseViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val expenseRepository: ExpenseRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val dailySnapshotRepository: DailySnapshotRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DailyCloseState())
    val state: StateFlow<DailyCloseState> = _state.asStateFlow()

    init {
        loadDaySummary()
    }

    private fun loadDaySummary() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val now = LocalDate.now()
            val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val alreadyClosed = dailySnapshotRepository.hasSnapshotForDate(startOfDay)
            val sales = transactionRepository.getTodaySalesTotal(startOfDay, endOfDay)
            val purchases = transactionRepository.getTodayPurchasesTotal(startOfDay, endOfDay)
            val expenses = expenseRepository.getTodayExpensesTotal(startOfDay, endOfDay)
            val creditGiven = transactionRepository.getTodayCreditGiven(startOfDay, endOfDay)
            val cashReceived = sales - creditGiven
            val stockValue = productRepository.getTotalStockValue().first() ?: 0.0

            _state.value = _state.value.copy(
                totalSales = sales,
                totalExpenses = expenses,
                totalPurchases = purchases,
                cashReceived = cashReceived,
                creditGiven = creditGiven,
                netProfit = sales - (expenses + purchases),
                stockValue = stockValue,
                alreadyClosed = alreadyClosed,
                isLoading = false
            )
        }
    }

    fun closeDay() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isClosing = true)
            val s = _state.value
            val now = LocalDate.now()
            val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            dailySnapshotRepository.insert(
                DailySnapshotEntity(
                    date = startOfDay,
                    totalSales = s.totalSales,
                    totalExpenses = s.totalExpenses,
                    totalPurchases = s.totalPurchases,
                    cashReceived = s.cashReceived,
                    creditGiven = s.creditGiven,
                    netProfit = s.netProfit,
                    openingStockValue = s.stockValue,
                    closingStockValue = s.stockValue
                )
            )
            _state.value = _state.value.copy(isClosing = false, isClosed = true)
        }
    }
}
