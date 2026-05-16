package com.rudra.hisab.ui.screens.customer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.ui.theme.BlueInfo
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel,
    onCustomerClick: (Long) -> Unit
) {
    val state by viewModel.listState.collectAsState()
    val filteredCustomers by remember(state.customers, state.searchQuery) {
        derivedStateOf { viewModel.getFilteredCustomers() }
    }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }
    val isBangla = true

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddCustomerDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = if (isBangla) "গ্রাহক যোগ" else "Add Customer")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = if (isBangla) "গ্রাহক" else "Customers",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlueInfo.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(if (isBangla) "মোট" else "Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.customers.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BlueInfo)
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = OrangeDue.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(if (isBangla) "বাকি" else "Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyFormatter.format(state.totalDues), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OrangeDue)
                    }
                }
                val paidCount = state.customers.count { it.totalDue <= 0 }
                Card(
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenProfit.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(if (isBangla) "পরিশোধিত" else "Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(paidCount.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GreenProfit)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text(if (isBangla) "গ্রাহক খুঁজুন" else "Search customer") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.customers.isEmpty())
                            (if (isBangla) "কোনো গ্রাহক নেই — বাকি বিক্রয় গ্রাহক যোগ করবে" else "No customers yet — Credit sales will add customers")
                        else
                            (if (isBangla) "কোনো গ্রাহক পাওয়া যায়নি" else "No customers found"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onClick = { onCustomerClick(customer.id) },
                            onLongClick = { customerToDelete = customer },
                            isBangla = isBangla
                        )
                    }
                }
            }
        }
    }

    customerToDelete?.let { c ->
        if (c.totalDue > 0) {
            AlertDialog(
                onDismissRequest = { customerToDelete = null },
                title = { Text(if (isBangla) "মুছা যাবে না" else "Cannot Delete") },
                text = { Text("${if (isBangla) "এই গ্রাহকের" else "This customer has"} ${CurrencyFormatter.format(c.totalDue)} ${if (isBangla) "বাকি আছে। আগে পেমেন্ট নিন।" else "due. Please receive payment first."}") },
                confirmButton = {
                    TextButton(onClick = { customerToDelete = null }) { Text("OK") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { customerToDelete = null },
                title = { Text(if (isBangla) "গ্রাহক মুছুন" else "Delete Customer") },
                text = { Text("${if (isBangla) "আপনি কি ${c.name} কে মুছে ফেলতে চান?" else "Are you sure you want to delete ${c.name}?"} ") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.deleteCustomer(c)
                        customerToDelete = null
                    }) { Text(if (isBangla) "মুছুন" else "Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { customerToDelete = null }) { Text(if (isBangla) "বাতিল" else "Cancel") }
                }
            )
        }
    }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddCustomerDialog() },
            title = { Text(if (isBangla) "নতুন গ্রাহক" else "New Customer") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.newCustomerName,
                        onValueChange = viewModel::setNewCustomerName,
                        label = { Text(if (isBangla) "নাম" else "Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.newCustomerPhone,
                        onValueChange = viewModel::setNewCustomerPhone,
                        label = { Text(if (isBangla) "ফোন" else "Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp)
                    )
                    state.phoneError?.let { error ->
                        Text(
                            text = error,
                            color = RedExpense,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.newCustomerAddress,
                        onValueChange = viewModel::setNewCustomerAddress,
                        label = { Text(if (isBangla) "ঠিকানা" else "Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addCustomer() },
                    enabled = state.newCustomerName.isNotBlank()
                ) { Text(if (isBangla) "যোগ করুন" else "Add") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddCustomerDialog() }) { Text(if (isBangla) "বাতিল" else "Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomerCard(
    customer: CustomerEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isBangla: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (customer.phone.isNotBlank()) {
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (customer.lastTransactionAt != null) {
                    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    Text(
                        text = dateFormat.format(Date(customer.lastTransactionAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (customer.totalDue > 0) {
                    Text(
                        text = CurrencyFormatter.format(customer.totalDue),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OrangeDue
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GreenProfit.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = if (isBangla) "পরিশোধিত" else "Paid",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GreenProfit,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}