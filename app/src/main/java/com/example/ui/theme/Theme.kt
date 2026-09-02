package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PastelPink,
    onPrimary = TextPrimary,
    primaryContainer = Color(0xFF4A2F39),
    onPrimaryContainer = PastelPinkLight,
    secondary = PastelLavender,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFF382F48),
    onSecondaryContainer = PastelLavenderLight,
    tertiary = PastelPeach,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF3A2D35),
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PastelPinkDark,
    onPrimary = Color.White,
    primaryContainer = PastelPinkLight,
    onPrimaryContainer = PastelPinkDark,
    secondary = PastelLavenderDark,
    onSecondary = Color.White,
    secondaryContainer = PastelLavenderLight,
    onSecondaryContainer = PastelLavenderDark,
    tertiary = PastelPeach,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
