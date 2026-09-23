import re

with open("app/src/main/java/com/example/ui/screens/tabs/MuhurtasTab.kt", "r") as f:
    content = f.read()

# Add imports
if "com.example.core.model.MuhurtaDeity" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.core.model.MuhurtaDeity")
    
# Add deity parameter to TimingCard calls
content = content.replace("""        // Brahma Muhurta
        TimingCard(
            title = dailyProfile.brahmaMuhurta.name,
            devanagari = dailyProfile.brahmaMuhurta.devanagari,
            start = dailyProfile.brahmaMuhurta.startTime.format(timeFormatter),
            end = dailyProfile.brahmaMuhurta.endTime.format(timeFormatter),
            isAuspicious = dailyProfile.brahmaMuhurta.isAuspicious,
            description = "The most sacred time for spiritual practices, meditation, and seeking divine knowledge."
        )""", """        // Brahma Muhurta
        TimingCard(
            title = dailyProfile.brahmaMuhurta.name,
            devanagari = dailyProfile.brahmaMuhurta.devanagari,
            start = dailyProfile.brahmaMuhurta.startTime.format(timeFormatter),
            end = dailyProfile.brahmaMuhurta.endTime.format(timeFormatter),
            isAuspicious = dailyProfile.brahmaMuhurta.isAuspicious,
            description = "The most sacred time for spiritual practices, meditation, and seeking divine knowledge.",
            deity = dailyProfile.brahmaMuhurta.deity
        )""")
        
content = content.replace("""            TimingCard(
                title = abhijit.name,
                devanagari = abhijit.devanagari,
                start = abhijit.startTime.format(timeFormatter),
                end = abhijit.endTime.format(timeFormatter),
                isAuspicious = abhijit.isAuspicious,
                description = if (abhijit.isAuspicious) "Powerful midday period capable of destroying myriad doshas. Excellent for new initiatives." else "Abhijit is nullified today due to Wednesday."
            )""", """            TimingCard(
                title = abhijit.name,
                devanagari = abhijit.devanagari,
                start = abhijit.startTime.format(timeFormatter),
                end = abhijit.endTime.format(timeFormatter),
                isAuspicious = abhijit.isAuspicious,
                description = if (abhijit.isAuspicious) "Powerful midday period capable of destroying myriad doshas. Excellent for new initiatives." else "Abhijit is nullified today due to Wednesday.",
                deity = abhijit.deity
            )""")
            
# Update TimingCard signature and implementation
orig_timing_card = """private fun TimingCard(
    title: String,
    devanagari: String,
    start: String,
    end: String,
    isAuspicious: Boolean,
    description: String
) {"""

new_timing_card = """private fun TimingCard(
    title: String,
    devanagari: String,
    start: String,
    end: String,
    isAuspicious: Boolean,
    description: String,
    deity: MuhurtaDeity? = null
) {"""

content = content.replace(orig_timing_card, new_timing_card)

# Insert UI for deity and mantra inside TimingCard
orig_desc = """            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )"""
            
new_desc = """            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            if (deity != null) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Divider(color = LightBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Star,
                        contentDescription = "Deity",
                        tint = OrangeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Deity: ${deity.deityName} (${deity.sanskritName})",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaroonPrimary)
                    )
                }
                deity.associatedMantra?.let { mantra ->
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF8F0), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = mantra.sanskrit, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary))
                            Text(text = mantra.transliteration, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = mantra.meaning, style = MaterialTheme.typography.labelSmall.copy(color = MaroonPrimary))
                        }
                    }
                }
            }"""

content = content.replace(orig_desc, new_desc)

with open("app/src/main/java/com/example/ui/screens/tabs/MuhurtasTab.kt", "w") as f:
    f.write(content)
