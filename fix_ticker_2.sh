# Fix WatchFaceTab parameter
sed -i 's/currentTime: LocalTime,/currentTimeProvider: () -> LocalTime,/' app/src/main/java/com/example/ui/screens/tabs/WatchFaceTab.kt
# Fix WatchFaceTab usage of currentTime
sed -i 's/currentTime = currentTime,/currentTime = currentTimeProvider(),/g' app/src/main/java/com/example/ui/screens/tabs/WatchFaceTab.kt

# Fix DashboardScreen
sed -i 's/currentTime = currentTimeState.value,/currentTimeProvider = { currentTimeState.value },/' app/src/main/java/com/example/ui/screens/DashboardScreen.kt

# Fix VedicTimeCard parameter
sed -i 's/currentTime: java.time.LocalTime,/currentTimeProvider: () -> java.time.LocalTime,/' app/src/main/java/com/example/ui/components/VedicTimeCard.kt
# Fix VedicTimeCard usage of currentTime
sed -i 's/val zdt = remember(currentTime, dailyProfile) { java.time.ZonedDateTime.of(dailyProfile.currentZonedDateTime.toLocalDate(), currentTime, zone) }/val currentTimeValue = currentTimeProvider()\n    val zdt = remember(currentTimeValue, dailyProfile) { java.time.ZonedDateTime.of(dailyProfile.currentZonedDateTime.toLocalDate(), currentTimeValue, zone) }/' app/src/main/java/com/example/ui/components/VedicTimeCard.kt

# Fix DailyHighlightsTab
sed -i 's/currentTime = currentTimeState.value,/currentTimeProvider = { currentTimeState.value },/' app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt
sed -i 's/VedicTimeCalculator.getVedicGreeting(currentTimeState.value)/VedicTimeCalculator.getVedicGreeting(LocalTime.now(zoneId))/' app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt

