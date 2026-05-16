package com.rudra.hisab.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.rudra.hisab.data.local.HisabDatabase
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.local.entity.ExpenseEntity
import com.rudra.hisab.data.local.entity.PaymentEntity
import com.rudra.hisab.data.local.entity.PaymentMethod
import com.rudra.hisab.data.local.entity.PaymentType
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.SalePaymentType
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.ExpenseRepository
import com.rudra.hisab.data.repository.PaymentRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class DashboardState(
    val shopName: String = "",
    val todaySales: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val totalDues: Double = 0.0,
    val lowStockCount: Int = 0,
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val todaySaleCount: Int = 0,
    val todayPurchases: Double = 0.0,
    val todayCreditGiven: Double = 0.0,
    val totalProductCount: Int = 0,
    val totalCustomerCount: Int = 0,
    val totalStockValue: Double = 0.0,
    val quickActions: List<String> = listOf("sale", "stock", "expense", "customer"),
    val isLoading: Boolean = true,
    val dateLabel: String = "",
    val languageCode: String = "bn",
    val showFabMenu: Boolean = false,
    val showQuickSaleDialog: Boolean = false,
    val showQuickExpenseDialog: Boolean = false,
    val showQuickStockDialog: Boolean = false,
    val showQuickPaymentDialog: Boolean = false,
    val quickSearchQuery: String = "",
    val quickSelectedProduct: ProductEntity? = null,
    val quickQuantity: String = "1",
    val quickAmount: String = "",
    val quickDescription: String = "",
    val quickExpenseCategory: ExpenseCategory = ExpenseCategory.OTHER,
    val quickPaymentType: SalePaymentType = SalePaymentType.CASH,
    val quickSelectedCustomer: CustomerEntity? = null,
    val quickStockIsAdd: Boolean = true,
    val quickPaymentIsReceive: Boolean = true,
    val allProducts: List<ProductEntity> = emptyList(),
    val allCustomers: List<CustomerEntity> = emptyList(),
    val quickIsSaving: Boolean = false,
    val quickSaleComplete: Boolean = false
) {
    val netProfit: Double get() = todaySales - todayExpenses
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val transactionRepository: TransactionRepository,
    private val expenseRepository: ExpenseRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val paymentRepository: PaymentRepository,
    private val database: HisabDatabase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
        loadProductsAndCustomers()
    }

    private fun loadDashboard() {
        val now = LocalDate.now()
        val startOfDay = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

         viewModelScope.launch {
             appPreferences.settings.collect { settings ->
                 val isBn = settings.languageCode == "bn"
                 val formatter = DateTimeFormatter.ofPattern(
                     if (isBn) "EEEE, dd MMMM yyyy" else "EEEE, MMMM dd, yyyy",
                     if (isBn) Locale.forLanguageTag("bn") else Locale.ENGLISH
                 )
                 val actions = settings.quickActions.split(",").map { it.trim() }
                 _state.value = _state.value.copy(
                     shopName = settings.shopName,
                     quickActions = actions,
                     languageCode = settings.languageCode,
                     dateLabel = now.format(formatter)
                 )
             }
         }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            combine(
                transactionRepository.getTodaySalesFlow(startOfDay, endOfDay),
                expenseRepository.getTodayExpensesFlow(startOfDay, endOfDay),
                customerRepository.getTotalDues(),
                productRepository.getLowStockProducts(),
                transactionRepository.getTodayPurchasesFlow(startOfDay, endOfDay),
                transactionRepository.getTodayCreditFlow(startOfDay, endOfDay),
                productRepository.getProductCount(),
                productRepository.getTotalStockValue()
            ) { args: Array<Any?> ->
                val sales = args[0] as Double
                val expenses = args[1] as Double
                val dues = args[2] as Double?
                val lowStock = args[3] as List<*>
                val purchases = args[4] as Double
                val credit = args[5] as Double
                val prodCount = args[6] as Int
                val stockVal = args[7] as Double?
                _state.value.copy(
                    todaySales = sales,
                    todayExpenses = expenses,
                    totalDues = dues ?: 0.0,
                    lowStockCount = lowStock.size,
                    lowStockProducts = lowStock as List<ProductEntity>,
                    todayPurchases = purchases,
                    todayCreditGiven = credit,
                    totalProductCount = prodCount,
                    totalStockValue = stockVal ?: 0.0,
                    isLoading = false
                )
            }.collect { newState ->
                _state.value = newState.copy(
                    todaySaleCount = transactionRepository.getTodaySaleCount(startOfDay, endOfDay)
                )
            }
        }
    }

    private fun loadProductsAndCustomers() {
        viewModelScope.launch {
            productRepository.getAllProducts().collect { products ->
                _state.value = _state.value.copy(allProducts = products)
            }
        }
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                _state.value = _state.value.copy(allCustomers = customers, totalCustomerCount = customers.size)
            }
        }
    }

    // --- FAB Menu ---
    fun toggleFabMenu() { _state.value = _state.value.copy(showFabMenu = !_state.value.showFabMenu) }
    fun hideFabMenu() { _state.value = _state.value.copy(showFabMenu = false) }

    // --- Quick Sale Dialog ---
    fun showQuickSale() {
        resetQuickState()
        _state.value = _state.value.copy(showQuickSaleDialog = true, showFabMenu = false)
    }
    fun hideQuickSale() { _state.value = _state.value.copy(showQuickSaleDialog = false, quickSaleComplete = false) }

    fun quickSetSearchQuery(q: String) { _state.value = _state.value.copy(quickSearchQuery = q) }
    fun quickSelectProduct(p: ProductEntity) { _state.value = _state.value.copy(quickSelectedProduct = p, quickSearchQuery = "", quickQuantity = "1") }
    fun quickSetQuantity(q: String) { if (q.length <= 6) _state.value = _state.value.copy(quickQuantity = q) }
    fun quickClearProduct() { _state.value = _state.value.copy(quickSelectedProduct = null, quickQuantity = "1", quickPaymentType = SalePaymentType.CASH) }

    fun quickCompleteSale() {
        val s = _state.value
        val product = s.quickSelectedProduct ?: return
        val qty = s.quickQuantity.toDoubleOrNull() ?: return
        if (qty <= 0) return

        val totalAmount = product.sellPrice * qty
        val paidAmount = when (s.quickPaymentType) {
            SalePaymentType.CASH -> totalAmount
            SalePaymentType.CREDIT -> 0.0
            SalePaymentType.PARTIAL -> s.quickAmount.toDoubleOrNull() ?: 0.0
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(quickIsSaving = true)
            try {
                database.withTransaction {
                    productRepository.removeStock(product.id, qty)
                    transactionRepository.insert(
                        TransactionEntity(
                            type = TransactionType.SALE,
                            paymentType = s.quickPaymentType,
                            productId = product.id,
                            customerId = s.quickSelectedCustomer?.id,
                            quantity = qty,
                            unitPrice = product.sellPrice,
                            totalAmount = totalAmount,
                            paidAmount = paidAmount
                        )
                    )
                    if (s.quickSelectedCustomer != null && s.quickPaymentType != SalePaymentType.CASH) {
                        val dueAmount = totalAmount - paidAmount
                        customerRepository.addDue(s.quickSelectedCustomer.id, dueAmount)
                        customerRepository.updateLastTransaction(s.quickSelectedCustomer.id, System.currentTimeMillis())
                    }
                }
                _state.value = _state.value.copy(quickIsSaving = false, quickSaleComplete = true)
            } catch (_: Exception) {
                _state.value = _state.value.copy(quickIsSaving = false)
            }
        }
    }

    // --- Quick Expense Dialog ---
    fun showQuickExpense() {
        resetQuickState()
        _state.value = _state.value.copy(showQuickExpenseDialog = true, showFabMenu = false)
    }
    fun hideQuickExpense() { _state.value = _state.value.copy(showQuickExpenseDialog = false) }
    fun quickSetAmount(a: String) { _state.value = _state.value.copy(quickAmount = a) }
    fun quickSetDescription(d: String) { _state.value = _state.value.copy(quickDescription = d) }
    fun quickSetExpenseCategory(c: ExpenseCategory) { _state.value = _state.value.copy(quickExpenseCategory = c) }

    fun quickAddExpense() {
        val s = _state.value
        val amount = s.quickAmount.toDoubleOrNull() ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            _state.value = _state.value.copy(quickIsSaving = true)
            try {
                database.withTransaction {
                    expenseRepository.insert(
                        ExpenseEntity(
                            categoryId = s.quickExpenseCategory,
                            amount = amount,
                            description = s.quickDescription,
                            date = System.currentTimeMillis()
                        )
                    )
                    transactionRepository.insert(
                        TransactionEntity(
                            type = TransactionType.EXPENSE,
                            quantity = 1.0,
                            unitPrice = amount,
                            totalAmount = amount,
                            notes = s.quickDescription
                        )
                    )
                }
                _state.value = _state.value.copy(quickIsSaving = false, showQuickExpenseDialog = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(quickIsSaving = false)
            }
        }
    }

    // --- Quick Stock Dialog ---
    fun showQuickStock() {
        resetQuickState()
        _state.value = _state.value.copy(showQuickStockDialog = true, showFabMenu = false)
    }
    fun hideQuickStock() { _state.value = _state.value.copy(showQuickStockDialog = false) }
    fun quickSetStockIsAdd(isAdd: Boolean) { _state.value = _state.value.copy(quickStockIsAdd = isAdd) }

    fun quickUpdateStock() {
        val s = _state.value
        val product = s.quickSelectedProduct ?: return
        val qty = s.quickQuantity.toDoubleOrNull() ?: return
        if (qty <= 0) return

        viewModelScope.launch {
            _state.value = _state.value.copy(quickIsSaving = true)
            try {
                database.withTransaction {
                    if (s.quickStockIsAdd) {
                        productRepository.addStock(product.id, qty)
                        transactionRepository.insert(
                            TransactionEntity(
                                type = TransactionType.PURCHASE,
                                productId = product.id,
                                quantity = qty,
                                unitPrice = product.buyPrice,
                                totalAmount = product.buyPrice * qty,
                                notes = s.quickDescription
                            )
                        )
                    } else {
                        productRepository.removeStock(product.id, qty)
                        transactionRepository.insert(
                            TransactionEntity(
                                type = TransactionType.STOCK_LOSS,
                                productId = product.id,
                                quantity = qty,
                                unitPrice = 0.0,
                                totalAmount = 0.0,
                                notes = s.quickDescription
                            )
                        )
                    }
                }
                _state.value = _state.value.copy(quickIsSaving = false, showQuickStockDialog = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(quickIsSaving = false)
            }
        }
    }

    // --- Quick Payment Dialog ---
    fun showQuickPayment() {
        resetQuickState()
        _state.value = _state.value.copy(showQuickPaymentDialog = true, showFabMenu = false)
    }
    fun hideQuickPayment() { _state.value = _state.value.copy(showQuickPaymentDialog = false) }
    fun quickSetPaymentIsReceive(isReceive: Boolean) { _state.value = _state.value.copy(quickPaymentIsReceive = isReceive) }
    fun quickSelectCustomer(c: CustomerEntity) { _state.value = _state.value.copy(quickSelectedCustomer = c, quickSearchQuery = "") }
    fun quickClearCustomer() { _state.value = _state.value.copy(quickSelectedCustomer = null) }

    fun quickRecordPayment() {
        val s = _state.value
        val amount = s.quickAmount.toDoubleOrNull() ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            _state.value = _state.value.copy(quickIsSaving = true)
            try {
                val paymentType = if (s.quickPaymentIsReceive) PaymentType.RECEIVED else PaymentType.PAID
                database.withTransaction {
                    paymentRepository.insert(
                        PaymentEntity(
                            type = paymentType,
                            customerId = s.quickSelectedCustomer?.id,
                            amount = amount,
                            paymentMethod = PaymentMethod.CASH,
                            description = s.quickDescription
                        )
                    )
                    if (s.quickSelectedCustomer != null) {
                        if (s.quickPaymentIsReceive) {
                            customerRepository.removeDue(s.quickSelectedCustomer.id, amount)
                        } else {
                            customerRepository.addDue(s.quickSelectedCustomer.id, amount)
                        }
                        customerRepository.updateLastTransaction(s.quickSelectedCustomer.id, System.currentTimeMillis())
                    }
                }
                _state.value = _state.value.copy(quickIsSaving = false, showQuickPaymentDialog = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(quickIsSaving = false)
            }
        }
    }

    fun quickSetPaymentType(type: SalePaymentType) {
        _state.value = _state.value.copy(quickPaymentType = type)
    }

    private fun resetQuickState() {
        _state.value = _state.value.copy(
            quickSearchQuery = "",
            quickSelectedProduct = null,
            quickSelectedCustomer = null,
            quickQuantity = "1",
            quickAmount = "",
            quickDescription = "",
            quickExpenseCategory = ExpenseCategory.OTHER,
            quickPaymentType = SalePaymentType.CASH,
            quickStockIsAdd = true,
            quickPaymentIsReceive = true,
            quickIsSaving = false,
            quickSaleComplete = false
        )
    }
}
