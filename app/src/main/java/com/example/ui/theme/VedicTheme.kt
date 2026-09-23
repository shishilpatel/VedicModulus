package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Sacred Vedic Deep Palette & Geometry Colors
val PrimaryBackgroundDark = Color(0xFF0B0F19) // Deep black/navy cosmic void
val SurfaceDark = Color(0xFF131B2E)
val SurfaceVariantDark = Color(0xFF1E293B)
val GoldGeometry = Color(0xFFFFD700)       // Pure Vedic Gold
val MutedGoldGeometry = Color(0xFFC5A059) // Muted antique gold for geometry and yantras

// Accent Colors for Chakras, Planets, and Spiritual Indicators
val ChakraRootRed = Color(0xFFE53935)
val ChakraSacralOrange = Color(0xFFFB8C00)
val ChakraSolarYellow = Color(0xFFFDD835)
val ChakraHeartGreen = Color(0xFF43A047)
val ChakraThroatBlue = Color(0xFF1E88E5)
val ChakraThirdEyeIndigo = Color(0xFF3949AB)
val ChakraCrownViolet = Color(0xFF8E24AA)

val PlanetSuryaGold = Color(0xFFFFC107)
val PlanetChandraSilver = Color(0xFFE0E0E0)
val PlanetMangalaRed = Color(0xFFD32F2F)
val PlanetBudhaGreen = Color(0xFF388E3C)
val PlanetGuruYellow = Color(0xFFFBC02D)
val PlanetShukraWhite = Color(0xFFF8F9FA)
val PlanetShaniBlue = Color(0xFF303F9F)
val PlanetRahuDark = Color(0xFF455A64)
val PlanetKetuBrown = Color(0xFF6D4C41)

private val VedicDarkColorScheme = darkColorScheme(
    primary = GoldGeometry,
    onPrimary = Color(0xFF0B0F19),
    primaryContainer = MutedGoldGeometry,
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = ChakraThroatBlue,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = Color(0xFFFFD700),
    background = PrimaryBackgroundDark,
    onBackground = Color(0xFFF1F5F9),
    surface = SurfaceDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = MutedGoldGeometry
)

@Composable
fun VedicTheme(
    darkTheme: Boolean = true, // Default to immersive cosmic dark for Vedic astronomy & yantras
    content: @Composable () -> Unit
) {
    val colorScheme = VedicDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
