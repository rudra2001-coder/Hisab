package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.SaleItemDao
import com.rudra.hisab.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleItemRepository @Inject constructor(
    private val saleItemDao: SaleItemDao
) {
    fun getItemsForSale(saleId: Long): Flow<List<SaleItemEntity>> =
        saleItemDao.getItemsForSale(saleId)

    suspend fun getItemsForSaleOnce(saleId: Long): List<SaleItemEntity> =
        saleItemDao.getItemsForSaleOnce(saleId)

    suspend fun getItemsByProduct(productId: Long): List<SaleItemEntity> =
        saleItemDao.getItemsByProduct(productId)

    suspend fun getTotalQuantitySold(productId: Long): Double =
        saleItemDao.getTotalQuantitySold(productId)

    suspend fun getTotalRevenueForProduct(productId: Long): Double =
        saleItemDao.getTotalRevenueForProduct(productId)

    suspend fun insert(item: SaleItemEntity): Long = saleItemDao.insert(item)

    suspend fun insertAll(items: List<SaleItemEntity>) = saleItemDao.insertAll(items)

    suspend fun deleteById(id: Long) = saleItemDao.deleteById(id)

    suspend fun deleteBySaleId(saleId: Long) = saleItemDao.deleteBySaleId(saleId)
}
