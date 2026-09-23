with open("app/src/main/java/com/example/ui/screens/tabs/ChoghadiyaHoraTab.kt", "r") as f:
    content = f.read()

bad = """    val solarTimes = remember(date, city) {
        astroEngine.calculateSolarTimes(date, city.latitude, city.longitude)
    }

    val solarTimesTomorrow = remember(nextDate, city) {
        astroEngine.calculateSolarTimes(nextDate, city.latitude, city.longitude)
    }

    val choghadiyaPeriods = remember(date, city) {
        choghadiyaEngine.calculateDailyChoghadiya(
            solarTimes,
            solarTimesTomorrow.sunrise,
            date.dayOfWeek
        )
    }

    val horaPeriods = remember(date, city) {
        horaEngine.calculateDailyHoras(
            solarTimes,
            solarTimesTomorrow.sunrise,
            date.dayOfWeek
        )
    }"""

good = """    val state = remember { mutableStateOf<Pair<List<com.example.engines.choghadiya.ChoghadiyaPeriod>, List<com.example.engines.hora.HoraPeriod>>?>(null) }
    LaunchedEffect(date, city) {
        val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val sToday = astroEngine.calculateSolarTimes(date, city.latitude, city.longitude)
            val sTom = astroEngine.calculateSolarTimes(nextDate, city.latitude, city.longitude)
            val c = choghadiyaEngine.calculateDailyChoghadiya(sToday, sTom.sunrise, date.dayOfWeek)
            val h = horaEngine.calculateDailyHoras(sToday, sTom.sunrise, date.dayOfWeek)
            Pair(c, h)
        }
        state.value = result
    }

    if (state.value == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaroonPrimary)
        }
        return
    }

    val choghadiyaPeriods = state.value!!.first
    val horaPeriods = state.value!!.second"""

content = content.replace(bad, good)
with open("app/src/main/java/com/example/ui/screens/tabs/ChoghadiyaHoraTab.kt", "w") as f:
    f.write(content)

