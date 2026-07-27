package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CyanAccent,
    onPrimary = DarkNavy,
    primaryContainer = CyanPrimary,
    onPrimaryContainer = TextPrimaryDark,
    secondary = OrangeIvory,
    onSecondary = TextPrimaryDark,
    tertiary = GreenActive,
    background = DarkNavy,
    onBackground = TextPrimaryDark,
    surface = DarkNavySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkNavy,
    onSurfaceVariant = TextSecondaryDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CyanPrimary,
    onPrimary = TextPrimaryDark,
    primaryContainer = CyanAccent,
    onPrimaryContainer = DarkNavy,
    secondary = OrangeIvory,
    onSecondary = TextPrimaryDark,
    tertiary = GreenActive,
    background = DarkNavy,
    onBackground = TextPrimaryDark,
    surface = DarkNavySurface,
    onSurface = TextPrimaryDark
  )

@Composable
fun DriveAITheme(
  darkTheme: Boolean = true, // Default to GPS Dark High-Contrast mode for night/day driving visibility
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

