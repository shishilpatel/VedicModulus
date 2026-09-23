with open("app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt", "r") as f:
    content = f.read()

orig = """        val ascReport = ascendantReportEngine.generateReport(natalChart.lagnaRashi)
        
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
        )"""

new = """        val ascReport = ascendantReportEngine.generateReport(natalChart.lagnaRashi)
        
        val zoneId = try { java.time.ZoneId.of(birthProfile.birthLocation.timezoneId) } catch (e: Exception) { java.time.ZoneId.systemDefault() }
        val birthZdt = ZonedDateTime.of(birthProfile.birthDate, birthProfile.birthTime, zoneId)

        // Calculate moon longitude manually or get it from chart. Wait, natalChart doesn't expose raw longitudes directly.
        // We'll calculate it using astronomicalEngine for the birth time.
        val moonPos = astronomicalEngine.moonPosition(birthZdt)
        
        // Ensure ayanamsha offset is applied to make it sidereal
        val siderealMoonPos = moonPos.tropicalLongitudeDeg - birthProfile.ayanamsha.offsetDegreesAtJ2000
        val adjustedMoonLong = (siderealMoonPos % 360.0 + 360.0) % 360.0
        
        val dashaPeriods = dashaEngine.calculateVimshottariDasha(
            birthDate = birthZdt,
            moonLongitude = adjustedMoonLong,
            currentDate = ZonedDateTime.now()
        )"""

content = content.replace(orig, new)

with open("app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt", "w") as f:
    f.write(content)
