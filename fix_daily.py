import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

bad_daily = """    val dailyResult = remember(activeLocation, profile, personalizedResult, currentTime.minute, selectedDateOffset, refreshTrigger) {
        val zoneId = try {
            java.time.ZoneId.of(activeLocation.timezoneId)
        } catch (e: Exception) {
            java.time.ZoneId.systemDefault()
        }
        val targetZdt = ZonedDateTime.now(zoneId).plusDays(selectedDateOffset.toLong())
        personalizationEngine.createDailyVedicProfile(
            now = targetZdt,
            location = activeLocation,
            ayanamsha = profile.ayanamsha,
            tradition = profile.calendarTradition,
            personalizedProfile = personalizedResult
        )
    }"""

good_daily = """    var dailyResult by remember { mutableStateOf<DailyVedicProfile?>(null) }
    LaunchedEffect(activeLocation, profile, personalizedResult, currentTime.minute, selectedDateOffset, refreshTrigger) {
        val zoneId = try {
            java.time.ZoneId.of(activeLocation.timezoneId)
        } catch (e: Exception) {
            java.time.ZoneId.systemDefault()
        }
        val targetZdt = ZonedDateTime.now(zoneId).plusDays(selectedDateOffset.toLong())
        dailyResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            personalizationEngine.createDailyVedicProfile(
                now = targetZdt,
                location = activeLocation,
                ayanamsha = profile.ayanamsha,
                tradition = profile.calendarTradition,
                personalizedProfile = personalizedResult
            )
        }
    }

    if (dailyResult == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaroonPrimary)
        }
        return
    }
"""

content = content.replace(bad_daily, good_daily)

# Because we put "return", smart cast makes dailyResult non-null for the rest of the function!
# Actually, wait, smart cast from `var`? No, smart cast from `var` is NOT allowed in Kotlin if it can be mutated.
# We have to introduce a local val!
content = content.replace("dailyResult == null", "val currentDailyResult = dailyResult\n    if (currentDailyResult == null)")

# And then replace all usages of `dailyResult` with `currentDailyResult` in the subsequent code.
content = content.replace("dailyProfile = dailyResult", "dailyProfile = currentDailyResult")
content = content.replace("dailyResult.currentVedicPeriod", "currentDailyResult.currentVedicPeriod")

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
