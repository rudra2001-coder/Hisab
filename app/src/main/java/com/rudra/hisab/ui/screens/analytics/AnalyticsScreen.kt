package com.rudra.hisab.ui.screens.analytics

import android.content.Context
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("bn"))

    if (showStartPicker) {
        val startPickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.customStartDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startPickerState.selectedDateMillis?.let { millis ->
                        viewModel.setCustomStartDate(
                            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        )
                    }
                    showStartPicker = false
                }) { Text("ঠিক") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("বাতিল") }
            }
        ) {
            DatePicker(state = startPickerState)
        }
    }

    if (showEndPicker) {
        val endPickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.customEndDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endPickerState.selectedDateMillis?.let { millis ->
                        viewModel.setCustomEndDate(
                            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        )
                    }
                    showEndPicker = false
                }) { Text("ঠিক") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("বাতিল") }
            }
        ) {
            DatePicker(state = endPickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "রিপোর্ট ও বিশ্লেষণ",
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
                Text(
                    text = state.customStartDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("—", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(
                onClick = { showEndPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = state.customEndDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = { viewModel.refreshWithCustomRange() }) {
                Icon(Icons.Default.Check, contentDescription = "প্রয়োগ", tint = BlueInfo)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isLoading) {
            Text("লোড হচ্ছে...", style = MaterialTheme.typography.bodyLarge)
        } else {
            // Weekly Profit
            ReportCard(title = "সাপ্তাহিক মুনাফা", icon = Icons.Default.TrendingUp) {
                state.weeklyProfit.forEachIndexed { index, profit ->
                    val dayName = when (index) { 0 -> "শনি"; 1 -> "রবি"; 2 -> "সোম"; 3 -> "মঙ্গল"; 4 -> "বুধ"; 5 -> "বৃহস্পতি"; 6 -> "শুক্র"; else -> "দিন $index" }
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(dayName, style = MaterialTheme.typography.bodyMedium)
                        Text(CurrencyFormatter.format(profit), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (profit >= 0) GreenProfit else RedExpense)
                    }
                }
            }

            // Customer Outstanding
            if (state.customerOutstanding.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ReportCard(title = "গ্রাহক বকেয়া", icon = Icons.Default.People) {
                    state.customerOutstanding.take(10).forEach { c ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(c.name, style = MaterialTheme.typography.bodyMedium)
                            Text(CurrencyFormatter.format(c.totalDue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = OrangeDue)
                        }
                    }
                }
            }

            // Product Profit
            if (state.productProfit.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ReportCard(title = "পণ্য লাভ (৩০ দিন)", icon = Icons.Default.AttachMoney) {
                    state.productProfit.take(10).forEach { (product, profit) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(product.nameBangla, style = MaterialTheme.typography.bodyMedium)
                            Text(CurrencyFormatter.format(profit), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (profit >= 0) GreenProfit else RedExpense)
                        }
                    }
                }
            }

            // Monthly Growth
            if (state.monthlyGrowth.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ReportCard(title = "মাসিক প্রবৃদ্ধি", icon = Icons.Default.TrendingUp) {
                    state.monthlyGrowth.forEach { (label, profit) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                            Text(CurrencyFormatter.format(profit), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (profit >= 0) GreenProfit else RedExpense)
                        }
                    }
                }
            }

            // Cash Flow
            if (state.cashFlow.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ReportCard(title = "নগদ প্রবাহ (দৈনিক)", icon = Icons.Default.AttachMoney) {
                    state.cashFlow.forEach { (date, amount) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(date, style = MaterialTheme.typography.bodyMedium)
                            Text(CurrencyFormatter.format(amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (amount >= 0) GreenProfit else RedExpense)
                        }
                    }
                }
            }

            // Top Products
            if (state.topProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ReportCard(title = "সেরা বিক্রয়", icon = Icons.Default.TrendingUp) {
                    state.topProducts.forEachIndexed { index, summary ->
                        val productName = state.topProductsDetails.getOrNull(index)?.nameBangla ?: "পণ্য #${summary.productId}"
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${index + 1}. $productName", style = MaterialTheme.typography.bodyMedium)
                            Text(CurrencyFormatter.format(summary.revenue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = GreenProfit)
                        }
                    }
                }
            }

            // Low stock
            if (state.lowStockProducts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ReportCard(title = "স্টক সতর্কতা", icon = Icons.Default.AttachMoney, accentColor = OrangeDue) {
                    state.lowStockProducts.forEach { product ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(product.nameBangla, style = MaterialTheme.typography.bodyMedium)
                            Text("বাকি: ${product.currentStock.toInt()}", style = MaterialTheme.typography.bodyMedium, color = OrangeDue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Slow movers
            if (state.slowMovers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ReportCard(title = "ধীরগতির পণ্য (১৪+ দিন)", icon = Icons.Default.AttachMoney) {
                    state.slowMovers.forEach { product ->
                        Text("• ${product.nameBangla} (স্টক: ${product.currentStock.toInt()})", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Due aging
            if (state.dueCustomers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ReportCard(title = "বাকি সময়সীমা", icon = Icons.Default.People, accentColor = OrangeDue) {
                    val counts = state.dueCustomers.groupBy({ it.first }, { it.second }).mapValues { it.value.count() }
                    counts.forEach { (bracket, count) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(when (bracket) { "0-7 days" -> "০-৭ দিন"; "7-30 days" -> "৭-৩০ দিন"; "30+ days" -> "৩০+ দিন"; else -> bracket }, style = MaterialTheme.typography.bodyMedium)
                            Text("$count জন", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (state.weeklyProfit.isEmpty() && state.topProducts.isEmpty() && state.lowStockProducts.isEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "পর্যাপ্ত তথ্য নেই। আরও বিক্রয় ও খরচ যোগ করার পর এখানে বিশ্লেষণ দেখুন।",
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
                Text("পিডিএফ রিপোর্ট তৈরি করুন")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.05f)
        )
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
