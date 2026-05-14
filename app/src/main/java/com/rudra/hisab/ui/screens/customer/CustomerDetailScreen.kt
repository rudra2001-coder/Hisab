package com.rudra.hisab.ui.screens.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    viewModel: CustomerViewModel,
    customerId: Long,
    onBack: () -> Unit
) {
    val state by viewModel.detailState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(customerId) {
        viewModel.loadCustomerDetail(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.customer?.name ?: "গ্রাহক") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val customer = state.customer ?: return@IconButton
                        val uri = Uri.parse("https://wa.me/${customer.phone}?text=${Uri.encode("বাকি পরিমাণ: ${CurrencyFormatter.format(customer.totalDue)}")}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share on WhatsApp")
                    }
                    IconButton(onClick = { viewModel.showPaymentDialog() }) {
                        Icon(Icons.Default.Payment, contentDescription = "Receive Payment")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            state.customer?.let { customer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (customer.totalDue > 0) OrangeDue.copy(alpha = 0.1f)
                        else GreenProfit.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "বর্তমান বাকি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.format(customer.totalDue),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (customer.totalDue > 0) OrangeDue else GreenProfit
                        )
                        if (customer.phone.isNotBlank()) {
                            Text(
                                text = "ফোন: ${customer.phone}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "লেনদেনের ইতিহাস",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.transactions.isEmpty()) {
                Text(
                    text = "কোনো লেনদেন নেই",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.transactions, key = { it.id }) { tx ->
                        TransactionRow(tx)
                    }
                }
            }
        }
    }

    if (state.showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hidePaymentDialog() },
            title = { Text("টাকা গ্রহণ") },
            text = {
                Column {
                    Text("গ্রাহক: ${state.customer?.name}", style = MaterialTheme.typography.bodyLarge)
                    Text("বাকি: ${CurrencyFormatter.format(state.customer?.totalDue ?: 0.0)}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.paymentAmount,
                        onValueChange = viewModel::setPaymentAmount,
                        label = { Text("পরিমাণ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.receivePayment() },
                    enabled = state.paymentAmount.toDoubleOrNull() ?: 0.0 > 0 && !state.isSaving
                ) { Text(if (state.isSaving) "..." else "গ্রহণ") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hidePaymentDialog() }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
private fun TransactionRow(tx: TransactionEntity) {
    val dateFormat = SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (tx.type) {
                TransactionType.SALE -> GreenProfit.copy(alpha = 0.05f)
                TransactionType.PURCHASE -> RedExpense.copy(alpha = 0.05f)
                TransactionType.EXPENSE -> RedExpense.copy(alpha = 0.05f)
            }
        )
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
                    text = when (tx.type) {
                        TransactionType.SALE -> "বিক্রয়"
                        TransactionType.PURCHASE -> "ক্রয়"
                        TransactionType.EXPENSE -> "খরচ"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when (tx.type) {
                        TransactionType.SALE -> GreenProfit
                        else -> RedExpense
                    }
                )
                Text(
                    text = dateFormat.format(Date(tx.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = CurrencyFormatter.format(tx.totalAmount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
