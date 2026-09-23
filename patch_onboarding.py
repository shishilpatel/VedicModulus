with open("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt", "r") as f:
    content = f.read()

orig = """        val dailyResult = remember(selectedCity, selectedAyanamsha, selectedTradition) {
        personalizationEngine.createDailyVedicProfile(
            now = ZonedDateTime.now(java.time.ZoneId.of(selectedCity.timezoneId)),
            location = selectedCity,
            ayanamsha = selectedAyanamsha,
            tradition = selectedTradition
        )"""

new = """        val dailyResult = remember(selectedCity, selectedAyanamsha, selectedTradition, personalizedResult) {
        personalizationEngine.createDailyVedicProfile(
            now = ZonedDateTime.now(java.time.ZoneId.of(selectedCity.timezoneId)),
            location = selectedCity,
            ayanamsha = selectedAyanamsha,
            tradition = selectedTradition,
            personalizedProfile = personalizedResult
        )"""

content = content.replace(orig, new)

with open("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt", "w") as f:
    f.write(content)
