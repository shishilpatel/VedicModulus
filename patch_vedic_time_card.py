import sys

# 1. Update DailyHighlightsTab.kt to pass currentTime
path = "app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt"
with open(path, "r") as f:
    content = f.read()
content = content.replace("VedicTimeCard(\n            dailyProfile = dailyProfile,", "VedicTimeCard(\n            dailyProfile = dailyProfile,\n            currentTime = currentTime,")
with open(path, "w") as f:
    f.write(content)

# 2. Update VedicTimeCard.kt to accept currentTime and remove internal LaunchedEffect
path = "app/src/main/java/com/example/ui/components/VedicTimeCard.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace(
"""fun VedicTimeCard(
    dailyProfile: DailyVedicProfile,
    currentLocation: LocationData,
    isGpsSynced: Boolean,
    onSyncLocationClick: () -> Unit,
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {""",
"""fun VedicTimeCard(
    dailyProfile: DailyVedicProfile,
    currentTime: java.time.LocalTime,
    currentLocation: LocationData,
    isGpsSynced: Boolean,
    onSyncLocationClick: () -> Unit,
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {""")

target_effect = """    var zdt by remember(dailyProfile.currentZonedDateTime) { mutableStateOf(dailyProfile.currentZonedDateTime) }
    
    LaunchedEffect(currentLocation) {
        val zone = java.time.ZoneId.of(currentLocation.timezoneId)
        while(true) {
            kotlinx.coroutines.delay(1000L)
            zdt = java.time.ZonedDateTime.now(zone)
        }
    }"""
    
replacement_effect = """    val zone = remember(currentLocation) { java.time.ZoneId.of(currentLocation.timezoneId) }
    val zdt = remember(currentTime, dailyProfile) { java.time.ZonedDateTime.of(dailyProfile.currentZonedDateTime.toLocalDate(), currentTime, zone) }"""

content = content.replace(target_effect, replacement_effect)

with open(path, "w") as f:
    f.write(content)
print("Patched VedicTimeCard realtime")

