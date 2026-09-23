package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
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
import com.example.engines.panchanga.MuhurtaReminderHelper
import com.example.engines.panchanga.PujaCategory
import com.example.engines.panchanga.PujaScheduleManager
import com.example.engines.panchanga.UpcomingPujaEvent
import com.example.ui.theme.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PujaScheduleSection(
    modifier: Modifier = Modifier,
    referenceDate: ZonedDateTime = remember { ZonedDateTime.now() }
) {
    val context = LocalContext.current
    val allEvents = remember(referenceDate) {
        PujaScheduleManager.getUpcomingPujas(referenceDate)
    }

    var selectedCategoryFilter by remember { mutableStateOf<PujaCategory?>(null) }
    var expandedEventId by remember { mutableStateOf<String?>(allEvents.firstOrNull()?.id) }

    val filteredEvents = remember(allEvents, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) allEvents
        else allEvents.filter { it.category == selectedCategoryFilter }
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, dd MMM yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Puja Schedule & Vrata Manager",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Tithi Fasting Rules, Ritual Timing & Divine Observances",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                    )
                }

                Surface(
                    color = SaffronAccent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${filteredEvents.size} Upcoming",
                        color = MaroonPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("All Pujas", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaroonPrimary,
                        selectedLabelColor = OnMaroon
                    )
                )

                FilterChip(
                    selected = selectedCategoryFilter == PujaCategory.EKADASHI,
                    onClick = {
                        selectedCategoryFilter = if (selectedCategoryFilter == PujaCategory.EKADASHI) null else PujaCategory.EKADASHI
                    },
                    label = { Text("Ekadashi", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaroonPrimary,
                        selectedLabelColor = OnMaroon
                    )
                )

                FilterChip(
                    selected = selectedCategoryFilter == PujaCategory.PRADOSHAM,
                    onClick = {
                        selectedCategoryFilter = if (selectedCategoryFilter == PujaCategory.PRADOSHAM) null else PujaCategory.PRADOSHAM
                    },
                    label = { Text("Pradosham", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaroonPrimary,
                        selectedLabelColor = OnMaroon
                    )
                )

                FilterChip(
                    selected = selectedCategoryFilter == PujaCategory.PURNIMA,
                    onClick = {
                        selectedCategoryFilter = if (selectedCategoryFilter == PujaCategory.PURNIMA) null else PujaCategory.PURNIMA
                    },
                    label = { Text("Purnima", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaroonPrimary,
                        selectedLabelColor = OnMaroon
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Events List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredEvents.forEach { event ->
                    val isExpanded = expandedEventId == event.id

                    Surface(
                        color = if (isExpanded) SaffronAccent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isExpanded) MaroonPrimary.copy(alpha = 0.6f) else LightBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedEventId = if (isExpanded) null else event.id
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Event Title Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = event.name,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaroonPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = GoldPrimary.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = event.tithiName.split(" ").first(),
                                                color = MaroonPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${event.devanagari} • ${event.date.format(dateFormatter)}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            MuhurtaReminderHelper.addToSystemCalendar(
                                                context = context,
                                                title = "${event.name} (${event.devanagari})",
                                                description = "${event.spiritualSignificance}\n\nPuja Window: ${event.pujaWindow}\n\nVrata Rules:\n" + event.vrataRules.joinToString("\n• ", prefix = "• "),
                                                startZoned = event.date,
                                                endZoned = event.date.plusHours(3)
                                            )
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Add to Calendar",
                                            tint = MaroonPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            MuhurtaReminderHelper.scheduleAdvanceReminder(
                                                context = context,
                                                title = event.name,
                                                startZoned = event.date,
                                                leadMinutes = 30
                                            )
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "Set Reminder",
                                            tint = OrangeAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle",
                                        tint = DarkTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Expandable Details
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                ) {
                                    HorizontalDivider(color = LightBorder)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Deity & Muhurta Window
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Presiding Deity: ${event.deity}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "⏰ ${event.pujaWindow}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaroonPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Vrata Rules
                                    Text(
                                        text = "Vrata Guidelines & Observances:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GrayText,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    event.vrataRules.forEach { rule ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(text = "• ", color = OrangeAccent, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = rule,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = DarkTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Sacred Mantra
                                    Surface(
                                        color = GoldPrimary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "SACRED JAPA MANTRA:",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaroonPrimary
                                            )
                                            Text(
                                                text = event.sacredMantra,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaroonPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = event.spiritualSignificance,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = GrayText,
                                            fontSize = 11.sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action Buttons Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                MuhurtaReminderHelper.setClockAlarm(
                                                    context = context,
                                                    title = "${event.name} Dawn Sankalpa",
                                                    startZoned = event.date
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Alarm,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaroonPrimary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Set Alarm", fontSize = 11.sp, color = MaroonPrimary)
                                        }

                                        Button(
                                            onClick = {
                                                MuhurtaReminderHelper.addToSystemCalendar(
                                                    context = context,
                                                    title = "${event.name} (${event.devanagari})",
                                                    description = "${event.spiritualSignificance}\n\nPuja Window: ${event.pujaWindow}\n\nVrata Rules:\n" + event.vrataRules.joinToString("\n• ", prefix = "• "),
                                                    startZoned = event.date,
                                                    endZoned = event.date.plusHours(3)
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Sync Calendar", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
