package com.rudra.hisab.ui.screens.sale

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.SalePaymentType
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SaleState(
    val products: List<ProductEntity> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val selectedProduct: ProductEntity? = null,
    val quantity: String = "1",
    val paymentType: SalePaymentType = SalePaymentType.CASH,
    val paidAmount: String = "",
    val selectedCustomerId: Long? = null,
    val isSaving: Boolean = false,
    val saleComplete: Boolean = false,
    val searchQuery: String = "",
    val todaySalesTotal: Double = 0.0,
    val todaySaleCount: Int = 0
)

@HiltViewModel
class SaleViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SaleState())
    val state: StateFlow<SaleState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val now = java.time.LocalDate.now()
            val startOfDay = now.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = now.atTime(java.time.LocalTime.MAX).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

            combine(
                productRepository.getAllProducts(),
                customerRepository.getAllCustomers(),
                transactionRepository.getTodaySalesFlow(startOfDay, endOfDay)
            ) { products, customers, salesFlow ->
                Triple(products, customers, salesFlow)
            }.collect { (products, customers, salesTotal) ->
                val saleCount = transactionRepository.getTodaySaleCount(startOfDay, endOfDay)
                _state.value = _state.value.copy(
                    products = products,
                    customers = customers,
                    todaySalesTotal = salesTotal,
                    todaySaleCount = saleCount
                )
            }
        }
    }

    fun selectProduct(product: ProductEntity) {
        _state.value = _state.value.copy(
            selectedProduct = product,
            quantity = "1",
            saleComplete = false
        )
    }

    fun setQuantity(qty: String) {
        if (qty.length <= 6) {
            _state.value = _state.value.copy(quantity = qty)
        }
    }

    fun setPaymentType(type: SalePaymentType) {
        _state.value = _state.value.copy(paymentType = type)
    }

    fun setPaidAmount(amount: String) {
        _state.value = _state.value.copy(paidAmount = amount)
    }

    fun setCustomer(id: Long?) {
        _state.value = _state.value.copy(selectedCustomerId = id)
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun clearSelection() {
        _state.value = _state.value.copy(
            selectedProduct = null,
            quantity = "1",
            paidAmount = "",
            paymentType = SalePaymentType.CASH,
            selectedCustomerId = null,
            saleComplete = false
        )
    }

    fun completeSale() {
        val s = _state.value
        val product = s.selectedProduct ?: return
        val qty = s.quantity.toDoubleOrNull() ?: return
        if (qty <= 0) return

        val totalAmount = product.sellPrice * qty
        val paidAmount = when (s.paymentType) {
            SalePaymentType.CASH -> totalAmount
            SalePaymentType.CREDIT -> 0.0
            SalePaymentType.PARTIAL -> s.paidAmount.toDoubleOrNull() ?: 0.0
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            productRepository.removeStock(product.id, qty)

            transactionRepository.insert(
                TransactionEntity(
                    type = TransactionType.SALE,
                    paymentType = s.paymentType,
                    productId = product.id,
                    customerId = s.selectedCustomerId,
                    quantity = qty,
                    unitPrice = product.sellPrice,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount
                )
            )

            if (s.selectedCustomerId != null && s.paymentType != SalePaymentType.CASH) {
                val dueAmount = totalAmount - paidAmount
                customerRepository.addDue(s.selectedCustomerId, dueAmount)
                customerRepository.updateLastTransaction(s.selectedCustomerId, System.currentTimeMillis())
            }

            context.getSharedPreferences("hisab_preferences", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_sale_date", System.currentTimeMillis())
                .apply()

            _state.value = _state.value.copy(isSaving = false, saleComplete = true)
        }
    }
}
