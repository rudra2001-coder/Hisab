package com.rudra.hisab.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.RedExpense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Palette tokens (map to your theme colors) ────────────────────────────────
private val Blue50   = Color(0xFFE6F1FB); private val Blue700  = Color(0xFF185FA5)
private val Teal50   = Color(0xFFE1F5EE); private val Teal700  = Color(0xFF0F6E56)
private val Amber50  = Color(0xFFFAEEDA); private val Amber700 = Color(0xFF854F0B)
private val Purple50 = Color(0xFFEEEDFE); private val Purple700= Color(0xFF534AB7)
private val Coral50  = Color(0xFFFAECE7); private val Coral700 = Color(0xFF993C1D)
private val Gray50   = Color(0xFFF1EFE8); private val Gray600  = Color(0xFF5F5E5A)
private val Red50    = Color(0xFFFCEBEB); private val Red700   = Color(0xFFA32D2D)
private val Red200   = Color(0xFFF09595); private val Red400   = Color(0xFFD85A30)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val bn = state.settings.languageCode == "bn"

    fun t(en: String, bn_: String) = if (bn) bn_ else en

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp)
    ) {
        // ── Page header ──────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
            Text(
                text = t("Settings", "সেটিংস"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = t("Preferences & configuration", "পছন্দ ও কনফিগারেশন"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Language & Appearance ────────────────────────────────────────────
        SettingsSection(label = t("Language & appearance", "ভাষা ও থিম")) {
            SettingsRow(
                icon = Icons.Default.Language, iconBg = Blue50, iconTint = Blue700,
                title = t("Language", "ভাষা"),
                subtitle = if (state.settings.languageCode == "bn") "বাংলা" else "English",
                trailing = {
                    Switch(
                        checked = state.settings.languageCode == "bn",
                        onCheckedChange = { viewModel.toggleLanguage() },
                        modifier = Modifier.height(24.dp)
                    )
                }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Brightness6, iconBg = Gray50, iconTint = Gray600,
                title = t("Theme", "থিম"),
                subtitle = when (state.settings.themeMode) {
                    "light" -> t("Light", "হালকা")
                    "dark"  -> t("Dark", "গাঢ়")
                    else    -> t("System default", "সিস্টেম")
                },
                onClick = { viewModel.showThemeSelector() }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.TextFields, iconBg = Gray50, iconTint = Gray600,
                title = t("Font size", "ফন্ট সাইজ"),
                subtitle = when (state.settings.fontSize) {
                    "small" -> t("Small", "ছোট")
                    "large" -> t("Large", "বড়")
                    else    -> t("Medium", "মধ্যম")
                },
                onClick = { viewModel.showFontSelector() }
            )
        }

        // ── Shop ─────────────────────────────────────────────────────────────
        SettingsSection(label = t("Shop", "দোকান")) {
            SettingsRow(
                icon = Icons.Default.Store, iconBg = Teal50, iconTint = Teal700,
                title = t("Shop info", "দোকানের তথ্য"),
                subtitle = state.settings.shopName.ifEmpty { t("Tap to set name", "নাম সেট করুন") },
                onClick = { viewModel.showShopEdit() }
            )
        }

        // ── Security ─────────────────────────────────────────────────────────
        SettingsSection(label = t("Security", "নিরাপত্তা")) {
            SettingsRow(
                icon = Icons.Default.Lock, iconBg = Purple50, iconTint = Purple700,
                title = t("PIN lock", "পিন লক"),
                subtitle = if (state.settings.isPinEnabled) t("4-digit app lock · On", "পিন চালু") else t("4-digit app lock · Off", "পিন বন্ধ"),
                trailing = {
                    StatusPill(
                        on = state.settings.isPinEnabled,
                        labelOn = t("On", "চালু"), labelOff = t("Off", "বন্ধ")
                    )
                },
                onClick = {
                    if (state.settings.isPinEnabled) viewModel.showPinChange()
                    else viewModel.showPinSetup()
                }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Fingerprint, iconBg = Purple50, iconTint = Purple700,
                title = t("Biometric lock", "বায়োমেট্রিক লক"),
                subtitle = t("Fingerprint unlock", "আঙুলের ছাপ দিয়ে খুলুন"),
                trailing = {
                    Switch(
                        checked = state.settings.isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric() },
                        modifier = Modifier.height(24.dp)
                    )
                }
            )
        }

        // ── Sales & Inventory ─────────────────────────────────────────────────
        SettingsSection(label = t("Sales & inventory", "বিক্রয় ও ইনভেন্টরি")) {
            SettingsRow(
                icon = Icons.Default.ShoppingCart, iconBg = Teal50, iconTint = Teal700,
                title = t("Cart mode", "কার্ট মোড"),
                subtitle = t("Multi-item sales cart", "একাধিক পণ্যের কার্ট"),
                trailing = {
                    Switch(
                        checked = state.settings.cartModeEnabled,
                        onCheckedChange = { viewModel.toggleCartMode() },
                        modifier = Modifier.height(24.dp)
                    )
                }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Dashboard, iconBg = Gray50, iconTint = Gray600,
                title = "FAB mode",
                subtitle = t("Floating action button", "ফ্লোটিং অ্যাকশন বাটন"),
                trailing = {
                    Switch(
                        checked = state.settings.fabModeEnabled,
                        onCheckedChange = { viewModel.toggleFabMode() },
                        modifier = Modifier.height(24.dp)
                    )
                }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Inventory2, iconBg = Amber50, iconTint = Amber700,
                title = t("Batch tracking", "ব্যাচ ট্র্যাকিং"),
                subtitle = t("Expiry & batch management", "মেয়াদ ও ব্যাচ ব্যবস্থাপনা"),
                trailing = {
                    Switch(
                        checked = state.settings.batchTrackingEnabled,
                        onCheckedChange = { viewModel.toggleBatchTracking() },
                        modifier = Modifier.height(24.dp)
                    )
                }
            )
        }

        // ── Customers & Transactions ──────────────────────────────────────────
        SettingsSection(label = t("Customers & transactions", "গ্রাহক ও লেনদেন")) {
            SettingsRow(
                icon = Icons.Default.Timer, iconBg = Amber50, iconTint = Amber700,
                title = t("Credit limit", "ক্রেডিট সীমা"),
                subtitle = if (state.settings.defaultCreditLimit > 0)
                    "৳${state.settings.defaultCreditLimit.toLong()}"
                else t("Unlimited", "সীমাহীন"),
                trailing = {
                    ValueChip(text = if (state.settings.defaultCreditLimit > 0)
                        "৳${state.settings.defaultCreditLimit.toLong()}" else t("∞", "∞"))
                },
                onClick = { viewModel.showCreditLimitDialog() }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Delete, iconBg = Coral50, iconTint = Coral700,
                title = t("Delete window", "মুছার সময়সীমা"),
                subtitle = t("How long to allow deletion", "কতক্ষণ মুছতে পারবেন"),
                trailing = { ValueChip(text = "${state.settings.deleteWindowHours}h") },
                onClick = { viewModel.showDeleteWindowDialog() }
            )
        }

        // ── Notifications ─────────────────────────────────────────────────────
        SettingsSection(label = t("Notifications", "নোটিফিকেশন")) {
            SettingsRow(
                icon = Icons.Default.Schedule, iconBg = Blue50, iconTint = Blue700,
                title = t("Sale reminder", "বিক্রয় রিমাইন্ডার"),
                subtitle = t("Daily reminder time", "দৈনিক রিমাইন্ডারের সময়"),
                trailing = {
                    ValueChip(
                        text = "${state.settings.saleReminderHour}:${
                            String.format("%02d", state.settings.saleReminderMinute)
                        }"
                    )
                },
                onClick = { viewModel.showReminderTimeDialog() }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Notifications, iconBg = Blue50, iconTint = Blue700,
                title = t("Monthly report", "মাসিক রিপোর্ট"),
                subtitle = t("End-of-month reminder", "মাসের শেষে রিমাইন্ডার"),
                trailing = {
                    Switch(
                        checked = state.settings.monthlyReportReminder,
                        onCheckedChange = { viewModel.toggleMonthlyReport() },
                        modifier = Modifier.height(24.dp)
                    )
                }
            )
        }

        // ── Navigation ────────────────────────────────────────────────────────
        SettingsSection(label = t("Navigation", "নেভিগেশন")) {
            SettingsRow(
                icon = Icons.Default.Dashboard, iconBg = Gray50, iconTint = Gray600,
                title = t("Nav order", "নেভিগেশন অর্ডার"),
                subtitle = t("Reorder bottom menu tabs", "নিচের মেনুর অর্ডার পরিবর্তন"),
                onClick = { viewModel.showNavOrderEditor() }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.TrendingUp, iconBg = Gray50, iconTint = Gray600,
                title = t("Quick actions", "দ্রুত অ্যাকশন"),
                subtitle = t("Dashboard shortcut buttons", "ড্যাশবোর্ডে কোন বাটন দেখাবে"),
                onClick = { viewModel.showQuickActionsEditor() }
            )
        }

        // ── Data & Backup ─────────────────────────────────────────────────────
        SettingsSection(label = t("Data & backup", "ডেটা ও ব্যাকআপ")) {
            SettingsRow(
                icon = Icons.Default.CloudUpload, iconBg = Teal50, iconTint = Teal700,
                title = t("Auto backup", "অটো ব্যাকআপ"),
                subtitle = if (state.settings.autoBackupEnabled) {
                    val freq = if (state.settings.backupFrequency == "daily") t("Daily", "প্রতিদিন") else t("Weekly", "সাপ্তাহিক")
                    t("On · $freq", "চালু · $freq")
                } else t("Off", "বন্ধ"),
                trailing = {
                    Switch(
                        checked = state.settings.autoBackupEnabled,
                        onCheckedChange = { viewModel.toggleAutoBackup() },
                        modifier = Modifier.height(24.dp)
                    )
                }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Schedule, iconBg = Teal50, iconTint = Teal700,
                title = t("Backup frequency", "ব্যাকআপ ফ্রিকোয়েন্সি"),
                subtitle = if (state.settings.backupFrequency == "daily") t("Daily", "প্রতিদিন") else t("Weekly", "সাপ্তাহিক"),
                onClick = { viewModel.showBackupDialog() }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.CloudUpload, iconBg = Teal50, iconTint = Teal700,
                title = t("Backup now", "এখনই ব্যাকআপ"),
                subtitle = if (state.settings.lastBackupTime > 0)
                    "${t("Last", "সর্বশেষ")}: ${
                        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                            .format(Date(state.settings.lastBackupTime))
                    }"
                else t("Never backed up", "কখনো ব্যাকআপ নেয়া হয়নি"),
                trailing = {
                    when {
                        state.isBackingUp ->
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        state.backupSuccess == true ->
                            Icon(Icons.Default.CheckCircle, null,
                                modifier = Modifier.size(20.dp), tint = GreenProfit)
                    }
                },
                onClick = { viewModel.performManualBackup() }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.FileDownload, iconBg = Blue50, iconTint = Blue700,
                title = t("Export data", "ডেটা এক্সপোর্ট"),
                subtitle = "JSON / CSV",
                onClick = { viewModel.showDataExportDialog() }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.FileUpload, iconBg = Blue50, iconTint = Blue700,
                title = t("Import data", "ডেটা ইম্পোর্ট"),
                subtitle = t("Restore from JSON file", "JSON ফাইল থেকে পুনরুদ্ধার"),
                onClick = { viewModel.showDataImportDialog() }
            )
        }

        // ── Danger zone ───────────────────────────────────────────────────────
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Red50),
                shape = RoundedCornerShape(14.dp)
            ) {
                SettingsRow(
                    icon = Icons.Default.ClearAll, iconBg = Red50, iconTint = Red700,
                    title = t("Clear all data", "সব ডেটা মুছুন"),
                    subtitle = t("Permanently delete everything", "সমস্ত তথ্য স্থায়ীভাবে মুছে ফেলুন"),
                    titleColor = Red700,
                    subtitleColor = Red400,
                    chevronTint = Red200,
                    onClick = { viewModel.showDeleteConfirm() }
                )
            }
        }

        // ── About ─────────────────────────────────────────────────────────────
        SettingsSection(label = t("App", "অ্যাপ")) {
            SettingsRow(
                icon = Icons.Default.Info, iconBg = Gray50, iconTint = Gray600,
                title = t("About Hisab", "হিসাব সম্পর্কে"),
                subtitle = "v2.0 · Licenses"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ── Dialogs (unchanged logic) ─────────────────────────────────────────────
    PinSetupDialog(state, viewModel, bn)
    ShopEditDialog(state, viewModel, bn)
    ThemeSelectorDialog(state, viewModel, bn)
    FontSelectorDialog(state, viewModel, bn)
    CreditLimitDialog(state, viewModel, bn)
    DeleteWindowDialog(state, viewModel, bn)
    ReminderTimeDialog(state, viewModel, bn)
    NavOrderDialog(state, viewModel, bn)
    QuickActionsDialog(state, viewModel, bn)
    DataExportDialog(state, viewModel, context, bn)
    DataImportDialog(state, viewModel, bn)
    DeleteConfirmDialog(state, viewModel, bn)
    BackupFrequencyDialog(state, viewModel, bn)
}

// ─── Layout primitives ────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.07.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp, top = 12.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp, end = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    titleColor: Color = Color.Unspecified,
    subtitleColor: Color = Color.Unspecified,
    chevronTint: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon chip
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (titleColor != Color.Unspecified) titleColor
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (subtitleColor != Color.Unspecified) subtitleColor
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        // Trailing
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (chevronTint != Color.Unspecified) chevronTint
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Small trailing widgets ───────────────────────────────────────────────────

@Composable
private fun StatusPill(on: Boolean, labelOn: String, labelOff: String) {
    val bg   = if (on) Teal50   else Gray50
    val text = if (on) Teal700  else Gray600
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (on) labelOn else labelOff,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = text
        )
    }
}

