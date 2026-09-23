import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace("DashboardTab.VEDIC_AI_ASSISTANT -> VedicAITab()", """DashboardTab.VEDIC_AI_ASSISTANT -> VedicAITab(
                        profile = profile,
                        personalizedProfile = personalizedResult,
                        dailyProfile = dailyResult
                    )""")

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
