package com.bypass.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bypass.ai.core.FileManager
import com.bypass.ai.core.TerminalRunner
import com.bypass.ai.ui.theme.BypassCyan
import com.bypass.ai.ui.theme.BypassDarkBackground
import com.bypass.ai.ui.theme.BypassTextSecondary
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(navController: NavHostController, fileManager: FileManager) {
    var command by remember { mutableStateOf("") }
    val history = remember { mutableStateListOf<String>(
        "[SYSTEM] Bypass IDE Root Subsystem Initialized",
        "[WORKSPACE] ${fileManager.workspaceDir.absolutePath}",
        "[PROJECT DIR] ${fileManager.workspaceDir.absolutePath}"
    ) }
    val coroutineScope = rememberCoroutineScope()
    val workingDir = fileManager.workspaceDir

    val quickCommands = listOf("ls -la", "pwd", "node -v", "python3 --version", "git status")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BypassDarkBackground)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Terminal, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("TERMINAL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(16.dp))
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable { /* Request Root */ }
            ) {
                Text("$ SHELL (Tap for Root)", color = BypassTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { /* Copy */ }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BypassTextSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy", color = BypassTextSecondary, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { history.clear() }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear", color = Color(0xFFF59E0B), fontSize = 10.sp)
            }
        }
        
        HorizontalDivider(color = Color(0xFF1E293B))
        
        // Quick Commands
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B1017))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickCommands) { cmd ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                        .clickable { command = cmd }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(cmd, color = BypassCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
        
        HorizontalDivider(color = Color(0xFF1E293B))

        // Output
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0B1017))
                .padding(16.dp),
            reverseLayout = false
        ) {
            items(history) { line ->
                val color = when {
                    line.startsWith("$") -> BypassCyan
                    line.startsWith("[SYSTEM]") || line.startsWith("[WORKSPACE]") || line.startsWith("[PROJECT DIR]") -> Color(0xFFA855F7)
                    line.startsWith("[ERROR]") -> Color(0xFFFF5252)
                    else -> Color(0xFFE2E8F0)
                }
                Text(
                    text = line,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        
        // Input Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (command.isNotBlank()) {
                            val currentCmd = command
                            history.add("$ $currentCmd")
                            command = ""
                            coroutineScope.launch {
                                val output = TerminalRunner.runCommand(currentCmd, workingDir)
                                if (output.isNotBlank()) {
                                    history.add(output)
                                }
                            }
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF0B1017),
                    unfocusedContainerColor = Color(0xFF0B1017)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .clickable {
                        if (command.isNotBlank()) {
                            val currentCmd = command
                            history.add("$ $currentCmd")
                            command = ""
                            coroutineScope.launch {
                                val output = TerminalRunner.runCommand(currentCmd, workingDir)
                                if (output.isNotBlank()) {
                                    history.add(output)
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Run", tint = BypassTextSecondary)
            }
        }
    }
}
