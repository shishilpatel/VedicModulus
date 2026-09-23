package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.R
import com.google.android.gms.wearable.Wearable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.*
import com.example.data.CityRepository
import com.example.data.VedicWearableSyncManager
import com.example.billing.PlayBillingManager
import com.example.ui.components.PlayBillingPaywallDialog
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import com.example.notifications.PanchangaNotificationManager
import android.widget.Toast
import com.example.engines.personalization.VedicPersonalizationEngine
import com.example.ui.screens.PastReadingsScreen
import com.example.ui.screens.UserProfileSettingsScreen
import com.example.ui.screens.report.KundliReportHubScreen
import com.example.ui.screens.report.PremiumKundliReportViewerScreen
import com.example.ui.screens.tabs.*
import com.example.ui.screens.tabs.DailyHighlightsTab
import com.example.ui.theme.*
import com.example.ui.viewmodel.VastuCompassViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

enum class DashboardTab(val title: String) {
    DAILY_HIGHLIGHTS("Daily Highlights"),
    PANCHANGA("Panchanga"),
    MUHURTAS("Muhurtas"),
    CHOGHADIYA_HORA("Choghadiya & Hora"),
    VEDIC_CALENDAR("Vedic Calendar"),
    PREMIUM_KUNDLI_REPORT("Premium Kundli Report"),
    KUNDALI_CHART("Kundali Chart"),
    LIFE_PREDICTIONS("Life Predictions"),
    COMPATIBILITY_CHECKER("Compatibility Checker"),
    NUMEROLOGY("Numerology"),
    PAST_READINGS("Past Readings"),
    VEDIC_AI_ASSISTANT("Vedic AI Assistant"),
    VASTU_COMPASS("Vastu Compass"),
    VASTU_SHASTRA("Vastu Shastra"),
    VEDIC_DINCHARYA("Vedic Dincharya"),
    BRAHMACHARYA("Brahmacharya"),
    SANATAN_SANSKRITI("Sanatan Sanskriti"),
    CHAKRA_MEDITATION("Chakra & Sadhana"),
    WATCH_FACE("Watch Face"),
    USER_PROFILE_SETTINGS("Profile Settings"),
    PROFILES_AND_TRAVEL("Profiles & Travel")
}

enum class NavCategory(val title: String, val icon: ImageVector, val tabs: List<DashboardTab>) {
    DASHBOARD(
        "Dashboard",
        Icons.Default.Home,
        listOf(DashboardTab.DAILY_HIGHLIGHTS, DashboardTab.PANCHANGA, DashboardTab.MUHURTAS, DashboardTab.CHOGHADIYA_HORA, DashboardTab.VEDIC_CALENDAR)
    ),
    JYOTISHA(
        "Jyotish",
        Icons.Default.AutoAwesome,
        listOf(DashboardTab.PREMIUM_KUNDLI_REPORT, DashboardTab.KUNDALI_CHART, DashboardTab.LIFE_PREDICTIONS, DashboardTab.COMPATIBILITY_CHECKER, DashboardTab.NUMEROLOGY, DashboardTab.PAST_READINGS, DashboardTab.VEDIC_AI_ASSISTANT)
    ),
    VASTU(
        "Vastu",
        Icons.Default.Explore,
        listOf(DashboardTab.VASTU_COMPASS, DashboardTab.VASTU_SHASTRA)
    ),
    DHYANA(
        "Sadhana",
        Icons.Default.SelfImprovement,
        listOf(DashboardTab.VEDIC_DINCHARYA, DashboardTab.BRAHMACHARYA, DashboardTab.SANATAN_SANSKRITI, DashboardTab.CHAKRA_MEDITATION)
    ),
    WATCH_FACES(
        "Watch Face",
        Icons.Default.Watch,
        listOf(DashboardTab.WATCH_FACE)
    ),
    SETTINGS(
        "Settings",
        Icons.Default.Settings,
        listOf(DashboardTab.USER_PROFILE_SETTINGS, DashboardTab.PROFILES_AND_TRAVEL)
    )
}

