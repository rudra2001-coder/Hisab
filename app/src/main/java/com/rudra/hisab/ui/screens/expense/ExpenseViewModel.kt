package com.rudra.hisab.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.rudra.hisab.data.local.HisabDatabase
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.local.entity.ExpenseEntity
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.ExpenseRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

enum class ExpenseFilter { ALL, TODAY, WEEK, MONTH }

data class ExpenseState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val totalForPeriod: Double = 0.0,
    val filter: ExpenseFilter = ExpenseFilter.TODAY,
    val groupedExpenses: Map<String, List<ExpenseEntity>> = emptyMap(),
    val showAddDialog: Boolean = false,
    val selectedCategory: ExpenseCategory = ExpenseCategory.OTHER,
    val amount: String = "",
    val description: String = "",
    val isSaving: Boolean = false,
    val deletedExpense: ExpenseEntity? = null,
    val showUndoSnackbar: Boolean = false,
    val receiptImageUri: String = "",
    val showDeleteConfirm: ExpenseEntity? = null,
    val isBangla: Boolean = true
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val transactionRepository: TransactionRepository,
    private val database: HisabDatabase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseState())
    val state: StateFlow<ExpenseState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            appPreferences.settings.collect { settings ->
                _state.value = _state.value.copy(isBangla = settings.languageCode == "bn")
            }
        }
        setFilter(ExpenseFilter.TODAY)
    }

    fun setFilter(filter: ExpenseFilter) {
        _state.value = _state.value.copy(filter = filter)
        loadExpenses()
    }

    private fun loadExpenses() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val now = LocalDate.now()
            val (start, end) = when (_state.value.filter) {
                ExpenseFilter.TODAY -> {
                    val s = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val e = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    Pair(s, e)
                }
                ExpenseFilter.WEEK -> {
                    val weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                    val s = weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val e = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    Pair(s, e)
                }
                ExpenseFilter.MONTH -> {
                    val monthStart = now.withDayOfMonth(1)
                    val s = monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val e = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    Pair(s, e)
                }
                ExpenseFilter.ALL -> Pair(0L, Long.MAX_VALUE)
            }

            expenseRepository.getExpensesByDateRange(start, end).collect { expenses ->
                val total = expenses.sumOf { it.amount }
                val formatter = DateTimeFormatter.ofPattern("dd MMM, EEE", Locale.forLanguageTag("bn"))
                val grouped = expenses.groupBy {
                    LocalDate.ofEpochDay(it.date / 86400000).format(formatter)
                }
                _state.value = _state.value.copy(
                    expenses = expenses,
                    totalForPeriod = total,
                    groupedExpenses = grouped
                )
            }
        }
    }

    fun showAddDialog() {
        _state.value = _state.value.copy(
            showAddDialog = true,
            selectedCategory = ExpenseCategory.OTHER,
            amount = "",
            description = ""
        )
    }

    fun hideAddDialog() {
        _state.value = _state.value.copy(showAddDialog = false)
    }

    fun setCategory(category: ExpenseCategory) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    fun setAmount(amount: String) {
        _state.value = _state.value.copy(amount = amount)
    }

    fun setDescription(desc: String) {
        _state.value = _state.value.copy(description = desc)
    }

    fun setReceiptImageUri(uri: String) {
        _state.value = _state.value.copy(receiptImageUri = uri)
    }

    fun addExpense() {
        val s = _state.value
        val amount = s.amount.toDoubleOrNull() ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            try {
                database.withTransaction {
                    expenseRepository.insert(
                        ExpenseEntity(
                            categoryId = s.selectedCategory,
                            amount = amount,
                            description = s.description,
                            date = System.currentTimeMillis()
                        )
                    )
                    transactionRepository.insert(
                        TransactionEntity(
                            type = TransactionType.EXPENSE,
                            quantity = 1.0,
                            unitPrice = amount,
                            totalAmount = amount,
                            notes = s.description
                        )
                    )
                }
                _state.value = _state.value.copy(isSaving = false, showAddDialog = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(isSaving = false)
            }
        }
    }

    fun requestDeleteExpense(expense: ExpenseEntity) {
        _state.value = _state.value.copy(showDeleteConfirm = expense)
    }

    fun confirmDeleteExpense() {
        val expense = _state.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            expenseRepository.delete(expense)
            _state.value = _state.value.copy(
                showDeleteConfirm = null,
                deletedExpense = expense,
                showUndoSnackbar = true
            )
            delay(5000)
            if (_state.value.deletedExpense?.id == expense.id) {
                _state.value = _state.value.copy(deletedExpense = null, showUndoSnackbar = false)
            }
        }
    }

    fun cancelDelete() {
        _state.value = _state.value.copy(showDeleteConfirm = null)
    }

    fun undoDelete() {
        val expense = _state.value.deletedExpense ?: return
        viewModelScope.launch {
            expenseRepository.insert(expense)
            _state.value = _state.value.copy(deletedExpense = null, showUndoSnackbar = false, showDeleteConfirm = null)
        }
    }
}
