package com.rudra.hisab.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MoreMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
fun MoreScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToSale: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToDailyClose: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAccounting: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToExport: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val allMenuItems = remember {
        listOf(
            MoreMenuItem(Icons.Default.Description, "Dashboard", "Main page & summary", onNavigateToDashboard),
            MoreMenuItem(Icons.Default.Inventory2, "Inventory", "Manage products & stock", onNavigateToInventory),
            MoreMenuItem(Icons.Default.PointOfSale, "Sale", "Record new sale", onNavigateToSale),
            MoreMenuItem(Icons.Default.People, "Customers", "Customer list & due", onNavigateToCustomers),
            MoreMenuItem(Icons.Default.ShoppingCart, "Expenses", "Daily expenses", onNavigateToExpenses),
            MoreMenuItem(Icons.Default.AccountBalance, "Accounting", "Cash book & ledger", onNavigateToAccounting),
            MoreMenuItem(Icons.Default.CalendarMonth, "Daily Close", "End of day closing", onNavigateToDailyClose),
            MoreMenuItem(Icons.Default.BarChart, "Analytics", "Sales analysis & charts", onNavigateToAnalytics),
            MoreMenuItem(Icons.Default.ReceiptLong, "Reports", "Detailed reports & PDF", onNavigateToReports),
            MoreMenuItem(Icons.Default.CloudUpload, "Backup & Export", "Data backup & export", onNavigateToExport),
            MoreMenuItem(Icons.Default.Settings, "Settings", "Language, PIN & other settings", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Language, "Language & Theme", "English / বাংলা, Light / Dark", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Store, "Shop Info", "Shop name & details", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Lock, "PIN Setup", "4-digit app lock", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Fingerprint, "Biometric Lock", "Fingerprint lock", onNavigateToSettings),
            MoreMenuItem(Icons.Default.ShoppingCart, "Cart Mode", "Cart system for sales", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Upcoming, "FAB Mode", "Floating action button", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Inventory2, "Batch Tracking", "Expiry & batch tracking", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Timer, "Credit Limit", "Max credit per customer", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Delete, "Delete Window", "Transaction delete window", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Schedule, "Sale Reminder", "Daily sale reminder time", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Notifications, "Report Reminder", "Monthly report notification", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Description, "Nav Order", "Customize bottom menu", onNavigateToSettings),
            MoreMenuItem(Icons.Default.TrendingUp, "Quick Actions", "Dashboard action buttons", onNavigateToSettings),
            MoreMenuItem(Icons.Default.FileDownload, "Data Export", "Export as JSON / CSV", onNavigateToSettings),
            MoreMenuItem(Icons.Default.FileUpload, "Data Import", "Restore from JSON", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Delete, "Clear All Data", "Permanently delete all data", onNavigateToSettings),
            MoreMenuItem(Icons.Default.Info, "About", "Hisab v2.0 — App info", onNavigateToSettings)
        )
    }

    val filteredItems = remember(searchQuery, allMenuItems) {
        if (searchQuery.isBlank()) allMenuItems
        else {
            val query = searchQuery.lowercase()
            allMenuItems.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.subtitle.contains(query, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "More",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Other Management",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search function…") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clickable { searchQuery = "" }
                            .padding(4.dp)
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isNotEmpty()) {
            Text(
                text = "${filteredItems.size} results found",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredItems) { item ->
                MoreGridCard(item = item)
            }
        }
    }
}

@Composable
private fun MoreGridCard(item: MoreMenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = item.onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}