@Composable
private fun ValueChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

// ─── All dialogs (logic unchanged, bilingual) ─────────────────────────────────

@Composable
private fun BackupFrequencyDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showBackupDialog) return
    AlertDialog(
        onDismissRequest = { vm.hideBackupDialog() },
        title = { Text(if (bn) "ব্যাকআপ ফ্রিকোয়েন্সি" else "Backup Frequency") },
        text = {
            Column {
                listOf("daily" to if (bn) "প্রতিদিন" else "Daily",
                    "weekly" to if (bn) "সাপ্তাহিক" else "Weekly").forEach { (key, label) ->
                    TextButton(onClick = { vm.setBackupFrequency(key); vm.hideBackupDialog() },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.settings.backupFrequency == key) "✓ $label" else label,
                            modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { vm.hideBackupDialog() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun PinSetupDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showPinDialog) return
    val title = when (state.pinMode) {
        PinMode.SETUP   -> if (bn) "পিন সেটআপ" else "Set up PIN"
        PinMode.CHANGE  -> if (state.pinStep == 1) (if (bn) "বর্তমান পিন" else "Current PIN") else (if (bn) "নতুন পিন" else "New PIN")
        PinMode.DISABLE -> if (bn) "পিন সরান" else "Remove PIN"
    }
    AlertDialog(
        onDismissRequest = { vm.hidePinSetup() },
        title = { Text(title) },
        text = {
            Column {
                val prompt = when {
                    state.pinMode == PinMode.DISABLE -> if (bn) "পিন দিন" else "Enter your PIN"
                    state.pinMode == PinMode.CHANGE && state.pinStep == 1 -> if (bn) "বর্তমান পিন দিন" else "Enter current PIN"
                    state.pinStep == 1 -> if (bn) "নতুন ৪-ডিজিটের পিন দিন" else "Enter a new 4-digit PIN"
                    else -> if (bn) "পিন আবার দিন" else "Confirm PIN"
                }
                Text(prompt, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = if (state.pinStep == 1) state.pinInput else state.pinConfirm,
                    onValueChange = { if (state.pinStep == 1) vm.setPinInput(it) else vm.setPinConfirm(it) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("••••") }
                )
                state.pinError?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            val (text, enabled, action) = when {
                state.pinMode == PinMode.DISABLE -> Triple(if (bn) "নিশ্চিত" else "Confirm", state.pinInput.length == 4, { vm.verifyAndProceed() })
                state.pinMode == PinMode.CHANGE && state.pinStep == 1 -> Triple(if (bn) "পরবর্তী" else "Next", state.pinInput.length == 4, { vm.verifyAndProceed() })
                state.pinStep == 1 -> Triple(if (bn) "পরবর্তী" else "Next", state.pinInput.length == 4, { vm.nextPinStep() })
                else -> Triple(if (bn) "সংরক্ষণ" else "Save", state.pinConfirm.length == 4 && state.pinInput == state.pinConfirm, { vm.savePin() })
            }
            Button(onClick = action, enabled = enabled) { Text(text) }
        },
        dismissButton = { TextButton(onClick = { vm.hidePinSetup() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun ShopEditDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showShopEdit) return
    AlertDialog(
        onDismissRequest = { vm.hideShopEdit() },
        title = { Text(if (bn) "দোকানের নাম" else "Shop name") },
        text = {
            OutlinedTextField(
                value = state.editShopName, onValueChange = vm::setEditShopName,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text(if (bn) "নাম" else "Name") }
            )
        },
        confirmButton = { Button(onClick = { vm.saveShopName() }) { Text(if (bn) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { vm.hideShopEdit() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun ThemeSelectorDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showThemeSelector) return
    AlertDialog(
        onDismissRequest = { vm.hideThemeSelector() },
        title = { Text(if (bn) "থিম" else "Theme") },
        text = {
            Column {
                listOf("system" to if (bn) "সিস্টেম" else "System",
                    "light"  to if (bn) "হালকা"  else "Light",
                    "dark"   to if (bn) "গাঢ়"    else "Dark").forEach { (key, label) ->
                    TextButton(onClick = { vm.setThemeMode(key) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.settings.themeMode == key) "✓ $label" else label,
                            modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { vm.hideThemeSelector() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun FontSelectorDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showFontSelector) return
    AlertDialog(
        onDismissRequest = { vm.hideFontSelector() },
        title = { Text(if (bn) "ফন্ট সাইজ" else "Font size") },
        text = {
            Column {
                listOf("small"  to if (bn) "ছোট"   else "Small",
                    "medium" to if (bn) "মধ্যম" else "Medium",
                    "large"  to if (bn) "বড়"    else "Large").forEach { (key, label) ->
                    TextButton(onClick = { vm.setFontSize(key) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.settings.fontSize == key) "✓ $label" else label,
                            modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { vm.hideFontSelector() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun CreditLimitDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showCreditLimitDialog) return
    AlertDialog(
        onDismissRequest = { vm.hideCreditLimitDialog() },
        title = { Text(if (bn) "ক্রেডিট সীমা" else "Credit limit") },
        text = {
            Column {
                Text(if (bn) "প্রতি গ্রাহকের সর্বোচ্চ বাকি (০ = সীমাহীন)"
                else "Max credit per customer (0 = unlimited)",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.creditLimitInput, onValueChange = vm::setCreditLimitInput,
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("৳") }
                )
            }
        },
        confirmButton = { Button(onClick = { vm.saveCreditLimit() }) { Text(if (bn) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { vm.hideCreditLimitDialog() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun DeleteWindowDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showDeleteWindowDialog) return
    AlertDialog(
        onDismissRequest = { vm.hideDeleteWindowDialog() },
        title = { Text(if (bn) "মুছার সময়সীমা" else "Delete window") },
        text = {
            Column {
                Text(if (bn) "কত ঘন্টার মধ্যে লেনদেন মুছতে পারবেন?"
                else "Within how many hours can a transaction be deleted?",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.deleteWindowInput, onValueChange = vm::setDeleteWindowInput,
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(if (bn) "ঘন্টা" else "Hours") }
                )
            }
        },
        confirmButton = { Button(onClick = { vm.saveDeleteWindow() }) { Text(if (bn) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { vm.hideDeleteWindowDialog() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun ReminderTimeDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showReminderTimeDialog) return
    AlertDialog(
        onDismissRequest = { vm.hideReminderTimeDialog() },
        title = { Text(if (bn) "রিমাইন্ডার সময়" else "Reminder time") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.reminderHour, onValueChange = vm::setReminderHour,
                    label = { Text(if (bn) "ঘন্টা" else "Hour") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                )
                OutlinedTextField(
                    value = state.reminderMinute, onValueChange = vm::setReminderMinute,
                    label = { Text(if (bn) "মিনিট" else "Minute") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = { vm.saveReminderTime() }) { Text(if (bn) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { vm.hideReminderTimeDialog() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun NavOrderDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showNavOrderEditor) return
    val order = remember { mutableStateOf(state.settings.navOrder) }
    AlertDialog(
        onDismissRequest = { vm.hideNavOrderEditor() },
        title = { Text(if (bn) "নেভিগেশন অর্ডার" else "Nav order") },
        text = {
            Column {
                Text(if (bn) "কমা দিয়ে আলাদা করুন:" else "Separate with commas:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("dashboard, inventory, sale, customers, more",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = order.value, onValueChange = { order.value = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = { Button(onClick = { vm.setNavOrder(order.value) }) { Text(if (bn) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { vm.hideNavOrderEditor() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun QuickActionsDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showQuickActionsEditor) return
    val actions = remember { mutableStateOf(state.settings.quickActions) }
    AlertDialog(
        onDismissRequest = { vm.hideQuickActionsEditor() },
        title = { Text(if (bn) "দ্রুত অ্যাকশন" else "Quick actions") },
        text = {
            Column {
                Text(if (bn) "কমা দিয়ে আলাদা করুন:" else "Separate with commas:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("sale, stock, expense, customer, purchase",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = actions.value, onValueChange = { actions.value = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = { Button(onClick = { vm.setQuickActions(actions.value) }) { Text(if (bn) "সংরক্ষণ" else "Save") } },
        dismissButton = { TextButton(onClick = { vm.hideQuickActionsEditor() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun DataExportDialog(state: SettingsState, vm: SettingsViewModel, context: android.content.Context, bn: Boolean) {
    if (!state.showDataExportDialog) return
    AlertDialog(
        onDismissRequest = { vm.hideDataExportDialog() },
        title = { Text(if (bn) "ডেটা এক্সপোর্ট" else "Export data") },
        text = {
            Column {
                Text(if (bn) "ফরম্যাট নির্বাচন করুন" else "Select format",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("json" to "JSON", "csv" to "CSV").forEach { (key, label) ->
                        val sel = state.exportFormat == key
                        Button(
                            onClick = { vm.setExportFormat(key) },
                            modifier = Modifier.weight(1f),
                            colors = if (sel) ButtonDefaults.buttonColors()
                            else ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) { Text(label) }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { vm.exportData(context) }) { Text(if (bn) "এক্সপোর্ট" else "Export") } },
        dismissButton = { TextButton(onClick = { vm.hideDataExportDialog() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun DataImportDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showDataImportDialog) return
    AlertDialog(
        onDismissRequest = { vm.hideDataImportDialog() },
        title = { Text(if (bn) "ডেটা ইম্পোর্ট" else "Import data") },
        text = {
            Column {
                Text(if (bn) "JSON ডেটা পেস্ট করুন" else "Paste JSON data",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.importData, onValueChange = vm::setImportData,
                    modifier = Modifier.fillMaxWidth().height(180.dp), maxLines = 10
                )
                state.importError?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, color = RedExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { Button(onClick = { vm.importData() }) { Text(if (bn) "ইম্পোর্ট" else "Import") } },
        dismissButton = { TextButton(onClick = { vm.hideDataImportDialog() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}

@Composable
private fun DeleteConfirmDialog(state: SettingsState, vm: SettingsViewModel, bn: Boolean) {
    if (!state.showDeleteConfirm) return
    AlertDialog(
        onDismissRequest = { vm.hideDeleteConfirm() },
        title = { Text(if (bn) "সব ডেটা মুছুন" else "Delete all data") },
        text = {
            Text(
                if (bn) "সমস্ত তথ্য স্থায়ীভাবে মুছে যাবে। এই কাজ পূর্বাবস্থায় ফেরানো যাবে না।"
                else "All data will be permanently deleted. This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = { vm.clearAllData() },
                colors = ButtonDefaults.buttonColors(containerColor = RedExpense)
            ) { Text(if (bn) "মুছুন" else "Delete", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = { vm.hideDeleteConfirm() }) { Text(if (bn) "বাতিল" else "Cancel") } }
    )
}