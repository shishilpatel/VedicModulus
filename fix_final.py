import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "var personalizedResult by remember { mutableStateOf<PersonalizedProfile?>(null) }" in line:
        new_lines.append(line)
        continue
    
    if "val currentPersonalizedResult = personalizedResult" in line:
        continue # we will insert it manually
    if "val currentDailyResult = dailyResult" in line:
        continue # we will insert it manually
    if "if (currentDailyResult == null || currentPersonalizedResult == null) {" in line:
        skip = True
        continue
    if skip:
        if "return" in line:
            pass
        elif "}" in line:
            skip = False
        continue

    new_lines.append(line)

content = "".join(new_lines)

# Now insert correctly
insert_pers = """    var personalizedResult by remember { mutableStateOf<PersonalizedProfile?>(null) }
    LaunchedEffect(profile) {
        personalizedResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            personalizationEngine.createPersonalizedProfile(profile)
        }
    }
    
    val currentPersonalizedResult = personalizedResult
    if (currentPersonalizedResult == null) {
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator(color = MaroonPrimary)
        }
        return
    }"""

content = re.sub(r'    var personalizedResult by remember \{ mutableStateOf<PersonalizedProfile\?>\(null\) \}\n    LaunchedEffect\(profile\) \{\n        personalizedResult = kotlinx\.coroutines\.withContext\(kotlinx\.coroutines\.Dispatchers\.Default\) \{\n            personalizationEngine\.createPersonalizedProfile\(profile\)\n        \}\n    \}', insert_pers, content)

insert_daily = """        dailyResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            personalizationEngine.createDailyVedicProfile(
                now = targetZdt,
                location = activeLocation,
                ayanamsha = profile.ayanamsha,
                tradition = profile.calendarTradition,
                personalizedProfile = currentPersonalizedResult
            )
        }
    }

    val currentDailyResult = dailyResult
    if (currentDailyResult == null) {
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator(color = MaroonPrimary)
        }
        return
    }"""

content = re.sub(r'        dailyResult = kotlinx\.coroutines\.withContext\(kotlinx\.coroutines\.Dispatchers\.Default\) \{\n            personalizationEngine\.createDailyVedicProfile\(\n                now = targetZdt,\n                location = activeLocation,\n                ayanamsha = profile\.ayanamsha,\n                tradition = profile\.calendarTradition,\n                personalizedProfile = currentPersonalizedResult\n            \)\n        \}\n    \}', insert_daily, content)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
