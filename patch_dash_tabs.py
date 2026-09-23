with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

orig_titles = """    val tabTitles = listOf(
        "Daily Highlights",
        "Watch Face",
        "Kundali Chart",
        "Vastu Compass",
        "Panchanga",
        "Choghadiya & Hora",
        "Muhurtas",
        "Numerology"
    )"""

new_titles = """    val tabTitles = listOf(
        "Daily Highlights",
        "Vedic Calendar",
        "Vedic AI Assistant",
        "Watch Face",
        "Kundali Chart",
        "Vastu Compass",
        "Panchanga",
        "Choghadiya & Hora",
        "Muhurtas",
        "Numerology"
    )"""

orig_when = """                when (selectedTab) {
                    0 -> DailyHighlightsTab(
                        dailyProfile = dailyResult,
                        personalizedProfile = personalizedResult
                    )
                    1 -> WatchFaceTab(
                        config = watchConfig,
                        personalizedProfile = personalizedResult,
                        dailyProfile = dailyResult,
                        currentTime = currentTime,
                        isAmbientMode = isAmbientMode,
                        onAmbientToggle = { isAmbientMode = !isAmbientMode },
                        onStyleSelect = { style ->
                            activeStyle = style
                            onStyleChanged(style)
                        },
                        onSyncToWatch = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vedic configuration synced to Samsung Galaxy Watch!")
                            }
                        }
                    )
                    2 -> KundaliTab(
                        profile = profile,
                        personalizedProfile = personalizedResult
                    )
                    3 -> VastuCompassTab(
                        personalizedProfile = personalizedResult
                    )
                    4 -> PanchangaTab(
                        dailyProfile = dailyResult,
                        personalizedProfile = personalizedResult,
                        selectedDateOffset = selectedDateOffset,
                        onDateOffsetChange = { selectedDateOffset = it }
                    )
                    5 -> ChoghadiyaHoraTab(
                        dailyProfile = dailyResult
                    )
                    6 -> MuhurtasTab(
                        dailyProfile = dailyResult
                    )
                    7 -> NumerologyTab(
                        personalizedProfile = personalizedResult
                    )
                }"""

new_when = """                when (selectedTab) {
                    0 -> DailyHighlightsTab(
                        dailyProfile = dailyResult,
                        personalizedProfile = personalizedResult
                    )
                    1 -> VedicCalendarTab(
                        profile = profile,
                        personalizedProfile = personalizedResult,
                        personalizationEngine = personalizationEngine
                    )
                    2 -> VedicAITab()
                    3 -> WatchFaceTab(
                        config = watchConfig,
                        personalizedProfile = personalizedResult,
                        dailyProfile = dailyResult,
                        currentTime = currentTime,
                        isAmbientMode = isAmbientMode,
                        onAmbientToggle = { isAmbientMode = !isAmbientMode },
                        onStyleSelect = { style ->
                            activeStyle = style
                            onStyleChanged(style)
                        },
                        onSyncToWatch = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vedic configuration synced to Samsung Galaxy Watch!")
                            }
                        }
                    )
                    4 -> KundaliTab(
                        profile = profile,
                        personalizedProfile = personalizedResult
                    )
                    5 -> VastuCompassTab(
                        personalizedProfile = personalizedResult
                    )
                    6 -> PanchangaTab(
                        dailyProfile = dailyResult,
                        personalizedProfile = personalizedResult,
                        selectedDateOffset = selectedDateOffset,
                        onDateOffsetChange = { selectedDateOffset = it }
                    )
                    7 -> ChoghadiyaHoraTab(
                        dailyProfile = dailyResult
                    )
                    8 -> MuhurtasTab(
                        dailyProfile = dailyResult
                    )
                    9 -> NumerologyTab(
                        personalizedProfile = personalizedResult
                    )
                }"""

content = content.replace(orig_titles, new_titles)
content = content.replace(orig_when, new_when)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
