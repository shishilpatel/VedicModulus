import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

bad_pers = """    val personalizedResult = remember(profile) {
        personalizationEngine.createPersonalizedProfile(profile)
    }"""

good_pers = """    var personalizedResult by remember { mutableStateOf<PersonalizedProfile?>(null) }
    LaunchedEffect(profile) {
        personalizedResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            personalizationEngine.createPersonalizedProfile(profile)
        }
    }"""

content = content.replace(bad_pers, good_pers)
content = content.replace("val currentDailyResult = dailyResult", "val currentPersonalizedResult = personalizedResult\n    val currentDailyResult = dailyResult")
content = content.replace("currentDailyResult == null", "currentDailyResult == null || currentPersonalizedResult == null")
content = content.replace("personalizedProfile = personalizedResult", "personalizedProfile = currentPersonalizedResult")
content = content.replace("personalizedResult, currentTime.minute", "currentPersonalizedResult, currentTime.minute")
content = content.replace("remember(personalizedResult, activeStyle)", "remember(currentPersonalizedResult, activeStyle)")

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
