package com.example.engines.astronomy

import com.example.core.model.Graha
import com.example.core.model.Nakshatra
import com.example.core.model.Rashi
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.abs

/**
 * Dignity of a celestial body in Vedic astrology.
 */
enum class PlanetDignity(val displayName: String, val devanagari: String) {
    UCHHA("Exalted (Uccha)", "उच्च"),
    NEECHA("Debilitated (Neecha)", "नीच"),
    MOOLATRIKONA("Moolatrikona", "मूलत्रिकोण"),
    SWAKSHETRA("Own Sign (Swakshetra)", "स्वक्षेत्र"),
    MITRA("Friendly Sign (Mitra)", "मित्र"),
    SHATRU("Inimical Sign (Shatru)", "शत्रु"),
    SAMA("Neutral Sign (Sama)", "सम")
}

/**
 * Detailed real-time Vedic Graha Spashta (planetary position).
 */
data class GrahaSpashta(
    val graha: Graha,
    val longitudeDeg: Double,
    val rashi: Rashi,
    val degreesInRashi: Double,
    val dmsFormatted: String,
    val nakshatra: Nakshatra,
    val nakshatraPada: Int,
    val speedDegPerDay: Double,
    val isRetrograde: Boolean,
    val isCombust: Boolean,
    val dignity: PlanetDignity,
    val bhava: Int, // House 1 to 12 from Lagna
    val grahaDrishtiBhavas: List<Int> // Aspect houses
)

/**
 * Real-time Lagna (Ascendant) calculation details.
 */
data class LagnaSpashta(
    val longitudeDeg: Double,
    val rashi: Rashi,
    val degreesInRashi: Double,
    val dmsFormatted: String,
    val nakshatra: Nakshatra,
    val nakshatraPada: Int
)

/**
 * Comprehensive real-time Graha Spashta report for a specific user location and timestamp.
 */
data class RealTimeGrahaSpashtaReport(
    val dateTime: ZonedDateTime,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val ayanamshaName: String,
    val ayanamshaDegrees: Double,
    val ayanamshaDms: String,
    val lagna: LagnaSpashta,
    val grahas: List<GrahaSpashta>,
    val bhavaCusps: List<Double>,
    val calculationEngine: String = "Vedic Sidereal Precision Engine"
) {
    fun getGraha(graha: Graha): GrahaSpashta? = grahas.find { it.graha == graha }
}

/**
 * Production-ready helper class utilizing the Astrodienst 'swisseph' library
 * to calculate real-time Vedic planetary positions (Graha Spashta) and Lagna
 * for the user's current geographic location.
 */
class SwissEphGrahaSpashtaHelper(private val ephemerisPath: String? = null) {

    private val swe = if (ephemerisPath != null) SwissEph(ephemerisPath) else SwissEph()

    private val cache = java.util.concurrent.ConcurrentHashMap<String, RealTimeGrahaSpashtaReport>()

    init {
        if (ephemerisPath == null) {
            swe.swe_set_ephe_path(null)
        }
    }

    /**
     * Calculates real-time Vedic planetary positions (Graha Spashta) for the given location and time.
     *
     * @param latitude Observer's latitude in degrees (North positive)
     * @param longitude Observer's longitude in degrees (East positive)
     * @param altitudeMeters Elevation above sea level
     * @param dateTime Local zoned timestamp of calculation
     * @param ayanamshaMode Sidereal ayanamsha (defaults to Chitrapaksha / Lahiri = SweConst.SE_SIDM_LAHIRI)
     */
    @Synchronized
    fun calculateRealTimeGrahaSpashta(
        latitude: Double,
        longitude: Double,
        altitudeMeters: Double = 0.0,
        dateTime: ZonedDateTime = ZonedDateTime.now(),
        ayanamshaMode: Int = SweConst.SE_SIDM_LAHIRI
    ): RealTimeGrahaSpashtaReport {
        val cacheKey = "${(latitude * 100).toInt()}_${(longitude * 100).toInt()}_${dateTime.toEpochSecond() / 120}_$ayanamshaMode"
        cache[cacheKey]?.let { return it }

        // Configure Swiss Ephemeris with Sidereal Ayanamsha (Lahiri)
        swe.swe_set_sid_mode(ayanamshaMode, 0.0, 0.0)

        // Set Topocentric coordinates for exact user location
        swe.swe_set_topo(longitude, latitude, altitudeMeters)

        // Convert target dateTime to Universal Time Julian Day
        val utcZdt = dateTime.withZoneSameInstant(ZoneOffset.UTC)
        val hourFraction = utcZdt.hour + (utcZdt.minute / 60.0) + ((utcZdt.second + utcZdt.nano / 1_000_000_000.0) / 3600.0)
        val sweDate = SweDate(utcZdt.year, utcZdt.monthValue, utcZdt.dayOfMonth, hourFraction, SweDate.SE_GREG_CAL)
        val jdUT = sweDate.julDay

        // Obtain exact Lahiri Ayanamsha for this epoch
        val ayanamshaDeg = normalizeDegrees(swe.swe_get_ayanamsa_ut(jdUT))
        val ayanamshaDms = formatDms(ayanamshaDeg)

        // Calculate Real-Time Lagna (Ascendant) & 12 Bhava Cusps
        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        // House system 'P' (Placidus) or 'W' (Whole sign) with SEFLG_SIDEREAL
        swe.swe_houses(jdUT, SweConst.SEFLG_SIDEREAL, latitude, longitude, 'P'.code, cusps, ascmc)
        val lagnaDeg = normalizeDegrees(ascmc[0])
        val lagnaRashi = rashiFromDegrees(lagnaDeg)
        val lagnaDegInRashi = lagnaDeg % 30.0
        val (lagnaNak, lagnaPada) = nakshatraFromDegrees(lagnaDeg)

        val lagnaSpashta = LagnaSpashta(
            longitudeDeg = lagnaDeg,
            rashi = lagnaRashi,
            degreesInRashi = lagnaDegInRashi,
            dmsFormatted = formatDms(lagnaDegInRashi),
            nakshatra = lagnaNak,
            nakshatraPada = lagnaPada
        )

        // Fast analytical calculation using Moshier ephemeris (SEFLG_MOSEPH)
        val calcFlags = SweConst.SEFLG_MOSEPH or SweConst.SEFLG_SIDEREAL or SweConst.SEFLG_SPEED

        // Temporary map of raw positions for combustion calculation
        val rawPositions = mutableMapOf<Graha, Double>()
        val rawSpeeds = mutableMapOf<Graha, Double>()

        val grahaPlanetIds = listOf(
            Graha.SURYA to SweConst.SE_SUN,
            Graha.CHANDRA to SweConst.SE_MOON,
            Graha.MANGALA to SweConst.SE_MARS,
            Graha.BUDHA to SweConst.SE_MERCURY,
            Graha.GURU to SweConst.SE_JUPITER,
            Graha.SHUKRA to SweConst.SE_VENUS,
            Graha.SHANI to SweConst.SE_SATURN
        )

        val xx = DoubleArray(6)
        val serr = StringBuffer()

        // 1. Calculate 7 Classical Planets
        for ((graha, pid) in grahaPlanetIds) {
            serr.setLength(0)
            swe.swe_calc_ut(jdUT, pid, calcFlags, xx, serr)
            val lon = normalizeDegrees(xx[0])
            val speed = xx[3]
            rawPositions[graha] = lon
            rawSpeeds[graha] = speed
        }

        // 2. Calculate Rahu (Mean Node for fast deterministic calculation) & Ketu (180° opposite)
        serr.setLength(0)
        swe.swe_calc_ut(jdUT, SweConst.SE_MEAN_NODE, calcFlags, xx, serr)
        val rahuLon = normalizeDegrees(xx[0])
        val rahuSpeed = xx[3]
        rawPositions[Graha.RAHU] = rahuLon
        rawSpeeds[Graha.RAHU] = rahuSpeed

        val ketuLon = normalizeDegrees(rahuLon + 180.0)
        val ketuSpeed = rahuSpeed // Nodes move in retrograde (approx -0.05°/day)
        rawPositions[Graha.KETU] = ketuLon
        rawSpeeds[Graha.KETU] = ketuSpeed

        val suryaLon = rawPositions[Graha.SURYA] ?: 0.0

        // 3. Build rich GrahaSpashta details for all 9 Navagrahas
        val allGrahas = Graha.values().map { graha ->
            val lon = rawPositions[graha] ?: 0.0
            val speed = rawSpeeds[graha] ?: 0.0
            val rashi = rashiFromDegrees(lon)
            val degInRashi = lon % 30.0
            val (nak, pada) = nakshatraFromDegrees(lon)
            val isRetro = speed < 0.0

            // Check Asta (Combustion) by angular distance to Surya
            val isCombust = if (graha == Graha.SURYA || graha == Graha.RAHU || graha == Graha.KETU) {
                false
            } else {
                val angularDistance = angularSeparation(lon, suryaLon)
                val combustionLimit = getCombustionLimitDeg(graha, isRetro)
                angularDistance <= combustionLimit
            }

            val dignity = calculateDignity(graha, rashi, degInRashi)
            val bhava = calculateBhavaFromLagna(lon, lagnaDeg)
            val drishtiBhavas = calculateGrahaDrishti(graha, bhava)

            GrahaSpashta(
                graha = graha,
                longitudeDeg = lon,
                rashi = rashi,
                degreesInRashi = degInRashi,
                dmsFormatted = formatDms(degInRashi),
                nakshatra = nak,
                nakshatraPada = pada,
                speedDegPerDay = speed,
                isRetrograde = isRetro,
                isCombust = isCombust,
                dignity = dignity,
                bhava = bhava,
                grahaDrishtiBhavas = drishtiBhavas
            )
        }

        val bhavaCuspsList = (1..12).map { cusps[it] }

        val ayanamsaNameStr = when (ayanamshaMode) {
            SweConst.SE_SIDM_RAMAN -> "B.V. Raman"
            SweConst.SE_SIDM_KRISHNAMURTI -> "Krishnamurti (KP)"
            SweConst.SE_SIDM_TRUE_PUSHYA -> "True Pushya"
            else -> "Lahiri (Chitrapaksha)"
        }

        val report = RealTimeGrahaSpashtaReport(
            dateTime = dateTime,
            latitude = latitude,
            longitude = longitude,
            altitudeMeters = altitudeMeters,
            ayanamshaName = ayanamsaNameStr,
            ayanamshaDegrees = ayanamshaDeg,
            ayanamshaDms = ayanamshaDms,
            lagna = lagnaSpashta,
            grahas = allGrahas,
            bhavaCusps = bhavaCuspsList
        )
        if (cache.size > 50) {
            cache.clear()
        }
        cache[cacheKey] = report
        return report
    }

