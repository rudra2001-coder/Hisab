package com.rudra.hisab.ui.screens.dailyclose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter

@Composable
fun DailyCloseScreen(
    viewModel: DailyCloseViewModel
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "দৈনিক সমাপ্তি",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            Text("লোড হচ্ছে...", style = MaterialTheme.typography.bodyLarge)
        } else if (state.alreadyClosed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GreenProfit.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenProfit)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("আজকের হিসাব ইতিমধ্যে বন্ধ করা হয়েছে", style = MaterialTheme.typography.titleLarge, color = GreenProfit)
                }
            }
        } else {
            SummaryRow("মোট বিক্রয়", state.totalSales, GreenProfit)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow("মোট খরচ", state.totalExpenses, RedExpense)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow("মোট ক্রয়", state.totalPurchases, RedExpense)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow("নগদ প্রাপ্তি", state.cashReceived, GreenProfit)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow("বাকি দেওয়া", state.creditGiven, OrangeDue)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.netProfit >= 0) GreenProfit.copy(alpha = 0.1f)
                    else RedExpense.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text("নিট মুনাফা", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = CurrencyFormatter.format(state.netProfit),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (state.netProfit >= 0) GreenProfit else RedExpense
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.closeDay() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isClosing
            ) {
                Text(
                    if (state.isClosing) "হিসাব বন্ধ হচ্ছে..." else "দিন শেষ করুন",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (state.isClosed) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "দিন সফলভাবে বন্ধ!",
                    style = MaterialTheme.typography.titleLarge,
                    color = GreenProfit,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = CurrencyFormatter.format(amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
