package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.ExpenseBreakdown
import com.rudra.hisab.data.local.dao.ExpenseDao
import com.rudra.hisab.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    suspend fun getExpenseById(id: Long): ExpenseEntity? = expenseDao.getExpenseById(id)

    fun getExpensesByDate(startOfDay: Long, endOfDay: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByDate(startOfDay, endOfDay)

    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByDateRange(startDate, endDate)

    suspend fun getTodayExpensesTotal(startOfDay: Long, endOfDay: Long): Double =
        expenseDao.getTodayExpensesTotal(startOfDay, endOfDay)

    fun getTodayExpensesFlow(startOfDay: Long, endOfDay: Long): Flow<Double> =
        expenseDao.getTodayExpensesFlow(startOfDay, endOfDay)

    fun getExpenseBreakdown(startDate: Long, endDate: Long): Flow<List<ExpenseBreakdown>> =
        expenseDao.getExpenseBreakdown(startDate, endDate)

    suspend fun insert(expense: ExpenseEntity): Long = expenseDao.insert(expense)

    suspend fun delete(expense: ExpenseEntity) = expenseDao.delete(expense)

    suspend fun deleteById(id: Long) = expenseDao.deleteById(id)
}
