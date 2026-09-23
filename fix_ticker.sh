# In DashboardScreen.kt
sed -i 's/var currentTime by remember/val currentTimeState = remember/' app/src/main/java/com/example/ui/screens/DashboardScreen.kt
sed -i 's/currentTime = now/currentTimeState.value = now/' app/src/main/java/com/example/ui/screens/DashboardScreen.kt
sed -i 's/currentTime = currentTime,/currentTime = currentTimeState.value,/' app/src/main/java/com/example/ui/screens/DashboardScreen.kt

# In DailyHighlightsTab.kt
sed -i 's/var currentTime by remember/val currentTimeState = remember/' app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt
sed -i 's/currentTime = now/currentTimeState.value = now/' app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt
sed -i 's/VedicTimeCalculator.getVedicGreeting(currentTime)/VedicTimeCalculator.getVedicGreeting(currentTimeState.value)/' app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt
sed -i 's/currentTime = currentTime,/currentTime = currentTimeState.value,/' app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt

