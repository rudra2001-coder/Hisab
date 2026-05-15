package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.hisab.data.local.entity.StockTransactionEntity
import com.rudra.hisab.data.local.entity.StockTransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransactionDao {

    @Query("SELECT * FROM stock_transactions ORDER BY createdAt DESC")
    fun getAllStockTransactions(): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY createdAt DESC")
    fun getStockTransactionsByProduct(productId: Long): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE type = :type ORDER BY createdAt DESC")
    fun getStockTransactionsByType(type: StockTransactionType): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC")
    fun getStockTransactionsByDate(startOfDay: Long, endOfDay: Long): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getStockTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId AND createdAt >= :since ORDER BY createdAt DESC")
    suspend fun getStockTransactionsSince(productId: Long, since: Long): List<StockTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: StockTransactionEntity): Long

    @Query("DELETE FROM stock_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