@Composable
fun DashboardTab.getLocalizedTitle(): String {
    return when (this) {
        DashboardTab.DAILY_HIGHLIGHTS -> stringResource(R.string.tab_daily_highlights)
        DashboardTab.PANCHANGA -> stringResource(R.string.tab_panchanga)
        DashboardTab.MUHURTAS -> stringResource(R.string.tab_muhurtas)
        DashboardTab.CHOGHADIYA_HORA -> stringResource(R.string.tab_choghadiya_hora)
        DashboardTab.VEDIC_CALENDAR -> stringResource(R.string.tab_vedic_calendar)
        DashboardTab.PREMIUM_KUNDLI_REPORT -> stringResource(R.string.tab_premium_kundli_report)
        DashboardTab.KUNDALI_CHART -> stringResource(R.string.tab_kundali_chart)
        DashboardTab.LIFE_PREDICTIONS -> stringResource(R.string.tab_life_predictions)
        DashboardTab.COMPATIBILITY_CHECKER -> stringResource(R.string.tab_compatibility_match)
        DashboardTab.NUMEROLOGY -> stringResource(R.string.tab_numerology)
        DashboardTab.PAST_READINGS -> stringResource(R.string.tab_past_readings)
        DashboardTab.VEDIC_AI_ASSISTANT -> stringResource(R.string.tab_vedic_ai_assistant)
        DashboardTab.VASTU_COMPASS -> stringResource(R.string.tab_vastu_compass)
        DashboardTab.VASTU_SHASTRA -> stringResource(R.string.tab_vastu_shastra)
        DashboardTab.VEDIC_DINCHARYA -> stringResource(R.string.tab_vedic_dincharya)
        DashboardTab.BRAHMACHARYA -> stringResource(R.string.tab_brahmacharya)
        DashboardTab.SANATAN_SANSKRITI -> stringResource(R.string.tab_sanatan_sanskriti)
        DashboardTab.CHAKRA_MEDITATION -> stringResource(R.string.tab_chakra_meditation)
        DashboardTab.WATCH_FACE -> stringResource(R.string.tab_watch_face)
        DashboardTab.USER_PROFILE_SETTINGS -> stringResource(R.string.tab_profile_settings)
        DashboardTab.PROFILES_AND_TRAVEL -> stringResource(R.string.tab_profiles_travel)
    }
}

@Composable
fun NavCategory.getLocalizedTitle(): String {
    return when (this) {
        NavCategory.DASHBOARD -> stringResource(R.string.cat_dashboard)
        NavCategory.JYOTISHA -> stringResource(R.string.cat_jyotisha)
        NavCategory.VASTU -> stringResource(R.string.cat_vastu)
        NavCategory.DHYANA -> stringResource(R.string.cat_dhyana)
        NavCategory.WATCH_FACES -> stringResource(R.string.tab_watch_face)
        NavCategory.SETTINGS -> stringResource(R.string.cat_settings)
    }
}

