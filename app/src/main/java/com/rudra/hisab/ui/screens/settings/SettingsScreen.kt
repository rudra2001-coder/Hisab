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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val state by viewModel.state.collectAsState()

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

        // Language
        SettingsCard {
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Shop Info
        SettingsCard {
            SettingsRow(
                icon = Icons.Default.Store,
                title = "দোকানের তথ্য",
                subtitle = state.settings.shopName.ifEmpty { "নাম সেট করুন" },
                onClick = { viewModel.showShopEdit() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Security
        SettingsCard {
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
                    onClick = { viewModel.disablePin() }
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

        // About
        SettingsCard {
            SettingsRow(
                icon = Icons.Default.Info,
                title = "সম্পর্কে",
                subtitle = "Hisab v1.0"
            )
        }
    }

    // PIN Dialog
    if (state.showPinDialog) {
        val dialogTitle = when (state.pinMode) {
            PinMode.SETUP -> "পিন সেটআপ"
            PinMode.CHANGE_OLD -> "পিন পরিবর্তন"
            PinMode.CHANGE_NEW -> "নতুন পিন"
            PinMode.DISABLE -> "পিন সরান"
        }
        AlertDialog(
            onDismissRequest = { viewModel.hidePinSetup() },
            title = { Text(dialogTitle) },
            text = {
                Column {
                    val promptText = when {
                        state.pinMode == PinMode.DISABLE -> "পিন দিন"
                        state.pinMode == PinMode.CHANGE_OLD -> "বর্তমান পিন দিন"
                        state.pinStep == 1 -> "নতুন ৪-ডিজিটের পিন দিন"
                        else -> "পিন আবার দিন"
                    }
                    Text(promptText)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = if (state.pinStep == 1) state.pinInput else state.pinConfirm,
                        onValueChange = { if (state.pinStep == 1) viewModel.setPinInput(it) else viewModel.setPinConfirm(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("পিন") }
                    )
                    state.pinError?.let { error ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (state.pinStep == 2 && state.pinInput != state.pinConfirm && state.pinConfirm.length == 4) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("পিন মিলছে না", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                val (buttonText, enabled, onClick) = when {
                    state.pinMode == PinMode.DISABLE -> Triple("নিশ্চিত", state.pinInput.length == 4, { viewModel.verifyAndProceed() })
                    state.pinMode == PinMode.CHANGE_OLD -> Triple("পরবর্তী", state.pinInput.length == 4, { viewModel.verifyAndProceed() })
                    state.pinStep == 1 -> Triple("পরবর্তী", state.pinInput.length == 4, { viewModel.nextPinStep() })
                    else -> Triple("সংরক্ষণ", state.pinConfirm.length == 4 && state.pinInput == state.pinConfirm, { viewModel.savePin() })
                }
                Button(onClick = onClick, enabled = enabled) { Text(buttonText) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hidePinSetup() }) { Text("বাতিল") }
            }
        )
    }

    // Shop edit dialog
    if (state.showShopEdit) {
        AlertDialog(
            onDismissRequest = { viewModel.hideShopEdit() },
            title = { Text("দোকানের নাম") },
            text = {
                OutlinedTextField(
                    value = state.editShopName,
                    onValueChange = viewModel::setEditShopName,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.saveShopName() }) { Text("সংরক্ষণ") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideShopEdit() }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
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
            .padding(16.dp),
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
