package com.example.engines.astronomy

import com.example.core.model.VedicAssetRegistry
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.ZonedDateTime

object MoonPhaseCalculator {

    private val swe = SwissEph().apply {
        swe_set_ephe_path(null) // Use Moshier analytical ephemeris
    }

    /**
     * Calculates the exact lunar phase angle (0.0 to 360.0 degrees).
     * 0 = New Moon, 90 = First Quarter, 180 = Full Moon, 270 = Last Quarter.
     */
    fun calculateLunarPhaseAngle(zonedDateTime: ZonedDateTime): Double {
        val y = zonedDateTime.year
        val m = zonedDateTime.monthValue
        val d = zonedDateTime.dayOfMonth
        val h = zonedDateTime.hour + zonedDateTime.minute / 60.0 + zonedDateTime.second / 3600.0
        
        val sweDate = SweDate(y, m, d, h, SweDate.SE_GREG_CAL)
        val jdUT = sweDate.julDay
        val jdET = jdUT + sweDate.deltaT

        val flags = SweConst.SEFLG_MOSEPH or SweConst.SEFLG_SWIEPH

        val xxSun = DoubleArray(6)
        val serrSun = StringBuffer()
        swe.swe_calc(jdET, SweConst.SE_SUN, flags, xxSun, serrSun)

        val xxMoon = DoubleArray(6)
        val serrMoon = StringBuffer()
        swe.swe_calc(jdET, SweConst.SE_MOON, flags, xxMoon, serrMoon)

        val sunLong = xxSun[0]
        val moonLong = xxMoon[0]

        var phaseAngle = moonLong - sunLong
        if (phaseAngle < 0) {
            phaseAngle += 360.0
        }
        return phaseAngle
    }

    /**
     * Maps the calculated lunar phase angle to the specific asset ID from VedicAssetRegistry
     * to be used on the watch face and app UI.
     */
    fun getMoonPhaseAssetId(phaseAngle: Double): String {
        return when {
            phaseAngle < 22.5 || phaseAngle >= 337.5 -> VedicAssetRegistry.MOON_PHASE_NEW
            phaseAngle < 67.5 -> VedicAssetRegistry.MOON_PHASE_WAXING_CRESCENT
            phaseAngle < 112.5 -> VedicAssetRegistry.MOON_PHASE_FIRST_QUARTER
            phaseAngle < 157.5 -> VedicAssetRegistry.MOON_PHASE_WAXING_GIBBOUS
            phaseAngle < 202.5 -> VedicAssetRegistry.MOON_PHASE_FULL
            phaseAngle < 247.5 -> VedicAssetRegistry.MOON_PHASE_WANING_GIBBOUS
            phaseAngle < 292.5 -> VedicAssetRegistry.MOON_PHASE_LAST_QUARTER
            else -> VedicAssetRegistry.MOON_PHASE_WANING_CRESCENT
        }
    }
}
