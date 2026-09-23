package com.example.engines.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.FileProvider
import com.example.core.model.DailyVedicProfile
import java.io.File
import java.io.FileOutputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Exporter utility to synchronize and export Vedic Panchang timings,
 * auspicious Muhurtas, and festivals directly into the Android / Google Calendar.
 */
object PanchangCalendarExporter {

    private val ICS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

    /**
     * Launches the native Android Calendar event creation screen with pre-filled
     * Vedic timing details, description, and exact start/end timestamps.
     */
    fun addEventToCalendar(
        context: Context,
        title: String,
        description: String,
        location: String,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.toInstant().toEpochMilli())
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.toInstant().toEpochMilli())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Generates a standard .ics iCalendar file containing all auspicious muhurtas,
     * Choghadiya windows, and festivals for the specified Vedic daily profile,
     * then opens the Android share sheet.
     */
    fun exportDayPanchangToIcs(context: Context, profile: DailyVedicProfile) {
        val calendarDir = File(context.cacheDir, "exports")
        if (!calendarDir.exists()) calendarDir.mkdirs()

        val dateStr = profile.currentZonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val icsFile = File(calendarDir, "panchang_$dateStr.ics")

        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCALENDAR")
        sb.appendLine("VERSION:2.0")
        sb.appendLine("PRODID:-//Vedic Astro Life//Panchang Calendar//EN")
        sb.appendLine("CALSCALE:GREGORIAN")
        sb.appendLine("METHOD:PUBLISH")

        val nowUtc = ZonedDateTime.now(java.time.ZoneOffset.UTC).format(ICS_DATE_FORMAT)

        // 1. Brahma Muhurta Event
        appendIcsEvent(
            sb = sb,
            uid = "brahma_muhurta_$dateStr@vedicastro",
            nowUtc = nowUtc,
            summary = "🕉 Brahma Muhurta (Auspicious Meditation Window)",
            description = "Sacred period for meditation, mantra japa, and spiritual study before sunrise. Tithi: ${profile.panchanga.tithi.name}, Nakshatra: ${profile.panchanga.nakshatra.name}.",
            location = "Sacred Space",
            startTime = profile.brahmaMuhurta.startTime,
            endTime = profile.brahmaMuhurta.endTime
        )

        // 2. Abhijit Muhurta (if available)
        profile.abhijitMuhurta?.let { abhijit ->
            appendIcsEvent(
                sb = sb,
                uid = "abhijit_muhurta_$dateStr@vedicastro",
                nowUtc = nowUtc,
                summary = "⭐ Abhijit Muhurta (Universal Victory Timing)",
                description = "Highly auspicious window around midday for commencing important undertakings and travels.",
                location = "Sacred Space",
                startTime = abhijit.startTime,
                endTime = abhijit.endTime
            )
        }

        // 3. Rahu Kaal (Inauspicious Window)
        appendIcsEvent(
            sb = sb,
            uid = "rahu_kaal_$dateStr@vedicastro",
            nowUtc = nowUtc,
            summary = "⚠️ Rahu Kaal (Inauspicious Period)",
            description = "Period ruled by Rahu. Traditional Jyotish advises avoiding commencement of new ventures or travel during this period.",
            location = "Sacred Space",
            startTime = profile.rahuKalam.startTime,
            endTime = profile.rahuKalam.endTime
        )

        // 4. Festivals & Vrats
        profile.festivals.forEachIndexed { index, festival ->
            appendIcsEvent(
                sb = sb,
                uid = "festival_${dateStr}_$index@vedicastro",
                nowUtc = nowUtc,
                summary = "🎉 ${festival.name} (Vedic Festival)",
                description = festival.description,
                location = "Sacred Space",
                startTime = profile.panchanga.sunrise,
                endTime = profile.panchanga.sunset
            )
        }

        sb.appendLine("END:VCALENDAR")

        FileOutputStream(icsFile).use { it.write(sb.toString().toByteArray()) }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            icsFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/calendar"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Vedic Panchang & Auspicious Timings - $dateStr")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(shareIntent, "Add / Share Vedic Calendar Timings").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun appendIcsEvent(
        sb: StringBuilder,
        uid: String,
        nowUtc: String,
        summary: String,
        description: String,
        location: String,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ) {
        val startUtc = startTime.withZoneSameInstant(java.time.ZoneOffset.UTC).format(ICS_DATE_FORMAT)
        val endUtc = endTime.withZoneSameInstant(java.time.ZoneOffset.UTC).format(ICS_DATE_FORMAT)

        sb.appendLine("BEGIN:VEVENT")
        sb.appendLine("UID:$uid")
        sb.appendLine("DTSTAMP:$nowUtc")
        sb.appendLine("DTSTART:$startUtc")
        sb.appendLine("DTEND:$endUtc")
        sb.appendLine("SUMMARY:$summary")
        sb.appendLine("DESCRIPTION:$description")
        sb.appendLine("LOCATION:$location")
        sb.appendLine("STATUS:CONFIRMED")
        sb.appendLine("END:VEVENT")
    }
}
