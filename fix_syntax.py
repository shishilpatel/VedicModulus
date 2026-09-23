import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

bad_syntax = """    if (val currentPersonalizedResult = personalizedResult
    val currentDailyResult = dailyResult
    if (currentDailyResult == null || currentPersonalizedResult == null)) {"""

good_syntax = """    val currentPersonalizedResult = personalizedResult
    val currentDailyResult = dailyResult
    if (currentDailyResult == null || currentPersonalizedResult == null) {"""

content = content.replace(bad_syntax, good_syntax)

# We also need to fix:
# `personalizationEngine.generateWatchFaceConfig(personalizedResult, activeStyle)`
# It should be `currentPersonalizedResult!!`
content = content.replace("personalizationEngine.generateWatchFaceConfig(personalizedResult, activeStyle)", "personalizationEngine.generateWatchFaceConfig(currentPersonalizedResult!!, activeStyle)")

# We also need to fix `dailyResult.panchanga.tithi` in LaunchedEffect!
content = content.replace("LaunchedEffect(activeStyle, dailyResult.panchanga.tithi, dailyResult.panchanga.nakshatra)", "LaunchedEffect(activeStyle, currentDailyResult!!.panchanga.tithi, currentDailyResult!!.panchanga.nakshatra)")

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
