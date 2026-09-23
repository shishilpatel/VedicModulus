with open("app/src/main/java/com/example/ui/screens/tabs/PanchangaTab.kt", "r") as f:
    content = f.read()

bad = """    val dailyResult = remember(date, city) {
        personalizationEngine.createDailyVedicProfile(
            userBirthProfile = profile.userBirthProfile,
            targetDate = date,
            targetLocation = city
        )
    }

    val taraBalaResult = remember(dailyResult) {
        tarabalaEngine.calculateDailyHarmony(
            profile.userBirthProfile.birthDate,
            profile.userBirthProfile.birthTime,
            profile.userBirthProfile.birthLocation,
            dailyResult.panchanga.nakshatra,
            profile.userBirthProfile.ayanamsha
        )
    }"""

good = """    val state = remember { mutableStateOf<Pair<com.example.core.model.DailyVedicProfile, com.example.engines.astrology.TaraBalaResult>?>(null) }
    LaunchedEffect(date, city) {
        val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val d = personalizationEngine.createDailyVedicProfile(
                userBirthProfile = profile.userBirthProfile,
                targetDate = date,
                targetLocation = city
            )
            val t = tarabalaEngine.calculateDailyHarmony(
                profile.userBirthProfile.birthDate,
                profile.userBirthProfile.birthTime,
                profile.userBirthProfile.birthLocation,
                d.panchanga.nakshatra,
                profile.userBirthProfile.ayanamsha
            )
            Pair(d, t)
        }
        state.value = result
    }

    if (state.value == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaroonPrimary)
        }
        return
    }

    val dailyResult = state.value!!.first
    val taraBalaResult = state.value!!.second"""

content = content.replace(bad, good)
with open("app/src/main/java/com/example/ui/screens/tabs/PanchangaTab.kt", "w") as f:
    f.write(content)

