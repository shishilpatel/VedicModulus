package com.example.wear

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * Custom ComplicationDataSource that streams real-time Panchang data (Tithi, Nakshatra, Yoga, Karana, Vara)
 * to system-provided Wear OS watch face slots.
 */
class VedicPanchangComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createPanchangData(
            tithi = "Shukla Dashami",
            tithiDeva = "शुक्ल दशमी",
            nakshatra = "Rohini",
            yoga = "Siddha",
            karana = "Bava",
            vara = "Ravivara",
            type = type
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val syncData = AstroDataStore.syncedData.value

        val tithi = syncData.tithi.ifEmpty { "Shukla Dashami" }
        val tithiDeva = syncData.tithiDevanagari.ifEmpty { "शुक्ल दशमी" }
        val nakshatra = syncData.nakshatra.ifEmpty { "Rohini" }
        val yoga = syncData.currentYoga.ifEmpty { "Siddha" }
        val karana = syncData.currentKarana.ifEmpty { "Bava" }
        val vara = syncData.currentVara.ifEmpty { "Ravivara" }

        return createPanchangData(tithi, tithiDeva, nakshatra, yoga, karana, vara, request.complicationType)
            ?: NoDataComplicationData()
    }

    private fun createPanchangData(
        tithi: String,
        tithiDeva: String,
        nakshatra: String,
        yoga: String,
        karana: String,
        vara: String,
        type: ComplicationType
    ): ComplicationData? {
        val titleText = PlainComplicationText.Builder("Panchang").build()
        val shortTitle = PlainComplicationText.Builder(nakshatra).build()
        val shortContent = tithi.split(" ").lastOrNull() ?: tithi
        val longContent = "$tithi ($tithiDeva) • $nakshatra • $yoga Yoga • $karana Karana"

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(shortContent).build(),
                contentDescription = PlainComplicationText.Builder("Panchang: $tithi, $nakshatra, $yoga Yoga").build()
            )
                .setTitle(shortTitle)
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(longContent).build(),
                contentDescription = PlainComplicationText.Builder("Full Vedic Panchang Stream: Tithi $tithi, Nakshatra $nakshatra, Yoga $yoga, Karana $karana, Vara $vara").build()
            )
                .setTitle(titleText)
                .build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 5f,
                min = 0f,
                max = 5f,
                contentDescription = PlainComplicationText.Builder("Panchanga 5 Limbs Synchronized").build()
            )
                .setText(PlainComplicationText.Builder("$tithi • $yoga").build())
                .setTitle(PlainComplicationText.Builder("5 Limbs").build())
                .build()

            else -> null
        }
    }
}
