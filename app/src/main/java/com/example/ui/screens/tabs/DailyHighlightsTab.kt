package com.example.ui.screens.tabs

import androidx.compose.ui.res.stringResource
import com.example.R
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.core.model.DailyVedicProfile
import com.example.core.model.Graha
import com.example.core.model.LocationData
import com.example.core.model.PersonalizedProfile
import com.example.data.CityRepository
import com.example.data.LocationSyncState
import com.example.data.UserLocationSyncManager
import com.example.engines.astronomy.VedicGreeting
import com.example.engines.astronomy.VedicTimeCalculator
import com.example.ui.components.AdvancedFeaturesCard
import com.example.ui.components.KalaChakraDialog
import com.example.ui.components.MoonPhaseCard
import com.example.ui.components.NumerologyWidgetCard
import com.example.ui.components.PanchangaWidgetsGrid
import com.example.ui.components.TithiCalculatorCard
import com.example.ui.components.TransitCalendarCard
import com.example.ui.components.TithiNakshatraWidgetCard
import com.example.ui.components.VedicRealtimeSpashtaCard
import com.example.ui.components.VedicTimeCard
import com.example.ui.components.DailyNumerologyCard
import com.example.ui.components.AiDailyHoroscopeCard
import com.example.ui.components.AiDailyNumerologyCard
import com.example.ui.components.PanchangWidgetCard
import com.example.ui.components.VedicTimelineCard
import com.example.ui.theme.*
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DailyHighlightsTab(
    dailyProfile: DailyVedicProfile,
    personalizedProfile: PersonalizedProfile,
    currentLocation: LocationData = personalizedProfile.userBirthProfile.birthLocation,
    onLocationSynced: ((LocationData) -> Unit)? = null,
    onRefresh: () -> Unit = {},
    onNavigateToVastu: (() -> Unit)? = null,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationSyncManager = remember { UserLocationSyncManager.getInstance() }
    val syncState by locationSyncManager.syncState.collectAsState()
    var isSyncingLocation by remember { mutableStateOf(false) }

    val zoneId = remember(currentLocation) { java.time.ZoneId.of(currentLocation.timezoneId) }

    // Permission launcher for Location Services
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch {
                isSyncingLocation = true
                val result = locationSyncManager.syncUserLocationAndTimezone(context, currentLocation)
                isSyncingLocation = false
                if (result is LocationSyncState.Synced) {
                    onLocationSynced?.invoke(result.location)
                }
            }
        }
    }

    fun triggerLocationSync() {
        if (locationSyncManager.hasLocationPermission(context)) {
            scope.launch {
                isSyncingLocation = true
                val result = locationSyncManager.syncUserLocationAndTimezone(context, currentLocation)
                isSyncingLocation = false
                if (result is LocationSyncState.Synced) {
                    onLocationSynced?.invoke(result.location)
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Initial background check if permission was already granted
    LaunchedEffect(Unit) {
        if (locationSyncManager.hasLocationPermission(context)) {
            val result = locationSyncManager.syncUserLocationAndTimezone(context, currentLocation)
            if (result is LocationSyncState.Synced) {
                onLocationSynced?.invoke(result.location)
            }
        }
    }

    // Contextual Vedic greeting based on current local time
    val greeting: VedicGreeting = remember(zoneId) {
        VedicTimeCalculator.getVedicGreeting(LocalTime.now(zoneId))
    }

    val rulingGraha = personalizedProfile.natalLagnaRashi.rulingGraha
    val ishtaMantraDetail = getIshtaInfo(rulingGraha)
    val mulank = personalizedProfile.numerology.birthNumber
    val mulankMantraDetail = getMulankMantraInfo(mulank)
    val vastuTip = getPersonalizedVastuTip(personalizedProfile.dominantVastuElement)

    var showKalaChakraDialog by remember { mutableStateOf(false) }
    var aiHoroscope by remember { mutableStateOf<String?>(null) }
    var isGeneratingHoroscope by remember { mutableStateOf(false) }

    val isGpsLive = (syncState as? LocationSyncState.Synced)?.isGpsLive ?: false

    if (showKalaChakraDialog) {
        KalaChakraDialog(
            dailyProfile = dailyProfile,
            onDismiss = { showKalaChakraDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ==========================================
        // PERSONAL GREETING
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Namaste, ${personalizedProfile.userBirthProfile.fullName.split(" ").firstOrNull() ?: ""}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Text(
                        text = "Welcome to your daily Vedic insights.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkTextSecondary
                        )
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.background(MaroonPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Data", tint = MaroonPrimary)
                }
            }
        }

        // ==========================================
        // 1. DYNAMIC TIME-BASED VEDIC GREETING BANNER
        // ==========================================
        item {
            Surface(
                color = MaroonPrimary.copy(alpha = 0.07f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Greeting Icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .background(OrangeAccent.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, OrangeAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        val iconVector = when (greeting.iconName) {
                            "sun" -> Icons.Default.WbSunny
                            "solar" -> Icons.Default.Brightness5
                            "sunset" -> Icons.Default.Brightness6
                            "moon" -> Icons.Default.NightsStay
                            else -> Icons.Default.Star
                        }
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = MaroonPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = greeting.englishTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = greeting.vedicTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = greeting.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // 1. UNIFIED SPIRITUAL & MANTRA BLESSINGS CARD
        // ==========================================
        item {
            UnifiedSpiritualMantrasCard(
                ishtaMantraDetail = ishtaMantraDetail,
                sanskritLagna = personalizedProfile.natalLagnaRashi.sanskritName,
                rulingGrahaName = rulingGraha.displayName,
                mulank = mulank,
                mulankMantraDetail = mulankMantraDetail
            )
        }

        // ==========================================
        // 2. UNIFIED DAINIK PANCHANGA & TITHI MASTER CARD
        // ==========================================
        item {
            UnifiedDainikPanchangCard(
                dailyProfile = dailyProfile,
                location = currentLocation
            )
        }

        // ==========================================
        // 3. VEDIC TIME & REGULAR TIME CARD WITH GPS SYNC
        // ==========================================
        item {
            VedicTimeCard(
                dailyProfile = dailyProfile,
                currentLocation = currentLocation,
                isGpsSynced = isGpsLive,
                onSyncLocationClick = { triggerLocationSync() },
                isSyncing = isSyncingLocation
            )
        }

        // ==========================================
        // 4. TODAY'S COSMIC SCORE CARD
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Today's Cosmic Score",
                        style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stringResource(R.string.vedic_inspired_summary),
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    dailyProfile.cosmicScore?.let { score ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ScoreCircle("Overall", score.overall, MaroonPrimary)
                            ScoreCircle("Luck", score.luck, OrangeAccent)
                            ScoreCircle("Energy", score.energy, MaroonPrimary)
                            ScoreCircle("Calm", score.calm, OrangeAccent)
                        }
                    } ?: run {
                        Text("Score not available.")
                    }
                }
            }
        }

        // ==========================================
        // 5. REAL-TIME SWISS EPHEMERIS GRAHA SPASHTA (PLANETARY DEG)
        // ==========================================
        item {
            VedicRealtimeSpashtaCard(
                currentLocation = currentLocation,
                currentZonedDateTime = dailyProfile.currentZonedDateTime
            )
        }

        // ==========================================
        // 6. ADVANCED ASTRONOMY (TRANSITS & ECLIPSES)
        // ==========================================
        item {
            AdvancedFeaturesCard(
                currentLocation = currentLocation,
                currentZonedDateTime = dailyProfile.currentZonedDateTime
            )
        }
        
        // ==========================================
        // 7. TRANSIT CALENDAR
        // ==========================================
        item {
            TransitCalendarCard(
                currentZonedDateTime = dailyProfile.currentZonedDateTime,
                userBirthProfile = personalizedProfile.userBirthProfile
            )
        }

        // ==========================================
        // 8. VEDIC TIMELINE
        // ==========================================
        item {
            VedicTimelineCard(dailyProfile = dailyProfile)
        }

        // ==========================================
        // 9. DAILY PERSONALIZED VASTU TIP CARD
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY PERSONALIZED VASTU TIP",
                            style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = OrangeAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Element: ${personalizedProfile.dominantVastuElement}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = vastuTip,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                    )
                    if (onNavigateToVastu != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onNavigateToVastu,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaroonPrimary),
                            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open Vastu Compass & Shastra",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 10. UNIFIED DAILY NUMEROLOGY & LIFE PATH INSIGHTS
        // ==========================================
        item {
            UnifiedDailyNumerologySection(
                personalizedProfile = personalizedProfile,
                dailyProfile = dailyProfile,
                currentLanguage = currentLanguage
            )
        }

        // ==========================================
        // 11. AI DAILY HOROSCOPE CARD
        // ==========================================
        item {
            AiDailyHoroscopeCard(
                personalizedProfile = personalizedProfile,
                currentLanguage = currentLanguage
            )
        }

        // ==========================================
        // 12. FESTIVALS & VRATS TODAY
        // ==========================================
        if (dailyProfile.festivals.isNotEmpty() || dailyProfile.vrats.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Festivals & Vrats Today",
                            style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        dailyProfile.festivals.forEach { festival ->
                            Text(text = "• ${festival.name}", style = MaterialTheme.typography.bodyMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold))
                            Text(text = festival.description, style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        dailyProfile.vrats.forEach { vrat ->
                            Text(text = "• ${vrat.name}", style = MaterialTheme.typography.bodyMedium.copy(color = OrangeAccent, fontWeight = FontWeight.Bold))
                            Text(text = vrat.recommendedPractice, style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        // ==========================================
        // 13. PUJA RECOMMENDATIONS
        // ==========================================
        if (dailyProfile.pujaRecommendations.isNotEmpty()) {
            item {
                val timeDisplayFormatter = DateTimeFormatter.ofPattern("hh:mm a")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Recommended Pujas",
                            style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        dailyProfile.pujaRecommendations.forEach { puja ->
                            Text(text = puja.pujaName, style = MaterialTheme.typography.bodyMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold))
                            Text(text = puja.reasoning, style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                            puja.recommendedWindow?.let { window ->
                                Text(text = "Suggested Time: ${window.startTime.format(timeDisplayFormatter)} - ${window.endTime.format(timeDisplayFormatter)}", style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // ==========================================
        // 14. MOON PHASE CARD
        // ==========================================
        item {
            MoonPhaseCard(dailyProfile = dailyProfile)
        }

        // ==========================================
        // 15. KALACHAKRA DIALOG ACTION
        // ==========================================
        item {
            Button(
                onClick = { showKalaChakraDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View KalaChakra Visualization")
            }
        }
    }
}

// ==========================================================
// CONSOLIDATED COMPONENT DEFINITIONS
// ==========================================================

data class MantraDetail(
    val deity: String,
    val sanskritMantra: String,
    val englishMantra: String
)

@Composable
private fun UnifiedSpiritualMantrasCard(
    ishtaMantraDetail: MantraDetail,
    sanskritLagna: String,
    rulingGrahaName: String,
    mulank: Int,
    mulankMantraDetail: MantraDetail,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaroonPrimary.copy(alpha = 0.07f)),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "DAILY SPIRITUAL & MANTRA BLESSINGS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Surface(
                    color = OrangeAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Natal & Numerology",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Ishta Dev Box
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.5.dp, LightBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ISHTA DEV: ${ishtaMantraDetail.deity}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.3.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Line 1: Sanskrit Mantra
                        Text(
                            text = ishtaMantraDetail.sanskritMantra,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        // Line 2: English transliteration
                        Text(
                            text = ishtaMantraDetail.englishMantra,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 15.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Natal Lagna ($sanskritLagna • $rulingGrahaName)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 9.5.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Mulank Mantra Box
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.5.dp, LightBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RULING GOD: ${mulankMantraDetail.deity}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.3.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Line 1: Sanskrit Mantra
                        Text(
                            text = mulankMantraDetail.sanskritMantra,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        // Line 2: English transliteration
                        Text(
                            text = mulankMantraDetail.englishMantra,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 15.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Birth Number $mulank (${mulankMantraDetail.deity})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 9.5.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnifiedDainikPanchangCard(
    dailyProfile: DailyVedicProfile,
    location: LocationData,
    modifier: Modifier = Modifier
) {
    val panchang = dailyProfile.panchanga
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
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
                            text = "DAINIK PANCHANGA & TITHI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "${panchang.vara} • ${location.cityName}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Surface(
                    color = AuspiciousGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(AuspiciousGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Vedic Planetary Alignment",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AuspiciousGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = LightBorder,
                thickness = 0.8.dp
            )

            // 4 Core Panchanga Elements Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PanchangElementBox(
                    title = "TITHI",
                    value = panchang.tithi.name,
                    subtext = "${panchang.tithi.paksha.displayName} • #${panchang.tithi.number}",
                    devanagari = panchang.tithi.devanagari,
                    modifier = Modifier.weight(1f)
                )
                PanchangElementBox(
                    title = "NAKSHATRA",
                    value = panchang.nakshatra.sanskritName,
                    subtext = "Pada ${panchang.nakshatraPada} • ${panchang.nakshatra.rulingGraha.displayName}",
                    devanagari = "",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PanchangElementBox(
                    title = "YOGA",
                    value = panchang.yoga,
                    subtext = "Solar-Lunar Angle",
                    devanagari = "",
                    modifier = Modifier.weight(1f)
                )
                PanchangElementBox(
                    title = "KARANA",
                    value = panchang.karana,
                    subtext = "Half Lunar Day",
                    devanagari = "",
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = LightBorder,
                thickness = 0.8.dp
            )

            // Key Muhurtas and Timings Section
            Text(
                text = "Key Daily Auspicious & Inauspicious Windows",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaroonPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                dailyProfile.brahmaMuhurta.let { brahma ->
                    TimingRow(
                        label = "Brahma Muhurta",
                        timeStr = "${brahma.startTime.format(timeFormatter)} - ${brahma.endTime.format(timeFormatter)}",
                        isAuspicious = true
                    )
                }
                dailyProfile.abhijitMuhurta?.let { abhijit ->
                    TimingRow(
                        label = "Abhijit Muhurta",
                        timeStr = "${abhijit.startTime.format(timeFormatter)} - ${abhijit.endTime.format(timeFormatter)}",
                        isAuspicious = true
                    )
                }
                dailyProfile.rahuKalam.let { rahu ->
                    TimingRow(
                        label = "Rahu Kalam",
                        timeStr = "${rahu.startTime.format(timeFormatter)} - ${rahu.endTime.format(timeFormatter)}",
                        isAuspicious = false
                    )
                }
            }
        }
    }
}

@Composable
private fun PanchangElementBox(
    title: String,
    value: String,
    subtext: String,
    devanagari: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, LightBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OrangeAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (devanagari.isNotEmpty()) {
                    Text(
                        text = devanagari,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaroonPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaroonPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = DarkTextSecondary,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TimingRow(
    label: String,
    timeStr: String,
    isAuspicious: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isAuspicious) AuspiciousGreen else InauspiciousRed,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        Text(
            text = timeStr,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isAuspicious) MaroonPrimary else InauspiciousRed,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun UnifiedDailyNumerologySection(
    personalizedProfile: PersonalizedProfile,
    dailyProfile: DailyVedicProfile,
    currentLanguage: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "DAILY NUMEROLOGY & LIFE PATH INSIGHTS",
            style = MaterialTheme.typography.labelSmall.copy(
                color = OrangeAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(start = 2.dp)
        )

        DailyNumerologyCard(
            birthProfile = personalizedProfile.userBirthProfile,
            currentDate = dailyProfile.currentZonedDateTime
        )

        NumerologyWidgetCard(numerologyProfile = personalizedProfile.numerology)

        AiDailyNumerologyCard(
            birthProfile = personalizedProfile.userBirthProfile,
            currentDate = dailyProfile.currentZonedDateTime,
            currentLanguage = currentLanguage
        )
    }
}

private fun getIshtaInfo(graha: Graha): MantraDetail {
    return when (graha) {
        Graha.SURYA -> MantraDetail("Lord Surya / Rama", "ॐ घृणि सूर्याय नमः", "Om Ghrini Suryaya Namah")
        Graha.CHANDRA -> MantraDetail("Goddess Gauri / Shiva", "ॐ सोमाय नमः", "Om Somaya Namah")
        Graha.MANGALA -> MantraDetail("Lord Hanuman / Kartikeya", "ॐ भौमाय नमः", "Om Bhaumaya Namah")
        Graha.BUDHA -> MantraDetail("Lord Vishnu", "ॐ बुधाय नमः", "Om Budhaya Namah")
        Graha.GURU -> MantraDetail("Lord Shiva / Dakshinamurthy", "ॐ बृं बृहस्पतये नमः", "Om Brim Brihaspataye Namah")
        Graha.SHUKRA -> MantraDetail("Goddess Lakshmi", "ॐ शुं शुक्राय नमः", "Om Shum Shukraya Namah")
        Graha.SHANI -> MantraDetail("Lord Hanuman / Shani Dev", "ॐ शं शनैश्चराय नमः", "Om Sham Shanaishcharaya Namah")
        Graha.RAHU -> MantraDetail("Goddess Durga", "ॐ दुं दुर्गायै नमः", "Om Dum Durgayai Namah")
        Graha.KETU -> MantraDetail("Lord Ganesha", "ॐ गं गणपतये नमः", "Om Gam Ganapataye Namah")
    }
}

private fun getPersonalizedVastuTip(element: String): String {
    return when (element.uppercase()) {
        "WATER" -> "Your element is Water (Jala). Ensure your North-East quadrant is unobstructed and houses clean water features or a drinking water station to attract uninterrupted prosperity."
        "FIRE" -> "Your element is Fire (Agni). Keep your South-East kitchen and stove area bright, clean, and maintain a red or copper accent to bolster household vitality and financial flow."
        "EARTH" -> "Your element is Earth (Prithvi). Heavy furniture, master bedroom placement, and cash lockers should be securely anchored in the South-West zone for stability."
        "AIR" -> "Your element is Air (Vayu). Keep your North-West sector airy and clutter-free to ensure smooth social networking, career circulation, and positive relationships."
        else -> "Your element is Space (Akasha). Ensure your central Brahmasthana remains open, well-lit, and entirely free of heavy structural clutter for universal prana flow."
    }
}

@Composable
private fun ScoreCircle(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.CircleShape)
                .border(2.dp, color, androidx.compose.foundation.shape.CircleShape)
        ) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(color = MaroonPrimary)
        )
    }
}

@Composable
private fun HighlightRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Medium)
        )
    }
}

private fun getMulankMantraInfo(mulank: Int): MantraDetail {
    return when (mulank) {
        1 -> MantraDetail("Surya (Sun)", "ॐ घृणि सूर्याय नमः", "Om Ghrini Suryaya Namah")
        2 -> MantraDetail("Chandra (Moon)", "ॐ सोमाय नमः", "Om Somaya Namah")
        3 -> MantraDetail("Guru (Jupiter)", "ॐ बृं बृहस्पतये नमः", "Om Brim Brihaspataye Namah")
        4 -> MantraDetail("Rahu", "ॐ राम राहुवे नमः", "Om Ram Rahave Namah")
        5 -> MantraDetail("Budha (Mercury)", "ॐ बुधाय नमः", "Om Budhaya Namah")
        6 -> MantraDetail("Shukra (Venus)", "ॐ शुं शुक्राय नमः", "Om Shum Shukraya Namah")
        7 -> MantraDetail("Ketu", "ॐ गं गणपतये नमः", "Om Gam Ganapataye Namah")
        8 -> MantraDetail("Shani (Saturn)", "ॐ शं शनैश्चराय नमः", "Om Sham Shanaishcharaya Namah")
        else -> MantraDetail("Mangala (Mars)", "ॐ भौमाय नमः", "Om Bhaumaya Namah")
    }
}
