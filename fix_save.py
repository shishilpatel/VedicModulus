import re

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "r") as f:
    content = f.read()

bad_copy = """                    val updatedProfile = currentProfile.copy(
                        fullName = fullName.trim(),
                        birthDate = newBirthDate,
                        birthTime = newBirthTime,
                        birthLocation = selectedLocation,
                        ayanamsha = selectedAyanamsha,
                        calendarTradition = selectedTradition,
                        numerologySystem = selectedNumerology
                    )"""

good_copy = """                    val updatedProfile = currentProfile.copy(
                        fullName = fullName.trim(),
                        birthDate = newBirthDate,
                        birthTime = newBirthTime,
                        birthLocation = selectedLocation,
                        ayanamsha = selectedAyanamsha,
                        calendarTradition = selectedTradition,
                        numerologySystem = selectedNumerology,
                        hasSmartWatch = hasSmartWatch,
                        watchModel = watchModel
                    )"""

content = content.replace(bad_copy, good_copy)

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "w") as f:
    f.write(content)
