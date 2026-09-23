package com.example.generator.watchface

import com.example.core.model.Graha
import com.example.core.model.Rashi
import com.example.engines.astronomy.SwissEphGrahaSpashtaHelper
import java.time.ZonedDateTime
import kotlin.math.roundToInt

/**
 * Visual presentation model for a planetary body on the celestial clock dial.
 */
data class CelestialPlanetMarker(
    val graha: Graha,
    val name: String,
    val sanskritName: String,
    val symbol: String,
    val longitudeDeg: Double,      // Sidereal Nirayana longitude (0.0 to 360.0) from Swisseph
    val speedDegPerDay: Double,    // Daily motion speed
    val isRetrograde: Boolean,     // True if moving backwards in zodiac
    val rashi: Rashi,              // Current sidereal sign
    val degreesInRashi: Double,    // 0.0 to 30.0
    val orbitRadiusRatio: Float,   // Relative distance from dial center (0.0 to 1.0)
    val colorHex: Long,            // Primary gemstone / celestial color
    val glowColorHex: Long,        // Radial aura color
    val markerRadiusDp: Float,     // Diameter size of planet sphere on dial
    val displayOrder: Int
)

/**
 * Real-time celestial clock state calculated from the Swiss Ephemeris library.
 * Represents the complete astronomical geometry of the skies for the watch face.
 */
data class CelestialClockState(
    val timestamp: ZonedDateTime,
    val latitude: Double,
    val longitude: Double,
    val lagnaLongitudeDeg: Double, // Exact Ascendant (Eastern Horizon) degree
    val lagnaRashi: Rashi,
    val ayanamshaDeg: Double,
    val ayanamshaName: String,
    val planets: List<CelestialPlanetMarker>,
    val surya: CelestialPlanetMarker,
    val chandra: CelestialPlanetMarker,
    val rahu: CelestialPlanetMarker,
    val ketu: CelestialPlanetMarker,
    val activeHoraLord: Graha? = null
) {
    fun getPlanet(graha: Graha): CelestialPlanetMarker? = planets.find { it.graha == graha }

    val formattedLagna: String
        get() = "${lagnaRashi.sanskritName} ${(lagnaLongitudeDeg % 30.0).roundToInt()}°"
}

/**
 * High-performance engine that bridges Swiss Ephemeris astronomical calculations
 * to the watch face's celestial clockwork gear animations and dial geometry.
 */
