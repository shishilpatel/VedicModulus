package com.example.ui.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.model.LocationData
import com.example.data.UserPreferencesRepository
import com.example.engines.personalization.VedicPersonalizationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class VedicAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesRepository(context)
                val birthProfile = prefs.birthProfileFlow.firstOrNull()

                val location = birthProfile?.birthLocation ?: LocationData(
                    country = "India",
                    state = "Madhya Pradesh",
                    district = "Ujjain",
                    talukaOrTown = "Ujjain",
                    cityName = "Ujjain",
                    latitude = 23.1765,
                    longitude = 75.7885,
                    timezoneId = "Asia/Kolkata"
                )

                val zoneId = try {
                    ZoneId.of(location.timezoneId)
                } catch (_: Exception) {
                    ZoneId.systemDefault()
                }

                val now = ZonedDateTime.now(zoneId)
                val personalizationEngine = VedicPersonalizationEngine()
                val daily = personalizationEngine.createDailyVedicProfile(
                    now = now,
                    location = location
                )

                val dateFormatted = now.format(DateTimeFormatter.ofPattern("EEE, dd MMM"))
                val tithiText = "Tithi: ${daily.panchanga.tithi.name} (${daily.panchanga.tithi.paksha.displayName})"
                val nakshatraText = "Nakshatra: ${daily.panchanga.nakshatra.sanskritName} (Pada ${daily.panchanga.nakshatraPada})"
                val choghadiyaText = "Choghadiya: ${daily.currentChoghadiya.type.displayName} (${daily.currentChoghadiya.type.quality}) • Hora: ${daily.currentHora.rulingGraha.displayName}"

                val isAuspicious = daily.currentChoghadiya.type.isAuspicious
                val muhurtaColor = if (isAuspicious) Color.parseColor("#4CAF50") else Color.parseColor("#E57373")

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_vedic_layout).apply {
                        setTextViewText(R.id.widget_title, "Vedic Cosmic Ghadi • $dateFormatted")
                        setTextViewText(R.id.widget_tithi, tithiText)
                        setTextViewText(R.id.widget_nakshatra, nakshatraText)
                        setTextViewText(R.id.widget_muhurta, choghadiyaText)
                        setTextColor(R.id.widget_muhurta, muhurtaColor)

                        // Launch MainActivity on click
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (_: Exception) {
                // Fallback to graceful default
                for (appWidgetId in appWidgetIds) {
                    val fallbackViews = RemoteViews(context.packageName, R.layout.widget_vedic_layout).apply {
                        setTextViewText(R.id.widget_title, "Vedic Cosmic Ghadi")
                        setTextViewText(R.id.widget_tithi, "Dainik Panchanga & Auspicious Timings")
                        setTextViewText(R.id.widget_nakshatra, "Tap to open Vedic Dashboard")
                        val intent = Intent(context, MainActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(
                            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, fallbackViews)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
