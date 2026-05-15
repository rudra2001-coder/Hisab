package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    fun getExpensesByDate(startOfDay: Long, endOfDay: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE date >= :startOfDay AND date < :endOfDay")
    suspend fun getTodayExpensesTotal(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE date >= :startOfDay AND date < :endOfDay")
    fun getTodayExpensesFlow(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT categoryId, COALESCE(SUM(amount), 0) as total FROM expenses WHERE date >= :startDate AND date <= :endDate GROUP BY categoryId")
    fun getExpenseBreakdown(startDate: Long, endDate: Long): Flow<List<ExpenseBreakdown>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class ExpenseBreakdown(
    val categoryId: ExpenseCategory,
    val total: Double
)
