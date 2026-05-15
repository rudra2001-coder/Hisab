package com.rudra.hisab.ui.screens.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
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
import com.rudra.hisab.ui.theme.BlueInfo
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter
import com.rudra.hisab.util.ExportFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("রিপোর্ট", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("তারিখের范围", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        Text("${sdf.format(Date(state.startDate))} - ${sdf.format(Date(state.endDate))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("পরিবর্তন")
                    }
                }
            }
        }

        if (showDatePicker) {
            item {
                DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("বন্ধ") } }) {
                    val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.endDate)
                    Column(Modifier.padding(16.dp)) {
                        Text("শুরুর তারিখ", style = MaterialTheme.typography.titleSmall)
                        DatePicker(state = pickerState, showModeToggle = false)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.setDateRange(state.startDate, pickerState.selectedDateMillis ?: state.endDate); showDatePicker = false }, Modifier.fillMaxWidth()) { Text("প্রয়োগ") }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReportSummaryCard("মোট বিক্রয়", state.salesReport.totalSales, GreenProfit, Modifier.weight(1f))
                ReportSummaryCard("মোট খরচ", state.expenseReport.totalExpenses, RedExpense, Modifier.weight(1f))
                ReportSummaryCard("নেট লাভ", state.profitLoss.netProfit, BlueInfo, Modifier.weight(1f))
            }
        }

        item {
            ReportSectionCard("বিক্রয় রিপোর্ট", Icons.Default.PointOfSale, GreenProfit) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportRow("মোট বিক্রয়", state.salesReport.totalSales)
                    ReportRow("মোট আদায়", state.salesReport.totalPaid)
                    ReportRow("মোট বাকি", state.salesReport.totalDue, RedExpense)
                    ReportRow("বিক্রয় সংখ্যা", state.salesReport.saleCount.toDouble(), suffix = "টি")
                    ReportRow("গড় বিক্রয়", state.salesReport.avgSaleValue)
                }
            }
        }

        item {
            ReportSectionCard("খরচ বিশ্লেষণ", Icons.Default.ShoppingCart, RedExpense) {
                if (state.expenseReport.breakdown.isEmpty()) {
                    Text("কোন খরচ নেই", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    state.expenseReport.breakdown.forEach { (category, total) -> ReportRow(category.name, total) }
                }
            }
        }

        item {
            ReportSectionCard("গ্রাহক বাকি", Icons.Default.People, OrangeDue) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportRow("মোট বাকি", state.totalDues, OrangeDue)
                    ReportRow("বাকি আছে গ্রাহক", state.dueCustomerCount.toDouble(), suffix = "জন")
                }
            }
        }

        item {
            ReportSectionCard("মজুদ সতর্কতা", Icons.Default.Inventory2, BlueInfo) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportRow("কম স্টক পণ্য", state.lowStockCount.toDouble(), suffix = "টি")
                    ReportRow("মোট স্টক মূল্য", state.totalStockValue)
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("রিপোর্ট এক্সপোর্ট", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    }
                    Text("রিপোর্ট CSV, Excel বা PDF ফরম্যাটে এক্সপোর্ট করুন", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExportFormat.entries.forEach { format ->
                            FilterChip(selected = state.selectedFormat == format, onClick = { viewModel.setFormat(format) }, label = { Text(format.name) }, leadingIcon = { if (state.selectedFormat == format) Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(16.dp)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                        }
                    }
                    Button(onClick = { viewModel.exportData() }, Modifier.fillMaxWidth(), enabled = !state.isExporting) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.isExporting) "এক্সপোর্ট হচ্ছে..." else "এক্সপোর্ট করুন")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(label: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = color)
            Spacer(Modifier.height(4.dp))
            Text(CurrencyFormatter.format(value), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun ReportSectionCard(title: String, icon: ImageVector, iconTint: Color, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@Composable
private fun ReportRow(label: String, value: Double, valueColor: Color = MaterialTheme.colorScheme.onSurface, suffix: String = "") {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${CurrencyFormatter.format(value)}$suffix", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
