import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# 1. Update NavCategory enum
old_nav_enum = """enum class NavCategory(val title: String, val icon: ImageVector, val tabs: List<DashboardTab>) {
    DASHBOARD("Dashboard", Icons.Default.Home, listOf(DashboardTab.DAILY_HIGHLIGHTS, DashboardTab.CHAKRA_MEDITATION, DashboardTab.PANCHANGA, DashboardTab.MUHURTAS)),
    COSMIC_TOOLS("Cosmic Tools", Icons.Default.Explore, listOf(DashboardTab.VASTU_COMPASS, DashboardTab.CHOGHADIYA_HORA, DashboardTab.VEDIC_CALENDAR, DashboardTab.WATCH_FACE)),
    PERSONALIZED_REPORTS("Personalized Reports", Icons.Default.Assessment, listOf(DashboardTab.KUNDALI_CHART, DashboardTab.NUMEROLOGY, DashboardTab.LIFE_PREDICTIONS, DashboardTab.PAST_READINGS, DashboardTab.VEDIC_AI_ASSISTANT, DashboardTab.COMPATIBILITY_CHECKER)),
    SETTINGS("Settings", Icons.Default.Settings, listOf(DashboardTab.USER_PROFILE_SETTINGS, DashboardTab.PROFILES_AND_TRAVEL))
}"""

new_nav_enum = """enum class NavCategory(val title: String, val icon: ImageVector, val tabs: List<DashboardTab>) {
    DASHBOARD("Dashboard", Icons.Default.Home, listOf(DashboardTab.DAILY_HIGHLIGHTS, DashboardTab.CHAKRA_MEDITATION, DashboardTab.PANCHANGA, DashboardTab.MUHURTAS)),
    COSMIC_TOOLS("Cosmic Tools", Icons.Default.Explore, listOf(DashboardTab.VASTU_COMPASS, DashboardTab.CHOGHADIYA_HORA, DashboardTab.VEDIC_CALENDAR)),
    WATCH_FACES("Watch Face", Icons.Default.Watch, listOf(DashboardTab.WATCH_FACE)),
    PERSONALIZED_REPORTS("Personalized Reports", Icons.Default.Assessment, listOf(DashboardTab.KUNDALI_CHART, DashboardTab.NUMEROLOGY, DashboardTab.LIFE_PREDICTIONS, DashboardTab.PAST_READINGS, DashboardTab.VEDIC_AI_ASSISTANT, DashboardTab.COMPATIBILITY_CHECKER)),
    SETTINGS("Settings", Icons.Default.Settings, listOf(DashboardTab.USER_PROFILE_SETTINGS, DashboardTab.PROFILES_AND_TRAVEL))
}"""
content = content.replace(old_nav_enum, new_nav_enum)

# 2. Add Icons.Default.Watch import if missing
if "import androidx.compose.material.icons.filled.Watch" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Settings", "import androidx.compose.material.icons.filled.Settings\nimport androidx.compose.material.icons.filled.Watch")

# 3. Update getLocalizedTitle
old_loc = """        NavCategory.COSMIC_TOOLS -> stringResource(R.string.cat_cosmic_tools)
        NavCategory.PERSONALIZED_REPORTS -> stringResource(R.string.cat_personalized_reports)"""
new_loc = """        NavCategory.COSMIC_TOOLS -> stringResource(R.string.cat_cosmic_tools)
        NavCategory.WATCH_FACES -> stringResource(R.string.tab_watch_face)
        NavCategory.PERSONALIZED_REPORTS -> stringResource(R.string.cat_personalized_reports)"""
content = content.replace(old_loc, new_loc)

# 4. Update getTabs
old_get_tabs = """fun NavCategory.getTabs(hasSmartWatch: Boolean): List<DashboardTab> {
    return when (this) {
        NavCategory.COSMIC_TOOLS -> {
            if (hasSmartWatch) {
                listOf(DashboardTab.VASTU_COMPASS, DashboardTab.CHOGHADIYA_HORA, DashboardTab.VEDIC_CALENDAR, DashboardTab.WATCH_FACE)
            } else {
                listOf(DashboardTab.VASTU_COMPASS, DashboardTab.CHOGHADIYA_HORA, DashboardTab.VEDIC_CALENDAR)
            }
        }
        else -> this.tabs
    }
}"""
new_get_tabs = """fun NavCategory.getTabs(hasSmartWatch: Boolean): List<DashboardTab> {
    return this.tabs
}"""
content = content.replace(old_get_tabs, new_get_tabs)

# 5. Update NavigationBar visible categories
old_nav_bar = """            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavCategory.values().forEach { category ->"""
new_nav_bar = """            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                val visibleCategories = if (profile.hasSmartWatch) {
                    NavCategory.values().toList()
                } else {
                    NavCategory.values().filter { it != NavCategory.WATCH_FACES }
                }
                visibleCategories.forEach { category ->"""
content = content.replace(old_nav_bar, new_nav_bar)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)

