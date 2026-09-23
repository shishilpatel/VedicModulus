import re

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "r") as f:
    content = f.read()

smartwatch_card = """            // Smartwatch Settings
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Smartwatch Integration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Watch, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("I have a Wear OS Smartwatch", fontWeight = FontWeight.SemiBold)
                                Text("Enables Watch Face syncing", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            }
                        }
                        Switch(
                            checked = hasSmartWatch,
                            onCheckedChange = { hasSmartWatch = it }
                        )
                    }
                    if (hasSmartWatch) {
                        OutlinedTextField(
                            value = watchModel,
                            onValueChange = { watchModel = it },
                            label = { Text("Watch Model (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
"""

content = content.replace('            Text("Reset Profile (Restart Onboarding)")\n            }\n\n', '            Text("Reset Profile (Restart Onboarding)")\n            }\n\n' + smartwatch_card)

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "w") as f:
    f.write(content)

