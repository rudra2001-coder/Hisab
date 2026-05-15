package com.rudra.hisab.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.preferences.AppSettings
import com.rudra.hisab.worker.MonthlyReportWorker
import com.rudra.hisab.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SettingsState(
    val settings: AppSettings = AppSettings(),
    val showPinDialog: Boolean = false,
    val pinInput: String = "",
    val pinConfirm: String = "",
    val pinStep: Int = 1,
    val pinMode: PinMode = PinMode.SETUP,
    val pinError: String? = null,
    val showShopEdit: Boolean = false,
    val editShopName: String = "",
    val showThemeSelector: Boolean = false,
    val showFontSelector: Boolean = false,
    val showNavOrderEditor: Boolean = false,
    val showQuickActionsEditor: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showDataExportDialog: Boolean = false,
    val showDataImportDialog: Boolean = false,
    val exportFormat: String = "json",
    val importData: String = "",
    val importError: String? = null,
    val showCreditLimitDialog: Boolean = false,
    val creditLimitInput: String = "",
    val showDeleteWindowDialog: Boolean = false,
    val deleteWindowInput: String = "",
    val showReminderTimeDialog: Boolean = false,
    val reminderHour: String = "",
    val reminderMinute: String = ""
)

enum class PinMode { SETUP, CHANGE, DISABLE }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.settings.collect { settings ->
                _state.value = _state.value.copy(settings = settings)
            }
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            appPreferences.setBangla(!_state.value.settings.isBangla)
        }
    }

    fun showPinSetup() { showPinDialog(PinMode.SETUP) }
    fun showPinChange() { showPinDialog(PinMode.CHANGE) }
    fun showPinDisable() { showPinDialog(PinMode.DISABLE) }

    private fun showPinDialog(mode: PinMode) {
        _state.value = _state.value.copy(
            showPinDialog = true,
            pinMode = mode,
            pinStep = 1,
            pinInput = "",
            pinConfirm = "",
            pinError = null
        )
    }

    fun hidePinSetup() {
        _state.value = _state.value.copy(showPinDialog = false)
    }

    fun setPinInput(pin: String) {
        if (pin.length <= 4) {
            _state.value = _state.value.copy(pinInput = pin, pinError = null)
        }
    }

    fun setPinConfirm(pin: String) {
        if (pin.length <= 4) {
            _state.value = _state.value.copy(pinConfirm = pin, pinError = null)
        }
    }

    fun nextPinStep() {
        _state.value = _state.value.copy(pinStep = _state.value.pinStep + 1, pinInput = "", pinError = null)
    }

    private fun hashPin(pin: String): String {
        return MessageDigest.getInstance("SHA-256").digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun verifyAndProceed() {
        val currentHash = _state.value.settings.pinHash
        val inputHash = hashPin(_state.value.pinInput)
        if (inputHash != currentHash) {
            _state.value = _state.value.copy(pinError = "ভুল পিন")
            return
        }
        when (_state.value.pinMode) {
            PinMode.CHANGE -> nextPinStep()
            PinMode.DISABLE -> disablePinConfirmed()
            else -> {}
        }
    }

    fun savePin() {
        val s = _state.value
        if (s.pinInput.length < 4) {
            _state.value = _state.value.copy(pinError = "পিন ৪ ডিজিটের হতে হবে")
            return
        }
        if (s.pinInput != s.pinConfirm) {
            _state.value = _state.value.copy(pinError = "পিন মিলছে না")
            return
        }
        viewModelScope.launch {
            appPreferences.setPinHash(hashPin(s.pinInput))
            appPreferences.setPinEnabled(true)
            _state.value = _state.value.copy(showPinDialog = false)
        }
    }

    private fun disablePinConfirmed() {
        viewModelScope.launch {
            appPreferences.setPinEnabled(false)
            appPreferences.setPinHash("")
            appPreferences.setBiometricEnabled(false)
            _state.value = _state.value.copy(showPinDialog = false)
        }
    }

    fun toggleBiometric() {
        viewModelScope.launch {
            appPreferences.setBiometricEnabled(!_state.value.settings.isBiometricEnabled)
        }
    }

    fun showShopEdit() {
        _state.value = _state.value.copy(
            showShopEdit = true,
            editShopName = _state.value.settings.shopName
        )
    }

    fun hideShopEdit() {
        _state.value = _state.value.copy(showShopEdit = false)
    }

    fun setEditShopName(name: String) {
        _state.value = _state.value.copy(editShopName = name)
    }

    fun saveShopName() {
        viewModelScope.launch {
            appPreferences.setShopName(_state.value.editShopName)
            _state.value = _state.value.copy(showShopEdit = false)
        }
    }

    // Theme
    fun showThemeSelector() { _state.value = _state.value.copy(showThemeSelector = true) }
    fun hideThemeSelector() { _state.value = _state.value.copy(showThemeSelector = false) }
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            appPreferences.setThemeMode(mode)
            _state.value = _state.value.copy(showThemeSelector = false)
        }
    }

    // Font Size
    fun showFontSelector() { _state.value = _state.value.copy(showFontSelector = true) }
    fun hideFontSelector() { _state.value = _state.value.copy(showFontSelector = false) }
    fun setFontSize(size: String) {
        viewModelScope.launch {
            appPreferences.setFontSize(size)
            _state.value = _state.value.copy(showFontSelector = false)
        }
    }

    // Cart Mode
    fun toggleCartMode() {
        viewModelScope.launch { appPreferences.setCartModeEnabled(!_state.value.settings.cartModeEnabled) }
    }

    // FAB Mode
    fun toggleFabMode() {
        viewModelScope.launch { appPreferences.setFabModeEnabled(!_state.value.settings.fabModeEnabled) }
    }

    // Batch Tracking
    fun toggleBatchTracking() {
        viewModelScope.launch { appPreferences.setBatchTrackingEnabled(!_state.value.settings.batchTrackingEnabled) }
    }

    // Monthly Report
    fun toggleMonthlyReport() {
        viewModelScope.launch {
            val enabled = !_state.value.settings.monthlyReportReminder
            appPreferences.setMonthlyReportReminder(enabled)
            if (enabled) {
                MonthlyReportWorker.schedule(context)
            }
        }
    }

    // Nav Order
    fun showNavOrderEditor() { _state.value = _state.value.copy(showNavOrderEditor = true) }
    fun hideNavOrderEditor() { _state.value = _state.value.copy(showNavOrderEditor = false) }
    fun setNavOrder(order: String) {
        viewModelScope.launch {
            appPreferences.setNavOrder(order)
            _state.value = _state.value.copy(showNavOrderEditor = false)
        }
    }

    // Quick Actions
    fun showQuickActionsEditor() { _state.value = _state.value.copy(showQuickActionsEditor = true) }
    fun hideQuickActionsEditor() { _state.value = _state.value.copy(showQuickActionsEditor = false) }
    fun setQuickActions(actions: String) {
        viewModelScope.launch {
            appPreferences.setQuickActions(actions)
            _state.value = _state.value.copy(showQuickActionsEditor = false)
        }
    }

    // Credit Limit
    fun showCreditLimitDialog() {
        _state.value = _state.value.copy(
            showCreditLimitDialog = true,
            creditLimitInput = _state.value.settings.defaultCreditLimit.toLong().toString()
        )
    }
    fun hideCreditLimitDialog() { _state.value = _state.value.copy(showCreditLimitDialog = false) }
    fun setCreditLimitInput(v: String) { _state.value = _state.value.copy(creditLimitInput = v) }
    fun saveCreditLimit() {
        viewModelScope.launch {
            appPreferences.setDefaultCreditLimit(_state.value.creditLimitInput.toDoubleOrNull() ?: 0.0)
            _state.value = _state.value.copy(showCreditLimitDialog = false)
        }
    }

    // Delete Window
    fun showDeleteWindowDialog() {
        _state.value = _state.value.copy(
            showDeleteWindowDialog = true,
            deleteWindowInput = _state.value.settings.deleteWindowHours.toString()
        )
    }
    fun hideDeleteWindowDialog() { _state.value = _state.value.copy(showDeleteWindowDialog = false) }
    fun setDeleteWindowInput(v: String) { _state.value = _state.value.copy(deleteWindowInput = v) }
    fun saveDeleteWindow() {
        viewModelScope.launch {
            appPreferences.setDeleteWindowHours(_state.value.deleteWindowInput.toIntOrNull() ?: 24)
            _state.value = _state.value.copy(showDeleteWindowDialog = false)
        }
    }

    // Reminder Time
    fun showReminderTimeDialog() {
        _state.value = _state.value.copy(
            showReminderTimeDialog = true,
            reminderHour = _state.value.settings.saleReminderHour.toString(),
            reminderMinute = _state.value.settings.saleReminderMinute.toString()
        )
    }
    fun hideReminderTimeDialog() { _state.value = _state.value.copy(showReminderTimeDialog = false) }
    fun setReminderHour(v: String) { _state.value = _state.value.copy(reminderHour = v) }
    fun setReminderMinute(v: String) { _state.value = _state.value.copy(reminderMinute = v) }
    fun saveReminderTime() {
        viewModelScope.launch {
            val h = _state.value.reminderHour.toIntOrNull() ?: 20
            val m = _state.value.reminderMinute.toIntOrNull() ?: 0
            appPreferences.setSaleReminderTime(h, m)
            ReminderWorker.schedule(context)
            _state.value = _state.value.copy(showReminderTimeDialog = false)
        }
    }

    // Delete Account / Clear All Data
    fun showDeleteConfirm() { _state.value = _state.value.copy(showDeleteConfirm = true) }
    fun hideDeleteConfirm() { _state.value = _state.value.copy(showDeleteConfirm = false) }
    fun clearAllData() {
        viewModelScope.launch {
            appPreferences.clearAllData()
            _state.value = _state.value.copy(showDeleteConfirm = false)
        }
    }

    // Data Export
    fun showDataExportDialog() { _state.value = _state.value.copy(showDataExportDialog = true) }
    fun hideDataExportDialog() { _state.value = _state.value.copy(showDataExportDialog = false) }
    fun setExportFormat(format: String) { _state.value = _state.value.copy(exportFormat = format) }

    fun exportData(context: Context) {
        viewModelScope.launch {
            val uri = if (_state.value.exportFormat == "csv") {
                exportAsCsv(context)
            } else {
                exportAsJson(context)
            }
            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = if (_state.value.exportFormat == "csv") "text/csv" else "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "ডেটা এক্সপোর্ট"))
            }
            _state.value = _state.value.copy(showDataExportDialog = false)
        }
    }

    private fun exportAsJson(context: Context): Uri? {
        return try {
            val file = java.io.File(context.cacheDir, "hisab_export.json")
            val json = buildString {
                append("{")
                append("\"version\":1,")
                append("\"exportDate\":\"${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())}\"")
                append("}")
            }
            file.writeText(json)
            androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) { null }
    }

    private fun exportAsCsv(context: Context): Uri? {
        return try {
            val file = java.io.File(context.cacheDir, "hisab_export.csv")
            file.writeText("Export,Date,Version\n1,${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())},1")
            androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) { null }
    }

    // Data Import
    fun showDataImportDialog() { _state.value = _state.value.copy(showDataImportDialog = true, importData = "", importError = null) }
    fun hideDataImportDialog() { _state.value = _state.value.copy(showDataImportDialog = false) }
    fun setImportData(data: String) { _state.value = _state.value.copy(importData = data) }
    fun importData() {
        try {
            val json = org.json.JSONObject(_state.value.importData)
            val version = json.optInt("version", 0)
            if (version != 1) {
                _state.value = _state.value.copy(importError = "অজানা ভার্সন")
                return
            }
            _state.value = _state.value.copy(showDataImportDialog = false, importError = null)
        } catch (e: Exception) {
            _state.value = _state.value.copy(importError = "পার্স করতে ব্যর্থ: ${e.localizedMessage}")
        }
    }
}
