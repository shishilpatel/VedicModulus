package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.core.model.*
import com.example.data.room.ClientProfileEntity
import com.example.data.room.VedicDatabase
import com.example.engines.personalization.VedicPersonalizationEngine
import com.example.ui.screens.tabs.AstrologerRatesAndStatusTab
import com.example.ui.screens.tabs.KundaliTab
import com.example.ui.screens.tabs.NumerologyTab
import com.example.ui.screens.tabs.VastuCompassTab
import com.example.ui.theme.*
import com.example.ui.viewmodel.VastuCompassViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstrologerDashboardScreen(
    userAccount: UserAccount,
    activeClientId: Long?,
    onUpdateActiveClientId: (Long?) -> Unit,
    onSignOut: () -> Unit,
    onLanguageChange: (String) -> Unit,
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { VedicDatabase.getDatabase(context) }
    val clientDao = remember { database.clientProfileDao() }

    // 0: Directory, 1: Kundli & Charts, 2: Numerology, 3: Consultation Notes, 4: Vastu & Tools
    var selectedAstrologerTab by remember { mutableIntStateOf(0) }

    val allClients by clientDao.getClientsForAstrologerFlow(userAccount.email)
        .collectAsState(initial = emptyList())

    val activeClient = remember(allClients, activeClientId) {
        allClients.find { it.id == activeClientId } ?: allClients.firstOrNull()
    }

    val activeClientBirthProfile = remember(activeClient) {
        if (activeClient != null) {
            val dateParts = try {
                val parts = activeClient.birthDate.split("-")
                Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            } catch (_: Exception) {
                Triple(1990, 1, 1)
            }
            val timeParts = try {
                val parts = activeClient.birthTime.split(":")
                Pair(parts[0].toInt(), parts[1].toInt())
            } catch (_: Exception) {
                Pair(10, 0)
            }

            BirthProfile(
                fullName = activeClient.fullName,
                birthDate = LocalDate.of(dateParts.first, dateParts.second, dateParts.third),
                birthTime = LocalTime.of(timeParts.first, timeParts.second),
                birthLocation = LocationData(
                    country = activeClient.country,
                    state = activeClient.state,
                    district = activeClient.district,
                    talukaOrTown = activeClient.talukaOrTown,
                    cityName = activeClient.cityName,
                    latitude = activeClient.latitude,
                    longitude = activeClient.longitude,
                    timezoneId = activeClient.timezoneId
                ),
                ayanamsha = AyanamshaSystem.LAHIRI,
                calendarTradition = CalendarTradition.AMANTA,
                numerologySystem = NumerologySystem.CHALDEAN,
                language = LanguageDisplay.ENGLISH,
                preferredStyle = WatchFaceStyle.COSMIC
            )
        } else {
            BirthProfile(
                fullName = "Sample Client",
                birthDate = LocalDate.of(1990, 5, 15),
                birthTime = LocalTime.of(8, 30),
                birthLocation = LocationData(
                    country = "India", state = "Maharashtra", district = "Mumbai",
                    talukaOrTown = "Mumbai", cityName = "Mumbai", latitude = 19.0760,
                    longitude = 72.8777, timezoneId = "Asia/Kolkata"
                )
            )
        }
    }

    // Dynamic personalized profile computation for charts
    val personalizationEngine = remember { VedicPersonalizationEngine() }
    val personalizedProfileState = produceState<PersonalizedProfile?>(initialValue = null, key1 = activeClientBirthProfile) {
        value = withContext(Dispatchers.Default) {
            personalizationEngine.createPersonalizedProfile(activeClientBirthProfile)
        }
    }

    val vastuCompassViewModel: VastuCompassViewModel = viewModel()

    var showAccountMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showClientPickerModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Vedic Jyotish Pro",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFF3CD),
                                    border = BorderStroke(1.dp, Color(0xFFFFC107))
                                ) {
                                    Text(
                                        text = "ASTROLOGER",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF856404),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (userAccount.astrologerTitleOrLicense.isNotBlank()) {
                                    userAccount.astrologerTitleOrLicense
                                } else {
                                    userAccount.fullName
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF6C757D),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showClientPickerModal = true },
                        modifier = Modifier.testTag("btn_quick_switch_client")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = "Switch Client",
                            tint = MaroonPrimary
                        )
                    }

                    Box {
                        IconButton(onClick = { showLanguageMenu = !showLanguageMenu }) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = MaroonPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false },
                            properties = androidx.compose.ui.window.PopupProperties(
                                focusable = true,
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            )
                        ) {
                            com.example.core.language.LanguageManager.getLanguageOptions().forEach { (code, nativeName, symbol) ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Text(
                                            text = symbol,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (currentLanguage == code) MaroonPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = nativeName + if (currentLanguage == code) " ✓" else "",
                                            color = if (currentLanguage == code) MaroonPrimary else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (currentLanguage == code) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        showLanguageMenu = false
                                        onLanguageChange(code)
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showAccountMenu = !showAccountMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Astrologer options",
                                tint = MaroonPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showAccountMenu,
                            onDismissRequest = { showAccountMenu = false },
                            properties = androidx.compose.ui.window.PopupProperties(
                                focusable = true,
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            )
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sign Out") },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaroonPrimary) },
                                onClick = {
                                    showAccountMenu = false
                                    onSignOut()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaroonPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Material 3 Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedAstrologerTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaroonPrimary,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedAstrologerTab]),
                        color = MaroonPrimary
                    )
                },
                divider = { HorizontalDivider(color = Color(0xFFE9ECEF)) }
            ) {
                Tab(
                    selected = selectedAstrologerTab == 0,
                    onClick = { selectedAstrologerTab = 0 },
                    text = {
                        Text(
                            "👥 Directory (${allClients.size})",
                            fontWeight = if (selectedAstrologerTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedAstrologerTab == 0) MaroonPrimary else Color(0xFF6C757D)
                        )
                    },
                    modifier = Modifier.testTag("tab_astrologer_directory")
                )
                Tab(
                    selected = selectedAstrologerTab == 1,
                    onClick = { selectedAstrologerTab = 1 },
                    text = {
                        Text(
                            "🔮 Kundli & Charts",
                            fontWeight = if (selectedAstrologerTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedAstrologerTab == 1) MaroonPrimary else Color(0xFF6C757D)
                        )
                    },
                    modifier = Modifier.testTag("tab_astrologer_kundli")
                )
                Tab(
                    selected = selectedAstrologerTab == 2,
                    onClick = { selectedAstrologerTab = 2 },
                    text = {
                        Text(
                            "🔢 Numerology",
                            fontWeight = if (selectedAstrologerTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedAstrologerTab == 2) MaroonPrimary else Color(0xFF6C757D)
                        )
                    },
                    modifier = Modifier.testTag("tab_astrologer_numerology")
                )
                Tab(
                    selected = selectedAstrologerTab == 3,
                    onClick = { selectedAstrologerTab = 3 },
                    text = {
                        Text(
                            "📝 Notes & Remedies",
                            fontWeight = if (selectedAstrologerTab == 3) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedAstrologerTab == 3) MaroonPrimary else Color(0xFF6C757D)
                        )
                    },
                    modifier = Modifier.testTag("tab_astrologer_notes")
                )
                Tab(
                    selected = selectedAstrologerTab == 4,
                    onClick = { selectedAstrologerTab = 4 },
                    text = {
                        Text(
                            "🧭 Vastu & Tools",
                            fontWeight = if (selectedAstrologerTab == 4) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedAstrologerTab == 4) MaroonPrimary else Color(0xFF6C757D)
                        )
                    },
                    modifier = Modifier.testTag("tab_astrologer_vastu")
                )
                Tab(
                    selected = selectedAstrologerTab == 5,
                    onClick = { selectedAstrologerTab = 5 },
                    text = {
                        Text(
                            "⭐ Rates & Availability",
                            fontWeight = if (selectedAstrologerTab == 5) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedAstrologerTab == 5) MaroonPrimary else Color(0xFF6C757D)
                        )
                    },
                    modifier = Modifier.testTag("tab_astrologer_rates_status")
                )
            }

            // Active Client Ribbon (visible when browsing client-specific tabs: Charts, Numerology, Notes, Vastu)
            if (selectedAstrologerTab in 1..4) {
                if (activeClient != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF9E6),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaroonPrimary,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = activeClient.fullName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = activeClient.fullName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF212529)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaroonPrimary
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${activeClient.birthDate} (${activeClient.birthTime}) • ${activeClient.cityName}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF6C757D),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showClientPickerModal = true },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaroonPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("switch_client_button")
                            ) {
                                Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8F9FA),
                        border = BorderStroke(1.dp, Color(0xFFDEE2E6)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "No active client selected. Select a profile to view chart.",
                                fontSize = 12.sp,
                                color = Color(0xFF6C757D)
                            )
                            Button(
                                onClick = { selectedAstrologerTab = 0 },
                                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Open Directory", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Tab Content Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedAstrologerTab) {
                    0 -> {
                        // Searchable Clients Directory Screen
                        AstrologerClientsScreen(
                            astrologerEmail = userAccount.email,
                            activeClientId = activeClient?.id,
                            onSelectActiveClient = { client ->
                                onUpdateActiveClientId(client.id)
                            },
                            onNavigateToCharts = { client ->
                                onUpdateActiveClientId(client.id)
                                selectedAstrologerTab = 1
                            },
                            onNavigateToNotes = { client ->
                                onUpdateActiveClientId(client.id)
                                selectedAstrologerTab = 3
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    1 -> {
                        // Kundli & Charts Screen directly populated with active client profile
                        if (activeClient == null) {
                            NoActiveClientPlaceholder(onSelectClient = { selectedAstrologerTab = 0 })
                        } else {
                            val currentPersonalized = personalizedProfileState.value
                            if (currentPersonalized == null) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaroonPrimary)
                                }
                            } else {
                                KundaliTab(
                                    profile = activeClientBirthProfile,
                                    personalizedProfile = currentPersonalized,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    2 -> {
                        // Numerology Tab populated with active client profile
                        if (activeClient == null) {
                            NoActiveClientPlaceholder(onSelectClient = { selectedAstrologerTab = 0 })
                        } else {
                            val currentPersonalized = personalizedProfileState.value
                            if (currentPersonalized == null) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaroonPrimary)
                                }
                            } else {
                                NumerologyTab(
                                    personalizedProfile = currentPersonalized,
                                    currentLanguage = currentLanguage,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    3 -> {
                        // Consultation & Remedies Workspace for the active client
                        AstrologerNotesWorkspace(
                            activeClient = activeClient,
                            onSaveNotes = { updatedNotes ->
                                if (activeClient != null) {
                                    scope.launch(Dispatchers.IO) {
                                        clientDao.updateClient(activeClient.copy(notes = updatedNotes))
                                    }
                                }
                            },
                            onSelectClient = { selectedAstrologerTab = 0 }
                        )
                    }
                    4 -> {
                        // Vastu Compass & Shastra tools for the active client
                        if (activeClient == null) {
                            NoActiveClientPlaceholder(onSelectClient = { selectedAstrologerTab = 0 })
                        } else {
                            val currentPersonalized = personalizedProfileState.value
                            if (currentPersonalized == null) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaroonPrimary)
                                }
                            } else {
                                VastuCompassTab(
                                    personalizedProfile = currentPersonalized,
                                    showOnlyShastra = false,
                                    viewModel = vastuCompassViewModel
                                )
                            }
                        }
                    }
                    5 -> {
                        AstrologerRatesAndStatusTab(
                            userAccount = userAccount,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Quick Client Picker Dialog
    if (showClientPickerModal) {
        AlertDialog(
            onDismissRequest = { showClientPickerModal = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.SwitchAccount,
                        contentDescription = null,
                        tint = MaroonPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Switch Active Client",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                }
            },
            text = {
                if (allClients.isEmpty()) {
                    Text("No clients found. Please add a client in the directory.", color = Color(0xFF6C757D))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allClients, key = { it.id }) { client ->
                            val isCurrent = client.id == activeClient?.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) Color(0xFFFFF9E6) else Color.White,
                                border = BorderStroke(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) MaroonPrimary else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onUpdateActiveClientId(client.id)
                                        showClientPickerModal = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isCurrent) MaroonPrimary else MaroonPrimary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = client.fullName.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (isCurrent) Color.White else MaroonPrimary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = client.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF212529)
                                        )
                                        Text(
                                            text = "${client.birthDate} • ${client.cityName}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF6C757D)
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaroonPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClientPickerModal = false
                        selectedAstrologerTab = 0
                    }
                ) {
                    Text("Manage Directory", color = MaroonPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClientPickerModal = false }) {
                    Text("Close", color = Color(0xFF6C757D))
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun NoActiveClientPlaceholder(onSelectClient: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonSearch,
                contentDescription = null,
                tint = Color(0xFFADB5BD),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Client Selected",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212529)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Please select or register a client from the Client Directory to compute authentic charts and calculations.",
                fontSize = 13.sp,
                color = Color(0xFF6C757D),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onSelectClient,
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.People, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Client Directory", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AstrologerNotesWorkspace(
    activeClient: ClientProfileEntity?,
    onSaveNotes: (String) -> Unit,
    onSelectClient: () -> Unit
) {
    if (activeClient == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = Color(0xFFADB5BD),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("No active client selected.", color = Color(0xFF212529), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Select a client from the directory to log consultation notes & remedies.", color = Color(0xFF6C757D), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSelectClient,
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select Client from Directory", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        return
    }

    var notesText by remember(activeClient) { mutableStateOf(activeClient.notes) }
    var isSavedToast by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE9ECEF)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "CONSULTATION LOGS & REMEDIES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Client: ${activeClient.fullName}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212529)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaroonPrimary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "Last: ${activeClient.lastConsultationDate.ifEmpty { "Today" }}",
                            fontSize = 11.sp,
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = {
                        notesText = it
                        isSavedToast = false
                    },
                    label = { Text("Consultation Notes, Mahadasha Observations & Remedies") },
                    placeholder = { Text("e.g. Prescribed Gemstone: Yellow Sapphire for Brihaspati. Advised Rahu Stotram chanting during evening hours...") },
                    minLines = 8,
                    maxLines = 15,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaroonPrimary,
                        focusedLabelColor = MaroonPrimary,
                        unfocusedBorderColor = Color(0xFFCED4DA),
                        focusedTextColor = Color(0xFF212529),
                        unfocusedTextColor = Color(0xFF212529),
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("astrologer_notes_textarea")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSavedToast) {
                        Text(
                            text = "✓ Consultation notes updated",
                            color = Color(0xFF2E7D32),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            onSaveNotes(notesText)
                            isSavedToast = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Consultation Notes", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
