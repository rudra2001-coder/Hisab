package com.rudra.hisab.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.rudra.hisab.data.local.HisabDatabase
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.SalePaymentType
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class TransactionWithBalance(
    val transaction: TransactionEntity,
    val runningBalance: Double
)

data class CustomerListState(
    val customers: List<CustomerEntity> = emptyList(),
    val searchQuery: String = "",
    val totalDues: Double = 0.0,
    val showAddDialog: Boolean = false,
    val newCustomerName: String = "",
    val newCustomerPhone: String = "",
    val newCustomerAddress: String = "",
    val phoneError: String? = null,
    val deleteError: String? = null
)

data class CustomerDetailState(
    val customer: CustomerEntity? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val transactionsWithBalance: List<TransactionWithBalance> = emptyList(),
    val showPaymentSheet: Boolean = false,
    val paymentAmount: String = "",
    val paymentNote: String = "",
    val isSaving: Boolean = false,
    val showOverpaymentWarning: Boolean = false,
    val errorMessage: String? = null,
    val creditLimit: Double = 0.0,
    val creditLimitExceeded: Boolean = false,
    val showTransactionDeleteConfirm: TransactionEntity? = null,
    val deleteWindowHours: Int = 24
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository,
    private val database: HisabDatabase,
    private val appPreferences: AppPreferences
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
        viewModelScope.launch {
            appPreferences.settings.collect { settings ->
                _detailState.value = _detailState.value.copy(
                    creditLimit = settings.defaultCreditLimit,
                    deleteWindowHours = settings.deleteWindowHours
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _listState.value = _listState.value.copy(searchQuery = query)
    }

    fun showAddCustomerDialog() {
        _listState.value = _listState.value.copy(
            showAddDialog = true,
            newCustomerName = "",
            newCustomerPhone = "",
            newCustomerAddress = "",
            phoneError = null
        )
    }

    fun hideAddCustomerDialog() {
        _listState.value = _listState.value.copy(showAddDialog = false)
    }

    fun setNewCustomerName(name: String) {
        _listState.value = _listState.value.copy(newCustomerName = name)
    }

    fun setNewCustomerPhone(phone: String) {
        _listState.value = _listState.value.copy(newCustomerPhone = phone)
    }

    fun setNewCustomerAddress(address: String) {
        _listState.value = _listState.value.copy(newCustomerAddress = address)
    }

    fun addCustomer() {
        val s = _listState.value
        if (s.newCustomerName.isBlank()) return
        if (s.newCustomerPhone.isNotBlank() && s.newCustomerPhone.length != 11) {
            _listState.value = _listState.value.copy(phoneError = "ফোন নম্বর ১১ ডিজিটের হতে হবে")
            return
        }
        viewModelScope.launch {
            customerRepository.insert(
                CustomerEntity(
                    name = s.newCustomerName.trim(),
                    phone = s.newCustomerPhone.trim(),
                    address = s.newCustomerAddress.trim()
                )
            )
            _listState.value = _listState.value.copy(showAddDialog = false)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            try {
                customerRepository.delete(customer)
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(deleteError = "মুছতে সমস্যা হয়েছে")
            }
        }
    }

    fun loadCustomerDetail(customerId: Long) {
        viewModelScope.launch {
            val customer = customerRepository.getCustomerById(customerId)
            val limit = _detailState.value.creditLimit
            val exceeded = limit > 0 && customer != null && customer.totalDue > limit
            _detailState.value = _detailState.value.copy(
                customer = customer,
                creditLimitExceeded = exceeded
            )
        }
        viewModelScope.launch {
            transactionRepository.getTransactionsByCustomer(customerId).collect { txs ->
                val customer = _detailState.value.customer
                val balances = computeRunningBalances(txs, customer?.totalDue ?: 0.0)
                _detailState.value = _detailState.value.copy(
                    transactions = txs,
                    transactionsWithBalance = balances
                )
            }
        }
    }

    private fun computeRunningBalances(
        transactions: List<TransactionEntity>,
        currentDue: Double
    ): List<TransactionWithBalance> {
        val chronological = transactions.sortedBy { it.createdAt }
        val result = mutableListOf<TransactionWithBalance>()
        var balance = 0.0

        for (tx in chronological) {
            val change = when (tx.type) {
                TransactionType.SALE -> {
                    if (tx.paymentType == SalePaymentType.CASH) 0.0
                    else tx.totalAmount - tx.paidAmount
                }
                TransactionType.PAYMENT -> -tx.totalAmount
                else -> 0.0
            }
            balance += change
            result.add(TransactionWithBalance(tx, balance))
        }

        return result.reversed()
    }

    fun showPaymentSheet() {
        val due = _detailState.value.customer?.totalDue ?: 0.0
        _detailState.value = _detailState.value.copy(
            showPaymentSheet = true,
            paymentAmount = if (due > 0) due.toLong().toString() else "",
            paymentNote = "",
            showOverpaymentWarning = false,
            errorMessage = null
        )
    }

    fun hidePaymentSheet() {
        _detailState.value = _detailState.value.copy(
            showPaymentSheet = false,
            paymentAmount = "",
            paymentNote = "",
            showOverpaymentWarning = false,
            errorMessage = null
        )
    }

    fun setPaymentAmount(amount: String) {
        val due = _detailState.value.customer?.totalDue ?: 0.0
        val parsed = amount.toDoubleOrNull() ?: 0.0
        val overpay = parsed > due && due > 0
        _detailState.value = _detailState.value.copy(
            paymentAmount = amount,
            showOverpaymentWarning = overpay
        )
    }

    fun setPaymentNote(note: String) {
        _detailState.value = _detailState.value.copy(paymentNote = note)
    }

    fun receivePayment() {
        val s = _detailState.value
        val customer = s.customer ?: return
        val amount = s.paymentAmount.toDoubleOrNull() ?: return
        if (amount <= 0) {
            _detailState.value = _detailState.value.copy(errorMessage = "পরিমাণ শূন্য হতে পারে না")
            return
        }
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isSaving = true, errorMessage = null)
            try {
                database.withTransaction {
                    customerRepository.removeDue(customer.id, amount)
                    customerRepository.updateLastTransaction(customer.id, System.currentTimeMillis())
                    transactionRepository.insert(
                        TransactionEntity(
                            type = TransactionType.PAYMENT,
                            paymentType = SalePaymentType.CASH,
                            customerId = customer.id,
                            totalAmount = amount,
                            notes = s.paymentNote.ifBlank { "টাকা গ্রহণ" },
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
                _detailState.value = _detailState.value.copy(
                    isSaving = false,
                    showPaymentSheet = false
                )
                loadCustomerDetail(customer.id)
            } catch (e: Exception) {
                _detailState.value = _detailState.value.copy(
                    isSaving = false,
                    errorMessage = "পেমেন্ট ব্যর্থ হয়েছে: ${e.localizedMessage}"
                )
            }
        }
    }

    fun requestDeleteTransaction(transaction: TransactionEntity) {
        _detailState.value = _detailState.value.copy(showTransactionDeleteConfirm = transaction)
    }

    fun confirmDeleteTransaction() {
        val t = _detailState.value.showTransactionDeleteConfirm ?: return
        val windowMs = _detailState.value.deleteWindowHours * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        if (now - t.createdAt > windowMs) {
            _detailState.value = _detailState.value.copy(
                errorMessage = "শুধুমাত্র ${_detailState.value.deleteWindowHours} ঘন্টার মধ্যে লেনদেন মুছতে পারবেন",
                showTransactionDeleteConfirm = null
            )
            return
        }
        viewModelScope.launch {
            try {
                database.withTransaction {
                    if (t.type == TransactionType.SALE && t.paymentType != SalePaymentType.CASH) {
                        val dueAmount = t.totalAmount - t.paidAmount
                        if (t.customerId != null && dueAmount > 0) {
                            customerRepository.removeDue(t.customerId, dueAmount)
                        }
                        if (t.productId != null) {
                            com.rudra.hisab.data.local.entity.ProductEntity::class
                        }
                    }
                    if (t.type == TransactionType.PAYMENT && t.customerId != null) {
                        customerRepository.addDue(t.customerId, t.totalAmount)
                    }
                    transactionRepository.deleteById(t.id)
                }
                _detailState.value = _detailState.value.copy(showTransactionDeleteConfirm = null)
                if (t.customerId != null) loadCustomerDetail(t.customerId)
            } catch (e: Exception) {
                _detailState.value = _detailState.value.copy(
                    errorMessage = "মুছতে ব্যর্থ: ${e.localizedMessage}",
                    showTransactionDeleteConfirm = null
                )
            }
        }
    }

    fun cancelDeleteTransaction() {
        _detailState.value = _detailState.value.copy(showTransactionDeleteConfirm = null)
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
