package com.rudra.hisab.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.local.entity.ExpenseEntity
import com.rudra.hisab.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class ExpenseState(
    val todayExpenses: List<ExpenseEntity> = emptyList(),
    val todayTotal: Double = 0.0,
    val showAddDialog: Boolean = false,
    val selectedCategory: ExpenseCategory = ExpenseCategory.OTHER,
    val amount: String = "",
    val description: String = "",
    val isSaving: Boolean = false
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseState())
    val state: StateFlow<ExpenseState> = _state.asStateFlow()

    init {
        loadTodayExpenses()
    }

    private fun loadTodayExpenses() {
        val now = LocalDate.now()
        val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            expenseRepository.getExpensesByDate(startOfDay, endOfDay).collect { expenses ->
                val total = expenses.sumOf { it.amount }
                _state.value = _state.value.copy(
                    todayExpenses = expenses,
                    todayTotal = total
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

    fun addExpense() {
        val s = _state.value
        val amount = s.amount.toDoubleOrNull() ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            expenseRepository.insert(
                ExpenseEntity(
                    categoryId = s.selectedCategory,
                    amount = amount,
                    description = s.description,
                    date = System.currentTimeMillis()
                )
            )
            _state.value = _state.value.copy(isSaving = false, showAddDialog = false)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.delete(expense)
        }
    }
}
