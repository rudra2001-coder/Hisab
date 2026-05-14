package com.rudra.hisab.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "hisab_preferences")

data class AppSettings(
    val isBangla: Boolean = true,
    val shopName: String = "",
    val shopCategory: String = "",
    val hasCompletedOnboarding: Boolean = false,
    val isPinEnabled: Boolean = false,
    val pinHash: String = "",
    val isBiometricEnabled: Boolean = false
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val IS_BANGLA = booleanPreferencesKey("is_bangla")
        val SHOP_NAME = stringPreferencesKey("shop_name")
        val SHOP_CATEGORY = stringPreferencesKey("shop_category")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            isBangla = prefs[Keys.IS_BANGLA] ?: true,
            shopName = prefs[Keys.SHOP_NAME] ?: "",
            shopCategory = prefs[Keys.SHOP_CATEGORY] ?: "",
            hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
            isPinEnabled = prefs[Keys.IS_PIN_ENABLED] ?: false,
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            isBiometricEnabled = prefs[Keys.IS_BIOMETRIC_ENABLED] ?: false
        )
    }

    suspend fun setBangla(isBangla: Boolean) {
        context.dataStore.edit { it[Keys.IS_BANGLA] = isBangla }
    }

    suspend fun setShopName(name: String) {
        context.dataStore.edit { it[Keys.SHOP_NAME] = name }
    }

    suspend fun setShopCategory(category: String) {
        context.dataStore.edit { it[Keys.SHOP_CATEGORY] = category }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = true }
    }

    suspend fun setPinEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_PIN_ENABLED] = enabled }
    }

    suspend fun setPinHash(hash: String) {
        context.dataStore.edit { it[Keys.PIN_HASH] = hash }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_BIOMETRIC_ENABLED] = enabled }
    }
}
