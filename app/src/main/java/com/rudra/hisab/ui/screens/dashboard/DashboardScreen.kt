package com.rudra.hisab.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.hisab.ui.theme.BlueInfo
import com.rudra.hisab.ui.theme.BlueInfoContainer
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
            SummaryCard(
                label = "আজকের বিক্রয়",
                amount = state.todaySales,
                color = GreenProfit,
                icon = Icons.Default.AttachMoney,
                onClick = onNavigateToSale
            )

            Spacer(modifier = Modifier.height(8.dp))

            SummaryCard(
                label = "আজকের খরচ",
                amount = state.todayExpenses,
                color = RedExpense,
                icon = Icons.Default.MoneyOff,
                onClick = onNavigateToExpenses
            )

            Spacer(modifier = Modifier.height(8.dp))

            SummaryCard(
                label = "নিট মুনাফা",
                amount = state.netProfit,
                color = if (state.netProfit >= 0) GreenProfit else RedExpense,
                icon = Icons.Default.AttachMoney,
                onClick = { }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SummaryCard(
                label = "মোট বাকি",
                amount = state.totalDues,
                color = OrangeDue,
                icon = Icons.Default.People,
                onClick = onNavigateToCustomers
            )

            if (state.lowStockCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAddStock?.invoke() },
                    colors = CardDefaults.cardColors(
                        containerColor = OrangeDue.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = OrangeDue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${state.lowStockCount} টি পণ্যের স্টক কম!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OrangeDue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "স্টক ইন করতে ট্যাপ করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrangeDue.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Buy Now",
                            tint = OrangeDue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "দ্রুত অ্যাকশন",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        val actionMap = mapOf(
            "sale" to ActionDef("বিক্রয়", Icons.Default.AttachMoney, GreenProfit, onNavigateToSale),
            "stock" to ActionDef("স্টক", Icons.Default.Inventory2, BlueInfo, onNavigateToInventory),
            "expense" to ActionDef("খরচ", Icons.Default.MoneyOff, RedExpense, onNavigateToExpenses),
            "customer" to ActionDef("গ্রাহক", Icons.Default.People, OrangeDue, onNavigateToCustomers),
            "purchase" to ActionDef("ক্রয়", Icons.Default.ShoppingCart, MaterialTheme.colorScheme.primary, onNavigateToInventory)
        )

        val enabledActions = state.quickActions.mapNotNull { actionMap[it] }
        enabledActions.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { action ->
                    ActionButton(
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
    }
}

private data class ActionDef(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun SummaryCard(
    label: String,
    amount: Double,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(amount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
