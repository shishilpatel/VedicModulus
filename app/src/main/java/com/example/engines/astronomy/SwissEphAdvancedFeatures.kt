package com.example.engines.astronomy

import com.example.core.model.Graha
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

data class EclipseEvent(
    val eclipseType: String,
    val peakTime: ZonedDateTime,
    val isVisibleAtLocation: Boolean,
    val description: String
)

data class TransitEvent(
    val graha: Graha,
    val targetRashi: String,
    val transitTime: ZonedDateTime
)

class SwissEphAdvancedFeatures(private val ephemerisPath: String? = null) {
    private val swe = if (ephemerisPath != null) SwissEph(ephemerisPath) else SwissEph()
    private val transitCache = ConcurrentHashMap<String, TransitEvent?>()
    private val eclipseCache = ConcurrentHashMap<String, EclipseEvent?>()

    init {
        if (ephemerisPath == null) {
            swe.swe_set_ephe_path(null)
        }
    }

    companion object {
        @Volatile
        private var instance: SwissEphAdvancedFeatures? = null

        fun getInstance(): SwissEphAdvancedFeatures {
            return instance ?: synchronized(this) {
                instance ?: SwissEphAdvancedFeatures().also { instance = it }
            }
        }
    }

    @Synchronized
    private fun getPlanetSiderealLon(planetFlag: Int, jdUT: Double): Pair<Double, Double> {
        val xx = DoubleArray(6)
        val err = StringBuffer()
        val flags = SweConst.SEFLG_MOSEPH or SweConst.SEFLG_SIDEREAL or SweConst.SEFLG_SPEED
        swe.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0.0, 0.0)
        val res = swe.swe_calc_ut(jdUT, planetFlag, flags, xx, err)
        val lon = if (res >= 0) (xx[0] % 360.0 + 360.0) % 360.0 else 0.0
        val speed = if (res >= 0) xx[3] else 0.0
        return Pair(lon, speed)
    }

    /**
     * Calculates the next solar or lunar eclipse from the given date.
     */
    @Synchronized
    fun findNextEclipse(
        latitude: Double,
        longitude: Double,
        altitudeMeters: Double = 0.0,
        fromDateTime: ZonedDateTime = ZonedDateTime.now(),
        isLunar: Boolean = false
    ): EclipseEvent? {
        val cacheKey = "${isLunar}_${fromDateTime.toLocalDate()}_${(latitude * 10).toInt()}_${(longitude * 10).toInt()}"
        if (eclipseCache.containsKey(cacheKey)) {
            return eclipseCache[cacheKey]
        }

        try {
            swe.swe_set_topo(longitude, latitude, altitudeMeters)
            
            val utcZdt = fromDateTime.withZoneSameInstant(ZoneOffset.UTC)
            val hourFraction = utcZdt.hour + (utcZdt.minute / 60.0)
            val sweDate = SweDate(utcZdt.year, utcZdt.monthValue, utcZdt.dayOfMonth, hourFraction, SweDate.SE_GREG_CAL)
            val jdUT = sweDate.julDay

            val tret = DoubleArray(10)
            val attr = DoubleArray(20)
            val geopos = doubleArrayOf(longitude, latitude, altitudeMeters)
            val err = StringBuffer()

            val flags = SweConst.SEFLG_MOSEPH or SweConst.SEFLG_TOPOCTR

            val result = if (isLunar) {
                swe.swe_lun_eclipse_when_loc(jdUT, flags, geopos, tret, attr, 0, err)
            } else {
                swe.swe_sol_eclipse_when_loc(jdUT, flags, geopos, tret, attr, 0, err)
            }

            if (result == SweConst.ERR || tret[0] <= 0.0) {
                eclipseCache[cacheKey] = null
                return null
            }

            val peakJd = tret[0]
            val peakSweDate = SweDate(peakJd)
            val year = peakSweDate.year
            val month = peakSweDate.month
            val day = peakSweDate.day
            val hourDouble = peakSweDate.hour
            val hr = hourDouble.toInt().coerceIn(0, 23)
            val minDouble = (hourDouble - hr) * 60
            val min = minDouble.toInt().coerceIn(0, 59)
            val sec = ((minDouble - min) * 60).toInt().coerceIn(0, 59)
            
            val peakTime = ZonedDateTime.of(year, month, day, hr, min, sec, 0, ZoneOffset.UTC).withZoneSameInstant(fromDateTime.zone)
            val isVisible = attr[0] > 0.0

            val event = EclipseEvent(
                eclipseType = if (isLunar) "Chandra Grahan (Lunar Eclipse)" else "Surya Grahan (Solar Eclipse)",
                peakTime = peakTime,
                isVisibleAtLocation = isVisible,
                description = "Peak of the eclipse."
            )
            eclipseCache[cacheKey] = event
            return event
        } catch (e: Exception) {
            eclipseCache[cacheKey] = null
            return null
        }
    }

    /**
     * Calculates the exact time a planet transits into a new Rashi (30 degree sectors).
     * Uses fast, deterministic bisection with the Moshier engine to eliminate hangs.
     */
    @Synchronized
    fun findNextRashiTransit(
        graha: Graha,
        fromDateTime: ZonedDateTime
    ): TransitEvent? {
        val cacheKey = "${graha.name}_${fromDateTime.toLocalDate()}"
        if (transitCache.containsKey(cacheKey)) {
            return transitCache[cacheKey]
        }

        val planetFlag = when(graha) {
            Graha.GURU -> SweConst.SE_JUPITER
            Graha.SHANI -> SweConst.SE_SATURN
            Graha.RAHU -> SweConst.SE_TRUE_NODE
            Graha.KETU -> SweConst.SE_TRUE_NODE
            Graha.SURYA -> SweConst.SE_SUN
            Graha.MANGALA -> SweConst.SE_MARS
            Graha.BUDHA -> SweConst.SE_MERCURY
            Graha.SHUKRA -> SweConst.SE_VENUS
            else -> return null
        }
        
        val utcZdt = fromDateTime.withZoneSameInstant(ZoneOffset.UTC)
        val hourFraction = utcZdt.hour + (utcZdt.minute / 60.0)
        val sweDate = SweDate(utcZdt.year, utcZdt.monthValue, utcZdt.dayOfMonth, hourFraction, SweDate.SE_GREG_CAL)
        val jdUT0 = sweDate.julDay

        return try {
            val isRetrogradeNode = (graha == Graha.RAHU || graha == Graha.KETU)
            val (startRawLon, startSpeed) = getPlanetSiderealLon(planetFlag, jdUT0)
            val currentLon = if (graha == Graha.KETU) (startRawLon + 180.0) % 360.0 else startRawLon
            val currentRashiIndex = (currentLon / 30.0).toInt().coerceIn(0, 11)

            // Target sign index
            val targetRashiIndex = if (isRetrogradeNode) {
                (currentRashiIndex + 11) % 12
            } else {
                (currentRashiIndex + 1) % 12
            }

            // Estimate step size
            val avgSpeed = when(graha) {
                Graha.GURU -> 0.0831
                Graha.SHANI -> 0.0335
                Graha.RAHU, Graha.KETU -> 0.0529
                Graha.SURYA -> 0.9856
                Graha.MANGALA -> 0.524
                else -> 0.6
            }

            val degToBoundary = if (isRetrogradeNode) {
                var d = currentLon - (currentRashiIndex * 30.0)
                if (d <= 0.0) d += 360.0
                d
            } else {
                var d = ((currentRashiIndex + 1) * 30.0) - currentLon
                if (d <= 0.0) d += 360.0
                d
            }

            val estimatedDays = (degToBoundary / avgSpeed).coerceIn(1.0, 1100.0)
            val stepDays = maxOf(1.0, estimatedDays / 10.0)

            // Step forward to bracket the transit entry
            var prevJd = jdUT0
            var currJd = jdUT0
            var foundBracket = false

            for (step in 1..40) {
                currJd = jdUT0 + (step * stepDays)
                val (rawLon, _) = getPlanetSiderealLon(planetFlag, currJd)
                val testLon = if (graha == Graha.KETU) (rawLon + 180.0) % 360.0 else rawLon
                val testRashi = (testLon / 30.0).toInt().coerceIn(0, 11)

                if (testRashi != currentRashiIndex) {
                    foundBracket = true
                    break
                }
                prevJd = currJd
            }

            if (!foundBracket) {
                transitCache[cacheKey] = null
                return null
            }

            // Binary search (bisection) to find the exact transit moment (< 1 minute accuracy)
            var left = prevJd
            var right = currJd
            for (iter in 0 until 18) {
                val mid = (left + right) / 2.0
                val (rawLon, _) = getPlanetSiderealLon(planetFlag, mid)
                val midLon = if (graha == Graha.KETU) (rawLon + 180.0) % 360.0 else rawLon
                val midRashi = (midLon / 30.0).toInt().coerceIn(0, 11)

                if (midRashi == currentRashiIndex) {
                    left = mid
                } else {
                    right = mid
                }
            }

            val transitJd = right
            val trSweDate = SweDate(transitJd)
            val year = trSweDate.year
            val month = trSweDate.month
            val day = trSweDate.day
            val hourDouble = trSweDate.hour
            val hr = hourDouble.toInt().coerceIn(0, 23)
            val minDouble = (hourDouble - hr) * 60
            val min = minDouble.toInt().coerceIn(0, 59)
            val sec = ((minDouble - min) * 60).toInt().coerceIn(0, 59)

            val transitTime = ZonedDateTime.of(year, month, day, hr, min, sec, 0, ZoneOffset.UTC)
                .withZoneSameInstant(fromDateTime.zone)

            val rashiNames = listOf(
                "Mesha", "Vrishabha", "Mithuna", "Karkata",
                "Simha", "Kanya", "Tula", "Vrishchika",
                "Dhanus", "Makara", "Kumbha", "Meena"
            )
            val targetRashiName = rashiNames[targetRashiIndex]

            val event = TransitEvent(
                graha = graha,
                targetRashi = targetRashiName,
                transitTime = transitTime
            )
            transitCache[cacheKey] = event
            event
        } catch (e: Exception) {
            transitCache[cacheKey] = null
            null
        }
    }
}
