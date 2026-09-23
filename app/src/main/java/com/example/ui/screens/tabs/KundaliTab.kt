package com.example.ui.screens.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import android.widget.Toast

import com.example.core.model.*
import com.example.engines.ai.AstrologicalCalculationAiBridge
import com.example.engines.astronomy.AstronomicalEngineProvider
import com.example.engines.export.VedicPdfExporter
import com.example.engines.jyotisha.JyotishaEngine
import com.example.engines.jyotisha.ShodashvargaEngine
import com.example.ui.components.NorthIndianKundaliCanvas
import com.example.ui.components.SouthIndianKundaliCanvas
import com.example.ui.components.EastIndianKundaliCanvas
import com.example.ui.components.CircularKundaliCanvas
import com.example.ui.components.BhavaDeepInspectorCard
import com.example.ui.components.DashaTimelineCard
import com.example.ui.components.KundliMatchingCard
import com.example.ui.components.SadeSatiTrackerCard
import com.example.ui.components.ShadbalaBreakdownCard
import com.example.ui.components.AshtakavargaHeatmapCard
import com.example.ui.components.ShodashvargaGridSelectorCard
import com.example.ui.components.VedicAlertsPreferenceCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.Locale

import com.example.pdf.KundaliPdfExporter

@Composable
fun KundaliTab(
    profile: BirthProfile,
    personalizedProfile: PersonalizedProfile,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val jyotishaEngine = remember { JyotishaEngine(AstronomicalEngineProvider.instance) }

    val natalSummaryState = remember { mutableStateOf<com.example.engines.jyotisha.JyotishaEngine.NatalChartSummary?>(null) }
    val natalSummary = natalSummaryState.value
    val kundaliDataState = remember { mutableStateOf<com.example.core.model.KundaliChartData?>(null) }
    val kundaliData = kundaliDataState.value
    
    LaunchedEffect(profile) {
        val summary = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            jyotishaEngine.calculateNatalChart(
                birthDate = profile.birthDate,
                birthTime = profile.birthTime,
                location = profile.birthLocation,
                ayanamsha = profile.ayanamsha
            )
        }
        natalSummaryState.value = summary
        kundaliDataState.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            jyotishaEngine.generateKundaliChart(summary)
        }
    }

    if (natalSummary == null || kundaliData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaroonPrimary)
        }
        return
    }

    var selectedHouseNum by remember { mutableStateOf(1) }
    var selectedChartStyle by remember { mutableStateOf(KundaliChartStyle.NORTH_INDIAN) }
    val shodashvargaEngine = remember { ShodashvargaEngine() }
    var selectedVarga by remember { mutableStateOf(ShodashvargaEngine.VargaType.D1) }

    val activeChartData = remember(selectedVarga, natalSummary, kundaliData) {
        if (selectedVarga == ShodashvargaEngine.VargaType.D1) {
            kundaliData
        } else {
            shodashvargaEngine.calculateDivisionalChart(
                vargaType = selectedVarga,
                planetaryPositions = natalSummary.planetaryPositions,
                lagnaDegrees = natalSummary.lagnaDegrees
            )
        }
    }

    val selectedBhava = remember(selectedHouseNum, activeChartData) {
        activeChartData.bhavas.firstOrNull { it.houseNumber == selectedHouseNum } ?: activeChartData.bhavas[0]
    }

    val context = LocalContext.current
    var transitOffsetDays by remember { mutableStateOf(0) }
    val dtfTransit = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy") }

    val targetTransitDate = remember(transitOffsetDays) {
        ZonedDateTime.now().plusDays(transitOffsetDays.toLong())
    }

    val transitPositions: Map<Graha, Double> = remember(targetTransitDate, profile.ayanamsha) {
        val engine = AstronomicalEngineProvider.instance
        val ayanamsha = engine.calculateLahiriAyanamsha(targetTransitDate)
        engine.planetaryPositions(targetTransitDate, ayanamsha).mapValues { it.value.siderealLongitudeDeg }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header summary
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.lagna_kundali_d1),
                            style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = profile.ayanamsha.displayName.substringBefore(" "),
                            style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.ascendant_lagna), style = MaterialTheme.typography.labelSmall.copy(color = GrayText))
                            Text(
                                text = "${natalSummary.lagnaRashi.sanskritName} ${String.format(Locale.US, "%.1f°", natalSummary.lagnaDegrees)}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.moon_sign_janma), style = MaterialTheme.typography.labelSmall.copy(color = GrayText))
                            Text(
                                text = "${natalSummary.chandraRashi.sanskritName} ${natalSummary.chandraRashi.symbol}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.sun_sign_surya), style = MaterialTheme.typography.labelSmall.copy(color = GrayText))
                            Text(
                                text = "${natalSummary.suryaRashi.sanskritName} ${natalSummary.suryaRashi.symbol}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // Export Official PDF Button
                    Button(
                        onClick = {
                            try {
                                val pdfFile = KundaliPdfExporter.generateKundaliPdf(context, profile)
                                KundaliPdfExporter.sharePdf(context, pdfFile)
                            } catch (e: Exception) {
                                Toast.makeText(context, "PDF Generation Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("export_kundali_pdf_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0F172A)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Export Official Janam Kundali PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // AI Vedic Kundli Interpretation Card
            var aiKundliAnalysis by remember { mutableStateOf<String?>(null) }
            var isAnalyzingKundli by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ai_kundli_analysis_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Button(
                            onClick = {
                                isAnalyzingKundli = true
                                coroutineScope.launch {
                                    try {
                                        aiKundliAnalysis = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                            AstrologicalCalculationAiBridge.interpretKundali(
                                                natalSummary = natalSummary,
                                                kundaliData = kundaliData,
                                                currentLanguage = currentLanguage
                                            )
                                        }
                                    } catch (e: Exception) {
                                        aiKundliAnalysis = "Error analyzing Kundli: ${e.message}"
                                    } finally {
                                        isAnalyzingKundli = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            enabled = !isAnalyzingKundli
                        ) {
                            if (isAnalyzingKundli) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isAnalyzingKundli) stringResource(R.string.calculating_stars) else stringResource(R.string.generate_ai_kundli_interpretation),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (isAnalyzingKundli) {
                        val langPromptName = com.example.core.language.LanguageManager.getLanguagePromptName(currentLanguage)
                        Text(
                            text = stringResource(R.string.translating_kundli, langPromptName),
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 11.sp)
                        )
                    } else if (aiKundliAnalysis != null) {
                        HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                        Text(
                            text = aiKundliAnalysis!!,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.5.sp)
                        )
                    }
                }
            }

            // Regional Kundali Method Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "KUNDALI CHART METHOD / भारतीय कुंडली पद्धति",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        KundaliChartStyle.values().forEach { style ->
                            val isSelected = style == selectedChartStyle
                            Surface(
                                color = if (isSelected) OrangeAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isSelected) MaroonPrimary else LightBorder),
                                modifier = Modifier.clickable { selectedChartStyle = style }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = style.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaroonPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = style.hindiTitle,
                                        fontSize = 9.sp,
                                        color = if (isSelected) MaroonPrimary else DarkTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${selectedChartStyle.region} • ${selectedChartStyle.description}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Shodashvarga 16 Divisional Chart Selector Card
            ShodashvargaGridSelectorCard(
                selectedVarga = selectedVarga,
                onVargaSelected = { selectedVarga = it }
            )

            // Interactive Chart Canvas (North Indian, South Indian, East Indian, Circular)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Viewing ${selectedVarga.sanskritName} (${selectedVarga.title}) • Tap house to inspect",
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 11.sp)
                )

                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val chartSize = minOf(maxWidth - 20.dp, 300.dp).coerceAtLeast(240.dp)
                    when (selectedChartStyle) {
                        KundaliChartStyle.NORTH_INDIAN -> {
                            NorthIndianKundaliCanvas(
                                chartData = activeChartData,
                                selectedHouse = selectedHouseNum,
                                onHouseSelected = { selectedHouseNum = it },
                                modifier = Modifier
                                    .size(chartSize)
                                    .testTag("kundali_chart_canvas")
                            )
                        }
                        KundaliChartStyle.SOUTH_INDIAN -> {
                            SouthIndianKundaliCanvas(
                                chartData = activeChartData,
                                selectedHouse = selectedHouseNum,
                                onHouseSelected = { selectedHouseNum = it },
                                modifier = Modifier
                                    .size(chartSize)
                                    .testTag("kundali_chart_canvas")
                            )
                        }
                        KundaliChartStyle.EAST_INDIAN -> {
                            EastIndianKundaliCanvas(
                                chartData = activeChartData,
                                selectedHouse = selectedHouseNum,
                                onHouseSelected = { selectedHouseNum = it },
                                modifier = Modifier
                                    .size(chartSize)
                                    .testTag("kundali_chart_canvas")
                            )
                        }
                        KundaliChartStyle.CIRCULAR -> {
                            CircularKundaliCanvas(
                                chartData = activeChartData,
                                selectedHouse = selectedHouseNum,
                                onHouseSelected = { selectedHouseNum = it },
                                modifier = Modifier
                                    .size(chartSize)
                                    .testTag("kundali_chart_canvas")
                            )
                        }
                    }
                }
            }

            // Interactive Gochar (Transit) Timeline Slider Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GOCHAR (TRANSIT) TIMELINE SLIDER",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaroonPrimary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "Scrub dates to view real-time transiting planetary influences",
                                style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                            )
                        }
                        Surface(
                            color = if (transitOffsetDays == 0) AuspiciousGreen.copy(alpha = 0.15f) else OrangeAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (transitOffsetDays == 0) AuspiciousGreen else OrangeAccent)
                        ) {
                            Text(
                                text = if (transitOffsetDays == 0) "TODAY" else if (transitOffsetDays > 0) "+$transitOffsetDays DAYS" else "$transitOffsetDays DAYS",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = if (transitOffsetDays == 0) AuspiciousGreen else OrangeAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Active Transit Date display
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Transit Date: ${targetTransitDate.format(dtfTransit)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                            if (transitOffsetDays != 0) {
                                TextButton(
                                    onClick = { transitOffsetDays = 0 },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("Reset to Today", fontSize = 10.5.sp, color = MaroonPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Timeline Slider (-30 to +365 days)
                    Slider(
                        value = transitOffsetDays.toFloat(),
                        onValueChange = { transitOffsetDays = it.toInt() },
                        valueRange = -30f..365f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaroonPrimary,
                            activeTrackColor = OrangeAccent,
                            inactiveTrackColor = LightBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick preset buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val presets = listOf(
                            Pair("Past 30d", -30),
                            Pair("Today", 0),
                            Pair("+1 Month", 30),
                            Pair("+3 Months", 90),
                            Pair("+6 Months", 180),
                            Pair("+1 Year", 365)
                        )
                        presets.forEach { (label, days) ->
                            val isSelected = transitOffsetDays == days
                            FilterChip(
                                selected = isSelected,
                                onClick = { transitOffsetDays = days },
                                label = { Text(label, fontSize = 10.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaroonPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = MaroonPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) MaroonPrimary else LightBorder
                                )
                            )
                        }
                    }

                    // Key Transit Grahas Live Indicators
                    Text(
                        text = "Key Graha Transits for this Date:",
                        style = MaterialTheme.typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val keyTransitGrahas = listOf(Graha.GURU, Graha.SHANI, Graha.RAHU, Graha.KETU, Graha.SURYA)
                        keyTransitGrahas.forEach { graha ->
                            val deg = transitPositions[graha] ?: 0.0
                            val rashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)
                            val rashi = Rashi.values()[rashiIdx]
                            val houseNum = ((rashiIdx - natalSummary.lagnaRashi.ordinal + 12) % 12) + 1

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, LightBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${graha.symbol} ${graha.displayName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaroonPrimary
                                    )
                                    Text(
                                        text = "in ${rashi.sanskritName} (H$houseNum)",
                                        fontSize = 10.sp,
                                        color = DarkTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Bhava Inspector with House Lords, Drishti, Dignity & Gochar
            BhavaDeepInspectorCard(
                selectedHouseNum = selectedHouseNum,
                chartData = kundaliData,
                transitPlanets = transitPositions
            )

            // Planetary Longitudes Table
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Planetary Longitudes (Nirayana)",
                        style = MaterialTheme.typography.titleSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                    )

                    natalSummary.planetaryPositions.forEach { (graha, deg) ->
                        val rashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)
                        val rashi = Rashi.values()[rashiIdx]
                        val degInRashi = deg % 30.0
                        val houseNum = ((rashiIdx - natalSummary.lagnaRashi.ordinal + 12) % 12) + 1

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(graha.symbol, color = MaroonPrimary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(graha.displayName, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.5.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                com.example.ui.components.icons.ModernZodiacIcon(
                                    rashi = rashi,
                                    size = 14.dp,
                                    isRealistic = true
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${rashi.sanskritName} ${String.format(Locale.US, "%.1f°", degInRashi)} (H$houseNum)",
                                    color = DarkTextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            // Ascendant Report Card
            personalizedProfile.ascendantReport?.let { report ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Ascendant (Lagna) Report",
                            style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Your Ascendant: ${report.lagnaName}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Ruling Planet: ${report.rulingPlanet}",
                            style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent)
                        )
                        Text(
                            text = report.coreNature,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "Physical Traits: ${report.physicalTraits}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "Mental Traits: ${report.mentalTraits}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Lucky Colors: ${report.luckyColors.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Lucky Days: ${report.luckyDays.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            // Interactive Dasha & Antardasha Timeline Card
            if (personalizedProfile.dashaPeriods.isNotEmpty()) {
                DashaTimelineCard(
                    dashaPeriods = personalizedProfile.dashaPeriods
                )
            }

            // Real-time Saturn Sade Sati & Jupiter Gochar Tracker
            SadeSatiTrackerCard(
                natalMoonSign = personalizedProfile.natalChandraRashi
            )

            // Interactive Vedic Ashtakoota Milan (36 Gunas) & Manglik Matcher
            KundliMatchingCard(
                userProfile = profile
            )

            // Shadbala 6-Fold Planetary Strength & Dignity Breakdown Card
            ShadbalaBreakdownCard(
                planetaryPositions = natalSummary.planetaryPositions,
                lagnaDegrees = natalSummary.lagnaDegrees,
                isDayBirth = profile.birthTime.hour in 6..18
            )

            // Ashtakavarga 12-House Cosmic Heatmap & Bhinnashtakavarga Card
            AshtakavargaHeatmapCard(
                planetaryPositions = natalSummary.planetaryPositions,
                lagnaDegrees = natalSummary.lagnaDegrees
            )

            // Vedic Muhurta Alarms & Notification Preferences
            VedicAlertsPreferenceCard()
        }
    }
}
