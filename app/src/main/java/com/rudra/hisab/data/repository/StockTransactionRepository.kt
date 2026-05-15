package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.StockTransactionDao
import com.rudra.hisab.data.local.entity.StockTransactionEntity
import com.rudra.hisab.data.local.entity.StockTransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockTransactionRepository @Inject constructor(
    private val stockTransactionDao: StockTransactionDao
) {
    fun getAllStockTransactions(): Flow<List<StockTransactionEntity>> =
        stockTransactionDao.getAllStockTransactions()

    fun getStockTransactionsByProduct(productId: Long): Flow<List<StockTransactionEntity>> =
        stockTransactionDao.getStockTransactionsByProduct(productId)

    fun getStockTransactionsByType(type: StockTransactionType): Flow<List<StockTransactionEntity>> =
        stockTransactionDao.getStockTransactionsByType(type)

    fun getStockTransactionsByDate(startOfDay: Long, endOfDay: Long): Flow<List<StockTransactionEntity>> =
        stockTransactionDao.getStockTransactionsByDate(startOfDay, endOfDay)

    fun getStockTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<StockTransactionEntity>> =
        stockTransactionDao.getStockTransactionsByDateRange(startDate, endDate)

    suspend fun getStockTransactionsSince(productId: Long, since: Long): List<StockTransactionEntity> =
        stockTransactionDao.getStockTransactionsSince(productId, since)

    suspend fun insert(transaction: StockTransactionEntity): Long =
        stockTransactionDao.insert(transaction)

    suspend fun deleteById(id: Long) = stockTransactionDao.deleteById(id)
}
