package com.rudra.hisab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.ui.navigation.BottomNavBar
import com.rudra.hisab.ui.navigation.Routes
import com.rudra.hisab.ui.navigation.allNavItems
import com.rudra.hisab.ui.screens.accounting.AccountingScreen
import com.rudra.hisab.ui.screens.accounting.AccountingViewModel
import com.rudra.hisab.ui.screens.analytics.AnalyticsScreen
import com.rudra.hisab.ui.screens.analytics.AnalyticsViewModel
import com.rudra.hisab.ui.screens.customer.CustomerDetailScreen
import com.rudra.hisab.ui.screens.customer.CustomerListScreen
import com.rudra.hisab.ui.screens.customer.CustomerViewModel
import com.rudra.hisab.ui.screens.dailyclose.DailyCloseScreen
import com.rudra.hisab.ui.screens.dailyclose.DailyCloseViewModel
import com.rudra.hisab.ui.screens.dashboard.DashboardScreen
import com.rudra.hisab.ui.screens.dashboard.DashboardViewModel
import com.rudra.hisab.ui.screens.expense.ExpenseScreen
import com.rudra.hisab.ui.screens.expense.ExpenseViewModel
import com.rudra.hisab.ui.screens.export.ExportScreen
import com.rudra.hisab.ui.screens.export.ExportViewModel
import com.rudra.hisab.ui.screens.inventory.InventoryScreen
import com.rudra.hisab.ui.screens.inventory.InventoryViewModel
import com.rudra.hisab.ui.screens.lock.LockScreen
import com.rudra.hisab.ui.screens.more.MoreScreen
import com.rudra.hisab.ui.screens.onboarding.OnboardingScreen
import com.rudra.hisab.ui.screens.onboarding.OnboardingViewModel
import com.rudra.hisab.ui.screens.reports.ReportsScreen
import com.rudra.hisab.ui.screens.reports.ReportsViewModel
import com.rudra.hisab.ui.screens.sale.QuickSaleScreen
import com.rudra.hisab.ui.screens.sale.QuickSaleViewModel
import com.rudra.hisab.ui.screens.settings.SettingsScreen
import com.rudra.hisab.ui.screens.settings.SettingsViewModel
import com.rudra.hisab.ui.screens.splash.SplashScreen
import com.rudra.hisab.ui.theme.HisabTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deepLinkTarget = intent?.data?.lastPathSegment

        setContent {
            val settings by appPreferences.settings.collectAsState(initial = null)
            val context = LocalContext.current
            val backgroundTime = remember { mutableLongStateOf(0L) }

            LaunchedEffect(settings?.isBangla) {
                if (settings != null) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(if (settings!!.isBangla) "bn" else "en")
                    )
                }
            }

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            LaunchedEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> backgroundTime.longValue = System.currentTimeMillis()
                        Lifecycle.Event.ON_RESUME -> {
                            backgroundTime.longValue = 0
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
            }

            HisabTheme(
                themeMode = settings?.themeMode ?: "system",
                fontSize = settings?.fontSize ?: "medium"
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (settings != null) {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        val showBottomBar = if (settings!!.fabModeEnabled) {
                            allNavItems.any { it.route == currentRoute } && currentRoute != Routes.SALE
                        } else {
                            allNavItems.any { it.route == currentRoute }
                        }
                        val isFabMode = settings!!.fabModeEnabled

                        Scaffold(
                            bottomBar = {
                                if (showBottomBar) {
                                    BottomNavBar(
                                        currentRoute = currentRoute,
                                        navOrder = settings!!.navOrder,
                                        isFabMode = isFabMode,
                                        onNavigate = { route ->
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            },
                            floatingActionButton = {
                                if (isFabMode && currentRoute != Routes.SALE && showBottomBar) {
                                    FloatingActionButton(
                                        onClick = {
                                            navController.navigate(Routes.SALE) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "বিক্রয়")
                                    }
                                }
                            }
                        ) { innerPadding ->
                            LaunchedEffect(deepLinkTarget) {
                                if (deepLinkTarget != null) {
                                    val route = when (deepLinkTarget) {
                                        "sale" -> Routes.SALE
                                        "inventory" -> Routes.INVENTORY
                                        "customers" -> Routes.CUSTOMERS
                                        "expenses" -> Routes.EXPENSES
                                        "analytics" -> Routes.ANALYTICS
                                        "settings" -> Routes.SETTINGS
                                        "daily_close" -> Routes.DAILY_CLOSE
                                        else -> null
                                    }
                                    if (route != null) {
                                        navController.navigate(route) {
                                            popUpTo(Routes.DASHBOARD)
                                        }
                                    }
                                }
                            }
                            NavHost(
                                navController = navController,
                                startDestination = Routes.SPLASH,
                                modifier = Modifier.padding(innerPadding)
                            ) {

                                composable(Routes.SPLASH) {
                                    SplashScreen(
                                        settings = settings!!,
                                        onNavigateToOnboarding = {
                                            navController.navigate(Routes.ONBOARDING) {
                                                popUpTo(Routes.SPLASH) { inclusive = true }
                                            }
                                        },
                                        onNavigateToLock = {
                                            navController.navigate(Routes.LOCK_SCREEN) {
                                                popUpTo(Routes.SPLASH) { inclusive = true }
                                            }
                                        },
                                        onNavigateToDashboard = {
                                            navController.navigate(Routes.DASHBOARD) {
                                                popUpTo(Routes.SPLASH) { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable(Routes.LOCK_SCREEN) {
                                    LockScreen(
                                        settings = settings!!,
                                        onUnlock = {
                                            navController.navigate(Routes.DASHBOARD) {
                                                popUpTo(Routes.LOCK_SCREEN) { inclusive = true }
                                            }
                                        },
                                        onExitApp = {
                                            (context as? Activity)?.finish()
                                        }
                                    )
                                }

                                composable(Routes.ONBOARDING) {
                                    val viewModel: OnboardingViewModel = hiltViewModel()
                                    OnboardingScreen(
                                        viewModel = viewModel,
                                        onNavigateToHome = {
                                            navController.navigate(Routes.DASHBOARD) {
                                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable(Routes.DASHBOARD) {
                                    val viewModel: DashboardViewModel = hiltViewModel()
                                    DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToSale = {
                                            navController.navigate(Routes.SALE) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToInventory = {
                                            navController.navigate(Routes.INVENTORY) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToExpenses = {
                                            navController.navigate(Routes.EXPENSES)
                                        },
                                        onNavigateToCustomers = {
                                            navController.navigate(Routes.CUSTOMERS) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToAddStock = {
                                            navController.navigate(Routes.INVENTORY)
                                        }
                                    )
                                }

                                composable(Routes.INVENTORY) {
                                    val viewModel: InventoryViewModel = hiltViewModel()
                                    InventoryScreen(viewModel = viewModel)
                                }

                                composable(Routes.SALE) {
                                    val viewModel: QuickSaleViewModel = hiltViewModel()
                                    QuickSaleScreen(viewModel = viewModel)
                                }

                                composable(Routes.CUSTOMERS) {
                                    val viewModel: CustomerViewModel = hiltViewModel()
                                    CustomerListScreen(
                                        viewModel = viewModel,
                                        onCustomerClick = { id ->
                                            navController.navigate(Routes.customerDetail(id))
                                        }
                                    )
                                }

                                composable(
                                    route = Routes.CUSTOMER_DETAIL,
                                    arguments = listOf(navArgument("customerId") { type = NavType.LongType })
                                ) { backStackEntry ->
                                    val customerId = backStackEntry.arguments?.getLong("customerId") ?: return@composable
                                    val viewModel: CustomerViewModel = hiltViewModel()
                                    CustomerDetailScreen(
                                        viewModel = viewModel,
                                        customerId = customerId,
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable(Routes.EXPENSES) {
                                    val viewModel: ExpenseViewModel = hiltViewModel()
                                    ExpenseScreen(viewModel = viewModel)
                                }

                                composable(Routes.DAILY_CLOSE) {
                                    val viewModel: DailyCloseViewModel = hiltViewModel()
                                    DailyCloseScreen(viewModel = viewModel)
                                }

                                composable(Routes.SETTINGS) {
                                    val viewModel: SettingsViewModel = hiltViewModel()
                                    SettingsScreen(viewModel = viewModel)
                                }

                                composable(Routes.ANALYTICS) {
                                    val viewModel: AnalyticsViewModel = hiltViewModel()
                                    AnalyticsScreen(viewModel = viewModel)
                                }

                                composable(Routes.ACCOUNTING) {
                                    val viewModel: AccountingViewModel = hiltViewModel()
                                    AccountingScreen(viewModel = viewModel)
                                }

                                composable(Routes.REPORTS) {
                                    val viewModel: ReportsViewModel = hiltViewModel()
                                    ReportsScreen(viewModel = viewModel)
                                }

                                composable(Routes.EXPORT) {
                                    val viewModel: ExportViewModel = hiltViewModel()
                                    ExportScreen(viewModel = viewModel)
                                }

                                composable(Routes.MORE) {
                                    MoreScreen(
                                        onNavigateToDashboard = {
                                            navController.navigate(Routes.DASHBOARD) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToInventory = {
                                            navController.navigate(Routes.INVENTORY) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToSale = {
                                            navController.navigate(Routes.SALE) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToCustomers = {
                                            navController.navigate(Routes.CUSTOMERS) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToExpenses = {
                                            navController.navigate(Routes.EXPENSES)
                                        },
                                        onNavigateToDailyClose = {
                                            navController.navigate(Routes.DAILY_CLOSE)
                                        },
                                        onNavigateToAnalytics = {
                                            navController.navigate(Routes.ANALYTICS)
                                        },
                                        onNavigateToSettings = {
                                            navController.navigate(Routes.SETTINGS)
                                        },
                                        onNavigateToAccounting = {
                                            navController.navigate(Routes.ACCOUNTING)
                                        },
                                        onNavigateToReports = {
                                            navController.navigate(Routes.REPORTS)
                                        },
                                        onNavigateToExport = {
                                            navController.navigate(Routes.EXPORT)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