    companion object {
        private var instance: SwissEphGrahaSpashtaHelper? = null

        fun getInstance(): SwissEphGrahaSpashtaHelper {
            return instance ?: synchronized(this) {
                instance ?: SwissEphGrahaSpashtaHelper().also { instance = it }
            }
        }

        fun normalizeDegrees(deg: Double): Double {
            var res = deg % 360.0
            if (res < 0) res += 360.0
            return res
        }

        fun formatDms(deg: Double): String {
            val d = deg.toInt()
            val remMinutes = (deg - d) * 60.0
            val m = remMinutes.toInt()
            val s = ((remMinutes - m) * 60.0).toInt()
            return "%02d° %02d' %02d\"".format(d, m, s)
        }

        fun rashiFromDegrees(deg: Double): Rashi {
            val norm = normalizeDegrees(deg)
            val index = (norm / 30.0).toInt().coerceIn(0, 11)
            return Rashi.values()[index]
        }

        fun nakshatraFromDegrees(deg: Double): Pair<Nakshatra, Int> {
            val norm = normalizeDegrees(deg)
            val nakshatraSpan = 360.0 / 27.0 // 13.3333333° (13°20')
            val padaSpan = nakshatraSpan / 4.0 // 3.3333333° (3°20')

            val nakshatraIndex = (norm / nakshatraSpan).toInt().coerceIn(0, 26)
            val remInNakshatra = norm - (nakshatraIndex * nakshatraSpan)
            val pada = (remInNakshatra / padaSpan).toInt().coerceIn(0, 3) + 1

            return Pair(Nakshatra.values()[nakshatraIndex], pada)
        }

        /**
         * Calculates angular separation on a 360° circle.
         */
        fun angularSeparation(deg1: Double, deg2: Double): Double {
            val diff = abs(normalizeDegrees(deg1) - normalizeDegrees(deg2))
            return if (diff > 180.0) 360.0 - diff else diff
        }

        /**
         * Classical Vedic combustion (Asta) orbs in degrees from Surya.
         */
        fun getCombustionLimitDeg(graha: Graha, isRetrograde: Boolean): Double {
            return when (graha) {
                Graha.CHANDRA -> 12.0
                Graha.MANGALA -> 17.0
                Graha.BUDHA -> if (isRetrograde) 12.0 else 14.0
                Graha.GURU -> 11.0
                Graha.SHUKRA -> if (isRetrograde) 8.0 else 10.0
                Graha.SHANI -> 15.0
                else -> 0.0
            }
        }

        /**
         * Calculates Bhava (House 1..12) from Lagna using Vedic Whole Sign / Rashi Bhava system.
         */
        fun calculateBhavaFromLagna(grahaLon: Double, lagnaLon: Double): Int {
            val lagnaRashiIndex = (normalizeDegrees(lagnaLon) / 30.0).toInt()
            val grahaRashiIndex = (normalizeDegrees(grahaLon) / 30.0).toInt()
            var bhava = (grahaRashiIndex - lagnaRashiIndex) + 1
            if (bhava <= 0) bhava += 12
            return bhava
        }

        /**
         * Classical Graha Drishti (aspects) on Bhavas.
         * All planets aspect 7th house from themselves.
         * Special aspects:
         * - Mars: 4th, 7th, 8th
         * - Jupiter & Rahu/Ketu: 5th, 7th, 9th
         * - Saturn: 3rd, 7th, 10th
         */
        fun calculateGrahaDrishti(graha: Graha, currentBhava: Int): List<Int> {
            fun aspectHouse(offset: Int): Int {
                var h = (currentBhava + offset - 1) % 12 + 1
                if (h <= 0) h += 12
                return h
            }

            return when (graha) {
                Graha.MANGALA -> listOf(aspectHouse(4), aspectHouse(7), aspectHouse(8))
                Graha.GURU, Graha.RAHU, Graha.KETU -> listOf(aspectHouse(5), aspectHouse(7), aspectHouse(9))
                Graha.SHANI -> listOf(aspectHouse(3), aspectHouse(7), aspectHouse(10))
                else -> listOf(aspectHouse(7))
            }
        }

        /**
         * Authentic dignity determination according to Parashara Hora Shastra.
         */
        fun calculateDignity(graha: Graha, rashi: Rashi, degInRashi: Double): PlanetDignity {
            return when (graha) {
                Graha.SURYA -> when (rashi) {
                    Rashi.MESHA -> if (degInRashi <= 10.0) PlanetDignity.UCHHA else PlanetDignity.MITRA
                    Rashi.TULA -> if (degInRashi <= 10.0) PlanetDignity.NEECHA else PlanetDignity.SHATRU
                    Rashi.SIMHA -> if (degInRashi <= 20.0) PlanetDignity.MOOLATRIKONA else PlanetDignity.SWAKSHETRA
                    Rashi.DHANUS, Rashi.MEENA, Rashi.VRISHCHIKA, Rashi.KARKATA -> PlanetDignity.MITRA
                    Rashi.MAKARA, Rashi.KUMBHA, Rashi.VRISHABHA, Rashi.KANYA -> PlanetDignity.SHATRU
                    else -> PlanetDignity.SAMA
                }
                Graha.CHANDRA -> when (rashi) {
                    Rashi.VRISHABHA -> if (degInRashi <= 3.0) PlanetDignity.UCHHA else PlanetDignity.MOOLATRIKONA
                    Rashi.VRISHCHIKA -> if (degInRashi <= 3.0) PlanetDignity.NEECHA else PlanetDignity.SAMA
                    Rashi.KARKATA -> PlanetDignity.SWAKSHETRA
                    Rashi.MESHA, Rashi.MITHUNA, Rashi.SIMHA, Rashi.KANYA -> PlanetDignity.MITRA
                    else -> PlanetDignity.SAMA
                }
                Graha.MANGALA -> when (rashi) {
                    Rashi.MAKARA -> if (degInRashi <= 28.0) PlanetDignity.UCHHA else PlanetDignity.SHATRU
                    Rashi.KARKATA -> if (degInRashi <= 28.0) PlanetDignity.NEECHA else PlanetDignity.MITRA
                    Rashi.MESHA -> if (degInRashi <= 12.0) PlanetDignity.MOOLATRIKONA else PlanetDignity.SWAKSHETRA
                    Rashi.VRISHCHIKA -> PlanetDignity.SWAKSHETRA
                    Rashi.SIMHA, Rashi.DHANUS, Rashi.MEENA -> PlanetDignity.MITRA
                    Rashi.MITHUNA, Rashi.KANYA -> PlanetDignity.SHATRU
                    else -> PlanetDignity.SAMA
                }
                Graha.BUDHA -> when (rashi) {
                    Rashi.KANYA -> if (degInRashi <= 15.0) PlanetDignity.UCHHA else if (degInRashi <= 20.0) PlanetDignity.MOOLATRIKONA else PlanetDignity.SWAKSHETRA
                    Rashi.MEENA -> if (degInRashi <= 15.0) PlanetDignity.NEECHA else PlanetDignity.SAMA
                    Rashi.MITHUNA -> PlanetDignity.SWAKSHETRA
                    Rashi.VRISHABHA, Rashi.SIMHA, Rashi.TULA -> PlanetDignity.MITRA
                    Rashi.KARKATA -> PlanetDignity.SHATRU
                    else -> PlanetDignity.SAMA
                }
                Graha.GURU -> when (rashi) {
                    Rashi.KARKATA -> if (degInRashi <= 5.0) PlanetDignity.UCHHA else PlanetDignity.MITRA
                    Rashi.MAKARA -> if (degInRashi <= 5.0) PlanetDignity.NEECHA else PlanetDignity.SHATRU
                    Rashi.DHANUS -> if (degInRashi <= 10.0) PlanetDignity.MOOLATRIKONA else PlanetDignity.SWAKSHETRA
                    Rashi.MEENA -> PlanetDignity.SWAKSHETRA
                    Rashi.MESHA, Rashi.SIMHA, Rashi.VRISHCHIKA -> PlanetDignity.MITRA
                    Rashi.MITHUNA, Rashi.KANYA -> PlanetDignity.SHATRU
                    else -> PlanetDignity.SAMA
                }
                Graha.SHUKRA -> when (rashi) {
                    Rashi.MEENA -> if (degInRashi <= 27.0) PlanetDignity.UCHHA else PlanetDignity.SAMA
                    Rashi.KANYA -> if (degInRashi <= 27.0) PlanetDignity.NEECHA else PlanetDignity.MITRA
                    Rashi.TULA -> if (degInRashi <= 15.0) PlanetDignity.MOOLATRIKONA else PlanetDignity.SWAKSHETRA
                    Rashi.VRISHABHA -> PlanetDignity.SWAKSHETRA
                    Rashi.MITHUNA, Rashi.MAKARA, Rashi.KUMBHA -> PlanetDignity.MITRA
                    Rashi.SIMHA, Rashi.KARKATA -> PlanetDignity.SHATRU
                    else -> PlanetDignity.SAMA
                }
                Graha.SHANI -> when (rashi) {
                    Rashi.TULA -> if (degInRashi <= 20.0) PlanetDignity.UCHHA else PlanetDignity.MITRA
                    Rashi.MESHA -> if (degInRashi <= 20.0) PlanetDignity.NEECHA else PlanetDignity.SHATRU
                    Rashi.KUMBHA -> if (degInRashi <= 20.0) PlanetDignity.MOOLATRIKONA else PlanetDignity.SWAKSHETRA
                    Rashi.MAKARA -> PlanetDignity.SWAKSHETRA
                    Rashi.VRISHABHA, Rashi.MITHUNA, Rashi.KANYA -> PlanetDignity.MITRA
                    Rashi.SIMHA, Rashi.KARKATA, Rashi.VRISHCHIKA -> PlanetDignity.SHATRU
                    else -> PlanetDignity.SAMA
                }
                Graha.RAHU -> when (rashi) {
                    Rashi.VRISHABHA, Rashi.MITHUNA -> PlanetDignity.UCHHA
                    Rashi.VRISHCHIKA, Rashi.DHANUS -> PlanetDignity.NEECHA
                    Rashi.KUMBHA -> PlanetDignity.MOOLATRIKONA
                    Rashi.KANYA -> PlanetDignity.SWAKSHETRA
                    else -> PlanetDignity.SAMA
                }
                Graha.KETU -> when (rashi) {
                    Rashi.VRISHCHIKA, Rashi.DHANUS -> PlanetDignity.UCHHA
                    Rashi.VRISHABHA, Rashi.MITHUNA -> PlanetDignity.NEECHA
                    Rashi.SIMHA -> PlanetDignity.MOOLATRIKONA
                    Rashi.MEENA -> PlanetDignity.SWAKSHETRA
                    else -> PlanetDignity.SAMA
                }
            }
        }
    }
}
