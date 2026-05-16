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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

// ─── Period selector ──────────────────────────────────────────────────────────

enum class DashboardPeriod { TODAY, MONTH }

// ─── State ───────────────────────────────────────────────────────────────────

data class DashboardState(
    val shopName: String = "",
    val dateLabel: String = "",
    val languageCode: String = "bn",

    // ── Today ────────────────────────────────────────────────────────────────
    val todaySales: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val totalDues: Double = 0.0,
    val todaySaleCount: Int = 0,
    val todayPurchases: Double = 0.0,
    val todayCreditGiven: Double = 0.0,

    // ── Month ────────────────────────────────────────────────────────────────
    val monthSales: Double = 0.0,
    val monthExpenses: Double = 0.0,
    val monthSaleCount: Int = 0,
    val monthPurchases: Double = 0.0,
    val monthCreditGiven: Double = 0.0,

    // ── Inventory ────────────────────────────────────────────────────────────
    val lowStockCount: Int = 0,
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val totalProductCount: Int = 0,
    val totalCustomerCount: Int = 0,
    val totalStockValue: Double = 0.0,

    // ── UI control ───────────────────────────────────────────────────────────
    val quickActions: List<String> = listOf("sale", "stock", "expense", "customer"),
    val isLoading: Boolean = true,
    val selectedPeriod: DashboardPeriod = DashboardPeriod.TODAY,
    val showFabMenu: Boolean = false,

    // ── Quick dialogs ────────────────────────────────────────────────────────
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
    val quickSaleComplete: Boolean = false,
) {
    val netProfit: Double      get() = todaySales - todayExpenses
    val monthNetProfit: Double get() = monthSales - monthExpenses

    /** Fraction (0–1) of today's sales relative to the monthly total. */
    val dailySalesFraction: Float
        get() = if (monthSales > 0) (todaySales / monthSales).toFloat().coerceIn(0f, 1f) else 0f

    /** Fraction (0–1) of today's expenses relative to the monthly total. */
    val dailyExpenseFraction: Float
        get() = if (monthExpenses > 0) (todayExpenses / monthExpenses).toFloat().coerceIn(0f, 1f) else 0f
}