fun NavCategory.getTabs(hasSmartWatch: Boolean): List<DashboardTab> {
    return this.tabs
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    profile: BirthProfile,
    userAccount: UserAccount? = null,
    onEditProfile: () -> Unit,
    onProfileAdd: (BirthProfile) -> Unit = {},
    onStyleChanged: (WatchFaceStyle) -> Unit,
    onThemeChanged: (AppThemePalette) -> Unit = {},
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onSignOut: (() -> Unit)? = null,
    initialNavTab: String? = null,
    modifier: Modifier = Modifier
) {
    val tabs = DashboardTab.values()
    val initialTab = when (initialNavTab) {
        "PANCHANGA" -> DashboardTab.PANCHANGA
        "MUHURTAS" -> DashboardTab.MUHURTAS
        "DAILY_HIGHLIGHTS" -> DashboardTab.DAILY_HIGHLIGHTS
        "VASTU" -> DashboardTab.VASTU_COMPASS
        else -> NavCategory.DASHBOARD.tabs.first()
    }
    val initialCategory = when (initialTab) {
        DashboardTab.PANCHANGA, DashboardTab.MUHURTAS, DashboardTab.DAILY_HIGHLIGHTS, DashboardTab.CHOGHADIYA_HORA, DashboardTab.VEDIC_CALENDAR -> NavCategory.DASHBOARD
        DashboardTab.VASTU_COMPASS, DashboardTab.VASTU_SHASTRA -> NavCategory.VASTU
        DashboardTab.VEDIC_DINCHARYA, DashboardTab.BRAHMACHARYA, DashboardTab.SANATAN_SANSKRITI, DashboardTab.CHAKRA_MEDITATION -> NavCategory.DHYANA
        DashboardTab.PREMIUM_KUNDLI_REPORT, DashboardTab.KUNDALI_CHART, DashboardTab.LIFE_PREDICTIONS, DashboardTab.COMPATIBILITY_CHECKER, DashboardTab.NUMEROLOGY, DashboardTab.PAST_READINGS, DashboardTab.VEDIC_AI_ASSISTANT -> NavCategory.JYOTISHA
        DashboardTab.WATCH_FACE -> NavCategory.WATCH_FACES
        DashboardTab.USER_PROFILE_SETTINGS, DashboardTab.PROFILES_AND_TRAVEL -> NavCategory.SETTINGS
    }
    var selectedCategory by rememberSaveable { mutableStateOf(initialCategory) }
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var refreshTrigger by remember { mutableStateOf(0) }

    var currentDateKey by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            val today = LocalDate.now()
            if (today != currentDateKey) {
                currentDateKey = today
            }
        }
    }

    val personalizationEngine = remember { VedicPersonalizationEngine() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userPreferencesRepository = remember { com.example.data.UserPreferencesRepository(context) }
    val vastuCompassViewModel: VastuCompassViewModel = viewModel()
    val billingManager = remember { PlayBillingManager.getInstance(context) }
    val billingStatus by billingManager.billingStatus.collectAsState()
    var showPaywallDialog by remember { mutableStateOf(false) }
    var showConsultationThreadDialog by remember { mutableStateOf(false) }
    var openedReportId by remember { mutableStateOf<String?>(null) }

    var activeProfile by remember(profile) { mutableStateOf(profile) }
    var activeStyle by remember(profile.preferredStyle) { mutableStateOf(profile.preferredStyle) }
    var isAmbientMode by remember { mutableStateOf(false) }
    val isWatchSynced by remember { mutableStateOf(true) }
    var selectedDateOffset by remember { mutableStateOf(0) }
    var isTravelModeEnabled by remember { mutableStateOf(false) }
    var travelLocation by remember {
        mutableStateOf(
            CityRepository.CITIES.firstOrNull { it.cityName.contains("New York") || it.cityName.contains("London") }
                ?: CityRepository.getDefaultCity()
        )
    }
    var userSyncedLocation by remember { mutableStateOf<LocationData?>(null) }

    var personalizedResult by remember { mutableStateOf<PersonalizedProfile?>(null) }
    LaunchedEffect(activeProfile) {
        personalizedResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                personalizationEngine.createPersonalizedProfile(activeProfile)
            } catch (e: Exception) {
                android.util.Log.e("DashboardScreen", "Error computing personalized profile", e)
                personalizationEngine.createPersonalizedProfile(activeProfile)
            }
        }
    }
    
    val currentPersonalizedResult = personalizedResult
    if (currentPersonalizedResult == null) {
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator(color = MaroonPrimary)
        }
        return
    }

    val activeLocation = if (isTravelModeEnabled) travelLocation else (userSyncedLocation ?: activeProfile.birthLocation)
    var dailyResult by remember { mutableStateOf<DailyVedicProfile?>(null) }
    LaunchedEffect(activeLocation, activeProfile, currentPersonalizedResult, currentDateKey, selectedDateOffset, refreshTrigger) {
        val zoneId = try {
            java.time.ZoneId.of(activeLocation.timezoneId)
        } catch (e: Exception) {
            java.time.ZoneId.systemDefault()
        }
        val targetZdt = ZonedDateTime.now(zoneId).plusDays(selectedDateOffset.toLong())
        dailyResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                personalizationEngine.createDailyVedicProfile(
                    now = targetZdt,
                    location = activeLocation,
                    ayanamsha = activeProfile.ayanamsha,
                    tradition = activeProfile.calendarTradition,
                    personalizedProfile = currentPersonalizedResult
                )
            } catch (e: Exception) {
                android.util.Log.e("DashboardScreen", "Error calculating daily vedic profile", e)
                null
            }
        }
    }

    val currentDailyResult = dailyResult
    if (currentDailyResult == null) {
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator(color = MaroonPrimary)
        }
        return
    }

    val watchConfig = remember(currentPersonalizedResult, activeStyle) {
        personalizationEngine.generateWatchFaceConfig(currentPersonalizedResult, activeStyle)
    }

    var isWearOsConnected by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaroonPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val isPrimaryChart = (activeProfile.fullName == profile.fullName && activeProfile.birthDate == profile.birthDate)
                            Text(
                                text = if (isPrimaryChart) "${profile.birthLocation.cityName} • ${currentDailyResult.currentVedicPeriod}"
                                       else "${activeProfile.fullName} • ${currentDailyResult.currentVedicPeriod}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isPrimaryChart) DarkTextSecondary else OrangeAccent,
                                    fontWeight = if (isPrimaryChart) FontWeight.Normal else FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    // Google Play Ad-Free Status Button / Badge
                    Surface(
                        onClick = { showPaywallDialog = true },
                        color = if (billingStatus.isAdFree) AuspiciousGreen.copy(alpha = 0.15f) else GoldPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (billingStatus.isAdFree) AuspiciousGreen else GoldPrimary
                        ),
                        modifier = Modifier.testTag("topbar_ad_free_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (billingStatus.isAdFree) Icons.Default.Verified else Icons.Default.WorkspacePremium,
                                contentDescription = "Ad-Free",
                                tint = if (billingStatus.isAdFree) AuspiciousGreen else GoldPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (billingStatus.isAdFree) "PRO" else "PRO",
                                color = if (billingStatus.isAdFree) AuspiciousGreen else GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    LanguageSwitcher(currentLanguage, onLanguageChange)

                    // 1-on-1 Consultation Thread Button
                    IconButton(
                        onClick = { showConsultationThreadDialog = true },
                        modifier = Modifier.testTag("topbar_consultation_thread_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = "Scholar Consultations",
                            tint = GoldPrimary
                        )
                    }

                    // Daily Muhurta & Rahu Kaal Alarms Trigger Icon
                    IconButton(
                        onClick = {
                            PanchangaNotificationManager.scheduleDailyMuhurtaAlarms(context)
                            PanchangaNotificationManager.triggerImmediateTestNotification(
                                context,
                                "⭐ Shubh Muhurta Alarms Active",
                                "Daily notifications for Brahma Muhurta, Abhijit, & Rahu Kaal are enabled!"
                            )
                            Toast.makeText(context, "Daily Muhurta & Rahu Kaal alerts activated!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("topbar_muhurta_alarm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Muhurta Notifications",
                            tint = Color(0xFF38BDF8)
                        )
                    }

                    // User Profile / Settings Avatar Button
                    IconButton(
                        onClick = {
                            selectedCategory = NavCategory.SETTINGS
                            selectedTab = DashboardTab.USER_PROFILE_SETTINGS
                        },
                        modifier = Modifier.testTag("action_edit_profile")
                    ) {
                        if (userAccount != null) {
                            Surface(
                                shape = CircleShape,
                                color = MaroonPrimary,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (userAccount.fullName.firstOrNull() ?: 'S').toString().uppercase(),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings & Profile",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 6.dp
            ) {
                val visibleCategories = if (profile.hasSmartWatch) {
                    NavCategory.values().toList()
                } else {
                    NavCategory.values().filter { it != NavCategory.WATCH_FACES }
                }
                visibleCategories.forEach { category ->
                    val isSelected = selectedCategory == category
                    NavigationBarItem(
                        selected = isSelected,
                        alwaysShowLabel = false,
                        onClick = {
                            selectedCategory = category
                            val categoryTabs = category.getTabs(profile.hasSmartWatch)
                            if (!categoryTabs.contains(selectedTab)) {
                                selectedTab = categoryTabs.first()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.getLocalizedTitle(),
                                modifier = Modifier
                                    .size(if (isSelected) 26.dp else 22.dp)
                                    .testTag("nav_icon_${category.name.lowercase()}")
                            )
                        },
                        label = null,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaroonPrimary,
                            unselectedIconColor = DarkTextSecondary,
                            indicatorColor = MaroonPrimary.copy(alpha = 0.18f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentCategoryTabs = selectedCategory.getTabs(profile.hasSmartWatch)
            // Show sub-tab row only if category has multiple tabs
            if (currentCategoryTabs.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = currentCategoryTabs.indexOf(selectedTab).coerceAtLeast(0),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaroonPrimary,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        val index = currentCategoryTabs.indexOf(selectedTab).coerceAtLeast(0)
                        if (index < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                                color = MaroonPrimary
                            )
                        }
                    }
                ) {
                    currentCategoryTabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.getLocalizedTitle(),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaroonPrimary else DarkTextSecondary
                                )
                            }
                        )
                    }
                }
            }

            // Active Tab View
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    DashboardTab.PREMIUM_KUNDLI_REPORT -> {
                        if (openedReportId != null) {
                            PremiumKundliReportViewerScreen(
                                reportId = openedReportId!!,
                                onBack = { openedReportId = null }
                            )
                        } else {
                            KundliReportHubScreen(
                                currentProfile = activeProfile,
                                onOpenReportViewer = { openedReportId = it }
                            )
                        }
                    }
                    DashboardTab.DAILY_HIGHLIGHTS -> DailyHighlightsTab(
                        dailyProfile = currentDailyResult,
                        personalizedProfile = currentPersonalizedResult,
                        currentLocation = activeLocation,
                        onLocationSynced = { syncedLoc ->
                            userSyncedLocation = syncedLoc
                        },
                        onRefresh = { refreshTrigger++ },
                        onNavigateToVastu = {
                            selectedCategory = NavCategory.VASTU
                            selectedTab = DashboardTab.VASTU_COMPASS
                        },
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.VEDIC_CALENDAR -> VedicCalendarTab(
                        profile = activeProfile,
                        personalizedProfile = currentPersonalizedResult,
                        personalizationEngine = personalizationEngine
                    )
                    DashboardTab.LIFE_PREDICTIONS -> LifePredictionsTab(
                        profile = activeProfile,
                        personalizedProfile = currentPersonalizedResult,
                        dailyProfile = currentDailyResult,
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.VEDIC_AI_ASSISTANT -> VedicAITab(
                        profile = activeProfile,
                        personalizedProfile = currentPersonalizedResult,
                        dailyProfile = currentDailyResult,
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.PROFILES_AND_TRAVEL -> ProfilesAndTravelTab(
                        primaryProfile = profile,
                        activeProfile = activeProfile,
                        onSwitchActiveProfile = { switchedProfile ->
                            activeProfile = switchedProfile
                        },
                        isTravelModeEnabled = isTravelModeEnabled,
                        onTravelModeChange = { isTravelModeEnabled = it },
                        travelLocation = travelLocation,
                        onTravelLocationChange = { travelLocation = it },
                        onProfileAdd = onProfileAdd,
                        onThemeChange = onThemeChanged
                    )
                    DashboardTab.WATCH_FACE -> WatchFaceTab(
                        profile = activeProfile,
                        config = watchConfig,
                        personalizedProfile = currentPersonalizedResult,
                        dailyProfile = currentDailyResult,
                        currentTimeProvider = { LocalTime.now() },
                        isAmbientMode = isAmbientMode,
                        onAmbientToggle = { isAmbientMode = !isAmbientMode },
                        onStyleSelect = { style ->
                            activeStyle = style
                            onStyleChanged(style)
                        },
                        onSyncToWatch = {
                            scope.launch {
                                val success = VedicWearableSyncManager.syncProfileAndHoroscopeToWatch(
                                    context = context,
                                    profile = activeProfile,
                                    personalizedProfile = currentPersonalizedResult,
                                    dailyProfile = currentDailyResult,
                                    watchConfig = watchConfig
                                )
                                if (success) {
                                    snackbarHostState.showSnackbar("Vedic profile & horoscope synced to Wear OS smartwatch via DataClient!")
                                } else {
                                    snackbarHostState.showSnackbar("Failed to sync data to watch.")
                                }
                            }
                        },
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.VEDIC_DINCHARYA -> VedicDincharyaTab(
                        profile = activeProfile,
                        dailyProfile = currentDailyResult,
                        personalizedProfile = currentPersonalizedResult,
                        currentLanguage = currentLanguage,
                        onNavigateToChakra = {
                            selectedTab = DashboardTab.CHAKRA_MEDITATION
                        }
                    )
                    DashboardTab.BRAHMACHARYA -> BrahmacharyaTab(
                        profile = activeProfile,
                        dailyProfile = currentDailyResult,
                        personalizedProfile = currentPersonalizedResult,
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.SANATAN_SANSKRITI -> SanatanSanskritiTab(
                        dailyProfile = currentDailyResult,
                        personalizedProfile = currentPersonalizedResult,
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.CHAKRA_MEDITATION -> ChakraMeditationTab(
                        profile = activeProfile,
                        dailyProfile = currentDailyResult,
                        personalizedProfile = currentPersonalizedResult
                    )
                    DashboardTab.KUNDALI_CHART -> KundaliTab(
                        profile = activeProfile,
                        personalizedProfile = currentPersonalizedResult,
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.VASTU_COMPASS -> VastuCompassTab(
                        personalizedProfile = currentPersonalizedResult,
                        showOnlyShastra = false,
                        viewModel = vastuCompassViewModel,
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.VASTU_SHASTRA -> VastuCompassTab(
                        personalizedProfile = currentPersonalizedResult,
                        showOnlyShastra = true,
                        viewModel = vastuCompassViewModel,
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.PANCHANGA -> PanchangaTab(
                        dailyProfile = currentDailyResult,
                        personalizedProfile = currentPersonalizedResult,
                        selectedDateOffset = selectedDateOffset,
                        onDateOffsetChange = { selectedDateOffset = it },
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.CHOGHADIYA_HORA -> ChoghadiyaHoraTab(
                        dailyProfile = currentDailyResult
                    )
                    DashboardTab.MUHURTAS -> MuhurtasTab(
                        dailyProfile = currentDailyResult
                    )
                    DashboardTab.NUMEROLOGY -> NumerologyTab(
                        personalizedProfile = currentPersonalizedResult,
                        currentLanguage = currentLanguage
                    )
                    DashboardTab.PAST_READINGS -> PastReadingsScreen(
                        onBack = { selectedTab = DashboardTab.LIFE_PREDICTIONS }
                    )
                    DashboardTab.USER_PROFILE_SETTINGS -> UserProfileSettingsScreen(
                        currentProfile = profile,
                        onBack = {
                            selectedCategory = NavCategory.DASHBOARD
                            selectedTab = DashboardTab.DAILY_HIGHLIGHTS
                        },
                        onProfileSaved = { updatedProfile ->
                            onProfileAdd(updatedProfile)
                            activeProfile = updatedProfile
                        },
                        onSignOut = onSignOut
                    )
                    DashboardTab.COMPATIBILITY_CHECKER -> com.example.ui.screens.tabs.CompatibilityMatchTab(
                        profile = activeProfile,
                        personalizationEngine = personalizationEngine,
                        currentLanguage = currentLanguage
                    )
                }
            }
        }
    }

    if (showPaywallDialog) {
        PlayBillingPaywallDialog(
            onDismiss = { showPaywallDialog = false }
        )
    }

    if (showConsultationThreadDialog) {
        ConsultationThreadDialog(
            query = ConsultationQuery(
                seekerName = profile.fullName,
                seekerEmail = "${profile.fullName.lowercase().replace(" ", ".")}@example.com",
                questionTopic = "Career Transition & Jupiter Mahadasha",
                detailedQuestion = "When is the most auspicious month to accept a new leadership position in 2026?"
            ),
            onDismiss = { showConsultationThreadDialog = false }
        )
    }
}

@Composable
fun LanguageSwitcher(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val languages = com.example.core.language.LanguageManager.getLanguageOptions()

    androidx.compose.foundation.layout.Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Translate, contentDescription = "Change Language", tint = MaterialTheme.colorScheme.onSurface)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            properties = androidx.compose.ui.window.PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            languages.forEach { (code, nativeName, symbol) ->
                DropdownMenuItem(
                    leadingIcon = {
                        Text(
                            text = symbol,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (currentLanguage == code) com.example.ui.theme.MaroonPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    text = {
                        Text(
                            text = nativeName,
                            color = if (currentLanguage == code) com.example.ui.theme.MaroonPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (currentLanguage == code) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        expanded = false
                        onLanguageChange(code)
                    }
                )
            }
        }
    }
}
