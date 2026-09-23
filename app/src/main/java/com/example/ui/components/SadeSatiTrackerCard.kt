package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Graha
import com.example.core.model.Rashi
import com.example.engines.jyotisha.SadeSatiReport
import com.example.engines.jyotisha.SadeSatiStatus
import com.example.engines.jyotisha.SadeSatiTransitEngine
import com.example.ui.theme.*

@Composable
fun SadeSatiTrackerCard(
    natalMoonSign: Rashi,
    transitSaturnSign: Rashi = Rashi.KUMBHA, // Current sidereal transit of Shani in Aquarius/Pisces
    transitJupiterSign: Rashi = Rashi.VRISHABHA, // Current sidereal transit of Guru in Taurus/Gemini
    modifier: Modifier = Modifier
) {
    val sadeSatiReport = remember(natalMoonSign, transitSaturnSign) {
        SadeSatiTransitEngine.calculateSadeSati(natalMoonSign, transitSaturnSign)
    }

    val guruReport = remember(natalMoonSign, transitJupiterSign) {
        SadeSatiTransitEngine.calculateGuruGochar(natalMoonSign, transitJupiterSign)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shani Sade Sati & Transit Tracker",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Saturn (शनि) & Jupiter (गुरु) Real-time Gochar",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                    )
                }

                Surface(
                    color = when (sadeSatiReport.status) {
                        SadeSatiStatus.NOT_ACTIVE -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                        SadeSatiStatus.PHASE_1_RISING, SadeSatiStatus.PHASE_3_SETTING -> Color(0xFFD97706).copy(alpha = 0.15f)
                        else -> MaroonPrimary.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "House ${sadeSatiReport.houseFromMoon} from Moon",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            color = when (sadeSatiReport.status) {
                                SadeSatiStatus.NOT_ACTIVE -> Color(0xFF2E7D32)
                                SadeSatiStatus.PHASE_1_RISING, SadeSatiStatus.PHASE_3_SETTING -> Color(0xFFD97706)
                                else -> MaroonPrimary
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active Status Banner
            Surface(
                color = when (sadeSatiReport.status) {
                    SadeSatiStatus.NOT_ACTIVE -> Color(0xFF2E7D32).copy(alpha = 0.08f)
                    SadeSatiStatus.PHASE_2_PEAK -> MaroonPrimary.copy(alpha = 0.08f)
                    else -> OrangeAccent.copy(alpha = 0.1f)
                },
                border = BorderStroke(
                    1.dp,
                    when (sadeSatiReport.status) {
                        SadeSatiStatus.NOT_ACTIVE -> Color(0xFF2E7D32).copy(alpha = 0.4f)
                        SadeSatiStatus.PHASE_2_PEAK -> MaroonPrimary.copy(alpha = 0.4f)
                        else -> OrangeAccent.copy(alpha = 0.4f)
                    }
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = sadeSatiReport.status.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = sadeSatiReport.status.hindiName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DarkTextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = sadeSatiReport.phaseDescription,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Key Influences
            Text(
                text = "PRIMARY THEMES & LIFE AREAS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaroonPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            sadeSatiReport.lifeImpacts.forEach { impact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("•", color = OrangeAccent, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = impact,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Traditional Classical Remedies
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Traditional Shani Remedies (उपाय)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    sadeSatiReport.traditionalRemedies.forEach { remedy ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("🙏", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = remedy,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = sadeSatiReport.classicalStotra,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaroonPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Jupiter (Guru) Transit Blessing Card
            Surface(
                color = Color(0xFFFFFBEB), // Soft golden yellow
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("♃", fontSize = 18.sp, color = Color(0xFFB45309))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Jupiter (Guru) Gochar",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            )
                        }
                        Surface(
                            color = if (guruReport.isAuspicious) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (guruReport.isAuspicious) "Auspicious (शुभ)" else "Reflective",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (guruReport.isAuspicious) Color(0xFF2E7D32) else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = guruReport.blessingTitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = guruReport.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF451A03),
                            lineHeight = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Suggested Sadhana: ${guruReport.suggestedSadhana}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
