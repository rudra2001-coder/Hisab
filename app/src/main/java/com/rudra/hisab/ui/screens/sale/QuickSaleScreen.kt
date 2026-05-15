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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.SalePaymentType
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.TransactionEntity
import com.rudra.hisab.ui.theme.BlueInfo
import com.rudra.hisab.ui.theme.BlueInfoContainer
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.GreenProfitContainer
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
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
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
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
    }

    if (state.selectedProduct != null && !state.saleComplete && !state.cartMode) {
        ModalBottomSheet(
            onDismissRequest = viewModel::clearSelection,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.imePadding()
        ) {
            SaleBottomSheetContent(
                state = state,
                onQuantityChange = viewModel::setQuantityDirect,
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
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.imePadding()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleBottomSheetContent(
    state: QuickSaleState,
    onQuantityChange: (String) -> Unit,
    onPaymentTypeChange: (SalePaymentType) -> Unit,
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
            .verticalScroll(rememberScrollState())
    ) {
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = Color.Transparent,
            contentColor = GreenProfit,
            indicator = { tabPositions ->
                if (tabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                        color = GreenProfit
                    )
                }
            },
            divider = {}
        ) {
            Tab(
                selected = tabIndex == 0,
                onClick = { tabIndex = 0 },
                text = { Text("বিক্রয়", fontWeight = if (tabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = tabIndex == 1,
                onClick = { tabIndex = 1 },
                text = { Text("ইতিহাস", fontWeight = if (tabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (tabIndex) {
            0 -> SaleTabContent(
                product = product,
                state = state,
                onQuantityChange = onQuantityChange,
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
    onQuantityChange: (String) -> Unit,
    onPaymentTypeChange: (SalePaymentType) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onCustomerSelect: (CustomerEntity?) -> Unit,
    onCustomerSearchChange: (String) -> Unit,
    onCreateCustomer: (String, String) -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.nameBangla,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "দাম: ${CurrencyFormatter.format(product.sellPrice)} / ${unitToBangla(product.unit)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    InteractiveQuantityCard(
        quantity = state.quantity,
        onQuantityChange = onQuantityChange,
        unit = product.unit,
        totalPrice = state.totalPrice,
        profit = state.profit
    )

    if (state.quantityDouble > product.currentStock) {
        Text(
            text = "সর্বোচ্চ ${product.currentStock.toInt()} ${unitToBangla(product.unit)} বিক্রি করতে পারবেন",
            color = RedExpense,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    PaymentTypeSelector(
        selected = state.paymentType,
        onChange = onPaymentTypeChange
    )

    if (state.paymentType == SalePaymentType.PARTIAL) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.paidAmount,
            onValueChange = onPaidAmountChange,
            label = { Text("প্রাপ্ত টাকা") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }

    if (state.isCustomerSelectionRequired) {
        Spacer(modifier = Modifier.height(16.dp))
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

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onConfirm,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        enabled = !state.isSaving && state.quantityDouble > 0,
        colors = ButtonDefaults.buttonColors(containerColor = GreenProfit),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        if (state.isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("হিসাব করা হচ্ছে...", style = MaterialTheme.typography.titleMedium)
        } else {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("বিক্রয় নিশ্চিত করুন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
        (product.currentStock / (product.lowStockThreshold * 3.0)).coerceAtMost(1.0) else 0.5
    val containerColor = when {
        outOfStock -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        lowStock -> RedExpense.copy(alpha = 0.05f)
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
        enabled = !outOfStock,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = product.nameBangla,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Column {
                Text(
                    text = CurrencyFormatter.format(product.sellPrice),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GreenProfit
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { stockRatio.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = stockColor,
                    trackColor = stockColor.copy(alpha = 0.2f),
                )
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
    val containerColor = if (isInCart) GreenProfitContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(120.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        enabled = !outOfStock,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column {
                Text(product.nameBangla, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(CurrencyFormatter.format(product.sellPrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GreenProfit)
            }
            if (isInCart) {
                Box(modifier = Modifier.align(Alignment.BottomEnd).background(GreenProfit, CircleShape).padding(6.dp)) {
                    Text("${cartQty.toInt()}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
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
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("আজকের কোনো বিক্রয় নেই", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            transactions.forEach { t ->
                val pName = productMap[t.productId]?.nameBangla ?: "পণ্য"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(pName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${BanglaNumberConverter.toBangla(t.quantity)} পিস", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(CurrencyFormatter.format(t.totalAmount), fontWeight = FontWeight.Bold, color = GreenProfit)
                    }
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
    onPaymentTypeChange: (SalePaymentType) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onCustomerSelect: (CustomerEntity?) -> Unit,
    onCustomerSearchChange: (String) -> Unit,
    onCreateCustomer: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("কার্ট (${state.cartCount})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
        }
        state.cartItems.forEach { item ->
            CartItemRow(item, onUpdateQuantity, { onRemoveItem(item.product.id) })
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = GreenProfit.copy(alpha = 0.1f))) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("মোট:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(CurrencyFormatter.format(state.cartTotal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GreenProfit)
            }
        }
        Spacer(Modifier.height(16.dp))
        PaymentTypeSelector(state.paymentType, onPaymentTypeChange)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = GreenProfit)) {
            Text("বিক্রয় নিশ্চিত করুন")
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, onUpdateQuantity: (Long, Double) -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.product.nameBangla, fontWeight = FontWeight.Bold)
                Text(CurrencyFormatter.format(item.product.sellPrice))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onUpdateQuantity(item.product.id, item.quantity - 1) }) { Icon(Icons.Default.Remove, null) }
                Text("${item.quantity.toInt()}")
                IconButton(onClick = { onUpdateQuantity(item.product.id, item.quantity + 1) }) { Icon(Icons.Default.Add, null) }
            }
        }
    }
}

@Composable
private fun InteractiveQuantityCard(
    quantity: String,
    onQuantityChange: (String) -> Unit,
    unit: String,
    totalPrice: Double,
    profit: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.text.BasicTextField(
                    value = quantity,
                    onValueChange = { if (it.length <= 6) onQuantityChange(it) },
                    textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(GreenProfit),
                    modifier = Modifier.width(IntrinsicSize.Min).padding(horizontal = 8.dp)
                )
                Text(unitToBangla(unit), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("মোট টাকা", style = MaterialTheme.typography.labelMedium)
                    Text(CurrencyFormatter.format(totalPrice), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GreenProfit)
                }
                VerticalDivider(Modifier.height(40.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("সম্ভাব্য লাভ", style = MaterialTheme.typography.labelMedium)
                    Text(CurrencyFormatter.format(profit), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (profit >= 0) GreenProfit else RedExpense)
                }
            }
        }
    }
}

@Composable
private fun PaymentTypeSelector(selected: SalePaymentType, onChange: (SalePaymentType) -> Unit) {
    Column {
        Text("পরিশোধের ধরন", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentTypeButton("নগদ", selected == SalePaymentType.CASH, GreenProfit, GreenProfitContainer, { onChange(SalePaymentType.CASH) }, Modifier.weight(1f))
            PaymentTypeButton("বাকি", selected == SalePaymentType.CREDIT, OrangeDue, OrangeDue.copy(alpha = 0.1f), { onChange(SalePaymentType.CREDIT) }, Modifier.weight(1f))
            PaymentTypeButton("আংশিক", selected == SalePaymentType.PARTIAL, BlueInfo, BlueInfoContainer, { onChange(SalePaymentType.PARTIAL) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PaymentTypeButton(label: String, selected: Boolean, color: Color, containerColor: Color, onClick: () -> Unit, modifier: Modifier) {
    Button(onClick = onClick, modifier = modifier.height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = if (selected) color else containerColor, contentColor = if (selected) Color.White else color)) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun CustomerSelector(
    customers: List<CustomerEntity>,
    recentCustomers: List<CustomerEntity>,
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
        Text("গ্রাহক", fontWeight = FontWeight.Bold)
        if (selectedCustomer != null) {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedCustomer.name, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onSelect(null) }) { Icon(Icons.Default.Close, null, tint = RedExpense) }
                }
            }
        } else {
            OutlinedTextField(value = searchQuery, onValueChange = onSearchChange, placeholder = { Text("গ্রাহক খুঁজুন") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    TextButton(onClick = { showCreateDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null)
                        Text("নতুন গ্রাহক")
                    }
                    customers.take(3).forEach { c ->
                        HorizontalDivider(thickness = 0.5.dp)
                        Text(c.name, modifier = Modifier.fillMaxWidth().clickable { onSelect(c) }.padding(12.dp))
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("নাম") }, singleLine = true)
                    OutlinedTextField(value = customerPhone, onValueChange = { customerPhone = it }, label = { Text("ফোন নম্বর") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                }
            },
            confirmButton = { Button(onClick = { onCreateCustomer(customerName, customerPhone); showCreateDialog = false }) { Text("যোগ করুন") } },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("বাতিল") } }
        )
    }
}

@Composable
private fun SaleSuccessOverlay(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) { delay(1500); onDismiss() }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.size(200.dp), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.CheckCircle, null, tint = GreenProfit, modifier = Modifier.size(64.dp))
                Text("বিক্রয় সফল!", style = MaterialTheme.typography.titleLarge, color = GreenProfit, fontWeight = FontWeight.Bold)
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
