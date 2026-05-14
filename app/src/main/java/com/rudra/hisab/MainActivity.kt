package com.rudra.hisab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.ui.navigation.BottomNavBar
import com.rudra.hisab.ui.navigation.Routes
import com.rudra.hisab.ui.navigation.bottomNavItems
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
import com.rudra.hisab.ui.screens.inventory.InventoryScreen
import com.rudra.hisab.ui.screens.inventory.InventoryViewModel
import com.rudra.hisab.ui.screens.onboarding.OnboardingScreen
import com.rudra.hisab.ui.screens.onboarding.OnboardingViewModel
import com.rudra.hisab.ui.screens.sale.SaleScreen
import com.rudra.hisab.ui.screens.sale.SaleViewModel
import com.rudra.hisab.ui.screens.settings.SettingsScreen
import com.rudra.hisab.ui.screens.settings.SettingsViewModel
import com.rudra.hisab.ui.theme.HisabTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by appPreferences.settings.collectAsState(initial = null)

            HisabTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (settings != null) {
                        val navController = rememberNavController()
                        val startDestination =
                            if (settings!!.hasCompletedOnboarding) Routes.DASHBOARD
                            else Routes.ONBOARDING

                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        val showBottomBar = bottomNavItems.any { it.route == currentRoute }

                        Scaffold(
                            bottomBar = {
                                if (showBottomBar) {
                                    BottomNavBar(
                                        currentRoute = currentRoute,
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
                            }
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = startDestination,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(Routes.ONBOARDING) {
                                    val viewModel: OnboardingViewModel = hiltViewModel()
                                    OnboardingScreen(
                                        viewModel = viewModel,
                                        onComplete = {
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
                                        }
                                    )
                                }

                                composable(Routes.INVENTORY) {
                                    val viewModel: InventoryViewModel = hiltViewModel()
                                    InventoryScreen(viewModel = viewModel)
                                }

                                composable(Routes.SALE) {
                                    val viewModel: SaleViewModel = hiltViewModel()
                                    SaleScreen(viewModel = viewModel)
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
                            }
                        }
                    }
                }
            }
        }
    }
}
