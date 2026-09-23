import sys
import re

path = "app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Replace the Date/Time TextFields in UserProfileSettingsScreen with DatePicker and TimePicker dialogs or simple buttons
# Finding the SECTION 2: Birth Details
pattern = r"// SECTION 2: Birth Details.*?// SECTION 3: Birth Location"
replacement = """// SECTION 2: Birth Details
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Birth Date & Time",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    
                    var showDatePicker by remember { mutableStateOf(false) }
                    var showTimePicker by remember { mutableStateOf(false) }
                    
                    val dateState = rememberDatePickerState(
                        initialSelectedDateMillis = try { 
                            java.time.LocalDate.of(birthYear.toInt(), birthMonth.toInt(), birthDay.toInt())
                                .atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
                        } catch(e:Exception) { null }
                    )
                    
                    val timeState = rememberTimePickerState(
                        initialHour = birthHour.toIntOrNull() ?: 12,
                        initialMinute = birthMinute.toIntOrNull() ?: 0,
                        is24Hour = false
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Date of Birth", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${birthDay.padStart(2, '0')} / ${birthMonth.padStart(2, '0')} / $birthYear",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                        
                        OutlinedCard(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Time of Birth", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${birthHour.padStart(2, '0')}:${birthMinute.padStart(2, '0')}",
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
                                        birthYear = date.year.toString()
                                        birthMonth = date.monthValue.toString()
                                        birthDay = date.dayOfMonth.toString()
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
                                    birthHour = timeState.hour.toString()
                                    birthMinute = timeState.minute.toString()
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

            // SECTION 3: Birth Location"""

content = re.sub(pattern, replacement, content, flags=re.DOTALL)
with open(path, "w") as f:
    f.write(content)
