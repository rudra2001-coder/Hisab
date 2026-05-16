package com.rudra.hisab.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.SalePaymentType
import com.rudra.hisab.ui.theme.BlueInfo
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSale: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToAddStock: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val isBangla = state.isBangla
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        floatingActionButton = {
            DashboardFabMenu(
                expanded = state.showFabMenu,
                isBangla = isBangla,
                onToggle = viewModel::toggleFabMenu,
                onSale = viewModel::showQuickSale,
                onExpense = viewModel::showQuickExpense,
                onStock = viewModel::showQuickStock,
                onPayment = viewModel::showQuickPayment
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val actionMap = remember {
                mapOf(
                    "sale" to ActionDef(
                        if (isBangla) "বিক্রয়" else "Sale",
                        Icons.Default.AttachMoney, GreenProfit, onNavigateToSale
                    ),
                    "stock" to ActionDef(
                        if (isBangla) "স্টক" else "Stock",
                        Icons.Default.Inventory2, BlueInfo, onNavigateToInventory
                    ),
                    "expense" to ActionDef(
                        if (isBangla) "খরচ" else "Expense",
                        Icons.Default.MoneyOff, RedExpense, onNavigateToExpenses
                    ),
                    "customer" to ActionDef(
                        if (isBangla) "গ্রাহক" else "Customer",
                        Icons.Default.People, OrangeDue, onNavigateToCustomers
                    ),
                    "purchase" to ActionDef(
                        if (isBangla) "ক্রয়" else "Purchase",
                        Icons.Default.ShoppingCart, primaryColor, onNavigateToInventory
                    )
                )
            }

            val enabledActions by remember(state.quickActions, isBangla) {
                derivedStateOf { state.quickActions.mapNotNull { actionMap[it] } }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = state.shopName.ifEmpty { "Hisab" },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = state.dateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    repeat(4) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) { }
                    }
                } else {
                    // ── 2x2 Primary Metric Grid ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = if (isBangla) "আজকের বিক্রয়" else "Today's Sales",
                            amount = state.todaySales,
                            icon = Icons.Default.TrendingUp,
                            color = GreenProfit,
                            onClick = onNavigateToSale
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = if (isBangla) "নিট মুনাফা" else "Net Profit",
                            amount = state.netProfit,
                            icon = Icons.Default.AttachMoney,
                            color = if (state.netProfit >= 0) GreenProfit else RedExpense,
                            onClick = { }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = if (isBangla) "আজকের খরচ" else "Today's Expenses",
                            amount = state.todayExpenses,
                            icon = Icons.Default.MoneyOff,
                            color = RedExpense,
                            onClick = onNavigateToExpenses
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = if (isBangla) "মোট বাকি" else "Total Dues",
                            amount = state.totalDues,
                            icon = Icons.Default.People,
                            color = OrangeDue,
                            onClick = onNavigateToCustomers
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Mini Stats Horizontal Row ──
                    Text(
                        text = if (isBangla) "দ্রুত পরিসংখ্যান" else "Quick Stats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniStatChip(
                            label = if (isBangla) "বিক্রয় সংখ্যা" else "Sale Count",
                            value = "${state.todaySaleCount}",
                            color = GreenProfit
                        )
                        MiniStatChip(
                            label = if (isBangla) "ক্রয় (টাকা)" else "Purchases",
                            value = CurrencyFormatter.format(state.todayPurchases),
                            color = BlueInfo
                        )
                        MiniStatChip(
                            label = if (isBangla) "ক্রেডিট" else "Credit Given",
                            value = CurrencyFormatter.format(state.todayCreditGiven),
                            color = OrangeDue
                        )
                        MiniStatChip(
                            label = if (isBangla) "পণ্য" else "Products",
                            value = "${state.totalProductCount}",
                            color = primaryColor
                        )
                        MiniStatChip(
                            label = if (isBangla) "গ্রাহক" else "Customers",
                            value = "${state.totalCustomerCount}",
                            color = OrangeDue
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Stock Overview ──
                    Text(
                        text = if (isBangla) "স্টক ওভারভিউ" else "Stock Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StockOverviewCard(
                        totalProducts = state.totalProductCount,
                        totalStockValue = state.totalStockValue,
                        lowStockCount = state.lowStockCount,
                        isBangla = isBangla,
                        onClick = { onNavigateToAddStock?.invoke() }
                    )

                    // ── Low Stock Alert ──
                    AnimatedVisibility(
                        visible = state.lowStockCount > 0,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            LowStockAlertCard(
                                lowStockCount = state.lowStockCount,
                                isBangla = isBangla,
                                onClick = { onNavigateToAddStock?.invoke() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isBangla) "দ্রুত অ্যাকশন" else "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                enabledActions.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { action ->
                            ModernActionButton(
                                label = action.label,
                                icon = action.icon,
                                color = action.color,
                                onClick = action.onClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialogs
    if (state.showQuickSaleDialog) {
        QuickSaleDialog(state = state, viewModel = viewModel, isBangla = isBangla)
    }
    if (state.showQuickExpenseDialog) {
        QuickExpenseDialog(state = state, viewModel = viewModel, isBangla = isBangla)
    }
    if (state.showQuickStockDialog) {
        QuickStockDialog(state = state, viewModel = viewModel, isBangla = isBangla)
    }
    if (state.showQuickPaymentDialog) {
        QuickPaymentDialog(state = state, viewModel = viewModel, isBangla = isBangla)
    }
}

// ─── FAB Speed Dial ───────────────────────────────────────────────

@Composable
private fun DashboardFabMenu(
    expanded: Boolean,
    isBangla: Boolean,
    onToggle: () -> Unit,
    onSale: () -> Unit,
    onExpense: () -> Unit,
    onStock: () -> Unit,
    onPayment: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FabActionItem(icon = Icons.Default.Sell, label = if (isBangla) "বিক্রয়" else "Sale", color = GreenProfit, onClick = onSale)
                FabActionItem(icon = Icons.Default.MoneyOff, label = if (isBangla) "খরচ" else "Expense", color = RedExpense, onClick = onExpense)
                FabActionItem(icon = Icons.Default.Inventory2, label = if (isBangla) "স্টক" else "Stock", color = BlueInfo, onClick = onStock)
                FabActionItem(icon = Icons.Default.Payment, label = if (isBangla) "পেমেন্ট" else "Payment", color = OrangeDue, onClick = onPayment)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FloatingActionButton(onClick = onToggle, containerColor = MaterialTheme.colorScheme.primary) {
            Icon(if (expanded) Icons.Default.Close else Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun FabActionItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(end = 8.dp))
        SmallFloatingActionButton(onClick = onClick, containerColor = color) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
    }
}

// ─── Metric Card (for 2x2 grid) ────────────────────────────────────

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .background(color)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }
}

// ─── Mini Stat Chip (horizontal scrollable) ───────────────────────

@Composable
private fun MiniStatChip(
    label: String,
    value: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

// ─── Stock Overview Card ──────────────────────────────────────────

@Composable
private fun StockOverviewCard(
    totalProducts: Int,
    totalStockValue: Double,
    lowStockCount: Int,
    isBangla: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = BlueInfo.copy(alpha = 0.12f))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = BlueInfo, modifier = Modifier.size(22.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isBangla) "ইনভেন্টরি সারসংক্ষেপ" else "Inventory Summary",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StockStatItem(
                    label = if (isBangla) "মোট পণ্য" else "Total Products",
                    value = "$totalProducts",
                    color = BlueInfo
                )
                StockStatItem(
                    label = if (isBangla) "স্টক মূল্য" else "Stock Value",
                    value = CurrencyFormatter.format(totalStockValue),
                    color = GreenProfit
                )
                StockStatItem(
                    label = if (isBangla) "কম স্টক" else "Low Stock",
                    value = "$lowStockCount",
                    color = if (lowStockCount > 0) RedExpense else GreenProfit
                )
            }

            if (lowStockCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val health = when {
                    lowStockCount > 10 -> 0.25f
                    lowStockCount > 5 -> 0.50f
                    lowStockCount > 2 -> 0.75f
                    else -> 1.0f
                }
                val healthColor = when {
                    lowStockCount > 10 -> RedExpense
                    lowStockCount > 5 -> OrangeDue
                    else -> OrangeDue
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBangla) "স্টক স্বাস্থ্য" else "Stock Health",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(healthColor.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(health.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(3.dp))
                                .background(healthColor)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBangla) "স্টক স্বাস্থ্য" else "Stock Health",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(GreenProfit.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(GreenProfit)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBangla) "ভাল" else "Good",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = GreenProfit
                    )
                }
            }
        }
    }
}

