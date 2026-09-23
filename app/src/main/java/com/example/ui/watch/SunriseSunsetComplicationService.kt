package com.example.ui.watch

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType as WearComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * Exposes current Sunrise and Sunset times to system watch face complications
 * (Wear OS and Samsung Galaxy Watch series).
 */
class SunriseSunsetComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val daily = VedicComplicationDataHelper.getDailyVedicProfile(applicationContext)
        return VedicComplicationDataHelper.buildSunriseSunsetData(
            daily = daily,
            type = request.complicationType,
            context = applicationContext
        )
    }

    override fun getPreviewData(type: WearComplicationType): ComplicationData? {
        return VedicComplicationDataHelper.buildSunriseSunsetPreview(type)
    }
}
