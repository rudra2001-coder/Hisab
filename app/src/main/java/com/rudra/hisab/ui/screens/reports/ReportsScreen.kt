package com.rudra.hisab.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.hisab.ui.theme.BlueInfo
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter
import com.rudra.hisab.util.ExportFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isBangla = state.isBangla
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    }

    val startLocal = remember(state.startDate) {
        Instant.ofEpochMilli(state.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val endLocal = remember(state.endDate) {
        Instant.ofEpochMilli(state.endDate).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    if (showStartPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.startDate
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.setDateRange(millis, state.endDate)
                    }
                    showStartPicker = false
                }) { Text(if (isBangla) "ঠিক" else "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text(if (isBangla) "বাতিল" else "Cancel") }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (showEndPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.endDate
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.setDateRange(state.startDate, millis)
                    }
                    showEndPicker = false
                }) { Text(if (isBangla) "ঠিক" else "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text(if (isBangla) "বাতিল" else "Cancel") }
            }
        ) { DatePicker(state = pickerState) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                if (isBangla) "রিপোর্ট" else "Reports",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BlueInfo, modifier = Modifier.size(20.dp))
                OutlinedButton(
                    onClick = { showStartPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(startLocal.format(dateFormatter), style = MaterialTheme.typography.bodySmall)
                }
                Text("—", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    onClick = { showEndPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(endLocal.format(dateFormatter), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReportSummaryCard(
                    label = if (isBangla) "মোট বিক্রয়" else "Total Sales",
                    value = state.salesReport.totalSales,
                    color = GreenProfit,
                    modifier = Modifier.weight(1f)
                )
                ReportSummaryCard(
                    label = if (isBangla) "মোট খরচ" else "Total Expenses",
                    value = state.expenseReport.totalExpenses,
                    color = RedExpense,
                    modifier = Modifier.weight(1f)
                )
                ReportSummaryCard(
                    label = if (isBangla) "নেট লাভ" else "Net Profit",
                    value = state.profitLoss.netProfit,
                    color = BlueInfo,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            ReportSectionCard(
                title = if (isBangla) "বিক্রয় রিপোর্ট" else "Sales Report",
                icon = Icons.Default.PointOfSale,
                iconTint = GreenProfit
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportRow(
                        label = if (isBangla) "মোট বিক্রয়" else "Total Sales",
                        value = state.salesReport.totalSales
                    )
                    ReportRow(
                        label = if (isBangla) "মোট আদায়" else "Total Paid",
                        value = state.salesReport.totalPaid
                    )
                    ReportRow(
                        label = if (isBangla) "মোট বাকি" else "Total Due",
                        value = state.salesReport.totalDue,
                        valueColor = RedExpense
                    )
                    ReportRow(
                        label = if (isBangla) "বিক্রয় সংখ্যা" else "Sales Count",
                        value = state.salesReport.saleCount.toDouble(),
                        suffix = if (isBangla) " টি" else ""
                    )
                    ReportRow(
                        label = if (isBangla) "গড় বিক্রয়" else "Avg Sale",
                        value = state.salesReport.avgSaleValue
                    )
                }
            }
        }

        item {
            ReportSectionCard(
                title = if (isBangla) "খরচ বিশ্লেষণ" else "Expense Analysis",
                icon = Icons.Default.ShoppingCart,
                iconTint = RedExpense
            ) {
                if (state.expenseReport.breakdown.isEmpty()) {
                    Text(
                        if (isBangla) "কোন খরচ নেই" else "No expenses",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.expenseReport.breakdown.forEach { (category, total) ->
                            ReportRow(label = category.name, value = total)
                        }
                    }
                }
            }
        }

        item {
            ReportSectionCard(
                title = if (isBangla) "লাভ-ক্ষতি" else "Profit & Loss",
                icon = Icons.Default.MonetizationOn,
                iconTint = if (state.profitLoss.netProfit >= 0) GreenProfit else RedExpense
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportRow(
                        label = if (isBangla) "মোট আয়" else "Total Revenue",
                        value = state.profitLoss.totalRevenue,
                        valueColor = GreenProfit
                    )
                    ReportRow(
                        label = if (isBangla) "মোট খরচ" else "Total Expenses",
                        value = state.profitLoss.totalExpenses,
                        valueColor = RedExpense
                    )
                    ReportRow(
                        label = if (isBangla) "নিট মুনাফা" else "Net Profit",
                        value = state.profitLoss.netProfit,
                        valueColor = if (state.profitLoss.netProfit >= 0) GreenProfit else RedExpense
                    )
                    ReportRow(
                        label = if (isBangla) "মুনাফার হার" else "Profit Margin",
                        value = state.profitLoss.profitMargin,
                        suffix = "%",
                        valueColor = if (state.profitLoss.profitMargin >= 0) GreenProfit else RedExpense
                    )
                }
            }
        }

        item {
            ReportSectionCard(
                title = if (isBangla) "গ্রাহক বাকি" else "Customer Dues",
                icon = Icons.Default.People,
                iconTint = OrangeDue
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportRow(
                        label = if (isBangla) "মোট বাকি" else "Total Due",
                        value = state.totalDues,
                        valueColor = OrangeDue
                    )
                    ReportRow(
                        label = if (isBangla) "বাকি গ্রাহক" else "Due Customers",
                        value = state.dueCustomerCount.toDouble(),
                        suffix = if (isBangla) " জন" else ""
                    )
                }
            }
        }

        item {
            ReportSectionCard(
                title = if (isBangla) "মজুদ সতর্কতা" else "Stock Alerts",
                icon = Icons.Default.Inventory2,
                iconTint = BlueInfo
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportRow(
                        label = if (isBangla) "কম স্টক পণ্য" else "Low Stock Items",
                        value = state.lowStockCount.toDouble(),
                        suffix = if (isBangla) " টি" else ""
                    )
                    ReportRow(
                        label = if (isBangla) "মোট স্টক মূল্য" else "Total Stock Value",
                        value = state.totalStockValue
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BlueInfo.copy(alpha = 0.06f))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = BlueInfo,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isBangla) "রিপোর্ট এক্সপোর্ট" else "Export Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        if (isBangla) "CSV, Excel বা PDF ফরম্যাটে এক্সপোর্ট করুন" else "Export in CSV, Excel or PDF format",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExportFormat.entries.forEach { format ->
                            FilterChip(
                                selected = state.selectedFormat == format,
                                onClick = { viewModel.setFormat(format) },
                                label = { Text(format.name) },
                                leadingIcon = {
                                    if (state.selectedFormat == format)
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(16.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BlueInfo.copy(alpha = 0.12f),
                                    selectedLabelColor = BlueInfo
                                )
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.exportData(context) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isExporting
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (state.isExporting)
                                (if (isBangla) "এক্সপোর্ট হচ্ছে..." else "Exporting...")
                            else
                                (if (isBangla) "এক্সপোর্ট করুন" else "Export")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(label: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = color)
            Spacer(Modifier.height(2.dp))
            Text(
                CurrencyFormatter.format(value),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ReportSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
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
private fun ReportRow(
    label: String,
    value: Double,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    suffix: String = ""
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "${CurrencyFormatter.format(value)}$suffix",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
