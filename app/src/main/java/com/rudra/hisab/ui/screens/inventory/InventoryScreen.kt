package com.rudra.hisab.ui.screens.inventory

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.CategoryEntity
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.data.local.entity.TransactionType
import com.rudra.hisab.ui.theme.BlueInfo
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.BanglaNumberConverter
import com.rudra.hisab.util.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showUndoSnackbar by remember { mutableStateOf(false) }

    val deletedProduct = state.deletedProduct
    LaunchedEffect(deletedProduct) {
        if (deletedProduct != null) {
            showUndoSnackbar = true
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "${deletedProduct.nameBangla} মুছে ফেলা হয়েছে",
                    actionLabel = "পূর্বাবস্থায় আনুন"
                )
                showUndoSnackbar = false
            }
        }
    }

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
            IconButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "পণ্য যোগ করুন")
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

        if (state.categories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategoryId == null,
                    onClick = { viewModel.setSelectedCategory(null) },
                    label = { Text("সব") }
                )
                state.categories.forEach { cat ->
                    FilterChip(
                        selected = state.selectedCategoryId == cat.id,
                        onClick = { viewModel.setSelectedCategory(cat.id) },
                        label = { Text(cat.nameBangla) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val productCount = state.products.size
        val totalStockValue = state.products.sumOf { it.currentStock * it.buyPrice }
        val lowStockCount = state.products.count { it.currentStock <= it.lowStockThreshold }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = BlueInfo.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("পণ্য", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$productCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BlueInfo)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = GreenProfit.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("স্টক মূল্য", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(totalStockValue), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GreenProfit)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = if (lowStockCount > 0) OrangeDue.copy(alpha = 0.12f) else GreenProfit.copy(alpha = 0.08f)),
                onClick = {
                    if (lowStockCount > 0) {
                        viewModel.setSearchQuery("")
                    }
                }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("কম স্টক", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$lowStockCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (lowStockCount > 0) OrangeDue else GreenProfit)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filteredProducts = viewModel.getFilteredProducts()

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.products.isEmpty()) "কোনো পণ্য নেই"
                        else "কোনো পণ্য পাওয়া যায়নি",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (state.products.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.showAddDialog() }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("পণ্য যোগ করুন")
                        }
                    }
                }
            }
        } else {
            LazyColumn {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onAddStock = { viewModel.showStockDialog(product, true) },
                        onRemoveStock = { viewModel.showStockDialog(product, false) },
                        onDelete = { viewModel.requestDeleteProduct(product) },
                        onShowHistory = { viewModel.showPriceHistory(product) }
                    )
                }
            }
        }
    }

    if (state.showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("পণ্য মুছুন") },
            text = { Text("${state.showDeleteConfirm!!.nameBangla} মুছে ফেলবেন? এই পণ্যের সকল তথ্য হারিয়ে যাবে।") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("মুছুন", color = RedExpense)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("বাতিল") }
            }
        )
    }

    if (state.showAddDialog) {
        AddProductDialog(
            categories = state.categories,
            onDismiss = viewModel::hideAddDialog,
            onConfirm = { name, nameBn, unit, buy, sell, stock, lowStock, catId ->
                viewModel.addProduct(name, nameBn, unit, buy, sell, stock, lowStock, catId)
            }
        )
    }

    if (state.showStockDialog && state.stockDialogProduct != null) {
        StockDialog(
            product = state.stockDialogProduct!!,
            isAdd = state.stockDialogIsAdd,
            quantity = state.stockQuantity,
            note = state.stockNote,
            onQuantityChange = viewModel::setStockQuantity,
            onNoteChange = viewModel::setStockNote,
            onDismiss = viewModel::hideStockDialog,
            onConfirm = viewModel::confirmStockUpdate
        )
    }

    if (state.showPriceHistory != null) {
        PriceHistoryDialog(
            product = state.showPriceHistory!!,
            transactions = state.priceHistory,
            onDismiss = viewModel::hidePriceHistory
        )
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp),
        snackbar = { data ->
            androidx.compose.material3.Snackbar(
                action = {
                    if (showUndoSnackbar) {
                        TextButton(onClick = { viewModel.undoDelete(); scope.launch { snackbarHostState.currentSnackbarData?.dismiss() } }) {
                            Text("পূর্বাবস্থায় আনুন", color = GreenProfit)
                        }
                    }
                }
            ) {
                Text(data.visuals.message)
            }
        }
    )
}

