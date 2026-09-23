import re

with open("app/src/main/java/com/example/ui/components/GalaxyWatchFaceCanvas.kt", "r") as f:
    content = f.read()

# Find the private fun DrawScope.renderComplications block
start_str = "private fun DrawScope.renderComplications("
end_str = "private fun DrawScope.renderHands("

start_idx = content.find(start_str)
end_idx = content.find(end_str)

new_render_complications = """private fun DrawScope.renderComplications(
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
    // Digital Time Readout (clean pill below center pin)
    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
    val timeStr = currentTime.format(timeFormatter)
    val timePos = Offset(center.x, center.y + radius * 0.22f)

    if (isAmbient) {
        // In Ambient AOD, draw high-contrast digital time centered with low power draw
        val aodMeasured = textMeasurer.measure(
            text = timeStr,
            style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        )
        drawText(aodMeasured, topLeft = Offset(center.x - aodMeasured.size.width / 2f, center.y + radius * 0.15f))
        return
    }

    // Active Digital Time Pill
    val timePillWidth = 80.dp.toPx()
    val timePillHeight = 22.dp.toPx()
    drawRoundRect(
        color = Color(0xCC0F172A),
        topLeft = Offset(timePos.x - timePillWidth / 2f, timePos.y - timePillHeight / 2f),
        size = Size(timePillWidth, timePillHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(11.dp.toPx())
    )
    drawRoundRect(
        color = Color(0x66FFD54F),
        topLeft = Offset(timePos.x - timePillWidth / 2f, timePos.y - timePillHeight / 2f),
        size = Size(timePillWidth, timePillHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(11.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )
    val measuredTime = textMeasurer.measure(
        text = timeStr,
        style = TextStyle(color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
    drawText(measuredTime, topLeft = Offset(timePos.x - measuredTime.size.width / 2f, timePos.y - measuredTime.size.height / 2f))

    val topPos = Offset(center.x, center.y - radius * 0.52f)
    val bottomPos = Offset(center.x, center.y + radius * 0.52f)
    val leftPos = Offset(center.x - radius * 0.54f, center.y)
    val rightPos = Offset(center.x + radius * 0.54f, center.y)
    
    drawComplicationSlot(complications.topSlot, topPos, config, dailyProfile, personalizedProfile, textMeasurer, battery, steps, currentTime)
    drawComplicationSlot(complications.bottomSlot, bottomPos, config, dailyProfile, personalizedProfile, textMeasurer, battery, steps, currentTime)
    drawComplicationSlot(complications.leftSlot, leftPos, config, dailyProfile, personalizedProfile, textMeasurer, battery, steps, currentTime)
    drawComplicationSlot(complications.rightSlot, rightPos, config, dailyProfile, personalizedProfile, textMeasurer, battery, steps, currentTime)
}

private fun DrawScope.drawComplicationSlot(
    type: ComplicationType,
    pos: Offset,
    config: WatchFaceConfiguration,
    dailyProfile: DailyVedicProfile?,
    personalizedProfile: PersonalizedProfile?,
    textMeasurer: TextMeasurer,
    battery: Int,
    steps: Int,
    currentTime: LocalTime
) {
    if (dailyProfile == null || personalizedProfile == null) return

    var textStr = ""
    var color = Color(0xFF38BDF8)
    var isBadge = false
    var badgeBgColor = Color.Transparent
    
    when (type) {
        ComplicationType.CHOGHADIYA -> {
            val choghadiya = dailyProfile.currentChoghadiya.type
            badgeBgColor = if (choghadiya.isAuspicious) Color(0xFF10B981) else Color(0xFFEF4444)
            color = Color.White
            isBadge = true
            textStr = "मुहूर्त: ${if (config.showDevanagari) choghadiya.devanagari else choghadiya.displayName}"
        }
        ComplicationType.RAHU_COUNTDOWN -> {
            val isRahu = dailyProfile.currentVedicPeriod.contains("Rahu")
            textStr = if (isRahu) "⚠ RAHU KAAL ACTIVE" else "Rahu Kaal: Safe"
            color = if (isRahu) Color(0xFFEF4444) else Color(0xFF10B981)
        }
        ComplicationType.TARA_BALA -> {
            val taraName = personalizedProfile.todayTaraBala?.name ?: "Mitra"
            textStr = "Tara: $taraName"
            color = Color(0xFF10B981)
        }
        ComplicationType.HORA_GRAHA -> {
            val horaSymbol = dailyProfile.currentHora.rulingGraha.symbol
            textStr = "Hora $horaSymbol"
            color = Color(0xFF38BDF8)
        }
        ComplicationType.TITHI_PHASE -> {
            textStr = if (config.showDevanagari) {
                dailyProfile.panchanga.tithi.devanagari
            } else {
                "${dailyProfile.panchanga.tithi.name} (${if (dailyProfile.panchanga.tithi.paksha == Paksha.SHUKLA) "Wax" else "Wan"})"
            }
            color = Color(0xFFFFD54F)
        }
        ComplicationType.JANMA_STAR -> {
            textStr = "★ ${personalizedProfile.birthNakshatra.sanskritName}"
            color = Color(0xFFFBBF24)
        }
        ComplicationType.VASTU_ELEMENT -> {
            // Derived from current Hora Lord
            val element = when (dailyProfile.currentHora.rulingGraha) {
                Graha.MANGAL, Graha.SURYA -> "Fire (Agni)"
                Graha.CHANDRA, Graha.SHUKRA -> "Water (Jala)"
                Graha.BUDH -> "Earth (Prithvi)"
                Graha.SHANI, Graha.RAHU -> "Air (Vayu)"
                else -> "Space (Akasha)"
            }
            textStr = "☤ $element"
            color = Color(0xFF8B5CF6)
        }
        ComplicationType.VASTU_COMPASS -> {
            // Live compass facing logic could be bound to sensors, using a static resonance for preview
            val currentHour = currentTime.hour
            val direction = when {
                currentHour in 6..12 -> "East (Indra)"
                currentHour in 13..18 -> "South (Yama)"
                currentHour in 19..23 -> "West (Varuna)"
                else -> "North (Kubera)"
            }
            textStr = "🧭 $direction"
            color = Color(0xFF34D399)
        }
        ComplicationType.NUMEROLOGY_PERSONAL_DAY -> {
            // Calculate a simple Numerology Daily Number
            val birthPath = config.personalizedBirthNumber
            val todayDay = dailyProfile.currentZonedDateTime.dayOfMonth
            val todayMonth = dailyProfile.currentZonedDateTime.monthValue
            val todayYear = dailyProfile.currentZonedDateTime.year
            
            // Formula: Personal Day = (Birth Month + Birth Day + Current Year + Current Month + Current Day) % 9
            // A simplified proxy using the precalculated birth path:
            val raw = birthPath + todayDay + todayMonth + todayYear
            var reduced = raw % 9
            if (reduced == 0) reduced = 9
            
            textStr = "Personal Day: $reduced"
            color = Color(0xFFF472B6)
        }
        ComplicationType.NUMEROLOGY_LUCKY_ALIGNMENT -> {
            val lp = config.personalizedBirthNumber
            val currHr = currentTime.hour
            val isAligned = (currHr % 9 == lp % 9) || (lp == 3 && currHr % 3 == 0)
            textStr = if (isAligned) "⚡ Lucky Align" else "Neutral Hour"
            color = if (isAligned) Color(0xFFFACC15) else Color(0xFF94A3B8)
        }
        ComplicationType.BATTERY_STEPS -> {
            textStr = "$battery% ⚡ | $steps 🚶"
            color = Color(0xFF94A3B8)
        }
    }
    
    val measured = textMeasurer.measure(
        text = textStr,
        style = TextStyle(color = color, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    )
    
    if (isBadge) {
        val pillWidth = measured.size.width + 24.dp.toPx()
        val pillHeight = measured.size.height + 12.dp.toPx()
        drawRoundRect(
            color = badgeBgColor.copy(alpha = 0.28f),
            topLeft = Offset(pos.x - pillWidth / 2f, pos.y - pillHeight / 2f),
            size = Size(pillWidth, pillHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(13.dp.toPx())
        )
        drawRoundRect(
            color = badgeBgColor,
            topLeft = Offset(pos.x - pillWidth / 2f, pos.y - pillHeight / 2f),
            size = Size(pillWidth, pillHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(13.dp.toPx()),
            style = Stroke(width = 1.2.dp.toPx())
        )
    }
    
    drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f))
}

"""

new_content = content[:start_idx] + new_render_complications + content[end_idx:]

with open("app/src/main/java/com/example/ui/components/GalaxyWatchFaceCanvas.kt", "w") as f:
    f.write(new_content)
