package com.tradedharma.app.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.tradedharma.app.data.local.DatabaseProvider
import com.tradedharma.app.domain.repository.TradeRepository
import com.tradedharma.app.ui.navigation.Routes
import com.tradedharma.app.ui.screens.addtrade.AddTradeScreen
import com.tradedharma.app.ui.screens.detail.TradeDetailScreen
import com.tradedharma.app.ui.screens.home.HomeScreen
import com.tradedharma.app.ui.screens.insights.InsightsScreen
import com.tradedharma.app.ui.screens.review.DailyReviewScreen
import com.tradedharma.app.ui.screens.review.WeeklyReviewScreen
import com.tradedharma.app.ui.screens.settings.SettingsScreen
import com.tradedharma.app.ui.screens.trades.TradesScreen
import com.tradedharma.app.ui.theme.ThemeMode
import com.tradedharma.app.ui.theme.TradeDharmaTheme

private const val PREFS = "tradedharma_prefs"
private const val THEME_KEY = "theme_mode"

@Composable
fun TradeDharmaApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var themeMode by remember {
        mutableStateOf(
            runCatching {
                ThemeMode.valueOf(prefs.getString(THEME_KEY, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM)
        )
    }

    val view = LocalView.current
    SideEffect {
        val activity = view.context as? Activity
        if (activity != null) {
            val window = activity.window
            val systemDark = isSystemInDarkTheme()
            val lightBars = themeMode == ThemeMode.LIGHT || (themeMode == ThemeMode.SYSTEM && !systemDark)
            val barColor = when {
                themeMode == ThemeMode.AMOLED_BLACK -> AndroidColor.BLACK
                lightBars -> AndroidColor.rgb(247, 248, 250)
                else -> AndroidColor.rgb(18, 20, 22)
            }
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
            window.decorView.systemUiVisibility = if (lightBars) {
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else 0
        }
    }

    TradeDharmaTheme(themeMode) {
        TradeDharmaContent(
            themeMode = themeMode,
            onThemeChange = { mode ->
                themeMode = mode
                prefs.edit().putString(THEME_KEY, mode.name).apply()
            }
        )
    }
}

private data class MainNavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun TradeDharmaContent(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val repository = remember { TradeRepository(DatabaseProvider.get(context)) }
    LaunchedEffect(Unit) { repository.seedCatalogDefaults() }

    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val mainItems = remember {
        listOf(
            MainNavItem(Routes.HOME, "Home", Icons.Default.Home),
            MainNavItem(Routes.TRADES, "Trades", Icons.Default.SwapHoriz),
            MainNavItem(Routes.INSIGHTS, "Insights", Icons.Default.BarChart),
            MainNavItem(Routes.MORE, "More", Icons.Default.MoreHoriz)
        )
    }
    val showMainChrome = route in mainItems.map { it.route }

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize().padding(bottom = if (showMainChrome) 88.dp else 0.dp)
        ) {
            composable(Routes.HOME) { HomeScreen(repository) { navController.navigate(Routes.add()) } }
            composable(Routes.TRADES) { TradesScreen(repository) { navController.navigate(Routes.detail(it)) } }
            composable(Routes.INSIGHTS) { InsightsScreen(repository) }
            composable(Routes.MORE) {
                SettingsScreen(
                    repository = repository,
                    onDailyReview = { navController.navigate(Routes.DAILY_REVIEW) },
                    onWeeklyReview = { navController.navigate(Routes.WEEKLY_REVIEW) },
                    themeMode = themeMode,
                    onThemeChange = onThemeChange
                )
            }
            composable(Routes.ADD, arguments = listOf(navArgument("tradeId") { type = NavType.LongType; defaultValue = -1L })) { entry ->
                val id = entry.arguments?.getLong("tradeId")?.takeIf { it >= 0 }
                AddTradeScreen(repository, id, { navController.popBackStack() }, { navController.popBackStack() })
            }
            composable(Routes.DETAIL, arguments = listOf(navArgument("tradeId") { type = NavType.LongType })) { entry ->
                val id = entry.arguments?.getLong("tradeId") ?: return@composable
                TradeDetailScreen(
                    repository = repository,
                    tradeId = id,
                    onEdit = { navController.navigate(Routes.add(id)) },
                    onBack = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() }
                )
            }
            composable(Routes.DAILY_REVIEW) { DailyReviewScreen(repository) { navController.popBackStack() } }
            composable(Routes.WEEKLY_REVIEW) { WeeklyReviewScreen(repository) { navController.popBackStack() } }
        }

        if (showMainChrome) {
            FloatingNavigationDock(
                items = mainItems,
                selectedRoute = route ?: Routes.HOME,
                onSelect = { target -> navigateMain(navController, target) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
            )
            FloatingActionButton(
                onClick = { navController.navigate(Routes.add()) },
                shape = CircleShape,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 88.dp)
            ) { Icon(Icons.Default.Add, contentDescription = "Add trade") }
        }
    }
}

private fun navigateMain(navController: NavHostController, target: String) {
    navController.navigate(target) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun FloatingNavigationDock(
    items: List<MainNavItem>,
    selectedRoute: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .wrapContentWidth()
            .shadow(14.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        tonalElevation = 5.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp).animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item.route == selectedRoute
                Surface(
                    onClick = { onSelect(item.route) },
                    modifier = Modifier
                        .height(52.dp)
                        .semantics { contentDescription = item.label },
                    shape = RoundedCornerShape(24.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    tonalElevation = if (selected) 2.dp else 0.dp
                ) {
                    AnimatedContent(targetState = selected, label = "navigation item") { active ->
                        Row(
                            modifier = Modifier
                                .padding(horizontal = if (active) 16.dp else 14.dp)
                                .animateContentSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (active) {
                                Spacer(Modifier.width(8.dp))
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Text(
                                        text = item.label,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.labelLarge
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
