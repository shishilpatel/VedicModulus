import re

with open("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt", "r") as f:
    content = f.read()

bad = """                8 -> Step8Preview(
                    config = watchConfig,
                    personalizedProfile = personalizedResult,
                    dailyProfile = dailyResult
                )"""

good = """                8 -> {
                    if (watchConfig != null && personalizedResult != null && dailyResult != null) {
                        Step8Preview(
                            config = watchConfig!!,
                            personalizedProfile = personalizedResult!!,
                            dailyProfile = dailyResult!!
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaroonPrimary)
                        }
                    }
                }"""

content = content.replace(bad, good)

with open("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt", "w") as f:
    f.write(content)
