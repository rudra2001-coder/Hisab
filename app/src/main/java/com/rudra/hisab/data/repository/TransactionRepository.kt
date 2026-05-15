package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.TransactionDao
import com.rudra.hisab.data.local.dao.ProductSalesSummary
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionDao.getTransactionById(id)

    fun getTransactionsByProduct(productId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByProduct(productId)
    suspend fun getTransactionsByProductOnce(productId: Long): List<TransactionEntity> =
        transactionDao.getTransactionsByProductOnce(productId)

    fun getTransactionsByCustomer(customerId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCustomer(customerId)

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByType(type)

    fun getTransactionsByDate(startOfDay: Long, endOfDay: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByDate(startOfDay, endOfDay)

    fun getTransactionsByTypeAndDate(
        type: TransactionType,
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByTypeAndDate(type, startOfDay, endOfDay)

    suspend fun getTodaySaleCount(startOfDay: Long, endOfDay: Long): Int =
        transactionDao.getTodaySaleCount(startOfDay, endOfDay)

    fun getTodaySalesFlow(startOfDay: Long, endOfDay: Long): Flow<Double> =
        transactionDao.getTodaySalesFlow(startOfDay, endOfDay)

    suspend fun getTodaySalesTotal(startOfDay: Long, endOfDay: Long): Double =
        transactionDao.getTodaySalesTotal(startOfDay, endOfDay)

    fun getTodayPurchasesFlow(startOfDay: Long, endOfDay: Long): Flow<Double> =
        transactionDao.getTodayPurchasesFlow(startOfDay, endOfDay)

    suspend fun getTodayPurchasesTotal(startOfDay: Long, endOfDay: Long): Double =
        transactionDao.getTodayPurchasesTotal(startOfDay, endOfDay)

    suspend fun getTodayCreditGiven(startOfDay: Long, endOfDay: Long): Double =
        transactionDao.getTodayCreditGiven(startOfDay, endOfDay)

    fun getTodayCreditFlow(startOfDay: Long, endOfDay: Long): Flow<Double> =
        transactionDao.getTodayCreditFlow(startOfDay, endOfDay)

    fun getTotalOutstandingDues(): Flow<Double?> = transactionDao.getTotalOutstandingDues()

    fun getTopProducts(since: Long): Flow<List<ProductSalesSummary>> =
        transactionDao.getTopProducts(since)

    suspend fun getProductSalesQuantity(productId: Long, since: Long): Double =
        transactionDao.getProductSalesQuantity(productId, since)

    suspend fun getLastSaleDate(productId: Long): Long? =
        transactionDao.getLastSaleDate(productId)

    suspend fun insert(transaction: TransactionEntity): Long =
        transactionDao.insert(transaction)

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    suspend fun deleteById(id: Long) = transactionDao.deleteById(id)
}
