package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.hisab.data.local.entity.PaymentEntity
import com.rudra.hisab.data.local.entity.PaymentMethod
import com.rudra.hisab.data.local.entity.PaymentType
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): PaymentEntity?

    @Query("SELECT * FROM payments WHERE type = :type ORDER BY createdAt DESC")
    fun getPaymentsByType(type: PaymentType): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getPaymentsByCustomer(customerId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE supplierId = :supplierId ORDER BY createdAt DESC")
    fun getPaymentsBySupplier(supplierId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC")
    fun getPaymentsByDate(startOfDay: Long, endOfDay: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getPaymentsByDateRange(startDate: Long, endDate: Long): Flow<List<PaymentEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE type = 'RECEIVED' AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodayReceivedTotal(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE type = 'PAID' AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getTodayPaidTotal(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE type = 'RECEIVED' AND createdAt >= :startDate AND createdAt <= :endDate")
    suspend fun getTotalReceivedInRange(startDate: Long, endDate: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE type = 'PAID' AND createdAt >= :startDate AND createdAt <= :endDate")
    suspend fun getTotalPaidInRange(startDate: Long, endDate: Long): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity): Long

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deleteById(id: Long)
}
