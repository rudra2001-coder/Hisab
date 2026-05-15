package com.rudra.hisab.ui.screens.sale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.PaymentType
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.GreenProfitContainer
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.ui.theme.WarningYellow
import com.rudra.hisab.ui.theme.WarningYellowContainer
import com.rudra.hisab.ui.theme.BlueInfo
import com.rudra.hisab.ui.theme.BlueInfoContainer
import com.rudra.hisab.util.BanglaNumberConverter
import com.rudra.hisab.util.CurrencyFormatter
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSaleScreen(
    viewModel: QuickSaleViewModel
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "দ্রুত বিক্রয়",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

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

            val filteredProducts = if (state.searchQuery.isBlank()) state.products
            else state.products.filter {
                it.name.contains(state.searchQuery, ignoreCase = true) ||
                        it.nameBangla.contains(state.searchQuery, ignoreCase = true)
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.products.isEmpty()) "প্রথমে পণ্য যোগ করুন"
                        else "কোনো পণ্য পাওয়া যায়নি",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductTile(
                            product = product,
                            onClick = { viewModel.selectProduct(product) }
                        )
                    }
                }
            }

            if (state.showLowStockWarning && !state.saleComplete) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { viewModel.dismissLowStockWarning() },
                    colors = CardDefaults.cardColors(containerColor = WarningYellowContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "কম স্টক!",
                            color = OrangeDue,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "পণ্যের স্টক কমে গেছে। দয়া করে স্টক আপডেট করুন।",
                            color = OrangeDue,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.dismissLowStockWarning() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = OrangeDue)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (state.saleComplete) {
            SaleSuccessOverlay(
                onDismiss = {
                    val amount = state.totalPrice
                    viewModel.resetAfterSale()
                    scope.launch {
                        snackbarHostState.showSnackbar("বিক্রয় সফল হয়েছে — ৳${CurrencyFormatter.format(amount)}")
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }

    if (state.selectedProduct != null && !state.saleComplete) {
        ModalBottomSheet(
            onDismissRequest = viewModel::clearSelection,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            SaleBottomSheetContent(
                state = state,
                onDismiss = viewModel::clearSelection,
                onDigit = viewModel::appendDigit,
                onBackspace = viewModel::backspaceQuantity,
                onClear = viewModel::clearQuantity,
                onPaymentTypeChange = viewModel::setPaymentType,
                onPaidAmountChange = viewModel::setPaidAmount,
                onCustomerSelect = viewModel::selectCustomer,
                onCustomerSearchChange = viewModel::setCustomerSearchQuery,
                onCreateCustomer = { name, phone -> viewModel.createAndSelectCustomer(name, phone) },
                onConfirm = viewModel::completeSale
            )
        }
    }

    state.errorMessage?.let { error ->
        androidx.compose.material3.Snackbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            containerColor = RedExpense
        ) {
            Text(text = error, color = Color.White)
        }
    }
}

@Composable
private fun ProductTile(
    product: ProductEntity,
    onClick: () -> Unit
) {
    val outOfStock = product.currentStock <= 0
    val lowStock = !outOfStock && product.currentStock <= product.lowStockThreshold
    val containerColor = when {
        outOfStock -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        lowStock -> WarningYellowContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val stockColor = when {
        outOfStock -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        lowStock -> OrangeDue
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = if (outOfStock) {} else onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        enabled = !outOfStock
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = product.nameBangla,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (outOfStock) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "৳${BanglaNumberConverter.toBangla(product.sellPrice)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (outOfStock) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                else GreenProfit
            )
            Text(
                text = "স্টক: ${BanglaNumberConverter.toBangla(product.currentStock.toInt())} ${unitToBangla(product.unit)}",
                style = MaterialTheme.typography.bodySmall,
                color = stockColor
            )
        }
    }
}

