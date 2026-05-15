package com.rudra.hisab.ui.screens.dailyclose

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.DailySnapshotEntity
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyCloseScreen(
    viewModel: DailyCloseViewModel
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
        } else if (state.isClosed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GreenProfit.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenProfit, modifier = Modifier.padding(bottom = 12.dp))
                    Text("দিন সফলভাবে বন্ধ!", style = MaterialTheme.typography.headlineSmall, color = GreenProfit, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("আগামীকাল আবার দেখা হবে", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (state.alreadyClosed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = OrangeDue.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("আজকের হিসাব আগেই বন্ধ হয়েছে", style = MaterialTheme.typography.titleLarge, color = OrangeDue, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            SummaryRow("আজকের বিক্রয়", state.totalSales, GreenProfit)
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow("নগদ বিক্রয়", state.cashSales, GreenProfit)
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow("বাকি বিক্রয়", state.creditSales, OrangeDue)
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow("আজকের খরচ", state.totalExpenses, RedExpense)
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow("ক্রয়", state.totalPurchases, RedExpense)
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow("কতটি বিক্রয়", state.saleCount.toDouble(), MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow("নতুন বাকি", state.newDues, OrangeDue)
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow("পেমেন্ট পেয়েছি", state.paymentsReceived, GreenProfit)
            Spacer(modifier = Modifier.height(12.dp))

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
                    horizontalArrangement = Arrangement.SpaceBetween
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
                    if (state.isClosing) "হিসাব বন্ধ হচ্ছে..." else "দিন বন্ধ করুন",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        if (state.pastSnapshots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "গত ৭ দিন",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            state.pastSnapshots.reversed().forEach { snapshot ->
                PastSnapshotRow(snapshot = snapshot, dateFormat = dateFormat)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = CurrencyFormatter.format(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun PastSnapshotRow(snapshot: DailySnapshotEntity, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateFormat.format(Date(snapshot.date)),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "বিক্রয়: ${CurrencyFormatter.format(snapshot.totalSales)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GreenProfit
                )
                Text(
                    text = "খরচ: ${CurrencyFormatter.format(snapshot.totalExpenses)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = RedExpense
                )
            }
            Text(
                text = CurrencyFormatter.format(snapshot.netProfit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (snapshot.netProfit >= 0) GreenProfit else RedExpense
            )
        }
    }
}
