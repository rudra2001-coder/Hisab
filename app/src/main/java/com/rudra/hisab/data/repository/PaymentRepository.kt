package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.PaymentDao
import com.rudra.hisab.data.local.entity.PaymentEntity
import com.rudra.hisab.data.local.entity.PaymentType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao
) {
    fun getAllPayments(): Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    suspend fun getPaymentById(id: Long): PaymentEntity? = paymentDao.getPaymentById(id)

    fun getPaymentsByType(type: PaymentType): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsByType(type)

    fun getPaymentsByCustomer(customerId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsByCustomer(customerId)

    fun getPaymentsBySupplier(supplierId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsBySupplier(supplierId)

    fun getPaymentsByDate(startOfDay: Long, endOfDay: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsByDate(startOfDay, endOfDay)

    fun getPaymentsByDateRange(startDate: Long, endDate: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsByDateRange(startDate, endDate)

    suspend fun getTodayReceivedTotal(startOfDay: Long, endOfDay: Long): Double =
        paymentDao.getTodayReceivedTotal(startOfDay, endOfDay)

    suspend fun getTodayPaidTotal(startOfDay: Long, endOfDay: Long): Double =
        paymentDao.getTodayPaidTotal(startOfDay, endOfDay)

    suspend fun getTotalReceivedInRange(startDate: Long, endDate: Long): Double =
        paymentDao.getTotalReceivedInRange(startDate, endDate)

    suspend fun getTotalPaidInRange(startDate: Long, endDate: Long): Double =
        paymentDao.getTotalPaidInRange(startDate, endDate)

    suspend fun insert(payment: PaymentEntity): Long = paymentDao.insert(payment)

    suspend fun deleteById(id: Long) = paymentDao.deleteById(id)
}
