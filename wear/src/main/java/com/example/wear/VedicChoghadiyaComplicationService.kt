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
 * Exposes real-time Choghadiya window (Shubh, Amrit, Labh, Kaal, etc.) with auspiciousness status
 * to Wear OS watch face complications.
 */
class VedicChoghadiyaComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationData("Amrit", "अमृत", true, "10:30 AM", type)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val syncData = AstroDataStore.syncedData.value
        val name = syncData.currentMuhurat.ifEmpty { "Amrit" }
        val deva = syncData.currentMuhuratDevanagari.ifEmpty { "अमृत" }
        val isAuspicious = syncData.isAuspicious
        val endTime = syncData.muhuratEndTime

        return createComplicationData(name, deva, isAuspicious, endTime, request.complicationType) ?: NoDataComplicationData()
    }

    private fun createComplicationData(
        name: String,
        deva: String,
        isAuspicious: Boolean,
        endTime: String,
        type: ComplicationType
    ): ComplicationData? {
        val status = if (isAuspicious) "Auspicious" else "Inauspicious"
        val shortText = PlainComplicationText.Builder(name).build()
        val titleText = PlainComplicationText.Builder(deva).build()
        val longText = if (endTime.isNotEmpty()) {
            PlainComplicationText.Builder("$name ($deva) • $status till $endTime").build()
        } else {
            PlainComplicationText.Builder("$name ($deva) • $status").build()
        }

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = shortText,
                contentDescription = PlainComplicationText.Builder("Choghadiya: $name ($deva) - $status").build()
            )
                .setTitle(titleText)
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = longText,
                contentDescription = PlainComplicationText.Builder("Choghadiya: $name ($deva) - $status").build()
            )
                .setTitle(PlainComplicationText.Builder("Choghadiya").build())
                .build()

            else -> null
        }
    }
}
