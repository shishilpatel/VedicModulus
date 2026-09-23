package com.example.engines.personalization

import com.example.core.model.*
import com.example.engines.astronomy.AstronomicalEngine
import com.example.engines.astronomy.AstronomicalEngineProvider
import com.example.engines.choghadiya.ChoghadiyaEngine
import com.example.engines.hora.HoraEngine
import com.example.engines.jyotisha.JyotishaEngine
import com.example.engines.muhurta.MuhurtaEngine
import com.example.engines.numerology.NumerologyEngine
import com.example.engines.panchanga.PanchangaEngine
import com.example.engines.spiritual.SpiritualDesignEngine
import com.example.engines.timing.DailyTimingEngine
import com.example.engines.reports.AscendantReportEngine
import com.example.engines.reports.DashaEngine
import com.example.engines.vastu.VastuEngine
import com.example.engines.vedictime.VedicTimeEngine
import com.example.engines.score.CosmicScoreEngine
import com.example.engines.festival.HinduFestivalEngine
import com.example.engines.festival.VratEngine
import com.example.engines.festival.PujaTimingEngine
import com.example.engines.festival.Festival
import com.example.engines.festival.Vrat
import com.example.engines.festival.PujaTimingRecommendation
import java.time.ZonedDateTime

class VedicPersonalizationEngine(
    private val astronomicalEngine: AstronomicalEngine = AstronomicalEngineProvider.instance,
    private val panchangaEngine: PanchangaEngine = PanchangaEngine(astronomicalEngine),
    private val jyotishaEngine: JyotishaEngine = JyotishaEngine(astronomicalEngine),
    private val vedicTimeEngine: VedicTimeEngine = VedicTimeEngine(),
    private val choghadiyaEngine: ChoghadiyaEngine = ChoghadiyaEngine(),
    private val horaEngine: HoraEngine = HoraEngine(),
    private val dailyTimingEngine: DailyTimingEngine = DailyTimingEngine(),
    private val muhurtaEngine: MuhurtaEngine = MuhurtaEngine(),
    private val numerologyEngine: NumerologyEngine = NumerologyEngine(),
    private val vastuEngine: VastuEngine = VastuEngine(),
    private val spiritualDesignEngine: SpiritualDesignEngine = SpiritualDesignEngine(),
    private val cosmicScoreEngine: CosmicScoreEngine = CosmicScoreEngine(),
    private val hinduFestivalEngine: HinduFestivalEngine = HinduFestivalEngine(),
    private val vratEngine: VratEngine = VratEngine(),
    private val pujaTimingEngine: PujaTimingEngine = PujaTimingEngine(),
    private val ascendantReportEngine: AscendantReportEngine = AscendantReportEngine(),
    private val dashaEngine: DashaEngine = DashaEngine()
) {

    /**
     * Synthesizes user birth input into a deterministic PersonalizedProfile.
     */
    fun createPersonalizedProfile(birthProfile: BirthProfile): PersonalizedProfile {
        // 1. Jyotisha Natal Chart
        val natalChart = jyotishaEngine.calculateNatalChart(
            birthDate = birthProfile.birthDate,
            birthTime = birthProfile.birthTime,
            location = birthProfile.birthLocation,
            ayanamsha = birthProfile.ayanamsha
        )

        // 2. Numerology Profile
        val numerology = numerologyEngine.calculateNumerology(
            birthDate = birthProfile.birthDate,
            fullName = birthProfile.fullName,
            system = birthProfile.numerologySystem
        )

        // 3. Vastu Element resonance
        val (dominantElement, _) = vastuEngine.deriveElementalAffinity(natalChart.chandraRashi)

        // 4. Color palette mapping based on Mulank and Rashi
        val colors = when (numerology.birthNumber) {
            1 -> listOf(0xFFFFB703, 0xFFFB8500, 0xFF023047) // Gold, Orange, Deep Blue
            2 -> listOf(0xFFE2E8F0, 0xFF94A3B8, 0xFF0F172A) // Silver, Pearl, Midnight
            3 -> listOf(0xFFFFD166, 0xFFEF476F, 0xFF118AB2) // Jupiter Saffron, Rose, Azure
            4 -> listOf(0xFF38BDF8, 0xFF818CF8, 0xFF030712) // Electric Cyan, Indigo, Void
            5 -> listOf(0xFF10B981, 0xFF34D399, 0xFF064E3B) // Emerald, Mint, Forest
            6 -> listOf(0xFFF472B6, 0xFFFDE047, 0xFF1E1B4B) // Venus Rose, Pale Gold, Night
            7 -> listOf(0xFF2DD4BF, 0xFFA7F3D0, 0xFF134E4A) // Sea Green, Turquoise, Mist
            8 -> listOf(0xFF6366F1, 0xFF475569, 0xFF09090B) // Cobalt, Slate, True Black
            else -> listOf(0xFFEF4444, 0xFFF97316, 0xFF450A0A) // Mars Coral, Ember, Obsidian
        }

        val ascReport = ascendantReportEngine.generateReport(natalChart.lagnaRashi)
        
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
        )
    }

    /**
     * Synthesizes dynamic real-time astronomical events into DailyVedicProfile.
     */
    fun createDailyVedicProfile(
        now: ZonedDateTime,
        location: LocationData,
        ayanamsha: AyanamshaSystem = AyanamshaSystem.LAHIRI,
        tradition: CalendarTradition = CalendarTradition.AMANTA,
        personalizedProfile: PersonalizedProfile? = null
    ): DailyVedicProfile {
        // 1. Calculate Astronomical Solar Events
        val solarTimes = astronomicalEngine.calculateSolarTimes(now, location.latitude, location.longitude)
        val nextDay = now.plusDays(1)
        val nextDaySolar = astronomicalEngine.calculateSolarTimes(nextDay, location.latitude, location.longitude)

        // 2. Complete Panchanga
        val panchanga = panchangaEngine.calculatePanchanga(
            dateTime = now,
            latitude = location.latitude,
            longitude = location.longitude,
            ayanamsha = ayanamsha,
            tradition = tradition
        )

        // 3. Choghadiya Schedule & Active slot
        val choghadiyaSlots = choghadiyaEngine.calculateDailyChoghadiya(
            solarTimes = solarTimes,
            nextDaySunrise = nextDaySolar.sunrise,
            dayOfWeek = now.dayOfWeek.value
        )
        val activeChoghadiya = choghadiyaEngine.findActiveChoghadiya(choghadiyaSlots, now)

        // 4. Hora Schedule & Active slot
        val horaSlots = horaEngine.calculateDailyHoras(
            solarTimes = solarTimes,
            nextDaySunrise = nextDaySolar.sunrise,
            dayOfWeek = now.dayOfWeek.value
        )
        val activeHora = horaEngine.findActiveHora(horaSlots, now)

        // 5. Vedic Time Period
        val period = vedicTimeEngine.getCurrentVedicPeriod(now, solarTimes)

        // 6. Rahu Kalam, Yamaganda, Gulika
        val (rahu, yama, gulika) = dailyTimingEngine.calculateInauspiciousWindows(
            solarTimes = solarTimes,
            dayOfWeek = now.dayOfWeek.value
        )

        // 7. Muhurtas
        val muhurtas = muhurtaEngine.calculateMuhurtas(
            solarTimes = solarTimes,
            nextDaySunrise = nextDaySolar.sunrise,
            dayOfWeek = now.dayOfWeek.value
        )
        val defaultMuhurta = TimingWindow(
            name = "Brahma Muhurta",
            devanagari = "ब्रह्म मुहूर्त",
            startTime = solarTimes.sunrise.minusMinutes(96),
            endTime = solarTimes.sunrise.minusMinutes(48),
            isAuspicious = true,
            deity = null
        )
        val brahma = muhurtas.firstOrNull { it.name.startsWith("Brahma") } ?: defaultMuhurta
        val abhijit = muhurtas.firstOrNull { it.name.startsWith("Abhijit") }

        // 8. Moon Phase Progress (0.0 to 1.0 to 0.0)
        val sunCoord = astronomicalEngine.sunPosition(now).tropicalLongitudeDeg
        val moonCoord = astronomicalEngine.moonPosition(now).tropicalLongitudeDeg
        var angleDiff = ((moonCoord - sunCoord) % 360.0 + 360.0) % 360.0
        val moonPhaseProgress = (angleDiff / 360.0).toFloat()

        val festivals = hinduFestivalEngine.calculateFestivals(panchanga, now)
        val vrats = vratEngine.calculateVrats(panchanga, now)
        val pradoshaDefault = TimingWindow(
            name = "Pradosha",
            devanagari = "प्रदोष काल",
            startTime = solarTimes.sunset,
            endTime = solarTimes.sunset.plusMinutes(96),
            isAuspicious = true,
            deity = null
        )
        val pradosha = muhurtas.firstOrNull { it.name.startsWith("Pradosha") } ?: pradoshaDefault
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
        )
        
        val score = cosmicScoreEngine.calculateScore(personalizedProfile, profileWithoutScore)
        return profileWithoutScore.copy(cosmicScore = score)
    }

    /**
     * Generates a concrete WatchFaceConfiguration from user's personalized profile and preferences.
     */
    fun generateWatchFaceConfig(
        personalizedProfile: PersonalizedProfile,
        style: WatchFaceStyle = personalizedProfile.userBirthProfile.preferredStyle
    ): WatchFaceConfiguration {
        val colors = personalizedProfile.favorableColorsHex
        val primaryColor = colors.getOrElse(0) { 0xFFFFD166 }
        val accentColor = colors.getOrElse(1) { 0xFF06D6A0 }
        val surfaceColor = 0xFF000000 // True AMOLED Black

        return WatchFaceConfiguration(
            style = style,
            primaryColorHex = primaryColor,
            accentColorHex = accentColor,
            surfaceColorHex = surfaceColor,
            showDevanagari = personalizedProfile.userBirthProfile.language != LanguageDisplay.ENGLISH,
            showChoghadiyaSubdial = true,
            showHoraBadge = true,
            showRahuRing = true,
            showMoonPhaseRing = true,
            showSacredGeometry = (style == WatchFaceStyle.YANTRA || style == WatchFaceStyle.CHAKRA || style == WatchFaceStyle.COSMIC),
            personalizedBirthNumber = personalizedProfile.numerology.birthNumber,
            zodiacSymbol = personalizedProfile.natalChandraRashi.symbol,
            nakshatraName = personalizedProfile.natalNakshatra.sanskritName
        )
    }
}
