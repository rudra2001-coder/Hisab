package com.rudra.hisab.ui.screens.accounting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.LedgerAccountType
import com.rudra.hisab.data.local.entity.LedgerEntryEntity
import com.rudra.hisab.data.local.entity.LedgerEntryType
import com.rudra.hisab.data.local.entity.PaymentEntity
import com.rudra.hisab.data.local.entity.PaymentMethod
import com.rudra.hisab.data.local.entity.PaymentType
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.LedgerEntryRepository
import com.rudra.hisab.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CashBookEntry(
    val id: Long,
    val type: String,          // "CASH_IN" | "CASH_OUT"
    val amount: Double,
    val description: String,
    val timestamp: Long,
    val paymentMethod: String? = null
)

data class AccountingUiState(
    val cashBookEntries: List<CashBookEntry> = emptyList(),
    val todayCashIn: Double = 0.0,
    val todayCashOut: Double = 0.0,
    val todayNetBalance: Double = 0.0,
    val selectedTab: AccountingTab = AccountingTab.CASH_BOOK,
    val ledgerEntries: List<LedgerEntryEntity> = emptyList(),
    val customerBalance: Double = 0.0,
    val supplierBalance: Double = 0.0,
    val generalBalance: Double = 0.0,
    val showAddCashEntryDialog: Boolean = false,
    val cashEntryType: String = "CASH_IN",
    val cashEntryAmount: String = "",
    val cashEntryDescription: String = "",
    val cashEntryMethod: PaymentMethod = PaymentMethod.CASH,
    val isBangla: Boolean = true
)

enum class AccountingTab { CASH_BOOK, LEDGER, EXPENSES }

@HiltViewModel
class AccountingViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val ledgerEntryRepository: LedgerEntryRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountingUiState())
    val uiState: StateFlow<AccountingUiState> = _uiState.asStateFlow()

    init {
        observeCashBook()
        observeBalances()
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = appPreferences.settings.first()
            _uiState.value = _uiState.value.copy(isBangla = settings.isBangla)
        }
    }

    private fun observeCashBook() {
        viewModelScope.launch {
            paymentRepository.getAllPayments().collect { payments ->
                val entries = payments.map { p ->
                    CashBookEntry(
                        id = p.id,
                        type = if (p.type == PaymentType.RECEIVED) "CASH_IN" else "CASH_OUT",
                        amount = p.amount,
                        description = p.description.ifEmpty { p.type.name },
                        timestamp = p.createdAt,
                        paymentMethod = p.paymentMethod.name
                    )
                }
                _uiState.value = _uiState.value.copy(cashBookEntries = entries)
            }
        }
    }

    private fun observeBalances() {
        viewModelScope.launch {
            val customer = ledgerEntryRepository.getAccountBalance(LedgerAccountType.CUSTOMER)
            val supplier = ledgerEntryRepository.getAccountBalance(LedgerAccountType.SUPPLIER)
            val general = ledgerEntryRepository.getAccountBalance(LedgerAccountType.GENERAL)
            _uiState.value = _uiState.value.copy(
                customerBalance = customer,
                supplierBalance = supplier,
                generalBalance = general
            )
        }
    }

    fun selectTab(tab: AccountingTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        if (tab == AccountingTab.LEDGER) loadLedgerEntries()
    }

    private fun loadLedgerEntries() {
        viewModelScope.launch {
            ledgerEntryRepository.getAllLedgerEntries().collect { entries ->
                _uiState.value = _uiState.value.copy(ledgerEntries = entries)
            }
        }
    }

    fun showAddCashEntry(type: String) {
        _uiState.value = _uiState.value.copy(
            showAddCashEntryDialog = true,
            cashEntryType = type,
            cashEntryAmount = "",
            cashEntryDescription = "",
            cashEntryMethod = PaymentMethod.CASH
        )
    }

    fun hideAddCashEntry() {
        _uiState.value = _uiState.value.copy(showAddCashEntryDialog = false)
    }

    fun setCashEntryAmount(amount: String) {
        _uiState.value = _uiState.value.copy(cashEntryAmount = amount)
    }

    fun setCashEntryDescription(desc: String) {
        _uiState.value = _uiState.value.copy(cashEntryDescription = desc)
    }

    fun setCashEntryMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(cashEntryMethod = method)
    }

    fun saveCashEntry() {
        val amount = _uiState.value.cashEntryAmount.toDoubleOrNull() ?: return
        if (amount <= 0) return

        val paymentType = if (_uiState.value.cashEntryType == "CASH_IN")
            PaymentType.RECEIVED else PaymentType.PAID

        viewModelScope.launch {
            paymentRepository.insert(
                PaymentEntity(
                    type = paymentType,
                    amount = amount,
                    paymentMethod = _uiState.value.cashEntryMethod,
                    description = _uiState.value.cashEntryDescription
                )
            )
            // Also create a ledger entry
            val ledgerType = if (_uiState.value.cashEntryType == "CASH_IN")
                LedgerEntryType.DEBIT else LedgerEntryType.CREDIT
            ledgerEntryRepository.insert(
                LedgerEntryEntity(
                    accountType = LedgerAccountType.GENERAL,
                    entryType = ledgerType,
                    amount = amount,
                    description = _uiState.value.cashEntryDescription.ifEmpty { paymentType.name }
                )
            )
            _uiState.value = _uiState.value.copy(showAddCashEntryDialog = false)
        }
    }
}
