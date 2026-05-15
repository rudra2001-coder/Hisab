package com.rudra.hisab.ui.screens.sale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.PaymentStatus
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.ui.theme.BlueInfo
import com.rudra.hisab.ui.theme.BlueInfoContainer
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.GreenProfitContainer
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.ui.theme.WarningYellow
import com.rudra.hisab.ui.theme.WarningYellowContainer
import com.rudra.hisab.util.BanglaNumberConverter
import com.rudra.hisab.util.CurrencyFormatter
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
    val haptic = LocalHapticFeedback.current
    var showCartSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    text = "দ্রুত বিক্রয়",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                if (state.cartMode) {
                    IconButton(onClick = { showCartSheet = !showCartSheet }) {
                        Box {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "কার্ট",
                                tint = if (state.cartCount > 0) GreenProfit else MaterialTheme.colorScheme.onSurface
                            )
                            if (state.cartCount > 0) {
                                Text(
                                    text = "${state.cartCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(
                                            RedExpense,
                                            CircleShape
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.products.isEmpty()) "প্রথমে পণ্য যোগ করুন"
                            else "কোনো পণ্য পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (state.products.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ইনভেন্টরি থেকে পণ্য যোগ করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        if (state.cartMode) {
                            CartProductTile(
                                product = product,
                                isInCart = state.isInCart(product.id),
                                cartQty = state.getCartItem(product.id)?.quantity ?: 0.0,
                                onClick = { viewModel.selectProduct(product) }
                            )
                        } else {
                            ProductTile(
                                product = product,
                                onClick = { viewModel.selectProduct(product) }
                            )
                        }
                    }
                }
            }

            if (state.showLowStockWarning && !state.saleComplete) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
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
            }
        }

        if (state.saleComplete) {
            SaleSuccessOverlay(
                onDismiss = {
                    val amount = state.totalPrice
                    viewModel.resetAfterSale()
                    scope.launch {
                        snackbarHostState.showSnackbar("বিক্রয় সফল হয়েছে — ${CurrencyFormatter.format(amount)}")
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )

        state.errorMessage?.let { error ->
            SnackbarHost(
                hostState = remember {
                    SnackbarHostState().also {
                        scope.launch { it.showSnackbar(error) }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            )
        }
    }

    if (state.selectedProduct != null && !state.saleComplete && !state.cartMode) {
        ModalBottomSheet(
            onDismissRequest = viewModel::clearSelection,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            SaleBottomSheetContent(
                state = state,
                onDigit = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.appendDigit(it) },
                onBackspace = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.backspaceQuantity() },
                onClear = viewModel::clearQuantity,
                onPaymentTypeChange = viewModel::setPaymentType,
                onPaidAmountChange = viewModel::setPaidAmount,
                onCustomerSelect = viewModel::selectCustomer,
                onCustomerSearchChange = viewModel::setCustomerSearchQuery,
                onCreateCustomer = { name, phone -> viewModel.createAndSelectCustomer(name, phone) },
                onConfirm = viewModel::completeSale,
                onShowHistory = { viewModel.setShowHistoryTab(true) }
            )
        }
    }

    if (showCartSheet && state.cartMode) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            CartBottomSheetContent(
                state = state,
                onUpdateQuantity = viewModel::updateCartItemQuantity,
                onRemoveItem = viewModel::removeCartItem,
                onClearCart = viewModel::clearCart,
                onPaymentTypeChange = viewModel::setPaymentType,
                onPaidAmountChange = viewModel::setPaidAmount,
                onCustomerSelect = viewModel::selectCustomer,
                onCustomerSearchChange = viewModel::setCustomerSearchQuery,
                onCreateCustomer = { name, phone -> viewModel.createAndSelectCustomer(name, phone) },
                onConfirm = viewModel::completeSale,
                onDismiss = { showCartSheet = false }
            )
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
    val stockRatio = if (product.currentStock > 0 && product.lowStockThreshold > 0)
        (product.currentStock / (product.lowStockThreshold * 3)).coerceAtMost(1.0) else 0.5
    val containerColor = when {
        outOfStock -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        lowStock -> WarningYellowContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val stockColor = when {
        outOfStock -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        lowStock -> OrangeDue
        else -> GreenProfit
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        enabled = !outOfStock
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = product.nameBangla,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (outOfStock) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurface
            )
            Column {
                Text(
                    text = CurrencyFormatter.format(product.sellPrice),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (outOfStock) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    else GreenProfit
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { stockRatio.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = stockColor,
                    trackColor = stockColor.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "স্টক: ${BanglaNumberConverter.toBangla(product.currentStock.toInt())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = stockColor
                )
            }
        }
    }
}

@Composable
private fun CartProductTile(
    product: ProductEntity,
    isInCart: Boolean,
    cartQty: Double,
    onClick: () -> Unit
) {
    val outOfStock = product.currentStock <= 0
    val containerColor = when {
        isInCart -> GreenProfitContainer.copy(alpha = 0.3f)
        outOfStock -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        enabled = !outOfStock
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Column {
                Text(
                    text = product.nameBangla,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.format(product.sellPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenProfit
                )
            }
            if (isInCart) {
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .background(GreenProfit, CircleShape)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${cartQty.toInt()}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (isInCart) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = GreenProfit,
                    modifier = Modifier.align(Alignment.TopEnd).size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleBottomSheetContent(
    state: QuickSaleState,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onPaymentTypeChange: (PaymentType) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onCustomerSelect: (CustomerEntity?) -> Unit,
    onCustomerSearchChange: (String) -> Unit,
    onCreateCustomer: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onShowHistory: () -> Unit
) {
    val product = state.selectedProduct ?: return
    var tabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        TabRow(selectedTabIndex = tabIndex) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("বিক্রয়") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("ইতিহাস") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (tabIndex) {
            0 -> SaleTabContent(
                product = product,
                state = state,
                onDigit = onDigit,
                onBackspace = onBackspace,
                onClear = onClear,
                onPaymentTypeChange = onPaymentTypeChange,
                onPaidAmountChange = onPaidAmountChange,
                onCustomerSelect = onCustomerSelect,
                onCustomerSearchChange = onCustomerSearchChange,
                onCreateCustomer = onCreateCustomer,
                onConfirm = onConfirm
            )
            1 -> HistoryTabContent(
                transactions = state.todayTransactions,
                productMap = state.todayProducts,
                onDelete = { }
            )
        }
    }
}

@Composable
private fun SaleTabContent(
    product: ProductEntity,
    state: QuickSaleState,
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = product.nameBangla,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "দাম: ${CurrencyFormatter.format(product.sellPrice)} / ${unitToBangla(product.unit)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

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

    if (state.paymentType == PaymentStatus.PARTIAL) {
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
            recentCustomers = state.recentCustomers,
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
        colors = ButtonDefaults.buttonColors(containerColor = GreenProfit)
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

@Composable
private fun HistoryTabContent(
    transactions: List<TransactionEntity>,
    productMap: Map<Long, ProductEntity>,
    onDelete: (Long) -> Unit
) {
    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "আজকের কোনো বিক্রয় নেই",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(transactions, key = { it.id }) { t ->
                val pName = productMap[t.productId]?.nameBangla ?: "পণ্য"
                var showDeleteConfirm by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                                text = pName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${BanglaNumberConverter.toBangla(t.quantity)} × ${CurrencyFormatter.format(t.unitPrice)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = CurrencyFormatter.format(t.totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GreenProfit
                        )
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "মুছুন",
                                tint = RedExpense.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("বিক্রয় মুছুন") },
                        text = { Text("এই বিক্রয়টি মুছে ফেলবেন? স্টক পুনরুদ্ধার হবে।") },
                        confirmButton = {
                            TextButton(onClick = {
                                onDelete(t.id)
                                showDeleteConfirm = false
                            }) { Text("মুছুন", color = RedExpense) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) { Text("বাতিল") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartBottomSheetContent(
    state: QuickSaleState,
    onUpdateQuantity: (Long, Double) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onClearCart: () -> Unit,
    onPaymentTypeChange: (PaymentType) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onCustomerSelect: (CustomerEntity?) -> Unit,
    onCustomerSearchChange: (String) -> Unit,
    onCreateCustomer: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
                text = "কার্ট (${state.cartCount})",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Row {
                if (state.cartCount > 0) {
                    TextButton(onClick = onClearCart) {
                        Text("সব মুছুন", color = RedExpense)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কার্ট খালি। পণ্য নির্বাচন করুন।",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                items(state.cartItems, key = { it.product.id }) { item ->
                    CartItemRow(
                        item = item,
                        onUpdateQuantity = onUpdateQuantity,
                        onRemove = { onRemoveItem(item.product.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = GreenProfit.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("মোট:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        CurrencyFormatter.format(state.cartTotal),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GreenProfit
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isCustomerSelectionRequired) {
                CustomerSelector(
                    customers = state.filteredCustomers,
                    recentCustomers = state.recentCustomers,
                    selectedCustomer = state.selectedCustomer,
                    searchQuery = state.customerSearchQuery,
                    onSearchChange = onCustomerSearchChange,
                    onSelect = onCustomerSelect,
                    onCreateCustomer = onCreateCustomer
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            PaymentTypeSelector(
                selected = state.paymentType,
                onChange = onPaymentTypeChange
            )

            if (state.paymentType == PaymentStatus.PARTIAL) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.paidAmount,
                    onValueChange = onPaidAmountChange,
                    label = { Text("প্রাপ্ত টাকা") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !state.isSaving && state.cartCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GreenProfit)
            ) {
                Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("কার্ট বিক্রয় নিশ্চিত করুন (${CurrencyFormatter.format(state.cartTotal)})")
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onUpdateQuantity: (Long, Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.nameBangla,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = CurrencyFormatter.format(item.product.sellPrice) + " / " + unitToBangla(item.product.unit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (item.quantity > 1) onUpdateQuantity(item.product.id, item.quantity - 1)
                        else onRemove()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "${item.quantity.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = {
                        if (item.quantity < item.product.currentStock)
                            onUpdateQuantity(item.product.id, item.quantity + 1)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = CurrencyFormatter.format(item.totalPrice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GreenProfit
            )
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
                    val isBackspace = label == "⌫"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isBackspace -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                    label == "." -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable {
                                when {
                                    isBackspace -> onBackspace()
                                    else -> onDigit(label)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = if (isBackspace) FontWeight.Normal else FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentTypeSelector(
    selected: PaymentStatus,
    onChange: (PaymentStatus) -> Unit
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
                selected = selected == PaymentStatus.CASH,
                color = GreenProfit,
                containerColor = GreenProfitContainer,
                onClick = { onChange(PaymentStatus.CASH) },
                modifier = Modifier.weight(1f)
            )
            PaymentTypeButton(
                label = "বাকি",
                selected = selected == PaymentStatus.CREDIT,
                color = OrangeDue,
                containerColor = OrangeDue.copy(alpha = 0.15f),
                onClick = { onChange(PaymentStatus.CREDIT) },
                modifier = Modifier.weight(1f)
            )
            PaymentTypeButton(
                label = "আংশিক",
                selected = selected == PaymentStatus.PARTIAL,
                color = BlueInfo,
                containerColor = BlueInfoContainer,
                onClick = { onChange(PaymentStatus.PARTIAL) },
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
    recentCustomers: List<CustomerEntity> = emptyList(),
    selectedCustomer: CustomerEntity?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelect: (CustomerEntity?) -> Unit,
    onCreateCustomer: (String, String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }

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
                    if (searchQuery.isBlank() && recentCustomers.isNotEmpty()) {
                        item {
                            Text(
                                text = "সাম্প্রতিক গ্রাহক",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        items(recentCustomers, key = { "recent_${it.id}" }) { customer ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(customer) }
                                    .padding(vertical = 6.dp, horizontal = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = customer.name, style = MaterialTheme.typography.bodyLarge)
                                        if (customer.phone.isNotBlank()) {
                                            Text(text = customer.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                        item { HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp)) }
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
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
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
                    .size(200.dp),
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
