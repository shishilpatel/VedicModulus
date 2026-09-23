private fun DrawScope.renderComplications(
    config: WatchFaceConfiguration,
    dailyProfile: DailyVedicProfile?,
    personalizedProfile: PersonalizedProfile?,
    complications: ComplicationCustomization,
    center: Offset,
    radius: Float,
    isAmbient: Boolean,
    textMeasurer: TextMeasurer,
    currentTime: LocalTime,
    battery: Int,
    steps: Int
) {
