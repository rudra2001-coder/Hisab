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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.DailySnapshotEntity
import com.rudra.hisab.util.LocalStrings
import com.rudra.hisab.util.CurrencyFormatter
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.RedExpense
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun DailyCloseScreen(
    viewModel: DailyCloseViewModel
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalStrings.current
    val isBangla = state.isBangla
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = strings.dailyClose,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.alreadyClosed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenProfit, modifier = Modifier.padding(bottom = 12.dp))
                    Text(
                        text = strings.dayClosedSuccess,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = strings.seeYouTomorrow)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SummaryRow(strings.cashSales, state.cashSales, GreenProfit)
                    SummaryRow(strings.creditSales, state.creditSales, RedExpense)
                    SummaryRow(strings.todaysSales, state.totalSales, MaterialTheme.colorScheme.primary)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SummaryRow(strings.todaysExpenses, state.totalExpenses, RedExpense)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SummaryRow(
                        label = strings.netProfit,
                        value = state.netProfit,
                        color = if (state.netProfit >= 0) GreenProfit else RedExpense,
                        isBold = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.closeDay() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isClosing
            ) {
                Text(if (state.isClosing) strings.closing else strings.closeDay)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = strings.last7Days,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.pastSnapshots.forEach { snapshot ->
                PastSnapshotRow(snapshot, dateFormat, isBangla)
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: Double,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = CurrencyFormatter.format(value),
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PastSnapshotRow(
    snapshot: DailySnapshotEntity,
    dateFormat: SimpleDateFormat,
    isBangla: Boolean
) {
    val strings = LocalStrings.current
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
                    text = "${strings.salesLabel}: ${CurrencyFormatter.format(snapshot.totalSales)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GreenProfit
                )
                Text(
                    text = "${strings.expensesLabel}: ${CurrencyFormatter.format(snapshot.totalExpenses)}",
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
