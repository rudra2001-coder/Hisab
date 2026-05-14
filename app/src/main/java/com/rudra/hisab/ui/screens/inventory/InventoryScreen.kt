package com.rudra.hisab.ui.screens.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel
) {
    val state by viewModel.state.collectAsState()
    val filteredProducts = viewModel.getFilteredProducts()
    var showAddForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "মজুদ",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showAddForm = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder = { Text("পণ্য খুঁজুন") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("কোনো পণ্য নেই", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddForm = true }) {
                        Text("পণ্য যোগ করুন")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onAddStock = { viewModel.showStockDialog(product, true) },
                        onRemoveStock = { viewModel.showStockDialog(product, false) },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
            }
        }
    }

    if (showAddForm) {
        AddProductDialog(
            categories = state.categories,
            onDismiss = { showAddForm = false },
            onSave = { name, nameBn, unit, buy, sell, stock, low, catId ->
                viewModel.addProduct(name, nameBn, unit, buy, sell, stock, low, catId)
                showAddForm = false
            }
        )
    }

    if (state.showStockDialog && state.stockDialogProduct != null) {
        StockDialog(
            product = state.stockDialogProduct!!,
            isAdd = state.stockDialogIsAdd,
            quantity = state.stockQuantity,
            onQuantityChange = viewModel::setStockQuantity,
            onConfirm = viewModel::confirmStockUpdate,
            onDismiss = viewModel::hideStockDialog
        )
    }
}

@Composable
private fun ProductCard(
    product: ProductEntity,
    onAddStock: () -> Unit,
    onRemoveStock: () -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = product.currentStock <= product.lowStockThreshold

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) OrangeDue.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isLowStock) androidx.compose.foundation.BorderStroke(1.dp, OrangeDue) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.nameBangla,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${product.unit} - ${CurrencyFormatter.format(product.sellPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (isLowStock) "${product.currentStock.toInt()} !" else product.currentStock.toInt().toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isLowStock) OrangeDue else GreenProfit
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onRemoveStock) {
                    Icon(
                        Icons.Default.RemoveCircle,
                        contentDescription = "Remove Stock",
                        tint = RedExpense
                    )
                }
                IconButton(onClick = onAddStock) {
                    Icon(
                        Icons.Default.AddCircle,
                        contentDescription = "Add Stock",
                        tint = GreenProfit
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddProductDialog(
    categories: List<com.rudra.hisab.data.local.entity.CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Double, Double, Double, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("piece") }
    var buyPrice by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var lowStock by remember { mutableStateOf("10") }
    var selectedCategory by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন পণ্য") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("নাম (English)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = nameBn, onValueChange = { nameBn = it }, label = { Text("নাম (বাংলা)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("একক (kg/piece/litre)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = buyPrice, onValueChange = { buyPrice = it }, label = { Text("ক্রয় মূল্য") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = sellPrice, onValueChange = { sellPrice = it }, label = { Text("বিক্রয় মূল্য") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("প্রাথমিক মজুদ") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = lowStock, onValueChange = { lowStock = it }, label = { Text("সতর্কতা সীমা") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val buy = buyPrice.toDoubleOrNull() ?: 0.0
                    val sell = sellPrice.toDoubleOrNull() ?: 0.0
                    val stk = stock.toDoubleOrNull() ?: 0.0
                    val low = lowStock.toDoubleOrNull() ?: 10.0
                    onSave(name, nameBn, unit, buy, sell, stk, low, selectedCategory)
                },
                enabled = name.isNotBlank() && nameBn.isNotBlank()
            ) {
                Text("সংরক্ষণ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
private fun StockDialog(
    product: ProductEntity,
    isAdd: Boolean,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isAdd) "স্টক যোগ" else "স্টক বাদ") },
        text = {
            Column {
                Text("পণ্য: ${product.nameBangla}", style = MaterialTheme.typography.bodyLarge)
                Text("বর্তমান স্টক: ${product.currentStock.toInt()}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("পরিমাণ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = quantity.toDoubleOrNull() ?: 0.0 > 0) {
                Text(if (isAdd) "যোগ" else "বাদ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
