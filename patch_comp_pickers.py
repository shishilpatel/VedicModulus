import sys
import re

path = "app/src/main/java/com/example/ui/screens/tabs/CompatibilityMatchTab.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r"Text\(\"Partner's Date of Birth\".*?Spacer\(modifier = Modifier\.height\(16\.dp\)\)"
replacement = """var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = try { 
                java.time.LocalDate.of(pYear.toInt(), pMonth.toInt(), pDay.toInt())
                    .atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch(e:Exception) { null }
        )
        
        val timeState = rememberTimePickerState(
            initialHour = pHour.toIntOrNull() ?: 12,
            initialMinute = pMinute.toIntOrNull() ?: 0,
            is24Hour = false
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Partner Date of Birth", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${pDay.padStart(2, '0')} / ${pMonth.padStart(2, '0')} / $pYear",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            
            OutlinedCard(
                onClick = { showTimePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Partner Time of Birth", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${pHour.padStart(2, '0')}:${pMinute.padStart(2, '0')}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
        
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                            pYear = date.year.toString()
                            pMonth = date.monthValue.toString()
                            pDay = date.dayOfMonth.toString()
                            savePrefs()
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
        
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pHour = timeState.hour.toString()
                        pMinute = timeState.minute.toString()
                        savePrefs()
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
        
        Spacer(modifier = Modifier.height(16.dp))"""

content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open(path, "w") as f:
    f.write(content)
