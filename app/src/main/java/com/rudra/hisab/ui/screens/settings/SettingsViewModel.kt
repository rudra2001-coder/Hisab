package com.rudra.hisab.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.preferences.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
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
    val editShopName: String = ""
)

enum class PinMode { SETUP, CHANGE_OLD, CHANGE_NEW, DISABLE }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.settings.collect { s ->
                _state.value = _state.value.copy(settings = s)
            }
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            appPreferences.setBangla(!_state.value.settings.isBangla)
        }
    }

    private fun hashPin(pin: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun showPinSetup() {
        _state.value = _state.value.copy(
            showPinDialog = true,
            pinMode = PinMode.SETUP,
            pinStep = 1,
            pinInput = "",
            pinConfirm = "",
            pinError = null
        )
    }

    fun showPinChange() {
        _state.value = _state.value.copy(
            showPinDialog = true,
            pinMode = PinMode.CHANGE_OLD,
            pinStep = 1,
            pinInput = "",
            pinConfirm = "",
            pinError = null
        )
    }

    fun showPinDisable() {
        _state.value = _state.value.copy(
            showPinDialog = true,
            pinMode = PinMode.DISABLE,
            pinStep = 1,
            pinInput = "",
            pinConfirm = "",
            pinError = null
        )
    }

    fun hidePinSetup() {
        _state.value = _state.value.copy(showPinDialog = false, pinInput = "", pinConfirm = "")
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
        _state.value = _state.value.copy(pinStep = 2, pinInput = "", pinConfirm = "", pinError = null)
    }

    fun verifyAndProceed() {
        val s = _state.value
        val currentHash = hashPin(s.pinInput)
        if (currentHash != s.settings.pinHash) {
            _state.value = _state.value.copy(pinError = "পুরনো পিন ভুল হয়েছে")
            return
        }
        when (s.pinMode) {
            PinMode.CHANGE_OLD -> {
                _state.value = _state.value.copy(
                    pinMode = PinMode.CHANGE_NEW,
                    pinStep = 1,
                    pinInput = "",
                    pinConfirm = "",
                    pinError = null
                )
            }
            PinMode.DISABLE -> {
                viewModelScope.launch {
                    appPreferences.setPinEnabled(false)
                    appPreferences.setPinHash("")
                    appPreferences.setBiometricEnabled(false)
                    hidePinSetup()
                }
            }
            else -> {}
        }
    }

    fun savePin() {
        val s = _state.value
        if (s.pinInput == s.pinConfirm && s.pinInput.length == 4) {
            viewModelScope.launch {
                appPreferences.setPinHash(hashPin(s.pinInput))
                appPreferences.setPinEnabled(true)
                hidePinSetup()
            }
        } else if (s.pinInput != s.pinConfirm) {
            _state.value = _state.value.copy(pinError = "পিন মিলছে না, আবার চেষ্টা করুন")
        }
    }

    fun toggleBiometric() {
        val s = _state.value
        if (!s.settings.isBiometricEnabled && !s.settings.isPinEnabled) {
            _state.value = _state.value.copy(pinError = "প্রথমে পিন সেট করুন")
            return
        }
        viewModelScope.launch {
            appPreferences.setBiometricEnabled(!s.settings.isBiometricEnabled)
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
            hideShopEdit()
        }
    }

    fun disablePin() {
        if (_state.value.settings.isPinEnabled) {
            showPinDisable()
        }
    }
}
