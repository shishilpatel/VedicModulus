package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.model.DailyVedicProfile

@Entity(tableName = "daily_panchang_cache")
data class DailyPanchangEntity(
    @PrimaryKey
    val dateString: String, // e.g., "2026-09-21"
    val tithiName: String,
    val tithiSanskrit: String,
    val paksha: String,
    val nakshatraName: String,
    val nakshatraPada: Int,
    val yogaName: String,
    val karanaName: String,
    val vara: String,
    val sunrise: String,
    val sunset: String,
    val muhurtaSummary: String,
    val updatedAt: Long
) {
    companion object {
        fun fromProfile(dateStr: String, profile: DailyVedicProfile): DailyPanchangEntity {
            return DailyPanchangEntity(
                dateString = dateStr,
                tithiName = profile.panchanga.tithi.name,
                tithiSanskrit = profile.panchanga.tithi.devanagari,
                paksha = profile.panchanga.tithi.paksha.displayName,
                nakshatraName = profile.panchanga.nakshatra.sanskritName,
                nakshatraPada = profile.panchanga.nakshatraPada,
                yogaName = profile.panchanga.yoga,
                karanaName = profile.panchanga.karana,
                vara = profile.panchanga.vara,
                sunrise = profile.panchanga.sunrise.toLocalTime().toString(),
                sunset = profile.panchanga.sunset.toLocalTime().toString(),
                muhurtaSummary = profile.festivals.joinToString("; ") { it.name },
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
