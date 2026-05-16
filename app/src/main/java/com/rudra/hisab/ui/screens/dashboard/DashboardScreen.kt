package com.rudra.hisab.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.SalePaymentType
import com.rudra.hisab.ui.theme.BlueInfo
import com.rudra.hisab.ui.theme.GreenProfit
import com.rudra.hisab.ui.theme.OrangeDue
import com.rudra.hisab.ui.theme.RedExpense
import com.rudra.hisab.util.CurrencyFormatter
import com.rudra.hisab.util.LocalStrings

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSale: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToAddStock: (() -> Unit)? = null,
) {
    val state   by viewModel.state.collectAsState()
    val strings = LocalStrings.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        floatingActionButton = {
            DashboardFabMenu(
                expanded  = state.showFabMenu,
                onToggle  = viewModel::toggleFabMenu,
                onSale    = viewModel::showQuickSale,
                onExpense = viewModel::showQuickExpense,
                onStock   = viewModel::showQuickStock,
                onPayment = viewModel::showQuickPayment,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero Section (gradient banner + period toggle) ─────────────
            HeroSection(
                state   = state,
                strings = strings,
                onPeriodSelect = viewModel::setSelectedPeriod,
            )

            Spacer(Modifier.height(20.dp))

            Column(Modifier.padding(horizontal = 16.dp)) {

                // ── Loading skeletons ──────────────────────────────────────
                if (state.isLoading) {
                    repeat(3) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(vertical = 5.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                        ) {}
                    }
                } else {

                    // ── Animated metric grid (Today ↔ Month) ───────────────
                    SectionLabel(
                        icon  = if (state.selectedPeriod == DashboardPeriod.TODAY)
                            Icons.Default.DateRange else Icons.Default.CalendarMonth,
                        text  = if (state.selectedPeriod == DashboardPeriod.TODAY)
                            strings.todayBreakdown else strings.monthlyBreakdown,
                    )
                    Spacer(Modifier.height(10.dp))

                    AnimatedContent(
                        targetState   = state.selectedPeriod,
                        transitionSpec = {
                            val toMonth = targetState == DashboardPeriod.MONTH
                            (slideInHorizontally(tween(320)) { if (toMonth) it else -it } + fadeIn(tween(280))) togetherWith
                                    (slideOutHorizontally(tween(280)) { if (toMonth) -it else it } + fadeOut(tween(220)))
                        },
                        label = "period_metrics",
                    ) { period ->
                        val (sales, expenses, profit, dues, profitColor) = when (period) {
                            DashboardPeriod.TODAY -> MetricValues(
                                sales       = state.todaySales,
                                expenses    = state.todayExpenses,
                                profit      = state.netProfit,
                                dues        = state.totalDues,
                                profitColor = if (state.netProfit >= 0) GreenProfit else RedExpense,
                            )
                            DashboardPeriod.MONTH -> MetricValues(
                                sales       = state.monthSales,
                                expenses    = state.monthExpenses,
                                profit      = state.monthNetProfit,
                                dues        = state.totalDues,
                                profitColor = if (state.monthNetProfit >= 0) GreenProfit else RedExpense,
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                MetricCard(
                                    modifier  = Modifier.weight(1f),
                                    label     = strings.totalSales,
                                    amount    = sales,
                                    icon      = Icons.Default.TrendingUp,
                                    color     = GreenProfit,
                                    progress  = if (period == DashboardPeriod.TODAY) state.dailySalesFraction else null,
                                    onClick   = onNavigateToSale,
                                )
                                MetricCard(
                                    modifier  = Modifier.weight(1f),
                                    label     = strings.netProfit,
                                    amount    = profit,
                                    icon      = if (profit >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    color     = profitColor,
                                    progress  = null,
                                    onClick   = {},
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                MetricCard(
                                    modifier  = Modifier.weight(1f),
                                    label     = strings.totalExpenses,
                                    amount    = expenses,
                                    icon      = Icons.Default.MoneyOff,
                                    color     = RedExpense,
                                    progress  = if (period == DashboardPeriod.TODAY) state.dailyExpenseFraction else null,
                                    onClick   = onNavigateToExpenses,
                                )
                                MetricCard(
                                    modifier  = Modifier.weight(1f),
                                    label     = strings.totalDues,
                                    amount    = dues,
                                    icon      = Icons.Default.People,
                                    color     = OrangeDue,
                                    progress  = null,
                                    onClick   = onNavigateToCustomers,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Mini Stats Row ─────────────────────────────────────
                    SectionLabel(icon = Icons.Default.AttachMoney, text = strings.quickStats)
                    Spacer(Modifier.height(10.dp))
                    AnimatedContent(
                        targetState = state.selectedPeriod,
                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                        label = "period_chips",
                    ) { period ->
                        val purchases = if (period == DashboardPeriod.TODAY) state.todayPurchases else state.monthPurchases
                        val credit    = if (period == DashboardPeriod.TODAY) state.todayCreditGiven else state.monthCreditGiven
                        val count     = if (period == DashboardPeriod.TODAY) state.todaySaleCount else state.monthSaleCount
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MiniStatChip(strings.saleCount,  "$count",                             GreenProfit)
                            MiniStatChip(strings.purchases,   CurrencyFormatter.format(purchases),  BlueInfo)
                            MiniStatChip(strings.creditGiven, CurrencyFormatter.format(credit),     OrangeDue)
                            MiniStatChip(strings.products,    "${state.totalProductCount}",         primaryColor)
                            MiniStatChip(strings.customers,   "${state.totalCustomerCount}",        OrangeDue)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Stock Overview ─────────────────────────────────────
                    SectionLabel(icon = Icons.Default.Inventory2, text = strings.stockOverview)
                    Spacer(Modifier.height(10.dp))
                    StockOverviewCard(
                        totalProducts   = state.totalProductCount,
                        totalStockValue = state.totalStockValue,
                        lowStockCount   = state.lowStockCount,
                        onClick         = { onNavigateToAddStock?.invoke() },
                    )

                    AnimatedVisibility(
                        visible = state.lowStockCount > 0,
                        enter   = fadeIn() + slideInVertically { it / 2 },
                        exit    = fadeOut() + slideOutVertically { it / 2 },
                    ) {
                        Column {
                            Spacer(Modifier.height(10.dp))
                            LowStockAlertCard(
                                lowStockCount = state.lowStockCount,
                                onClick       = { onNavigateToAddStock?.invoke() },
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Quick Actions ──────────────────────────────────────
                    SectionLabel(icon = Icons.Default.Sell, text = strings.quickActions)
                    Spacer(Modifier.height(10.dp))

                    val actionMap = remember {
                        mapOf(
                            "sale"     to ActionDef(strings.qaSale,      Icons.Default.AttachMoney,  GreenProfit,  onNavigateToSale),
                            "stock"    to ActionDef(strings.qaStock,     Icons.Default.Inventory2,   BlueInfo,     onNavigateToInventory),
                            "expense"  to ActionDef(strings.qaExpense,   Icons.Default.MoneyOff,     RedExpense,   onNavigateToExpenses),
                            "customer" to ActionDef(strings.customerLabel, Icons.Default.People,     OrangeDue,    onNavigateToCustomers),
                            "purchase" to ActionDef(strings.qaPurchase,  Icons.Default.ShoppingCart, primaryColor, onNavigateToInventory),
                        )
                    }
                    val enabledActions by remember(state.quickActions) {
                        derivedStateOf { state.quickActions.mapNotNull { actionMap[it] } }
                    }

                    enabledActions.chunked(2).forEach { row ->
                        Row(
                            modifier             = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            row.forEach { action ->
                                ModernActionButton(
                                    label    = action.label,
                                    icon     = action.icon,
                                    color    = action.color,
                                    onClick  = action.onClick,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(80.dp)) // FAB clearance
        }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────────
    if (state.showQuickSaleDialog)    QuickSaleDialog(state, viewModel)
    if (state.showQuickExpenseDialog) QuickExpenseDialog(state, viewModel)
    if (state.showQuickStockDialog)   QuickStockDialog(state, viewModel)
    if (state.showQuickPaymentDialog) QuickPaymentDialog(state, viewModel)
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(
    state:          DashboardState,
    strings:        com.rudra.hisab.util.Strings,
    onPeriodSelect: (DashboardPeriod) -> Unit,
) {
    val isTodayMode = state.selectedPeriod == DashboardPeriod.TODAY
    val heroSales   = if (isTodayMode) state.todaySales  else state.monthSales
    val heroProfit  = if (isTodayMode) state.netProfit   else state.monthNetProfit
    val heroExpense = if (isTodayMode) state.todayExpenses else state.monthExpenses
    val heroCount   = if (isTodayMode) state.todaySaleCount else state.monthSaleCount
    val profitPositive = heroProfit >= 0

    // Dynamic gradient based on profit sign
    val gradientColors = if (profitPositive)
        listOf(GreenProfit, GreenProfit.copy(alpha = 0.72f), BlueInfo.copy(alpha = 0.55f))
    else
        listOf(OrangeDue, RedExpense.copy(alpha = 0.75f), OrangeDue.copy(alpha = 0.5f))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .drawBehind {
                // Gradient fill
                drawRect(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start  = Offset(0f, 0f),
                        end    = Offset(size.width * 1.2f, size.height),
                    )
                )
                // Decorative circles — large ambient blobs
                drawCircle(
                    color  = Color.White.copy(alpha = 0.06f),
                    radius = size.width * 0.72f,
                    center = Offset(size.width * 1.05f, -size.height * 0.15f),
                )
                drawCircle(
                    color  = Color.White.copy(alpha = 0.04f),
                    radius = size.width * 0.48f,
                    center = Offset(-size.width * 0.1f, size.height * 1.1f),
                )
                drawCircle(
                    color  = Color.White.copy(alpha = 0.07f),
                    radius = size.width * 0.28f,
                    center = Offset(size.width * 0.6f, size.height * 1.05f),
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            // Shop name + date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text     = state.shopName.ifEmpty { "Hisab" },
                        style    = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color    = Color.White,
                    )
                    Text(
                        text  = state.dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
                // Period pill toggle
                PeriodToggle(
                    selected = state.selectedPeriod,
                    onSelect = onPeriodSelect,
                )
            }

            Spacer(Modifier.weight(1f))

            // Big Revenue Number
            Text(
                text     = if (isTodayMode) strings.todayRevenue else strings.monthRevenue,
                style    = MaterialTheme.typography.labelMedium,
                color    = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(2.dp))
            AnimatedContent(
                targetState   = heroSales,
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(250)) },
                label         = "hero_sales",
            ) { sales ->
                Text(
                    text       = CurrencyFormatter.format(sales),
                    style      = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Sub-metrics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                HeroSubStat(
                    icon  = if (profitPositive) Icons.Default.TrendingUp else Icons.Default.ArrowDropDown,
                    label = strings.netProfit,
                    value = CurrencyFormatter.format(heroProfit),
                    color = if (profitPositive) Color.White else Color.White.copy(alpha = 0.85f),
                )
                HeroVerticalDivider()
                HeroSubStat(
                    icon  = Icons.Default.MoneyOff,
                    label = strings.expenses,
                    value = CurrencyFormatter.format(heroExpense),
                    color = Color.White.copy(alpha = 0.9f),
                )
                HeroVerticalDivider()
                HeroSubStat(
                    icon  = Icons.Default.Sell,
                    label = strings.sales,
                    value = "$heroCount",
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun HeroSubStat(icon: ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(3.dp))
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.75f))
    }
}

@Composable
private fun HeroVerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(Color.White.copy(alpha = 0.25f))
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Period Toggle (pill-shaped segmented control)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PeriodToggle(
    selected: DashboardPeriod,
    onSelect: (DashboardPeriod) -> Unit,
) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.22f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        DashboardPeriod.entries.forEach { period ->
            val isSelected = selected == period
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.9f)
                        else Color.Transparent
                    )
                    .clickable { onSelect(period) }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = if (period == DashboardPeriod.TODAY) strings.today else strings.thisMonth,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (isSelected) GreenProfit else Color.White,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section Label
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text       = text,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Metric Card (redesigned with optional progress bar)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetricCard(
    modifier:  Modifier = Modifier,
    label:     String,
    amount:    Double,
    icon:      ImageVector,
    color:     Color,
    /** 0–1 fraction to show as a progress bar. null = no bar shown. */
    progress:  Float?,
    onClick:   () -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue    = progress ?: 0f,
        animationSpec  = tween(durationMillis = 900, easing = LinearEasing),
        label          = "metric_progress",
    )

    Card(
        onClick = onClick,
        modifier = modifier,
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Coloured top stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(color, color.copy(alpha = 0.4f))
                        )
                    )
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text     = label,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text       = CurrencyFormatter.format(amount),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = color,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                // Progress bar: today vs month
                if (progress != null) {
                    Spacer(Modifier.height(8.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text  = LocalStrings.current.ofMonth,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                            Text(
                                text       = "${(animatedProgress * 100).toInt()}%",
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = color,
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(color.copy(alpha = 0.12f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f)))
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mini Stat Chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MiniStatChip(label: String, value: String, color: Color) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.09f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.65f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stock Overview Card
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockOverviewCard(
    totalProducts:   Int,
    totalStockValue: Double,
    lowStockCount:   Int,
    onClick:         () -> Unit,
) {
    val strings = LocalStrings.current
    val health  = when {
        lowStockCount > 10 -> 0.20f
        lowStockCount > 5  -> 0.50f
        lowStockCount > 2  -> 0.75f
        lowStockCount > 0  -> 0.88f
        else               -> 1.00f
    }
    val healthColor = when {
        lowStockCount > 10 -> RedExpense
        lowStockCount > 5  -> OrangeDue
        lowStockCount > 0  -> OrangeDue
        else               -> GreenProfit
    }
    val animatedHealth by animateFloatAsState(
        targetValue   = health,
        animationSpec = tween(1000),
        label         = "stock_health",
    )

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StockStat(strings.totalProducts,  "$totalProducts",                        BlueInfo)
                StockStatDivider()
                StockStat(strings.stockValue,     CurrencyFormatter.format(totalStockValue), GreenProfit)
                StockStatDivider()
                StockStat(
                    label = strings.lowStock,
                    value = "$lowStockCount",
                    color = if (lowStockCount > 0) RedExpense else GreenProfit,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    strings.stockHealth,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(healthColor.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedHealth)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(listOf(healthColor, healthColor.copy(alpha = 0.55f)))
                            )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = if (lowStockCount == 0) strings.good else "$lowStockCount ${strings.lowStock}",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = healthColor,
                )
            }
        }
    }
}

@Composable
private fun StockStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StockStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Low Stock Alert Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LowStockAlertCard(lowStockCount: Int, onClick: () -> Unit) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OrangeDue.copy(alpha = 0.09f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OrangeDue.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = OrangeDue, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = strings.lowStockLabel(lowStockCount),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = OrangeDue,
                )
                Text(
                    text  = strings.tapToRestock,
                    style = MaterialTheme.typography.labelSmall,
                    color = OrangeDue.copy(alpha = 0.7f),
                )
            }
            Icon(Icons.Default.Add, contentDescription = null, tint = OrangeDue, modifier = Modifier.size(28.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Modern Action Button
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernActionButton(
    label:    String,
    icon:     ImageVector,
    color:    Color,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick   = onClick,
        modifier  = modifier.height(92.dp),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Subtle top-right accent circle
                    drawCircle(
                        color  = color.copy(alpha = 0.06f),
                        radius = size.width * 0.55f,
                        center = Offset(size.width * 1.0f, -size.height * 0.1f),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FAB Speed Dial
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardFabMenu(
    expanded:  Boolean,
    onToggle:  () -> Unit,
    onSale:    () -> Unit,
    onExpense: () -> Unit,
    onStock:   () -> Unit,
    onPayment: () -> Unit,
) {
    val strings = LocalStrings.current
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = expanded,
            enter   = fadeIn(tween(200)) + slideInVertically(tween(220)) { it / 2 },
            exit    = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 2 },
        ) {
            Column(
                horizontalAlignment  = Alignment.End,
                verticalArrangement  = Arrangement.spacedBy(8.dp),
            ) {
                FabItem(Icons.Default.Sell,       strings.qaSale,    GreenProfit, onSale)
                FabItem(Icons.Default.MoneyOff,   strings.qaExpense, RedExpense,  onExpense)
                FabItem(Icons.Default.Inventory2, strings.qaStock,   BlueInfo,    onStock)
                FabItem(Icons.Default.Payment,    strings.qaPayment, OrangeDue,   onPayment)
            }
        }
        Spacer(Modifier.height(8.dp))
        FloatingActionButton(
            onClick         = onToggle,
            containerColor  = MaterialTheme.colorScheme.primary,
        ) {
            Box(
                modifier = Modifier.graphicsLayer { rotationZ = if (expanded) 45f else 0f },
            ) {
                Icon(
                    if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun FabItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Label pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(8.dp))
        SmallFloatingActionButton(
            onClick        = onClick,
            containerColor = color,
            contentColor   = Color.White,
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Internal helpers
// ─────────────────────────────────────────────────────────────────────────────

private data class ActionDef(
    val label:   String,
    val icon:    ImageVector,
    val color:   Color,
    val onClick: () -> Unit,
)

/** Helper to destructure 5 metric values cleanly in AnimatedContent. */
private data class MetricValues(
    val sales:       Double,
    val expenses:    Double,
    val profit:      Double,
    val dues:        Double,
    val profitColor: Color,
)

// ─────────────────────────────────────────────────────────────────────────────
// Quick Sale Dialog (unchanged functionality, keeps existing logic)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickSaleDialog(state: DashboardState, viewModel: DashboardViewModel) {
    val strings = LocalStrings.current
    AlertDialog(
        onDismissRequest = viewModel::hideQuickSale,
        title = { Text(strings.quickSale) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.quickSelectedProduct == null) {
                    OutlinedTextField(
                        value            = state.quickSearchQuery,
                        onValueChange    = viewModel::quickSetSearchQuery,
                        placeholder      = { Text(strings.searchProducts) },
                        leadingIcon      = { Icon(Icons.Default.Search, null) },
                        modifier         = Modifier.fillMaxWidth(),
                        singleLine       = true,
                    )
                    val filtered = (
                            if (state.quickSearchQuery.isBlank()) state.allProducts.take(8)
                            else state.allProducts.filter {
                                it.name.contains(state.quickSearchQuery, ignoreCase = true) ||
                                        it.nameBangla.contains(state.quickSearchQuery, ignoreCase = true)
                            }.take(8)
                            )
                    if (filtered.isNotEmpty()) {
                        FlowRow(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            filtered.forEach { p ->
                                FilterChip(
                                    selected = false,
                                    onClick  = { viewModel.quickSelectProduct(p) },
                                    label    = { Text(strings.nameOrBangla(p.name, p.nameBangla), style = MaterialTheme.typography.bodySmall) },
                                )
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(strings.nameOrBangla(state.quickSelectedProduct.name, state.quickSelectedProduct.nameBangla), fontWeight = FontWeight.Bold)
                            Text("${CurrencyFormatter.format(state.quickSelectedProduct.sellPrice)} / ${state.quickSelectedProduct.unit}", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = viewModel::quickClearProduct) { Text(strings.change) }
                    }
                    OutlinedTextField(
                        value         = state.quickQuantity,
                        onValueChange = viewModel::quickSetQuantity,
                        label         = { Text(strings.quantity) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier      = Modifier.fillMaxWidth(), singleLine = true,
                    )
                    val total = (state.quickQuantity.toDoubleOrNull() ?: 0.0) * state.quickSelectedProduct.sellPrice
                    Text("${strings.total}: ${CurrencyFormatter.format(total)}", fontWeight = FontWeight.Bold, color = GreenProfit)
                    Text(strings.paymentType, style = MaterialTheme.typography.bodySmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SalePaymentType.entries.forEach { type ->
                            FilterChip(
                                selected = state.quickPaymentType == type,
                                onClick  = { viewModel.quickSetPaymentType(type) },
                                label    = {
                                    Text(when (type) {
                                        SalePaymentType.CASH    -> strings.cash
                                        SalePaymentType.CREDIT  -> strings.credit
                                        SalePaymentType.PARTIAL -> strings.partial
                                    }, style = MaterialTheme.typography.bodySmall)
                                },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenProfit.copy(alpha = 0.2f)),
                            )
                        }
                    }
                    if (state.quickPaymentType == SalePaymentType.PARTIAL) {
                        OutlinedTextField(
                            value         = state.quickAmount,
                            onValueChange = viewModel::quickSetAmount,
                            label         = { Text(strings.paidAmount) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier      = Modifier.fillMaxWidth(), singleLine = true,
                        )
                    }
                    if (state.quickSaleComplete) {
                        Text(strings.saleSuccessful, color = GreenProfit, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (state.quickSelectedProduct != null && !state.quickSaleComplete) {
                Button(
                    onClick  = viewModel::quickCompleteSale,
                    enabled  = !state.quickIsSaving && (state.quickQuantity.toDoubleOrNull() ?: 0.0) > 0,
                ) { Text(if (state.quickIsSaving) strings.processing else strings.confirm) }
            }
        },
        dismissButton = { TextButton(onClick = viewModel::hideQuickSale) { Text(strings.close) } },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick Expense Dialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickExpenseDialog(state: DashboardState, viewModel: DashboardViewModel) {
    val strings = LocalStrings.current
    AlertDialog(
        onDismissRequest = viewModel::hideQuickExpense,
        title = { Text(strings.quickExpense) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = state.quickAmount,
                    onValueChange = viewModel::quickSetAmount,
                    label         = { Text(strings.amount) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier      = Modifier.fillMaxWidth(), singleLine = true,
                )
                OutlinedTextField(
                    value         = state.quickDescription,
                    onValueChange = viewModel::quickSetDescription,
                    label         = { Text(strings.description) },
                    modifier      = Modifier.fillMaxWidth(), singleLine = true,
                )
                Text(strings.category, style = MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExpenseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = state.quickExpenseCategory == cat,
                            onClick  = { viewModel.quickSetExpenseCategory(cat) },
                            label    = { Text(cat.name, style = MaterialTheme.typography.bodySmall) },
                            colors   = FilterChipDefaults.filterChipColors(selectedContainerColor = RedExpense.copy(alpha = 0.2f)),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = viewModel::quickAddExpense,
                enabled  = !state.quickIsSaving && (state.quickAmount.toDoubleOrNull() ?: 0.0) > 0,
            ) { Text(if (state.quickIsSaving) strings.saving else strings.save) }
        },
        dismissButton = { TextButton(onClick = viewModel::hideQuickExpense) { Text(strings.cancel) } },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick Stock Dialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickStockDialog(state: DashboardState, viewModel: DashboardViewModel) {
    val strings = LocalStrings.current
    AlertDialog(
        onDismissRequest = viewModel::hideQuickStock,
        title = { Text(strings.quickStock) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(state.quickStockIsAdd,  { viewModel.quickSetStockIsAdd(true)  }, { Text(strings.stockIn)  }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenProfit.copy(alpha = 0.2f)))
                    FilterChip(!state.quickStockIsAdd, { viewModel.quickSetStockIsAdd(false) }, { Text(strings.stockOut) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedExpense.copy(alpha = 0.2f)))
                }
                if (state.quickSelectedProduct == null) {
                    OutlinedTextField(
                        value         = state.quickSearchQuery,
                        onValueChange = viewModel::quickSetSearchQuery,
                        placeholder   = { Text(strings.searchProducts) },
                        leadingIcon   = { Icon(Icons.Default.Search, null) },
                        modifier      = Modifier.fillMaxWidth(), singleLine = true,
                    )
                    val filtered = if (state.quickSearchQuery.isBlank()) state.allProducts.take(8)
                    else state.allProducts.filter {
                        it.name.contains(state.quickSearchQuery, ignoreCase = true) ||
                                it.nameBangla.contains(state.quickSearchQuery, ignoreCase = true)
                    }.take(8)
                    if (filtered.isNotEmpty()) {
                        FlowRow(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            filtered.forEach { p ->
                                FilterChip(false, { viewModel.quickSelectProduct(p) }, { Text(strings.nameOrBangla(p.name, p.nameBangla), style = MaterialTheme.typography.bodySmall) })
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(strings.nameOrBangla(state.quickSelectedProduct.name, state.quickSelectedProduct.nameBangla), fontWeight = FontWeight.Bold)
                            Text("${strings.stockLabel}: ${state.quickSelectedProduct.currentStock.toInt()}", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = viewModel::quickClearProduct) { Text(strings.change) }
                    }
                    OutlinedTextField(
                        value         = state.quickQuantity,
                        onValueChange = viewModel::quickSetQuantity,
                        label         = { Text(strings.quantity) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier      = Modifier.fillMaxWidth(), singleLine = true,
                    )
                    OutlinedTextField(
                        value         = state.quickDescription,
                        onValueChange = viewModel::quickSetDescription,
                        label         = { Text(strings.notes) },
                        modifier      = Modifier.fillMaxWidth(), singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            if (state.quickSelectedProduct != null) {
                Button(
                    onClick  = viewModel::quickUpdateStock,
                    enabled  = !state.quickIsSaving && (state.quickQuantity.toDoubleOrNull() ?: 0.0) > 0,
                ) { Text(if (state.quickIsSaving) strings.saving else strings.save) }
            }
        },
        dismissButton = { TextButton(onClick = viewModel::hideQuickStock) { Text(strings.cancel) } },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick Payment Dialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickPaymentDialog(state: DashboardState, viewModel: DashboardViewModel) {
    val strings = LocalStrings.current
    AlertDialog(
        onDismissRequest = viewModel::hideQuickPayment,
        title = { Text(strings.quickPayment) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(state.quickPaymentIsReceive,  { viewModel.quickSetPaymentIsReceive(true)  }, { Text(strings.receive) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenProfit.copy(alpha = 0.2f)))
                    FilterChip(!state.quickPaymentIsReceive, { viewModel.quickSetPaymentIsReceive(false) }, { Text(strings.pay)     }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedExpense.copy(alpha = 0.2f)))
                }
                if (state.quickSelectedCustomer == null) {
                    OutlinedTextField(
                        value         = state.quickSearchQuery,
                        onValueChange = viewModel::quickSetSearchQuery,
                        placeholder   = { Text(strings.searchCustomers) },
                        leadingIcon   = { Icon(Icons.Default.Search, null) },
                        modifier      = Modifier.fillMaxWidth(), singleLine = true,
                    )
                    val filtered = if (state.quickSearchQuery.isBlank()) state.allCustomers.take(8)
                    else state.allCustomers.filter {
                        it.name.contains(state.quickSearchQuery, ignoreCase = true) ||
                                it.phone.contains(state.quickSearchQuery)
                    }.take(8)
                    if (filtered.isNotEmpty()) {
                        FlowRow(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            filtered.forEach { c ->
                                FilterChip(false, { viewModel.quickSelectCustomer(c) }, { Text(c.name, style = MaterialTheme.typography.bodySmall) })
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(state.quickSelectedCustomer.name, fontWeight = FontWeight.Bold)
                            Text("${strings.due}: ${CurrencyFormatter.format(state.quickSelectedCustomer.totalDue)}", style = MaterialTheme.typography.bodySmall, color = OrangeDue)
                        }
                        TextButton(onClick = viewModel::quickClearCustomer) { Text(strings.change) }
                    }
                }
                OutlinedTextField(
                    value         = state.quickAmount,
                    onValueChange = viewModel::quickSetAmount,
                    label         = { Text(strings.amount) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier      = Modifier.fillMaxWidth(), singleLine = true,
                )
                OutlinedTextField(
                    value         = state.quickDescription,
                    onValueChange = viewModel::quickSetDescription,
                    label         = { Text(strings.description) },
                    modifier      = Modifier.fillMaxWidth(), singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = viewModel::quickRecordPayment,
                enabled  = !state.quickIsSaving && (state.quickAmount.toDoubleOrNull() ?: 0.0) > 0,
            ) { Text(if (state.quickIsSaving) strings.saving else strings.save) }
        },
        dismissButton = { TextButton(onClick = viewModel::hideQuickPayment) { Text(strings.cancel) } },
    )
}