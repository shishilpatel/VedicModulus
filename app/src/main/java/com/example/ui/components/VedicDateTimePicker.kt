package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Material 3 Date Picker Dialog supporting LocalDate.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VedicDatePickerModal(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = remember(selectedDate) {
        selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val pickedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onDateSelected(pickedDate)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
            ) {
                Text("Select Date")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaroonPrimary)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = MaroonPrimary,
                todayDateBorderColor = OrangeAccent,
                todayContentColor = MaroonPrimary
            )
        )
    }
}

/**
 * Material 3 Time Picker Dialog supporting LocalTime.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VedicTimePickerModal(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    is24Hour: Boolean = false
) {
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = is24Hour
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaroonPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select Time of Birth",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )
            }
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = MaroonPrimary,
                        selectorColor = MaroonPrimary,
                        periodSelectorSelectedContainerColor = MaroonPrimary,
                        periodSelectorSelectedContentColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pickedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onTimeSelected(pickedTime)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
            ) {
                Text("Confirm Time")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaroonPrimary)
            }
        }
    )
}

/**
 * Interactive Date Picker Field displaying formatted date and opening DatePickerDialog on tap.
 */
@Composable
fun VedicDatePickerField(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    label: String = "Date of Birth",
    modifier: Modifier = Modifier,
    testTag: String = "vedic_date_picker_field",
    borderColor: Color = LightBorder,
    accentColor: Color = MaroonPrimary
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        VedicDatePickerModal(
            selectedDate = date,
            onDateSelected = onDateChange,
            onDismiss = { showDialog = false }
        )
    }

    Surface(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US)),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pick Date",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Interactive Time Picker Field displaying formatted time (12h & 24h) and opening TimePickerDialog on tap.
 */
@Composable
fun VedicTimePickerField(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    label: String = "Time of Birth",
    modifier: Modifier = Modifier,
    testTag: String = "vedic_time_picker_field",
    borderColor: Color = LightBorder,
    accentColor: Color = OrangeAccent
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        VedicTimePickerModal(
            selectedTime = time,
            onTimeSelected = onTimeChange,
            onDismiss = { showDialog = false }
        )
    }

    Surface(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${time.format(DateTimeFormatter.ofPattern("hh:mm a"))} (${time.format(DateTimeFormatter.ofPattern("HH:mm"))})",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Pick Time",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Combined Date and Time Picker Row
 */
@Composable
fun VedicDateTimeRow(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    dateLabel: String = "Date of Birth",
    timeLabel: String = "Time of Birth",
    modifier: Modifier = Modifier,
    borderColor: Color = LightBorder
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        VedicDatePickerField(
            date = date,
            onDateChange = onDateChange,
            label = dateLabel,
            modifier = Modifier.weight(1.1f),
            borderColor = borderColor
        )
        VedicTimePickerField(
            time = time,
            onTimeChange = onTimeChange,
            label = timeLabel,
            modifier = Modifier.weight(1f),
            borderColor = borderColor
        )
    }
}
