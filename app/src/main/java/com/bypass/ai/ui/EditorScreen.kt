package com.bypass.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bypass.ai.core.FileManager
import com.bypass.ai.ui.theme.BypassCyan
import com.bypass.ai.ui.theme.BypassDarkBackground
import com.bypass.ai.ui.theme.BypassTextSecondary
import java.io.File
import java.net.URLDecoder

@Composable
fun EditorScreen(navController: NavHostController, fileManager: FileManager, encodedPath: String) {
    if (encodedPath.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(BypassDarkBackground), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Code, contentDescription = null, tint = BypassTextSecondary.copy(alpha=0.5f), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No File Open", color = Color.White, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Select a file from Explorer or ask AI Agent in Home to architect code.", color = BypassTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate("files") },
                    colors = ButtonDefaults.buttonColors(containerColor = BypassCyan),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Open File Explorer", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
        return
    }

    val filePath = URLDecoder.decode(encodedPath, "UTF-8")
    val file = File(filePath)
    var content by remember { mutableStateOf(fileManager.readFile(file) ?: "") }
    
    Column(modifier = Modifier.fillMaxSize().background(BypassDarkBackground)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(file.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = { fileManager.writeFile(file, content) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Save, contentDescription = "Save", tint = BypassCyan)
            }
        }
        
        HorizontalDivider(color = Color(0xFF1E293B))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0B1017))
        ) {
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                textStyle = TextStyle(
                    color = Color(0xFFE2E8F0),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .padding(16.dp)
            )
        }
    }
}
