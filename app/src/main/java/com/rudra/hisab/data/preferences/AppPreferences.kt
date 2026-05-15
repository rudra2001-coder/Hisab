package com.rudra.hisab.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
    val isBiometricEnabled: Boolean = false,
    val themeMode: String = "system",
    val fontSize: String = "medium",
    val cartModeEnabled: Boolean = false,
    val fabModeEnabled: Boolean = false,
    val defaultCreditLimit: Double = 0.0,
    val deleteWindowHours: Int = 24,
    val saleReminderHour: Int = 20,
    val saleReminderMinute: Int = 0,
    val monthlyReportReminder: Boolean = false,
    val quickActions: String = "sale,stock,expense,customer",
    val navOrder: String = "dashboard,inventory,sale,customers,more",
    val batchTrackingEnabled: Boolean = false,
    val autoBackupEnabled: Boolean = false,
    val backupFrequency: String = "daily",
    val lastBackupTime: Long = 0L,
    val primaryColor: String = "",
    val secondaryColor: String = ""
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
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val CART_MODE_ENABLED = booleanPreferencesKey("cart_mode_enabled")
        val FAB_MODE_ENABLED = booleanPreferencesKey("fab_mode_enabled")
        val DEFAULT_CREDIT_LIMIT = stringPreferencesKey("default_credit_limit")
        val DELETE_WINDOW_HOURS = intPreferencesKey("delete_window_hours")
        val SALE_REMINDER_HOUR = intPreferencesKey("sale_reminder_hour")
        val SALE_REMINDER_MINUTE = intPreferencesKey("sale_reminder_minute")
        val MONTHLY_REPORT_REMINDER = booleanPreferencesKey("monthly_report_reminder")
        val QUICK_ACTIONS = stringPreferencesKey("quick_actions")
        val NAV_ORDER = stringPreferencesKey("nav_order")
        val BATCH_TRACKING_ENABLED = booleanPreferencesKey("batch_tracking_enabled")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val BACKUP_FREQUENCY = stringPreferencesKey("backup_frequency")
        val LAST_BACKUP_TIME = androidx.datastore.preferences.core.longPreferencesKey("last_backup_time")
        val PRIMARY_COLOR = stringPreferencesKey("primary_color")
        val SECONDARY_COLOR = stringPreferencesKey("secondary_color")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            isBangla = prefs[Keys.IS_BANGLA] ?: true,
            shopName = prefs[Keys.SHOP_NAME] ?: "",
            shopCategory = prefs[Keys.SHOP_CATEGORY] ?: "",
            hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
            isPinEnabled = prefs[Keys.IS_PIN_ENABLED] ?: false,
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            isBiometricEnabled = prefs[Keys.IS_BIOMETRIC_ENABLED] ?: false,
            themeMode = prefs[Keys.THEME_MODE] ?: "system",
            fontSize = prefs[Keys.FONT_SIZE] ?: "medium",
            cartModeEnabled = prefs[Keys.CART_MODE_ENABLED] ?: false,
            fabModeEnabled = prefs[Keys.FAB_MODE_ENABLED] ?: false,
            defaultCreditLimit = prefs[Keys.DEFAULT_CREDIT_LIMIT]?.toDoubleOrNull() ?: 0.0,
            deleteWindowHours = prefs[Keys.DELETE_WINDOW_HOURS] ?: 24,
            saleReminderHour = prefs[Keys.SALE_REMINDER_HOUR] ?: 20,
            saleReminderMinute = prefs[Keys.SALE_REMINDER_MINUTE] ?: 0,
            monthlyReportReminder = prefs[Keys.MONTHLY_REPORT_REMINDER] ?: false,
            quickActions = prefs[Keys.QUICK_ACTIONS] ?: "sale,stock,expense,customer",
            navOrder = prefs[Keys.NAV_ORDER] ?: "dashboard,inventory,sale,customers,more",
            batchTrackingEnabled = prefs[Keys.BATCH_TRACKING_ENABLED] ?: false,
            autoBackupEnabled = prefs[Keys.AUTO_BACKUP_ENABLED] ?: false,
            backupFrequency = prefs[Keys.BACKUP_FREQUENCY] ?: "daily",
            lastBackupTime = prefs[Keys.LAST_BACKUP_TIME] ?: 0L,
            primaryColor = prefs[Keys.PRIMARY_COLOR] ?: "",
            secondaryColor = prefs[Keys.SECONDARY_COLOR] ?: ""
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

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setFontSize(size: String) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = size }
    }

    suspend fun setCartModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CART_MODE_ENABLED] = enabled }
    }

    suspend fun setFabModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FAB_MODE_ENABLED] = enabled }
    }

    suspend fun setDefaultCreditLimit(limit: Double) {
        context.dataStore.edit { it[Keys.DEFAULT_CREDIT_LIMIT] = limit.toString() }
    }

    suspend fun setDeleteWindowHours(hours: Int) {
        context.dataStore.edit { it[Keys.DELETE_WINDOW_HOURS] = hours }
    }

    suspend fun setSaleReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.SALE_REMINDER_HOUR] = hour
            it[Keys.SALE_REMINDER_MINUTE] = minute
        }
    }

    suspend fun setMonthlyReportReminder(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MONTHLY_REPORT_REMINDER] = enabled }
    }

    suspend fun setQuickActions(actions: String) {
        context.dataStore.edit { it[Keys.QUICK_ACTIONS] = actions }
    }

    suspend fun setNavOrder(order: String) {
        context.dataStore.edit { it[Keys.NAV_ORDER] = order }
    }

    suspend fun setBatchTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BATCH_TRACKING_ENABLED] = enabled }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_BACKUP_ENABLED] = enabled }
    }

    suspend fun setBackupFrequency(frequency: String) {
        context.dataStore.edit { it[Keys.BACKUP_FREQUENCY] = frequency }
    }

    suspend fun setLastBackupTime(time: Long) {
        context.dataStore.edit { it[Keys.LAST_BACKUP_TIME] = time }
    }

    suspend fun setPrimaryColor(color: String) {
        context.dataStore.edit { it[Keys.PRIMARY_COLOR] = color }
    }

    suspend fun setSecondaryColor(color: String) {
        context.dataStore.edit { it[Keys.SECONDARY_COLOR] = color }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { it.clear() }
    }
}
