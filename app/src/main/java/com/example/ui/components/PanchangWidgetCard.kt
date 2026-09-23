package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.LocationData
import com.example.engines.astronomy.AstronomicalEngineProvider
import com.example.engines.panchanga.PanchangaEngine
import com.example.ui.theme.*
import java.time.ZonedDateTime

@Composable
fun PanchangWidgetCard(
    location: LocationData,
    modifier: Modifier = Modifier
) {
    val zonedDateTime = remember { ZonedDateTime.now(java.time.ZoneId.of(location.timezoneId)) }
    val engine = remember { PanchangaEngine(AstronomicalEngineProvider.instance) }
    
    val panchang = remember(location, zonedDateTime) {
        try {
            engine.calculatePanchanga(
                dateTime = zonedDateTime,
                latitude = location.latitude,
                longitude = location.longitude
            )
        } catch (e: Exception) {
            null
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("panchang_widget_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaroonPrimary.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaroonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DAINIK PANCHANGA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "${panchang?.vara ?: "Vara"} • ${location.cityName}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Surface(
                    color = OrangeAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Vedic Live",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OrangeAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = LightBorder,
                thickness = 0.8.dp
            )

            if (panchang != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PanchangLimbItem(
                        title = "TITHI",
                        name = panchang.tithi.name,
                        sanskrit = panchang.tithi.devanagari,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PanchangLimbItem(
                        title = "NAKSHATRA",
                        name = panchang.nakshatra.sanskritName,
                        sanskrit = "Pada ${panchang.nakshatraPada}",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PanchangLimbItem(
                        title = "YOGA",
                        name = panchang.yoga,
                        sanskrit = "Solar-Lunar",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = "Calculating accurate panchang...",
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun PanchangLimbItem(
    title: String,
    name: String,
    sanskrit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, LightBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GrayText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaroonPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sanskrit,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = DarkTextSecondary,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}
