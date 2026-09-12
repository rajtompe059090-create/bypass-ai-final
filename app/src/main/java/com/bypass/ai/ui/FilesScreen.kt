package com.bypass.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bypass.ai.core.FileManager
import com.bypass.ai.ui.theme.BypassCyan
import com.bypass.ai.ui.theme.BypassDarkBackground
import com.bypass.ai.ui.theme.BypassDarkSurface
import com.bypass.ai.ui.theme.BypassTextSecondary
import java.io.File
import java.net.URLEncoder

@Composable
fun FilesScreen(navController: NavHostController, fileManager: FileManager) {
    var currentDir by remember { mutableStateOf(fileManager.workspaceDir) }
    val files = remember(currentDir) { fileManager.listFiles(currentDir) }

    Column(modifier = Modifier.fillMaxSize().background(BypassDarkBackground)) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF064E3B))
                    .border(1.dp, BypassCyan.copy(alpha=0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(currentDir.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(16.dp))
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BypassCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = BypassTextSecondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = "New File", tint = BypassTextSecondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = BypassTextSecondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.AddBox, contentDescription = "Add", tint = BypassCyan, modifier = Modifier.size(20.dp))
            }
        }
        
        HorizontalDivider(color = Color(0xFF1E293B))
        
        Text(
            text = "Path: ${currentDir.absolutePath}",
            color = BypassTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        HorizontalDivider(color = Color(0xFF1E293B))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(currentDir.name, color = BypassCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        if (files.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = BypassTextSecondary.copy(alpha=0.5f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No files in this project folder yet", color = Color.White, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Create a file above or ask Bypass AI in Home to build a project.", color = BypassTextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(containerColor = BypassCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New File", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn {
                if (currentDir != fileManager.workspaceDir) {
                    item {
                        ListItem(
                            headlineContent = { Text("..", color = Color.White) },
                            leadingContent = { Icon(Icons.Default.Folder, contentDescription = null, tint = BypassTextSecondary) },
                            modifier = Modifier.clickable {
                                currentDir.parentFile?.let { currentDir = it }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
                items(files) { file ->
                    ListItem(
                        headlineContent = { Text(file.name, color = Color.White) },
                        leadingContent = {
                            Icon(
                                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null,
                                tint = BypassTextSecondary
                            )
                        },
                        modifier = Modifier.clickable {
                            if (file.isDirectory) {
                                currentDir = file
                            } else {
                                val encodedPath = URLEncoder.encode(file.absolutePath, "UTF-8")
                                navController.navigate("editor/$encodedPath")
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = BypassTextSecondary.copy(alpha = 0.2f))
                }
            }
        }
    }
}
