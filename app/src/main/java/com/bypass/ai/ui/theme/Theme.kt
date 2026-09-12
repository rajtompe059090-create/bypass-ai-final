package com.bypass.ai.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BypassColorScheme = darkColorScheme(
    primary = BypassCyan,
    secondary = BypassCyanDark,
    background = BypassDarkBackground,
    surface = BypassDarkSurface,
    onPrimary = Color.Black,
    onBackground = BypassTextPrimary,
    onSurface = BypassTextPrimary
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for the AI Developer Assistant look
  dynamicColor: Boolean = false, // Disable dynamic colors to keep the custom palette consistent
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = BypassColorScheme,
    typography = Typography,
    content = content
  )
}
