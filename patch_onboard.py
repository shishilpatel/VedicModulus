import sys
import re

path = "app/src/main/java/com/example/ui/screens/OnboardingScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Replace the text fields with modern Date/Time pickers.
pattern_date = r"@Composable\s+private fun Step2BirthDate[\s\S]*?@Composable\s+private fun Step3BirthTime"

replacement_date = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2BirthDate(
    year: String, onYearChange: (String) -> Unit,
    month: String, onMonthChange: (String) -> Unit,
    day: String, onDayChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "When were you born?",
            style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Used to determine planetary positions.",
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
                Text("Date of Birth", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
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
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = dateState)
            }
        }
    }
}

@Composable
private fun Step3BirthTime"""

content = re.sub(pattern_date, replacement_date, content, flags=re.DOTALL)


pattern_time = r"@Composable\s+private fun Step3BirthTime[\s\S]*?@Composable\s+private fun Step4BirthLocation"

replacement_time = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step3BirthTime(
    hour: String, onHourChange: (String) -> Unit,
    minute: String, onMinuteChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Exact time of birth",
            style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Crucial for exact Ascendant (Lagna) calculation.",
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
                Text("Time of Birth", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
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
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                text = {
                    TimePicker(state = timeState)
                }
            )
        }
    }
}

@Composable
private fun Step4BirthLocation"""

content = re.sub(pattern_time, replacement_time, content, flags=re.DOTALL)

with open(path, "w") as f:
    f.write(content)
