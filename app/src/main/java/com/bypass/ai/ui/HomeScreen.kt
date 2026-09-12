package com.bypass.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bypass.ai.ChatMessage
import com.bypass.ai.ai.ActionParser
import com.bypass.ai.core.FileManager
import com.bypass.ai.generateGeminiResponse
import com.bypass.ai.ui.theme.BypassCyan
import com.bypass.ai.ui.theme.BypassDarkBackground
import com.bypass.ai.ui.theme.BypassDarkSurface
import com.bypass.ai.ui.theme.BypassTextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(rootNavController: NavHostController, fileManager: FileManager) {
    var text by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BypassDarkBackground)
    ) {
        // Chat Header
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
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat #1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = BypassTextSecondary, modifier = Modifier.size(16.dp))
            }
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Spacer(modifier = Modifier.width(6.dp))
                Text("READY", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BypassCyan.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                        .clickable { messages.clear() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", color = BypassCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BypassTextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = BypassTextSecondary, modifier = Modifier.size(20.dp).clickable { messages.clear() })
            }
        }
        
        HorizontalDivider(color = Color(0xFF1E293B))

        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF00E5FF).copy(alpha = 0.2f))
                                )
                            )
                            .border(2.dp, BypassCyan.copy(alpha=0.5f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, BypassCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("ADVANCED ROOT AI ENGINE", color = BypassCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("BYPASS AUTONOMOUS IDE", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Architect full-stack apps, write root utilities, and execute direct\nOS commands autonomously. Prompt in English or Hindi.",
                        color = BypassTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, BypassCyan.copy(alpha=0.5f), RoundedCornerShape(24.dp))
                            .clickable { text = "Make a Cyberpunk Game" }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cyberpunk Game", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = BypassTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    val isLast = message == messages.last()
                    ChatBubble(
                        message = message,
                        onRetry = if (message.isError && isLast) {
                            {
                                messages.remove(message)
                                val lastUserMsg = messages.lastOrNull { it.isUser }
                                if (lastUserMsg != null && !isLoading) {
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            val response = generateGeminiResponse(messages.dropLast(1), lastUserMsg.text)
                                            messages.add(ChatMessage(response, isUser = false))
                                        } catch (e: Exception) {
                                            messages.add(ChatMessage(e.message ?: "An error occurred", isUser = false, isError = true))
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        } else null,
                        onExecuteAction = { action ->
                            coroutineScope.launch {
                                when (action.type) {
                                    "create_file", "update_file" -> {
                                        if (action.path != null && action.content != null) {
                                            fileManager.writeFile(java.io.File(fileManager.workspaceDir, action.path), action.content)
                                            messages.add(ChatMessage("File ${action.path} saved successfully.", isUser = false))
                                        }
                                    }
                                    "run_command" -> {
                                        if (action.command != null) {
                                            val output = com.bypass.ai.core.TerminalRunner.runCommand(action.command, fileManager.workspaceDir)
                                            messages.add(ChatMessage("Command output:\n$output", isUser = false))
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // Bottom Input Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Build apps, create websites, or write code...", color = BypassTextSecondary, fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = BypassCyan
                    )
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF064E3B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Mic", tint = BypassCyan, modifier = Modifier.size(20.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .clickable {
                                if (text.isNotBlank() && !isLoading) {
                                    val prompt = text
                                    messages.add(ChatMessage(prompt, isUser = true))
                                    text = ""
                                    isLoading = true
                                    
                                    coroutineScope.launch {
                                        try {
                                            val response = generateGeminiResponse(messages.dropLast(1), prompt)
                                            messages.add(ChatMessage(response, isUser = false))
                                        } catch (e: Exception) {
                                            messages.add(ChatMessage(e.message ?: "An error occurred", isUser = false, isError = true))
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = BypassTextSecondary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = BypassTextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BypassCyan.copy(alpha=0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gemini 3.8 Flash", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BypassTextSecondary, modifier = Modifier.size(16.dp))
                }
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BypassCyan.copy(alpha=0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Builder", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BypassTextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onRetry: (() -> Unit)? = null, onExecuteAction: ((com.bypass.ai.ai.AiAction) -> Unit)? = null) {
    val align = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isError) Color(0x33FF5252) else if (message.isUser) BypassCyan.copy(alpha = 0.2f) else BypassDarkSurface
    val textColor = if (message.isError) Color(0xFFFF5252) else if (message.isUser) BypassCyan else Color.White
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = align
    ) {
        Column(
            modifier = Modifier
                .clip(shape)
                .background(bgColor)
                .padding(16.dp)
                .widthIn(max = 300.dp)
        ) {
            if (!message.isUser && !message.isError) {
                val (textBody, actions) = ActionParser.parseResponse(message.text)
                if (textBody.isNotEmpty()) {
                    Text(
                        text = textBody,
                        color = textColor,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                actions.forEach { action ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BypassDarkBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Action: ${action.type}", color = BypassCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            action.path?.let { Text("File: $it", color = BypassTextSecondary, fontSize = 10.sp) }
                            action.command?.let { Text("Cmd: $it", color = BypassTextSecondary, fontSize = 10.sp) }
                            if (onExecuteAction != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onExecuteAction(action) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BypassCyan),
                                    modifier = Modifier.fillMaxWidth().height(28.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Execute", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            
            if (message.isError) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ERROR",
                        color = Color(0xFFFF5252),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (onRetry != null) {
                        Text(
                            text = "RETRY",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(onClick = onRetry)
                                .padding(4.dp)
                        )
                    }
                }
            } else if (!message.isUser) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BYPASS AI",
                        color = BypassCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
