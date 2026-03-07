package com.magicai.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Brand Colors
val Purple600 = Color(0xFF6366F1)
val Purple700 = Color(0xFF4F46E5)
val Purple800 = Color(0xFF4338CA)
val PurpleLight = Color(0xFFA5B4FC)
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate400 = Color(0xFF94A3B8)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val GreenSuccess = Color(0xFF10B981)
val RedError = Color(0xFFEF4444)
val YellowWarning = Color(0xFFF59E0B)
val BlueInfo = Color(0xFF3B82F6)

private val DarkColorScheme = darkColorScheme(
    primary = Purple600,
    onPrimary = White,
    primaryContainer = Purple800,
    onPrimaryContainer = PurpleLight,
    secondary = Slate600,
    onSecondary = White,
    secondaryContainer = Slate700,
    onSecondaryContainer = Slate200,
    tertiary = GreenSuccess,
    onTertiary = White,
    background = Slate900,
    onBackground = White,
    surface = Slate800,
    onSurface = White,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate400,
    error = RedError,
    onError = White,
    outline = Slate600,
    outlineVariant = Slate700,
    inverseSurface = Slate200,
    inverseOnSurface = Slate800,
    inversePrimary = Purple700,
    surfaceTint = Purple600,
    scrim = Black.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple600,
    onPrimary = White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Purple800,
    secondary = Slate600,
    onSecondary = White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate700,
    tertiary = GreenSuccess,
    onTertiary = White,
    background = White,
    onBackground = Slate900,
    surface = White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    error = RedError,
    onError = White,
    outline = Slate400,
    outlineVariant = Slate200,
    inverseSurface = Slate900,
    inverseOnSurface = White,
    inversePrimary = PurpleLight,
    surfaceTint = Purple600,
    scrim = Black.copy(alpha = 0.3f)
)

@Composable
fun MagicAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MagicAITypography,
        content = content
    )
}
