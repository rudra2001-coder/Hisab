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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material.icons.filled.VerifiedUser
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
    onNavigateToSettings: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val allMenuItems = remember {
        listOf(
            // ── Main Navigation ──────────────────────────────────────────
            MoreMenuItem(
                icon = Icons.Default.Description,
                title = "ড্যাশবোর্ড",
                subtitle = "মূল পৃষ্ঠা ও সারসংক্ষেপ",
                onClick = onNavigateToDashboard
            ),
            MoreMenuItem(
                icon = Icons.Default.Inventory2,
                title = "মজুদ",
                subtitle = "পণ্য ও স্টক পরিচালনা",
                onClick = onNavigateToInventory
            ),
            MoreMenuItem(
                icon = Icons.Default.PointOfSale,
                title = "বিক্রয়",
                subtitle = "নতুন বিক্রয় রেকর্ড করুন",
                onClick = onNavigateToSale
            ),
            MoreMenuItem(
                icon = Icons.Default.People,
                title = "গ্রাহক",
                subtitle = "গ্রাহক তালিকা ও বাকি",
                onClick = onNavigateToCustomers
            ),
            MoreMenuItem(
                icon = Icons.Default.ShoppingCart,
                title = "খরচ",
                subtitle = "দৈনিক খরচ পরিচালনা",
                onClick = onNavigateToExpenses
            ),
            MoreMenuItem(
                icon = Icons.Default.CalendarMonth,
                title = "দৈনিক ক্লোজ",
                subtitle = "দিন শেষে হিসাব বন্ধ করুন",
                onClick = onNavigateToDailyClose
            ),
            MoreMenuItem(
                icon = Icons.Default.Analytics,
                title = "রিপোর্ট",
                subtitle = "বিক্রয় বিশ্লেষণ ও রিপোর্ট",
                onClick = onNavigateToAnalytics
            ),
            MoreMenuItem(
                icon = Icons.Default.Settings,
                title = "সেটিংস",
                subtitle = "ভাষা, PIN ও অন্যান্য সেটিংস",
                onClick = onNavigateToSettings
            ),

            // ── Settings Sub-items ───────────────────────────────────────
            MoreMenuItem(
                icon = Icons.Default.Language,
                title = "ভাষা ও থিম",
                subtitle = "বাংলা / English, হালকা / গাঢ় থিম",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Store,
                title = "দোকানের তথ্য",
                subtitle = "দোকানের নাম ও বিবরণ",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Lock,
                title = "পিন সেটআপ",
                subtitle = "অ্যাপ লক için ৪-ডিজিট পিন",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Fingerprint,
                title = "বায়োমেট্রিক লক",
                subtitle = "ফিঙ্গারপ্রিন্ট দিয়ে লক",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.ShoppingCart,
                title = "কার্ট মোড",
                subtitle = "বিক্রয়ে কার্ট সিস্টেম",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Upcoming,
                title = "FAB মোড",
                subtitle = "ড্যাশবোর্ডে ফ্লোটিং অ্যাকশন বাটন",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Inventory2,
                title = "ব্যাচ / মেয়াদ ট্র্যাকিং",
                subtitle = "পণ্যের মেয়াদ ও ব্যাচ নম্বর",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Timer,
                title = "ক্রেডিট লিমিট",
                subtitle = "গ্রাহকের সর্বোচ্চ বাকি সীমা",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Delete,
                title = "মুছার সময়সীমা",
                subtitle = "লেনদেন মুছতে পারবেন কত ঘন্টার মধ্যে",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Schedule,
                title = "বিক্রয় রিমাইন্ডার",
                subtitle = "দৈনিক বিক্রয় রিমাইন্ডার সময়",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Notifications,
                title = "মাসিক রিপোর্ট রিমাইন্ডার",
                subtitle = "প্রতিমাসে অটো রিপোর্ট নোটিফিকেশন",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Description,
                title = "নেভিগেশন অর্ডার",
                subtitle = "নিচের মেনুর অর্ডার পরিবর্তন",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.TrendingUp,
                title = "দ্রুত অ্যাকশন",
                subtitle = "ড্যাশবোর্ডে দ্রুত অ্যাকশন বাটন",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.FileDownload,
                title = "ডেটা এক্সপোর্ট",
                subtitle = "JSON / CSV ফরম্যাটে ব্যাকআপ",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.FileUpload,
                title = "ডেটা ইম্পোর্ট",
                subtitle = "JSON ফাইল থেকে ডেটা পুনরুদ্ধার",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Delete,
                title = "সব ডেটা মুছুন",
                subtitle = "সমস্ত তথ্য স্থায়ীভাবে মুছে ফেলুন",
                onClick = onNavigateToSettings
            ),
            MoreMenuItem(
                icon = Icons.Default.Info,
                title = "সম্পর্কে",
                subtitle = "Hisab v1.0 — অ্যাপ তথ্য",
                onClick = onNavigateToSettings
            )
        )
    }

    val filteredItems = remember(searchQuery, allMenuItems) {
        if (searchQuery.isBlank()) {
            allMenuItems
        } else {
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
        // ── Header ────────────────────────────────────────────────────
        Text(
            text = "আরও",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "অন্যান্য ব্যবস্থাপনা",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Search Bar ────────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "ফাংশন খুঁজুন…",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
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

        // ── Results count ─────────────────────────────────────────────
        if (searchQuery.isNotEmpty()) {
            Text(
                text = "${filteredItems.size}টি ফলাফল পাওয়া গেছে",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Grid ──────────────────────────────────────────────────────
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
            .clickable(onClick = item.onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