class CelestialClockEngine(
    private val helper: SwissEphGrahaSpashtaHelper = SwissEphGrahaSpashtaHelper()
) {

    /**
     * Calculates the complete real-time celestial clock state.
     */
    fun calculateClockState(
        dateTime: ZonedDateTime,
        latitude: Double,
        longitude: Double
    ): CelestialClockState {
        val report = try {
            helper.calculateRealTimeGrahaSpashta(
                latitude = latitude,
                longitude = longitude,
                altitudeMeters = 0.0,
                dateTime = dateTime
            )
        } catch (e: Exception) {
            // Safe fallback in case of any calculation error
            null
        }

        val lagnaDeg = report?.lagna?.longitudeDeg ?: 45.0
        val lagnaRashi = report?.lagna?.rashi ?: Rashi.MESHA
        val ayanamsha = report?.ayanamshaDegrees ?: 24.15
        val ayanName = report?.ayanamshaName ?: "Lahiri"

        // Orbital radius calibration: creates distinct concentric planetary tracks
        // from outer edge to dial center without overlapping
        val planetConfigurations = listOf(
            GrahaConfig(Graha.RAHU, 0.86f, 0xFF8B5CF6, 0x448B5CF6, 3.2f, 8),
            GrahaConfig(Graha.SHANI, 0.78f, 0xFF3B82F6, 0x443B82F6, 4.0f, 7),
            GrahaConfig(Graha.GURU, 0.70f, 0xFFF59E0B, 0x44F59E0B, 4.5f, 6),
            GrahaConfig(Graha.MANGALA, 0.62f, 0xFFEF4444, 0x44EF4444, 3.8f, 5),
            GrahaConfig(Graha.SURYA, 0.54f, 0xFFFFD700, 0x66FFD700, 5.5f, 1),
            GrahaConfig(Graha.SHUKRA, 0.46f, 0xFFF472B6, 0x44F472B6, 3.6f, 4),
            GrahaConfig(Graha.BUDHA, 0.38f, 0xFF10B981, 0x4410B981, 3.2f, 3),
            GrahaConfig(Graha.CHANDRA, 0.30f, 0xFFE0F2FE, 0x55BAE6FD, 4.8f, 2),
            GrahaConfig(Graha.KETU, 0.86f, 0xFFA855F7, 0x44A855F7, 3.2f, 9)
        )

        val planetMarkers = planetConfigurations.map { cfg ->
            val spashta = report?.getGraha(cfg.graha)
            val lon = spashta?.longitudeDeg ?: defaultLongitude(cfg.graha, dateTime)
            val speed = spashta?.speedDegPerDay ?: defaultSpeed(cfg.graha)
            val isRetro = spashta?.isRetrograde ?: (speed < 0.0)
            val rashi = spashta?.rashi ?: rashiFromDegrees(lon)
            val degInRashi = spashta?.degreesInRashi ?: (lon % 30.0)

            CelestialPlanetMarker(
                graha = cfg.graha,
                name = cfg.graha.displayName,
                sanskritName = cfg.graha.sanskritName,
                symbol = cfg.graha.symbol,
                longitudeDeg = lon,
                speedDegPerDay = speed,
                isRetrograde = isRetro,
                rashi = rashi,
                degreesInRashi = degInRashi,
                orbitRadiusRatio = cfg.radiusRatio,
                colorHex = cfg.colorHex,
                glowColorHex = cfg.glowHex,
                markerRadiusDp = cfg.sizeDp,
                displayOrder = cfg.order
            )
        }

        val defaultMarker = CelestialPlanetMarker(
            graha = Graha.SURYA,
            name = "Sun",
            sanskritName = "Surya",
            symbol = "☉",
            longitudeDeg = 0.0,
            speedDegPerDay = 1.0,
            isRetrograde = false,
            rashi = Rashi.MESHA,
            degreesInRashi = 0.0,
            orbitRadiusRatio = 0.5f,
            colorHex = 0xFFFFD54F,
            glowColorHex = 0x66FFD54F,
            markerRadiusDp = 5f,
            displayOrder = 0
        )
        val suryaMarker = planetMarkers.firstOrNull { it.graha == Graha.SURYA } ?: defaultMarker
        val chandraMarker = planetMarkers.firstOrNull { it.graha == Graha.CHANDRA } ?: defaultMarker.copy(
            graha = Graha.CHANDRA,
            name = "Moon",
            sanskritName = "Chandra",
            symbol = "☽"
        )
        val rahuMarker = planetMarkers.firstOrNull { it.graha == Graha.RAHU } ?: defaultMarker.copy(
            graha = Graha.RAHU,
            name = "Rahu",
            sanskritName = "Rahu",
            symbol = "☊"
        )
        val ketuMarker = planetMarkers.firstOrNull { it.graha == Graha.KETU } ?: defaultMarker.copy(
            graha = Graha.KETU,
            name = "Ketu",
            sanskritName = "Ketu",
            symbol = "☋"
        )

        return CelestialClockState(
            timestamp = dateTime,
            latitude = latitude,
            longitude = longitude,
            lagnaLongitudeDeg = lagnaDeg,
            lagnaRashi = lagnaRashi,
            ayanamshaDeg = ayanamsha,
            ayanamshaName = ayanName,
            planets = planetMarkers,
            surya = suryaMarker,
            chandra = chandraMarker,
            rahu = rahuMarker,
            ketu = ketuMarker
        )
    }

    private fun defaultLongitude(graha: Graha, dt: ZonedDateTime): Double {
        val dayOfYear = dt.dayOfYear
        return when (graha) {
            Graha.SURYA -> (dayOfYear * (360.0 / 365.25)) % 360.0
            Graha.CHANDRA -> (dayOfYear * 13.176 + dt.hour * 0.55) % 360.0
            Graha.MANGALA -> (dayOfYear * 0.524) % 360.0
            Graha.BUDHA -> ((dayOfYear * (360.0 / 365.25)) + 15.0) % 360.0
            Graha.GURU -> (dayOfYear * 0.083) % 360.0
            Graha.SHUKRA -> ((dayOfYear * (360.0 / 365.25)) - 20.0 + 360.0) % 360.0
            Graha.SHANI -> (dayOfYear * 0.033) % 360.0
            Graha.RAHU -> (360.0 - (dayOfYear * 0.053)) % 360.0
            Graha.KETU -> (180.0 - (dayOfYear * 0.053) + 360.0) % 360.0
        }
    }

    private fun defaultSpeed(graha: Graha): Double {
        return when (graha) {
            Graha.SURYA -> 0.985
            Graha.CHANDRA -> 13.176
            Graha.MANGALA -> 0.524
            Graha.BUDHA -> 1.38
            Graha.GURU -> 0.083
            Graha.SHUKRA -> 1.2
            Graha.SHANI -> 0.033
            Graha.RAHU -> -0.053
            Graha.KETU -> -0.053
        }
    }

    private fun rashiFromDegrees(deg: Double): Rashi {
        val normalized = ((deg % 360.0) + 360.0) % 360.0
        val rashiIndex = (normalized / 30.0).toInt().coerceIn(0, 11)
        return Rashi.values()[rashiIndex]
    }

    private data class GrahaConfig(
        val graha: Graha,
        val radiusRatio: Float,
        val colorHex: Long,
        val glowHex: Long,
        val sizeDp: Float,
        val order: Int
    )
}
