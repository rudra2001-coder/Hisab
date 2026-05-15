package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.LedgerEntryDao
import com.rudra.hisab.data.local.entity.LedgerAccountType
import com.rudra.hisab.data.local.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LedgerEntryRepository @Inject constructor(
    private val ledgerEntryDao: LedgerEntryDao
) {
    fun getAllLedgerEntries(): Flow<List<LedgerEntryEntity>> =
        ledgerEntryDao.getAllLedgerEntries()

    suspend fun getLedgerEntryById(id: Long): LedgerEntryEntity? =
        ledgerEntryDao.getLedgerEntryById(id)

    fun getLedgerEntriesByAccountType(accountType: LedgerAccountType): Flow<List<LedgerEntryEntity>> =
        ledgerEntryDao.getLedgerEntriesByAccountType(accountType)

    fun getLedgerEntriesByCustomer(customerId: Long): Flow<List<LedgerEntryEntity>> =
        ledgerEntryDao.getLedgerEntriesByCustomer(customerId)

    fun getLedgerEntriesBySupplier(supplierId: Long): Flow<List<LedgerEntryEntity>> =
        ledgerEntryDao.getLedgerEntriesBySupplier(supplierId)

    fun getLedgerEntriesByDate(startOfDay: Long, endOfDay: Long): Flow<List<LedgerEntryEntity>> =
        ledgerEntryDao.getLedgerEntriesByDate(startOfDay, endOfDay)

    fun getLedgerEntriesByDateRange(startDate: Long, endDate: Long): Flow<List<LedgerEntryEntity>> =
        ledgerEntryDao.getLedgerEntriesByDateRange(startDate, endDate)

    suspend fun getCustomerBalance(accountType: LedgerAccountType, customerId: Long): Double =
        ledgerEntryDao.getCustomerBalance(accountType, customerId)

    suspend fun getSupplierBalance(accountType: LedgerAccountType, supplierId: Long): Double =
        ledgerEntryDao.getSupplierBalance(accountType, supplierId)

    suspend fun getAccountBalance(accountType: LedgerAccountType): Double =
        ledgerEntryDao.getAccountBalance(accountType)

    suspend fun insert(entry: LedgerEntryEntity): Long = ledgerEntryDao.insert(entry)

    suspend fun deleteById(id: Long) = ledgerEntryDao.deleteById(id)
}
