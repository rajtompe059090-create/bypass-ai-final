package com.bypass.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bypass.ai.core.FileManager
import com.bypass.ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(rootNavController: NavHostController, fileManager: FileManager) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, BypassCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "Logo",
                                    tint = BypassCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "BYPASS IDE",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BypassDarkBackground
                    ),
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$ SHELL", color = BypassTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF064E3B))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF10B981)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(":8080", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            IconButton(onClick = { /* Run */ }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = BypassCyan)
                            }
                            IconButton(onClick = { rootNavController.navigate("settings") }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = BypassTextSecondary)
                            }
                        }
                    }
                )
                
                var showBanner by remember { mutableStateOf(true) }
                if (showBanner) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF332000))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = "Warning", tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Standard mode active. Grant root for full POSIX & Termux execution.",
                                color = Color(0xFFF59E0B),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Grant Root",
                                color = BypassCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { /* Request Root */ }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = BypassTextSecondary, modifier = Modifier.size(16.dp).clickable { showBanner = false })
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = BypassDarkSurface,
                contentColor = BypassTextSecondary,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    Triple("home", Icons.Default.Home, "Home"),
                    Triple("files", Icons.Default.Folder, "Files"),
                    Triple("editor", Icons.Default.Code, "Editor"),
                    Triple("terminal", Icons.Default.Terminal, "Terminal"),
                    Triple("preview", Icons.Default.PlayCircle, "Preview"),
                    Triple("dev", Icons.Default.CheckCircle, "Dev")
                )
                items.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp) },
                        selected = currentRoute?.startsWith(route) == true || (currentRoute == null && route == "home"),
                        onClick = {
                            if (currentRoute?.startsWith(route) != true) {
                                bottomNavController.navigate(if (route == "editor") "editor/" else route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BypassCyan,
                            selectedTextColor = BypassCyan,
                            indicatorColor = BypassCyan.copy(alpha = 0.15f),
                            unselectedIconColor = BypassTextSecondary,
                            unselectedTextColor = BypassTextSecondary
                        )
                    )
                }
            }
        },
        containerColor = BypassDarkBackground
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(rootNavController, fileManager) }
            composable("files") { FilesScreen(rootNavController, fileManager) }
            composable("editor/{path}") { backStackEntry ->
                val path = backStackEntry.arguments?.getString("path") ?: ""
                EditorScreen(rootNavController, fileManager, path)
            }
            composable("editor/") {
                EditorScreen(rootNavController, fileManager, "")
            }
            composable("terminal") { TerminalScreen(rootNavController, fileManager) }
            composable("preview") { PreviewScreen(rootNavController) }
            composable("dev") { DevScreen(rootNavController) }
        }
    }
}
