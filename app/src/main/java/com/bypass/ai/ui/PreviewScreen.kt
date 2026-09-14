package com.bypass.ai.ui

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.bypass.ai.core.AgentViewModel
import com.bypass.ai.ui.theme.BypassCyan
import com.bypass.ai.ui.theme.BypassDarkBackground
import com.bypass.ai.ui.theme.BypassTextSecondary

@Composable
fun PreviewScreen(navController: NavHostController, viewModel: AgentViewModel) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var url by remember { mutableStateOf("http://127.0.0.1:8080/") }
    val logs = remember { mutableStateListOf<String>() }

    LaunchedEffect(viewModel.isBuilding.value) {
        if (!viewModel.isBuilding.value && viewModel.status.value == "SUCCESS") {
            webView?.reload()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BypassDarkBackground)) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0B1017))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = url, 
                    color = Color.White, 
                    fontSize = 12.sp, 
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BypassTextSecondary, modifier = Modifier.size(20.dp).clickable {
                webView?.reload()
            })
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.Upload, contentDescription = "Upload", tint = BypassCyan, modifier = Modifier.size(20.dp))
        }
        
        HorizontalDivider(color = Color(0xFF1E293B))

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, urlStr: String?) {
                                if (urlStr != null) url = urlStr
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let {
                                    logs.add("${it.messageLevel()}: ${it.message()}")
                                    if (it.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                                        viewModel.sendError("JavaScript Error in Preview: ${it.message()}")
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                }
                                return super.onConsoleMessage(consoleMessage)
                            }
                        }
                        loadUrl(url)
                        webView = this
                    }
                },
                update = { view ->
                    webView = view
                },
                modifier = Modifier.fillMaxSize()
            )
            
            DisposableEffect(Unit) {
                onDispose {
                    webView?.destroy()
                    webView = null
                }
            }
        }
        
        HorizontalDivider(color = Color(0xFF1E293B))
        
        // Console Logs Bottom Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = BypassTextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Console Logs (${logs.size})", color = BypassTextSecondary, fontSize = 14.sp)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { 
                        if (logs.isNotEmpty()) {
                            viewModel.sendError(logs.joinToString("\n"))
                            navController.navigate("home")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = BypassCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send to AI", color = BypassCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.ExpandLess, contentDescription = null, tint = BypassTextSecondary)
            }
        }
    }
}
