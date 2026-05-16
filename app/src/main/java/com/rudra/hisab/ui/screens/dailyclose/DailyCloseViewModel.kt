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
import java.time.ZoneOffset
import javax.inject.Inject

data class DailyCloseState(
    val totalSales: Double = 0.0,
    val cashSales: Double = 0.0,
    val creditSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val cashReceived: Double = 0.0,
    val creditGiven: Double = 0.0,
    val netProfit: Double = 0.0,
    val stockValue: Double = 0.0,
    val saleCount: Int = 0,
    val newDues: Double = 0.0,
    val paymentsReceived: Double = 0.0,
    val isClosing: Boolean = false,
    val isClosed: Boolean = false,
    val alreadyClosed: Boolean = false,
    val isLoading: Boolean = true,
    val pastSnapshots: List<DailySnapshotEntity> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val showCalendar: Boolean = false,
    val selectedSnapshot: DailySnapshotEntity? = null,
    val isBangla: Boolean = true
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

    fun refresh() {
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
            val cashSales = sales - creditGiven
            val stockValue = productRepository.getTotalStockValue().first() ?: 0.0
            val saleCount = transactionRepository.getTodaySaleCount(startOfDay, endOfDay)

            val allTodayTxs = transactionRepository.getTransactionsByDate(startOfDay, endOfDay).first()
            val paymentsReceived = allTodayTxs.filter { it.type == com.rudra.hisab.data.local.entity.TransactionType.PAYMENT }
                .sumOf { it.totalAmount }
            val newDues = allTodayTxs.filter {
                it.type == com.rudra.hisab.data.local.entity.TransactionType.SALE &&
                        it.paymentType != com.rudra.hisab.data.local.entity.SalePaymentType.CASH
            }.sumOf { it.totalAmount - it.paidAmount }

            val sevenDaysAgo = now.minusDays(7)
            val pastStart = sevenDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val pastSnapshots = dailySnapshotRepository.getSnapshotsByRange(pastStart, startOfDay).first()

            _state.value = _state.value.copy(
                totalSales = sales,
                cashSales = cashSales,
                creditSales = creditGiven,
                totalExpenses = expenses,
                totalPurchases = purchases,
                cashReceived = cashSales,
                creditGiven = creditGiven,
                netProfit = sales - (expenses + purchases),
                stockValue = stockValue,
                saleCount = saleCount,
                newDues = newDues,
                paymentsReceived = paymentsReceived,
                alreadyClosed = alreadyClosed,
                pastSnapshots = pastSnapshots,
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

            val existing = dailySnapshotRepository.getSnapshotByDate(startOfDay)
            if (existing != null) {
                dailySnapshotRepository.insert(
                    existing.copy(
                        totalSales = s.totalSales,
                        totalExpenses = s.totalExpenses,
                        totalPurchases = s.totalPurchases,
                        cashReceived = s.cashReceived,
                        creditGiven = s.creditGiven,
                        netProfit = s.netProfit,
                        closingStockValue = s.stockValue,
                        createdAt = System.currentTimeMillis()
                    )
                )
            } else {
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
            }
            _state.value = _state.value.copy(isClosing = false, isClosed = true, alreadyClosed = true)
        }
    }

    fun showCalendar() {
        _state.value = _state.value.copy(showCalendar = true)
    }

    fun hideCalendar() {
        _state.value = _state.value.copy(showCalendar = false)
    }

    fun selectDate(date: LocalDate) {
        _state.value = _state.value.copy(selectedDate = date, showCalendar = false)
        viewModelScope.launch {
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val snapshot = dailySnapshotRepository.getSnapshotByDate(startOfDay)
            _state.value = _state.value.copy(selectedSnapshot = snapshot)
        }
    }
}
