package com.example.ui.watch

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType as WearComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.example.MainActivity
import com.example.core.model.DailyVedicProfile
import com.example.core.model.LocationData
import com.example.core.model.Paksha
import com.example.data.UserPreferencesRepository
import com.example.engines.personalization.VedicPersonalizationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object VedicComplicationDataHelper {
    private const val TAG = "VedicComplicationHelper"

    // In-memory cache to prevent redundant Swiss Ephemeris calculations across concurrent complication requests
    private var cachedProfile: DailyVedicProfile? = null
    private var cachedProfileTimestamp: Long = 0L
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    suspend fun getDailyVedicProfile(context: Context): DailyVedicProfile = withContext(Dispatchers.IO) {
        val nowMs = System.currentTimeMillis()
        val currentCached = cachedProfile
        if (currentCached != null && (nowMs - cachedProfileTimestamp) < 60_000L) {
            return@withContext currentCached
        }

        val location = try {
            val prefs = UserPreferencesRepository(context)
            val birthProfile = prefs.birthProfileFlow.firstOrNull()
            birthProfile?.birthLocation ?: defaultLocation()
        } catch (e: Exception) {
            defaultLocation()
        }

        val zoneId = try {
            ZoneId.of(location.timezoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }

        val now = ZonedDateTime.now(zoneId)
        val engine = VedicPersonalizationEngine()
        val computed = engine.createDailyVedicProfile(now = now, location = location)
        cachedProfile = computed
        cachedProfileTimestamp = nowMs
        return@withContext computed
    }

    private fun defaultLocation() = LocationData(
        country = "India",
        state = "Madhya Pradesh",
        district = "Ujjain",
        talukaOrTown = "Ujjain",
        cityName = "Ujjain",
        latitude = 23.1765,
        longitude = 75.7885,
        timezoneId = "Asia/Kolkata"
    )

    fun createTapIntent(context: Context, tabName: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_TARGET_TAB", tabName)
        }
        return PendingIntent.getActivity(
            context,
            tabName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // --- SUNRISE / SUNSET COMPLICATION DATA ---
    fun buildSunriseSunsetData(
        daily: DailyVedicProfile,
        type: WearComplicationType,
        context: Context
    ): ComplicationData? {
        val now = daily.currentZonedDateTime
        val sunrise = daily.panchanga.sunrise
        val sunset = daily.panchanga.sunset
        val sunriseStr = sunrise.format(timeFormatter)
        val sunsetStr = sunset.format(timeFormatter)

        val isBeforeSunrise = now.isBefore(sunrise)
        val isBeforeSunset = now.isBefore(sunset)

        val nextEventLabel: String
        val nextEventTime: String
        val nextEventDesc: String

        if (isBeforeSunrise) {
            nextEventLabel = "Rise"
            nextEventTime = "↑ $sunriseStr"
            nextEventDesc = "Sunrise at $sunriseStr"
        } else if (isBeforeSunset) {
            nextEventLabel = "Set"
            nextEventTime = "↓ $sunsetStr"
            nextEventDesc = "Sunset at $sunsetStr"
        } else {
            nextEventLabel = "Rise"
            nextEventTime = "↑ $sunriseStr"
            nextEventDesc = "Next Sunrise at $sunriseStr"
        }

        val tapIntent = createTapIntent(context, "Panchanga")

        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(nextEventTime).build(),
                    contentDescription = PlainComplicationText.Builder(nextEventDesc).build()
                )
                    .setTitle(PlainComplicationText.Builder(nextEventLabel).build())
                    .setTapAction(tapIntent)
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Rise: $sunriseStr • Set: $sunsetStr").build(),
                    contentDescription = PlainComplicationText.Builder("Sunrise at $sunriseStr, Sunset at $sunsetStr").build()
                )
                    .setTitle(PlainComplicationText.Builder("Surya Udaya / Astha").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            else -> null
        }
    }

    fun buildSunriseSunsetPreview(type: WearComplicationType): ComplicationData? {
        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("↑ 06:15 AM").build(),
                    contentDescription = PlainComplicationText.Builder("Sunrise preview at 06:15 AM").build()
                )
                    .setTitle(PlainComplicationText.Builder("Rise").build())
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Rise: 06:15 AM • Set: 06:45 PM").build(),
                    contentDescription = PlainComplicationText.Builder("Surya Udaya / Astha preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("Surya Udaya / Astha").build())
                    .build()
            }
            else -> null
        }
    }

    // --- LUNAR TITHI COMPLICATION DATA ---
    fun buildTithiData(
        daily: DailyVedicProfile,
        type: WearComplicationType,
        context: Context
    ): ComplicationData? {
        val tithi = daily.panchanga.tithi
        val pakshaName = if (tithi.paksha == Paksha.SHUKLA) "Shukla" else "Krishna"
        val pakshaAbbr = if (tithi.paksha == Paksha.SHUKLA) "S" else "K"
        val shortName = tithi.name
        val devanagari = tithi.devanagari

        val tapIntent = createTapIntent(context, "Panchanga")

        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(shortName).build(),
                    contentDescription = PlainComplicationText.Builder("$pakshaName $shortName ($devanagari)").build()
                )
                    .setTitle(PlainComplicationText.Builder(pakshaAbbr).build())
                    .setTapAction(tapIntent)
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("$pakshaName $shortName • $devanagari").build(),
                    contentDescription = PlainComplicationText.Builder("Tithi: $pakshaName $shortName ($devanagari)").build()
                )
                    .setTitle(PlainComplicationText.Builder("Lunar Tithi").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            else -> null
        }
    }

    fun buildTithiPreview(type: WearComplicationType): ComplicationData? {
        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Ekadashi").build(),
                    contentDescription = PlainComplicationText.Builder("Shukla Ekadashi preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("Shukla").build())
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Shukla Ekadashi • एकादशी").build(),
                    contentDescription = PlainComplicationText.Builder("Shukla Ekadashi preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("Lunar Tithi").build())
                    .build()
            }
            else -> null
        }
    }

    // --- CHOGHADIYA COMPLICATION DATA ---
    fun buildChoghadiyaData(
        daily: DailyVedicProfile,
        type: WearComplicationType,
        context: Context
    ): ComplicationData? {
        val choghadiya = daily.currentChoghadiya.type
        val endStr = daily.currentChoghadiya.endTime.format(timeFormatter)
        val name = choghadiya.displayName
        val deva = choghadiya.devanagari
        val isAuspicious = choghadiya.isAuspicious

        val tapIntent = createTapIntent(context, "Choghadiya & Hora")

        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(name).build(),
                    contentDescription = PlainComplicationText.Builder("Choghadiya: $name ($deva) - ${if (isAuspicious) "Auspicious" else "Inauspicious"}").build()
                )
                    .setTitle(PlainComplicationText.Builder(deva).build())
                    .setTapAction(tapIntent)
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                val status = if (isAuspicious) "Auspicious" else "Inauspicious"
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("$name ($deva) • $status till $endStr").build(),
                    contentDescription = PlainComplicationText.Builder("$name $status until $endStr").build()
                )
                    .setTitle(PlainComplicationText.Builder("Choghadiya").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            else -> null
        }
    }

    fun buildChoghadiyaPreview(type: WearComplicationType): ComplicationData? {
        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Shubh").build(),
                    contentDescription = PlainComplicationText.Builder("Shubh Muhurta preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("शुभ").build())
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Shubh (शुभ) • Auspicious till 10:30 AM").build(),
                    contentDescription = PlainComplicationText.Builder("Shubh Muhurta preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("Choghadiya").build())
                    .build()
            }
            else -> null
        }
    }

    // --- HORA COMPLICATION DATA ---
    fun buildHoraData(
        daily: DailyVedicProfile,
        type: WearComplicationType,
        context: Context
    ): ComplicationData? {
        val graha = daily.currentHora.rulingGraha
        val symbol = graha.symbol
        val name = graha.displayName
        val sanskrit = graha.sanskritName

        val tapIntent = createTapIntent(context, "Choghadiya & Hora")

        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("$symbol $name").build(),
                    contentDescription = PlainComplicationText.Builder("Hora of $name ($sanskrit)").build()
                )
                    .setTitle(PlainComplicationText.Builder("Hora").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("$symbol $name ($sanskrit) • Active Planetary Ruler").build(),
                    contentDescription = PlainComplicationText.Builder("Planetary Hora: $name ($sanskrit)").build()
                )
                    .setTitle(PlainComplicationText.Builder("Planetary Hora").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            else -> null
        }
    }

    fun buildHoraPreview(type: WearComplicationType): ComplicationData? {
        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("☉ Surya").build(),
                    contentDescription = PlainComplicationText.Builder("Surya Hora preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("Hora").build())
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("☉ Surya (Sun) • Leadership & Administration").build(),
                    contentDescription = PlainComplicationText.Builder("Surya Hora preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("Planetary Hora").build())
                    .build()
            }
            else -> null
        }
    }

    // --- COSMIC SCORE COMPLICATION DATA ---
    fun buildCosmicScoreData(
        daily: DailyVedicProfile,
        type: WearComplicationType,
        context: Context
    ): ComplicationData? {
        val score = daily.cosmicScore?.overall ?: 85
        val period = daily.currentVedicPeriod

        val tapIntent = createTapIntent(context, "Dashboard")

        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("$score%").build(),
                    contentDescription = PlainComplicationText.Builder("Cosmic harmony score: $score percent").build()
                )
                    .setTitle(PlainComplicationText.Builder("Cosmic").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Score: $score% • $period").build(),
                    contentDescription = PlainComplicationText.Builder("Cosmic Score: $score%, $period").build()
                )
                    .setTitle(PlainComplicationText.Builder("Cosmic Harmony").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            else -> null
        }
    }

    fun buildCosmicScorePreview(type: WearComplicationType): ComplicationData? {
        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("88%").build(),
                    contentDescription = PlainComplicationText.Builder("Cosmic harmony score preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("Cosmic").build())
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Score: 88% • Auspicious Period").build(),
                    contentDescription = PlainComplicationText.Builder("Cosmic harmony score preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("Cosmic Harmony").build())
                    .build()
            }
            else -> null
        }
    }

    // --- REAL-TIME PANCHANG STREAM COMPLICATION DATA ---
    fun buildPanchangData(
        daily: DailyVedicProfile,
        type: WearComplicationType,
        context: Context
    ): ComplicationData? {
        val tithi = daily.panchanga.tithi.name
        val paksha = if (daily.panchanga.tithi.paksha == Paksha.SHUKLA) "Shukla" else "Krishna"
        val nakshatra = daily.panchanga.nakshatra.sanskritName
        val yoga = daily.panchanga.yoga
        val karana = daily.panchanga.karana
        val vara = daily.panchanga.vara

        val tapIntent = createTapIntent(context, "Panchanga")

        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(tithi).build(),
                    contentDescription = PlainComplicationText.Builder("Real-Time Panchang: $paksha $tithi, $nakshatra, $yoga Yoga").build()
                )
                    .setTitle(PlainComplicationText.Builder(nakshatra).build())
                    .setTapAction(tapIntent)
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("$paksha $tithi • $nakshatra • $yoga Yoga • $karana Karana • $vara").build(),
                    contentDescription = PlainComplicationText.Builder("Vedic Panchang: $paksha $tithi, Nakshatra: $nakshatra, Yoga: $yoga, Karana: $karana, Vara: $vara").build()
                )
                    .setTitle(PlainComplicationText.Builder("Real-Time Panchang").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            WearComplicationType.RANGED_VALUE -> {
                androidx.wear.watchface.complications.data.RangedValueComplicationData.Builder(
                    value = 5f,
                    min = 0f,
                    max = 5f,
                    contentDescription = PlainComplicationText.Builder("Panchanga 5 Limbs Synchronized").build()
                )
                    .setText(PlainComplicationText.Builder("$tithi • $yoga").build())
                    .setTitle(PlainComplicationText.Builder("5 Limbs").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            else -> null
        }
    }

    fun buildPanchangPreview(type: WearComplicationType): ComplicationData? {
        return when (type) {
            WearComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Ekadashi").build(),
                    contentDescription = PlainComplicationText.Builder("Panchang preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("Rohini").build())
                    .build()
            }
            WearComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("Shukla Ekadashi • Rohini • Siddha • Bava").build(),
                    contentDescription = PlainComplicationText.Builder("Panchang preview").build()
                )
                    .setTitle(PlainComplicationText.Builder("Real-Time Panchang").build())
                    .build()
            }
            WearComplicationType.RANGED_VALUE -> {
                androidx.wear.watchface.complications.data.RangedValueComplicationData.Builder(
                    value = 5f,
                    min = 0f,
                    max = 5f,
                    contentDescription = PlainComplicationText.Builder("Panchang preview").build()
                )
                    .setText(PlainComplicationText.Builder("Ekadashi • Siddha").build())
                    .setTitle(PlainComplicationText.Builder("5 Limbs").build())
                    .build()
            }
            else -> null
        }
    }

    /**
     * Updates all system complications registered across the Wear OS system.
     */
    fun requestComplicationUpdates(context: Context) {
        try {
            val services = listOf(
                SunriseSunsetComplicationService::class.java,
                TithiComplicationService::class.java,
                ChoghadiyaComplicationService::class.java,
                HoraComplicationService::class.java,
                CosmicScoreComplicationService::class.java,
                PanchangComplicationService::class.java
            )
            for (serviceClass in services) {
                try {
                    val componentName = ComponentName(context, serviceClass)
                    val requester = ComplicationDataSourceUpdateRequester.create(context, componentName)
                    requester.requestUpdateAll()
                } catch (e: Exception) {
                    Log.d(TAG, "Could not request update for ${serviceClass.simpleName}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "requestComplicationUpdates error: ${e.message}")
        }
    }
}
