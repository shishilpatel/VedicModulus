package com.example.ui.screens.tabs

import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.foundation.BorderStroke
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
import com.example.core.model.AppThemePalette
import com.example.core.model.BirthProfile
import com.example.core.model.LocationData
import com.example.data.CityRepository
import com.example.data.room.UserProfileEntity
import com.example.data.room.VedicDatabase
import com.example.ui.components.GooglePlacesSearchField
import com.example.ui.components.VedicDatePickerField
import com.example.ui.components.VedicTimePickerField
import com.example.ui.theme.AuspiciousGreen
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesAndTravelTab(
    primaryProfile: BirthProfile,
    activeProfile: BirthProfile = primaryProfile,
    onSwitchActiveProfile: (BirthProfile) -> Unit = {},
    isTravelModeEnabled: Boolean,
    onTravelModeChange: (Boolean) -> Unit,
    travelLocation: LocationData,
    onTravelLocationChange: (LocationData) -> Unit,
    onProfileAdd: (BirthProfile) -> Unit = {},
    onThemeChange: (AppThemePalette) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { VedicDatabase.getDatabase(context) }
    val userProfileDao = remember { database.userProfileDao() }

    // Collect all stored profiles directly from Room Database
    val dbProfiles by userProfileDao.getAllProfilesFlow().collectAsState(initial = emptyList())

    // Ensure the primary registered profile always exists and remains updated in Room DB
    LaunchedEffect(primaryProfile) {
        withContext(Dispatchers.IO) {
            val existingPrimary = userProfileDao.getPrimaryProfile()
            if (existingPrimary == null) {
                val primaryEntity = UserProfileEntity.fromBirthProfile(
                    profile = primaryProfile,
                    relation = "Self",
                    isPrimary = true
                )
                userProfileDao.insertProfile(primaryEntity)
            } else {
                // Keep the primary profile data synchronized with the registered primary profile
                val updatedPrimary = UserProfileEntity.fromBirthProfile(
                    profile = primaryProfile,
                    relation = if (existingPrimary.relation.isNotBlank()) existingPrimary.relation else "Self",
                    isPrimary = true,
                    id = existingPrimary.id
                )
                userProfileDao.updateProfile(updatedPrimary)
            }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<UserProfileEntity?>(null) }

    var newName by remember { mutableStateOf("") }
    var newRelation by remember { mutableStateOf("Family Member") }
    var newBirthDate by remember { mutableStateOf(LocalDate.of(1995, 5, 15)) }
    var newBirthTime by remember { mutableStateOf(LocalTime.of(10, 30)) }
    var newCity by remember { mutableStateOf(CityRepository.getDefaultCity()) }

    // Dialog for Adding a New Profile
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = MaroonPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Family / Friend Profile", color = MaroonPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Pooja Patel") },
                        modifier = Modifier.fillMaxWidth().testTag("add_profile_name_input")
                    )

                    OutlinedTextField(
                        value = newRelation,
                        onValueChange = { newRelation = it },
                        label = { Text("Relation (e.g. Spouse, Child, Parent, Friend)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_profile_relation_input")
                    )

                    // Quick relation suggestions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Spouse", "Child", "Parent", "Friend").forEach { rel ->
                            FilterChip(
                                selected = newRelation == rel,
                                onClick = { newRelation = rel },
                                label = { Text(rel, fontSize = 11.sp) }
                            )
                        }
                    }

                    VedicDatePickerField(
                        date = newBirthDate,
                        onDateChange = { newBirthDate = it },
                        label = "Date of Birth (Tap to select)",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "add_profile_date_picker"
                    )

                    VedicTimePickerField(
                        time = newBirthTime,
                        onTimeChange = { newBirthTime = it },
                        label = "Time of Birth (Tap to select)",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "add_profile_time_picker"
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Birth Place", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary))
                    GooglePlacesSearchField(
                        selectedLocation = newCity,
                        onLocationSelected = { newCity = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val newProfile = primaryProfile.copy(
                                fullName = newName.trim(),
                                birthDate = newBirthDate,
                                birthTime = newBirthTime,
                                birthLocation = newCity
                            )

                            // Save to Room DB with isPrimary = false
                            coroutineScope.launch(Dispatchers.IO) {
                                val entity = UserProfileEntity.fromBirthProfile(
                                    profile = newProfile,
                                    relation = newRelation.ifBlank { "Family Member" },
                                    isPrimary = false,
                                    id = 0
                                )
                                userProfileDao.insertProfile(entity)

                                withContext(Dispatchers.Main) {
                                    // Switch active chart to the newly created profile without modifying primary registered profile
                                    onSwitchActiveProfile(newProfile)
                                    showAddDialog = false
                                    newName = ""
                                    newRelation = "Family Member"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                    modifier = Modifier.testTag("confirm_add_profile_button")
                ) {
                    Text("Save & Switch Chart")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.past_readings_cancel))
                }
            }
        )
    }

    // Confirmation dialog for Deletion
    if (profileToDelete != null) {
        val target = profileToDelete!!
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Profile?") },
            text = {
                Text("Are you sure you want to permanently delete \"${target.fullName}\" (${target.relation})? This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val profileId = target.id
                        val entityBirthProfile = target.toBirthProfile()
                        coroutineScope.launch(Dispatchers.IO) {
                            userProfileDao.deleteProfile(profileId)
                            withContext(Dispatchers.Main) {
                                // If the active profile was the deleted profile, safely revert active chart back to Primary Profile!
                                if (activeProfile.fullName == entityBirthProfile.fullName &&
                                    activeProfile.birthDate == entityBirthProfile.birthDate
                                ) {
                                    onSwitchActiveProfile(primaryProfile)
                                }
                                profileToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Prepare list of profiles from Room DB (fallback to primary profile if DB is loading)
    val displayProfiles = if (dbProfiles.isNotEmpty()) {
        dbProfiles
    } else {
        listOf(
            UserProfileEntity.fromBirthProfile(
                profile = primaryProfile,
                relation = "Self",
                isPrimary = true
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Multi-Profile Circle",
                style = MaterialTheme.typography.titleMedium,
                color = MaroonPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Seamlessly switch calculations, Janam Kundli, and planetary transits across family members. Your original registered profile always remains safe and intact.",
                style = MaterialTheme.typography.bodySmall,
                color = DarkTextSecondary
            )
        }

        // List all family & friends profiles from Room
        items(displayProfiles, key = { if (it.id != 0L) it.id else it.fullName.hashCode().toLong() }) { entity ->
            val entityBirthProfile = entity.toBirthProfile()
            val isPrimary = entity.isPrimary
            val isActive = (activeProfile.fullName == entityBirthProfile.fullName &&
                    activeProfile.birthDate == entityBirthProfile.birthDate &&
                    activeProfile.birthTime == entityBirthProfile.birthTime)

            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_card_${entity.fullName.replace(" ", "_")}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) AuspiciousGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isActive) AuspiciousGreen else Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = if (isPrimary) MaroonPrimary.copy(alpha = 0.15f) else OrangeAccent.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPrimary) Icons.Default.Star else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isPrimary) MaroonPrimary else OrangeAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = entity.fullName,
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isPrimary) MaroonPrimary.copy(alpha = 0.15f) else OrangeAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isPrimary) "Primary (Self)" else entity.relation,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isPrimary) MaroonPrimary else OrangeAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "DOB: ${entityBirthProfile.birthDate} at ${entityBirthProfile.birthTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                            Text(
                                text = "Place: ${entityBirthProfile.birthLocation.talukaOrTown}, ${entityBirthProfile.birthLocation.state}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }

                        // Delete button: Only allow deletion of non-primary profiles!
                        if (!isPrimary) {
                            IconButton(
                                onClick = {
                                    profileToDelete = entity
                                },
                                modifier = Modifier.size(32.dp).testTag("delete_profile_${entity.id}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Profile",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AuspiciousGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AuspiciousGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = AuspiciousGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Active Chart (All Dashas & Transits Computed)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = AuspiciousGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                onSwitchActiveProfile(entityBirthProfile)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("switch_chart_${entity.fullName.replace(" ", "_")}"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Switch Active Chart to ${entity.fullName.split(" ").firstOrNull() ?: entity.fullName}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth().testTag("add_profile_dialog_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Family / Friend Profile", fontSize = 12.sp)
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Travel Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaroonPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Temporarily recalculate Panchanga and timings for a new location without changing your birth data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                }
                Switch(
                    checked = isTravelModeEnabled,
                    onCheckedChange = onTravelModeChange
                )
            }
        }

        if (isTravelModeEnabled) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Travel Location", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary))
                    GooglePlacesSearchField(
                        selectedLocation = travelLocation,
                        onLocationSelected = onTravelLocationChange
                    )
                }
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "App Theme Palette",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaroonPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose between Solar Gold, Lunar Silver, and Traditional Earth color palettes for the app UI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppThemePalette.values().forEach { palette ->
                    val isSelected = primaryProfile.appTheme == palette
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeChange(palette) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) BorderStroke(1.5.dp, MaroonPrimary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onThemeChange(palette) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = palette.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaroonPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = palette.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
