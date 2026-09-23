package com.example.engines.astronomy

import com.example.core.model.Graha
import java.time.ZonedDateTime

class SimpleLruCache<K, V>(private val maxSize: Int) {
    private val map = object : LinkedHashMap<K, V>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxSize
        }
    }

    @Synchronized
    fun get(key: K): V? = map[key]

    @Synchronized
    fun put(key: K, value: V): V? = map.put(key, value)
}

class CachedAstronomicalEngine(
    private val delegate: AstronomicalEngine,
    cacheSize: Int = 100
) : AstronomicalEngine {

    private val sunCache = SimpleLruCache<String, EclipticCoordinates>(cacheSize)
    private val moonCache = SimpleLruCache<String, EclipticCoordinates>(cacheSize)
    private val planetsCache = SimpleLruCache<String, Map<Graha, CelestialBodyPosition>>(cacheSize)
    private val solarTimesCache = SimpleLruCache<String, SolarTimes>(cacheSize)
    private val lunarTimesCache = SimpleLruCache<String, LunarTimes>(cacheSize)
    private val ayanamshaCache = SimpleLruCache<String, Double>(cacheSize)

    // Using a rounded timestamp string (to minute precision or second) as a cache key for time.
    // For extreme precision, seconds could be used, but since the UI ticks every second, 
    // caching at the minute or exact second level is very beneficial.
    private fun buildKey(dateTime: ZonedDateTime, vararg extras: Any): String {
        // We cache at the exact second to prevent freezing during second-by-second updates.
        val base = "${dateTime.toEpochSecond()}"
        if (extras.isEmpty()) return base
        return base + "_" + extras.joinToString("_")
    }

    override fun sunPosition(dateTime: ZonedDateTime): EclipticCoordinates {
        val key = buildKey(dateTime)
        return sunCache.get(key) ?: delegate.sunPosition(dateTime).also { sunCache.put(key, it) }
    }

    override fun moonPosition(dateTime: ZonedDateTime): EclipticCoordinates {
        val key = buildKey(dateTime)
        return moonCache.get(key) ?: delegate.moonPosition(dateTime).also { moonCache.put(key, it) }
    }

    override fun planetaryPositions(dateTime: ZonedDateTime, ayanamshaDeg: Double): Map<Graha, CelestialBodyPosition> {
        val key = buildKey(dateTime, ayanamshaDeg)
        return planetsCache.get(key) ?: delegate.planetaryPositions(dateTime, ayanamshaDeg).also { planetsCache.put(key, it) }
    }

    override fun calculateSolarTimes(date: ZonedDateTime, latitude: Double, longitude: Double): SolarTimes {
        // Solar times are usually daily, so date's start of day can be used, but we'll use exact time just in case.
        val key = buildKey(date, latitude, longitude)
        return solarTimesCache.get(key) ?: delegate.calculateSolarTimes(date, latitude, longitude).also { solarTimesCache.put(key, it) }
    }

    override fun calculateLunarTimes(date: ZonedDateTime, latitude: Double, longitude: Double): LunarTimes {
        val key = buildKey(date, latitude, longitude)
        return lunarTimesCache.get(key) ?: delegate.calculateLunarTimes(date, latitude, longitude).also { lunarTimesCache.put(key, it) }
    }

    override fun calculateLahiriAyanamsha(dateTime: ZonedDateTime): Double {
        val key = buildKey(dateTime)
        return ayanamshaCache.get(key) ?: delegate.calculateLahiriAyanamsha(dateTime).also { ayanamshaCache.put(key, it) }
    }
}
