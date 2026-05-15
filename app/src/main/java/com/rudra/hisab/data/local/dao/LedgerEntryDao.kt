package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.hisab.data.local.entity.LedgerAccountType
import com.rudra.hisab.data.local.entity.LedgerEntryEntity
import com.rudra.hisab.data.local.entity.LedgerEntryType
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerEntryDao {

    @Query("SELECT * FROM ledger_entries ORDER BY createdAt DESC")
    fun getAllLedgerEntries(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE id = :id")
    suspend fun getLedgerEntryById(id: Long): LedgerEntryEntity?

    @Query("SELECT * FROM ledger_entries WHERE accountType = :accountType ORDER BY createdAt DESC")
    fun getLedgerEntriesByAccountType(accountType: LedgerAccountType): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getLedgerEntriesByCustomer(customerId: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE supplierId = :supplierId ORDER BY createdAt DESC")
    fun getLedgerEntriesBySupplier(supplierId: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC")
    fun getLedgerEntriesByDate(startOfDay: Long, endOfDay: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt ASC")
    fun getLedgerEntriesByDateRange(startDate: Long, endDate: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT COALESCE(SUM(CASE WHEN entryType = 'DEBIT' THEN amount ELSE -amount END), 0) FROM ledger_entries WHERE accountType = :accountType AND customerId = :customerId")
    suspend fun getCustomerBalance(accountType: LedgerAccountType, customerId: Long): Double

    @Query("SELECT COALESCE(SUM(CASE WHEN entryType = 'DEBIT' THEN amount ELSE -amount END), 0) FROM ledger_entries WHERE accountType = :accountType AND supplierId = :supplierId")
    suspend fun getSupplierBalance(accountType: LedgerAccountType, supplierId: Long): Double

    @Query("SELECT COALESCE(SUM(CASE WHEN entryType = 'DEBIT' THEN amount ELSE -amount END), 0) FROM ledger_entries WHERE accountType = :accountType")
    suspend fun getAccountBalance(accountType: LedgerAccountType): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LedgerEntryEntity): Long

    @Query("DELETE FROM ledger_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
