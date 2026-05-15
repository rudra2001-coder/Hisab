package com.rudra.hisab.ui.screens.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.PaymentType
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SaleScreen(
    viewModel: SaleViewModel
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "নতুন বিক্রয়",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = GreenProfit.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GreenProfit, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("আজকের বিক্রয়", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyFormatter.format(state.todaySalesTotal), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GreenProfit)
                    }
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = GreenProfit.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sell, contentDescription = null, tint = GreenProfit, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("বিক্রয় সংখ্যা", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.todaySaleCount}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GreenProfit)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.selectedProduct == null) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("পণ্য খুঁজুন") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                val filteredProducts = if (state.searchQuery.isBlank()) state.products
                else state.products.filter {
                    it.name.contains(state.searchQuery, ignoreCase = true) ||
                            it.nameBangla.contains(state.searchQuery, ignoreCase = true)
                }

                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("কোনো পণ্য নেই", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredProducts.forEach { product ->
                            ProductTile(
                                product = product,
                                onClick = { viewModel.selectProduct(product) }
                            )
                        }
                    }
                }
            }
        } else {
            SaleForm(
                state = state,
                onQuantityChange = viewModel::setQuantity,
                onPaymentTypeChange = viewModel::setPaymentType,
                onPaidAmountChange = viewModel::setPaidAmount,
                onConfirm = viewModel::completeSale,
                onCancel = viewModel::clearSelection
            )
        }
    }
}

@Composable
private fun ProductTile(
    product: ProductEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(150.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = product.nameBangla,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyFormatter.format(product.sellPrice),
                style = MaterialTheme.typography.titleLarge,
                color = GreenProfit,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "স্টক: ${product.currentStock.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SaleForm(
    state: SaleState,
    onQuantityChange: (String) -> Unit,
    onPaymentTypeChange: (PaymentType) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val product = state.selectedProduct ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = product.nameBangla,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "দাম: ${CurrencyFormatter.format(product.sellPrice)} / ${product.unit}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("পরিমাণ", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.quantity,
            onValueChange = onQuantityChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineLarge
        )

        val totalAmount = (state.quantity.toDoubleOrNull() ?: 0.0) * (product.sellPrice)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "মোট: ${CurrencyFormatter.format(totalAmount)}",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = GreenProfit
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("পরিশোধের ধরন", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentType.entries.forEach { type ->
                FilterChip(
                    selected = state.paymentType == type,
                    onClick = { onPaymentTypeChange(type) },
                    label = {
                        Text(
                            when (type) {
                                PaymentType.CASH -> "নগদ"
                                PaymentType.CREDIT -> "বাকি"
                                PaymentType.PARTIAL -> "আংশিক"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenProfit.copy(alpha = 0.2f)
                    )
                )
            }
        }

        if (state.paymentType == PaymentType.PARTIAL) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.paidAmount,
                onValueChange = onPaidAmountChange,
                label = { Text("পরিশোধিত পরিমাণ") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !state.isSaving && state.quantity.toDoubleOrNull() ?: 0.0 > 0
        ) {
            if (state.isSaving) {
                Text("হিসাব করা হচ্ছে...")
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("বিক্রয় নিশ্চিত", style = MaterialTheme.typography.titleLarge)
            }
        }

        if (state.saleComplete) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = GreenProfit.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "বিক্রয় সফল!",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = GreenProfit,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
