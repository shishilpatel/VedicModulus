import re

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "r") as f:
    content = f.read()

bad_keys = """    val draftProfile = remember(
        fullName, birthYear, birthMonth, birthDay, birthHour, birthMinute,
        selectedLocation, selectedAyanamsha, selectedTradition, selectedNumerology
    ) {"""

good_keys = """    val draftProfile = remember(
        fullName, birthYear, birthMonth, birthDay, birthHour, birthMinute,
        selectedLocation, selectedAyanamsha, selectedTradition, selectedNumerology,
        hasSmartWatch, watchModel
    ) {"""

content = content.replace(bad_keys, good_keys)

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "w") as f:
    f.write(content)
