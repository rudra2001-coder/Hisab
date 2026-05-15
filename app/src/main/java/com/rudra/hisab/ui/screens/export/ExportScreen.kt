package com.rudra.hisab.ui.screens.export

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter
import com.rudra.hisab.util.ExportFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("রিপোর্ট এক্সপোর্ট", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.clickable { showStartDatePicker = true }) {
                        Text("শুরুর তারিখ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(sdf.format(Date(state.startDate)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    }
                    Column(Modifier.clickable { showEndDatePicker = true }) {
                        Text("শেষের তারিখ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(sdf.format(Date(state.endDate)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showStartDatePicker = true }) { Text("শুরু") }
                    OutlinedButton(onClick = { showEndDatePicker = true }) { Text("শেষ") }
                }
            }
        }

        if (showStartDatePicker) {
            DatePickerDialog(onDismissRequest = { showStartDatePicker = false }, confirmButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("বন্ধ") } }) {
                val picker = rememberDatePickerState(initialSelectedDateMillis = state.startDate)
                Column(Modifier.padding(16.dp)) {
                    DatePicker(state = picker, showModeToggle = false)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.setDateRange(picker.selectedDateMillis ?: state.startDate, state.endDate); showStartDatePicker = false }, Modifier.fillMaxWidth()) { Text("সেট করুন") }
                }
            }
        }
        if (showEndDatePicker) {
            DatePickerDialog(onDismissRequest = { showEndDatePicker = false }, confirmButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("বন্ধ") } }) {
                val picker = rememberDatePickerState(initialSelectedDateMillis = state.endDate)
                Column(Modifier.padding(16.dp)) {
                    DatePicker(state = picker, showModeToggle = false)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.setDateRange(state.startDate, picker.selectedDateMillis ?: state.endDate); showEndDatePicker = false }, Modifier.fillMaxWidth()) { Text("সেট করুন") }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("কি অন্তর্ভুক্ত করবেন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                ExportToggleRow("বিক্রয়", Icons.Default.PointOfSale, state.includeSales, viewModel::toggleIncludeSales)
                ExportToggleRow("মজুদ", Icons.Default.Inventory2, state.includeInventory, viewModel::toggleIncludeInventory)
                ExportToggleRow("গ্রাহক", Icons.Default.People, state.includeCustomers, viewModel::toggleIncludeCustomers)
                ExportToggleRow("খরচ", Icons.Default.ShoppingCart, state.includeExpenses, viewModel::toggleIncludeExpenses)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ফরম্যাট নির্বাচন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormat.entries.forEach { format ->
                        FilterChip(selected = state.selectedFormat == format, onClick = { viewModel.setFormat(format) }, label = { Text(format.name) }, leadingIcon = { if (state.selectedFormat == format) Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(16.dp)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(onClick = { viewModel.exportData() }, Modifier.fillMaxWidth().height(56.dp), enabled = !state.isExporting, shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Default.FileDownload, contentDescription = null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (state.isExporting) "এক্সপোর্ট হচ্ছে..." else "রিপোর্ট এক্সপোর্ট করুন", fontWeight = FontWeight.SemiBold)
        }

        state.exportResult?.let {
            Card(colors = CardDefaults.cardColors(containerColor = GreenProfit.copy(alpha = 0.1f))) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenProfit)
                    Spacer(Modifier.width(8.dp))
                    Text("রিপোর্ট সফলভাবে এক্সপোর্ট হয়েছে!", color = GreenProfit, modifier = Modifier.weight(1f))
                }
            }
        }
        state.exportError?.let {
            Card(colors = CardDefaults.cardColors(containerColor = RedExpense.copy(alpha = 0.1f))) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = RedExpense)
                    Spacer(Modifier.width(8.dp))
                    Text(it, color = RedExpense, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ExportToggleRow(label: String, icon: ImageVector, checked: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        FilterChip(selected = checked, onClick = onToggle, label = { Text(if (checked) "হ্যাঁ" else "না") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenProfit.copy(alpha = 0.15f), selectedLabelColor = GreenProfit))
    }
}
