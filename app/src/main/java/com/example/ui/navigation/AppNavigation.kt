package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AIPredictionScreen
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AuthContainerScreen
import com.example.ui.screens.BrokerConnectScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MarketMoversScreen
import com.example.ui.screens.MutualFundsScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.PaperTradingScreen
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.PriceAlertsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecommendationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StockDetailScreen
import com.example.ui.screens.StockSearchScreen
import com.example.ui.screens.TechnicalChartScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.screens.WhatsAppNotificationScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.SleekLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Watchlist : Screen("watchlist", "Watchlist", Icons.Default.Star)
    object Orders : Screen("orders", "Orders", Icons.AutoMirrored.Filled.ReceiptLong)
    object Portfolio : Screen("portfolio", "Portfolio", Icons.Default.PieChart)
    object Account : Screen("account", "Account", Icons.Default.Person)

    object Search : Screen("search", "Markets", Icons.Default.Search)
    object PaperTrading : Screen("paper_trading", "Paper", Icons.Default.AccountBalanceWallet)
    object News : Screen("news", "News", Icons.Default.Newspaper)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    object MutualFunds : Screen("mutual_funds", "Mutual Funds")
    object MarketMovers : Screen("market_movers", "Market Movers")

    object StockDetail : Screen("stock_detail/{symbol}") {
        fun createRoute(symbol: String) = "stock_detail/$symbol"
    }

    object TechnicalChart : Screen("technical_chart/{symbol}") {
        fun createRoute(symbol: String) = "technical_chart/$symbol"
    }

    object AIPrediction : Screen("ai_prediction/{symbol}") {
        fun createRoute(symbol: String) = "ai_prediction/$symbol"
    }

    object AIRecommendation : Screen("ai_recommendation/{symbol}") {
        fun createRoute(symbol: String) = "ai_recommendation/$symbol"
    }

    object PriceAlerts : Screen("price_alerts")
    object WhatsAppLogs : Screen("whatsapp_logs")
    object Settings : Screen("settings")
    object BrokerConnect : Screen("broker_connect", "Connect Broker")
}

