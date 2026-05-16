package com.rudra.hisab.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Data model ───────────────────────────────────────────────────────────────

data class MoreMenuItem(
    val key: String,
    val icon: ImageVector,
    val color: MenuColor,
    val onClick: () -> Unit
)

data class MoreMenuSection(
    val key: String,
    val items: List<MoreMenuItem>
)

enum class MenuColor { Blue, Teal, Amber, Purple, Coral, Pink, Green, Gray }

// ─── Localisation ─────────────────────────────────────────────────────────────

private val EN = mapOf(
    // page
    "pageTitle"    to "More",
    "pageSubtitle" to "All features & settings",
    "search"       to "Search features…",
    "noResults"    to "No results found",
    // section headers
    "sec_main"     to "Main Screens",
    "sec_finance"  to "Finance & Reports",
    "sec_inventory" to "Inventory & Sales",
    "sec_settings" to "Settings",
    "sec_data"     to "Data & Backup",
    "sec_info"     to "About",
    // item titles + subtitles
    "dashboard"    to "Dashboard",        "dashboard_s"    to "Overview & summary",
    "sale"         to "Sale",             "sale_s"         to "Record new sale",
    "customers"    to "Customers",        "customers_s"    to "Customer list & dues",
    "expenses"     to "Expenses",         "expenses_s"     to "Daily expenses",
    "inventory"    to "Inventory",        "inventory_s"    to "Products & stock",
    "accounting"   to "Accounting",       "accounting_s"   to "Cash book & ledger",
    "analytics"    to "Analytics",        "analytics_s"    to "Sales charts & trends",
    "reports"      to "Reports",          "reports_s"      to "Detailed reports & PDF",
    "dailyclose"   to "Daily Close",      "dailyclose_s"   to "End of day closing",
    "batchtracking" to "Batch Tracking",  "batchtracking_s" to "Expiry & batch tracking",
    "creditlimit"  to "Credit Limit",     "creditlimit_s"  to "Max credit per customer",
    "settings"     to "Settings",         "settings_s"     to "App preferences",
    "language"     to "Language & Theme", "language_s"     to "English / বাংলা, Light / Dark",
    "shopinfo"     to "Shop Info",        "shopinfo_s"     to "Shop name & address",
    "pin"          to "PIN & Biometric",  "pin_s"          to "App lock setup",
    "notifications" to "Notifications",  "notifications_s" to "Sale & report reminders",
    "cartmode"     to "Cart & FAB Mode",  "cartmode_s"     to "Sales cart & quick button",
    "deletewindow" to "Delete Window",    "deletewindow_s" to "Transaction delete window",
    "navorder"     to "Nav Order",        "navorder_s"     to "Customize bottom menu",
    "quickactions" to "Quick Actions",    "quickactions_s" to "Dashboard shortcuts",
    "export"       to "Backup & Export",  "export_s"       to "JSON / CSV export",
    "import"       to "Data Import",      "import_s"       to "Restore from JSON / CSV",
    "cleardata"    to "Clear All Data",   "cleardata_s"    to "Delete all app data",
    "about"        to "About Hisab",      "about_s"        to "v2.0 — App info & licenses"
)

