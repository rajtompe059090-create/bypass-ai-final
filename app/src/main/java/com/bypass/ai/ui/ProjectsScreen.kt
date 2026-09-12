package com.bypass.ai.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bypass.ai.core.FileManager
import com.bypass.ai.ui.theme.BypassCyan
import com.bypass.ai.ui.theme.BypassTextSecondary
import java.io.File

@Composable
fun ProjectsScreen(navController: NavHostController, fileManager: FileManager) {
    val projects = remember { mutableStateListOf<File>().apply { addAll(fileManager.listProjects()) } }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Projects Workspace", style = MaterialTheme.typography.titleLarge, color = Color.White)
            IconButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "New Project", tint = BypassCyan)
            }
        }

        if (projects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No projects yet. Create one!", color = BypassTextSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(projects) { project ->
                    ListItem(
                        headlineContent = { Text(project.name, color = Color.White) },
                        leadingContent = { Icon(Icons.Default.Folder, contentDescription = null, tint = BypassTextSecondary) },
                        modifier = Modifier.clickable { /* TODO: Open Project context */ },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    Divider(color = BypassTextSecondary.copy(alpha = 0.2f))
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("New Project") },
                text = {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("Project Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newProjectName.isNotBlank()) {
                            fileManager.createProject(newProjectName)
                            projects.clear()
                            projects.addAll(fileManager.listProjects())
                        }
                        showCreateDialog = false
                        newProjectName = ""
                    }) {
                        Text("Create", color = BypassCyan)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("Cancel", color = Color.White) }
                },
                containerColor = com.bypass.ai.ui.theme.BypassDarkSurface
            )
        }
    }
}
