package com.rudra.hisab.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.hisab.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY totalDue DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT COUNT(*) FROM customers WHERE totalDue > 0")
    fun getDueCustomerCount(): Flow<Int>

    @Query("SELECT SUM(totalDue) FROM customers")
    fun getTotalDues(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Query("UPDATE customers SET totalDue = totalDue + :amount WHERE id = :customerId")
    suspend fun addDue(customerId: Long, amount: Double)

    @Query("UPDATE customers SET totalDue = totalDue - :amount WHERE id = :customerId")
    suspend fun removeDue(customerId: Long, amount: Double)

    @Query("UPDATE customers SET lastTransactionAt = :time WHERE id = :customerId")
    suspend fun updateLastTransaction(customerId: Long, time: Long)

    @Delete
    suspend fun delete(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteById(id: Long)
}
