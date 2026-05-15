package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.hisab.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleItemDao {

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId ORDER BY id ASC")
    fun getItemsForSale(saleId: Long): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId ORDER BY id ASC")
    suspend fun getItemsForSaleOnce(saleId: Long): List<SaleItemEntity>

    @Query("SELECT sale_items.* FROM sale_items INNER JOIN sales ON sale_items.saleId = sales.id WHERE sale_items.productId = :productId ORDER BY sales.createdAt DESC")
    suspend fun getItemsByProduct(productId: Long): List<SaleItemEntity>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sale_items WHERE productId = :productId")
    suspend fun getTotalQuantitySold(productId: Long): Double

    @Query("SELECT COALESCE(SUM(totalPrice), 0) FROM sale_items WHERE productId = :productId")
    suspend fun getTotalRevenueForProduct(productId: Long): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SaleItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SaleItemEntity>)

    @Query("DELETE FROM sale_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteBySaleId(saleId: Long)
}
