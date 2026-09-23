package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GooglePlacesSearchField
import com.example.core.model.*
import com.example.data.CityRepository
import com.example.engines.personalization.VedicPersonalizationEngine
import com.example.ui.components.GalaxyWatchFaceCanvas
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    initialProfile: BirthProfile? = null,
    onComplete: (BirthProfile) -> Unit,
    currentLanguage: String = "en",
    onLanguageChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }
    var fullName by remember(initialProfile) { mutableStateOf(initialProfile?.fullName ?: "Aarav Sharma") }
    var birthYear by remember(initialProfile) { mutableStateOf(initialProfile?.birthDate?.year?.toString() ?: "1990") }
    var birthMonth by remember(initialProfile) { mutableStateOf(initialProfile?.birthDate?.monthValue?.toString() ?: "8") }
    var birthDay by remember(initialProfile) { mutableStateOf(initialProfile?.birthDate?.dayOfMonth?.toString() ?: "15") }
    var birthHour by remember(initialProfile) { mutableStateOf(initialProfile?.birthTime?.hour?.toString() ?: "7") }
    var birthMinute by remember(initialProfile) { mutableStateOf(initialProfile?.birthTime?.minute?.toString() ?: "42") }
    var selectedCity by remember(initialProfile) { mutableStateOf(initialProfile?.birthLocation ?: CityRepository.getDefaultCity()) }
    var selectedAyanamsha by remember(initialProfile) { mutableStateOf(initialProfile?.ayanamsha ?: AyanamshaSystem.LAHIRI) }
    var selectedTradition by remember(initialProfile) { mutableStateOf(initialProfile?.calendarTradition ?: CalendarTradition.AMANTA) }
    var selectedNumerology by remember(initialProfile) { mutableStateOf(initialProfile?.numerologySystem ?: NumerologySystem.CHALDEAN) }
    var selectedLanguage by remember(initialProfile) { mutableStateOf(initialProfile?.language ?: LanguageDisplay.ENGLISH) }
    var selectedStyle by remember(initialProfile) { mutableStateOf(initialProfile?.preferredStyle ?: WatchFaceStyle.COSMIC) }
    var hasSmartWatch by remember(initialProfile) { mutableStateOf(initialProfile?.hasSmartWatch ?: false) }
    var watchModel by remember(initialProfile) { mutableStateOf(initialProfile?.watchModel ?: "None") }

    val personalizationEngine = remember { VedicPersonalizationEngine() }
    val currentProfile = remember(
        fullName, birthYear, birthMonth, birthDay, birthHour, birthMinute,
        selectedCity, selectedAyanamsha, selectedTradition, selectedNumerology,
        selectedLanguage, selectedStyle, hasSmartWatch, watchModel
    ) {
        val y = birthYear.toIntOrNull()?.coerceIn(1900, 2030) ?: 1990
        val m = birthMonth.toIntOrNull()?.coerceIn(1, 12) ?: 8
        val d = birthDay.toIntOrNull()?.coerceIn(1, 31) ?: 15
        val hh = birthHour.toIntOrNull()?.coerceIn(0, 23) ?: 7
        val mm = birthMinute.toIntOrNull()?.coerceIn(0, 59) ?: 42
        BirthProfile(
            fullName = fullName.ifBlank { "Seeker" },
            birthDate = LocalDate.of(y, m, d),
            birthTime = LocalTime.of(hh, mm),
            birthLocation = selectedCity,
            ayanamsha = selectedAyanamsha,
            calendarTradition = selectedTradition,
            numerologySystem = selectedNumerology,
            language = selectedLanguage,
            preferredStyle = selectedStyle,
            hasSmartWatch = hasSmartWatch,
            watchModel = watchModel
        )
    }

    var personalizedResult by remember { mutableStateOf<PersonalizedProfile?>(null) }
    var dailyResult by remember { mutableStateOf<DailyVedicProfile?>(null) }
    var watchConfig by remember { mutableStateOf<WatchFaceConfiguration?>(null) }

    LaunchedEffect(currentProfile) {
        kotlinx.coroutines.delay(500L)
        val pResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            personalizationEngine.createPersonalizedProfile(currentProfile)
        }
        personalizedResult = pResult
        
        val zoneId = try {
            java.time.ZoneId.of(selectedCity.timezoneId)
        } catch (e: Exception) {
            java.time.ZoneId.systemDefault()
        }
        
        val dResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            personalizationEngine.createDailyVedicProfile(
                now = ZonedDateTime.now(zoneId),
                location = selectedCity,
                ayanamsha = selectedAyanamsha,
                tradition = selectedTradition,
                personalizedProfile = pResult
            )
        }
        dailyResult = dResult
        
        watchConfig = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            personalizationEngine.generateWatchFaceConfig(pResult, selectedStyle)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.titleMedium, color = MaroonPrimary) },
                navigationIcon = {
                    if (step > 1) {
                        IconButton(onClick = { step-- }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = { LanguageSwitcher(currentLanguage, onLanguageChange) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_step_fmt, step),
                        style = MaterialTheme.typography.bodyMedium.copy(color = GrayText)
                    )
                    Button(
                        onClick = {
                            if (step == 6 && !hasSmartWatch) {
                                onComplete(currentProfile)
                            } else if (step < 8) {
                                step++
                            } else {
                                onComplete(currentProfile)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = OnMaroon),
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text(
                            text = if (step == 8) "Apply to Watch" else "Continue",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (step == 8) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { step / 8f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaroonPrimary,
                trackColor = LightBorder
            )
            Spacer(modifier = Modifier.height(24.dp))
            when (step) {
                1 -> Step1Welcome()
                2 -> Step2Name(
                    name = fullName,
                    onNameChange = { fullName = it }
                )
                3 -> Column {
                    Step2BirthDate(
                        year = birthYear, onYearChange = { birthYear = it },
                        month = birthMonth, onMonthChange = { birthMonth = it },
                        day = birthDay, onDayChange = { birthDay = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Step3BirthTime(
                        hour = birthHour, onHourChange = { birthHour = it },
                        minute = birthMinute, onMinuteChange = { birthMinute = it }
                    )
                }
                4 -> Step4BirthLocation(
                    selectedCity = selectedCity,
                    onCitySelected = { selectedCity = it }
                )
                5 -> Step5Preferences(
                    ayanamsha = selectedAyanamsha, onAyanamshaChange = { selectedAyanamsha = it },
                    tradition = selectedTradition, onTraditionChange = { selectedTradition = it },
                    numerology = selectedNumerology, onNumerologyChange = { selectedNumerology = it },
                    language = selectedLanguage, onLanguageChange = { selectedLanguage = it }
                )
                6 -> Step6SmartWatchSelection(
                    hasSmartWatch = hasSmartWatch,
                    onHasSmartWatchChange = { hasSmartWatch = it },
                    selectedModel = watchModel,
                    onModelChange = { watchModel = it }
                )
                7 -> Step7StyleSelection(
                    selectedStyle = selectedStyle,
                    onStyleChange = { selectedStyle = it }
                )
                8 -> {
                    if (watchConfig != null && personalizedResult != null && dailyResult != null) {
                        Step8Preview(
                            config = watchConfig!!,
                            personalizedProfile = personalizedResult!!,
                            dailyProfile = dailyResult!!
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaroonPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1Welcome() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .border(2.dp, GoldPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.app_logo),
                contentDescription = "Vedic Cosmic Logo",
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaroonPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Personalized Astrology • Numerology • Vastu • Cosmic Time",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = OrangeAccent,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                BulletPoint("Pure Offline Astronomical Algorithms (Jean Meeus standard)")
                Spacer(modifier = Modifier.height(10.dp))
                BulletPoint("Real-time Choghadiya, Hora & Rahu Kalam watch face subdials")
                Spacer(modifier = Modifier.height(10.dp))
                BulletPoint("Bespoke Sacred Geometry (Sri Yantra, Chakras, Vastu Compass)")
                Spacer(modifier = Modifier.height(10.dp))
                BulletPoint("True AMOLED Black battery-saving architecture")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Disclaimer",
                    tint = MaroonPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Important: This app provides astrology, numerology, Vastu, meditation, yoga and spiritual content for informational and personal guidance purposes only. Results and interpretations are not guaranteed and should not be considered professional advice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkText
                )
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("✦", color = MaroonPrimary, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = DarkText, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Step2Name(
    name: String,
    onNameChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.onboarding_name_title),
            style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            text = stringResource(R.string.onboarding_name_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.full_legal_name)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaroonPrimary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_full_name"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaroonPrimary,
                unfocusedBorderColor = LightBorder,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2BirthDate(
    year: String, onYearChange: (String) -> Unit,
    month: String, onMonthChange: (String) -> Unit,
    day: String, onDayChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.onboarding_birth_date_title),
            style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            text = stringResource(R.string.onboarding_birth_date_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        var showDatePicker by remember { mutableStateOf(false) }
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = try { 
                java.time.LocalDate.of(year.toInt(), month.toInt(), day.toInt())
                    .atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch(e:Exception) { null }
        )
        
        OutlinedCard(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.birth_date), style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${day.padStart(2, '0')} / ${month.padStart(2, '0')} / $year",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
        
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                            onYearChange(date.year.toString())
                            onMonthChange(date.monthValue.toString())
                            onDayChange(date.dayOfMonth.toString())
                        }
                        showDatePicker = false
                    }) { Text(stringResource(android.R.string.ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text(stringResource(android.R.string.cancel)) }
                }
            ) {
                DatePicker(state = dateState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step3BirthTime(
    hour: String, onHourChange: (String) -> Unit,
    minute: String, onMinuteChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.onboarding_birth_time_title),
            style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            text = stringResource(R.string.onboarding_birth_time_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        var showTimePicker by remember { mutableStateOf(false) }
        val timeState = rememberTimePickerState(
            initialHour = hour.toIntOrNull() ?: 12,
            initialMinute = minute.toIntOrNull() ?: 0,
            is24Hour = false
        )
        
        OutlinedCard(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.birth_time), style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${hour.padStart(2, '0')}:${minute.padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
        
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        onHourChange(timeState.hour.toString())
                        onMinuteChange(timeState.minute.toString())
                        showTimePicker = false
                    }) { Text(stringResource(android.R.string.ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text(stringResource(android.R.string.cancel)) }
                },
                text = {
                    TimePicker(state = timeState)
                }
            )
        }
    }
}

@Composable
private fun Step4BirthLocation(
    selectedCity: LocationData,
    onCitySelected: (LocationData) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.onboarding_location_title),
            style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            text = stringResource(R.string.onboarding_location_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        GooglePlacesSearchField(
            selectedLocation = selectedCity,
            onLocationSelected = onCitySelected
        )
    }
}

@Composable
private fun Step5Preferences(
    ayanamsha: AyanamshaSystem, onAyanamshaChange: (AyanamshaSystem) -> Unit,
    tradition: CalendarTradition, onTraditionChange: (CalendarTradition) -> Unit,
    numerology: NumerologySystem, onNumerologyChange: (NumerologySystem) -> Unit,
    language: LanguageDisplay, onLanguageChange: (LanguageDisplay) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.onboarding_tradition_title),
            style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            text = stringResource(R.string.onboarding_tradition_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.ayanamsha_system), color = OrangeAccent, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        AyanamshaSystem.values().forEach { item ->
            RadioOption(title = item.displayName, selected = item == ayanamsha) { onAyanamshaChange(item) }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(stringResource(R.string.lunar_month_tradition), color = OrangeAccent, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        CalendarTradition.values().forEach { item ->
            RadioOption(title = item.displayName, selected = item == tradition) { onTraditionChange(item) }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(stringResource(R.string.numerology_standard), color = OrangeAccent, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        NumerologySystem.values().forEach { item ->
            RadioOption(title = item.displayName, selected = item == numerology) { onNumerologyChange(item) }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(stringResource(R.string.display_script), color = OrangeAccent, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        LanguageDisplay.values().forEach { item ->
            RadioOption(title = item.displayName, selected = item == language) { onLanguageChange(item) }
        }
    }
}

@Composable
private fun RadioOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaroonPrimary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, color = if (selected) MaterialTheme.colorScheme.onSurface else DarkTextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun Step6SmartWatchSelection(
    hasSmartWatch: Boolean,
    onHasSmartWatchChange: (Boolean) -> Unit,
    selectedModel: String,
    onModelChange: (String) -> Unit
) {
    val supportedModels = com.example.data.SmartWatchRepository.SUPPORTED_WATCH_MODELS

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.onboarding_smartwatch_title),
            style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            text = stringResource(R.string.onboarding_smartwatch_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onHasSmartWatchChange(true) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasSmartWatch) MaroonPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (hasSmartWatch) OnMaroon else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.yes_i_do), fontWeight = FontWeight.Bold, color = if (hasSmartWatch) Color.White else MaterialTheme.colorScheme.onSurface)
            }
            Button(
                onClick = {
                    onHasSmartWatchChange(false)
                    onModelChange("None")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!hasSmartWatch) MaroonPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (!hasSmartWatch) OnMaroon else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.no_phone_only), fontWeight = FontWeight.Bold, color = if (!hasSmartWatch) Color.White else MaterialTheme.colorScheme.onSurface)
            }
        }

        if (hasSmartWatch) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.onboarding_wear_os_models),
                style = MaterialTheme.typography.titleMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = stringResource(R.string.onboarding_wear_os_desc),
                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
            )
            Spacer(modifier = Modifier.height(12.dp))

            supportedModels.forEach { model ->
                val isSelected = selectedModel == model
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) MaroonPrimary else LightBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onModelChange(model) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onModelChange(model) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaroonPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = model,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isSelected) MaroonPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step7StyleSelection(
    selectedStyle: WatchFaceStyle,
    onStyleChange: (WatchFaceStyle) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.onboarding_watch_face_title),
            style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            text = stringResource(R.string.onboarding_watch_face_desc),
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        WatchFaceStyle.values().forEach { style ->
            val isSelected = style == selectedStyle
            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaroonPrimary else LightBorder
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { onStyleChange(style) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onStyleChange(style) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaroonPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = style.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isSelected) MaroonPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = style.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step8Preview(
    config: WatchFaceConfiguration,
    personalizedProfile: PersonalizedProfile,
    dailyProfile: DailyVedicProfile
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.onboarding_personalized_watch_face),
            style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            text = stringResource(R.string.onboarding_procedural_simulation),
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        GalaxyWatchFaceCanvas(
            config = config,
            personalizedProfile = personalizedProfile,
            dailyProfile = dailyProfile,
            modifier = Modifier.size(240.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = stringResource(R.string.onboarding_resonance_summary),
                    style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.lagna_ascendant_colon), color = DarkTextSecondary)
                    Text(personalizedProfile.natalLagnaRashi.sanskritName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.chandra_rashi_colon), color = DarkTextSecondary)
                    Text("${personalizedProfile.natalChandraRashi.sanskritName} ${personalizedProfile.natalChandraRashi.symbol}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.janma_nakshatra_colon), color = DarkTextSecondary)
                    Text("${personalizedProfile.natalNakshatra.sanskritName} (Pada ${personalizedProfile.natalNakshatraPada})", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.mulank_birth_no_colon), color = DarkTextSecondary)
                    Text("#${personalizedProfile.numerology.birthNumber} (${personalizedProfile.numerology.rulingPlanet.sanskritName})", color = MaroonPrimary, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.vastu_element_colon), color = DarkTextSecondary)
                    Text(personalizedProfile.dominantVastuElement, color = OrangeAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
