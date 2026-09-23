import sys

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

target = """                            Spacer(modifier = Modifier.width(8.dp))
                            // Smartwatch / Phone-only bridge indicator pill
                            val syncColor = if (!profile.hasSmartWatch) MaroonPrimary else if (isWatchSynced) AuspiciousGreen else InauspiciousRed
                            val syncText = if (!profile.hasSmartWatch) stringResource(R.string.phone_only) else if (isWatchSynced) stringResource(R.string.smartwatch_synced) else stringResource(R.string.watch_offline)
                            Surface(
                                color = syncColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, syncColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(syncColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = syncText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = syncColor,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }"""

if target in content:
    content = content.replace(target, "")
    with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
        f.write(content)
    print("Patched DashboardScreen.kt")
else:
    print("Target not found in DashboardScreen.kt")