private val BN = mapOf(
    "pageTitle"    to "আরও",
    "pageSubtitle" to "সব ফিচার ও সেটিংস",
    "search"       to "ফিচার খুঁজুন…",
    "noResults"    to "কোনো ফলাফল পাওয়া যায়নি",
    "sec_main"     to "প্রধান স্ক্রিন",
    "sec_finance"  to "ফাইন্যান্স ও রিপোর্ট",
    "sec_inventory" to "ইনভেন্টরি ও বিক্রয়",
    "sec_settings" to "সেটিংস",
    "sec_data"     to "ডেটা ও ব্যাকআপ",
    "sec_info"     to "অ্যাপ তথ্য",
    "dashboard"    to "ড্যাশবোর্ড",        "dashboard_s"    to "সারসংক্ষেপ ও ওভারভিউ",
    "sale"         to "বিক্রয়",            "sale_s"         to "নতুন বিক্রয় যোগ করুন",
    "customers"    to "কাস্টমার",          "customers_s"    to "তালিকা ও বাকি হিসাব",
    "expenses"     to "খরচ",              "expenses_s"     to "দৈনিক খরচের হিসাব",
    "inventory"    to "ইনভেন্টরি",         "inventory_s"    to "পণ্য ও স্টক ব্যবস্থাপনা",
    "accounting"   to "অ্যাকাউন্টিং",      "accounting_s"   to "ক্যাশ বুক ও লেজার",
    "analytics"    to "অ্যানালিটিক্স",      "analytics_s"    to "বিক্রয় চার্ট ও ট্রেন্ড",
    "reports"      to "রিপোর্ট",           "reports_s"      to "বিস্তারিত রিপোর্ট ও PDF",
    "dailyclose"   to "দৈনিক বন্ধ",        "dailyclose_s"   to "দিনের শেষ হিসাব",
    "batchtracking" to "ব্যাচ ট্র্যাকিং",   "batchtracking_s" to "মেয়াদ ও ব্যাচ ট্র্যাকিং",
    "creditlimit"  to "ক্রেডিট সীমা",      "creditlimit_s"  to "প্রতি কাস্টমারের সর্বোচ্চ বাকি",
    "settings"     to "সেটিংস",            "settings_s"     to "অ্যাপ পছন্দ সমূহ",
    "language"     to "ভাষা ও থিম",        "language_s"     to "বাংলা / English, আলো / অন্ধকার",
    "shopinfo"     to "দোকানের তথ্য",       "shopinfo_s"     to "দোকানের নাম ও ঠিকানা",
    "pin"          to "PIN ও বায়োমেট্রিক",  "pin_s"          to "অ্যাপ লক সেটআপ",
    "notifications" to "নোটিফিকেশন",       "notifications_s" to "বিক্রয় ও রিপোর্ট রিমাইন্ডার",
    "cartmode"     to "কার্ট ও FAB মোড",   "cartmode_s"     to "বিক্রয় কার্ট ও কুইক বাটন",
    "deletewindow" to "মুছার সময়সীমা",     "deletewindow_s" to "লেনদেন মুছার উইন্ডো",
    "navorder"     to "নেভিগেশন অর্ডার",    "navorder_s"     to "নিচের মেনু সাজান",
    "quickactions" to "দ্রুত অ্যাকশন",      "quickactions_s" to "ড্যাশবোর্ড শর্টকাট",
    "export"       to "ব্যাকআপ ও রপ্তানি",  "export_s"       to "JSON / CSV এক্সপোর্ট",
    "import"       to "ডেটা আমদানি",        "import_s"       to "JSON / CSV থেকে পুনরুদ্ধার",
    "cleardata"    to "সব ডেটা মুছুন",      "cleardata_s"    to "সব অ্যাপ ডেটা মুছে দিন",
    "about"        to "হিসাব সম্পর্কে",      "about_s"        to "v2.0 — অ্যাপ তথ্য ও লাইসেন্স"
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun MoreScreen(
    isBangla: Boolean,                     // hoisted from app-level language state
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
    val str = if (isBangla) BN else EN

    // ── Section definitions ──────────────────────────────────────────────────
    val sections = remember(isBangla) {
        listOf(
            MoreMenuSection("sec_main", listOf(
                MoreMenuItem("dashboard",    Icons.Default.Description,     MenuColor.Blue,   onNavigateToDashboard),
                MoreMenuItem("sale",         Icons.Default.PointOfSale,     MenuColor.Teal,   onNavigateToSale),
                MoreMenuItem("customers",    Icons.Default.People,          MenuColor.Purple, onNavigateToCustomers),
                MoreMenuItem("expenses",     Icons.Default.ShoppingCart,    MenuColor.Coral,  onNavigateToExpenses),
            )),
            MoreMenuSection("sec_finance", listOf(
                MoreMenuItem("accounting",   Icons.Default.AccountBalance,  MenuColor.Blue,   onNavigateToAccounting),
                MoreMenuItem("analytics",    Icons.Default.BarChart,        MenuColor.Teal,   onNavigateToAnalytics),
                MoreMenuItem("reports",      Icons.Default.ReceiptLong,     MenuColor.Amber,  onNavigateToReports),
                MoreMenuItem("dailyclose",   Icons.Default.CalendarMonth,   MenuColor.Purple, onNavigateToDailyClose),
            )),
            MoreMenuSection("sec_inventory", listOf(
                MoreMenuItem("inventory",    Icons.Default.Inventory2,      MenuColor.Green,  onNavigateToInventory),
                MoreMenuItem("batchtracking",Icons.Default.Timer,           MenuColor.Amber,  onNavigateToSettings),
                MoreMenuItem("creditlimit",  Icons.Default.Lock,            MenuColor.Coral,  onNavigateToSettings),
            )),
            MoreMenuSection("sec_settings", listOf(
                MoreMenuItem("language",     Icons.Default.Language,        MenuColor.Blue,   onNavigateToSettings),
                MoreMenuItem("shopinfo",     Icons.Default.Store,           MenuColor.Teal,   onNavigateToSettings),
                MoreMenuItem("pin",          Icons.Default.Fingerprint,     MenuColor.Purple, onNavigateToSettings),
                MoreMenuItem("notifications",Icons.Default.Notifications,   MenuColor.Amber,  onNavigateToSettings),
                MoreMenuItem("cartmode",     Icons.Default.ShoppingCart,    MenuColor.Coral,  onNavigateToSettings),
                MoreMenuItem("deletewindow", Icons.Default.Delete,          MenuColor.Pink,   onNavigateToSettings),
                MoreMenuItem("navorder",     Icons.Default.Description,     MenuColor.Gray,   onNavigateToSettings),
                MoreMenuItem("quickactions", Icons.Default.TrendingUp,      MenuColor.Amber,  onNavigateToSettings),
            )),
            MoreMenuSection("sec_data", listOf(
                MoreMenuItem("export",       Icons.Default.CloudUpload,     MenuColor.Teal,   onNavigateToExport),
                MoreMenuItem("import",       Icons.Default.FileDownload,    MenuColor.Blue,   onNavigateToExport),
                MoreMenuItem("cleardata",    Icons.Default.Delete,          MenuColor.Coral,  onNavigateToSettings),
            )),
            MoreMenuSection("sec_info", listOf(
                MoreMenuItem("about",        Icons.Default.Info,            MenuColor.Gray,   onNavigateToSettings),
            )),
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(searchQuery, sections, isBangla) {
        val q = searchQuery.lowercase().trim()
        if (q.isBlank()) sections
        else sections.mapNotNull { sec ->
            val matchedItems = sec.items.filter { item ->
                val title = (str[item.key] ?: "").lowercase()
                val sub   = (str["${item.key}_s"] ?: "").lowercase()
                title.contains(q) || sub.contains(q)
            }
            if (matchedItems.isEmpty()) null else sec.copy(items = matchedItems)
        }
    }

    val totalVisible = filtered.sumOf { it.items.size }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top bar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = str["pageTitle"] ?: "",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = str["pageSubtitle"] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Language toggle — wired to your app-level language setting
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 0.dp
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    // These chips just navigate to Settings → Language section
                    // so user can switch there (keeps single source of truth)
                    LangChip("EN", selected = !isBangla) { onNavigateToSettings() }
                    LangChip("বাং", selected = isBangla)  { onNavigateToSettings() }
                }
            }
        }

        // ── Search ──────────────────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(str["search"] ?: "") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clickable { searchQuery = "" }
                            .padding(4.dp)
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (searchQuery.isNotEmpty()) {
            Text(
                text = if (isBangla) "${totalVisible}টি ফলাফল পাওয়া গেছে"
                else "$totalVisible result${if (totalVisible != 1) "s" else ""} found",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Content ─────────────────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = str["noResults"] ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 24.dp
                )
            ) {
                filtered.forEach { section ->
                    item {
                        Text(
                            text = (str[section.key] ?: "").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.08.sp,
                            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp, start = 4.dp)
                        )
                    }
                    // Two-column grid via chunked rows
                    val rows = section.items.chunked(2)
                    items(rows) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { item ->
                                MoreGridCard(
                                    item = item,
                                    title = str[item.key] ?: item.key,
                                    subtitle = str["${item.key}_s"] ?: "",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty slot in last odd row
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

// ─── Sub-components ───────────────────────────────────────────────────────────

@Composable
private fun LangChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(17.dp),
        color = if (selected) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (selected) 1.dp else 0.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun MoreGridCard(
    item: MoreMenuItem,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val iconBg = when (item.color) {
        MenuColor.Blue   -> MaterialTheme.colorScheme.primaryContainer
        MenuColor.Teal   -> MaterialTheme.colorScheme.secondaryContainer
        MenuColor.Amber  -> MaterialTheme.colorScheme.tertiaryContainer
        MenuColor.Purple -> MaterialTheme.colorScheme.primaryContainer
        MenuColor.Coral  -> MaterialTheme.colorScheme.errorContainer
        MenuColor.Pink   -> MaterialTheme.colorScheme.secondaryContainer
        MenuColor.Green  -> MaterialTheme.colorScheme.tertiaryContainer
        MenuColor.Gray   -> MaterialTheme.colorScheme.surfaceVariant
    }
    val iconTint = when (item.color) {
        MenuColor.Blue   -> MaterialTheme.colorScheme.primary
        MenuColor.Teal   -> MaterialTheme.colorScheme.secondary
        MenuColor.Amber  -> MaterialTheme.colorScheme.tertiary
        MenuColor.Purple -> MaterialTheme.colorScheme.primary
        MenuColor.Coral  -> MaterialTheme.colorScheme.error
        MenuColor.Pink   -> MaterialTheme.colorScheme.secondary
        MenuColor.Green  -> MaterialTheme.colorScheme.tertiary
        MenuColor.Gray   -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.clickable(onClick = item.onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}