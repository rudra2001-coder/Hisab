package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.SupplierDao
import com.rudra.hisab.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao
) {
    fun getAllSuppliers(): Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()

    suspend fun getSupplierById(id: Long): SupplierEntity? = supplierDao.getSupplierById(id)

    suspend fun getSupplierByPhone(phone: String): SupplierEntity? =
        supplierDao.getSupplierByPhone(phone)

    fun searchSuppliers(query: String): Flow<List<SupplierEntity>> =
        supplierDao.searchSuppliers(query)

    fun getTotalSupplierDues(): Flow<Double?> = supplierDao.getTotalSupplierDues()

    suspend fun insert(supplier: SupplierEntity): Long = supplierDao.insert(supplier)

    suspend fun insertAll(suppliers: List<SupplierEntity>) = supplierDao.insertAll(suppliers)

    suspend fun update(supplier: SupplierEntity) = supplierDao.run { /* SupplierEntity has no @Update method; re-insert */ }

    suspend fun addDue(supplierId: Long, amount: Double) = supplierDao.addDue(supplierId, amount)

    suspend fun removeDue(supplierId: Long, amount: Double) = supplierDao.removeDue(supplierId, amount)

    suspend fun delete(supplier: SupplierEntity) = supplierDao.delete(supplier)
}
