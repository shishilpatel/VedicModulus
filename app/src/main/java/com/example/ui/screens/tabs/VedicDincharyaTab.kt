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
import com.example.core.model.*
import com.example.core.model.dincharya.*
import com.example.data.DincharyaProgressRepository
import com.example.engines.dincharya.VedicDincharyaEngine
import com.example.engines.panchanga.MuhurtaReminderHelper
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

enum class DincharyaFilterPhase(val displayName: String, val phases: Set<DincharyaTimePhase>?) {
    ALL("All Day", null),
    MORNING("Morning", setOf(DincharyaTimePhase.BRAHMA_MUHURTA, DincharyaTimePhase.PRATAH_SANDHYA, DincharyaTimePhase.PURVAHNA)),
    NOON("Midday", setOf(DincharyaTimePhase.MADHYAHNA, DincharyaTimePhase.APARAHNA)),
    EVENING("Evening", setOf(DincharyaTimePhase.SAYAM_SANDHYA, DincharyaTimePhase.PRADOSHA)),
    NIGHT("Bedtime", setOf(DincharyaTimePhase.RATRI_CHARYA))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VedicDincharyaTab(
    profile: BirthProfile?,
    dailyProfile: DailyVedicProfile,
    personalizedProfile: PersonalizedProfile?,
    currentLanguage: String = "en",
    onNavigateToChakra: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dincharyaRepo = remember { DincharyaProgressRepository.getInstance(context) }
    
    val selectedGoal by dincharyaRepo.selectedGoalFlow.collectAsState(initial = DincharyaGoal.SPIRITUAL_GROWTH)
    val todayDate = remember { LocalDate.now() }
    val completedActivities by dincharyaRepo.getCompletedActivitiesFlow(todayDate).collectAsState(initial = emptySet())

    var selectedPhaseFilter by remember { mutableStateOf(DincharyaFilterPhase.ALL) }
    var selectedCategoryFilter by remember { mutableStateOf<DincharyaCategory?>(null) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showNutritionDialog by remember { mutableStateOf(false) }
    var expandedActivityId by remember { mutableStateOf<String?>("act_brahma_muhurta") }

    // Generate dynamic schedule
    val schedule = remember(dailyProfile, personalizedProfile, profile, selectedGoal, completedActivities) {
        VedicDincharyaEngine.generateSchedule(
            dailyProfile = dailyProfile,
            personalizedProfile = personalizedProfile,
            birthProfile = profile,
            selectedGoal = selectedGoal,
            completedIds = completedActivities,
            targetDate = todayDate
        )
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }
    val now = dailyProfile.currentZonedDateTime

    val filteredActivities = remember(schedule.activities, selectedPhaseFilter, selectedCategoryFilter) {
        schedule.activities.filter { activity ->
            val matchesPhase = selectedPhaseFilter.phases == null || selectedPhaseFilter.phases!!.contains(activity.phase)
            val matchesCategory = selectedCategoryFilter == null || activity.category == selectedCategoryFilter
            matchesPhase && matchesCategory
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("vedic_dincharya_tab")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner & Sun Arc Status
        item {
            DincharyaHeaderCard(
                schedule = schedule,
                now = now,
                timeFormatter = timeFormatter,
                onInfoClick = { showDisclaimerDialog = true },
                onNutritionClick = { showNutritionDialog = true }
            )
        }

        // 2. Personalized Goal Selector
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personalized Dincharya Goal",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Text(
                        text = selectedGoal.sanskrit,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DincharyaGoal.values()) { goal ->
                        val isSelected = goal == selectedGoal
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                scope.launch {
                                    dincharyaRepo.setSelectedGoal(goal)
                                }
                            },
                            label = {
                                Text(
                                    text = "${goal.iconEmoji} ${goal.title}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
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
                Text(
                    text = selectedGoal.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = DarkTextSecondary
                    ),
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )
            }
        }

        // 3. Progress Card & Routine Stats
        item {
            DincharyaProgressSummaryCard(
                schedule = schedule,
                onReset = {
                    scope.launch {
                        dincharyaRepo.resetDayProgress(todayDate)
                        Toast.makeText(context, "Today's progress reset", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // 4. Phase Filter Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DincharyaFilterPhase.values().forEach { phaseFilter ->
                    val isSel = selectedPhaseFilter == phaseFilter
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) MaroonPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSel) MaroonPrimary else LightBorder),
                        modifier = Modifier
                            .clickable { selectedPhaseFilter = phaseFilter }
                    ) {
                        Text(
                            text = phaseFilter.displayName,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 5. Section Header with Active Window Indicator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Routine Timeline (${filteredActivities.size} Activities)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AuspiciousGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "⚡ Astronomical Sync",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        color = AuspiciousGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 6. Interactive Activities Timeline
        items(filteredActivities, key = { it.id }) { activity ->
            val isExpanded = expandedActivityId == activity.id
            val isCurrentlyActive = now.isAfter(activity.startTime) && now.isBefore(activity.endTime)

            DincharyaActivityCard(
                activity = activity,
                isExpanded = isExpanded,
                isCurrentlyActive = isCurrentlyActive,
                timeFormatter = timeFormatter,
                onToggleExpand = {
                    expandedActivityId = if (isExpanded) null else activity.id
                },
                onToggleComplete = {
                    scope.launch {
                        dincharyaRepo.toggleActivityCompletion(todayDate, activity.id)
                    }
                },
                onSetAlarm = {
                    MuhurtaReminderHelper.setClockAlarm(
                        context = context,
                        title = "Dincharya: ${activity.title}",
                        startZoned = activity.startTime
                    )
                },
                onAddToCalendar = {
                    MuhurtaReminderHelper.addToSystemCalendar(
                        context = context,
                        title = "Vedic Dincharya: ${activity.title} (${activity.sanskritTitle})",
                        description = "${activity.description}\n\nAyurvedic principle: ${activity.ayurvedicPrinciple}",
                        startZoned = activity.startTime,
                        endZoned = activity.endTime
                    )
                },
                onNavigateToChakra = onNavigateToChakra
            )
        }

        // 7. Disclaimer Footer
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vedic Dincharya is an authentic traditional wellness, astrological, and spiritual routine system. It provides holistic lifestyle recommendations and does not constitute medical diagnosis, treatment, or guaranteed spiritual outcomes.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = DarkTextSecondary,
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            title = {
                Text("About Vedic Dincharya", fontWeight = FontWeight.Bold, color = MaroonPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Dincharya (दिनचर्या) is the classical Vedic & Ayurvedic science of aligning individual circadian physiology with the cosmic movements of the Sun, Moon, and planetary horas.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Calculations are derived dynamically from your coordinates and solar positions.\n• Activities are customized according to your chosen goal.\n• Disclaimers: Traditional lifestyle guidance only; consult qualified Ayurvedic or medical practitioners for clinical conditions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDisclaimerDialog = false }) {
                    Text("Understood", fontWeight = FontWeight.Bold, color = MaroonPrimary)
                }
            }
        )
    }

    if (showNutritionDialog) {
        AyurvedicNutritionDialog(
            meals = schedule.meals,
            onDismiss = { showNutritionDialog = false }
        )
    }
}

@Composable
private fun DincharyaHeaderCard(
    schedule: DincharyaDailySchedule,
    now: ZonedDateTime,
    timeFormatter: DateTimeFormatter,
    onInfoClick: () -> Unit,
    onNutritionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaroonPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🕉", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Vedic Dincharya",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary
                            )
                        )
                        Text(
                            text = "दैनिक वैदिक दिनचर्या • Circadian Alignment",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        )
                    }
                }

                Row {
                    IconButton(onClick = onNutritionClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Ayurvedic Nutrition",
                            tint = AuspiciousGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ephemeris Chips: Sunrise, Solar Noon, Sunset, Day Lord
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌅 Sunrise", fontSize = 10.sp, color = DarkTextSecondary)
                        Text(
                            schedule.sunrise.format(timeFormatter),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("☀️ Zenith (Noon)", fontSize = 10.sp, color = DarkTextSecondary)
                        Text(
                            schedule.solarNoon.format(timeFormatter),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌇 Sunset", fontSize = 10.sp, color = DarkTextSecondary)
                        Text(
                            schedule.sunset.format(timeFormatter),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🪐 Day Ruler", fontSize = 10.sp, color = DarkTextSecondary)
                        Text(
                            "${schedule.rulingVaraGraha.displayName} ${schedule.rulingVaraGraha.symbol}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DincharyaProgressSummaryCard(
    schedule: DincharyaDailySchedule,
    onReset: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(52.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { schedule.completionPercentage },
                        modifier = Modifier.fillMaxSize(),
                        color = AuspiciousGreen,
                        trackColor = Color.LightGray.copy(alpha = 0.4f),
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = "${(schedule.completionPercentage * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuspiciousGreen
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Today's Sadhana Progress",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${schedule.completedCount} of ${schedule.totalCount} practices completed",
                        fontSize = 11.sp,
                        color = DarkTextSecondary
                    )
                }
            }

            if (schedule.completedCount > 0) {
                TextButton(
                    onClick = onReset,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Reset", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun DincharyaActivityCard(
    activity: DincharyaActivity,
    isExpanded: Boolean,
    isCurrentlyActive: Boolean,
    timeFormatter: DateTimeFormatter,
    onToggleExpand: () -> Unit,
    onToggleComplete: () -> Unit,
    onSetAlarm: () -> Unit,
    onAddToCalendar: () -> Unit,
    onNavigateToChakra: (() -> Unit)?
) {
    val categoryColor = Color(activity.category.colorHex)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (activity.isCompleted) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            } else if (isCurrentlyActive) {
                categoryColor.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isCurrentlyActive) 2.dp else 1.dp,
            color = if (isCurrentlyActive) categoryColor else if (activity.isCompleted) AuspiciousGreen.copy(alpha = 0.5f) else LightBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Time, Category Badge, Status, Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Color Pill
                    Box(
                        modifier = Modifier
                            .size(8.dp, 28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${activity.startTime.format(timeFormatter)} - ${activity.endTime.format(timeFormatter)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentlyActive) categoryColor else MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrentlyActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = categoryColor,
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE NOW",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = activity.phase.title,
                            fontSize = 10.sp,
                            color = DarkTextSecondary
                        )
                    }
                }

                Checkbox(
                    checked = activity.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AuspiciousGreen,
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Activity Titles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (activity.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = activity.sanskritTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = if (activity.isCompleted) Color.LightGray else GoldPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                if (activity.muhurtaTag != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(0.8.dp, GoldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = activity.muhurtaTag,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaroonPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (activity.isRecommendedForGoal) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐ Recommended for your goal",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                }
            }

            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = DarkTextSecondary,
                    lineHeight = 16.sp
                ),
                modifier = Modifier.padding(top = 6.dp)
            )

            // Expandable Deep Details
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = LightBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Actionable Steps
                    Text(
                        text = "Step-by-Step Instructions:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    activity.instructions.forEachIndexed { idx, step ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${idx + 1}.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                modifier = Modifier.width(18.dp)
                            )
                            Text(
                                text = step,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Mantra / Shloka Box
                    if (activity.mantra != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaroonPrimary.copy(alpha = 0.06f),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "🕉 Sacred Vedic Chanting",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activity.mantra.sanskrit,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activity.mantra.transliteration,
                                    fontSize = 10.sp,
                                    color = DarkTextSecondary,
                                    fontFamily = FontFamily.Serif
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Meaning: ${activity.mantra.meaning}",
                                    fontSize = 10.sp,
                                    color = DarkTextSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Ayurvedic Principle
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AuspiciousGreen.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("🌿", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Ayurvedic Circadian Principle",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuspiciousGreen
                                )
                                Text(
                                    text = activity.ayurvedicPrinciple,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    if (activity.cautionNote != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠ ${activity.cautionNote}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InauspiciousRed
                        )
                    }

                    // Quick Action Shortcuts: Set Alarm, Add to Calendar
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSetAlarm,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = "Alarm", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Set Alarm", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onAddToCalendar,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Cal", fontSize = 11.sp)
                        }

                        if (onNavigateToChakra != null && (activity.category == DincharyaCategory.MEDITATION_DHYANA || activity.category == DincharyaCategory.YOGA_PRANAYAMA)) {
                            Button(
                                onClick = onNavigateToChakra,
                                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text("Chant / Focus", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AyurvedicNutritionDialog(
    meals: List<DincharyaMealRecommendation>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🥗 Ayurvedic Mitahara & Meals", fontWeight = FontWeight.Bold, color = MaroonPrimary)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Vedic nutrition follows the solar rhythm of Jatharagni (digestive fire). Food is medicine when eaten in right quantity (Mitahara) at the right astronomical hour.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                    )
                }

                items(meals) { meal ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, LightBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = meal.mealType,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary
                                )
                                Text(
                                    text = meal.suggestedTime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            }
                            Text(
                                text = "Agni Status: ${meal.agniStatus}",
                                fontSize = 10.sp,
                                color = AuspiciousGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = meal.principle,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "✓ Recommended: ${meal.recommendedFoods.joinToString(", ")}",
                                fontSize = 10.sp,
                                color = AuspiciousGreen
                            )
                            Text(
                                text = "✗ Avoid: ${meal.avoidFoods.joinToString(", ")}",
                                fontSize = 10.sp,
                                color = InauspiciousRed
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold, color = MaroonPrimary)
            }
        }
    )
}
