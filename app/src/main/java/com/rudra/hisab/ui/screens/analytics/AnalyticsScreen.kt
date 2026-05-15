package com.rudra.hisab.ui.screens.analytics

import android.content.Context
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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter
import com.rudra.hisab.util.PdfReportUtil
import com.rudra.hisab.util.ReportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val state by viewModel.state.collectAsState()

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

        Spacer(modifier = Modifier.height(20.dp))

        if (state.isLoading) {
            Text("লোড হচ্ছে...", style = MaterialTheme.typography.bodyLarge)
        } else {
            // Profit trend
            if (state.weeklyProfit.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("সাপ্তাহিক মুনাফা", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        state.weeklyProfit.forEachIndexed { index, profit ->
                            val dayName = when (index) {
                                0 -> "শনি"
                                1 -> "রবি"
                                2 -> "সোম"
                                3 -> "মঙ্গল"
                                4 -> "বুধ"
                                5 -> "বৃহস্পতি"
                                6 -> "শুক্র"
                                else -> "দিন $index"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
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
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Top products
            if (state.topProducts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("সেরা বিক্রয়", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        state.topProducts.forEachIndexed { index, summary ->
                            val productName = state.topProductsDetails.getOrNull(index)?.nameBangla ?: "পণ্য #${summary.productId}"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
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
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Low stock
            if (state.lowStockProducts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = OrangeDue.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("স্টক সতর্কতা", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = OrangeDue)
                        Spacer(modifier = Modifier.height(8.dp))
                        state.lowStockProducts.forEach { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(product.nameBangla, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "বাকি: ${product.currentStock.toInt()} ${product.unit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OrangeDue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Slow movers
            if (state.slowMovers.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ধীরগতির পণ্য (১৪+ দিন)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        state.slowMovers.forEach { product ->
                            Text(
                                "• ${product.nameBangla} (স্টক: ${product.currentStock.toInt()})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Due aging
            if (state.dueCustomers.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = OrangeDue.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("বাকি সময়সীমা", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = OrangeDue)
                        Spacer(modifier = Modifier.height(8.dp))
                        val counts = state.dueCustomers.groupBy({ it.first }, { it.second }).mapValues { it.value.count() }
                        counts.forEach { (bracket, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    when (bracket) {
                                        "0-7 days" -> "০-৭ দিন"
                                        "7-30 days" -> "৭-৩০ দিন"
                                        "30+ days" -> "৩০+ দিন"
                                        else -> bracket
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "$count জন",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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

            val context = LocalContext.current
            val scope = rememberCoroutineScope()
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
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text("পিডিএফ রিপোর্ট তৈরি করুন")
            }
        }
    }
}
