package com.example

import com.example.core.model.*
import com.example.data.CityRepository
import com.example.engines.astronomy.MeeusAstronomicalEngine
import com.example.engines.choghadiya.ChoghadiyaEngine
import com.example.engines.hora.HoraEngine
import com.example.engines.numerology.NumerologyEngine
import com.example.engines.panchanga.PanchangaEngine
import com.example.engines.personalization.VedicPersonalizationEngine
import com.example.engines.timing.DailyTimingEngine
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class VedicEnginesUnitTest {

    private val astroEngine = MeeusAstronomicalEngine()
    private val panchangaEngine = PanchangaEngine(astroEngine)
    private val choghadiyaEngine = ChoghadiyaEngine()
    private val horaEngine = HoraEngine()
    private val timingEngine = DailyTimingEngine()
    private val numerologyEngine = NumerologyEngine()
    private val personalizationEngine = VedicPersonalizationEngine()

    @Test
    fun testAstronomicalCalculations() {
        val dt = ZonedDateTime.of(2026, 9, 13, 12, 0, 0, 0, ZoneId.of("Asia/Kolkata"))
        val ayanamsha = astroEngine.calculateLahiriAyanamsha(dt)
        assertTrue("Lahiri Ayanamsha should be around 24 degrees in 2026", ayanamsha in 23.5..24.5)

        val sun = astroEngine.sunPosition(dt)
        assertTrue("Sun longitude in range 0..360", sun.tropicalLongitudeDeg in 0.0..360.0)

        val moon = astroEngine.moonPosition(dt)
        assertTrue("Moon longitude in range 0..360", moon.tropicalLongitudeDeg in 0.0..360.0)

        val city = CityRepository.getDefaultCity() // Ahmedabad
        val solarTimes = astroEngine.calculateSolarTimes(dt, city.latitude, city.longitude)
        assertTrue("Sunrise before Sunset", solarTimes.sunrise.isBefore(solarTimes.sunset))
        assertTrue("Solar Noon between Sunrise and Sunset",
            solarTimes.solarNoon.isAfter(solarTimes.sunrise) && solarTimes.solarNoon.isBefore(solarTimes.sunset))
    }

    @Test
    fun testPanchangaCalculations() {
        val dt = ZonedDateTime.of(2026, 9, 13, 7, 0, 0, 0, ZoneId.of("Asia/Kolkata"))
        val city = CityRepository.getDefaultCity()
        val panchanga = panchangaEngine.calculatePanchanga(dt, city.latitude, city.longitude)

        assertTrue("Tithi number between 1 and 30", panchanga.tithi.number in 1..30)
        assertTrue("Nakshatra number between 1 and 27", panchanga.nakshatra.number in 1..27)
        assertTrue("Nakshatra pada between 1 and 4", panchanga.nakshatraPada in 1..4)
        assertNotNull("Yoga not null", panchanga.yoga)
        assertNotNull("Karana not null", panchanga.karana)
        assertNotNull("Vara not null", panchanga.vara)
    }

    @Test
    fun testChoghadiyaDailySchedule() {
        val dt = ZonedDateTime.of(2026, 9, 13, 10, 0, 0, 0, ZoneId.of("Asia/Kolkata"))
        val city = CityRepository.getDefaultCity()
        val solarTimes = astroEngine.calculateSolarTimes(dt, city.latitude, city.longitude)
        val nextSolarTimes = astroEngine.calculateSolarTimes(dt.plusDays(1), city.latitude, city.longitude)

        val slots = choghadiyaEngine.calculateDailyChoghadiya(solarTimes, nextSolarTimes.sunrise, dt.dayOfWeek.value)
        assertEquals("Total 16 Choghadiya intervals (8 day + 8 night)", 16, slots.size)

        val active = choghadiyaEngine.findActiveChoghadiya(slots, dt)
        assertNotNull("Active Choghadiya must exist", active)
    }

    @Test
    fun testHoraDailySchedule() {
        val dt = ZonedDateTime.of(2026, 9, 13, 10, 0, 0, 0, ZoneId.of("Asia/Kolkata"))
        val city = CityRepository.getDefaultCity()
        val solarTimes = astroEngine.calculateSolarTimes(dt, city.latitude, city.longitude)
        val nextSolarTimes = astroEngine.calculateSolarTimes(dt.plusDays(1), city.latitude, city.longitude)

        val horas = horaEngine.calculateDailyHoras(solarTimes, nextSolarTimes.sunrise, dt.dayOfWeek.value)
        assertEquals("24 Horas per day", 24, horas.size)

        val active = horaEngine.findActiveHora(horas, dt)
        assertNotNull("Active Hora must exist", active)
    }

    @Test
    fun testNumerologyCalculations() {
        // Date: 15 August 1947 -> Mulank = 1+5 = 6. Bhagyank = 1+5+8+1+9+4+7 = 35 -> 3+5 = 8.
        val birthDate = LocalDate.of(1947, 8, 15)
        val result = numerologyEngine.calculateNumerology(birthDate, "India", NumerologySystem.CHALDEAN)

        assertEquals("Mulank for 15th is 6", 6, result.birthNumber)
        assertEquals("Bhagyank for 15 Aug 1947 is 8", 8, result.lifePathNumber)
        assertTrue("Namank in 1..9", result.destinyNameNumber in 1..9)
        assertTrue("Favorable numbers not empty", result.favorableNumbers.isNotEmpty())
    }

    @Test
    fun testPersonalizationEngineDeterminism() {
        val profile = BirthProfile(
            fullName = "Aryabhata",
            birthDate = LocalDate.of(1995, 5, 20),
            birthTime = LocalTime.of(6, 30),
            birthLocation = CityRepository.getDefaultCity(),
            ayanamsha = AyanamshaSystem.LAHIRI,
            calendarTradition = CalendarTradition.AMANTA,
            numerologySystem = NumerologySystem.CHALDEAN,
            language = LanguageDisplay.ENGLISH,
            preferredStyle = WatchFaceStyle.COSMIC
        )

        val p1 = personalizationEngine.createPersonalizedProfile(profile)
        val p2 = personalizationEngine.createPersonalizedProfile(profile)

        assertEquals("Lagna Rashi must be deterministic", p1.natalLagnaRashi, p2.natalLagnaRashi)
        assertEquals("Surya Rashi must be deterministic", p1.natalSuryaRashi, p2.natalSuryaRashi)
        assertEquals("Chandra Rashi must be deterministic", p1.natalChandraRashi, p2.natalChandraRashi)
        assertEquals("Mulank must be deterministic", p1.numerology.birthNumber, p2.numerology.birthNumber)

        val config = personalizationEngine.generateWatchFaceConfig(p1, WatchFaceStyle.YANTRA)
        assertEquals("Watch style matches request", WatchFaceStyle.YANTRA, config.style)
    }

    @Test
    fun testTarabalaAndChandrabalaCalculation() {
        val tarabalaEngine = com.example.engines.jyotisha.TarabalaEngine()

        // Same nakshatra = Janma (Tara 1)
        val taraJanma = tarabalaEngine.calculateTarabala(Nakshatra.ASHWINI, Nakshatra.ASHWINI)
        assertEquals("Same Nakshatra is Janma Tara", TaraType.JANMA, taraJanma)
        assertFalse("Janma Tara is not auspicious for major worldly contracts", taraJanma.isAuspicious)

        // Ashwini to Bharani = Sampat (Tara 2, Auspicious)
        val taraSampat = tarabalaEngine.calculateTarabala(Nakshatra.ASHWINI, Nakshatra.BHARANI)
        assertEquals("Second Nakshatra is Sampat Tara", TaraType.SAMPAT, taraSampat)
        assertTrue("Sampat Tara is auspicious for wealth", taraSampat.isAuspicious)

        // Chandrabala: Mesha to Mesha (House 1) -> Favorable
        val (house1, isFav1) = tarabalaEngine.calculateChandrabala(Rashi.MESHA, Rashi.MESHA)
        assertEquals("House 1 from Moon", 1, house1)
        assertTrue("House 1 is favorable", isFav1)

        // Chandrabala: Mesha to Vrishabha (House 2) -> Sensitive
        val (house2, isFav2) = tarabalaEngine.calculateChandrabala(Rashi.MESHA, Rashi.VRISHABHA)
        assertEquals("House 2 from Moon", 2, house2)
        assertFalse("House 2 is sensitive", isFav2)

        val harmony = tarabalaEngine.calculateDailyHarmony(
            natalNakshatra = Nakshatra.ASHWINI,
            natalChandraRashi = Rashi.MESHA,
            transitNakshatra = Nakshatra.BHARANI,
            transitChandraRashi = Rashi.MESHA
        )
        assertTrue("Overall score between 15 and 98", harmony.overallScorePercent in 15..98)
        assertNotNull("Advice must be populated", harmony.advice)
    }

    @Test
    fun testKundaliChartGeneration() {
        val jyotishaEngine = com.example.engines.jyotisha.JyotishaEngine(astroEngine)
        val city = CityRepository.getDefaultCity()
        val natalSummary = jyotishaEngine.calculateNatalChart(
            birthDate = LocalDate.of(1995, 5, 20),
            birthTime = LocalTime.of(6, 30),
            location = city,
            ayanamsha = AyanamshaSystem.LAHIRI
        )

        val kundali = jyotishaEngine.generateKundaliChart(natalSummary)
        assertEquals("Chart has 12 Bhavas", 12, kundali.bhavas.size)
        assertEquals("House 1 Rashi matches Lagna Rashi", natalSummary.lagnaRashi, kundali.bhavas[0].rashi)

        // House sequence follows continuous zodiac order
        for (i in 0 until 12) {
            val expectedRashiIdx = (natalSummary.lagnaRashi.ordinal + i) % 12
            val expectedRashi = Rashi.values()[expectedRashiIdx]
            assertEquals("House ${i + 1} Rashi matches zodiac sequence", expectedRashi, kundali.bhavas[i].rashi)
        }
    }

    @Test
    fun testVastuCompassDirections() {
        val vastuEngine = com.example.engines.vastu.VastuEngine()
        val north = vastuEngine.getDirectionByAngle(0.0)
        assertEquals("0 degrees is North", "North", north.directionName)

        val northEast = vastuEngine.getDirectionByAngle(45.0)
        assertEquals("45 degrees is North-East", "North-East", northEast.directionName)

        val east = vastuEngine.getDirectionByAngle(90.0)
        assertEquals("90 degrees is East", "East", east.directionName)

        val south = vastuEngine.getDirectionByAngle(180.0)
        assertEquals("180 degrees is South", "South", south.directionName)
    }
}
