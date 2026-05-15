package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.CustomerDao
import com.rudra.hisab.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)

    suspend fun getCustomerByPhone(phone: String): CustomerEntity? =
        customerDao.getCustomerByPhone(phone)

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> =
        customerDao.searchCustomers(query)

    fun getDueCustomers(): Flow<List<CustomerEntity>> = customerDao.getDueCustomers()

    fun getDueCustomerCount(): Flow<Int> = customerDao.getDueCustomerCount()

    fun getTotalDues(): Flow<Double?> = customerDao.getTotalDues()

    fun getCustomersOverCreditLimit(): Flow<List<CustomerEntity>> =
        customerDao.getCustomersOverCreditLimit()

    suspend fun insert(customer: CustomerEntity): Long = customerDao.insert(customer)

    suspend fun update(customer: CustomerEntity) = customerDao.update(customer)

    suspend fun addDue(customerId: Long, amount: Double) =
        customerDao.addDue(customerId, amount)

    suspend fun removeDue(customerId: Long, amount: Double) =
        customerDao.removeDue(customerId, amount)

    suspend fun updateLastTransaction(customerId: Long, time: Long) =
        customerDao.updateLastTransaction(customerId, time)

    suspend fun delete(customer: CustomerEntity) = customerDao.delete(customer)
}
