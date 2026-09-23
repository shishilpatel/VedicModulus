import sys

path = "app/src/main/java/com/example/ui/screens/DashboardScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Add to enum class DashboardTab
content = content.replace(
    "    USER_PROFILE_SETTINGS(\"Profile Settings\")\n}",
    "    USER_PROFILE_SETTINGS(\"Profile Settings\"),\n    COMPATIBILITY_CHECKER(\"Compatibility Match\")\n}"
)

# Add to NavCategory
content = content.replace(
    "DashboardTab.PAST_READINGS, DashboardTab.VEDIC_AI_ASSISTANT)",
    "DashboardTab.PAST_READINGS, DashboardTab.VEDIC_AI_ASSISTANT, DashboardTab.COMPATIBILITY_CHECKER)"
)

# Add to string mapping
content = content.replace(
    "        DashboardTab.USER_PROFILE_SETTINGS -> \"Profile Settings\"",
    "        DashboardTab.USER_PROFILE_SETTINGS -> \"Profile Settings\"\n        DashboardTab.COMPATIBILITY_CHECKER -> \"Compatibility Match\""
)

# Add to `when(selectedTab)`
content = content.replace(
    """                    DashboardTab.USER_PROFILE_SETTINGS -> UserProfileSettingsScreen(
                        currentProfile = profile,
                        onBack = { selectedTab = DashboardTab.DAILY_HIGHLIGHTS },
                        onProfileSaved = { updatedProfile ->
                            onProfileAdd(updatedProfile)
                        }
                    )""",
    """                    DashboardTab.USER_PROFILE_SETTINGS -> UserProfileSettingsScreen(
                        currentProfile = profile,
                        onBack = { selectedTab = DashboardTab.DAILY_HIGHLIGHTS },
                        onProfileSaved = { updatedProfile ->
                            onProfileAdd(updatedProfile)
                        }
                    )
                    DashboardTab.COMPATIBILITY_CHECKER -> com.example.ui.screens.tabs.CompatibilityMatchTab(
                        profile = profile,
                        personalizationEngine = personalizationEngine
                    )"""
)

with open(path, "w") as f:
    f.write(content)
print("Patched Dashboard enum")
