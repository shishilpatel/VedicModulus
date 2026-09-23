import re

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "r") as f:
    content = f.read()

disclaimer_old = """            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = MaroonPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Important: This app provides astrology, numerology, Vastu, meditation, yoga and spiritual content for informational and personal guidance purposes only. Results and interpretations are not guaranteed and should not be considered professional advice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkText
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }"""

disclaimer_new = """            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = MaroonPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Important: This app provides astrology, numerology, Vastu, meditation, yoga and spiritual content for informational and personal guidance purposes only. Results and interpretations are not guaranteed and should not be considered professional advice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkText
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { /* Open Privacy Policy URL */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Privacy Policy & Terms of Service",
                    color = MaroonPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }"""

content = content.replace(disclaimer_old, disclaimer_new)

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "w") as f:
    f.write(content)
