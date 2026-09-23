import sys
import re

path = "app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt"
with open(path, "r") as f:
    content = f.read()

# Add onRefresh to DailyHighlightsTab
if "onRefresh: () -> Unit = {}," not in content:
    content = content.replace(
        "onLocationSynced: ((LocationData) -> Unit)? = null,",
        "onLocationSynced: ((LocationData) -> Unit)? = null,\n    onRefresh: () -> Unit = {},"
    )
    
    # Add Refresh button next to Personal Greeting
    pattern = r"// PERSONAL GREETING\s+// ==========================================\s+Text\(\s+text = \"Namaste.*?\s+.*?color = MaroonPrimary\s+\)\s+\)"
    
    replacement = """// PERSONAL GREETING
        // ==========================================
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    text = "Namaste, ${personalizedProfile.userBirthProfile.fullName.split(" ").firstOrNull() ?: ""}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )
                Text(
                    text = "Welcome to your daily Vedic insights.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DarkTextSecondary
                    )
                )
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.background(MaroonPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh Data", tint = MaroonPrimary)
            }
        }"""
        
    content = re.sub(pattern, replacement, content, flags=re.DOTALL)
    
    # Remove the old Text("Welcome to your daily Vedic insights.") that was underneath the old one
    content = content.replace("""        Text(
            text = "Welcome to your daily Vedic insights.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DarkTextSecondary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))""", """        Spacer(modifier = Modifier.height(16.dp))""")

    # Let's add the import for Icons.Default.Refresh if not present
    if "import androidx.compose.material.icons.filled.Refresh" not in content:
        content = content.replace("import androidx.compose.material.icons.filled.WbSunny", "import androidx.compose.material.icons.filled.WbSunny\nimport androidx.compose.material.icons.filled.Refresh")

    with open(path, "w") as f:
        f.write(content)
