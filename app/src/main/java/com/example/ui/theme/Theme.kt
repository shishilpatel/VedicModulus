package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.core.model.AppThemePalette

@Composable
fun MyApplicationTheme(
    themePalette: AppThemePalette = AppThemePalette.SOLAR_GOLD,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themePalette) {
        AppThemePalette.SOLAR_GOLD -> lightColorScheme(
            primary = MaroonPrimary,
            onPrimary = OnMaroon,
            primaryContainer = Color(0xFFFDF2F2),
            onPrimaryContainer = MaroonPrimary,
            secondary = GoldPrimary,
            onSecondary = DarkText,
            secondaryContainer = Color(0xFFFEF9C3),
            onSecondaryContainer = Color(0xFF854D0E),
            tertiary = OrangeAccent,
            onTertiary = OnOrange,
            tertiaryContainer = Color(0xFFFFF7ED),
            onTertiaryContainer = Color(0xFFC2410C),
            background = WhiteBackground,
            onBackground = DarkText,
            surface = WhiteSurface,
            onSurface = DarkText,
            surfaceVariant = OffWhiteSurface,
            onSurfaceVariant = DarkTextSecondary,
            outline = LightBorder,
            outlineVariant = Color(0xFFCBD5E1),
            error = CrimsonError,
            onError = Color.White
        )
        AppThemePalette.LUNAR_SILVER -> lightColorScheme(
            primary = Color(0xFF1E293B),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFF1F5F9),
            onPrimaryContainer = Color(0xFF0F172A),
            secondary = Color(0xFF0284C7),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE0F2FE),
            onSecondaryContainer = Color(0xFF0369A1),
            tertiary = Color(0xFF6366F1),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFEEF2FF),
            onTertiaryContainer = Color(0xFF4338CA),
            background = Color(0xFFF8FAFC),
            onBackground = DarkText,
            surface = WhiteSurface,
            onSurface = DarkText,
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = DarkTextSecondary,
            outline = LightBorder,
            outlineVariant = Color(0xFFCBD5E1),
            error = CrimsonError,
            onError = Color.White
        )
        AppThemePalette.TRADITIONAL_EARTH -> lightColorScheme(
            primary = Color(0xFF5D4037),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEFEBE9),
            onPrimaryContainer = Color(0xFF3E2723),
            secondary = Color(0xFF2E7D32),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE8F5E9),
            onSecondaryContainer = Color(0xFF1B5E20),
            tertiary = Color(0xFFD97706),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFFBEB),
            onTertiaryContainer = Color(0xFF92400E),
            background = Color(0xFFFDFBF7),
            onBackground = DarkText,
            surface = WhiteSurface,
            onSurface = DarkText,
            surfaceVariant = Color(0xFFF5F0EB),
            onSurfaceVariant = DarkTextSecondary,
            outline = Color(0xFFD7CCC8),
            outlineVariant = Color(0xFFBCAAA4),
            error = CrimsonError,
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
