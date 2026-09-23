with open("app/src/main/java/com/example/ui/screens/tabs/KundaliTab.kt", "r") as f:
    content = f.read()

import re

# Add imports for date formatting
imports = """import java.time.format.DateTimeFormatter
"""
if "DateTimeFormatter" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\n" + imports)

# We want to add Ascendant Report Card and Dasha Timeline Card under the Planetary Longitudes Table
new_cards = """
        // Ascendant Report Card
        personalizedProfile.ascendantReport?.let { report ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ascendant (Lagna) Report",
                        style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your Ascendant: ${report.lagnaName}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Ruling Planet: ${report.rulingPlanet}",
                        style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = report.coreNature,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Physical Traits: ${report.physicalTraits}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mental Traits: ${report.mentalTraits}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lucky Colors: ${report.luckyColors.joinToString(", ")}", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary))
                        Text("Lucky Days: ${report.luckyDays.joinToString(", ")}", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary))
                    }
                }
            }
        }

        // Dasha Timeline Card
        if (personalizedProfile.dashaPeriods.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vimshottari Dasha Timeline (Mahadashas)",
                        style = MaterialTheme.typography.titleSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val dtf = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy")
                    
                    personalizedProfile.dashaPeriods.forEach { period ->
                        val isCurrent = period.isCurrent
                        val color = if (isCurrent) MaroonPrimary else DarkTextSecondary
                        val bgColor = if (isCurrent) OrangeAccent.copy(alpha = 0.1f) else Color.Transparent
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor, shape = RoundedCornerShape(4.dp))
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${period.lord.symbol} ${period.lord.displayName} Dasha",
                                    color = color,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = OrangeAccent, shape = RoundedCornerShape(4.dp)) {
                                        Text("ACTIVE", color = Color.White, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(
                                text = "${period.startDate.format(dtf)} - ${period.endDate.format(dtf)}",
                                color = color,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (isCurrent) {
                            Text(
                                text = "Current Mahadasha of ${period.lord.displayName}. This marks a significant period ruled by ${period.lord.displayName}'s significations.",
                                color = color.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}"""

content = content.replace("    }\n}", new_cards)

with open("app/src/main/java/com/example/ui/screens/tabs/KundaliTab.kt", "w") as f:
    f.write(content)
