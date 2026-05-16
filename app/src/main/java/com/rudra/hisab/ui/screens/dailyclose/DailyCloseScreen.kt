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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    val isBangla = state.isBangla

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (isBangla) "দৈনিক সমাপ্তি" else "Daily Close",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            Text(
                if (isBangla) "লোড হচ্ছে..." else "Loading...",
                style = MaterialTheme.typography.bodyLarge
            )
        } else if (state.isClosed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GreenProfit.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenProfit, modifier = Modifier.padding(bottom = 12.dp))
                    Text(
                        if (isBangla) "দিন সফলভাবে বন্ধ!" else "Day closed successfully!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GreenProfit,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (isBangla) "আগামীকাল আবার দেখা হবে" else "See you tomorrow",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (state.alreadyClosed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeDue.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (isBangla) "আজকের হিসাব আগেই বন্ধ হয়েছে" else "Today already closed",
                        style = MaterialTheme.typography.titleLarge,
                        color = OrangeDue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            SummaryRow(
                if (isBangla) "আজকের বিক্রয়" else "Today's Sales",
                state.totalSales, GreenProfit
            )
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow(
                if (isBangla) "নগদ বিক্রয়" else "Cash Sales",
                state.cashSales, GreenProfit
            )
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow(
                if (isBangla) "বাকি বিক্রয়" else "Credit Sales",
                state.creditSales, OrangeDue
            )
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow(
                if (isBangla) "আজকের খরচ" else "Today's Expenses",
                state.totalExpenses, RedExpense
            )
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow(
                if (isBangla) "ক্রয়" else "Purchases",
                state.totalPurchases, RedExpense
            )
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow(
                if (isBangla) "কতটি বিক্রয়" else "Sale Count",
                state.saleCount.toDouble(), MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow(
                if (isBangla) "নতুন বাকি" else "New Dues",
                state.newDues, OrangeDue
            )
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRow(
                if (isBangla) "পেমেন্ট পেয়েছি" else "Payments Received",
                state.paymentsReceived, GreenProfit
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                    Text(
                        if (isBangla) "নিট মুনাফা" else "Net Profit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
                enabled = !state.isClosing,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (state.isClosing) {
                        if (isBangla) "হিসাব বন্ধ হচ্ছে..." else "Closing..."
                    } else {
                        if (isBangla) "দিন বন্ধ করুন" else "Close Day"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        if (state.pastSnapshots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isBangla) "গত ৭ দিন" else "Last 7 Days",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            state.pastSnapshots.reversed().forEach { snapshot ->
                PastSnapshotRow(
                    snapshot = snapshot,
                    dateFormat = dateFormat,
                    isBangla = isBangla
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
private fun PastSnapshotRow(
    snapshot: DailySnapshotEntity,
    dateFormat: SimpleDateFormat,
    isBangla: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                    text = "${if (isBangla) "বিক্রয়" else "Sales"}: ${CurrencyFormatter.format(snapshot.totalSales)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GreenProfit
                )
                Text(
                    text = "${if (isBangla) "খরচ" else "Expenses"}: ${CurrencyFormatter.format(snapshot.totalExpenses)}",
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