package com.example.ui.watch

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType as WearComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * Complication provider service for displaying the active Vedic Choghadiya window (e.g. अमृत Amrit, शुभ Shubh, काल Kaal)
 * with auspiciousness status on Wear OS watchfaces.
 */
class ChoghadiyaComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val daily = VedicComplicationDataHelper.getDailyVedicProfile(applicationContext)
        return VedicComplicationDataHelper.buildChoghadiyaData(
            daily = daily,
            type = request.complicationType,
            context = applicationContext
        )
    }

    override fun getPreviewData(type: WearComplicationType): ComplicationData? {
        return VedicComplicationDataHelper.buildChoghadiyaPreview(type)
    }
}

