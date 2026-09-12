package com.bypass.ai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bypass.ai.core.FileManager
import com.bypass.ai.ui.MainScreen
import com.bypass.ai.ui.TerminalScreen
import com.bypass.ai.ui.EditorScreen
import com.bypass.ai.ui.PreviewScreen
import com.bypass.ai.ui.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val fileManager = remember { FileManager(context) }
    
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController, fileManager) }
        composable("settings") { SettingsScreen(navController) }
    }
}
