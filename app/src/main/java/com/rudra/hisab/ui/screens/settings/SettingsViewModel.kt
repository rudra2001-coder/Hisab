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
import javax.inject.Inject

data class SettingsState(
    val settings: AppSettings = AppSettings(),
    val showPinDialog: Boolean = false,
    val pinInput: String = "",
    val pinConfirm: String = "",
    val pinStep: Int = 1,
    val showShopEdit: Boolean = false,
    val editShopName: String = ""
)

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

    fun showPinSetup() {
        _state.value = _state.value.copy(
            showPinDialog = true,
            pinStep = 1,
            pinInput = "",
            pinConfirm = ""
        )
    }

    fun hidePinSetup() {
        _state.value = _state.value.copy(showPinDialog = false, pinInput = "", pinConfirm = "")
    }

    fun setPinInput(pin: String) {
        if (pin.length <= 4) {
            _state.value = _state.value.copy(pinInput = pin)
        }
    }

    fun setPinConfirm(pin: String) {
        if (pin.length <= 4) {
            _state.value = _state.value.copy(pinConfirm = pin)
        }
    }

    fun nextPinStep() {
        _state.value = _state.value.copy(pinStep = 2)
    }

    fun savePin() {
        val s = _state.value
        if (s.pinInput == s.pinConfirm && s.pinInput.length == 4) {
            viewModelScope.launch {
                appPreferences.setPinHash(s.pinInput) 
                appPreferences.setPinEnabled(true)
                hidePinSetup()
            }
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
            hideShopEdit()
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            appPreferences.setPinEnabled(false)
            appPreferences.setPinHash("")
        }
    }
}
