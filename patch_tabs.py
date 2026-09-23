with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

orig_when = """                when (selectedTab) {
                    0 -> WatchFaceTab(
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
                    1 -> KundaliTab(
                        profile = profile,
                        personalizedProfile = personalizedResult
                    )
                    2 -> VastuCompassTab(
                        personalizedProfile = personalizedResult
                    )
                    3 -> PanchangaTab(
                        dailyProfile = dailyResult,
                        personalizedProfile = personalizedResult,
                        selectedDateOffset = selectedDateOffset,
                        onDateOffsetChange = { selectedDateOffset = it }
                    )
                    4 -> ChoghadiyaHoraTab(
                        dailyProfile = dailyResult
                    )
                    5 -> MuhurtasTab(
                        dailyProfile = dailyResult
                    )
                    6 -> NumerologyTab(
                        personalizedProfile = personalizedResult
                    )
                }"""

new_when = """                when (selectedTab) {
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

content = content.replace(orig_when, new_when)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
