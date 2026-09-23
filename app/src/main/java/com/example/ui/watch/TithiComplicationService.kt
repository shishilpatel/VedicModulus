package com.example.ui.watch

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType as WearComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * Complication provider service for displaying the active lunar Tithi, Paksha, and Devanagari details
 * to system watch face complications (Wear OS and Samsung Galaxy Watch series).
 */
class TithiComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val daily = VedicComplicationDataHelper.getDailyVedicProfile(applicationContext)
        return VedicComplicationDataHelper.buildTithiData(
            daily = daily,
            type = request.complicationType,
            context = applicationContext
        )
    }

    override fun getPreviewData(type: WearComplicationType): ComplicationData? {
        return VedicComplicationDataHelper.buildTithiPreview(type)
    }
}

