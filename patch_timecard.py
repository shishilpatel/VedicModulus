import sys

with open("app/src/main/java/com/example/ui/components/VedicTimeCard.kt", "r") as f:
    content = f.read()

target = """    val zdt = dailyProfile.currentZonedDateTime
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
    val sunriseFormatter = DateTimeFormatter.ofPattern("hh:mm a")"""

replacement = """    var zdt by remember(dailyProfile.currentZonedDateTime) { mutableStateOf(dailyProfile.currentZonedDateTime) }
    
    LaunchedEffect(currentLocation) {
        val zone = java.time.ZoneId.of(currentLocation.timezoneId)
        while(true) {
            kotlinx.coroutines.delay(1000L)
            zdt = java.time.ZonedDateTime.now(zone)
        }
    }

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
    val sunriseFormatter = DateTimeFormatter.ofPattern("hh:mm a")"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/components/VedicTimeCard.kt", "w") as f:
        f.write(content)
    print("Patched VedicTimeCard.kt")
else:
    print("Target not found in VedicTimeCard.kt")

