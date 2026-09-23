import re

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "r") as f:
    content = f.read()

insert_button = """            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        preferencesRepository.clearBirthProfile()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset Profile (Restart Onboarding)")
            }

            // Daily Push Notifications"""

content = content.replace("            // Daily Push Notifications", insert_button)

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "w") as f:
    f.write(content)
