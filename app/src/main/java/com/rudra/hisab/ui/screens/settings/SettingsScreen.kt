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
                    if (state.settings.isPinEnabled) viewModel.disablePin()
                    else viewModel.showPinSetup()
                }
            )
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
        AlertDialog(
            onDismissRequest = { viewModel.hidePinSetup() },
            title = { Text("পিন সেটআপ") },
            text = {
                Column {
                    if (state.pinStep == 1) {
                        Text("একটি ৪-ডিজিটের পিন দিন")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.pinInput,
                            onValueChange = viewModel::setPinInput,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("পিন") }
                        )
                    } else {
                        Text("পিন আবার দিন")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.pinConfirm,
                            onValueChange = viewModel::setPinConfirm,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("পিন নিশ্চিত করুন") }
                        )
                        if (state.pinInput != state.pinConfirm && state.pinConfirm.length == 4) {
                            Text("পিন মিলছে না", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (state.pinStep == 1) viewModel.nextPinStep()
                        else viewModel.savePin()
                    },
                    enabled = if (state.pinStep == 1) state.pinInput.length == 4
                    else state.pinConfirm.length == 4 && state.pinInput == state.pinConfirm
                ) { Text(if (state.pinStep == 1) "পরবর্তী" else "সংরক্ষণ") }
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