@Composable
private fun SaleBottomSheetContent(
    state: QuickSaleState,
    onDismiss: () -> Unit,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onPaymentTypeChange: (PaymentType) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onCustomerSelect: (CustomerEntity?) -> Unit,
    onCustomerSearchChange: (String) -> Unit,
    onCreateCustomer: (String, String) -> Unit,
    onConfirm: () -> Unit
) {
    val product = state.selectedProduct ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = product.nameBangla,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }

        Text(
            text = "দাম: ${CurrencyFormatter.format(product.sellPrice)} / ${unitToBangla(product.unit)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        QuantityDisplay(
            quantity = state.quantity,
            unit = product.unit,
            totalPrice = state.totalPrice,
            profit = state.profit
        )

        if (state.quantityDouble > product.currentStock) {
            Text(
                text = "সর্বোচ্চ ${product.currentStock.toInt()} ${unitToBangla(product.unit)} বিক্রি করতে পারবেন",
                color = RedExpense,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        NumberPad(
            onDigit = onDigit,
            onBackspace = onBackspace,
            onClear = onClear
        )

        Spacer(modifier = Modifier.height(16.dp))

        PaymentTypeSelector(
            selected = state.paymentType,
            onChange = onPaymentTypeChange
        )

        if (state.paymentType == PaymentType.PARTIAL) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.paidAmount,
                onValueChange = onPaidAmountChange,
                label = { Text("প্রাপ্ত টাকা") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall
            )
        }

        if (state.isCustomerSelectionRequired) {
            Spacer(modifier = Modifier.height(12.dp))
            CustomerSelector(
                customers = state.filteredCustomers,
                selectedCustomer = state.selectedCustomer,
                searchQuery = state.customerSearchQuery,
                onSearchChange = onCustomerSearchChange,
                onSelect = onCustomerSelect,
                onCreateCustomer = onCreateCustomer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !state.isSaving && state.quantityDouble > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenProfit
            )
        ) {
            if (state.isSaving) {
                Text("হিসাব করা হচ্ছে...", style = MaterialTheme.typography.titleMedium)
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("বিক্রয় নিশ্চিত করুন", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun QuantityDisplay(
    quantity: String,
    unit: String,
    totalPrice: Double,
    profit: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = quantity,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unitToBangla(unit),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "মোট: ${CurrencyFormatter.format(totalPrice)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GreenProfit
                )
                Text(
                    text = "লাভ: ${CurrencyFormatter.format(profit)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (profit >= 0) GreenProfit else RedExpense
                )
            }
        }
    }
}

@Composable
private fun NumberPad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (label) {
                                    "⌫" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                    "." -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable {
                                when (label) {
                                    "⌫" -> onBackspace()
                                    else -> onDigit(label)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = if (label == "⌫") FontWeight.Normal else FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentTypeSelector(
    selected: PaymentType,
    onChange: (PaymentType) -> Unit
) {
    Column {
        Text(
            text = "পরিশোধের ধরন",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentTypeButton(
                label = "নগদ",
                selected = selected == PaymentType.CASH,
                color = GreenProfit,
                containerColor = GreenProfitContainer,
                onClick = { onChange(PaymentType.CASH) },
                modifier = Modifier.weight(1f)
            )
            PaymentTypeButton(
                label = "বাকি",
                selected = selected == PaymentType.CREDIT,
                color = OrangeDue,
                containerColor = OrangeDue.copy(alpha = 0.15f),
                onClick = { onChange(PaymentType.CREDIT) },
                modifier = Modifier.weight(1f)
            )
            PaymentTypeButton(
                label = "আংশিক",
                selected = selected == PaymentType.PARTIAL,
                color = BlueInfo,
                containerColor = BlueInfoContainer,
                onClick = { onChange(PaymentType.PARTIAL) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PaymentTypeButton(
    label: String,
    selected: Boolean,
    color: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) color else containerColor,
            contentColor = if (selected) Color.White else color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun CustomerSelector(
    customers: List<CustomerEntity>,
    selectedCustomer: CustomerEntity?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelect: (CustomerEntity?) -> Unit,
    onCreateCustomer: (String, String) -> Unit
) {
    var showCreateDialog by androidx.compose.runtime.remember { mutableStateOf(false) }
    var customerName by androidx.compose.runtime.remember { mutableStateOf("") }
    var customerPhone by androidx.compose.runtime.remember { mutableStateOf("") }

    Column {
        Text(
            text = "গ্রাহক",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (selectedCustomer != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedCustomer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (selectedCustomer.phone.isNotBlank()) {
                        Text(
                            text = selectedCustomer.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { onSelect(null) }) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = RedExpense)
                }
            }
        } else {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("গ্রাহক খুঁজুন") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            val displayCustomers = if (searchQuery.isBlank()) customers.take(5) else customers

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                LazyColumn {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    customerName = ""
                                    customerPhone = ""
                                    showCreateDialog = true
                                }
                                .padding(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "নতুন গ্রাহক",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    items(displayCustomers, key = { it.id }) { customer ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(customer) }
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Column {
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                if (customer.phone.isNotBlank()) {
                                    Text(
                                        text = customer.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (customer != displayCustomers.last()) {
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("নতুন গ্রাহক") },
            text = {
                Column {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("ফোন নম্বর") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customerName.isNotBlank()) {
                            onCreateCustomer(customerName, customerPhone)
                            showCreateDialog = false
                        }
                    },
                    enabled = customerName.isNotBlank()
                ) { Text("যোগ করুন") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
private fun SaleSuccessOverlay(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(300)
        onDismiss()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Card(
                modifier = Modifier
                    .size(200.dp)
                    .clickable(onClick = onDismiss),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(GreenProfit)
                            .padding(12.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "বিক্রয় সফল!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenProfit
                    )
                }
            }
        }
    }
}

private fun unitToBangla(unit: String): String = when (unit.lowercase()) {
    "kg" -> "কেজি"
    "piece", "pcs" -> "পিস"
    "litre", "l", "liter" -> "লিটার"
    "mon" -> "মণ"
    "dozen" -> "ডজন"
    "gram", "g" -> "গ্রাম"
    "ton" -> "টন"
    else -> unit
}
