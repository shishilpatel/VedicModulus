# 1. DashboardScreen.kt
sed -i 's/Dispatchers.Default/Dispatchers.IO/g' app/src/main/java/com/example/ui/screens/DashboardScreen.kt

# 2. OnboardingScreen.kt
sed -i 's/Dispatchers.Default/Dispatchers.IO/g' app/src/main/java/com/example/ui/screens/OnboardingScreen.kt

# 3. UserProfileSettingsScreen.kt
sed -i 's/Dispatchers.Default/Dispatchers.IO/g' app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt

# 4. KundaliTab.kt
sed -i 's/Dispatchers.Default/Dispatchers.IO/g' app/src/main/java/com/example/ui/screens/tabs/KundaliTab.kt

# 5. CompatibilityMatchTab.kt
sed -i 's/Dispatchers.Default/Dispatchers.IO/g' app/src/main/java/com/example/ui/screens/tabs/CompatibilityMatchTab.kt

# 6. VedicCalendarTab.kt
sed -i 's/Dispatchers.Default/Dispatchers.IO/g' app/src/main/java/com/example/ui/screens/tabs/VedicCalendarTab.kt

