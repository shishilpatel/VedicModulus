import sys
import re

path = "app/src/main/java/com/example/ui/screens/DashboardScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Add refreshTrigger
if "var refreshTrigger by remember { mutableStateOf(0) }" not in content:
    content = content.replace(
        "var selectedTab by remember { mutableStateOf(NavCategory.DASHBOARD.tabs.first()) }",
        "var selectedTab by remember { mutableStateOf(NavCategory.DASHBOARD.tabs.first()) }\n    var refreshTrigger by remember { mutableStateOf(0) }"
    )
    
    # Update remember block for dailyResult
    content = content.replace(
        "val dailyResult = remember(activeLocation, profile, personalizedResult, currentTime.minute, selectedDateOffset) {",
        "val dailyResult = remember(activeLocation, profile, personalizedResult, currentTime.minute, selectedDateOffset, refreshTrigger) {"
    )

    # Pass onRefresh to DailyHighlightsTab
    content = content.replace(
        """                    DashboardTab.DAILY_HIGHLIGHTS -> DailyHighlightsTab(
                        dailyProfile = dailyResult,
                        personalizedProfile = personalizedResult,
                        currentLocation = activeLocation,
                        onLocationSynced = { syncedLoc ->
                            userSyncedLocation = syncedLoc
                        }
                    )""",
        """                    DashboardTab.DAILY_HIGHLIGHTS -> DailyHighlightsTab(
                        dailyProfile = dailyResult,
                        personalizedProfile = personalizedResult,
                        currentLocation = activeLocation,
                        onLocationSynced = { syncedLoc ->
                            userSyncedLocation = syncedLoc
                        },
                        onRefresh = { refreshTrigger++ }
                    )"""
    )
    
    with open(path, "w") as f:
        f.write(content)
