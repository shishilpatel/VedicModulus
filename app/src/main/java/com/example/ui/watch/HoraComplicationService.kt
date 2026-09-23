package com.example.ui.watch

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType as WearComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * Complication provider service for displaying the active Vedic Planetary Hora (e.g. ☉ Surya Hora, ☽ Chandra Hora)
 * on Wear OS smart watchfaces and widgets.
 */
class HoraComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val daily = VedicComplicationDataHelper.getDailyVedicProfile(applicationContext)
        return VedicComplicationDataHelper.buildHoraData(
            daily = daily,
            type = request.complicationType,
            context = applicationContext
        )
    }

    override fun getPreviewData(type: WearComplicationType): ComplicationData? {
        return VedicComplicationDataHelper.buildHoraPreview(type)
    }
}

