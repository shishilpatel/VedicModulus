package com.example.ui.screens.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.GenerativeModel
import com.example.BuildConfig
import com.example.core.model.*
import com.example.engines.ai.AstrologicalCalculationAiBridge
import com.example.engines.export.VedicPdfExporter
import com.example.engines.jyotisha.TarabalaEngine
import com.example.ui.components.PujaScheduleSection
import com.example.ui.components.ZodiacMoonPhaseOverlayCard
import com.example.ui.theme.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PanchangaTab(
    dailyProfile: DailyVedicProfile,
    personalizedProfile: PersonalizedProfile,
    selectedDateOffset: Int,
    onDateOffsetChange: (Int) -> Unit,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val p = dailyProfile.panchanga
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    val tarabalaEngine = remember { TarabalaEngine() }

    var aiInsight by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val harmonyScore = remember(personalizedProfile, p.nakshatra) {
        // Derive transit Moon sign from nakshatra position approximately
        val moonSignIdx = ((p.nakshatra.number - 1) * 4 + p.nakshatraPada - 1) / 9
        val transitMoonRashi = Rashi.values()[moonSignIdx.coerceIn(0, 11)]

        tarabalaEngine.calculateDailyHarmony(
            natalNakshatra = personalizedProfile.natalNakshatra,
            natalChandraRashi = personalizedProfile.natalChandraRashi,
            transitNakshatra = p.nakshatra,
            transitChandraRashi = transitMoonRashi
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Date travel selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onDateOffsetChange(selectedDateOffset - 1) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Day", tint = MaroonPrimary)
            }

            Text(
                text = when (selectedDateOffset) {
                    0 -> "Today (${dailyProfile.currentZonedDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))})"
                    1 -> "Tomorrow (${dailyProfile.currentZonedDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))})"
                    -1 -> "Yesterday (${dailyProfile.currentZonedDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))})"
                    else -> dailyProfile.currentZonedDateTime.format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy"))
                },
                color = MaroonPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            IconButton(
                onClick = { onDateOffsetChange(selectedDateOffset + 1) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Day", tint = MaroonPrimary)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // PDF Report Export Button
        OutlinedButton(
            onClick = {
                val pdfFile = VedicPdfExporter.exportDailyPanchangToPdf(
                    context = context,
                    dailyProfile = dailyProfile,
                    cityName = personalizedProfile.userBirthProfile.birthLocation.cityName
                )
                if (pdfFile != null) {
                    VedicPdfExporter.sharePdf(
                        context = context,
                        pdfFile = pdfFile,
                        subject = "Dainik Vedic Panchang Report",
                        message = "Here is today's authentic Dainik Vedic Panchang report with Muhurtas and Choghadiya."
                    )
                } else {
                    Toast.makeText(context, "Could not generate PDF", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaroonPrimary),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("export_daily_panchang_pdf_button")
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Export PDF",
                modifier = Modifier.size(16.dp),
                tint = MaroonPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.export_dainik_panchang),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI Translated Dainik Panchanga Guidance Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.panchanga_ai_insight),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Button(
                        onClick = {
                            isAiLoading = true
                            scope.launch {
                                try {
                                    aiInsight = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        AstrologicalCalculationAiBridge.interpretPanchanga(
                                            panchanga = p,
                                            dailyHarmony = harmonyScore,
                                            currentLanguage = currentLanguage
                                        )
                                    }
                                } catch (e: Exception) {
                                    aiInsight = "Error generating insight: ${e.message}"
                                } finally {
                                    isAiLoading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        enabled = !isAiLoading
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isAiLoading) stringResource(R.string.calculating_stars) else stringResource(R.string.generate_ai_panchanga),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (isAiLoading) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val langPromptName = com.example.core.language.LanguageManager.getLanguagePromptName(currentLanguage)
                    Text(
                        text = stringResource(R.string.translating_panchanga, langPromptName),
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                    )
                } else if (aiInsight != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = aiInsight!!,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Daily Personal Cosmic Harmony Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, if (harmonyScore.overallScorePercent >= 60) AuspiciousGreen else InauspiciousRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.personal_cosmic_harmony),
                        style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${harmonyScore.overallScorePercent}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = if (harmonyScore.overallScorePercent >= 60) AuspiciousGreen else InauspiciousRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.tarabala_fmt, harmonyScore.taraType.displayName),
                    style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = harmonyScore.advice,
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = stringResource(R.string.janma_star_fmt, personalizedProfile.natalNakshatra.sanskritName),
                        fontSize = 11.sp,
                        color = GrayText
                    )
                    Text(
                        text = stringResource(R.string.transit_star_fmt, p.nakshatra.sanskritName),
                        fontSize = 11.sp,
                        color = MaroonPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ZodiacMoonPhaseOverlayCard(dailyProfile = dailyProfile)

        Spacer(modifier = Modifier.height(8.dp))

        // Solar Times Hero Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.surya_transit_events),
                    style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricItem("Sunrise (सूर्योदय)", p.sunrise.format(timeFormatter), Icons.Default.AddCircle)
                    MetricItem("Solar Noon", p.solarNoon.format(timeFormatter), Icons.Default.Info)
                    MetricItem("Sunset (सूर्यास्त)", p.sunset.format(timeFormatter), Icons.Default.Star)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Panchanga 5 Limbs
        Text(
            text = stringResource(R.string.five_limbs_panchanga),
            style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        PanchangaLimbCard("1. Vara (Solar Day)", p.vara, p.varaDevanagari, "Ruled by celestial day lord")
        Spacer(modifier = Modifier.height(6.dp))
        PanchangaLimbCard("2. Tithi (Lunar Phase)", "${p.tithi.name} (${p.tithi.paksha.displayName})", p.tithi.devanagari, "Moon-Sun longitudinal separation (Tithi #${p.tithi.number})")
        Spacer(modifier = Modifier.height(6.dp))
        PanchangaLimbCard("3. Nakshatra (Lunar Mansion)", "${p.nakshatra.sanskritName} (Pada ${p.nakshatraPada})", p.nakshatra.devanagari, "Moon residing in 13°20' arc segment (Lord: ${p.nakshatra.rulingGraha.displayName})")
        Spacer(modifier = Modifier.height(6.dp))
        PanchangaLimbCard("4. Yoga (Solar-Lunar Harmony)", p.yoga, "योग", "Combined angular sum of luminaries")
        Spacer(modifier = Modifier.height(6.dp))
        PanchangaLimbCard("5. Karana (Half Tithi)", p.karana, "करण", "Action-oriented half-tithi interval")

        Spacer(modifier = Modifier.height(10.dp))

        // Vedic Calendar Cycles
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.calendrical_eras_seasons),
                    style = MaterialTheme.typography.titleSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.lunar_month_masa), color = DarkTextSecondary)
                    Text(p.masa, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.season_ritu), color = DarkTextSecondary)
                    Text(p.ritu, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.solar_course_ayana), color = DarkTextSecondary)
                    Text(p.ayana, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.vikram_samvat), color = DarkTextSecondary)
                    Text("${p.vikramSamvat}", color = MaroonPrimary, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.shaka_samvat), color = DarkTextSecondary)
                    Text("${p.shakaSamvat}", color = MaroonPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sacred Puja & Vrata Schedule Manager with Native Calendar Sync & Reminders
        PujaScheduleSection(referenceDate = dailyProfile.currentZonedDateTime)
    }
}

@Composable
private fun PanchangaLimbCard(title: String, value: String, devanagari: String, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelMedium.copy(color = OrangeAccent))
                Text(value, style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = GrayText))
            }
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, LightBorder)
            ) {
                Text(
                    text = devanagari,
                    color = MaroonPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, time: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(time, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = GrayText, fontSize = 10.sp)
    }
}
