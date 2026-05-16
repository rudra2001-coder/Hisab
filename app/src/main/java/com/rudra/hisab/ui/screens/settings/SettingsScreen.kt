package com.rudra.hisab.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.RedExpense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val isBangla = state.settings.isBangla

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (isBangla) "সেটিংস" else "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsCard(title = if (isBangla) "ভাষা ও থিম" else "Language & Theme") {
            SettingsRow(
                icon = Icons.Default.Language,
                title = if (isBangla) "ভাষা" else "Language",
                subtitle = if (state.settings.isBangla) "বাংলা" else "English",
                trailing = {
                    Switch(
                        checked = state.settings.isBangla,
                        onCheckedChange = { viewModel.toggleLanguage() }
                    )
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Brightness6,
                title = if (isBangla) "থিম" else "Theme",
                subtitle = when (state.settings.themeMode) {
                    "light" -> if (isBangla) "হালকা" else "Light"
                    "dark" -> if (isBangla) "গাঢ়" else "Dark"
                    else -> if (isBangla) "সিস্টেম" else "System"
                },
                onClick = { viewModel.showThemeSelector() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.TextFields,
                title = if (isBangla) "ফন্ট সাইজ" else "Font Size",
                subtitle = when (state.settings.fontSize) {
                    "small" -> if (isBangla) "ছোট" else "Small"
                    "large" -> if (isBangla) "বড়" else "Large"
                    else -> if (isBangla) "মধ্যম" else "Medium"
                },
                onClick = { viewModel.showFontSelector() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = if (isBangla) "দোকান" else "Shop") {
            SettingsRow(
                icon = Icons.Default.Store,
                title = if (isBangla) "দোকানের তথ্য" else "Shop Info",
                subtitle = state.settings.shopName.ifEmpty {
                    if (isBangla) "নাম সেট করুন" else "Set name"
                },
                onClick = { viewModel.showShopEdit() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = if (isBangla) "নিরাপত্তা" else "Security") {
            SettingsRow(
                icon = Icons.Default.Lock,
                title = if (isBangla) "পিন" else "PIN",
                subtitle = if (state.settings.isPinEnabled)
                    (if (isBangla) "পিন চালু" else "PIN On")
                else
                    (if (isBangla) "পিন বন্ধ" else "PIN Off"),
                onClick = {
                    if (state.settings.isPinEnabled) viewModel.showPinChange()
                    else viewModel.showPinSetup()
                }
            )
            if (state.settings.isPinEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = if (isBangla) "পিন সরান" else "Remove PIN",
                    subtitle = if (isBangla) "পিন নিষ্ক্রিয় করুন" else "Disable PIN",
                    onClick = { viewModel.showPinDisable() }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Fingerprint,
                title = if (isBangla) "বায়োমেট্রিক লক" else "Biometric Lock",
                subtitle = if (state.settings.isBiometricEnabled)
                    (if (isBangla) "চালু" else "On")
                else
                    (if (isBangla) "বন্ধ" else "Off"),
                trailing = {
                    Switch(
                        checked = state.settings.isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric() }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = if (isBangla) "বিক্রয় ও ইনভেন্টরি" else "Sales & Inventory") {
            SettingsRow(
                icon = Icons.Default.ShoppingCart,
                title = if (isBangla) "কার্ট মোড" else "Cart Mode",
                subtitle = if (state.settings.cartModeEnabled)
                    (if (isBangla) "চালু" else "On")
                else
                    (if (isBangla) "বন্ধ" else "Off"),
                trailing = {
                    Switch(checked = state.settings.cartModeEnabled, onCheckedChange = { viewModel.toggleCartMode() })
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Dashboard,
                title = "FAB Mode",
                subtitle = if (state.settings.fabModeEnabled)
                    (if (isBangla) "চালু" else "On")
                else
                    (if (isBangla) "বন্ধ" else "Off"),
                trailing = {
                    Switch(checked = state.settings.fabModeEnabled, onCheckedChange = { viewModel.toggleFabMode() })
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Inventory2,
                title = if (isBangla) "ব্যাচ/মেয়াদ ট্র্যাকিং" else "Batch Tracking",
                subtitle = if (state.settings.batchTrackingEnabled)
                    (if (isBangla) "চালু" else "On")
                else
                    (if (isBangla) "বন্ধ" else "Off"),
                trailing = {
                    Switch(checked = state.settings.batchTrackingEnabled, onCheckedChange = { viewModel.toggleBatchTracking() })
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = if (isBangla) "গ্রাহক ও লেনদেন" else "Customers & Transactions") {
            SettingsRow(
                icon = Icons.Default.Timer,
                title = if (isBangla) "ক্রেডিট লিমিট" else "Credit Limit",
                subtitle = if (state.settings.defaultCreditLimit > 0)
                    "৳${state.settings.defaultCreditLimit.toLong()}"
                else
                    (if (isBangla) "সীমাহীন" else "Unlimited"),
                onClick = { viewModel.showCreditLimitDialog() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Delete,
                title = if (isBangla) "মুছার সময়সীমা" else "Delete Window",
                subtitle = "${state.settings.deleteWindowHours} ${if (isBangla) "ঘন্টা" else "hours"}",
                onClick = { viewModel.showDeleteWindowDialog() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = if (isBangla) "নোটিফিকেশন" else "Notifications") {
            SettingsRow(
                icon = Icons.Default.Schedule,
                title = if (isBangla) "বিক্রয় রিমাইন্ডার সময়" else "Sale Reminder",
                subtitle = "${state.settings.saleReminderHour}:${String.format("%02d", state.settings.saleReminderMinute)}",
                onClick = { viewModel.showReminderTimeDialog() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Notifications,
                title = if (isBangla) "মাসিক রিপোর্ট রিমাইন্ডার" else "Monthly Report",
                subtitle = if (state.settings.monthlyReportReminder)
                    (if (isBangla) "চালু" else "On")
                else
                    (if (isBangla) "বন্ধ" else "Off"),
                trailing = {
                    Switch(checked = state.settings.monthlyReportReminder, onCheckedChange = { viewModel.toggleMonthlyReport() })
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = if (isBangla) "নেভিগেশন" else "Navigation") {
            SettingsRow(
                icon = Icons.Default.Dashboard,
                title = if (isBangla) "নেভিগেশন অর্ডার" else "Nav Order",
                subtitle = if (isBangla) "নিচের মেনুর অর্ডার পরিবর্তন" else "Change bottom menu order",
                onClick = { viewModel.showNavOrderEditor() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Dashboard,
                title = if (isBangla) "দ্রুত অ্যাকশন" else "Quick Actions",
                subtitle = if (isBangla) "ড্যাশবোর্ডে কোন বাটন দেখাবে" else "Which buttons on dashboard",
                onClick = { viewModel.showQuickActionsEditor() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = if (isBangla) "ডেটা ও ব্যাকআপ" else "Data & Backup") {
            SettingsRow(
                icon = Icons.Default.CloudUpload,
                title = if (isBangla) "অটো ব্যাকআপ" else "Auto Backup",
                subtitle = if (state.settings.autoBackupEnabled)
                    "${if (isBangla) "চালু" else "On"} (${if (state.settings.backupFrequency == "daily") (if (isBangla) "প্রতিদিন" else "Daily") else (if (isBangla) "সাপ্তাহিক" else "Weekly")})"
                else
                    (if (isBangla) "বন্ধ" else "Off"),
                trailing = {
                    Switch(
                        checked = state.settings.autoBackupEnabled,
                        onCheckedChange = { viewModel.toggleAutoBackup() }
                    )
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Schedule,
                title = if (isBangla) "ব্যাকআপ ফ্রিকোয়েন্সি" else "Backup Frequency",
                subtitle = if (state.settings.backupFrequency == "daily")
                    (if (isBangla) "প্রতিদিন" else "Daily")
                else
                    (if (isBangla) "সাপ্তাহিক" else "Weekly"),
                onClick = { viewModel.showBackupDialog() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.CloudUpload,
                title = if (isBangla) "এখনই ব্যাকআপ নিন" else "Backup Now",
                subtitle = if (state.settings.lastBackupTime > 0)
                    "${if (isBangla) "সর্বশেষ" else "Last"}: ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(state.settings.lastBackupTime))}"
                else
                    (if (isBangla) "কখনো ব্যাকআপ নেয়া হয়নি" else "Never backed up"),
                onClick = { viewModel.performManualBackup() },
                trailing = {
                    if (state.isBackingUp) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else if (state.backupSuccess == true) {
                        Icon(androidx.compose.material.icons.Icons.Default.CheckCircle, contentDescription = null, tint = GreenProfit)
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.FileDownload,
                title = if (isBangla) "ডেটা এক্সপোর্ট" else "Data Export",
                subtitle = "JSON / CSV",
                onClick = { viewModel.showDataExportDialog() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.FileUpload,
                title = if (isBangla) "ডেটা ইম্পোর্ট" else "Data Import",
                subtitle = if (isBangla) "JSON ফাইল থেকে পুনরুদ্ধার" else "Restore from JSON",
                onClick = { viewModel.showDataImportDialog() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RedExpense.copy(alpha = 0.08f))
        ) {
            SettingsRow(
                icon = Icons.Default.ClearAll,
                title = if (isBangla) "সব ডেটা মুছুন" else "Clear All Data",
                subtitle = if (isBangla) "সমস্ত তথ্য স্থায়ীভাবে মুছে ফেলুন" else "Permanently delete all data",
                onClick = { viewModel.showDeleteConfirm() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = if (isBangla) "অ্যাপ" else "App") {
            SettingsRow(
                icon = Icons.Default.Info,
                title = if (isBangla) "সম্পর্কে" else "About",
                subtitle = "Hisab v2.0"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Dialogs
    PinSetupDialog(state, viewModel, isBangla)
    ShopEditDialog(state, viewModel, isBangla)
    ThemeSelectorDialog(state, viewModel, isBangla)
    FontSelectorDialog(state, viewModel, isBangla)
    CreditLimitDialog(state, viewModel, isBangla)
    DeleteWindowDialog(state, viewModel, isBangla)
    ReminderTimeDialog(state, viewModel, isBangla)
    NavOrderDialog(state, viewModel, isBangla)
    QuickActionsDialog(state, viewModel, isBangla)
    DataExportDialog(state, viewModel, context, isBangla)
    DataImportDialog(state, viewModel, isBangla)
    DeleteConfirmDialog(state, viewModel, isBangla)
    BackupFrequencyDialog(state, viewModel, isBangla)
}

@Composable
private fun BackupFrequencyDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showBackupDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideBackupDialog() },
        title = { Text(if (isBangla) "ব্যাকআপ ফ্রিকোয়েন্সি" else "Backup Frequency") },
        text = {
            Column {
                val items = if (isBangla) listOf("daily" to "প্রতিদিন", "weekly" to "সাপ্তাহিক") else listOf("daily" to "Daily", "weekly" to "Weekly")
                items.forEach { (key, label) ->
                    TextButton(
                        onClick = { viewModel.setBackupFrequency(key); viewModel.hideBackupDialog() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.settings.backupFrequency == key) "✓ $label" else label, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.hideBackupDialog() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun SettingsCard(
    title: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 4.dp)
            )
        }
        content()
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PinSetupDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showPinDialog) return
    val dialogTitle = when (state.pinMode) {
        PinMode.SETUP -> if (isBangla) "পিন সেটআপ" else "Pin Setup"
        PinMode.CHANGE -> if (state.pinStep == 1) (if (isBangla) "পুরনো পিন" else "Old Pin") else (if (isBangla) "নতুন পিন" else "New Pin")
        PinMode.DISABLE -> if (isBangla) "পিন সরান" else "Remove Pin"
    }
    AlertDialog(
        onDismissRequest = { viewModel.hidePinSetup() },
        title = { Text(dialogTitle) },
        text = {
            Column {
                val promptText = when {
                    state.pinMode == PinMode.DISABLE -> if (isBangla) "পিন দিন" else "Enter PIN"
                    state.pinMode == PinMode.CHANGE && state.pinStep == 1 -> if (isBangla) "বর্তমান পিন দিন" else "Enter current PIN"
                    state.pinStep == 1 -> if (isBangla) "নতুন ৪-ডিজিটের পিন দিন" else "Enter new 4-digit PIN"
                    else -> if (isBangla) "পিন আবার দিন" else "Re-enter PIN"
                }
                Text(promptText)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = if (state.pinStep == 1) state.pinInput else state.pinConfirm,
                    onValueChange = { if (state.pinStep == 1) viewModel.setPinInput(it) else viewModel.setPinConfirm(it) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text(if (isBangla) "পিন" else "PIN") }
                )
                state.pinError?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            val (text, enabled, onClick) = when {
                state.pinMode == PinMode.DISABLE -> Triple(if (isBangla) "নিশ্চিত" else "Confirm", state.pinInput.length == 4, { viewModel.verifyAndProceed() })
                state.pinMode == PinMode.CHANGE && state.pinStep == 1 -> Triple(if (isBangla) "পরবর্তী" else "Next", state.pinInput.length == 4, { viewModel.verifyAndProceed() })
                state.pinStep == 1 -> Triple(if (isBangla) "পরবর্তী" else "Next", state.pinInput.length == 4, { viewModel.nextPinStep() })
                else -> Triple(if (isBangla) "সংরক্ষণ" else "Save", state.pinConfirm.length == 4 && state.pinInput == state.pinConfirm, { viewModel.savePin() })
            }
            Button(onClick = onClick, enabled = enabled) { Text(text) }
        },
        dismissButton = { TextButton(onClick = { viewModel.hidePinSetup() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun ShopEditDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showShopEdit) return
    AlertDialog(
        onDismissRequest = { viewModel.hideShopEdit() },
        title = { Text(if (isBangla) "দোকানের নাম" else "Shop Name") },
        text = { OutlinedTextField(value = state.editShopName, onValueChange = viewModel::setEditShopName, modifier = Modifier.fillMaxWidth(), singleLine = true) },
        confirmButton = { Button(onClick = { viewModel.saveShopName() }) { Text(if (isBangla) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { viewModel.hideShopEdit() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun ThemeSelectorDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showThemeSelector) return
    AlertDialog(
        onDismissRequest = { viewModel.hideThemeSelector() },
        title = { Text(if (isBangla) "থিম" else "Theme") },
        text = {
            Column {
                val items = if (isBangla) listOf("system" to "সিস্টেম", "light" to "হালকা", "dark" to "গাঢ়") else listOf("system" to "System", "light" to "Light", "dark" to "Dark")
                items.forEach { (key, label) ->
                    TextButton(
                        onClick = { viewModel.setThemeMode(key) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.settings.themeMode == key) "✓ $label" else label, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.hideThemeSelector() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun FontSelectorDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showFontSelector) return
    AlertDialog(
        onDismissRequest = { viewModel.hideFontSelector() },
        title = { Text(if (isBangla) "ফন্ট সাইজ" else "Font Size") },
        text = {
            Column {
                val items = if (isBangla) listOf("small" to "ছোট", "medium" to "মধ্যম", "large" to "বড়") else listOf("small" to "Small", "medium" to "Medium", "large" to "Large")
                items.forEach { (key, label) ->
                    TextButton(
                        onClick = { viewModel.setFontSize(key) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.settings.fontSize == key) "✓ $label" else label, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.hideFontSelector() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun CreditLimitDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showCreditLimitDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideCreditLimitDialog() },
        title = { Text(if (isBangla) "ক্রেডিট লিমিট" else "Credit Limit") },
        text = {
            Column {
                Text(if (isBangla) "প্রতি গ্রাহকের জন্য সর্বোচ্চ বাকির পরিমাণ (০ = সীমাহীন)" else "Maximum credit per customer (0 = unlimited)")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.creditLimitInput,
                    onValueChange = viewModel::setCreditLimitInput,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = { viewModel.saveCreditLimit() }) { Text(if (isBangla) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { viewModel.hideCreditLimitDialog() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun DeleteWindowDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showDeleteWindowDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideDeleteWindowDialog() },
        title = { Text(if (isBangla) "মুছার সময়সীমা" else "Delete Window") },
        text = {
            Column {
                Text(if (isBangla) "কত ঘন্টার মধ্যে লেনদেন মুছতে পারবেন?" else "Within how many hours can you delete a transaction?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.deleteWindowInput,
                    onValueChange = viewModel::setDeleteWindowInput,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = { viewModel.saveDeleteWindow() }) { Text(if (isBangla) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { viewModel.hideDeleteWindowDialog() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun ReminderTimeDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showReminderTimeDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideReminderTimeDialog() },
        title = { Text(if (isBangla) "রিমাইন্ডার সময়" else "Reminder Time") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.reminderHour,
                        onValueChange = viewModel::setReminderHour,
                        label = { Text(if (isBangla) "ঘন্টা" else "Hour") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.reminderMinute,
                        onValueChange = viewModel::setReminderMinute,
                        label = { Text(if (isBangla) "মিনিট" else "Minute") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = { Button(onClick = { viewModel.saveReminderTime() }) { Text(if (isBangla) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { viewModel.hideReminderTimeDialog() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun NavOrderDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showNavOrderEditor) return
    val order = remember { mutableStateOf(state.settings.navOrder) }
    AlertDialog(
        onDismissRequest = { viewModel.hideNavOrderEditor() },
        title = { Text(if (isBangla) "নেভিগেশন অর্ডার" else "Navigation Order") },
        text = {
            Column {
                Text(if (isBangla) "কমা দিয়ে আলাদা করুন: dashboard,inventory,sale,customers,more" else "Separate with commas: dashboard,inventory,sale,customers,more")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = order.value,
                    onValueChange = { order.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = { viewModel.setNavOrder(order.value) }) { Text(if (isBangla) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { viewModel.hideNavOrderEditor() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun QuickActionsDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showQuickActionsEditor) return
    val actions = remember { mutableStateOf(state.settings.quickActions) }
    AlertDialog(
        onDismissRequest = { viewModel.hideQuickActionsEditor() },
        title = { Text(if (isBangla) "দ্রুত অ্যাকশন" else "Quick Actions") },
        text = {
            Column {
                Text(if (isBangla) "কমা দিয়ে আলাদা করুন: sale,stock,expense,customer,purchase" else "Separate with commas: sale,stock,expense,customer,purchase")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = actions.value,
                    onValueChange = { actions.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = { viewModel.setQuickActions(actions.value) }) { Text(if (isBangla) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { viewModel.hideQuickActionsEditor() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun DataExportDialog(state: SettingsState, viewModel: SettingsViewModel, context: android.content.Context, isBangla: Boolean) {
    if (!state.showDataExportDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideDataExportDialog() },
        title = { Text(if (isBangla) "ডেটা এক্সপোর্ট" else "Data Export") },
        text = {
            Column {
                Text(if (isBangla) "ফরম্যাট নির্বাচন করুন:" else "Select format:")
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.setExportFormat("json") },
                        colors = if (state.exportFormat == "json") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) { Text("JSON") }
                    Button(
                        onClick = { viewModel.setExportFormat("csv") },
                        colors = if (state.exportFormat == "csv") ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) { Text("CSV") }
                }
            }
        },
        confirmButton = { Button(onClick = { viewModel.exportData(context) }) { Text(if (isBangla) "এক্সপোর্ট" else "Export") } },
        dismissButton = { TextButton(onClick = { viewModel.hideDataExportDialog() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun DataImportDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showDataImportDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideDataImportDialog() },
        title = { Text(if (isBangla) "ডেটা ইম্পোর্ট" else "Data Import") },
        text = {
            Column {
                Text(if (isBangla) "JSON ডেটা পেস্ট করুন:" else "Paste JSON data:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.importData,
                    onValueChange = viewModel::setImportData,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    maxLines = 10
                )
                state.importError?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, color = RedExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { Button(onClick = { viewModel.importData() }) { Text(if (isBangla) "ইম্পোর্ট" else "Import") } },
        dismissButton = { TextButton(onClick = { viewModel.hideDataImportDialog() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun DeleteConfirmDialog(state: SettingsState, viewModel: SettingsViewModel, isBangla: Boolean) {
    if (!state.showDeleteConfirm) return
    AlertDialog(
        onDismissRequest = { viewModel.hideDeleteConfirm() },
        title = { Text(if (isBangla) "সব ডেটা মুছুন" else "Delete All Data") },
        text = { Text(if (isBangla) "সমস্ত তথ্য স্থায়ীভাবে মুছে যাবে! এই কাজ পূর্বাবস্থায় ফেরানো যাবে না। আপনি কি নিশ্চিত?" else "All data will be permanently deleted! This action cannot be undone. Are you sure?") },
        confirmButton = {
            Button(
                onClick = { viewModel.clearAllData() },
                colors = ButtonDefaults.buttonColors(containerColor = RedExpense)
            ) { Text(if (isBangla) "মুছুন" else "Delete", color = androidx.compose.ui.graphics.Color.White) }
        },
        dismissButton = { TextButton(onClick = { viewModel.hideDeleteConfirm() }) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}