@Composable
private fun StockStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Quick Sale Dialog ────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickSaleDialog(state: DashboardState, viewModel: DashboardViewModel, isBangla: Boolean) {
    AlertDialog(
        onDismissRequest = viewModel::hideQuickSale,
        title = { Text(if (isBangla) "দ্রুত বিক্রয়" else "Quick Sale") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.quickSelectedProduct == null) {
                    OutlinedTextField(
                        value = state.quickSearchQuery,
                        onValueChange = viewModel::quickSetSearchQuery,
                        placeholder = { Text(if (isBangla) "পণ্য খুঁজুন..." else "Search products...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    val filtered = if (state.quickSearchQuery.isBlank()) state.allProducts.take(8)
                    else state.allProducts.filter {
                        it.name.contains(state.quickSearchQuery, ignoreCase = true) ||
                        it.nameBangla.contains(state.quickSearchQuery, ignoreCase = true)
                    }.take(8)
                    if (filtered.isNotEmpty()) {
                        FlowRow(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            filtered.forEach { product ->
                                FilterChip(
                                    selected = false,
                                    onClick = { viewModel.quickSelectProduct(product) },
                                    label = { Text(if (isBangla) product.nameBangla else product.name, style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(if (isBangla) state.quickSelectedProduct.nameBangla else state.quickSelectedProduct.name, fontWeight = FontWeight.Bold)
                            Text("${CurrencyFormatter.format(state.quickSelectedProduct.sellPrice)} / ${state.quickSelectedProduct.unit}", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = viewModel::quickClearProduct) { Text(if (isBangla) "পরিবর্তন" else "Change") }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.quickQuantity,
                        onValueChange = viewModel::quickSetQuantity,
                        label = { Text(if (isBangla) "পরিমাণ" else "Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    val total = (state.quickQuantity.toDoubleOrNull() ?: 0.0) * (state.quickSelectedProduct.sellPrice)
                    Text("${if (isBangla) "মোট" else "Total"}: ${CurrencyFormatter.format(total)}", fontWeight = FontWeight.Bold, color = GreenProfit)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isBangla) "পরিশোধের ধরন" else "Payment Type", style = MaterialTheme.typography.bodySmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SalePaymentType.entries.forEach { type ->
                            FilterChip(
                                selected = state.quickPaymentType == type,
                                onClick = { viewModel.quickSetPaymentType(type) },
                                label = { Text(
                                    when (type) {
                                        SalePaymentType.CASH -> if (isBangla) "নগদ" else "Cash"
                                        SalePaymentType.CREDIT -> if (isBangla) "বাকি" else "Credit"
                                        SalePaymentType.PARTIAL -> if (isBangla) "আংশিক" else "Partial"
                                    }, style = MaterialTheme.typography.bodySmall
                                ) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenProfit.copy(alpha = 0.2f))
                            )
                        }
                    }

                    if (state.quickPaymentType == SalePaymentType.PARTIAL) {
                        OutlinedTextField(
                            value = state.quickAmount,
                            onValueChange = viewModel::quickSetAmount,
                            label = { Text(if (isBangla) "পরিশোধিত পরিমাণ" else "Paid Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                    }

                    if (state.quickSaleComplete) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(if (isBangla) "বিক্রয় সফল!" else "Sale successful!", color = GreenProfit, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (state.quickSelectedProduct != null && !state.quickSaleComplete) {
                Button(
                    onClick = viewModel::quickCompleteSale,
                    enabled = !state.quickIsSaving && (state.quickQuantity.toDoubleOrNull() ?: 0.0) > 0
                ) { Text(if (state.quickIsSaving) (if (isBangla) "হিসাব করা হচ্ছে..." else "Processing...") else (if (isBangla) "নিশ্চিত" else "Confirm")) }
            }
        },
        dismissButton = { TextButton(onClick = viewModel::hideQuickSale) { Text(if (isBangla) "বন্ধ" else "Close") } }
    )
}

// ─── Quick Expense Dialog ─────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickExpenseDialog(state: DashboardState, viewModel: DashboardViewModel, isBangla: Boolean) {
    AlertDialog(
        onDismissRequest = viewModel::hideQuickExpense,
        title = { Text(if (isBangla) "দ্রুত খরচ" else "Quick Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.quickAmount,
                    onValueChange = viewModel::quickSetAmount,
                    label = { Text(if (isBangla) "পরিমাণ" else "Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = state.quickDescription,
                    onValueChange = viewModel::quickSetDescription,
                    label = { Text(if (isBangla) "বিবরণ" else "Description") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Text(if (isBangla) "ক্যাটাগরি" else "Category", style = MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExpenseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = state.quickExpenseCategory == cat,
                            onClick = { viewModel.quickSetExpenseCategory(cat) },
                            label = { Text(cat.name, style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedExpense.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = viewModel::quickAddExpense,
                enabled = !state.quickIsSaving && (state.quickAmount.toDoubleOrNull() ?: 0.0) > 0
            ) { Text(if (state.quickIsSaving) (if (isBangla) "সংরক্ষণ..." else "Saving...") else (if (isBangla) "সংরক্ষণ" else "Save")) }
        },
        dismissButton = { TextButton(onClick = viewModel::hideQuickExpense) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

// ─── Quick Stock Dialog ───────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickStockDialog(state: DashboardState, viewModel: DashboardViewModel, isBangla: Boolean) {
    AlertDialog(
        onDismissRequest = viewModel::hideQuickStock,
        title = { Text(if (isBangla) "দ্রুত স্টক" else "Quick Stock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = state.quickStockIsAdd, onClick = { viewModel.quickSetStockIsAdd(true) }, label = { Text(if (isBangla) "স্টক ইন" else "Stock In") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenProfit.copy(alpha = 0.2f)))
                    FilterChip(selected = !state.quickStockIsAdd, onClick = { viewModel.quickSetStockIsAdd(false) }, label = { Text(if (isBangla) "স্টক আউট" else "Stock Out") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedExpense.copy(alpha = 0.2f)))
                }

                if (state.quickSelectedProduct == null) {
                    OutlinedTextField(
                        value = state.quickSearchQuery,
                        onValueChange = viewModel::quickSetSearchQuery,
                        placeholder = { Text(if (isBangla) "পণ্য খুঁজুন..." else "Search products...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    val filtered = if (state.quickSearchQuery.isBlank()) state.allProducts.take(8)
                    else state.allProducts.filter {
                        it.name.contains(state.quickSearchQuery, ignoreCase = true) ||
                        it.nameBangla.contains(state.quickSearchQuery, ignoreCase = true)
                    }.take(8)
                    if (filtered.isNotEmpty()) {
                        FlowRow(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            filtered.forEach { product ->
                                FilterChip(
                                    selected = false,
                                    onClick = { viewModel.quickSelectProduct(product) },
                                    label = { Text(if (isBangla) product.nameBangla else product.name, style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(if (isBangla) state.quickSelectedProduct.nameBangla else state.quickSelectedProduct.name, fontWeight = FontWeight.Bold)
                            Text("${if (isBangla) "স্টক" else "Stock"}: ${state.quickSelectedProduct.currentStock.toInt()}", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { viewModel.quickClearProduct() }) { Text(if (isBangla) "পরিবর্তন" else "Change") }
                    }
                    OutlinedTextField(
                        value = state.quickQuantity,
                        onValueChange = viewModel::quickSetQuantity,
                        label = { Text(if (isBangla) "পরিমাণ" else "Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = state.quickDescription,
                        onValueChange = viewModel::quickSetDescription,
                        label = { Text(if (isBangla) "নোট" else "Note") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            if (state.quickSelectedProduct != null) {
                Button(
                    onClick = viewModel::quickUpdateStock,
                    enabled = !state.quickIsSaving && (state.quickQuantity.toDoubleOrNull() ?: 0.0) > 0
                ) { Text(if (state.quickIsSaving) (if (isBangla) "সংরক্ষণ..." else "Saving...") else (if (isBangla) "সংরক্ষণ" else "Save")) }
            }
        },
        dismissButton = { TextButton(onClick = viewModel::hideQuickStock) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

// ─── Quick Payment Dialog ──────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickPaymentDialog(state: DashboardState, viewModel: DashboardViewModel, isBangla: Boolean) {
    AlertDialog(
        onDismissRequest = viewModel::hideQuickPayment,
        title = { Text(if (isBangla) "দ্রুত পেমেন্ট" else "Quick Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = state.quickPaymentIsReceive, onClick = { viewModel.quickSetPaymentIsReceive(true) }, label = { Text(if (isBangla) "প্রাপ্তি" else "Receive") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenProfit.copy(alpha = 0.2f)))
                    FilterChip(selected = !state.quickPaymentIsReceive, onClick = { viewModel.quickSetPaymentIsReceive(false) }, label = { Text(if (isBangla) "পরিশোধ" else "Pay") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedExpense.copy(alpha = 0.2f)))
                }

                if (state.quickSelectedCustomer == null) {
                    OutlinedTextField(
                        value = state.quickSearchQuery,
                        onValueChange = viewModel::quickSetSearchQuery,
                        placeholder = { Text(if (isBangla) "গ্রাহক খুঁজুন..." else "Search customers...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    val filtered = if (state.quickSearchQuery.isBlank()) state.allCustomers.take(8)
                    else state.allCustomers.filter {
                        it.name.contains(state.quickSearchQuery, ignoreCase = true) ||
                        it.phone.contains(state.quickSearchQuery)
                    }.take(8)
                    if (filtered.isNotEmpty()) {
                        FlowRow(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            filtered.forEach { customer ->
                                FilterChip(
                                    selected = false,
                                    onClick = { viewModel.quickSelectCustomer(customer) },
                                    label = { Text(customer.name, style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(state.quickSelectedCustomer.name, fontWeight = FontWeight.Bold)
                            Text("${if (isBangla) "বাকি" else "Due"}: ${CurrencyFormatter.format(state.quickSelectedCustomer.totalDue)}", style = MaterialTheme.typography.bodySmall, color = OrangeDue)
                        }
                        TextButton(onClick = viewModel::quickClearCustomer) { Text(if (isBangla) "পরিবর্তন" else "Change") }
                    }
                }

                OutlinedTextField(
                    value = state.quickAmount,
                    onValueChange = viewModel::quickSetAmount,
                    label = { Text(if (isBangla) "পরিমাণ" else "Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                OutlinedTextField(
                    value = state.quickDescription,
                    onValueChange = viewModel::quickSetDescription,
                    label = { Text(if (isBangla) "বিবরণ" else "Description") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = viewModel::quickRecordPayment,
                enabled = !state.quickIsSaving && (state.quickAmount.toDoubleOrNull() ?: 0.0) > 0
            ) { Text(if (state.quickIsSaving) (if (isBangla) "সংরক্ষণ..." else "Saving...") else (if (isBangla) "সংরক্ষণ" else "Save")) }
        },
        dismissButton = { TextButton(onClick = viewModel::hideQuickPayment) { Text(if (isBangla) "বাতিল" else "Cancel") } }
    )
}

// ─── Shared Components ────────────────────────────────────────────

private data class ActionDef(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun LowStockAlertCard(
    lowStockCount: Int,
    isBangla: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = OrangeDue.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = OrangeDue.copy(alpha = 0.15f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = OrangeDue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) "$lowStockCount টি পণ্যের স্টক কম!" else "$lowStockCount products low on stock!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OrangeDue,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isBangla) "স্টক ইন করতে ট্যাপ করুন" else "Tap to restock",
                    style = MaterialTheme.typography.bodySmall,
                    color = OrangeDue.copy(alpha = 0.7f)
                )
            }
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = OrangeDue,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun ModernActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(88.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = color.copy(alpha = 0.12f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
