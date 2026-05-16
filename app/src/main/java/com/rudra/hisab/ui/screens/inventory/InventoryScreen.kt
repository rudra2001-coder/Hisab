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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
    val isBangla = state.isBangla

    val deletedProduct = state.deletedProduct
    LaunchedEffect(deletedProduct) {
        if (deletedProduct != null) {
            showUndoSnackbar = true
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = if (isBangla) "${deletedProduct.nameBangla} মুছে ফেলা হয়েছে"
                    else "${deletedProduct.nameBangla} deleted",
                    actionLabel = if (isBangla) "পূর্বাবস্থায় আনুন" else "Undo"
                )
                showUndoSnackbar = false
            }
        }
    }

    val filteredProducts by remember(state.products, state.searchQuery, state.selectedCategoryId) {
        derivedStateOf {
            viewModel.getFilteredProducts()
        }
    }

    val totalStockValue by remember(state.products) {
        derivedStateOf { state.products.sumOf { it.currentStock * it.buyPrice } }
    }

    val lowStockCount by remember(state.products) {
        derivedStateOf { state.products.count { it.currentStock <= it.lowStockThreshold } }
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
                text = if (isBangla) "মজুদ" else "Inventory",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = if (isBangla) "পণ্য যোগ করুন" else "Add Product")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder = { Text(if (isBangla) "পণ্য খুঁজুন" else "Search products") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
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
                    label = { Text(if (isBangla) "সব" else "All") }
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = if (isBangla) "পণ্য" else "Items",
                value = "${state.products.size}",
                color = BlueInfo,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = if (isBangla) "স্টক মূল্য" else "Stock Value",
                value = CurrencyFormatter.format(totalStockValue),
                color = GreenProfit,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = if (isBangla) "কম স্টক" else "Low Stock",
                value = "$lowStockCount",
                color = if (lowStockCount > 0) OrangeDue else GreenProfit,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.products.isEmpty())
                            (if (isBangla) "কোনো পণ্য নেই" else "No products")
                        else
                            (if (isBangla) "কোনো পণ্য পাওয়া যায়নি" else "No products found"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (state.products.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.showAddDialog() }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBangla) "পণ্য যোগ করুন" else "Add Product")
                        }
                    }
                }
            }
        } else {
            LazyColumn {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isBangla = isBangla,
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
        val product = state.showDeleteConfirm!!
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text(if (isBangla) "পণ্য মুছুন" else "Delete Product") },
            text = {
                Text(
                    if (isBangla) "${product.nameBangla} মুছে ফেলবেন? এই পণ্যের সকল তথ্য হারিয়ে যাবে。"
                    else "Delete ${product.nameBangla}? All product data will be lost."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text(if (isBangla) "মুছুন" else "Delete", color = RedExpense)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    if (state.showAddDialog) {
        AddProductDialog(
            categories = state.categories,
            isBangla = isBangla,
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
            isBangla = isBangla,
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
            isBangla = isBangla,
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
                        TextButton(onClick = {
                            viewModel.undoDelete()
                            scope.launch { snackbarHostState.currentSnackbarData?.dismiss() }
                        }) {
                            Text(
                                if (isBangla) "পূর্বাবস্থায় আনুন" else "Undo",
                                color = GreenProfit
                            )
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
private fun StatCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductEntity,
    isBangla: Boolean,
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
            .padding(vertical = 4.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                        text = "${BanglaNumberConverter.toBangla(product.currentStock.toInt())} ${unitToText(product.unit, isBangla)}",
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
                        text = "${if (isBangla) "ক্রয়" else "Buy"}: ${CurrencyFormatter.format(product.buyPrice)}",
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
                    Icon(
                        Icons.Default.History,
                        contentDescription = if (isBangla) "ইতিহাস" else "History",
                        tint = BlueInfo,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onRemoveStock, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.RemoveCircle,
                        contentDescription = if (isBangla) "স্টক আউট" else "Stock Out",
                        tint = OrangeDue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onAddStock, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.AddCircle,
                        contentDescription = if (isBangla) "স্টক ইন" else "Stock In",
                        tint = GreenProfit,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = if (isBangla) "মুছুন" else "Delete",
                        tint = RedExpense.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProductDialog(
    categories: List<CategoryEntity>,
    isBangla: Boolean,
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
        title = { Text(if (isBangla) "নতুন পণ্য" else "New Product") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "পণ্যের নাম (ইংরেজি)" else "Product Name (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameBangla,
                    onValueChange = { nameBangla = it },
                    label = { Text(if (isBangla) "পণ্যের নাম (বাংলা)" else "Product Name (Bengali)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                    OutlinedTextField(
                        value = unitToText(unit, isBangla),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "একক" else "Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(unitToText(u, isBangla)) },
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
                            label = { Text(if (isBangla) "বিভাগ" else "Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
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
                        label = { Text(if (isBangla) "ক্রয় মূল্য" else "Buy Price") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) sellPrice = it },
                        label = { Text(if (isBangla) "বিক্রয় মূল্য" else "Sell Price") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) stock = it },
                        label = { Text(if (isBangla) "স্টক" else "Stock") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = lowStock,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) lowStock = it },
                        label = { Text(if (isBangla) "নিম্ন সীমা" else "Min Stock") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
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
            ) { Text(if (isBangla) "যোগ করুন" else "Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isBangla) "বাতিল" else "Cancel") }
        }
    )
}

@Composable
private fun StockDialog(
    product: ProductEntity,
    isAdd: Boolean,
    isBangla: Boolean,
    quantity: String,
    note: String,
    onQuantityChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isAdd) {
                    if (isBangla) "স্টক ইন - ${product.nameBangla}" else "Stock In - ${product.nameBangla}"
                } else {
                    if (isBangla) "স্টক আউট - ${product.nameBangla}" else "Stock Out - ${product.nameBangla}"
                }
            )
        },
        text = {
            Column {
                Text(
                    text = "${if (isBangla) "বর্তমান স্টক" else "Current Stock"}: ${BanglaNumberConverter.toBangla(product.currentStock.toInt())} ${unitToText(product.unit, isBangla)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    label = { Text(if (isBangla) "পরিমাণ" else "Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text(if (isBangla) "কারণ/নোট" else "Reason/Note") },
                    placeholder = {
                        Text(
                            if (isBangla) "কেন স্টক পরিবর্তন করছেন?" else "Why are you changing stock?"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = note.isBlank() && quantity.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                )
                if (note.isBlank() && quantity.isNotBlank()) {
                    Text(
                        text = if (isBangla) "কারণ উল্লেখ করা আবশ্যক" else "Reason is required",
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
                Text(
                    if (isAdd) {
                        if (isBangla) "স্টক ইন" else "Stock In"
                    } else {
                        if (isBangla) "স্টক আউট" else "Stock Out"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isBangla) "বাতিল" else "Cancel") }
        }
    )
}

@Composable
private fun PriceHistoryDialog(
    product: ProductEntity,
    isBangla: Boolean,
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isBangla) "লেনদেন ইতিহাস - ${product.nameBangla}"
                else "Transaction History - ${product.nameBangla}"
            )
        },
        text = {
            LazyColumn(modifier = Modifier.height(300.dp)) {
                if (transactions.isEmpty()) {
                    item {
                        Text(
                            text = if (isBangla) "কোনো লেনদেন নেই" else "No transactions",
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
                                        TransactionType.SALE -> if (isBangla) "বিক্রয়" else "Sale"
                                        TransactionType.PURCHASE -> if (isBangla) "ক্রয়" else "Purchase"
                                        TransactionType.STOCK_LOSS -> if (isBangla) "স্টক আউট" else "Stock Out"
                                        else -> t.type.name
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${if (isBangla) "পরিমাণ" else "Qty"}: ${BanglaNumberConverter.toBangla(t.quantity)}",
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
            TextButton(onClick = onDismiss) { Text(if (isBangla) "বন্ধ" else "Close") }
        }
    )
}

private fun unitToText(unit: String, isBangla: Boolean): String {
    if (!isBangla) {
        return when (unit.lowercase()) {
            "kg" -> "kg"
            "piece", "pcs" -> "pcs"
            "litre", "l", "liter" -> "L"
            "mon" -> "mon"
            "dozen" -> "dozen"
            "gram", "g" -> "g"
            "ton" -> "ton"
            "bag" -> "bag"
            "sack" -> "sack"
            else -> unit
        }
    }
    return when (unit.lowercase()) {
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
}