package com.rudra.hisab.ui.screens.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.rudra.hisab.data.local.HisabDatabase
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.PaymentStatus
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class CartItem(
    val product: ProductEntity,
    val quantity: Double
) {
    val totalPrice: Double get() = product.sellPrice * quantity
    val profit: Double get() = (product.sellPrice - product.buyPrice) * quantity
}

data class QuickSaleState(
    val products: List<ProductEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val recentCustomers: List<CustomerEntity> = emptyList(),
    val selectedProduct: ProductEntity? = null,
    val quantity: String = "1",
    val paymentType: PaymentStatus = PaymentStatus.CASH,
    val paidAmount: String = "",
    val selectedCustomer: CustomerEntity? = null,
    val customerSearchQuery: String = "",
    val isSaving: Boolean = false,
    val saleComplete: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val showLowStockWarning: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val cartMode: Boolean = false,
    val todayTransactions: List<TransactionEntity> = emptyList(),
    val todayProducts: Map<Long, ProductEntity> = emptyMap(),
    val showHistoryTab: Boolean = false,
    val isBangla: Boolean = true
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
        PaymentStatus.CASH -> 0.0
        PaymentStatus.CREDIT -> totalPrice
        PaymentStatus.PARTIAL -> {
            val paid = paidAmount.toDoubleOrNull() ?: 0.0
            (totalPrice - paid).coerceAtLeast(0.0)
        }
    }
    val cartTotal: Double get() = cartItems.sumOf { it.totalPrice }
    val cartProfit: Double get() = cartItems.sumOf { it.profit }
    val cartCount: Int get() = cartItems.size
    val filteredCustomers: List<CustomerEntity>
        get() = if (customerSearchQuery.isBlank()) customers
        else customers.filter {
            it.name.contains(customerSearchQuery, ignoreCase = true) ||
                    it.phone.contains(customerSearchQuery, ignoreCase = true)
        }
    val isCustomerSelectionRequired: Boolean get() = paymentType != PaymentStatus.CASH

    fun isInCart(productId: Long): Boolean = cartItems.any { it.product.id == productId }
    fun getCartItem(productId: Long): CartItem? = cartItems.find { it.product.id == productId }
}

