import re

with open("app/src/main/java/com/example/ui/components/VedicTimeCard.kt", "r") as f:
    content = f.read()

# Replace the specific Text block
text_block = """                        Text(
                            text = zdt.format(timeFormatter),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        )"""

content = content.replace(text_block, "                        TickingDigitalClock(currentTimeProvider, zone)")

# Also, at the top, replace:
#     val currentTimeValue = currentTimeProvider()
#     val zdt = remember(currentTimeValue, dailyProfile) { java.time.ZonedDateTime.of(dailyProfile.currentZonedDateTime.toLocalDate(), currentTimeValue, zone) }
# with:
#     val zdt = dailyProfile.currentZonedDateTime

content = re.sub(
    r"    val currentTimeValue = currentTimeProvider\(\)\n    val zdt = remember\(currentTimeValue, dailyProfile\) \{ java.time.ZonedDateTime.of\(dailyProfile.currentZonedDateTime.toLocalDate\(\), currentTimeValue, zone\) \}",
    r"    val zdt = dailyProfile.currentZonedDateTime",
    content
)

# Append the TickingDigitalClock composable
composable = """
@Composable
fun TickingDigitalClock(currentTimeProvider: () -> java.time.LocalTime, zone: java.time.ZoneId) {
    val currentTimeValue = currentTimeProvider()
    val zdt = remember(currentTimeValue) { java.time.ZonedDateTime.of(java.time.LocalDate.now(zone), currentTimeValue, zone) }
    val timeFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a") }
    Text(
        text = zdt.format(timeFormatter),
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaroonPrimary,
            fontFamily = FontFamily.Monospace
        )
    )
}
"""

with open("app/src/main/java/com/example/ui/components/VedicTimeCard.kt", "w") as f:
    f.write(content + composable)
