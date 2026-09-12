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
import com.bypass.ai.core.AgentViewModel
import com.bypass.ai.ui.theme.BypassCyan
import com.bypass.ai.ui.theme.BypassDarkBackground
import com.bypass.ai.ui.theme.BypassDarkSurface
import com.bypass.ai.ui.theme.BypassTextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(rootNavController: NavHostController, viewModel: AgentViewModel) {
    var text by remember { mutableStateOf("") }
    
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
                    .border(1.dp, if (viewModel.status.value == "SUCCESS") Color(0xFF10B981) else if (viewModel.status.value == "ERROR") Color.Red else BypassCyan, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (viewModel.status.value == "SUCCESS") Color(0xFF10B981) else if (viewModel.status.value == "ERROR") Color.Red else BypassCyan))
                Spacer(modifier = Modifier.width(6.dp))
                Text(viewModel.status.value, color = if (viewModel.status.value == "SUCCESS") Color(0xFF10B981) else if (viewModel.status.value == "ERROR") Color.Red else BypassCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BypassCyan.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.messages.clear() }
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
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = BypassTextSecondary, modifier = Modifier.size(20.dp).clickable { viewModel.messages.clear() })
            }
        }
        
        HorizontalDivider(color = Color(0xFF1E293B))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (viewModel.messages.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            )
                            .border(1.dp, BypassCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Logo",
                            tint = BypassCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "ADVANCED ROOT AI ENGINE",
                        color = BypassCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, BypassCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "BYPASS AUTONOMOUS IDE",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Architect full-stack apps, write root utilities, and execute direct\nOS commands autonomously. Prompt in English or Hindi.",
                        color = BypassTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(viewModel.messages) { message ->
                        ChatBubble(message)
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
        
        HorizontalDivider(color = Color(0xFF1E293B))
        
        // Input Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BypassDarkSurface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice", tint = BypassCyan, modifier = Modifier.size(16.dp))
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
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
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (text.isNotEmpty() && !viewModel.isBuilding.value) BypassCyan else Color(0xFF1E293B))
                        .clickable {
                            if (text.isNotEmpty() && !viewModel.isBuilding.value) {
                                val prompt = text
                                text = ""
                                viewModel.executePrompt(prompt)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.isBuilding.value) {
                        CircularProgressIndicator(color = BypassTextSecondary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = if (text.isNotEmpty()) Color.Black else BypassTextSecondary, modifier = Modifier.size(20.dp))
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
                    Text("Gemini 3.6 Flash", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
fun ChatBubble(message: ChatMessage) {
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
            Text(
                text = message.text,
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
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
