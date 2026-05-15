package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.hisab.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isDeleted = 0 ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id AND isDeleted = 0")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE currentStock <= lowStockThreshold AND isDeleted = 0 ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE currentStock <= lowStockThreshold AND isDeleted = 0 ORDER BY currentStock ASC")
    suspend fun getLowStockProductsOnce(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE (name LIKE '%' || :query || '%' OR nameBangla LIKE '%' || :query || '%') AND isDeleted = 0")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    @Query("SELECT SUM(currentStock * buyPrice) FROM products")
    fun getTotalStockValue(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    suspend fun delete(product: ProductEntity) {
        deleteById(product.id)
    }

    @Query("UPDATE products SET isDeleted = 1 WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE products SET currentStock = currentStock + :quantity WHERE id = :productId")
    suspend fun addStock(productId: Long, quantity: Double)

    @Query("UPDATE products SET currentStock = CASE WHEN currentStock - :quantity >= 0 THEN currentStock - :quantity ELSE 0 END WHERE id = :productId")
    suspend fun removeStock(productId: Long, quantity: Double)

    @Query("UPDATE products SET buyPrice = :buyPrice, sellPrice = :sellPrice WHERE id = :productId")
    suspend fun updatePrices(productId: Long, buyPrice: Double, sellPrice: Double)
}
