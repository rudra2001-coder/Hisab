package com.rudra.hisab.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerListState(
    val customers: List<CustomerEntity> = emptyList(),
    val searchQuery: String = "",
    val totalDues: Double = 0.0
)

data class CustomerDetailState(
    val customer: CustomerEntity? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val showPaymentDialog: Boolean = false,
    val paymentAmount: String = "",
    val isSaving: Boolean = false
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(CustomerListState())
    val listState: StateFlow<CustomerListState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(CustomerDetailState())
    val detailState: StateFlow<CustomerDetailState> = _detailState.asStateFlow()

    init {
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                _listState.value = _listState.value.copy(customers = customers)
            }
        }
        viewModelScope.launch {
            customerRepository.getTotalDues().collect { dues ->
                _listState.value = _listState.value.copy(totalDues = dues ?: 0.0)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _listState.value = _listState.value.copy(searchQuery = query)
    }

    fun loadCustomerDetail(customerId: Long) {
        viewModelScope.launch {
            val customer = customerRepository.getCustomerById(customerId)
            _detailState.value = _detailState.value.copy(customer = customer)
        }
        viewModelScope.launch {
            transactionRepository.getTransactionsByCustomer(customerId).collect { txs ->
                _detailState.value = _detailState.value.copy(transactions = txs)
            }
        }
    }

    fun showPaymentDialog() {
        _detailState.value = _detailState.value.copy(showPaymentDialog = true, paymentAmount = "")
    }

    fun hidePaymentDialog() {
        _detailState.value = _detailState.value.copy(showPaymentDialog = false, paymentAmount = "")
    }

    fun setPaymentAmount(amount: String) {
        _detailState.value = _detailState.value.copy(paymentAmount = amount)
    }

    fun receivePayment() {
        val s = _detailState.value
        val customer = s.customer ?: return
        val amount = s.paymentAmount.toDoubleOrNull() ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isSaving = true)
            customerRepository.removeDue(customer.id, amount)
            customerRepository.updateLastTransaction(customer.id, System.currentTimeMillis())
            _detailState.value = _detailState.value.copy(isSaving = false, showPaymentDialog = false)
        }
    }

    fun getFilteredCustomers(): List<CustomerEntity> {
        val s = _listState.value
        return if (s.searchQuery.isBlank()) s.customers
        else s.customers.filter {
            it.name.contains(s.searchQuery, ignoreCase = true) ||
                    it.phone.contains(s.searchQuery, ignoreCase = true)
        }
    }
}
