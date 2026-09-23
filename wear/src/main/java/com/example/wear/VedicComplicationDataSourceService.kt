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
 * Exposes current Lunar Tithi and Paksha to Wear OS watch face complications.
 */
class VedicComplicationDataSourceService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationData("Shukla Dashami", "शुक्ल दशमी", "Rohini", type)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        // Retrieve the latest synced Tithi and Nakshatra from AstroDataStore
        val syncData = AstroDataStore.syncedData.value
        
        val tithi = syncData.tithi.ifEmpty { "Shukla Dashami" }
        val tithiDeva = syncData.tithiDevanagari.ifEmpty { "शुक्ल दशमी" }
        val nakshatra = syncData.nakshatra.ifEmpty { "Rohini" }

        return createComplicationData(tithi, tithiDeva, nakshatra, request.complicationType) ?: 
            NoDataComplicationData()
    }

    private fun createComplicationData(
        tithi: String,
        tithiDeva: String,
        nakshatra: String,
        type: ComplicationType
    ): ComplicationData? {
        val titleText = PlainComplicationText.Builder("Tithi").build()
        val shortText = PlainComplicationText.Builder(tithi.split(" ").lastOrNull() ?: tithi).build()
        val paksha = if (tithi.contains("Shukla", ignoreCase = true)) "Shukla" else "Krishna"
        val longText = PlainComplicationText.Builder("$tithi ($tithiDeva) • $nakshatra").build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = shortText,
                contentDescription = PlainComplicationText.Builder("Tithi: $tithi ($tithiDeva)").build()
            )
                .setTitle(PlainComplicationText.Builder(paksha).build())
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = longText,
                contentDescription = PlainComplicationText.Builder("Vedic Tithi: $tithi, Nakshatra: $nakshatra").build()
            )
                .setTitle(titleText)
                .build()
            
            else -> null
        }
    }
}

