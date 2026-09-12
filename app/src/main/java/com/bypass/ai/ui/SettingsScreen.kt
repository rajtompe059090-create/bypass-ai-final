package com.bypass.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
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
import com.bypass.ai.ui.theme.BypassCyan
import com.bypass.ai.ui.theme.BypassDarkBackground
import com.bypass.ai.ui.theme.BypassDarkSurface
import com.bypass.ai.ui.theme.BypassTextSecondary

@Composable
fun SettingsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = BypassCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("IDE SETTINGS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = BypassTextSecondary)
            }
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gemini Settings
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, BypassCyan.copy(alpha=0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = BypassDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF10B981)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GEMINI 3.8 FLASH HIGH", color = BypassCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier.border(1.dp, Color(0xFF10B981), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("UNLIMITED", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Unlimited code generation, full-stack project building, and multi-file debugging enabled with zero restrictions.",
                        color = BypassTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Engine Pipeline: Online & Active", color = BypassTextSecondary, fontSize = 12.sp)
                    }
                }
            }
            
            // Reasoning
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF9333EA), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = BypassDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFFD946EF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REASONING & THINKING BUDGET", color = Color(0xFFD946EF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier.background(Color(0xFFD946EF).copy(alpha=0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("HIGH", color = Color(0xFFD946EF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Configure reasoning depth and thinking token allocation for complex architecture synthesis:",
                        color = BypassTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ReasoningOption("High", "12k", true)
                        ReasoningOption("Medium", "6k", false)
                        ReasoningOption("Low", "2k", false)
                        ReasoningOption("Off", "0k", false)
                    }
                }
            }
            
            SettingsActionRow("IP & NETWORK RESET", "Rotate carrier IP via Root & flush DNS cache", "Reset IP")
            SettingsActionRow("VOICE DIAGNOSTICS & ERROR LOGS", "Inspect live speech recognizer & system voice logs", "View Logs")
            SettingsToggleRow("DYNAMIC ISLAND TOP CAPSULE", "Floating futuristic top capsule overlay with audio visualizer", true)
            SettingsToggleRow("BACKGROUND ASSISTANT SERVICE", "Keep Maria active in background when IDE is minimized", true)
            SettingsToggleRow("LIVE WEB SEARCH GROUNDING", "Automatically fetches live docs & APIs for Gemini (@web)", true)
            
            // Root Access
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF59E0B).copy(alpha=0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = BypassDarkSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ROOT ACCESS: STANDARD SHELL", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Standard user sandbox", color = BypassTextSecondary, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(containerColor = BypassCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Request SU", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BypassCyan),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Done", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun ReasoningOption(title: String, subtitle: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFD946EF).copy(alpha = 0.1f) else Color(0xFF1E293B))
            .border(1.dp, if (isSelected) Color(0xFFD946EF) else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = if (isSelected) Color(0xFFD946EF) else BypassTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = if (isSelected) Color(0xFFD946EF).copy(alpha=0.8f) else BypassTextSecondary.copy(alpha=0.5f), fontSize = 10.sp)
        }
    }
}

@Composable
fun SettingsActionRow(title: String, subtitle: String, actionText: String) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = BypassDarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = BypassTextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BypassCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, BypassCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(actionText, color = BypassCyan, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SettingsToggleRow(title: String, subtitle: String, checked: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = BypassDarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = BypassTextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = BypassCyan,
                    uncheckedThumbColor = BypassTextSecondary,
                    uncheckedTrackColor = Color(0xFF1E293B)
                )
            )
        }
    }
}
