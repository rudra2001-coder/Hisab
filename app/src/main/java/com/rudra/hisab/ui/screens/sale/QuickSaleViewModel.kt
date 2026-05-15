package com.rudra.hisab.ui.screens.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.HisabDatabase
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.PaymentType
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickSaleState(
    val products: List<ProductEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val selectedProduct: ProductEntity? = null,
    val quantity: String = "1",
    val paymentType: PaymentType = PaymentType.CASH,
    val paidAmount: String = "",
    val selectedCustomer: CustomerEntity? = null,
    val customerSearchQuery: String = "",
    val isSaving: Boolean = false,
    val saleComplete: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val showLowStockWarning: Boolean = false
) {
    val quantityDouble: Double get() = quantity.toDoubleOrNull() ?: 0.0
    val totalPrice: Double get() {
        val p = selectedProduct ?: return 0.0
        return p.sellPrice * quantityDouble
    }
    val profit: Double get() {
        val p = selectedProduct ?: return 0.0
        return (p.sellPrice - p.buyPrice) * quantityDouble
    }
    val dueAmount: Double get() = when (paymentType) {
        PaymentType.CASH -> 0.0
        PaymentType.CREDIT -> totalPrice
        PaymentType.PARTIAL -> {
            val paid = paidAmount.toDoubleOrNull() ?: 0.0
            (totalPrice - paid).coerceAtLeast(0.0)
        }
    }
    val filteredCustomers: List<CustomerEntity>
        get() = if (customerSearchQuery.isBlank()) customers
        else customers.filter {
            it.name.contains(customerSearchQuery, ignoreCase = true) ||
                    it.phone.contains(customerSearchQuery, ignoreCase = true)
        }
    val isCustomerSelectionRequired: Boolean get() = paymentType != PaymentType.CASH
}

