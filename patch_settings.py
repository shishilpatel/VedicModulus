import re

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "r") as f:
    content = f.read()

# Add state variables for smartwatch
state_vars_insert = """
    var hasSmartWatch by remember(currentProfile) { mutableStateOf(currentProfile.hasSmartWatch) }
    var watchModel by remember(currentProfile) { mutableStateOf(currentProfile.watchModel) }
"""
content = content.replace("    var selectedNumerology by remember(currentProfile) { mutableStateOf(currentProfile.numerologySystem) }", 
                          "    var selectedNumerology by remember(currentProfile) { mutableStateOf(currentProfile.numerologySystem) }\n" + state_vars_insert)

# Include in draft profile
content = content.replace(
"""            hasSmartWatch = currentProfile.hasSmartWatch,
            watchModel = currentProfile.watchModel""",
"""            hasSmartWatch = hasSmartWatch,
            watchModel = watchModel""")

# Add to the UI (at the bottom before the save button)
ui_insert = """
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Smartwatch Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaroonPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, LightBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("I have a Wear OS Smartwatch", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        Switch(
                                            checked = hasSmartWatch,
                                            onCheckedChange = { hasSmartWatch = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimary, checkedTrackColor = MaroonPrimary.copy(alpha = 0.5f))
                                        )
                                    }
                                    if (hasSmartWatch) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = watchModel,
                                            onValueChange = { watchModel = it },
                                            label = { Text("Watch Model (e.g. Galaxy Watch 6)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }
                                }
                            }
                        }
"""
content = content.replace("                        // Notification Settings", ui_insert + "\n                        // Notification Settings")

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "w") as f:
    f.write(content)
