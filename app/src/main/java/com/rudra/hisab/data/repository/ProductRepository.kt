package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.ProductDao
import com.rudra.hisab.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getProductsByCategory(categoryId: Long): Flow<List<ProductEntity>> =
        productDao.getProductsByCategory(categoryId)

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)

    fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    suspend fun getLowStockProductsOnce(): List<ProductEntity> = productDao.getLowStockProductsOnce()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    fun getProductCount(): Flow<Int> = productDao.getProductCount()

    fun getTotalStockValue(): Flow<Double?> = productDao.getTotalStockValue()

    suspend fun insert(product: ProductEntity): Long = productDao.insert(product)

    suspend fun insertAll(products: List<ProductEntity>) = productDao.insertAll(products)

    suspend fun update(product: ProductEntity) = productDao.update(product)

    suspend fun delete(product: ProductEntity) = productDao.delete(product)

    suspend fun addStock(productId: Long, quantity: Double) =
        productDao.addStock(productId, quantity)

    suspend fun removeStock(productId: Long, quantity: Double) =
        productDao.removeStock(productId, quantity)

    suspend fun updatePrices(productId: Long, buyPrice: Double, sellPrice: Double) =
        productDao.updatePrices(productId, buyPrice, sellPrice)
}