@HiltViewModel
class QuickSaleViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val customerRepository: CustomerRepository,
    private val database: HisabDatabase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(QuickSaleState())
    val state: StateFlow<QuickSaleState> = _state.asStateFlow()

    private var isProcessing = false
    private var lastHapticTime = 0L

    init {
        val now = LocalDate.now()
        val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            combine(
                productRepository.getAllProducts(),
                customerRepository.getAllCustomers(),
                transactionRepository.getTransactionsByDate(startOfDay, endOfDay),
                appPreferences.settings
            ) { products, customers, transactions, settings ->
                val recent = customers
                    .filter { it.lastTransactionAt != null }
                    .sortedByDescending { it.lastTransactionAt }
                    .take(5)
                val productMap = products.associateBy { it.id }
                Pair(Pair(products, customers), Pair(transactions, settings))
            }.collect { (first, second) ->
                val (products, customers) = first
                val (transactions, settings) = second
                val recent = customers
                    .filter { it.lastTransactionAt != null }
                    .sortedByDescending { it.lastTransactionAt }
                    .take(5)
                val productMap = products.associateBy { it.id }
                _state.value = _state.value.copy(
                    products = products,
                    customers = customers,
                    recentCustomers = recent,
                    todayTransactions = transactions,
                    todayProducts = productMap,
                    cartMode = settings.cartModeEnabled,
                    isBangla = settings.isBangla
                )
            }
        }
    }

    fun selectProduct(product: ProductEntity) {
        if (_state.value.cartMode) {
            toggleCartItem(product)
            return
        }
        _state.value = _state.value.copy(
            selectedProduct = product,
            quantity = "1",
            paidAmount = "",
            paymentType = PaymentStatus.CASH,
            selectedCustomer = null,
            customerSearchQuery = "",
            saleComplete = false,
            errorMessage = null,
            showLowStockWarning = false,
            showHistoryTab = false
        )
    }

    private fun toggleCartItem(product: ProductEntity) {
        val current = _state.value
        val existing = current.getCartItem(product.id)
        if (existing != null) {
            _state.value = current.copy(
                cartItems = current.cartItems.filter { it.product.id != product.id }
            )
        } else {
            if (product.currentStock <= 0) return
            _state.value = current.copy(
                cartItems = current.cartItems + CartItem(product = product, quantity = 1.0)
            )
        }
    }

    fun updateCartItemQuantity(productId: Long, quantity: Double) {
        val current = _state.value
        _state.value = current.copy(
            cartItems = current.cartItems.map {
                if (it.product.id == productId) it.copy(quantity = quantity.coerceAtMost(it.product.currentStock))
                else it
            }.filter { it.quantity > 0 }
        )
    }

    fun removeCartItem(productId: Long) {
        val current = _state.value
        _state.value = current.copy(
            cartItems = current.cartItems.filter { it.product.id != productId }
        )
    }

    fun clearCart() {
        _state.value = _state.value.copy(cartItems = emptyList())
    }

    fun clearSelection() {
        _state.value = _state.value.copy(
            selectedProduct = null,
            quantity = "1",
            paidAmount = "",
            paymentType = PaymentStatus.CASH,
            selectedCustomer = null,
            customerSearchQuery = "",
            saleComplete = false,
            errorMessage = null,
            showHistoryTab = false
        )
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun setPaymentType(type: PaymentStatus) {
        val s = _state.value
        val newPaid = when (type) {
            PaymentStatus.CASH -> s.totalPrice.toString()
            PaymentStatus.CREDIT -> "0"
            PaymentStatus.PARTIAL -> ""
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
                    errorMessage = "সর্বোচ্চ ${product.currentStock.toInt()} টি বিক্রি করতে পারবেন"
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

    fun setShowHistoryTab(show: Boolean) {
        _state.value = _state.value.copy(showHistoryTab = show)
    }

    fun completeSale() {
        if (isProcessing) return
        isProcessing = true

        val s = _state.value

        if (s.cartMode && s.cartItems.isNotEmpty()) {
            completeCartSale(s)
            return
        }

        val product = s.selectedProduct ?: run { isProcessing = false; return }
        val qty = s.quantityDouble
        if (qty <= 0) {
            _state.value = _state.value.copy(errorMessage = "পরিমাণ শূন্য হতে পারে না")
            isProcessing = false
            return
        }
        if (qty > product.currentStock) {
            _state.value = _state.value.copy(
                errorMessage = "সর্বোচ্চ ${product.currentStock.toInt()} টি বিক্রি করতে পারবেন"
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
            PaymentStatus.CASH -> totalAmount
            PaymentStatus.CREDIT -> 0.0
            PaymentStatus.PARTIAL -> s.paidAmount.toDoubleOrNull() ?: 0.0
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
                    if (s.selectedCustomer != null && s.paymentType != PaymentStatus.CASH) {
                        val dueAmount = totalAmount - paidAmount
                        if (dueAmount > 0) {
                            customerRepository.addDue(s.selectedCustomer.id, dueAmount)
                        }
                        customerRepository.updateLastTransaction(s.selectedCustomer.id, System.currentTimeMillis())
                    }
                }
                _state.value = _state.value.copy(isSaving = false, saleComplete = true)
                if (product.currentStock - qty <= product.lowStockThreshold) {
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

    private fun completeCartSale(s: QuickSaleState) {
        if (s.selectedCustomer == null && s.isCustomerSelectionRequired) {
            _state.value = _state.value.copy(errorMessage = "গ্রাহক নির্বাচন করুন")
            isProcessing = false
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)
            try {
                database.withTransaction {
                    var totalDue = 0.0
                    for (item in s.cartItems) {
                        productRepository.removeStock(item.product.id, item.quantity)
                        transactionRepository.insert(
                            TransactionEntity(
                                type = TransactionType.SALE,
                                paymentType = s.paymentType,
                                productId = item.product.id,
                                customerId = s.selectedCustomer?.id,
                                quantity = item.quantity,
                                unitPrice = item.product.sellPrice,
                                totalAmount = item.totalPrice,
                                paidAmount = if (s.paymentType == PaymentStatus.CASH) item.totalPrice else 0.0
                            )
                        )
                        if (s.paymentType != PaymentStatus.CASH) {
                            totalDue += item.totalPrice
                        }
                    }
                    val paidAmountVal = s.paidAmount.toDoubleOrNull() ?: 0.0
                    val dueForCustomer = totalDue - paidAmountVal
                    if (s.selectedCustomer != null && dueForCustomer > 0) {
                        customerRepository.addDue(s.selectedCustomer.id, dueForCustomer)
                        customerRepository.updateLastTransaction(s.selectedCustomer.id, System.currentTimeMillis())
                    }
                }
                _state.value = _state.value.copy(
                    isSaving = false,
                    saleComplete = true,
                    cartItems = emptyList()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = "কার্ট বিক্রয় ব্যর্থ হয়েছে: ${e.localizedMessage}"
                )
            } finally {
                isProcessing = false
            }
        }
    }

    fun deleteTodayTransaction(transactionId: Long) {
        viewModelScope.launch {
            try {
                database.withTransaction {
                    val t = transactionRepository.getTransactionById(transactionId) ?: return@withTransaction
                    if (t.productId != null) {
                        productRepository.addStock(t.productId, t.quantity)
                    }
                    if (t.customerId != null && t.paymentType != PaymentStatus.CASH) {
                        val dueAmount = t.totalAmount - t.paidAmount
                        if (dueAmount > 0) {
                            customerRepository.removeDue(t.customerId, dueAmount)
                        }
                    }
                    transactionRepository.deleteById(transactionId)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "মুছে ফেলা ব্যর্থ হয়েছে: ${e.localizedMessage}")
            }
        }
    }

    fun resetAfterSale() {
        clearSelection()
    }

    fun dismissLowStockWarning() {
        _state.value = _state.value.copy(showLowStockWarning = false)
    }
}