// ─── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val transactionRepository: TransactionRepository,
    private val expenseRepository: ExpenseRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val paymentRepository: PaymentRepository,
    private val database: HisabDatabase,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
        loadProductsAndCustomers()
    }

    // ── Dashboard data ────────────────────────────────────────────────────────

    private fun loadDashboard() {
        val now         = LocalDate.now()
        val zone        = ZoneId.systemDefault()

        // Daily range
        val dayStart = now.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd   = now.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()

        // Monthly range
        val monthStart = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val monthEnd   = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX)
            .atZone(zone).toInstant().toEpochMilli()

        // Settings (language, shop name, quick-actions)
        viewModelScope.launch {
            appPreferences.settings.collect { settings ->
                val isBn      = settings.languageCode == "bn"
                val formatter = DateTimeFormatter.ofPattern(
                    if (isBn) "EEEE, dd MMMM yyyy" else "EEEE, MMMM dd, yyyy",
                    if (isBn) Locale.forLanguageTag("bn") else Locale.ENGLISH
                )
                _state.update { it.copy(
                    shopName     = settings.shopName,
                    quickActions = settings.quickActions.split(",").map { it.trim() },
                    languageCode = settings.languageCode,
                    dateLabel    = now.format(formatter),
                )}
            }
        }

        // Daily aggregates
        viewModelScope.launch {
            combine(
                transactionRepository.getTodaySalesFlow(dayStart, dayEnd),
                expenseRepository.getTodayExpensesFlow(dayStart, dayEnd),
                customerRepository.getTotalDues(),
                productRepository.getLowStockProducts(),
                transactionRepository.getTodayPurchasesFlow(dayStart, dayEnd),
                transactionRepository.getTodayCreditFlow(dayStart, dayEnd),
                productRepository.getProductCount(),
                productRepository.getTotalStockValue(),
                transactionRepository.getTodaySaleCountFlow(dayStart, dayEnd),
            ) { args: Array<Any?> -> args }.collect { args ->
                _state.update { it.copy(
                    todaySales    = args[0] as Double,
                    todayExpenses = args[1] as Double,
                    totalDues     = (args[2] as Double?) ?: 0.0,
                    lowStockCount = (args[3] as List<*>).size,
                    lowStockProducts = args[3] as List<ProductEntity>,
                    todayPurchases   = args[4] as Double,
                    todayCreditGiven = args[5] as Double,
                    totalProductCount = args[6] as Int,
                    totalStockValue   = (args[7] as Double?) ?: 0.0,
                    todaySaleCount    = args[8] as Int,
                    isLoading         = false,
                )}
            }
        }

        // Monthly aggregates
        viewModelScope.launch {
            combine(
                transactionRepository.getTodaySalesFlow(monthStart, monthEnd),
                expenseRepository.getTodayExpensesFlow(monthStart, monthEnd),
                transactionRepository.getTodayPurchasesFlow(monthStart, monthEnd),
                transactionRepository.getTodayCreditFlow(monthStart, monthEnd),
                transactionRepository.getTodaySaleCountFlow(monthStart, monthEnd),
            ) { sales, expenses, purchases, credit, count ->
                arrayOf<Any?>(sales, expenses, purchases, credit, count)
            }.collect { arr ->
                _state.update { it.copy(
                    monthSales       = arr[0] as Double,
                    monthExpenses    = arr[1] as Double,
                    monthPurchases   = arr[2] as Double,
                    monthCreditGiven = arr[3] as Double,
                    monthSaleCount   = arr[4] as Int,
                )}
            }
        }
    }

    private fun loadProductsAndCustomers() {
        viewModelScope.launch {
            productRepository.getAllProducts().collect { products ->
                _state.update { it.copy(allProducts = products) }
            }
        }
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                _state.update { it.copy(
                    allCustomers        = customers,
                    totalCustomerCount  = customers.size,
                )}
            }
        }
    }

    // ── Period toggle ─────────────────────────────────────────────────────────

    fun setSelectedPeriod(period: DashboardPeriod) {
        _state.update { it.copy(selectedPeriod = period) }
    }

    // ── FAB ───────────────────────────────────────────────────────────────────

    fun toggleFabMenu() { _state.update { it.copy(showFabMenu = !it.showFabMenu) } }
    fun hideFabMenu()   { _state.update { it.copy(showFabMenu = false) } }

    // ── Quick Sale ────────────────────────────────────────────────────────────

    fun showQuickSale() {
        resetQuickState()
        _state.update { it.copy(showQuickSaleDialog = true, showFabMenu = false) }
    }
    fun hideQuickSale() {
        _state.update { it.copy(showQuickSaleDialog = false, quickSaleComplete = false) }
    }

    fun quickSetSearchQuery(q: String) { _state.update { it.copy(quickSearchQuery = q) } }
    fun quickSelectProduct(p: ProductEntity) {
        _state.update { it.copy(quickSelectedProduct = p, quickSearchQuery = "", quickQuantity = "1") }
    }
    fun quickSetQuantity(q: String) {
        if (q.length <= 6) _state.update { it.copy(quickQuantity = q) }
    }
    fun quickClearProduct() {
        _state.update { it.copy(
            quickSelectedProduct = null, quickQuantity = "1", quickPaymentType = SalePaymentType.CASH,
        )}
    }

    fun quickCompleteSale() {
        val s       = _state.value
        val product = s.quickSelectedProduct ?: return
        val qty     = s.quickQuantity.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        val total   = product.sellPrice * qty
        val paid    = when (s.quickPaymentType) {
            SalePaymentType.CASH    -> total
            SalePaymentType.CREDIT  -> 0.0
            SalePaymentType.PARTIAL -> s.quickAmount.toDoubleOrNull() ?: 0.0
        }

        viewModelScope.launch {
            _state.update { it.copy(quickIsSaving = true) }
            runCatching {
                database.withTransaction {
                    productRepository.removeStock(product.id, qty)
                    transactionRepository.insert(
                        TransactionEntity(
                            type         = TransactionType.SALE,
                            paymentType  = s.quickPaymentType,
                            productId    = product.id,
                            customerId   = s.quickSelectedCustomer?.id,
                            quantity     = qty,
                            unitPrice    = product.sellPrice,
                            totalAmount  = total,
                            paidAmount   = paid,
                        )
                    )
                    if (s.quickSelectedCustomer != null && s.quickPaymentType != SalePaymentType.CASH) {
                        customerRepository.addDue(s.quickSelectedCustomer.id, total - paid)
                        customerRepository.updateLastTransaction(s.quickSelectedCustomer.id, System.currentTimeMillis())
                    }
                }
            }.onSuccess {
                _state.update { it.copy(quickIsSaving = false, quickSaleComplete = true) }
            }.onFailure {
                _state.update { it.copy(quickIsSaving = false) }
            }
        }
    }

    // ── Quick Expense ─────────────────────────────────────────────────────────

    fun showQuickExpense() {
        resetQuickState()
        _state.update { it.copy(showQuickExpenseDialog = true, showFabMenu = false) }
    }
    fun hideQuickExpense() { _state.update { it.copy(showQuickExpenseDialog = false) } }
    fun quickSetAmount(a: String)      { _state.update { it.copy(quickAmount = a) } }
    fun quickSetDescription(d: String) { _state.update { it.copy(quickDescription = d) } }
    fun quickSetExpenseCategory(c: ExpenseCategory) { _state.update { it.copy(quickExpenseCategory = c) } }

    fun quickAddExpense() {
        val s      = _state.value
        val amount = s.quickAmount.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        viewModelScope.launch {
            _state.update { it.copy(quickIsSaving = true) }
            runCatching {
                database.withTransaction {
                    expenseRepository.insert(
                        ExpenseEntity(
                            categoryId  = s.quickExpenseCategory,
                            amount      = amount,
                            description = s.quickDescription,
                            date        = System.currentTimeMillis(),
                        )
                    )
                    transactionRepository.insert(
                        TransactionEntity(
                            type        = TransactionType.EXPENSE,
                            quantity    = 1.0,
                            unitPrice   = amount,
                            totalAmount = amount,
                            notes       = s.quickDescription,
                        )
                    )
                }
            }.onSuccess {
                _state.update { it.copy(quickIsSaving = false, showQuickExpenseDialog = false) }
            }.onFailure {
                _state.update { it.copy(quickIsSaving = false) }
            }
        }
    }

    // ── Quick Stock ───────────────────────────────────────────────────────────

    fun showQuickStock() {
        resetQuickState()
        _state.update { it.copy(showQuickStockDialog = true, showFabMenu = false) }
    }
    fun hideQuickStock() { _state.update { it.copy(showQuickStockDialog = false) } }
    fun quickSetStockIsAdd(isAdd: Boolean) { _state.update { it.copy(quickStockIsAdd = isAdd) } }

    fun quickUpdateStock() {
        val s       = _state.value
        val product = s.quickSelectedProduct ?: return
        val qty     = s.quickQuantity.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        viewModelScope.launch {
            _state.update { it.copy(quickIsSaving = true) }
            runCatching {
                database.withTransaction {
                    if (s.quickStockIsAdd) {
                        productRepository.addStock(product.id, qty)
                        transactionRepository.insert(
                            TransactionEntity(
                                type        = TransactionType.PURCHASE,
                                productId   = product.id,
                                quantity    = qty,
                                unitPrice   = product.buyPrice,
                                totalAmount = product.buyPrice * qty,
                                notes       = s.quickDescription,
                            )
                        )
                    } else {
                        productRepository.removeStock(product.id, qty)
                        transactionRepository.insert(
                            TransactionEntity(
                                type        = TransactionType.STOCK_LOSS,
                                productId   = product.id,
                                quantity    = qty,
                                unitPrice   = 0.0,
                                totalAmount = 0.0,
                                notes       = s.quickDescription,
                            )
                        )
                    }
                }
            }.onSuccess {
                _state.update { it.copy(quickIsSaving = false, showQuickStockDialog = false) }
            }.onFailure {
                _state.update { it.copy(quickIsSaving = false) }
            }
        }
    }

    // ── Quick Payment ─────────────────────────────────────────────────────────

    fun showQuickPayment() {
        resetQuickState()
        _state.update { it.copy(showQuickPaymentDialog = true, showFabMenu = false) }
    }
    fun hideQuickPayment() { _state.update { it.copy(showQuickPaymentDialog = false) } }
    fun quickSetPaymentIsReceive(r: Boolean) { _state.update { it.copy(quickPaymentIsReceive = r) } }
    fun quickSelectCustomer(c: CustomerEntity) {
        _state.update { it.copy(quickSelectedCustomer = c, quickSearchQuery = "") }
    }
    fun quickClearCustomer() { _state.update { it.copy(quickSelectedCustomer = null) } }
    fun quickSetPaymentType(type: SalePaymentType) { _state.update { it.copy(quickPaymentType = type) } }

    fun quickRecordPayment() {
        val s      = _state.value
        val amount = s.quickAmount.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        viewModelScope.launch {
            _state.update { it.copy(quickIsSaving = true) }
            runCatching {
                val pType = if (s.quickPaymentIsReceive) PaymentType.RECEIVED else PaymentType.PAID
                database.withTransaction {
                    paymentRepository.insert(
                        PaymentEntity(
                            type          = pType,
                            customerId    = s.quickSelectedCustomer?.id,
                            amount        = amount,
                            paymentMethod = PaymentMethod.CASH,
                            description   = s.quickDescription,
                        )
                    )
                    s.quickSelectedCustomer?.let { cust ->
                        if (s.quickPaymentIsReceive) customerRepository.removeDue(cust.id, amount)
                        else                         customerRepository.addDue(cust.id, amount)
                        customerRepository.updateLastTransaction(cust.id, System.currentTimeMillis())
                    }
                }
            }.onSuccess {
                _state.update { it.copy(quickIsSaving = false, showQuickPaymentDialog = false) }
            }.onFailure {
                _state.update { it.copy(quickIsSaving = false) }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun resetQuickState() {
        _state.update { it.copy(
            quickSearchQuery    = "",
            quickSelectedProduct = null,
            quickSelectedCustomer = null,
            quickQuantity       = "1",
            quickAmount         = "",
            quickDescription    = "",
            quickExpenseCategory = ExpenseCategory.OTHER,
            quickPaymentType    = SalePaymentType.CASH,
            quickStockIsAdd     = true,
            quickPaymentIsReceive = true,
            quickIsSaving       = false,
            quickSaleComplete   = false,
        )}
    }
}