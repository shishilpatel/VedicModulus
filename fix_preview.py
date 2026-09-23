import re

with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "r") as f:
    content = f.read()

bad_preview = """    val livePreview = remember(draftProfile) {
        personalizationEngine.createPersonalizedProfile(draftProfile)
    }"""

good_preview = """    var livePreview by remember { mutableStateOf<PersonalizedProfile?>(null) }
    LaunchedEffect(draftProfile) {
        kotlinx.coroutines.delay(500L)
        livePreview = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            personalizationEngine.createPersonalizedProfile(draftProfile)
        }
    }"""

content = content.replace(bad_preview, good_preview)

# Now we need to handle livePreview being nullable.
content = content.replace("livePreview.natalLagnaRashi.sanskritName", "livePreview?.natalLagnaRashi?.sanskritName ?: \"...\"")
content = content.replace("livePreview.natalLagnaRashi.symbol", "livePreview?.natalLagnaRashi?.symbol ?: \"\"")
content = content.replace("livePreview.natalChandraRashi.sanskritName", "livePreview?.natalChandraRashi?.sanskritName ?: \"...\"")
content = content.replace("livePreview.natalChandraRashi.symbol", "livePreview?.natalChandraRashi?.symbol ?: \"\"")
content = content.replace("livePreview.natalNakshatra.sanskritName", "livePreview?.natalNakshatra?.sanskritName ?: \"...\"")
content = content.replace("livePreview.natalNakshatraPada", "livePreview?.natalNakshatraPada ?: 1")
content = content.replace("livePreview.numerology.birthNumber", "livePreview?.numerology?.birthNumber ?: 1")
content = content.replace("livePreview.numerology.lifePathNumber", "livePreview?.numerology?.lifePathNumber ?: 1")
content = content.replace("livePreview.numerology.rulingPlanet.sanskritName", "livePreview?.numerology?.rulingPlanet?.sanskritName ?: \"...\"")


with open("app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt", "w") as f:
    f.write(content)