@Composable
fun AppNavigation(
    viewModel: TradingViewModel,
    navController: NavHostController = rememberNavController()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val bottomNavScreens = listOf(
        Screen.Home,
        Screen.Watchlist,
        Screen.Orders,
        Screen.Portfolio,
        Screen.Account
    )

    val showBottomNav = isLoggedIn && bottomNavScreens.any { it.route == currentRoute }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && currentRoute == Screen.Auth.route) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Auth.route) { inclusive = true }
            }
        } else if (!isLoggedIn && currentRoute != null && currentRoute != Screen.Auth.route) {
            navController.navigate(Screen.Auth.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomNav) {
                Surface(
                    color = NavySurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.height(72.dp)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        contentColor = TextPrimary,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        bottomNavScreens.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    screen.icon?.let { icon ->
                                        Box(
                                            modifier = Modifier.size(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = screen.title,
                                                tint = if (selected) SleekLavender else TextSecondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) SleekLavender else TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                },
                                alwaysShowLabel = true,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = SleekLavender.copy(alpha = 0.18f)
                                ),
                                modifier = Modifier.testTag("nav_item_${screen.route}")
                            )
                        }
                    }
                }
            }
        },
        containerColor = NavyDeep
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Auth.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Auth.route) {
                AuthContainerScreen(
                    viewModel = viewModel,
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToBrokerConnect = { navController.navigate(Screen.BrokerConnect.route) }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToStockDetail = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.StockDetail.createRoute(symbol))
                    },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToPrediction = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.AIPrediction.createRoute(symbol))
                    },
                    onNavigateToPaperTrading = { navController.navigate(Screen.PaperTrading.route) },
                    onNavigateToChart = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.TechnicalChart.createRoute(symbol))
                    },
                    onNavigateToRecommendation = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.AIRecommendation.createRoute(symbol))
                    },
                    onNavigateToWhatsAppLogs = { navController.navigate(Screen.WhatsAppLogs.route) },
                    onNavigateToAlerts = { navController.navigate(Screen.PriceAlerts.route) },
                    onNavigateToMutualFunds = { navController.navigate(Screen.MutualFunds.route) },
                    onNavigateToMarketMovers = { navController.navigate(Screen.MarketMovers.route) },
                    onNavigateToBrokerConnect = { navController.navigate(Screen.BrokerConnect.route) },
                    onNavigateToNews = { navController.navigate(Screen.News.route) }
                )
            }

            composable(Screen.MutualFunds.route) {
                MutualFundsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPortfolio = { navController.navigate(Screen.Portfolio.route) }
                )
            }

            composable(Screen.MarketMovers.route) {
                MarketMoversScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToStockDetail = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.StockDetail.createRoute(symbol))
                    },
                    onNavigateToChart = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.TechnicalChart.createRoute(symbol))
                    },
                    onNavigateToPrediction = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.AIPrediction.createRoute(symbol))
                    }
                )
            }

            composable(Screen.Search.route) {
                StockSearchScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToStockDetail = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.StockDetail.createRoute(symbol))
                    }
                )
            }

            composable(Screen.PaperTrading.route) {
                PaperTradingScreen(
                    viewModel = viewModel,
                    onNavigateToStockDetail = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.StockDetail.createRoute(symbol))
                    }
                )
            }

            composable(Screen.Orders.route) {
                OrdersScreen(
                    viewModel = viewModel,
                    onNavigateToStockDetail = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.StockDetail.createRoute(symbol))
                    }
                )
            }

            composable(Screen.Account.route) {
                AccountScreen(
                    viewModel = viewModel,
                    onNavigateToBrokerConnect = { navController.navigate(Screen.BrokerConnect.route) },
                    onNavigateToAlerts = { navController.navigate(Screen.PriceAlerts.route) },
                    onNavigateToAuth = {
                        viewModel.logout {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Portfolio.route) {
                PortfolioScreen(
                    viewModel = viewModel,
                    onNavigateToStockDetail = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.StockDetail.createRoute(symbol))
                    },
                    onNavigateToBrokerConnect = {
                        navController.navigate(Screen.BrokerConnect.route)
                    }
                )
            }

            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    viewModel = viewModel,
                    onNavigateToStockDetail = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.StockDetail.createRoute(symbol))
                    },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                )
            }

            composable(Screen.News.route) {
                NewsScreen(
                    viewModel = viewModel,
                    onNavigateToStockDetail = { symbol ->
                        viewModel.selectStockBySymbol(symbol)
                        navController.navigate(Screen.StockDetail.createRoute(symbol))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToBrokerConnect = { navController.navigate(Screen.BrokerConnect.route) },
                    onNavigateToAuth = {
                        viewModel.logout {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.BrokerConnect.route) {
                BrokerConnectScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = Screen.StockDetail.route,
                arguments = listOf(navArgument("symbol") { type = NavType.StringType })
            ) { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol") ?: "RELIANCE"
                StockDetailScreen(
                    viewModel = viewModel,
                    symbol = symbol,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChart = { s -> navController.navigate(Screen.TechnicalChart.createRoute(s)) },
                    onNavigateToPrediction = { s -> navController.navigate(Screen.AIPrediction.createRoute(s)) },
                    onNavigateToRecommendation = { s -> navController.navigate(Screen.AIRecommendation.createRoute(s)) }
                )
            }

            composable(
                route = Screen.TechnicalChart.route,
                arguments = listOf(navArgument("symbol") { type = NavType.StringType })
            ) { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol") ?: "RELIANCE"
                TechnicalChartScreen(
                    viewModel = viewModel,
                    symbol = symbol,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.AIPrediction.route,
                arguments = listOf(navArgument("symbol") { type = NavType.StringType })
            ) { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol") ?: "RELIANCE"
                AIPredictionScreen(
                    viewModel = viewModel,
                    symbol = symbol,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.AIRecommendation.route,
                arguments = listOf(navArgument("symbol") { type = NavType.StringType })
            ) { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol") ?: "RELIANCE"
                RecommendationScreen(
                    viewModel = viewModel,
                    symbol = symbol,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPaperTrading = { navController.navigate(Screen.PaperTrading.route) }
                )
            }

            composable(Screen.PriceAlerts.route) {
                PriceAlertsScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                )
            }

            composable(Screen.WhatsAppLogs.route) {
                WhatsAppNotificationScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBrokerConnect = { navController.navigate(Screen.BrokerConnect.route) }
                )
            }
        }
    }
}
