with open("app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt", "r") as f:
    content = f.read()

import re

imports = """import com.example.engines.reports.AscendantReportEngine
import com.example.engines.reports.DashaEngine"""

content = content.replace("import com.example.engines.spiritual.SpiritualDesignEngine\nimport com.example.engines.timing.DailyTimingEngine", "import com.example.engines.spiritual.SpiritualDesignEngine\nimport com.example.engines.timing.DailyTimingEngine\n" + imports)

# Update constructor
orig_cons = "private val pujaTimingEngine: PujaTimingEngine = PujaTimingEngine()"
new_cons = """private val pujaTimingEngine: PujaTimingEngine = PujaTimingEngine(),
    private val ascendantReportEngine: AscendantReportEngine = AscendantReportEngine(),
    private val dashaEngine: DashaEngine = DashaEngine()"""
    
content = content.replace(orig_cons, new_cons)

# Update createPersonalizedProfile
orig_ret = """        return PersonalizedProfile(
            userBirthProfile = birthProfile,
            natalLagnaRashi = natalChart.lagnaRashi,
            natalSuryaRashi = natalChart.suryaRashi,
            natalChandraRashi = natalChart.chandraRashi,
            natalNakshatra = natalChart.janmaNakshatra,
            natalNakshatraPada = natalChart.janmaNakshatraPada,
            numerology = numerology,
            dominantVastuElement = dominantElement,
            favorableColorsHex = colors
        )"""

new_ret = """        val ascReport = ascendantReportEngine.generateReport(natalChart.lagnaRashi)
        
        // Calculate moon longitude manually or get it from chart. Wait, natalChart doesn't expose raw longitudes directly.
        // We'll calculate it using astronomicalEngine for the birth time.
        val moonPos = astronomicalEngine.moonPosition(birthProfile.birthDate)
        
        // Ensure ayanamsha offset is applied to make it sidereal
        val siderealMoonPos = moonPos.tropicalLongitudeDeg - birthProfile.ayanamsha.offsetDegreesAtJ2000
        val adjustedMoonLong = (siderealMoonPos % 360.0 + 360.0) % 360.0
        
        val dashaPeriods = dashaEngine.calculateVimshottariDasha(
            birthDate = birthProfile.birthDate,
            moonLongitude = adjustedMoonLong,
            currentDate = ZonedDateTime.now()
        )

        return PersonalizedProfile(
            userBirthProfile = birthProfile,
            natalLagnaRashi = natalChart.lagnaRashi,
            natalSuryaRashi = natalChart.suryaRashi,
            natalChandraRashi = natalChart.chandraRashi,
            natalNakshatra = natalChart.janmaNakshatra,
            natalNakshatraPada = natalChart.janmaNakshatraPada,
            numerology = numerology,
            dominantVastuElement = dominantElement,
            favorableColorsHex = colors,
            ascendantReport = ascReport,
            dashaPeriods = dashaPeriods
        )"""

content = content.replace(orig_ret, new_ret)

with open("app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt", "w") as f:
    f.write(content)
