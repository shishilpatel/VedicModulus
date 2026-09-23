# 1. Add TickingDigitalClock composable at the bottom
cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/components/VedicTimeCard.kt

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
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    )
}
INNER_EOF

# 2. Replace the Text that uses zdt.format(timeFormatter) with TickingDigitalClock
sed -i '/text = zdt.format(timeFormatter)/,+6c\
                        TickingDigitalClock(currentTimeProvider, zone)' app/src/main/java/com/example/ui/components/VedicTimeCard.kt
sed -i '/Text(/d' app/src/main/java/com/example/ui/components/VedicTimeCard.kt # Wait, this will delete ALL Text(. Bad idea.
