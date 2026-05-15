package com.rudra.hisab.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val route: String,
    val label: String,
    val labelEn: String,
    val icon: ImageVector
)

val allNavItems = listOf(
    NavItem(Routes.DASHBOARD, "ড্যাশবোর্ড", "Dashboard", Icons.Default.Home),
    NavItem(Routes.INVENTORY, "মজুদ", "Inventory", Icons.Default.Inventory),
    NavItem(Routes.SALE, "বিক্রয়", "Sale", Icons.Default.PointOfSale),
    NavItem(Routes.CUSTOMERS, "গ্রাহক", "Customers", Icons.Default.People),
    NavItem(Routes.MORE, "আরও", "More", Icons.Default.Description)
)

fun getOrderedNavItems(navOrder: String): List<NavItem> {
    val orderMap = navOrder.split(",").mapIndexed { index, s -> s.trim() to index }.toMap()
    return allNavItems.sortedBy { orderMap[it.route] ?: Int.MAX_VALUE }
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    navOrder: String = "dashboard,inventory,sale,customers,more",
    isFabMode: Boolean = false,
    onNavigate: (String) -> Unit,
    onFabClick: (() -> Unit)? = null
) {
    val items = getOrderedNavItems(navOrder)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
