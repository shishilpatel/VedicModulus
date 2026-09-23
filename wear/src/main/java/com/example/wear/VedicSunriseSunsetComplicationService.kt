package com.example.wear

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * Exposes current Sunrise and Sunset times to Wear OS watch face complications.
 */
class VedicSunriseSunsetComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationData("06:00 AM", "06:00 PM", type)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val syncData = AstroDataStore.syncedData.value
        val sunrise = syncData.sunrise.ifEmpty { "06:00 AM" }
        val sunset = syncData.sunset.ifEmpty { "06:00 PM" }

        return createComplicationData(sunrise, sunset, request.complicationType) ?: NoDataComplicationData()
    }

    private fun createComplicationData(
        sunrise: String,
        sunset: String,
        type: ComplicationType
    ): ComplicationData? {
        val titleText = PlainComplicationText.Builder("Sun").build()
        val mainText = PlainComplicationText.Builder("Rise: $sunrise • Set: $sunset").build()
        val shortText = PlainComplicationText.Builder("↑ $sunrise").build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = shortText,
                contentDescription = PlainComplicationText.Builder("Sunrise at $sunrise, Sunset at $sunset").build()
            )
                .setTitle(titleText)
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = mainText,
                contentDescription = PlainComplicationText.Builder("Surya Udaya: $sunrise, Surya Astha: $sunset").build()
            )
                .setTitle(PlainComplicationText.Builder("Surya Udaya/Astha").build())
                .build()

            else -> null
        }
    }
}
