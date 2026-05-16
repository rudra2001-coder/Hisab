package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE productId = :productId ORDER BY createdAt DESC")
    fun getTransactionsByProduct(productId: Long): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE productId = :productId ORDER BY createdAt DESC")
    suspend fun getTransactionsByProductOnce(productId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getTransactionsByCustomer(customerId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY createdAt DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC")
    fun getTransactionsByDate(startOfDay: Long, endOfDay: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type AND createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC")
    fun getTransactionsByTypeAndDate(
        type: TransactionType,
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE type = 'SALE' AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodaySaleCount(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM transactions WHERE type = 'SALE' AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    fun getTodaySalesFlow(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM transactions WHERE type = 'SALE' AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodaySalesTotal(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM transactions WHERE type = 'PURCHASE' AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    fun getTodayPurchasesFlow(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM transactions WHERE type = 'PURCHASE' AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodayPurchasesTotal(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(paidAmount), 0) FROM transactions WHERE type = 'SALE' AND paymentType IN ('CREDIT', 'PARTIAL') AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodayCreditGiven(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(totalAmount - paidAmount), 0) FROM transactions WHERE type = 'SALE' AND paymentType IN ('CREDIT', 'PARTIAL') AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    fun getTodayCreditFlow(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount - paidAmount), 0) FROM transactions WHERE type = 'SALE' AND paymentType IN ('CREDIT', 'PARTIAL') AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTotalDueInRange(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(paidAmount), 0) FROM transactions WHERE type = 'SALE' AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTotalSalesPaidInRange(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(totalAmount - paidAmount), 0) FROM transactions WHERE type = 'SALE' AND paymentType IN ('CREDIT', 'PARTIAL')")
    fun getTotalOutstandingDues(): Flow<Double?>

    @Query("SELECT SUM(totalAmount) as revenue, COUNT(*) as count, productId FROM transactions WHERE type = 'SALE' AND productId IS NOT NULL AND createdAt >= :since GROUP BY productId ORDER BY revenue DESC LIMIT 5")
    fun getTopProducts(since: Long): Flow<List<ProductSalesSummary>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM transactions WHERE type = 'SALE' AND productId = :productId AND createdAt >= :since")
    suspend fun getProductSalesQuantity(productId: Long, since: Long): Double

    @Query("SELECT MAX(createdAt) FROM transactions WHERE type = 'SALE' AND productId = :productId")
    suspend fun getLastSaleDate(productId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class ProductSalesSummary(
    val productId: Long,
    val revenue: Double,
    val count: Int
)
