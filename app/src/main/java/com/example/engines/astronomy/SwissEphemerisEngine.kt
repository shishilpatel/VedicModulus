package com.example.engines.astronomy

import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.ZonedDateTime

/**
 * An integration of the Thomas Mack Java port of Swiss Ephemeris.
 * 
 * NOTE: To use this engine in production, you must:
 * 1. Add the Swisseph library dependency (e.g., via JitPack `com.github.krishnact:swisseph` or manually bundle the jar).
 * 2. Ensure ephemeris data files (.se1) are bundled in the Android assets and copied to the internal storage, 
 *    then pass that directory path to `SwissEph(ephePath)`.
 * 3. Comply with the GPLv2 or purchase a commercial license from Astrodienst.
 */
class SwissEphemerisEngine(ephemerisPath: String? = null) {
    
    private val swe = if (ephemerisPath != null) SwissEph(ephemerisPath) else SwissEph()

    init {
        // Use Moshier analytical ephemeris if no data files are available (slightly less accurate but requires no files)
        if (ephemerisPath == null) {
            swe.swe_set_ephe_path(null)
        }
    }

    /**
     * Calculates the sidereal longitude of a specific planet.
     */
    fun calculatePlanetLongitude(
        zonedDateTime: ZonedDateTime,
        planetId: Int, // e.g., SweConst.SE_SUN
        ayanamsha: Int = SweConst.SE_SIDM_LAHIRI
    ): Double {
        // Convert ZonedDateTime to Julian Day (ET)
        val sweDate = getSweDate(zonedDateTime)
        val jdUT = sweDate.julDay
        val jdET = jdUT + sweDate.deltaT

        // Set Ayanamsha
        swe.swe_set_sid_mode(ayanamsha, 0.0, 0.0)

        val flags = SweConst.SEFLG_SWIEPH or SweConst.SEFLG_SIDEREAL or SweConst.SEFLG_SPEED
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        val result = swe.swe_calc(jdET, planetId, flags, xx, serr)
        
        if (result < 0) {
            // Fallback to Moshier if Swiss Ephemeris files are missing
            val moshierFlags = SweConst.SEFLG_MOSEPH or SweConst.SEFLG_SIDEREAL
            swe.swe_calc(jdET, planetId, moshierFlags, xx, serr)
        }

        return xx[0] // Longitude in degrees
    }

    private fun getSweDate(zdt: ZonedDateTime): SweDate {
        val y = zdt.year
        val m = zdt.monthValue
        val d = zdt.dayOfMonth
        val h = zdt.hour + zdt.minute / 60.0 + zdt.second / 3600.0
        
        return SweDate(y, m, d, h, SweDate.SE_GREG_CAL)
    }
}
