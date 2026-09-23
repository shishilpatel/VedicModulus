import re

with open("app/src/main/java/com/example/ui/screens/StartupRewardedAdScreen.kt", "r") as f:
    content = f.read()

launch_old = """    // Load and auto-start 30-second ad playback on startup
    LaunchedEffect(Unit) {
        if (adState == AdState.IDLE) {
            adManager.loadAd(scope) {
                adManager.startPlayback(scope)
            }
        } else if (adState == AdState.LOADED) {
            adManager.startPlayback(scope)
        }
    }"""

launch_new = """    var hasUserOptedInToAd by remember { mutableStateOf(false) }

    // Load ad on startup, but do not auto-start playback
    LaunchedEffect(Unit) {
        if (adState == AdState.IDLE) {
            adManager.loadAd(scope) {}
        }
    }"""

content = content.replace(launch_old, launch_new)

header_old = """            // 1. Top Linear Countdown Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .testTag("ad_progress_bar"),
                color = if (isRewardEarned) Color(0xFF10B981) else Color(0xFFFFB703),
                trackColor = Color(0x33334155)
            )"""

header_new = """            // 1. Top Linear Countdown Progress Bar
            if (hasUserOptedInToAd) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .testTag("ad_progress_bar"),
                    color = if (isRewardEarned) Color(0xFF10B981) else Color(0xFFFFB703),
                    trackColor = Color(0x33334155)
                )
            }"""
            
content = content.replace(header_old, header_new)

with open("app/src/main/java/com/example/ui/screens/StartupRewardedAdScreen.kt", "w") as f:
    f.write(content)
