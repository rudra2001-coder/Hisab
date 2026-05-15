package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.SaleDao
import com.rudra.hisab.data.local.entity.PaymentStatus
import com.rudra.hisab.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val saleDao: SaleDao
) {
    fun getAllSales(): Flow<List<SaleEntity>> = saleDao.getAllSales()

    suspend fun getSaleById(id: Long): SaleEntity? = saleDao.getSaleById(id)

    fun getSalesByCustomer(customerId: Long): Flow<List<SaleEntity>> =
        saleDao.getSalesByCustomer(customerId)

    fun getSalesByStatus(status: PaymentStatus): Flow<List<SaleEntity>> =
        saleDao.getSalesByStatus(status)

    fun getSalesByDate(startOfDay: Long, endOfDay: Long): Flow<List<SaleEntity>> =
        saleDao.getSalesByDate(startOfDay, endOfDay)

    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<SaleEntity>> =
        saleDao.getSalesByDateRange(startDate, endDate)

    suspend fun getTodaySalesTotal(startOfDay: Long, endOfDay: Long): Double =
        saleDao.getTodaySalesTotal(startOfDay, endOfDay)

    suspend fun getTotalSalesInRange(startDate: Long, endDate: Long): Double =
        saleDao.getTotalSalesInRange(startDate, endDate)

    suspend fun getTodayCollectedTotal(startOfDay: Long, endOfDay: Long): Double =
        saleDao.getTodayCollectedTotal(startOfDay, endOfDay)

    suspend fun getTodayDueTotal(startOfDay: Long, endOfDay: Long): Double =
        saleDao.getTodayDueTotal(startOfDay, endOfDay)

    suspend fun getTotalOutstandingDues(): Double = saleDao.getTotalOutstandingDues()

    suspend fun getTodaySaleCount(startOfDay: Long, endOfDay: Long): Int =
        saleDao.getTodaySaleCount(startOfDay, endOfDay)

    suspend fun getSaleCountInRange(startDate: Long, endDate: Long): Int =
        saleDao.getSaleCountInRange(startDate, endDate)

    suspend fun insert(sale: SaleEntity): Long = saleDao.insert(sale)

    suspend fun addPayment(saleId: Long, amount: Double) = saleDao.addPayment(saleId, amount)

    suspend fun deleteById(id: Long) = saleDao.deleteById(id)
}
