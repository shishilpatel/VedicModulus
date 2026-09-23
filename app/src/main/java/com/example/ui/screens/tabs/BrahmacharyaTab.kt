package com.example.ui.screens.tabs

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.example.core.model.brahmacharya.*
import com.example.data.brahmacharya.BrahmacharyaRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrahmacharyaTab(
    profile: BirthProfile?,
    dailyProfile: DailyVedicProfile,
    personalizedProfile: PersonalizedProfile?,
    currentLanguage: String = "en",
    onNavigateToMantra: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { BrahmacharyaRepository.getInstance(context) }

    val audience by repo.audienceCategoryFlow.collectAsState(initial = AudienceCategory.UNMARRIED)
    val activeSankalpa by repo.activeSankalpaFlow.collectAsState(initial = null)
    val sankalpaStartDate by repo.sankalpaStartDateFlow.collectAsState(initial = null)
    val currentStreak by repo.currentStreakFlow.collectAsState(initial = 0)
    val longestStreak by repo.longestStreakFlow.collectAsState(initial = 0)
    val totalPureDays by repo.totalPureDaysFlow.collectAsState(initial = 0)

    val today = remember { LocalDate.now() }
    val completedHabits by repo.getCompletedHabitsFlow(today).collectAsState(initial = emptySet())

    var searchQuery by remember { mutableStateOf("") }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showSankalpaDialog by remember { mutableStateOf(false) }
    var showJournalDialog by remember { mutableStateOf(false) }
    var showJapaDialog by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<BrahmacharyaExercise?>(null) }
    var expandedPillarId by remember { mutableStateOf<String?>("meaning_purpose") }

    val filteredPillars = remember(searchQuery) {
        if (searchQuery.isBlank()) BrahmacharyaContent.PILLARS
        else BrahmacharyaContent.PILLARS.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.sanskritTitle.contains(searchQuery, ignoreCase = true) ||
            it.summary.contains(searchQuery, ignoreCase = true) ||
            it.detailedText.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredExercises = remember(searchQuery) {
        if (searchQuery.isBlank()) BrahmacharyaContent.EXERCISES
        else BrahmacharyaContent.EXERCISES.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.sanskritName.contains(searchQuery, ignoreCase = true) ||
            it.type.label.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("brahmacharya_tab_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner & Disclaimer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = SaffronAccent,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.SelfImprovement,
                                        contentDescription = "Brahmacharya",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Brahmacharya Sadhana",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "ब्रह्मचर्य — Mind, Body & Pranic Mastery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        IconButton(onClick = { showDisclaimerDialog = true }) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = "Scriptural Context",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Vedic science of conserving and transmuting vital physical energy (Prana) into luminous spiritual intellect (Ojas & Tejas).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Audience Stage Selector
                    Text(
                        text = "Your Life Stage Guidance:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AudienceCategory.values().forEach { cat ->
                            val isSelected = audience == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    scope.launch { repo.setAudienceCategory(cat) }
                                },
                                label = {
                                    Text(
                                        text = when (cat) {
                                            AudienceCategory.STUDENT -> "Student"
                                            AudienceCategory.UNMARRIED -> "Aspirant"
                                            AudienceCategory.MARRIED -> "Grihastha"
                                        },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // 2. Audience Guidance Focus Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = SaffronAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = audience.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = audience.focusArea,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Quick Action Buttons: SOS Shield, Sankalpa, 108 Japa, Reflection
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Emergency SOS Button
                Button(
                    onClick = { showSosDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("brahmacharya_sos_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = InauspiciousRed),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Urge SOS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Sacred Sankalpa Button
                OutlinedButton(
                    onClick = { showSankalpaDialog = true },
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("brahmacharya_sankalpa_btn"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Stars, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (activeSankalpa != null) "Vow Status" else "Take Vow", fontSize = 13.sp)
                }

                // 108 Japa Counter
                OutlinedButton(
                    onClick = { showJapaDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("brahmacharya_japa_btn"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Spa, contentDescription = null, tint = AuspiciousGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("108 Japa", fontSize = 13.sp)
                }
            }
        }

        // 4. Streak & Vow Progress Dashboard
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sadhana & Vow Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showJournalDialog = true }) {
                            Icon(Icons.Default.EditNote, contentDescription = "Journal Reflection", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StreakStatBox(
                            value = "$currentStreak",
                            unit = "Days",
                            label = "Current Streak",
                            color = OrangeAccent,
                            icon = Icons.Default.LocalFireDepartment
                        )
                        StreakStatBox(
                            value = "$longestStreak",
                            unit = "Days",
                            label = "Longest Record",
                            color = AuspiciousGreen,
                            icon = Icons.Default.EmojiEvents
                        )
                        StreakStatBox(
                            value = "$totalPureDays",
                            unit = "Days",
                            label = "Total Days Pure",
                            color = NeutralBlue,
                            icon = Icons.Default.Verified
                        )
                    }

                    if (activeSankalpa != null && sankalpaStartDate != null) {
                        val elapsedDays = ChronoUnit.DAYS.between(sankalpaStartDate, today).toInt().coerceAtLeast(0) + 1
                        val totalVowDays = activeSankalpa!!.days
                        val progress = (elapsedDays.toFloat() / totalVowDays).coerceIn(0f, 1f)

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = LightBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeSankalpa!!.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = activeSankalpa!!.sanskritName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrayText
                                )
                            }
                            Text(
                                text = "Day $elapsedDays / $totalVowDays",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (elapsedDays >= totalVowDays) AuspiciousGreen else OrangeAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (elapsedDays >= totalVowDays) AuspiciousGreen else SaffronAccent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // 5. Daily Brahmacharya Checklist (6 Core Habits)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Mind & Body Checklist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${completedHabits.size} of ${BrahmacharyaContent.DAILY_HABITS.size} completed today",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (completedHabits.size >= 4) AuspiciousGreen else GrayText
                            )
                        }
                        if (completedHabits.size == BrahmacharyaContent.DAILY_HABITS.size) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AuspiciousGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "★ All Done!",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AuspiciousGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    BrahmacharyaContent.DAILY_HABITS.forEach { habit ->
                        val isChecked = completedHabits.contains(habit.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    scope.launch {
                                        repo.toggleHabit(today, habit.id)
                                    }
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    scope.launch {
                                        repo.toggleHabit(today, habit.id)
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = AuspiciousGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = habit.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isChecked) FontWeight.Normal else FontWeight.SemiBold,
                                    color = if (isChecked) GrayText else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${habit.sanskritName} • ${habit.timing}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrayText
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("brahmacharya_search_bar"),
                placeholder = { Text("Search principles, practices, asanas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                } else null,
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // 7. Core Pillars & Chapters Header
        item {
            Text(
                text = "Core Pillars of Brahmacharya",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // List of Pillars
        items(filteredPillars, key = { it.id }) { pillar ->
            val isExpanded = expandedPillarId == pillar.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pillar_card_${pillar.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (isExpanded) SaffronAccent else LightBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedPillarId = if (isExpanded) null else pillar.id
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pillar.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = pillar.sanskritTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = SaffronAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = { expandedPillarId = if (isExpanded) null else pillar.id }) {
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = pillar.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            HorizontalDivider(color = LightBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = pillar.detailedText,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Scriptural Shloka Box
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = OffWhiteSurface,
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = pillar.scripturalQuote,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaroonPrimary,
                                        fontFamily = FontFamily.Serif
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pillar.quoteSource,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DarkTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Key Daily Practices:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            pillar.keyPractices.forEach { practice ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("• ", color = SaffronAccent, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = practice,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Audience tailored note
                            pillar.audienceNotes[audience]?.let { advice ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "For ${audience.displayName}: $advice",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Practical Yogic Exercises & Techniques
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Practical Yogic Exercises & Breathwork",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(filteredExercises, key = { it.id }) { ex ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedExercise = ex },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${ex.durationMinutes} min",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = ex.type.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = GrayText
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ex.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ex.sanskritName,
                            style = MaterialTheme.typography.bodySmall,
                            color = SaffronAccent
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog 1: Disclaimer & Scriptural Context
    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaroonPrimary)
                    Text("Scriptural & Traditional Guidance", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "This module presents traditional Vedic and Yogic philosophical principles of sense-control (Indriya Nigraha), moral discipline, and meditation.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Spiritual Focus: Content is rooted in the Vedas, Upanishads, Patanjali Yoga Sutras, and Bhagavad Gita.\n• No Medical Guarantees: These teachings represent lifestyle and ethical recommendations for personal wellness and self-mastery. They do not constitute medical, psychological, or psychiatric therapy.\n• Universal Reverence: Adapt practices according to your personal health, stage of life, and conscience.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDisclaimerDialog = false }) {
                    Text("I Understand")
                }
            }
        )
    }

    // Dialog 2: Emergency SOS Urge Surfing
    if (showSosDialog) {
        EmergencySosDialog(
            onDismiss = { showSosDialog = false },
            onIncrementSos = { scope.launch { repo.incrementSosCount() } }
        )
    }

    // Dialog 3: Take Sacred Sankalpa
    if (showSankalpaDialog) {
        SankalpaSelectionDialog(
            currentSankalpa = activeSankalpa,
            onSelectSankalpa = { duration ->
                scope.launch {
                    repo.takeSankalpa(duration)
                    showSankalpaDialog = false
                    Toast.makeText(context, "Sankalpa taken for ${duration.title}! May the Divine grant you unshakeable resolve.", Toast.LENGTH_LONG).show()
                }
            },
            onCancelSankalpa = {
                scope.launch {
                    repo.cancelOrCompleteSankalpa()
                    showSankalpaDialog = false
                    Toast.makeText(context, "Sankalpa concluded.", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showSankalpaDialog = false }
        )
    }

    // Dialog 4: 108 Japa Counter
    if (showJapaDialog) {
        QuickJapaDialog(
            onDismiss = { showJapaDialog = false },
            onComplete = {
                scope.launch {
                    repo.toggleHabit(today, "habit_japa_108")
                    Toast.makeText(context, "108 Japa completed and recorded in daily sadhana!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog 5: Journal Reflection
    if (showJournalDialog) {
        DailyReflectionDialog(
            date = today,
            onSave = { score, note ->
                scope.launch {
                    repo.logJournalEntry(today, score, note)
                    showJournalDialog = false
                    Toast.makeText(context, "Reflection saved!", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showJournalDialog = false }
        )
    }

    // Dialog 6: Exercise Step-by-Step Detail
    selectedExercise?.let { ex ->
        ExerciseDetailDialog(
            exercise = ex,
            onDismiss = { selectedExercise = null }
        )
    }
}

@Composable
private fun StreakStatBox(
    value: String,
    unit: String,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = unit, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 2.dp))
            }
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
        }
    }
}

@Composable
private fun EmergencySosDialog(
    onDismiss: () -> Unit,
    onIncrementSos: () -> Unit
) {
    var timerSeconds by remember { mutableStateOf(120) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            onIncrementSos()
            while (timerSeconds > 0 && isTimerRunning) {
                delay(1000L)
                timerSeconds--
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = InauspiciousRed)
                Text("Emergency Urge-Shield (२ मिनट)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Countdown Timer Box
                Surface(
                    shape = CircleShape,
                    color = InauspiciousRed.copy(alpha = 0.12f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${timerSeconds / 60}:${String.format("%02d", timerSeconds % 60)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = InauspiciousRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Do not panic. You are not the urge. An urge is merely an energetic impulse in the physical sheath (Annamaya Kosha). Follow these 5 steps immediately:",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                val steps = listOf(
                    "1. Physically move away from the bed or secluded screen right now.",
                    "2. Splash cold tap water generously over your face, eyes, and wrists 5 times.",
                    "3. Inhale deeply through folded tongue (Sheetali) and exhale slowly through nose.",
                    "4. Drink 1 glass of cold/room-temperature water in slow sips.",
                    "5. Chanting: 'ॐ नमः शिवाय' or Maha Mrityunjaya Mantra 11 times."
                )

                steps.forEach { step ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AuspiciousGreen)
            ) {
                Text("I Am Grounded & Calm")
            }
        }
    )
}

@Composable
private fun SankalpaSelectionDialog(
    currentSankalpa: SankalpaDuration?,
    onSelectSankalpa: (SankalpaDuration) -> Unit,
    onCancelSankalpa: () -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(currentSankalpa ?: SankalpaDuration.DAYS_21) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Stars, contentDescription = null, tint = GoldPrimary)
                Text("Sacred Sankalpa (व्रत संकल्प)", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "A Sankalpa is a solemn divine commitment made to one's higher Self. Select a duration for your Brahmacharya Vrata:",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                SankalpaDuration.values().forEach { dur ->
                    val isChosen = selected == dur
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isChosen) SaffronAccent else LightBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selected = dur }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${dur.days} Days: ${dur.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dur.sanskritName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isChosen) MaroonPrimary else GrayText
                                )
                            }
                            RadioButton(
                                selected = isChosen,
                                onClick = { selected = dur }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Sanskrit Sankalpa Vow
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OffWhiteSurface,
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "संकल्प मंत्र:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                        Text(
                            text = "ॐ तत्सत्। अद्य अहं मनसा वाचा कर्मणा च आत्म-शुद्धये ब्रह्मचर्य-व्रतं धारयामि।",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSelectSankalpa(selected) }) {
                Text(if (currentSankalpa != null) "Update Vow" else "Take Vow")
            }
        },
        dismissButton = {
            if (currentSankalpa != null) {
                TextButton(onClick = onCancelSankalpa) {
                    Text("Conclude Vow", color = InauspiciousRed)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun QuickJapaDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var count by remember { mutableStateOf(0) }
    val target = 108

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Spa, contentDescription = null, tint = AuspiciousGreen)
                Text("108 Japa Counter", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Chant Gayatri, Maha Mrityunjaya or Om Namah Shivaya",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tap Button Bead
                Surface(
                    shape = CircleShape,
                    color = if (count >= target) AuspiciousGreen else MaroonPrimary,
                    modifier = Modifier
                        .size(130.dp)
                        .clickable {
                            if (count < target) count++
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "/ $target",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (count >= target) "✓ 108 Chants Complete! Sadhana Recorded." else "Tap the bead with each mantra repetition.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (count >= target) FontWeight.Bold else FontWeight.Normal,
                    color = if (count >= target) AuspiciousGreen else DarkTextSecondary
                )
            }
        },
        confirmButton = {
            if (count >= target) {
                Button(onClick = {
                    onComplete()
                    onDismiss()
                }) {
                    Text("Save & Finish")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun DailyReflectionDialog(
    date: LocalDate,
    onSave: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var clarity by remember { mutableStateOf(4) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Evening Reflection (${date})")
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "How clear and serene was your mind today?",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..5).forEach { score ->
                        FilterChip(
                            selected = clarity == score,
                            onClick = { clarity = score },
                            label = { Text("$score ★") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Triggers overcome / Spiritual notes") },
                    placeholder = { Text("E.g., Felt calm during study, avoided phone at night...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(clarity, note) }) {
                Text("Save Reflection")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ExerciseDetailDialog(
    exercise: BrahmacharyaExercise,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(exercise.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(exercise.sanskritName, style = MaterialTheme.typography.bodySmall, color = SaffronAccent)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Category: ${exercise.type.label} • Duration: ${exercise.durationMinutes} minutes",
                    style = MaterialTheme.typography.labelSmall,
                    color = GrayText
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("Step-by-Step Instructions:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                exercise.steps.forEachIndexed { idx, step ->
                    Text(
                        text = "${idx + 1}. $step",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 3.dp),
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Key Benefits:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = AuspiciousGreen)
                Spacer(modifier = Modifier.height(4.dp))
                exercise.benefits.forEach { b ->
                    Text("✓ $b", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                }

                if (exercise.precautions.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Precautions:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = InauspiciousRed)
                    Text(exercise.precautions, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
