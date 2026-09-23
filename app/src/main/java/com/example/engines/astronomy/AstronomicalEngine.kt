package com.example.engines.astronomy

import com.example.core.model.Graha
import java.time.ZonedDateTime

/**
 * Astronomical coordinates in the ecliptic system.
 */
data class EclipticCoordinates(
    val tropicalLongitudeDeg: Double,
    val latitudeDeg: Double = 0.0,
    val distanceAu: Double = 1.0
)

/**
 * Planetary position result containing both tropical and sidereal longitude.
 */
data class CelestialBodyPosition(
    val graha: Graha,
    val tropicalLongitudeDeg: Double,
    val siderealLongitudeDeg: Double,
    val rightAscensionHours: Double,
    val declinationDeg: Double
)

/**
 * Solar transit events for a given calendar day and location.
 */
data class SolarTimes(
    val sunrise: ZonedDateTime,
    val sunset: ZonedDateTime,
    val solarNoon: ZonedDateTime
)

/**
 * Lunar transit events for a given calendar day and location.
 */
data class LunarTimes(
    val moonrise: ZonedDateTime?,
    val moonset: ZonedDateTime?
)

/**
 * Abstraction for astronomical calculations.
 * Allows interchangeable implementations (e.g. Jean Meeus VSOP87, Swiss Ephemeris wrapper, or USNO tables).
 */
interface AstronomicalEngine {
    /**
     * Calculates Sun's apparent geocentric ecliptic longitude and coordinates.
     */
    fun sunPosition(dateTime: ZonedDateTime): EclipticCoordinates

    /**
     * Calculates Moon's apparent geocentric ecliptic longitude and coordinates.
     */
    fun moonPosition(dateTime: ZonedDateTime): EclipticCoordinates

    /**
     * Calculates positions of all Navagrahas for the specified time and ayanamsha offset.
     */
    fun planetaryPositions(dateTime: ZonedDateTime, ayanamshaDeg: Double): Map<Graha, CelestialBodyPosition>

    /**
     * Calculates precise sunrise, sunset, and solar noon taking into account
     * atmospheric refraction (34 arcmin) and solar semidiameter (16 arcmin).
     */
    fun calculateSolarTimes(date: ZonedDateTime, latitude: Double, longitude: Double): SolarTimes

    /**
     * Calculates moonrise and moonset times for the specified date and coordinates.
     */
    fun calculateLunarTimes(date: ZonedDateTime, latitude: Double, longitude: Double): LunarTimes

    /**
     * Calculates the Chitrapaksha / Lahiri Ayanamsha for a given epoch.
     */
    fun calculateLahiriAyanamsha(dateTime: ZonedDateTime): Double
}
