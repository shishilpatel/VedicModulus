package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.model.*
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val birthDate: String, // ISO String (YYYY-MM-DD)
    val birthTime: String, // HH:mm
    val country: String,
    val state: String,
    val district: String,
    val talukaOrTown: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val ayanamsha: String = AyanamshaSystem.LAHIRI.name,
    val calendarTradition: String = CalendarTradition.AMANTA.name,
    val numerologySystem: String = NumerologySystem.CHALDEAN.name,
    val language: String = LanguageDisplay.ENGLISH.name,
    val preferredStyle: String = WatchFaceStyle.COSMIC.name,
    val isPrimary: Boolean = false,
    val relation: String = "Self",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toBirthProfile(): BirthProfile {
        val date = try { LocalDate.parse(birthDate) } catch (_: Exception) { LocalDate.of(1990, 8, 15) }
        val time = try { LocalTime.parse(birthTime) } catch (_: Exception) { LocalTime.of(7, 42) }
        val loc = LocationData(
            country = country.ifBlank { "India" },
            state = state.ifBlank { "Gujarat" },
            district = district.ifBlank { "Ahmedabad" },
            talukaOrTown = talukaOrTown.ifBlank { "Ahmedabad" },
            cityName = talukaOrTown.ifBlank { "Ahmedabad" },
            latitude = latitude,
            longitude = longitude,
            timezoneId = timezoneId.ifBlank { "Asia/Kolkata" }
        )
        val ayan = try { AyanamshaSystem.valueOf(ayanamsha) } catch (_: Exception) { AyanamshaSystem.LAHIRI }
        val trad = try { CalendarTradition.valueOf(calendarTradition) } catch (_: Exception) { CalendarTradition.AMANTA }
        val num = try { NumerologySystem.valueOf(numerologySystem) } catch (_: Exception) { NumerologySystem.CHALDEAN }
        val lang = try { LanguageDisplay.valueOf(language) } catch (_: Exception) { LanguageDisplay.ENGLISH }
        val style = try { WatchFaceStyle.valueOf(preferredStyle) } catch (_: Exception) { WatchFaceStyle.COSMIC }

        return BirthProfile(
            fullName = fullName,
            birthDate = date,
            birthTime = time,
            birthLocation = loc,
            ayanamsha = ayan,
            calendarTradition = trad,
            numerologySystem = num,
            language = lang,
            preferredStyle = style
        )
    }

    companion object {
        fun fromBirthProfile(
            profile: BirthProfile,
            relation: String = "Self",
            isPrimary: Boolean = false,
            id: Long = 0
        ): UserProfileEntity {
            return UserProfileEntity(
                id = id,
                fullName = profile.fullName,
                birthDate = profile.birthDate.toString(),
                birthTime = profile.birthTime.toString(),
                country = profile.birthLocation.country,
                state = profile.birthLocation.state,
                district = profile.birthLocation.district,
                talukaOrTown = profile.birthLocation.talukaOrTown,
                latitude = profile.birthLocation.latitude,
                longitude = profile.birthLocation.longitude,
                timezoneId = profile.birthLocation.timezoneId,
                ayanamsha = profile.ayanamsha.name,
                calendarTradition = profile.calendarTradition.name,
                numerologySystem = profile.numerologySystem.name,
                language = profile.language.name,
                preferredStyle = profile.preferredStyle.name,
                isPrimary = isPrimary,
                relation = relation
            )
        }
    }
}