@Composable
private fun ProductCard(
    product: ProductEntity,
    onAddStock: () -> Unit,
    onRemoveStock: () -> Unit,
    onDelete: () -> Unit,
    onShowHistory: () -> Unit
) {
    val outOfStock = product.currentStock <= 0
    val lowStock = !outOfStock && product.currentStock <= product.lowStockThreshold
    val stockColor = when {
        outOfStock -> RedExpense
        lowStock -> OrangeDue
        else -> GreenProfit
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${BanglaNumberConverter.toBangla(product.currentStock.toInt())} ${unitToBangla(product.unit)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = stockColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.format(product.sellPrice),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GreenProfit
                    )
                    Text(
                        text = "ক্রয়: ${CurrencyFormatter.format(product.buyPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onShowHistory, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.History, contentDescription = "ইতিহাস", tint = BlueInfo, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onRemoveStock, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.RemoveCircle, contentDescription = "স্টক আউট", tint = OrangeDue, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onAddStock, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.AddCircle, contentDescription = "স্টক ইন", tint = GreenProfit, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = RedExpense.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProductDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, Double, Double, Double, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameBangla by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("piece") }
    var buyPrice by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("0") }
    var lowStock by remember { mutableStateOf("10") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val units = listOf("kg", "piece", "litre", "mon", "dozen", "gram", "bag", "sack")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন পণ্য") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পণ্যের নাম (ইংরেজি)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameBangla,
                    onValueChange = { nameBangla = it },
                    label = { Text("পণ্যের নাম (বাংলা)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                    OutlinedTextField(
                        value = unitToBangla(unit),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("একক") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(unitToBangla(u)) },
                                onClick = { unit = u; unitExpanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (categories.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                        OutlinedTextField(
                            value = categories.find { it.id == selectedCategoryId }?.nameBangla ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("বিভাগ") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.nameBangla) },
                                    onClick = { selectedCategoryId = cat.id; categoryExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = buyPrice,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) buyPrice = it },
                        label = { Text("ক্রয় মূল্য") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) sellPrice = it },
                        label = { Text("বিক্রয় মূল্য") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) stock = it },
                        label = { Text("স্টক") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lowStock,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) lowStock = it },
                        label = { Text("নিম্ন সীমা") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val buy = buyPrice.toDoubleOrNull() ?: 0.0
                    val sell = sellPrice.toDoubleOrNull() ?: 0.0
                    val stk = stock.toDoubleOrNull() ?: 0.0
                    val low = lowStock.toDoubleOrNull() ?: 10.0
                    if (name.isNotBlank() && nameBangla.isNotBlank() && sell > 0) {
                        onConfirm(name, nameBangla, unit, buy, sell, stk, low, selectedCategoryId)
                    }
                },
                enabled = name.isNotBlank() && nameBangla.isNotBlank()
            ) { Text("যোগ করুন") }
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
    note: String,
    onQuantityChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isAdd) "স্টক ইন - ${product.nameBangla}" else "স্টক আউট - ${product.nameBangla}") },
        text = {
            Column {
                Text(
                    text = "বর্তমান স্টক: ${BanglaNumberConverter.toBangla(product.currentStock.toInt())} ${unitToBangla(product.unit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("পরিমাণ") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("কারণ/নোট *") },
                    placeholder = { Text("কেন স্টক পরিবর্তন করছেন?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = note.isBlank() && quantity.isNotBlank()
                )
                if (note.isBlank() && quantity.isNotBlank()) {
                    Text(
                        text = "কারণ উল্লেখ করা আবশ্যক",
                        color = RedExpense,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = quantity.toDoubleOrNull()?.let { it > 0 } == true && note.isNotBlank()
            ) {
                Text(if (isAdd) "স্টক ইন" else "স্টক আউট")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
private fun PriceHistoryDialog(
    product: ProductEntity,
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("লেনদেন ইতিহাস - ${product.nameBangla}") },
        text = {
            LazyColumn(modifier = Modifier.height(300.dp)) {
                if (transactions.isEmpty()) {
                    item {
                        Text(
                            text = "কোনো লেনদেন নেই",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(transactions) { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = when (t.type) {
                                        TransactionType.SALE -> "বিক্রয়"
                                        TransactionType.PURCHASE -> "ক্রয়"
                                        TransactionType.STOCK_LOSS -> "স্টক আউট"
                                        else -> t.type.name
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "পরিমাণ: ${BanglaNumberConverter.toBangla(t.quantity)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.format(t.totalAmount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (t.type == TransactionType.SALE) GreenProfit else OrangeDue
                                )
                                if (t.notes.isNotBlank()) {
                                    Text(
                                        text = t.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("বন্ধ") }
        }
    )
}

private fun unitToBangla(unit: String): String = when (unit.lowercase()) {
    "kg" -> "কেজি"
    "piece", "pcs" -> "পিস"
    "litre", "l", "liter" -> "লিটার"
    "mon" -> "মণ"
    "dozen" -> "ডজন"
    "gram", "g" -> "গ্রাম"
    "ton" -> "টন"
    "bag" -> "বস্তা"
    "sack" -> "বস্তা"
    else -> unit
}
