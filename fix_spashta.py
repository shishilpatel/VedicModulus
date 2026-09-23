import re

with open("app/src/main/java/com/example/ui/components/VedicRealtimeSpashtaCard.kt", "r") as f:
    content = f.read()

bad = """    val report: RealTimeGrahaSpashtaReport = remember(currentLocation, currentZonedDateTime.minute, selectedAyanamsa) {
        helper.calculateRealTimeGrahaSpashta(
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            altitudeMeters = 0.0,
            dateTime = currentZonedDateTime,
            ayanamshaMode = selectedAyanamsa.sweConst
        )
    }"""

good = """    var report by remember { mutableStateOf<RealTimeGrahaSpashtaReport?>(null) }
    LaunchedEffect(currentLocation, currentZonedDateTime.minute, selectedAyanamsa) {
        report = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            helper.calculateRealTimeGrahaSpashta(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                altitudeMeters = 0.0,
                dateTime = currentZonedDateTime,
                ayanamshaMode = selectedAyanamsa.sweConst
            )
        }
    }
    
    if (report == null) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            modifier = modifier.fillMaxWidth().height(200.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaroonPrimary)
            }
        }
        return
    }"""

content = content.replace(bad, good)

# In the rest of the file, we might have to use `report!!` since we early return.
# Let's write the file and compile.
with open("app/src/main/java/com/example/ui/components/VedicRealtimeSpashtaCard.kt", "w") as f:
    f.write(content)
