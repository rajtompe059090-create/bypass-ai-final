package com.bypass.ai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bypass.ai.core.AgentViewModel
import com.bypass.ai.ui.MainScreen
import com.bypass.ai.ui.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: AgentViewModel = viewModel()
    
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController, viewModel) }
        composable("settings") { SettingsScreen(navController) }
    }
}
