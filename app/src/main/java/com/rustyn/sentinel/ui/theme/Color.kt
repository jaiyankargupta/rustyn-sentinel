package com.rustyn.sentinel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

// ── Raw Colors for Theme definitions ──
val RawDarkBackground = Color(0xFF0B0F19)
val RawDarkSurface = Color(0xFF151D30)
val RawDarkSurfaceVariant = Color(0xFF1A2340)
val RawDarkSurfaceElevated = Color(0xFF1E2A45)

val RawPrimarySky = Color(0xFF38BDF8)
val RawPrimarySkyDark = Color(0xFF0EA5E9)
val RawAccentIndigo = Color(0xFF818CF8)
val RawAccentIndigoDark = Color(0xFF6366F1)

val RawBlockRed = Color(0xFFF87171)
val RawBlockRedDark = Color(0xFFEF4444)
val RawSuccessGreen = Color(0xFF34D399)
val RawSuccessGreenDark = Color(0xFF10B981)
val RawWarningAmber = Color(0xFFFBBF24)
val RawWarningAmberDark = Color(0xFFF59E0B)

val RawTextLight = Color(0xFFF8FAFC)
val RawTextMuted = Color(0xFF94A3B8)
val RawTextSubtle = Color(0xFF64748B)

val RawBorderSlate = Color(0xFF334155)
val RawBorderSubtle = Color(0xFF1E293B)

val RawSecondaryContainer = Color(0xFF1E2A45)
val RawOnSecondaryContainer = Color(0xFFE2E8F0)

// ── Dynamic Theme Colors (used by UI components) ──
val DarkBackground: Color @Composable get() = MaterialTheme.colorScheme.background
val DarkSurface: Color @Composable get() = MaterialTheme.colorScheme.surface
val DarkSurfaceVariant: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val DarkSurfaceElevated: Color @Composable get() = MaterialTheme.colorScheme.tertiaryContainer

val PrimarySky: Color @Composable get() = MaterialTheme.colorScheme.primary
val PrimarySkyDark: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
val AccentIndigo: Color @Composable get() = MaterialTheme.colorScheme.secondary
val AccentIndigoDark: Color @Composable get() = MaterialTheme.colorScheme.inversePrimary

val BlockRed: Color @Composable get() = MaterialTheme.colorScheme.error
val BlockRedDark: Color @Composable get() = MaterialTheme.colorScheme.scrim
val SuccessGreen: Color @Composable get() = RawSuccessGreen
val SuccessGreenDark: Color @Composable get() = RawSuccessGreenDark
val WarningAmber: Color @Composable get() = RawWarningAmber
val WarningAmberDark: Color @Composable get() = RawWarningAmberDark

val TextLight: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val TextMuted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val TextSubtle: Color @Composable get() = MaterialTheme.colorScheme.tertiary

val BorderSlate: Color @Composable get() = MaterialTheme.colorScheme.outline
val BorderSubtle: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
val GlassOverlay: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
val GlassOverlayLight: Color @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)

val GradientCyanStart = Color(0xFF06B6D4)
val GradientCyanEnd = Color(0xFF6366F1)
val GradientWarmStart = Color(0xFFF59E0B)
val GradientWarmEnd = Color(0xFFEF4444)
val GradientGreenStart = Color(0xFF10B981)
val GradientGreenEnd = Color(0xFF06B6D4)

val HeroGradientStart: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF0F3642) else Color(0xFFE0F2FE)
val HeroGradientMid: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF142036) else Color(0xFFBAE6FD)
val HeroGradientEnd: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF101726) else Color(0xFFF0F9FF)

val ShimmerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.05f)
val GlowCyan: Color @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
val GlowIndigo: Color @Composable get() = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
val GlowRed: Color @Composable get() = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)

val SecondaryContainer: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
val OnSecondaryContainer: Color @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer
