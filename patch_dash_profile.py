with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

orig = """        val targetZdt = ZonedDateTime.now(zoneId).plusDays(selectedDateOffset.toLong())
        personalizationEngine.createDailyVedicProfile(
            now = targetZdt,
            location = profile.birthLocation,
            ayanamsha = profile.ayanamsha,
            tradition = profile.calendarTradition
        )"""

new = """        val targetZdt = ZonedDateTime.now(zoneId).plusDays(selectedDateOffset.toLong())
        personalizationEngine.createDailyVedicProfile(
            now = targetZdt,
            location = profile.birthLocation,
            ayanamsha = profile.ayanamsha,
            tradition = profile.calendarTradition,
            personalizedProfile = personalizedResult
        )"""

content = content.replace(orig, new)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
