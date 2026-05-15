package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.hisab.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    @Query("SELECT * FROM suppliers WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id AND isDeleted = 0")
    suspend fun getSupplierById(id: Long): SupplierEntity?

    @Query("SELECT * FROM suppliers WHERE phone = :phone AND isDeleted = 0 LIMIT 1")
    suspend fun getSupplierByPhone(phone: String): SupplierEntity?

    @Query("SELECT * FROM suppliers WHERE (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') AND isDeleted = 0")
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>>

    @Query("SELECT SUM(totalDue) FROM suppliers WHERE isDeleted = 0")
    fun getTotalSupplierDues(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suppliers: List<SupplierEntity>)

    @Query("UPDATE suppliers SET totalDue = totalDue + :amount WHERE id = :supplierId")
    suspend fun addDue(supplierId: Long, amount: Double)

    @Query("UPDATE suppliers SET totalDue = CASE WHEN totalDue - :amount >= 0 THEN totalDue - :amount ELSE 0 END WHERE id = :supplierId")
    suspend fun removeDue(supplierId: Long, amount: Double)

    suspend fun delete(supplier: SupplierEntity) {
        deleteById(supplier.id)
    }

    @Query("UPDATE suppliers SET isDeleted = 1 WHERE id = :id")
    suspend fun deleteById(id: Long)
}
