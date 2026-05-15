package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.hisab.data.local.entity.PaymentStatus
import com.rudra.hisab.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE paymentStatus = :status ORDER BY createdAt DESC")
    fun getSalesByStatus(status: PaymentStatus): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC")
    fun getSalesByDate(startOfDay: Long, endOfDay: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<SaleEntity>>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM sales WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodaySalesTotal(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM sales WHERE createdAt >= :startDate AND createdAt <= :endDate")
    suspend fun getTotalSalesInRange(startDate: Long, endDate: Long): Double

    @Query("SELECT COALESCE(SUM(paidAmount), 0) FROM sales WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodayCollectedTotal(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(dueAmount), 0) FROM sales WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodayDueTotal(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(dueAmount), 0) FROM sales")
    suspend fun getTotalOutstandingDues(): Double

    @Query("SELECT COUNT(*) FROM sales WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodaySaleCount(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM sales WHERE createdAt >= :startDate AND createdAt <= :endDate")
    suspend fun getSaleCountInRange(startDate: Long, endDate: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: SaleEntity): Long

    @Query("UPDATE sales SET paidAmount = paidAmount + :amount, dueAmount = totalAmount - paidAmount - :amount, paymentStatus = CASE WHEN totalAmount - paidAmount - :amount <= 0 THEN 'PAID' WHEN paidAmount + :amount > 0 THEN 'PARTIAL' ELSE 'DUE' END WHERE id = :saleId")
    suspend fun addPayment(saleId: Long, amount: Double)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteById(id: Long)
}
