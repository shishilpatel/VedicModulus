with open("app/src/main/java/com/example/ui/components/VedicCurrentDateCalendarComponent.kt", "r") as f:
    content = f.read()

bad = """    // Calculate Vedic Panchanga (Tithi & Nakshatra) for selected date at current/noon time
    val panchangaData = remember(selectedDate, location, ayanamsha, tradition) {
        val zoneId = try {
            ZoneId.of(location.timezoneId)
        } catch (e: Exception) {
            ZoneId.systemDefault()
        }
        val targetZdt = selectedDate.atTime(12, 0).atZone(zoneId)
        panchangaEngine.calculatePanchanga(
            dateTime = targetZdt,
            latitude = location.latitude,
            longitude = location.longitude,
            ayanamsha = ayanamsha,
            tradition = tradition
        )
    }

    val isCurrentDate = selectedDate == today
    val tithi = panchangaData.tithi
    val nakshatra = panchangaData.nakshatra
    val pada = panchangaData.nakshatraPada"""

good = """    // Calculate Vedic Panchanga (Tithi & Nakshatra) for selected date at current/noon time
    var panchangaData by remember { mutableStateOf<com.example.engines.panchanga.PanchangaResult?>(null) }
    LaunchedEffect(selectedDate, location, ayanamsha, tradition) {
        val zoneId = try {
            ZoneId.of(location.timezoneId)
        } catch (e: Exception) {
            ZoneId.systemDefault()
        }
        val targetZdt = selectedDate.atTime(12, 0).atZone(zoneId)
        panchangaData = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            panchangaEngine.calculatePanchanga(
                dateTime = targetZdt,
                latitude = location.latitude,
                longitude = location.longitude,
                ayanamsha = ayanamsha,
                tradition = tradition
            )
        }
    }

    if (panchangaData == null) {
        Card(
            modifier = modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.25f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaroonPrimary)
            }
        }
        return
    }

    val isCurrentDate = selectedDate == today
    val tithi = panchangaData!!.tithi
    val nakshatra = panchangaData!!.nakshatra
    val pada = panchangaData!!.nakshatraPada"""

content = content.replace(bad, good)
with open("app/src/main/java/com/example/ui/components/VedicCurrentDateCalendarComponent.kt", "w") as f:
    f.write(content)
