package com.example.engines.astronomy

object AstronomicalEngineProvider {
    private val baseEngine: AstronomicalEngine by lazy {
        MeeusAstronomicalEngine()
    }

    val instance: AstronomicalEngine by lazy {
        CachedAstronomicalEngine(baseEngine, cacheSize = 500)
    }
}
