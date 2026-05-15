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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "সেটিংস",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsCard(title = "ভাষা ও থিম") {
            SettingsRow(
                icon = Icons.Default.Language,
                title = "ভাষা",
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
                title = "থিম",
                subtitle = when (state.settings.themeMode) { "light" -> "হালকা"; "dark" -> "গাঢ়"; else -> "সিস্টেম" },
                onClick = { viewModel.showThemeSelector() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.TextFields,
                title = "ফন্ট সাইজ",
                subtitle = when (state.settings.fontSize) { "small" -> "ছোট"; "large" -> "বড়"; else -> "মধ্যম" },
                onClick = { viewModel.showFontSelector() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = "দোকান") {
            SettingsRow(
                icon = Icons.Default.Store,
                title = "দোকানের তথ্য",
                subtitle = state.settings.shopName.ifEmpty { "নাম সেট করুন" },
                onClick = { viewModel.showShopEdit() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = "নিরাপত্তা") {
            SettingsRow(
                icon = Icons.Default.Lock,
                title = "পিন",
                subtitle = if (state.settings.isPinEnabled) "পিন চালু" else "পিন বন্ধ",
                onClick = {
                    if (state.settings.isPinEnabled) viewModel.showPinChange()
                    else viewModel.showPinSetup()
                }
            )
            if (state.settings.isPinEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = "পিন সরান",
                    subtitle = "পিন নিষ্ক্রিয় করুন",
                    onClick = { viewModel.showPinDisable() }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Fingerprint,
                title = "বায়োমেট্রিক লক",
                subtitle = if (state.settings.isBiometricEnabled) "চালু" else "বন্ধ",
                trailing = {
                    Switch(
                        checked = state.settings.isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric() }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = "বিক্রয় ও ইনভেন্টরি") {
            SettingsRow(
                icon = Icons.Default.ShoppingCart,
                title = "কার্ট মোড",
                subtitle = if (state.settings.cartModeEnabled) "চালু" else "বন্ধ",
                trailing = {
                    Switch(checked = state.settings.cartModeEnabled, onCheckedChange = { viewModel.toggleCartMode() })
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Dashboard,
                title = "FAB মোড",
                subtitle = if (state.settings.fabModeEnabled) "চালু" else "বন্ধ",
                trailing = {
                    Switch(checked = state.settings.fabModeEnabled, onCheckedChange = { viewModel.toggleFabMode() })
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Inventory2,
                title = "ব্যাচ/মেয়াদ ট্র্যাকিং",
                subtitle = if (state.settings.batchTrackingEnabled) "চালু" else "বন্ধ",
                trailing = {
                    Switch(checked = state.settings.batchTrackingEnabled, onCheckedChange = { viewModel.toggleBatchTracking() })
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = "গ্রাহক ও লেনদেন") {
            SettingsRow(
                icon = Icons.Default.Timer,
                title = "ক্রেডিট লিমিট",
                subtitle = if (state.settings.defaultCreditLimit > 0) "৳${state.settings.defaultCreditLimit.toLong()}" else "সীমাহীন",
                onClick = { viewModel.showCreditLimitDialog() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Delete,
                title = "মুছার সময়সীমা",
                subtitle = "${state.settings.deleteWindowHours} ঘন্টা",
                onClick = { viewModel.showDeleteWindowDialog() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = "নোটিফিকেশন") {
            SettingsRow(
                icon = Icons.Default.Schedule,
                title = "বিক্রয় রিমাইন্ডার সময়",
                subtitle = "${state.settings.saleReminderHour}:${String.format("%02d", state.settings.saleReminderMinute)}",
                onClick = { viewModel.showReminderTimeDialog() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Notifications,
                title = "মাসিক রিপোর্ট রিমাইন্ডার",
                subtitle = if (state.settings.monthlyReportReminder) "চালু" else "বন্ধ",
                trailing = {
                    Switch(checked = state.settings.monthlyReportReminder, onCheckedChange = { viewModel.toggleMonthlyReport() })
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = "নেভিগেশন") {
            SettingsRow(
                icon = Icons.Default.Dashboard,
                title = "নেভিগেশন অর্ডার",
                subtitle = "নিচের মেনুর অর্ডার পরিবর্তন",
                onClick = { viewModel.showNavOrderEditor() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Dashboard,
                title = "দ্রুত অ্যাকশন",
                subtitle = "ড্যাশবোর্ডে কোন বাটন দেখাবে",
                onClick = { viewModel.showQuickActionsEditor() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = "ডেটা ও ব্যাকআপ") {
            SettingsRow(
                icon = Icons.Default.CloudUpload,
                title = "অটো ব্যাকআপ",
                subtitle = if (state.settings.autoBackupEnabled) "চালু (${if (state.settings.backupFrequency == "daily") "প্রতিদিন" else "সাপ্তাহিক"})" else "বন্ধ",
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
                title = "ব্যাকআপ ফ্রিকোয়েন্সি",
                subtitle = if (state.settings.backupFrequency == "daily") "প্রতিদিন" else "সাপ্তাহিক",
                onClick = { viewModel.showBackupDialog() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.CloudUpload,
                title = "এখনই ব্যাকআপ নিন",
                subtitle = if (state.settings.lastBackupTime > 0) "সর্বশেষ: ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(state.settings.lastBackupTime))}" else "কখনো ব্যাকআপ নেয়া হয়নি",
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
                title = "ডেটা এক্সপোর্ট",
                subtitle = "JSON / CSV ফরম্যাট",
                onClick = { viewModel.showDataExportDialog() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.FileUpload,
                title = "ডেটা ইম্পোর্ট",
                subtitle = "JSON ফাইল থেকে পুনরুদ্ধার",
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
                title = "সব ডেটা মুছুন",
                subtitle = "সমস্ত তথ্য স্থায়ীভাবে মুছে ফেলুন",
                onClick = { viewModel.showDeleteConfirm() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = "অ্যাপ") {
            SettingsRow(
                icon = Icons.Default.Info,
                title = "সম্পর্কে",
                subtitle = "Hisab v1.0"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Dialogs
    PinSetupDialog(state, viewModel)
    ShopEditDialog(state, viewModel)
    ThemeSelectorDialog(state, viewModel)
    FontSelectorDialog(state, viewModel)
    CreditLimitDialog(state, viewModel)
    DeleteWindowDialog(state, viewModel)
    ReminderTimeDialog(state, viewModel)
    NavOrderDialog(state, viewModel)
    QuickActionsDialog(state, viewModel)
    DataExportDialog(state, viewModel, context)
    DataImportDialog(state, viewModel)
    DeleteConfirmDialog(state, viewModel)
    BackupFrequencyDialog(state, viewModel)
}

@Composable
private fun BackupFrequencyDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showBackupDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideBackupDialog() },
        title = { Text("ব্যাকআপ ফ্রিকোয়েন্সি") },
        text = {
            Column {
                listOf("daily" to "প্রতিদিন", "weekly" to "সাপ্তাহিক").forEach { (key, label) ->
                    TextButton(
                        onClick = { viewModel.setBackupFrequency(key); viewModel.hideBackupDialog() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.settings.backupFrequency == key) "✓ $label" else label, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.hideBackupDialog() }) { Text("বাতিল") } }
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
private fun PinSetupDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showPinDialog) return
    val dialogTitle = when (state.pinMode) {
        PinMode.SETUP -> "পিন সেটআপ"
        PinMode.CHANGE -> if (state.pinStep == 1) "পুরনো পিন" else "নতুন পিন"
        PinMode.DISABLE -> "পিন সরান"
    }
    AlertDialog(
        onDismissRequest = { viewModel.hidePinSetup() },
        title = { Text(dialogTitle) },
        text = {
            Column {
                val promptText = when {
                    state.pinMode == PinMode.DISABLE -> "পিন দিন"
                    state.pinMode == PinMode.CHANGE && state.pinStep == 1 -> "বর্তমান পিন দিন"
                    state.pinStep == 1 -> "নতুন ৪-ডিজিটের পিন দিন"
                    else -> "পিন আবার দিন"
                }
                Text(promptText)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = if (state.pinStep == 1) state.pinInput else state.pinConfirm,
                    onValueChange = { if (state.pinStep == 1) viewModel.setPinInput(it) else viewModel.setPinConfirm(it) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("পিন") }
                )
                state.pinError?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            val (text, enabled, onClick) = when {
                state.pinMode == PinMode.DISABLE -> Triple("নিশ্চিত", state.pinInput.length == 4, { viewModel.verifyAndProceed() })
                state.pinMode == PinMode.CHANGE && state.pinStep == 1 -> Triple("পরবর্তী", state.pinInput.length == 4, { viewModel.verifyAndProceed() })
                state.pinStep == 1 -> Triple("পরবর্তী", state.pinInput.length == 4, { viewModel.nextPinStep() })
                else -> Triple("সংরক্ষণ", state.pinConfirm.length == 4 && state.pinInput == state.pinConfirm, { viewModel.savePin() })
            }
            Button(onClick = onClick, enabled = enabled) { Text(text) }
        },
        dismissButton = { TextButton(onClick = { viewModel.hidePinSetup() }) { Text("বাতিল") } }
    )
}

@Composable
private fun ShopEditDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showShopEdit) return
    AlertDialog(
        onDismissRequest = { viewModel.hideShopEdit() },
        title = { Text("দোকানের নাম") },
        text = { OutlinedTextField(value = state.editShopName, onValueChange = viewModel::setEditShopName, modifier = Modifier.fillMaxWidth(), singleLine = true) },
        confirmButton = { Button(onClick = { viewModel.saveShopName() }) { Text("সংরক্ষণ") } },
        dismissButton = { TextButton(onClick = { viewModel.hideShopEdit() }) { Text("বাতিল") } }
    )
}

@Composable
private fun ThemeSelectorDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showThemeSelector) return
    AlertDialog(
        onDismissRequest = { viewModel.hideThemeSelector() },
        title = { Text("থিম") },
        text = {
            Column {
                listOf("system" to "সিস্টেম", "light" to "হালকা", "dark" to "গাঢ়").forEach { (key, label) ->
                    TextButton(
                        onClick = { viewModel.setThemeMode(key) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.settings.themeMode == key) "✓ $label" else label, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.hideThemeSelector() }) { Text("বাতিল") } }
    )
}

@Composable
private fun FontSelectorDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showFontSelector) return
    AlertDialog(
        onDismissRequest = { viewModel.hideFontSelector() },
        title = { Text("ফন্ট সাইজ") },
        text = {
            Column {
                listOf("small" to "ছোট", "medium" to "মধ্যম", "large" to "বড়").forEach { (key, label) ->
                    TextButton(
                        onClick = { viewModel.setFontSize(key) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.settings.fontSize == key) "✓ $label" else label, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.hideFontSelector() }) { Text("বাতিল") } }
    )
}

@Composable
private fun CreditLimitDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showCreditLimitDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideCreditLimitDialog() },
        title = { Text("ক্রেডিট লিমিট") },
        text = {
            Column {
                Text("প্রতি গ্রাহকের জন্য সর্বোচ্চ বাকির পরিমাণ (০ = সীমাহীন)")
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
        confirmButton = { Button(onClick = { viewModel.saveCreditLimit() }) { Text("সংরক্ষণ") } },
        dismissButton = { TextButton(onClick = { viewModel.hideCreditLimitDialog() }) { Text("বাতিল") } }
    )
}

@Composable
private fun DeleteWindowDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showDeleteWindowDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideDeleteWindowDialog() },
        title = { Text("মুছার সময়সীমা") },
        text = {
            Column {
                Text("কত ঘন্টার মধ্যে লেনদেন মুছতে পারবেন?")
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
        confirmButton = { Button(onClick = { viewModel.saveDeleteWindow() }) { Text("সংরক্ষণ") } },
        dismissButton = { TextButton(onClick = { viewModel.hideDeleteWindowDialog() }) { Text("বাতিল") } }
    )
}

@Composable
private fun ReminderTimeDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showReminderTimeDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideReminderTimeDialog() },
        title = { Text("রিমাইন্ডার সময়") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.reminderHour,
                        onValueChange = viewModel::setReminderHour,
                        label = { Text("ঘন্টা") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.reminderMinute,
                        onValueChange = viewModel::setReminderMinute,
                        label = { Text("মিনিট") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = { Button(onClick = { viewModel.saveReminderTime() }) { Text("সংরক্ষণ") } },
        dismissButton = { TextButton(onClick = { viewModel.hideReminderTimeDialog() }) { Text("বাতিল") } }
    )
}

@Composable
private fun NavOrderDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showNavOrderEditor) return
    val order = remember { mutableStateOf(state.settings.navOrder) }
    AlertDialog(
        onDismissRequest = { viewModel.hideNavOrderEditor() },
        title = { Text("নেভিগেশন অর্ডার") },
        text = {
            Column {
                Text("কমা দিয়ে আলাদা করুন: dashboard,inventory,sale,customers,more")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = order.value,
                    onValueChange = { order.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = { viewModel.setNavOrder(order.value) }) { Text("সংরক্ষণ") } },
        dismissButton = { TextButton(onClick = { viewModel.hideNavOrderEditor() }) { Text("বাতিল") } }
    )
}

@Composable
private fun QuickActionsDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showQuickActionsEditor) return
    val actions = remember { mutableStateOf(state.settings.quickActions) }
    AlertDialog(
        onDismissRequest = { viewModel.hideQuickActionsEditor() },
        title = { Text("দ্রুত অ্যাকশন") },
        text = {
            Column {
                Text("কমা দিয়ে আলাদা করুন: sale,stock,expense,customer,purchase")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = actions.value,
                    onValueChange = { actions.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = { viewModel.setQuickActions(actions.value) }) { Text("সংরক্ষণ") } },
        dismissButton = { TextButton(onClick = { viewModel.hideQuickActionsEditor() }) { Text("বাতিল") } }
    )
}

@Composable
private fun DataExportDialog(state: SettingsState, viewModel: SettingsViewModel, context: android.content.Context) {
    if (!state.showDataExportDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideDataExportDialog() },
        title = { Text("ডেটা এক্সপোর্ট") },
        text = {
            Column {
                Text("ফরম্যাট নির্বাচন করুন:")
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
        confirmButton = { Button(onClick = { viewModel.exportData(context) }) { Text("এক্সপোর্ট") } },
        dismissButton = { TextButton(onClick = { viewModel.hideDataExportDialog() }) { Text("বাতিল") } }
    )
}

@Composable
private fun DataImportDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showDataImportDialog) return
    AlertDialog(
        onDismissRequest = { viewModel.hideDataImportDialog() },
        title = { Text("ডেটা ইম্পোর্ট") },
        text = {
            Column {
                Text("JSON ডেটা পেস্ট করুন:")
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
        confirmButton = { Button(onClick = { viewModel.importData() }) { Text("ইম্পোর্ট") } },
        dismissButton = { TextButton(onClick = { viewModel.hideDataImportDialog() }) { Text("বাতিল") } }
    )
}

@Composable
private fun DeleteConfirmDialog(state: SettingsState, viewModel: SettingsViewModel) {
    if (!state.showDeleteConfirm) return
    AlertDialog(
        onDismissRequest = { viewModel.hideDeleteConfirm() },
        title = { Text("সব ডেটা মুছুন") },
        text = { Text("সমস্ত তথ্য স্থায়ীভাবে মুছে যাবে! এই কাজ পূর্বাবস্থায় ফেরানো যাবে না। আপনি কি নিশ্চিত?") },
        confirmButton = {
            Button(
                onClick = { viewModel.clearAllData() },
                colors = ButtonDefaults.buttonColors(containerColor = RedExpense)
            ) { Text("মুছুন", color = androidx.compose.ui.graphics.Color.White) }
        },
        dismissButton = { TextButton(onClick = { viewModel.hideDeleteConfirm() }) { Text("বাতিল") } }
    )
}
