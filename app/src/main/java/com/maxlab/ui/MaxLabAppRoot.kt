package com.maxlab.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maxlab.ui.debug.DebugConsoleScreen
import com.maxlab.ui.home.HomeScreen
import com.maxlab.ui.insights.InsightsScreen
import com.maxlab.ui.issue.IssueReproScreen
import com.maxlab.ui.setup.SetupScreen

sealed class NavItem(val route: String, val label: String) {
    data object Home : NavItem("home", "Home")
    data object Debug : NavItem("debug", "Debug")
    data object Insights : NavItem("insights", "Insights")
    data object Issue : NavItem("issue", "Issue Repro")
}

@Composable
fun MaxLabAppRoot() {
    val mainViewModel: MainViewModel = hiltViewModel()
    val setupState by mainViewModel.setupUiState.collectAsState()
    if (setupState.needsSetup && !setupState.setupDismissed) {
        SetupScreen(onContinue = { mainViewModel.dismissSetup() })
        return
    }
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavHost(navController = navController, padding = padding)
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = NavItem.Home.route
    ) {
        composable(NavItem.Home.route) { HomeScreen(padding) }
        composable(NavItem.Debug.route) { DebugConsoleScreen(padding) }
        composable(NavItem.Insights.route) { InsightsScreen(padding) }
        composable(NavItem.Issue.route) { IssueReproScreen(padding) }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val items = listOf(NavItem.Home, NavItem.Debug, NavItem.Insights, NavItem.Issue)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            val icon = when (item) {
                NavItem.Home -> Icons.Default.Home
                NavItem.Debug -> Icons.Default.BugReport
                NavItem.Insights -> Icons.Default.Insights
                NavItem.Issue -> Icons.Default.Tune
            }
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { androidx.compose.material3.Icon(icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
