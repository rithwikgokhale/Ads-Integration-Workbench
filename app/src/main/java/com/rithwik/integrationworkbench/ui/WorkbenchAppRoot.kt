package com.rithwik.integrationworkbench.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rithwik.integrationworkbench.ui.actions.ActionsScreen
import com.rithwik.integrationworkbench.ui.debug.DebugConsoleScreen
import com.rithwik.integrationworkbench.ui.insights.InsightsScreen
import com.rithwik.integrationworkbench.ui.integrations.IntegrationsScreen

sealed class NavItem(val route: String, val label: String) {
    data object Integrations : NavItem("integrations", "Integrations")
    data object Actions : NavItem("actions", "Actions")
    data object Debug : NavItem("debug", "Debug")
    data object Insights : NavItem("insights", "Insights")
}

@Composable
fun WorkbenchAppRoot() {
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
        startDestination = NavItem.Integrations.route
    ) {
        composable(NavItem.Integrations.route) { IntegrationsScreen(padding) }
        composable(NavItem.Actions.route) { ActionsScreen(padding) }
        composable(NavItem.Debug.route) { DebugConsoleScreen(padding) }
        composable(NavItem.Insights.route) { InsightsScreen(padding) }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val items = listOf(NavItem.Integrations, NavItem.Actions, NavItem.Debug, NavItem.Insights)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            val icon = when (item) {
                NavItem.Integrations -> Icons.Default.Extension
                NavItem.Actions -> Icons.Default.PlayArrow
                NavItem.Debug -> Icons.Default.BugReport
                NavItem.Insights -> Icons.Default.Insights
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
                icon = { Icon(icon, contentDescription = item.label) },
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
