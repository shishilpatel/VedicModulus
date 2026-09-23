package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.*
import com.example.data.CityRepository
import com.example.data.UserPreferencesRepository
import com.example.billing.PlayBillingManager
import com.example.billing.UserBillingStatus
import com.example.ui.components.PlayBillingPaywallDialog
import com.example.engines.personalization.VedicPersonalizationEngine
import com.example.ui.components.GooglePlacesSearchField
import com.example.ui.components.VedicDatePickerField
import com.example.ui.components.VedicTimePickerField
import com.example.ui.components.VedicTimePickerModal
import com.example.ui.theme.*
import com.example.workers.DailyVedicNotificationManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileSettingsScreen(
    currentProfile: BirthProfile,
    onBack: (() -> Unit)? = null,
    onProfileSaved: ((BirthProfile) -> Unit)? = null,
    onSignOut: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember { UserPreferencesRepository(context) }
    val personalizationEngine = remember { VedicPersonalizationEngine() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state initialized with current profile
    var fullName by remember(currentProfile) { mutableStateOf(currentProfile.fullName) }
    var birthDate by remember(currentProfile) { mutableStateOf(currentProfile.birthDate) }
    var birthTime by remember(currentProfile) { mutableStateOf(currentProfile.birthTime) }
    var selectedLocation by remember(currentProfile) { mutableStateOf(currentProfile.birthLocation) }
    var selectedAyanamsha by remember(currentProfile) { mutableStateOf(currentProfile.ayanamsha) }
    var selectedTradition by remember(currentProfile) { mutableStateOf(currentProfile.calendarTradition) }
    var selectedNumerology by remember(currentProfile) { mutableStateOf(currentProfile.numerologySystem) }

    var hasSmartWatch by remember(currentProfile) { mutableStateOf(currentProfile.hasSmartWatch) }
    var watchModel by remember(currentProfile) { mutableStateOf(currentProfile.watchModel) }


    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val dailyNotificationsEnabled by preferencesRepository.dailyNotificationsEnabledFlow.collectAsState(initial = true)
    val dailyNotificationTime by preferencesRepository.dailyNotificationTimeFlow.collectAsState(initial = "06:30")
    var showCustomTimePicker by remember { mutableStateOf(false) }
    val notifyFestivals by preferencesRepository.notifyFestivalsFlow.collectAsState(initial = true)
    val notifyMuhurtas by preferencesRepository.notifyMuhurtasFlow.collectAsState(initial = true)
    val notifyRahuKaal by preferencesRepository.notifyRahuKaalFlow.collectAsState(initial = true)
    val wearBackgroundSyncEnabled by preferencesRepository.wearBackgroundSyncEnabledFlow.collectAsState(initial = true)
    var isSendingTestNotification by remember { mutableStateOf(false) }
    var isManualSyncing by remember { mutableStateOf(false) }

    val billingManager = remember { PlayBillingManager.getInstance(context) }
    val billingStatus by billingManager.billingStatus.collectAsState()
    var showPaywallDialog by remember { mutableStateOf(false) }
    var showAstrologerConsultationDialog by remember { mutableStateOf(false) }
    val userAccount by preferencesRepository.userAccountFlow.collectAsState(initial = null)
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteConfirmationText by remember { mutableStateOf("") }
    var isDeletingAccount by remember { mutableStateOf(false) }

    // Derived draft profile for previewing live calculations
    val draftProfile = remember(
        fullName, birthDate, birthTime,
        selectedLocation, selectedAyanamsha, selectedTradition, selectedNumerology,
        hasSmartWatch, watchModel
    ) {
        BirthProfile(
            fullName = fullName.ifBlank { currentProfile.fullName },
            birthDate = birthDate,
            birthTime = birthTime,
            birthLocation = selectedLocation,
            ayanamsha = selectedAyanamsha,
            calendarTradition = selectedTradition,
            numerologySystem = selectedNumerology,
            language = currentProfile.language,
            preferredStyle = currentProfile.preferredStyle,
            appTheme = currentProfile.appTheme,
            hasSmartWatch = hasSmartWatch,
            watchModel = watchModel
        )
    }

    var livePreview by remember { mutableStateOf<PersonalizedProfile?>(null) }
    LaunchedEffect(draftProfile) {
        kotlinx.coroutines.delay(500L)
        livePreview = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            personalizationEngine.createPersonalizedProfile(draftProfile)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Birth Profile & Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Persistent DataStore Configuration",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("action_back_profile_settings")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Success alert banner if saved
            AnimatedVisibility(visible = saveSuccessMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = saveSuccessMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF1B5E20),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // Error alert banner if any
            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
            }

            // Google & Vedic Account Status Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_google_account_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (userAccount != null) Color(0xFFFAF5FF) else Color(0xFFF8FAFC)
                ),
                border = BorderStroke(
                    1.dp,
                    if (userAccount != null) MaroonPrimary.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (userAccount != null) MaroonPrimary else Color(0xFF64748B),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (userAccount?.fullName?.firstOrNull() ?: 'G').toString().uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = userAccount?.fullName ?: "Guest / Offline Mode",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                                Text(
                                    text = userAccount?.email ?: "Not linked to a Google account",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (userAccount != null) AuspiciousGreen.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                1.dp,
                                if (userAccount != null) AuspiciousGreen else Color(0xFFCBD5E1)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (userAccount != null) Icons.Default.Verified else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (userAccount != null) AuspiciousGreen else Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (userAccount != null) "Verified" else "Guest",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userAccount != null) AuspiciousGreen else Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (userAccount != null)
                            "Account is authenticated with Google & Firebase. Your astrological charts and settings sync across devices."
                        else
                            "Sign in with Google to enable cloud backup of your Kundali charts, astrologer consultations, and multi-device sync.",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        lineHeight = 15.sp
                    )
                }
            }

            // Google Play Store Ad-Free Subscription Management Card
            GooglePlaySubscriptionSettingsCard(
                billingStatus = billingStatus,
                onOpenPaywall = { showPaywallDialog = true },
                onManageInPlayStore = { billingManager.openPlayStoreSubscriptionManagement(context) },
                onOpenAstrologerConsultation = { showAstrologerConsultationDialog = true }
            )

            // Live Astronomical Calculation Preview Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIVE ASTROLOGICAL PREVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OrangeAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = draftProfile.fullName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaroonPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = draftProfile.ayanamsha.name,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaroonPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = LightBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Lagna (Ascendant)", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 10.sp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                livePreview?.natalLagnaRashi?.let { rashi ->
                                    com.example.ui.components.icons.ModernZodiacIcon(rashi = rashi, size = 15.dp, isRealistic = true)
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = livePreview?.natalLagnaRashi?.sanskritName ?: "...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Column {
                            Text("Chandra (Moon Sign)", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 10.sp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                livePreview?.natalChandraRashi?.let { rashi ->
                                    com.example.ui.components.icons.ModernZodiacIcon(rashi = rashi, size = 15.dp, isRealistic = true)
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = livePreview?.natalChandraRashi?.sanskritName ?: "...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Nakshatra", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 10.sp))
                            Text(
                                text = "${livePreview?.natalNakshatra?.sanskritName ?: "..."} P${livePreview?.natalNakshatraPada ?: 1}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = OrangeAccent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Numerology: Mulank ${livePreview?.numerology?.birthNumber ?: 1} • Bhagyank ${livePreview?.numerology?.lifePathNumber ?: 1}",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 11.sp)
                        )
                        Text(
                            text = "Lord: ${livePreview?.numerology?.rulingPlanet?.sanskritName ?: "..."}",
                            style = MaterialTheme.typography.bodySmall.copy(color = OrangeAccent, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        )
                    }
                }
            }

            // SECTION 1: Personal Details
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorMessage = null
                            saveSuccessMessage = null
                        },
                        label = { Text("Full Name", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_full_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // SECTION 2: Birth Date & Time (Interactive Material 3 Pickers)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Birth Date & Time",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Text(
                        text = "Tap the Date and Time fields below to open the interactive celestial calendar and time pickers.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 11.sp)
                    )

                    VedicDatePickerField(
                        date = birthDate,
                        onDateChange = {
                            birthDate = it
                            saveSuccessMessage = null
                        },
                        label = "Date of Birth (Tap to select)",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "input_birth_date_picker"
                    )

                    VedicTimePickerField(
                        time = birthTime,
                        onTimeChange = {
                            birthTime = it
                            saveSuccessMessage = null
                        },
                        label = "Time of Birth (Tap to select)",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "input_birth_time_picker"
                    )
                }
            }

            // SECTION 3: Birth Location (Google Places / Exact Coordinates)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Birth Location & Coordinates",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Text(
                        text = "Vedic calculations use your exact latitude, longitude, and historical timezone to derive astronomical horizon and houses accurately.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 11.sp)
                    )

                    GooglePlacesSearchField(
                        selectedLocation = selectedLocation,
                        onLocationSelected = { loc ->
                            selectedLocation = loc
                            saveSuccessMessage = null
                        }
                    )

                    // Details of selected location
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Selected: ${selectedLocation.cityName}, ${selectedLocation.state}, ${selectedLocation.country}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lat: ${String.format(Locale.US, "%.4f", selectedLocation.latitude)}° • Long: ${String.format(Locale.US, "%.4f", selectedLocation.longitude)}° • Timezone: ${selectedLocation.timezoneId}",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }

            // SECTION 4: Astrological & Numerological Systems
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Calculation Settings",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )

                    // Ayanamsha
                    Text("Ayanamsha System", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                    @kotlin.OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AyanamshaSystem.values().forEach { sys ->
                            FilterChip(
                                selected = selectedAyanamsha == sys,
                                onClick = {
                                    selectedAyanamsha = sys
                                    saveSuccessMessage = null
                                },
                                label = { Text(sys.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Calendar tradition
                    Text("Calendar Tradition (Panchanga)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                    @kotlin.OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CalendarTradition.values().forEach { trad ->
                            FilterChip(
                                selected = selectedTradition == trad,
                                onClick = {
                                    selectedTradition = trad
                                    saveSuccessMessage = null
                                },
                                label = { Text(trad.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Numerology system
                    Text("Numerology System", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                    @kotlin.OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        NumerologySystem.values().forEach { numSys ->
                            FilterChip(
                                selected = selectedNumerology == numSys,
                                onClick = {
                                    selectedNumerology = numSys
                                    saveSuccessMessage = null
                                },
                                label = { Text(numSys.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Reset Profile button
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        preferencesRepository.clearBirthProfile()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Profile (Restart Onboarding)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            // Smartwatch Settings
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.onboarding_smartwatch_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Watch, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("I have a Smartwatch / Wear OS Watch", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Supports Pixel Watch, Galaxy Watch, OnePlus, Xiaomi, TicWatch, Fossil & Wear OS", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = hasSmartWatch,
                            onCheckedChange = { hasSmartWatch = it }
                        )
                    }
                    if (hasSmartWatch) {
                        Spacer(modifier = Modifier.height(4.dp))
                        var watchDropdownExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = watchDropdownExpanded,
                            onExpandedChange = { watchDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth().testTag("settings_watch_model_dropdown_box")
                        ) {
                            OutlinedTextField(
                                value = if (watchModel.isBlank() || watchModel == "None") "Select Smartwatch Model" else watchModel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Connected Smartwatch Model", fontSize = 12.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = watchDropdownExpanded) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Watch,
                                        contentDescription = null,
                                        tint = MaroonPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaroonPrimary,
                                    unfocusedBorderColor = LightBorder
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("settings_watch_model_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = watchDropdownExpanded,
                                onDismissRequest = { watchDropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 320.dp)
                            ) {
                                com.example.data.SmartWatchRepository.SUPPORTED_WATCH_MODELS.forEach { modelName ->
                                    val isSelected = watchModel == modelName
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = modelName,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaroonPrimary else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 13.sp
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaroonPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            watchDropdownExpanded = false
                                            watchModel = modelName
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }

                        if (watchModel.isNotBlank() && watchModel != "None" && watchModel != "Select Watch Model" && watchModel != "Select Smartwatch Model") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaroonPrimary.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaroonPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Selected: $watchModel (Universal Wear OS & Companion Sync)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaroonPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = LightBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Background Data Sync Toggle for Battery Saving
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (wearBackgroundSyncEnabled) Icons.Default.Sync else Icons.Default.SyncDisabled,
                                    contentDescription = null,
                                    tint = if (wearBackgroundSyncEnabled) MaroonPrimary else GrayText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Wear OS Background Data Sync",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (!wearBackgroundSyncEnabled) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaroonPrimary.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = if (!wearBackgroundSyncEnabled) "🔋 Battery Saver Active" else "⚡ Auto-Sync",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (!wearBackgroundSyncEnabled) Color(0xFF2E7D32) else MaroonPrimary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (wearBackgroundSyncEnabled)
                                            "Background syncing is active. Updates Hora, Muhurtas, and Panchang in the background."
                                        else
                                            "Background sync is disabled to maximize battery life on your phone & watch. You can still sync manually anytime.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DarkTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Switch(
                                checked = wearBackgroundSyncEnabled,
                                onCheckedChange = { isEnabled ->
                                    coroutineScope.launch {
                                        preferencesRepository.updateWearBackgroundSyncEnabled(isEnabled)
                                        if (!isEnabled) {
                                            androidx.work.WorkManager.getInstance(context).cancelUniqueWork("VedicWearSyncWork")
                                            Toast.makeText(context, "Background watch sync disabled (Battery Saver Active)", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val constraints = androidx.work.Constraints.Builder()
                                                .setRequiresBatteryNotLow(true)
                                                .build()
                                            val syncWorkRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.workers.VedicSyncWorker>(
                                                120, java.util.concurrent.TimeUnit.MINUTES
                                            )
                                                .setConstraints(constraints)
                                                .build()
                                            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                                                "VedicWearSyncWork",
                                                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                                                syncWorkRequest
                                            )
                                            Toast.makeText(context, "Background watch sync enabled", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("toggle_wear_background_sync")
                            )
                        }
                    }
                }
            }
            // Daily Push Notifications & Auspicious Timings Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Daily Vedic Push Notifications",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary)
                                )
                                Text(
                                    text = "Location: ${selectedLocation.cityName}, ${selectedLocation.state}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                        Switch(
                            checked = dailyNotificationsEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    preferencesRepository.updateDailyNotificationsEnabled(enabled)
                                    if (enabled) {
                                        DailyVedicNotificationManager.scheduleDailyNotification(context)
                                    } else {
                                        DailyVedicNotificationManager.cancelDailyNotification(context)
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Delivers a morning summary of today's sacred festivals, Tithi, Nakshatra, Brahma Muhurta, Abhijit Muhurta, and Rahu Kaal computed for your coordinates (${selectedLocation.latitude}°, ${selectedLocation.longitude}°).",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Preferred Morning Alert Time:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("06:00" to "6:00 AM", "06:30" to "6:30 AM", "07:00" to "7:00 AM").forEach { (timeVal, label) ->
                            val isSel = dailyNotificationTime == timeVal
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    coroutineScope.launch {
                                        preferencesRepository.updateDailyNotificationTime(timeVal)
                                        val parts = timeVal.split(":")
                                        DailyVedicNotificationManager.scheduleDailyNotification(context, parts[0].toInt(), parts[1].toInt())
                                    }
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }

                        val isCustom = dailyNotificationTime !in listOf("06:00", "06:30", "07:00")
                        FilterChip(
                            selected = isCustom,
                            onClick = { showCustomTimePicker = true },
                            label = { Text(if (isCustom) dailyNotificationTime else "Custom", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(13.dp))
                            }
                        )
                    }

                    if (showCustomTimePicker) {
                        val parsedTime = try {
                            val p = dailyNotificationTime.split(":")
                            LocalTime.of(p[0].toInt(), p[1].toInt())
                        } catch (_: Exception) {
                            LocalTime.of(6, 30)
                        }
                        VedicTimePickerModal(
                            selectedTime = parsedTime,
                            onTimeSelected = { newTime ->
                                val formatted = String.format(Locale.US, "%02d:%02d", newTime.hour, newTime.minute)
                                coroutineScope.launch {
                                    preferencesRepository.updateDailyNotificationTime(formatted)
                                    DailyVedicNotificationManager.scheduleDailyNotification(context, newTime.hour, newTime.minute)
                                }
                            },
                            onDismiss = { showCustomTimePicker = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Granular toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vedic Festivals & Vrats", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp))
                        Checkbox(
                            checked = notifyFestivals,
                            onCheckedChange = { coroutineScope.launch { preferencesRepository.updateNotifyFestivals(it) } }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Brahma & Abhijit Muhurtas", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp))
                        Checkbox(
                            checked = notifyMuhurtas,
                            onCheckedChange = { coroutineScope.launch { preferencesRepository.updateNotifyMuhurtas(it) } }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rahu Kaal Inauspicious Alert", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp))
                        Checkbox(
                            checked = notifyRahuKaal,
                            onCheckedChange = { coroutineScope.launch { preferencesRepository.updateNotifyRahuKaal(it) } }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Manual 'Sync Now' Background Worker Button
                        Button(
                            onClick = {
                                isManualSyncing = true
                                coroutineScope.launch {
                                    DailyVedicNotificationManager.triggerOneTimeSync(context).collect { workInfo ->
                                        if (workInfo != null) {
                                            when (workInfo.state) {
                                                androidx.work.WorkInfo.State.SUCCEEDED -> {
                                                    isManualSyncing = false
                                                    Toast.makeText(context, "Panchang & cosmic data synced via Background Worker!", Toast.LENGTH_SHORT).show()
                                                    snackbarHostState.showSnackbar("Panchang sync complete for ${selectedLocation.cityName}!")
                                                }
                                                androidx.work.WorkInfo.State.FAILED, androidx.work.WorkInfo.State.CANCELLED -> {
                                                    isManualSyncing = false
                                                    Toast.makeText(context, "Background worker sync failed.", Toast.LENGTH_SHORT).show()
                                                }
                                                else -> {}
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isManualSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_sync_now_panchang")
                        ) {
                            if (isManualSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Syncing...", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Instant Test Notification Button
                        OutlinedButton(
                            onClick = {
                                isSendingTestNotification = true
                                DailyVedicNotificationManager.sendInstantNotification(context) {
                                    isSendingTestNotification = false
                                }
                                Toast.makeText(context, "Daily Vedic Notification sent! Check your notification shade.", Toast.LENGTH_LONG).show()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Vedic notification dispatched for ${selectedLocation.cityName}!")
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaroonPrimary),
                            border = BorderStroke(1.dp, MaroonPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_test_push_notification")
                        ) {
                            Icon(Icons.Default.NotificationAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSendingTestNotification) "Sending..." else "Test Alert", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Inline Save Success Badge if just saved
            AnimatedVisibility(visible = saveSuccessMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AuspiciousGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AuspiciousGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AuspiciousGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Settings & Profile Saved!",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = AuspiciousGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = saveSuccessMessage ?: "Planetary positions, Panchanga calculations, and smartwatch settings have been updated.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        errorMessage = "Please enter your full name."
                        return@Button
                    }

                    errorMessage = null
                    isSaving = true

                    val updatedProfile = currentProfile.copy(
                        fullName = fullName.trim(),
                        birthDate = birthDate,
                        birthTime = birthTime,
                        birthLocation = selectedLocation,
                        ayanamsha = selectedAyanamsha,
                        calendarTradition = selectedTradition,
                        numerologySystem = selectedNumerology,
                        hasSmartWatch = hasSmartWatch,
                        watchModel = watchModel
                    )

                    coroutineScope.launch {
                        try {
                            preferencesRepository.saveBirthProfile(updatedProfile)
                            try {
                                val db = com.example.data.room.VedicDatabase.getDatabase(context)
                                val existingPrimary = db.userProfileDao().getPrimaryProfile()
                                val entity = com.example.data.room.UserProfileEntity.fromBirthProfile(
                                    profile = updatedProfile,
                                    relation = "Self",
                                    isPrimary = true,
                                    id = existingPrimary?.id ?: 0L
                                )
                                db.userProfileDao().insertProfile(entity)
                            } catch (e: Exception) {
                                // Non-fatal Room cache update
                            }

                            onProfileSaved?.invoke(updatedProfile)
                            saveSuccessMessage = "Settings and profile saved successfully! Astrological calculations and smartwatch preferences are updated."
                            Toast.makeText(context, "Settings & Profile saved successfully", Toast.LENGTH_SHORT).show()
                            snackbarHostState.showSnackbar("Settings & Profile saved successfully!")
                        } catch (e: Exception) {
                            errorMessage = "Failed to save settings: ${e.localizedMessage}"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("action_save_profile_settings"),
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving Settings...", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Settings & Update Calculations", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = MaroonPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Important: This app provides astrology, numerology, Vastu, meditation, yoga and spiritual content for informational and personal guidance purposes only. Results and interpretations are not guaranteed and should not be considered professional advice.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = DarkText
                    )
                }
            }
            
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            TextButton(
                onClick = { 
                    try {
                        uriHandler.openUri("https://sites.google.com/view/vedic-cosmic-ghadi/privacy-policy")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
                Text(
                    text = "Privacy Policy & Terms of Service",
                    color = MaroonPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 12.sp,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }

            if (onSignOut != null) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            preferencesRepository.signOutUser()
                            onSignOut()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("action_sign_out_user")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out / Switch Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Delete Account Button (Danger Zone)
                OutlinedButton(
                    onClick = {
                        deleteConfirmationText = ""
                        showDeleteAccountDialog = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFEF2F2),
                        contentColor = Color(0xFFDC2626)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFF87171)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("action_delete_account_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delete Account & All Vedic Data",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showPaywallDialog) {
        PlayBillingPaywallDialog(
            onDismiss = { showPaywallDialog = false }
        )
    }

    if (showAstrologerConsultationDialog) {
        com.example.ui.components.AskAstrologerConsultationDialog(
            birthProfile = currentProfile,
            onDismiss = { showAstrologerConsultationDialog = false }
        )
    }

    // Irreversible Account Deletion Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeletingAccount) {
                    showDeleteAccountDialog = false
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Delete Account Permanently?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "This action is irreversible and permanent.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Deleting your account will immediately and permanently erase:",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(modifier = Modifier.padding(start = 6.dp)) {
                        Text("• All your birth profiles, stored charts & Kundalis", fontSize = 12.sp, color = Color(0xFF334155))
                        Text("• Astrologer consultation history & cached predictions", fontSize = 12.sp, color = Color(0xFF334155))
                        Text("• Connected Google account authentication credentials", fontSize = 12.sp, color = Color(0xFF334155))
                        Text("• Local SQLite databases and app preferences", fontSize = 12.sp, color = Color(0xFF334155))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "To confirm deletion, please type DELETE below:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = deleteConfirmationText,
                        onValueChange = { deleteConfirmationText = it },
                        placeholder = { Text("Type DELETE") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFDC2626),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_confirm_delete_account")
                    )
                }
            },
            confirmButton = {
                val isConfirmed = deleteConfirmationText.trim() == "DELETE"
                Button(
                    onClick = {
                        if (isConfirmed && !isDeletingAccount) {
                            isDeletingAccount = true
                            coroutineScope.launch {
                                try {
                                    val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (e: Exception) { null }
                                    try {
                                        auth?.currentUser?.delete()
                                    } catch (e: Exception) {
                                        android.util.Log.w("DeleteAccount", "Firebase user delete: ${e.message}")
                                    }
                                    auth?.signOut()
                                } catch (e: Exception) {
                                    android.util.Log.e("DeleteAccount", "Auth cleanup error", e)
                                }
                                preferencesRepository.deleteAccountAndWipeData(context)
                                isDeletingAccount = false
                                showDeleteAccountDialog = false
                                onSignOut?.invoke()
                            }
                        }
                    },
                    enabled = isConfirmed && !isDeletingAccount,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        disabledContainerColor = Color(0xFFFCA5A5)
                    ),
                    modifier = Modifier.testTag("button_confirm_delete_account")
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Deleting...", color = Color.White)
                    } else {
                        Text("Permanently Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    enabled = !isDeletingAccount
                ) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun GooglePlaySubscriptionSettingsCard(
    billingStatus: UserBillingStatus,
    onOpenPaywall: () -> Unit,
    onManageInPlayStore: () -> Unit,
    onOpenAstrologerConsultation: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (billingStatus.isAdFree) Color(0xFF064E3B) else Color(0xFF1E293B)
        ),
        border = BorderStroke(
            1.dp,
            if (billingStatus.isAdFree) Color(0xFF10B981) else Color(0x66FFB703)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (billingStatus.isAdFree) Icons.Default.Verified else Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = if (billingStatus.isAdFree) Color(0xFF34D399) else Color(0xFFFFB703),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (billingStatus.isAdFree) "Google Play Ad-Free Plan" else "Vedic Ad-Free & Consultations",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (billingStatus.isAdFree) "Active Subscription • Zero Ads" else "Free Tier • ₹49/mo Subscription or ₹199 Consult",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (billingStatus.isAdFree) Color(0xFFA7F3D0) else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    color = if (billingStatus.isAdFree) Color(0xFF047857) else Color(0xFF334155),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (billingStatus.isAdFree) (billingStatus.planTier?.name ?: "ACTIVE") else "FREE TIER",
                        color = if (billingStatus.isAdFree) Color(0xFFE2E8F0) else Color(0xFFFFB703),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (billingStatus.isAdFree) {
                Text(
                    text = "Order ID: ${billingStatus.orderId ?: "Google Play Verified"} • Auto-renewing / Lifetime",
                    color = Color(0xFFA7F3D0),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onManageInPlayStore,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Manage in Play Store", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onOpenPaywall,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF34D399)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF34D399))
                    ) {
                        Text("Change Plan", fontSize = 12.sp)
                    }
                }
            } else {
                Text(
                    text = "Unlock an entirely ad-free sanctuary across the app, or ask a verified Vedic astrologer a 1-on-1 written query with personalized remedies.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenPaywall,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("settings_upgrade_play_billing_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB703),
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Plans (from ₹49)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (onOpenAstrologerConsultation != null) {
                        OutlinedButton(
                            onClick = onOpenAstrologerConsultation,
                            modifier = Modifier
                                .weight(1.1f)
                                .height(42.dp)
                                .testTag("settings_ask_astrologer_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB703)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB703))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ask Query (₹199)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

