with open("app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt", "r") as f:
    content = f.read()

import re

orig = """        val profileWithoutScore = DailyVedicProfile(
            currentZonedDateTime = now,
            panchanga = panchanga,
            currentChoghadiya = activeChoghadiya,
            currentHora = activeHora,
            currentVedicPeriod = "${period.displayName} (${period.devanagari})",
            rahuKalam = rahu,
            yamaganda = yama,
            gulikaKalam = gulika,
            abhijitMuhurta = abhijit,
            brahmaMuhurta = brahma,
            moonPhaseProgress = moonPhaseProgress
        )"""

new = """        val festivals = hinduFestivalEngine.calculateFestivals(panchanga, now)
        val vrats = vratEngine.calculateVrats(panchanga, now)
        val pradosha = muhurtas.first { it.name.startsWith("Pradosha") }
        val pujas = pujaTimingEngine.getDailyPujaRecommendations(panchanga, brahma, pradosha)

        val profileWithoutScore = DailyVedicProfile(
            currentZonedDateTime = now,
            panchanga = panchanga,
            currentChoghadiya = activeChoghadiya,
            currentHora = activeHora,
            currentVedicPeriod = "${period.displayName} (${period.devanagari})",
            rahuKalam = rahu,
            yamaganda = yama,
            gulikaKalam = gulika,
            abhijitMuhurta = abhijit,
            brahmaMuhurta = brahma,
            moonPhaseProgress = moonPhaseProgress,
            festivals = festivals,
            vrats = vrats,
            pujaRecommendations = pujas
        )"""
        
content = content.replace(orig, new)

with open("app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt", "w") as f:
    f.write(content)
