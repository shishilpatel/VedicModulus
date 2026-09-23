package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.WatchFaceConfiguration
import com.example.core.model.WatchFaceStyle
import com.example.ui.theme.*

data class VedicThemeDescriptor(
    val style: WatchFaceStyle,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val suggestedPrimaryHex: Long,
    val suggestedAccentHex: Long,
    val features: List<String>
)

val VEDIC_ARTISTIC_THEMES = listOf(
    VedicThemeDescriptor(
        style = WatchFaceStyle.YANTRA,
        title = "Traditional Yantra Art",
        subtitle = "Sacred Sri Chakra geometry with interlocking harmonic triangles & gold filigree",
        icon = Icons.Default.Hexagon,
        accentColor = Color(0xFFFFB300),
        suggestedPrimaryHex = 0xFFFFD700,
        suggestedAccentHex = 0xFFD97706,
        features = listOf("Sri Chakra Geometry", "Bhupura Citadel", "Sacred Lotus Petals")
    ),
    VedicThemeDescriptor(
        style = WatchFaceStyle.MINIMAL,
        title = "Minimalist Modern",
        subtitle = "Contemporary luxury dial with sleek Vedic indices, high contrast & battery-saving AMOLED black",
        icon = Icons.Default.FilterFrames,
        accentColor = Color(0xFF38BDF8),
        suggestedPrimaryHex = 0xFFFFFFFF,
        suggestedAccentHex = 0xFF38BDF8,
        features = listOf("AMOLED True Black", "Minimalist Numerals", "Ultra-Low Power")
    ),
    VedicThemeDescriptor(
        style = WatchFaceStyle.COSMIC,
        title = "Cosmic Planetary Orbit",
        subtitle = "Dynamic Vedic zodiac chapter ring, planetary orbit tracks & live sun-moon glyphs",
        icon = Icons.Default.Public,
        accentColor = Color(0xFF818CF8),
        suggestedPrimaryHex = 0xFFFFD166,
        suggestedAccentHex = 0xFF818CF8,
        features = listOf("Live Zodiac Orbit", "Sun & Moon Glyphs", "Planetary Nodes")
    ),
    VedicThemeDescriptor(
        style = WatchFaceStyle.CHAKRA,
        title = "Sacred Chakra Mandala",
        subtitle = "Seven-chakra energy vortex rings with blooming 16-petal lotus geometry",
        icon = Icons.Default.Brightness7,
        accentColor = Color(0xFFF472B6),
        suggestedPrimaryHex = 0xFFEC4899,
        suggestedAccentHex = 0xFFA855F7,
        features = listOf("7 Kundalini Rings", "Lotus Petal Dial", "Pranic Aura")
    ),
    VedicThemeDescriptor(
        style = WatchFaceStyle.ROYAL,
        title = "Royal Temple Gold",
        subtitle = "Opulent Rajasthani & Mughal heritage motifs with gold borders & Vedic shloka accents",
        icon = Icons.Default.WorkspacePremium,
        accentColor = Color(0xFFFFD700),
        suggestedPrimaryHex = 0xFFFFD700,
        suggestedAccentHex = 0xFFDC2626,
        features = listOf("Imperial Gold Trims", "Vedic Shloka Inscriptions", "Royal Crimson Dials")
    ),
    VedicThemeDescriptor(
        style = WatchFaceStyle.VASTU,
        title = "Vastu Elemental Compass",
        subtitle = "8-directional Mahabhuta quadrant compass with presiding directional guardian deities",
        icon = Icons.Default.Explore,
        accentColor = Color(0xFF34D399),
        suggestedPrimaryHex = 0xFF10B981,
        suggestedAccentHex = 0xFFF59E0B,
        features = listOf("8-Direction Compass", "Mahabhuta Elements", "Digpala Resonance")
    ),
    VedicThemeDescriptor(
        style = WatchFaceStyle.PANCHANGA,
        title = "Vedic Panchanga Classic",
        subtitle = "Traditional Devanagari hour numerals with real-time Choghadiya and Hora rings",
        icon = Icons.Default.CalendarToday,
        accentColor = Color(0xFFFF8A65),
        suggestedPrimaryHex = 0xFFFF7043,
        suggestedAccentHex = 0xFF8D6E63,
        features = listOf("Devanagari Numerals", "Panchanga Indicators", "Hora Alignment")
    ),
    VedicThemeDescriptor(
        style = WatchFaceStyle.MODERN,
        title = "Active Modern Smartwatch",
        subtitle = "Dynamic gradient arcs, telemetry dials and modern typography for daily active sadhana",
        icon = Icons.Default.Speed,
        accentColor = Color(0xFF06B6D4),
        suggestedPrimaryHex = 0xFF38BDF8,
        suggestedAccentHex = 0xFF06B6D4,
        features = listOf("Active Telemetry", "Gradient Progress Arcs", "Smooth Escapement")
    )
)

@Composable
fun VedicArtisticThemesCard(
    currentConfig: WatchFaceConfiguration,
    onConfigChange: (WatchFaceConfiguration) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTheme by remember(currentConfig.style) {
        mutableStateOf(VEDIC_ARTISTIC_THEMES.find { it.style == currentConfig.style } ?: VEDIC_ARTISTIC_THEMES.first())
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("vedic_artistic_themes_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaroonPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaroonPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Vedic Artistic Themes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary
                            )
                        )
                        Text(
                            text = "Watch Face Background & Aesthetics",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GrayText,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = selectedTheme.accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, selectedTheme.accentColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = selectedTheme.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Toggle between authentic Vedic artistic themes and contemporary layouts. Every background style is optimized for AMOLED battery efficiency and high-contrast smartwatch legibility.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            )

            // Horizontal Scrollable Theme Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VEDIC_ARTISTIC_THEMES.forEach { theme ->
                    val isSelected = (theme.style == currentConfig.style)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaroonPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaroonPrimary else LightBorder
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedTheme = theme
                                onConfigChange(
                                    currentConfig.copy(
                                        style = theme.style,
                                        primaryColorHex = theme.suggestedPrimaryHex,
                                        accentColorHex = theme.suggestedAccentHex
                                    )
                                )
                            }
                            .testTag("artistic_theme_chip_${theme.style.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = theme.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaroonPrimary else GrayText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = theme.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaroonPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // Detailed Active Theme Highlight Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, selectedTheme.accentColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(selectedTheme.accentColor)
                            )
                            Text(
                                text = selectedTheme.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary
                                )
                            )
                        }
                        Text(
                            text = "Active Theme",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AuspiciousGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Text(
                        text = selectedTheme.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )

                    // Feature Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedTheme.features.forEach { feature ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = selectedTheme.accentColor.copy(alpha = 0.12f),
                                border = BorderStroke(0.5.dp, selectedTheme.accentColor.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = "✦ $feature",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
