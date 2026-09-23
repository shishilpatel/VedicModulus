package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.engines.astronomy.MoonPhaseCalculator
import com.example.core.model.VedicAssetRegistry
import com.example.core.model.DailyVedicProfile
import com.example.ui.theme.*

@Composable
fun MoonPhaseCard(
    dailyProfile: DailyVedicProfile,
    modifier: Modifier = Modifier
) {
    val phaseAngle = MoonPhaseCalculator.calculateLunarPhaseAngle(dailyProfile.currentZonedDateTime)
    val moonAssetId = MoonPhaseCalculator.getMoonPhaseAssetId(phaseAngle)
    val moonAsset = VedicAssetRegistry.getAsset(moonAssetId)

    val progress = dailyProfile.moonPhaseProgress
    val phaseName = moonAsset?.title ?: "Moon Phase"
    val illumination = (kotlin.math.sin((progress - 0.25f) * 2 * Math.PI) * 50 + 50).toInt().coerceIn(0, 100)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, LightBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LUNAR PHASE & MANTRA",
                        style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = phaseName,
                        style = MaterialTheme.typography.titleMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (moonAsset != null) {
                        Image(
                            painter = painterResource(id = moonAsset.drawableResId),
                            contentDescription = "Moon Phase",
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(40.dp)) {
                            val radius = size.minDimension / 2f
                            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                            drawCircle(color = androidx.compose.ui.graphics.Color(0xFF2C2C3E), radius = radius, center = center)
                            drawCircle(color = androidx.compose.ui.graphics.Color(0xFFFFD700), radius = radius, center = center, alpha = (illumination / 100f).coerceIn(0.15f, 1f))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaroonPrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$illumination% Illum.",
                            color = OnMaroon,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = OrangeAccent,
                trackColor = LightBorder
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Moon Cycle Progress: ${(progress * 100).toInt()}% • Tithi: ${dailyProfile.panchanga.tithi.name}",
                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
            )
        }
    }
}
