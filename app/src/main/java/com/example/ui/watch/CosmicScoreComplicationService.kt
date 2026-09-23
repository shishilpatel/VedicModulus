package com.example.ui.watch

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType as WearComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * Complication provider service for displaying the personalized cosmic harmony score
 * on Wear OS watchfaces.
 */
class CosmicScoreComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val daily = VedicComplicationDataHelper.getDailyVedicProfile(applicationContext)
        return VedicComplicationDataHelper.buildCosmicScoreData(
            daily = daily,
            type = request.complicationType,
            context = applicationContext
        )
    }

    override fun getPreviewData(type: WearComplicationType): ComplicationData? {
        return VedicComplicationDataHelper.buildCosmicScorePreview(type)
    }
}

