package com.rudra.hisab.ui.screens.customer

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.PaymentStatus
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.ui.theme.WarningYellow
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
    val listState by viewModel.listState.collectAsState()
    val detailState by viewModel.detailState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentCustomer = detailState.customer

    LaunchedEffect(customerId) {
        viewModel.loadCustomerDetail(customerId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(detailState.customer?.name ?: "গ্রাহক") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val customer = detailState.customer
                    if (customer != null) {
                        IconButton(onClick = {
                            if (customer.phone.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("গ্রাহকের ফোন নম্বর নেই")
                                }
                            } else {
                                val dateStr = dateFormat.format(Date())
                                val message = "প্রিয় ${customer.name}, আপনার হিসাব অ্যাপে বর্তমান বকেয়া: ${CurrencyFormatter.format(customer.totalDue)}। তারিখ: $dateStr. ধন্যবাদ।"
                                val uri = Uri.parse("https://wa.me/88${customer.phone}?text=${Uri.encode(message)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "WhatsApp")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val customer = detailState.customer

        if (customer == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("গ্রাহক লোড হচ্ছে...", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    HeaderCard(
                        customer = customer,
                        onCall = {
                            if (customer.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                context.startActivity(intent)
                            }
                        },
                        onPayment = { viewModel.showPaymentSheet() }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "লেনদেনের ইতিহাস",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (detailState.transactionsWithBalance.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কোনো লেনদেন নেই",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(detailState.transactionsWithBalance, key = { it.transaction.id }) { twb ->
                        TransactionRow(
                            transaction = twb.transaction,
                            runningBalance = twb.runningBalance
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (detailState.showPaymentSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hidePaymentSheet() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PaymentSheetContent(
                customer = currentCustomer!!,
                amount = detailState.paymentAmount,
                note = detailState.paymentNote,
                showOverpaymentWarning = detailState.showOverpaymentWarning,
                isSaving = detailState.isSaving,
                errorMessage = detailState.errorMessage,
                onAmountChange = viewModel::setPaymentAmount,
                onNoteChange = viewModel::setPaymentNote,
                onConfirm = viewModel::receivePayment,
                onDismiss = viewModel::hidePaymentSheet
            )
        }
    }
}

@Composable
private fun HeaderCard(
    customer: CustomerEntity,
    onCall: () -> Unit,
    onPayment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (customer.totalDue > 0) OrangeDue.copy(alpha = 0.08f)
            else GreenProfit.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = customer.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (customer.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .clickable(onClick = onCall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "বর্তমান বাকি",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(customer.totalDue),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (customer.totalDue > 0) OrangeDue else GreenProfit
                    )
                }
                if (customer.totalDue > 0) {
                    Button(
                        onClick = onPayment,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenProfit
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("পেমেন্ট নিন")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GreenProfit,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "পরিশোধিত",
                            style = MaterialTheme.typography.titleMedium,
                            color = GreenProfit,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    runningBalance: Double
) {
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    val isSale = transaction.type == TransactionType.SALE
    val isPayment = transaction.type == TransactionType.PAYMENT
    val isCredit = transaction.paymentType != PaymentStatus.CASH

    val description = when {
        isPayment -> "টাকা গ্রহণ"
        isSale && isCredit -> "বিক্রয় (বাকি)"
        isSale -> "বিক্রয়"
        transaction.type == TransactionType.PURCHASE -> "ক্রয়"
        transaction.type == TransactionType.EXPENSE -> "খরচ"
        else -> "লেনদেন"
    }

    val amountColor = when {
        isPayment -> GreenProfit
        isSale && isCredit -> RedExpense
        isSale -> GreenProfit
        else -> MaterialTheme.colorScheme.onSurface
    }

    val amount = when {
        isPayment -> transaction.totalAmount
        isSale && isCredit -> transaction.totalAmount - transaction.paidAmount
        isSale -> transaction.totalAmount
        else -> transaction.totalAmount
    }

    val amountPrefix = when {
        isPayment -> "-"
        isSale && isCredit -> "+"
        isSale -> ""
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPayment -> GreenProfit.copy(alpha = 0.05f)
                isSale && isCredit -> RedExpense.copy(alpha = 0.05f)
                isSale -> GreenProfit.copy(alpha = 0.05f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
                Text(
                    text = dateFormat.format(Date(transaction.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.notes.isNotBlank() && transaction.notes != "টাকা গ্রহণ") {
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${CurrencyFormatter.format(amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = "ব্যাল: ${CurrencyFormatter.format(runningBalance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentSheetContent(
    customer: CustomerEntity,
    amount: String,
    note: String,
    showOverpaymentWarning: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "টাকা গ্রহণ",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "গ্রাহক: ${customer.name}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = OrangeDue.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "বাকি আছে",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(customer.totalDue),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangeDue
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("পরিমাণ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.headlineSmall,
            isError = showOverpaymentWarning
        )

        if (showOverpaymentWarning) {
            Text(
                text = "এই পরিমাণ বকেয়ার চেয়ে বেশি",
                color = WarningYellow,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            label = { Text("নোট (ঐচ্ছিক)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = RedExpense,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isSaving && (amount.toDoubleOrNull() ?: 0.0) > 0,
            colors = ButtonDefaults.buttonColors(containerColor = GreenProfit)
        ) {
            if (isSaving) {
                Text("হিসাব করা হচ্ছে...")
            } else {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("পেমেন্ট গ্রহণ", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
