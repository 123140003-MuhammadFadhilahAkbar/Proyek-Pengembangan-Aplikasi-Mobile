package com.learncore.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ==================== BRAND COLORS ====================

// Eisenhower quadrant accent colors (shared across themes)
val QuadrantDoFirst = Color(0xFFE53935)       // Q1 - Urgent & Important
val QuadrantSchedule = Color(0xFF1E88E5)      // Q2 - Not Urgent & Important
val QuadrantDelegate = Color(0xFFFB8C00)      // Q3 - Urgent & Not Important
val QuadrantEliminate = Color(0xFF757575)     // Q4 - Not Urgent & Not Important

// Light scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A237E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8EAF6),
    onPrimaryContainer = Color(0xFF0D1257),
    secondary = Color(0xFF1565C0),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF0D3B7A),
    tertiary = Color(0xFF2E7D32),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE8F5E9),
    onTertiaryContainer = Color(0xFF1B5E20),
    error = Color(0xFFC62828),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFF7F0000),
    background = Color(0xFFF8F9FD),
    onBackground = Color(0xFF0D0D14),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0D0D14),
    surfaceVariant = Color(0xFFF0F1F8),
    onSurfaceVariant = Color(0xFF44475A),
    outline = Color(0xFFBDBECC),
    outlineVariant = Color(0xFFDEDFF0),
    surfaceTint = Color(0xFF1A237E)
)

// Dark scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7986CB),
    onPrimary = Color(0xFF0D0F2E),
    primaryContainer = Color(0xFF1A237E),
    onPrimaryContainer = Color(0xFFB8BBE8),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF0A1F3F),
    secondaryContainer = Color(0xFF1565C0),
    onSecondaryContainer = Color(0xFFB3D7F8),
    tertiary = Color(0xFF81C784),
    onTertiary = Color(0xFF0A2E0B),
    tertiaryContainer = Color(0xFF2E7D32),
    onTertiaryContainer = Color(0xFFB8DFB9),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF3F0000),
    errorContainer = Color(0xFFC62828),
    onErrorContainer = Color(0xFFFFCDD2),
    background = Color(0xFF0D0E18),
    onBackground = Color(0xFFE8E9F4),
    surface = Color(0xFF12131F),
    onSurface = Color(0xFFE8E9F4),
    surfaceVariant = Color(0xFF1E2030),
    onSurfaceVariant = Color(0xFFB0B3CC),
    outline = Color(0xFF44475A),
    outlineVariant = Color(0xFF2A2D42),
    surfaceTint = Color(0xFF7986CB)
)

@Composable
fun LearnCoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
