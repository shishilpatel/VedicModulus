import re

with open("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt", "r") as f:
    content = f.read()

bad = """    val personalizedResult = remember(currentProfile) {
        personalizationEngine.createPersonalizedProfile(currentProfile)
    }

    val dailyResult = remember(selectedCity, selectedAyanamsha, selectedTradition, personalizedResult) {
        personalizationEngine.createDailyVedicProfile(
            now = ZonedDateTime.now(java.time.ZoneId.of(selectedCity.timezoneId)),
            location = selectedCity,
            ayanamsha = selectedAyanamsha,
            tradition = selectedTradition,
            personalizedProfile = personalizedResult
        )
    }

    val watchConfig = remember(personalizedResult, selectedStyle) {
        personalizationEngine.generateWatchFaceConfig(personalizedResult, selectedStyle)
    }"""

good = """    var personalizedResult by remember { mutableStateOf<PersonalizedProfile?>(null) }
    var dailyResult by remember { mutableStateOf<DailyVedicProfile?>(null) }
    var watchConfig by remember { mutableStateOf<WatchFaceConfiguration?>(null) }

    LaunchedEffect(currentProfile) {
        kotlinx.coroutines.delay(500L)
        val pResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            personalizationEngine.createPersonalizedProfile(currentProfile)
        }
        personalizedResult = pResult
        
        val zoneId = try {
            java.time.ZoneId.of(selectedCity.timezoneId)
        } catch (e: Exception) {
            java.time.ZoneId.systemDefault()
        }
        
        val dResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            personalizationEngine.createDailyVedicProfile(
                now = ZonedDateTime.now(zoneId),
                location = selectedCity,
                ayanamsha = selectedAyanamsha,
                tradition = selectedTradition,
                personalizedProfile = pResult
            )
        }
        dailyResult = dResult
        
        watchConfig = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            personalizationEngine.generateWatchFaceConfig(pResult, selectedStyle)
        }
    }"""

content = content.replace(bad, good)

# In OnboardingScreen, they might use personalizedResult directly, which is now nullable.
# Let's check usages of personalizedResult and dailyResult and watchConfig.
with open("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt", "w") as f:
    f.write(content)
