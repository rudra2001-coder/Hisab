package com.rudra.hisab.ui.screens.accounting

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.LedgerEntryEntity
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: AccountingViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(
        floatingActionButton = {
            if (state.selectedTab == AccountingTab.CASH_BOOK) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FloatingActionButton(
                        onClick = { viewModel.showAddCashEntry("CASH_IN") },
                        containerColor = GreenProfit
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Cash In", tint = Color.White)
                    }
                    FloatingActionButton(
                        onClick = { viewModel.showAddCashEntry("CASH_OUT") },
                        containerColor = RedExpense
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Cash Out", tint = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "অ্যাকাউন্টিং",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                AccountingTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    AccountingTab.CASH_BOOK -> "ক্যাশ বুক"
                                    AccountingTab.LEDGER -> "লেজার"
                                    AccountingTab.EXPENSES -> "খরচ"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (state.selectedTab) {
                AccountingTab.CASH_BOOK -> CashBookContent(state, sdf)
                AccountingTab.LEDGER -> LedgerContent(state)
                AccountingTab.EXPENSES -> ExpensesContent()
            }
        }

        if (state.showAddCashEntryDialog) {
            AddCashEntryDialog(state, viewModel)
        }
    }
}

@Composable
private fun CashBookContent(state: AccountingUiState, sdf: SimpleDateFormat) {
    val todayIn = state.cashBookEntries.filter { it.type == "CASH_IN" }.sumOf { it.amount }
    val todayOut = state.cashBookEntries.filter { it.type == "CASH_OUT" }.sumOf { it.amount }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryCard("আজকের ইন", todayIn, GreenProfit, Modifier.weight(1f))
        SummaryCard("আজকের আউট", todayOut, RedExpense, Modifier.weight(1f))
        SummaryCard("নেট ব্যালেন্স", todayIn - todayOut, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.cashBookEntries) { entry ->
            CashBookRow(entry, sdf)
        }
    }
}

@Composable
private fun LedgerContent(state: AccountingUiState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryCard("গ্রাহক", state.customerBalance, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        SummaryCard("সরবরাহকারী", state.supplierBalance, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
        SummaryCard("জেনারেল", state.generalBalance, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.ledgerEntries) { entry ->
            LedgerRow(entry)
        }
    }
}

@Composable
private fun ExpensesContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("খরচ ব্যবস্থাপনা শীঘ্রই আসছে", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SummaryCard(label: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyFormatter.format(value),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun CashBookRow(entry: com.rudra.hisab.ui.screens.accounting.CashBookEntry, sdf: SimpleDateFormat) {
    val isIn = entry.type == "CASH_IN"
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isIn) GreenProfit.copy(alpha = 0.08f) else RedExpense.copy(alpha = 0.08f)
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(entry.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(sdf.format(Date(entry.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIn) "+" else "-"}${CurrencyFormatter.format(entry.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIn) GreenProfit else RedExpense
                )
                entry.paymentMethod?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun LedgerRow(entry: com.rudra.hisab.data.local.entity.LedgerEntryEntity) {
    val isDebit = entry.entryType.name == "DEBIT"
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(entry.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text("${entry.accountType} · ${entry.entryType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "${if (isDebit) "Dr" else "Cr"} ${CurrencyFormatter.format(entry.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDebit) GreenProfit else RedExpense
            )
        }
    }
}

@Composable
private fun AddCashEntryDialog(state: AccountingUiState, viewModel: AccountingViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::hideAddCashEntry,
        title = { Text(if (state.cashEntryType == "CASH_IN") "ক্যাশ ইন" else "ক্যাশ আউট") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.cashEntryAmount,
                    onValueChange = viewModel::setCashEntryAmount,
                    label = { Text("পরিমাণ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.cashEntryDescription,
                    onValueChange = viewModel::setCashEntryDescription,
                    label = { Text("বিবরণ") },
                    singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = viewModel::saveCashEntry) { Text("সংরক্ষণ") } },
        dismissButton = { TextButton(onClick = viewModel::hideAddCashEntry) { Text("বাতিল") } }
    )
}
