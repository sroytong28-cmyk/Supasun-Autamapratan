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
    primary = WoodBrownLight,
    secondary = RiverGreenLight,
    tertiary = MistGoldAccent,
    background = Color(0xFF1E1D1C),
    surface = Color(0xFF262524),
    onPrimary = Color(0xFF5D3A30),
    onSecondary = Color(0xFF1E3D34),
    onBackground = Color(0xFFFAF9F6),
    onSurface = Color(0xFFFAF9F6),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = WoodBrownPrimary,
    secondary = RiverGreenSecondary,
    tertiary = MistGoldAccent,
    background = WarmCreamBackground,
    surface = SoftCardSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary,
    primaryContainer = WoodBrownLight,
    onPrimaryContainer = WoodBrownDark,
    secondaryContainer = RiverGreenLight,
    onSecondaryContainer = RiverGreenDark,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors by default to preserve the curated resort and nature branding
  dynamicColor: Boolean = false,
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
