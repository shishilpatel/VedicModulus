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
 * Exposes current planetary Hora ruler and astrological symbol to Wear OS complications.
 */
class VedicHoraComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationData("Surya", "☉", type)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val syncData = AstroDataStore.syncedData.value
        val graha = syncData.currentHora.ifEmpty { "Surya" }
        val symbol = syncData.horaSymbol.ifEmpty { "☉" }

        return createComplicationData(graha, symbol, request.complicationType) ?: NoDataComplicationData()
    }

    private fun createComplicationData(
        graha: String,
        symbol: String,
        type: ComplicationType
    ): ComplicationData? {
        val shortText = PlainComplicationText.Builder("$symbol $graha").build()
        val titleText = PlainComplicationText.Builder("Hora").build()
        val longText = PlainComplicationText.Builder("$symbol $graha Hora • Active Lord").build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = shortText,
                contentDescription = PlainComplicationText.Builder("Planetary Hora: $graha").build()
            )
                .setTitle(titleText)
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = longText,
                contentDescription = PlainComplicationText.Builder("Planetary Hora: $graha").build()
            )
                .setTitle(titleText)
                .build()

            else -> null
        }
    }
}