@HiltViewModel
class QuickSaleViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val customerRepository: CustomerRepository,
    private val database: HisabDatabase
) : ViewModel() {

    private val _state = MutableStateFlow(QuickSaleState())
    val state: StateFlow<QuickSaleState> = _state.asStateFlow()

    private var isProcessing = false

    init {
        viewModelScope.launch {
            combine(
                productRepository.getAllProducts(),
                customerRepository.getAllCustomers()
            ) { products, customers ->
                Pair(products, customers)
            }.collect { (products, customers) ->
                _state.value = _state.value.copy(
                    products = products,
                    customers = customers
                )
            }
        }
    }

    fun selectProduct(product: ProductEntity) {
        _state.value = _state.value.copy(
            selectedProduct = product,
            quantity = "1",
            paidAmount = "",
            paymentType = PaymentType.CASH,
            selectedCustomer = null,
            customerSearchQuery = "",
            saleComplete = false,
            errorMessage = null,
            showLowStockWarning = false
        )
    }

    fun clearSelection() {
        _state.value = _state.value.copy(
            selectedProduct = null,
            quantity = "1",
            paidAmount = "",
            paymentType = PaymentType.CASH,
            selectedCustomer = null,
            customerSearchQuery = "",
            saleComplete = false,
            errorMessage = null
        )
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun setPaymentType(type: PaymentType) {
        val s = _state.value
        val newPaid = when (type) {
            PaymentType.CASH -> s.totalPrice.toString()
            PaymentType.CREDIT -> "0"
            PaymentType.PARTIAL -> ""
        }
        _state.value = _state.value.copy(
            paymentType = type,
            paidAmount = newPaid
        )
    }

    fun setPaidAmount(amount: String) {
        if (amount.length <= 8) {
            _state.value = _state.value.copy(paidAmount = amount)
        }
    }

    fun selectCustomer(customer: CustomerEntity?) {
        _state.value = _state.value.copy(selectedCustomer = customer)
    }

    fun setCustomerSearchQuery(query: String) {
        _state.value = _state.value.copy(customerSearchQuery = query)
    }

    fun appendDigit(digit: String) {
        val s = _state.value
        if (digit == "." && s.quantity.contains(".")) return
        val newQty = s.quantity + digit
        val parsed = newQty.toDoubleOrNull()
        if (parsed != null && parsed > 0 && newQty.length <= 8) {
            val product = s.selectedProduct
            if (product != null && parsed > product.currentStock) {
                _state.value = _state.value.copy(
                    quantity = product.currentStock.toInt().toString(),
                    errorMessage = "সর্বোচ্চ ${product.currentStock.toInt()} ${product.unit.replace("piece","পিস").replace("kg","কেজি")} বিক্রি করতে পারবেন"
                )
            } else {
                _state.value = _state.value.copy(quantity = newQty)
            }
        }
    }

    fun backspaceQuantity() {
        val s = _state.value
        if (s.quantity.length > 1) {
            _state.value = _state.value.copy(quantity = s.quantity.dropLast(1))
        } else {
            _state.value = _state.value.copy(quantity = "1")
        }
    }

    fun clearQuantity() {
        _state.value = _state.value.copy(quantity = "1")
    }

    fun setQuantityDirect(qty: String) {
        if (qty.length <= 8) {
            _state.value = _state.value.copy(quantity = qty)
        }
    }

    fun createAndSelectCustomer(name: String, phone: String) {
        viewModelScope.launch {
            val existing = customerRepository.getCustomerByPhone(phone)
            if (existing != null) {
                _state.value = _state.value.copy(selectedCustomer = existing)
                return@launch
            }
            val id = customerRepository.insert(
                CustomerEntity(name = name, phone = phone)
            )
            val created = customerRepository.getCustomerById(id)
            _state.value = _state.value.copy(selectedCustomer = created)
        }
    }

    fun completeSale() {
        if (isProcessing) return
        isProcessing = true

        val s = _state.value
        val product = s.selectedProduct ?: run { isProcessing = false; return }
        val qty = s.quantityDouble
        if (qty <= 0) {
            _state.value = _state.value.copy(errorMessage = "পরিমাণ শূন্য হতে পারে না")
            isProcessing = false
            return
        }
        if (qty > product.currentStock) {
            _state.value = _state.value.copy(
                errorMessage = "সর্বোচ্চ ${product.currentStock.toInt()} ${product.unit.replace("piece","পিস").replace("kg","কেজি")} বিক্রি করতে পারবেন"
            )
            isProcessing = false
            return
        }
        if (s.isCustomerSelectionRequired && s.selectedCustomer == null) {
            _state.value = _state.value.copy(errorMessage = "গ্রাহক নির্বাচন করুন")
            isProcessing = false
            return
        }

        val totalAmount = s.totalPrice
        val paidAmount = when (s.paymentType) {
            PaymentType.CASH -> totalAmount
            PaymentType.CREDIT -> 0.0
            PaymentType.PARTIAL -> s.paidAmount.toDoubleOrNull() ?: 0.0
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)
            try {
                database.withTransaction {
                    productRepository.removeStock(product.id, qty)
                    transactionRepository.insert(
                        TransactionEntity(
                            type = TransactionType.SALE,
                            paymentType = s.paymentType,
                            productId = product.id,
                            customerId = s.selectedCustomer?.id,
                            quantity = qty,
                            unitPrice = product.sellPrice,
                            totalAmount = totalAmount,
                            paidAmount = paidAmount
                        )
                    )
                    if (s.selectedCustomer != null && s.paymentType != PaymentType.CASH) {
                        val dueAmount = totalAmount - paidAmount
                        if (dueAmount > 0) {
                            customerRepository.addDue(s.selectedCustomer.id, dueAmount)
                        }
                        customerRepository.updateLastTransaction(s.selectedCustomer.id, System.currentTimeMillis())
                    }
                }
                _state.value = _state.value.copy(
                    isSaving = false,
                    saleComplete = true
                )
                val remainingStock = product.currentStock - qty
                if (remainingStock <= product.lowStockThreshold) {
                    _state.value = _state.value.copy(showLowStockWarning = true)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = "বিক্রয় ব্যর্থ হয়েছে: ${e.localizedMessage}"
                )
            } finally {
                isProcessing = false
            }
        }
    }
        if (qty > product.currentStock) {
            _state.value = _state.value.copy(errorMessage = "সর্বোচ্চ ${product.currentStock.toInt()} ${product.unit} বিক্রি করতে পারবেন")
            return
        }
        if (s.isCustomerSelectionRequired && s.selectedCustomer == null) {
            _state.value = _state.value.copy(errorMessage = "গ্রাহক নির্বাচন করুন")
            return
        }

        val totalAmount = s.totalPrice
        val paidAmount = when (s.paymentType) {
            PaymentType.CASH -> totalAmount
            PaymentType.CREDIT -> 0.0
            PaymentType.PARTIAL -> s.paidAmount.toDoubleOrNull() ?: 0.0
        }

        isProcessing = true
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)
            try {
                database.withTransaction {
                    productRepository.removeStock(product.id, qty)
                    transactionRepository.insert(
                        TransactionEntity(
                            type = TransactionType.SALE,
                            paymentType = s.paymentType,
                            productId = product.id,
                            customerId = s.selectedCustomer?.id,
                            quantity = qty,
                            unitPrice = product.sellPrice,
                            totalAmount = totalAmount,
                            paidAmount = paidAmount
                        )
                    )
                    if (s.selectedCustomer != null && s.paymentType != PaymentType.CASH) {
                        val dueAmount = totalAmount - paidAmount
                        if (dueAmount > 0) {
                            customerRepository.addDue(s.selectedCustomer.id, dueAmount)
                        }
                        customerRepository.updateLastTransaction(s.selectedCustomer.id, System.currentTimeMillis())
                    }
                }
                _state.value = _state.value.copy(
                    isSaving = false,
                    saleComplete = true
                )
                checkLowStockAndNotify(product)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = "বিক্রয় ব্যর্থ হয়েছে: ${e.localizedMessage}"
                )
            } finally {
                isProcessing = false
            }
        }
    }

    private fun checkLowStockAndNotify(product: ProductEntity) {
        // TODO: Implement low stock notification
    }

    fun resetAfterSale() {
        clearSelection()
    }

    fun dismissLowStockWarning() {
        _state.value = _state.value.copy(showLowStockWarning = false)
    }
}
