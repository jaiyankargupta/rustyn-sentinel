package com.rustyn.sentinel.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RawPrimarySky,
    onPrimary = RawDarkBackground,
    primaryContainer = RawPrimarySkyDark,
    onPrimaryContainer = RawTextLight,
    secondary = RawAccentIndigo,
    onSecondary = RawDarkBackground,
    secondaryContainer = RawSecondaryContainer,
    onSecondaryContainer = RawOnSecondaryContainer,
    background = RawDarkBackground,
    onBackground = RawTextLight,
    surface = RawDarkSurface,
    onSurface = RawTextLight,
    surfaceVariant = RawDarkSurfaceVariant,
    onSurfaceVariant = RawTextMuted,
    error = RawBlockRed,
    onError = RawDarkBackground,
    errorContainer = RawBlockRedDark.copy(alpha = 0.2f),
    onErrorContainer = RawBlockRed,
    outline = RawBorderSlate,
    outlineVariant = RawBorderSubtle,
    inverseSurface = RawTextLight,
    inverseOnSurface = RawDarkBackground,
    surfaceTint = RawPrimarySky,
    tertiary = RawTextSubtle,
    tertiaryContainer = RawDarkSurfaceElevated,
    inversePrimary = RawAccentIndigoDark,
    scrim = RawBlockRedDark
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = RawAccentIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF312E81),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF334155),
    error = RawBlockRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    tertiary = Color(0xFF475569),
    tertiaryContainer = Color(0xFFF8FAFC),
    inversePrimary = Color(0xFF4F46E5),
    scrim = Color(0xFFB91C1C)
)

@Composable
fun RustynSentinelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to enforce our curated palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
