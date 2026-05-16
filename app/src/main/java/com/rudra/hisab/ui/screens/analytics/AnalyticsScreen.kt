package com.rudra.hisab.ui.screens.analytics

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.rudra.hisab.util.PdfReportUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBangla = state.isBangla

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val dateFormatter = remember(isBangla) {
        DateTimeFormatter.ofPattern("dd MMM yyyy", if (isBangla) Locale.forLanguageTag("bn") else Locale.getDefault())
    }

    if (showStartPicker) {
        val startPickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.customStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startPickerState.selectedDateMillis?.let { millis ->
                        viewModel.setCustomStartDate(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                    }
                    showStartPicker = false
                }) { Text(if (isBangla) "ঠিক" else "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text(if (isBangla) "বাতিল" else "Cancel") }
            }
        ) { DatePicker(state = startPickerState) }
    }

    if (showEndPicker) {
        val endPickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.customEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endPickerState.selectedDateMillis?.let { millis ->
                        viewModel.setCustomEndDate(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                    }
                    showEndPicker = false
                }) { Text(if (isBangla) "ঠিক" else "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text(if (isBangla) "বাতিল" else "Cancel") }
            }
        ) { DatePicker(state = endPickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (isBangla) "রিপোর্ট ও বিশ্লেষণ" else "Reports & Analytics",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = BlueInfo, modifier = Modifier.size(20.dp))
            OutlinedButton(
                onClick = { showStartPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(state.customStartDate.format(dateFormatter), style = MaterialTheme.typography.bodySmall)
            }
            Text("—", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(
                onClick = { showEndPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(state.customEndDate.format(dateFormatter), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { viewModel.refreshWithCustomRange() }) {
                Icon(Icons.Default.Check, contentDescription = if (isBangla) "প্রয়োগ" else "Apply", tint = BlueInfo)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isLoading) {
            Text(if (isBangla) "লোড হচ্ছে..." else "Loading...", style = MaterialTheme.typography.bodyLarge)
        } else {
            // Weekly Profit
            if (state.weeklyProfit.isNotEmpty()) {
                ReportCard(
                    title = if (isBangla) "সাপ্তাহিক মুনাফা" else "Weekly Profit",
                    icon = Icons.Default.TrendingUp,
                    accentColor = GreenProfit
                ) {
                    state.weeklyProfit.forEachIndexed { index, profit ->
                        val dayName = if (isBangla)
                            when (index) { 0 -> "শনি"; 1 -> "রবি"; 2 -> "সোম"; 3 -> "মঙ্গল"; 4 -> "বুধ"; 5 -> "বৃহস্পতি"; 6 -> "শুক্র"; else -> "দিন $index" }
                        else
                            when (index) { 0 -> "Sat"; 1 -> "Sun"; 2 -> "Mon"; 3 -> "Tue"; 4 -> "Wed"; 5 -> "Thu"; 6 -> "Fri"; else -> "Day $index" }
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(dayName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyFormatter.format(profit),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (profit >= 0) GreenProfit else RedExpense
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Customer Outstanding
            if (state.customerOutstanding.isNotEmpty()) {
                ReportCard(
                    title = if (isBangla) "গ্রাহক বকেয়া" else "Customer Outstanding",
                    icon = Icons.Default.People,
                    accentColor = OrangeDue
                ) {
                    state.customerOutstanding.take(10).forEach { c ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(c.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyFormatter.format(c.totalDue),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangeDue
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Product Profit
            if (state.productProfit.isNotEmpty()) {
                ReportCard(
                    title = if (isBangla) "পণ্য লাভ (৩০ দিন)" else "Product Profit (30 days)",
                    icon = Icons.Default.AttachMoney,
                    accentColor = GreenProfit
                ) {
                    state.productProfit.take(10).forEach { (product, profit) ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (isBangla) product.nameBangla else product.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                CurrencyFormatter.format(profit),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (profit >= 0) GreenProfit else RedExpense
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Monthly Growth
            if (state.monthlyGrowth.isNotEmpty()) {
                ReportCard(
                    title = if (isBangla) "মাসিক প্রবৃদ্ধি" else "Monthly Growth",
                    icon = Icons.Default.TrendingUp,
                    accentColor = BlueInfo
                ) {
                    state.monthlyGrowth.forEach { (label, profit) ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyFormatter.format(profit),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (profit >= 0) GreenProfit else RedExpense
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Cash Flow
            if (state.cashFlow.isNotEmpty()) {
                ReportCard(
                    title = if (isBangla) "নগদ প্রবাহ (দৈনিক)" else "Cash Flow (Daily)",
                    icon = Icons.Default.AttachMoney,
                    accentColor = BlueInfo
                ) {
                    state.cashFlow.forEach { (date, amount) ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(date, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyFormatter.format(amount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (amount >= 0) GreenProfit else RedExpense
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Top Products
            if (state.topProducts.isNotEmpty()) {
                ReportCard(
                    title = if (isBangla) "সেরা বিক্রয়" else "Top Products",
                    icon = Icons.Default.TrendingUp,
                    accentColor = GreenProfit
                ) {
                    state.topProducts.forEachIndexed { index, summary ->
                        val productName = state.topProductsDetails.getOrNull(index)?.let {
                            if (isBangla) it.nameBangla else it.name
                        } ?: if (isBangla) "পণ্য #${summary.productId}" else "Product #${summary.productId}"
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${index + 1}. $productName", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyFormatter.format(summary.revenue),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = GreenProfit
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Low stock
            if (state.lowStockProducts.isNotEmpty()) {
                ReportCard(
                    title = if (isBangla) "স্টক সতর্কতা" else "Stock Alert",
                    icon = Icons.Default.Inventory2,
                    accentColor = OrangeDue
                ) {
                    state.lowStockProducts.forEach { product ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (isBangla) product.nameBangla else product.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                if (isBangla) "বাকি: ${product.currentStock.toInt()}" else "Remaining: ${product.currentStock.toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OrangeDue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Slow movers
            if (state.slowMovers.isNotEmpty()) {
                ReportCard(
                    title = if (isBangla) "ধীরগতির পণ্য (১৪+ দিন)" else "Slow Movers (14+ days)",
                    icon = Icons.Default.Inventory2,
                    accentColor = OrangeDue
                ) {
                    state.slowMovers.forEach { product ->
                        Text(
                            if (isBangla) "• ${product.nameBangla} (স্টক: ${product.currentStock.toInt()})"
                            else "• ${product.name} (Stock: ${product.currentStock.toInt()})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Due aging
            if (state.dueCustomers.isNotEmpty()) {
                ReportCard(
                    title = if (isBangla) "বাকি সময়সীমা" else "Due Aging",
                    icon = Icons.Default.People,
                    accentColor = OrangeDue
                ) {
                    val counts = state.dueCustomers.groupBy({ it.first }, { it.second }).mapValues { it.value.count() }
                    counts.forEach { (bracket, count) ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (!isBangla) bracket
                                else when (bracket) {
                                    "0-7 days" -> "০-৭ দিন"
                                    "7-30 days" -> "৭-৩০ দিন"
                                    "30+ days" -> "৩০+ দিন"
                                    else -> bracket
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                if (isBangla) "$count জন" else "$count people",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (state.weeklyProfit.isEmpty() && state.topProducts.isEmpty() && state.lowStockProducts.isEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (isBangla) "পর্যাপ্ত তথ্য নেই। আরও বিক্রয় ও খরচ যোগ করার পর এখানে বিশ্লেষণ দেখুন।"
                    else "Not enough data. Add more sales and expenses to see analysis here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val reportData = viewModel.buildReportData()
                            val file = PdfReportUtil.generateReport(context, reportData)
                            withContext(Dispatchers.Main) {
                                PdfReportUtil.shareReport(context, file)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBangla) "পিডিএফ রিপোর্ট তৈরি করুন" else "Generate PDF Report")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
