import re

with open("wear/src/main/java/com/example/wear/VedicWatchFaceCanvas.kt", "r") as f:
    content = f.read()

bg_bad = """            // 1. Render Procedural Dial Background Elements
            if (!isAmbientMode) {
                renderDialElements(dialModel.elements, center, radius, textMeasurer)
            } else {
                drawCircle(
                    color = Color(0x33FFFFFF),
                    radius = radius * 0.95f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }"""

bg_good = """            // 1. Modern Procedural Gradients (AI Generated Style)
            if (!isAmbientMode) {
                val bgStyle = try { WatchFaceStyle.valueOf(syncData.watchStyle) } catch (e: Exception) { WatchFaceStyle.COSMIC }
                when (bgStyle) {
                    WatchFaceStyle.COSMIC, WatchFaceStyle.MODERN -> {
                        val brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF1E1E38), Color(0xFF0F0F1A), Color(0xFF000000)),
                            center = center,
                            radius = radius * 1.2f
                        )
                        drawCircle(brush = brush, radius = radius, center = center)
                        val glowBrush = Brush.sweepGradient(
                            colors = listOf(Color(0x00F43F5E), Color(0x558B5CF6), Color(0x993B82F6), Color(0x55F43F5E), Color(0x00F43F5E)),
                            center = center
                        )
                        drawCircle(brush = glowBrush, radius = radius * 0.9f, center = center, style = Stroke(width = 6.dp.toPx()))
                    }
                    WatchFaceStyle.VASTU, WatchFaceStyle.YANTRA, WatchFaceStyle.ROYAL -> {
                        val brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF2A1C1B), Color(0xFF140D0C), Color(0xFF000000)),
                            center = center,
                            radius = radius
                        )
                        drawCircle(brush = brush, radius = radius, center = center)
                        val auraBrush = Brush.sweepGradient(
                            colors = listOf(Color(0x33FFD54F), Color(0x88FFB300), Color(0x33FFD54F), Color(0x11FFD54F), Color(0x33FFD54F)),
                            center = center
                        )
                        drawCircle(brush = auraBrush, radius = radius * 0.95f, center = center, style = Stroke(width = 4.dp.toPx()))
                    }
                    else -> {
                        val brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF151515), Color.Black),
                            center = center,
                            radius = radius
                        )
                        drawCircle(brush = brush, radius = radius, center = center)
                    }
                }
                // Still render dial elements over the gradient
                renderDialElements(dialModel.elements, center, radius, textMeasurer)
            } else {
                drawCircle(color = Color(0x33FFFFFF), radius = radius * 0.95f, center = center, style = Stroke(width = 1.dp.toPx()))
            }"""

content = content.replace(bg_bad, bg_good)

comp_bad = """private fun DrawScope.renderComplications(
    syncData: SyncData,
    currentTime: LocalTime,
    center: Offset,
    radius: Float,
    isAmbient: Boolean,
    textMeasurer: TextMeasurer
) {
    // 1. Digital Time Readout (crystal-clear time readout)
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val timeStr = currentTime.format(timeFormatter)
    val timePos = Offset(center.x, center.y + radius * 0.22f)

    if (isAmbient) {
        // AOD minimal power mode
        val aodMeasured = textMeasurer.measure(
            text = timeStr,
            style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        )
        drawText(aodMeasured, topLeft = Offset(center.x - aodMeasured.size.width / 2f, center.y + radius * 0.15f))
        return
    }

    // Active Digital Time Pill
    val timePillWidth = 84.dp.toPx()
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

    // 2. Top: Active Choghadiya / Muhurat Badge (12 o'clock area)
    val badgeColor = if (syncData.isAuspicious) Color(0xFF10B981) else Color(0xFFEF4444)
    val muhuratName = syncData.currentMuhuratDevanagari.ifEmpty { syncData.currentMuhurat }
    val muhuratBadgeText = "मुहूर्त: $muhuratName"

    val topPos = Offset(center.x, center.y - radius * 0.52f)
    val pillWidth = 98.dp.toPx()
    val pillHeight = 26.dp.toPx()
    drawRoundRect(
        color = badgeColor.copy(alpha = 0.28f),
        topLeft = Offset(topPos.x - pillWidth / 2f, topPos.y - pillHeight / 2f),
        size = Size(pillWidth, pillHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(13.dp.toPx())
    )
    drawRoundRect(
        color = badgeColor,
        topLeft = Offset(topPos.x - pillWidth / 2f, topPos.y - pillHeight / 2f),
        size = Size(pillWidth, pillHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(13.dp.toPx()),
        style = Stroke(width = 1.2.dp.toPx())
    )
    val measuredMuhurat = textMeasurer.measure(
        text = muhuratBadgeText,
        style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
    drawText(measuredMuhurat, topLeft = Offset(topPos.x - measuredMuhurat.size.width / 2f, topPos.y - measuredMuhurat.size.height / 2f))

    // 3. Bottom: Tithi & Nakshatra
    val bottomPos = Offset(center.x, center.y + radius * 0.52f)
    val tithiText = syncData.tithiDevanagari.ifEmpty { syncData.tithi }
    val bottomText = "$tithiText • ${syncData.nakshatra}"
    val measuredBottom = textMeasurer.measure(
        text = bottomText,
        style = TextStyle(color = Color(0xFFFFD54F), fontSize = 9.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
    )
    drawText(measuredBottom, topLeft = Offset(bottomPos.x - measuredBottom.size.width / 2f, bottomPos.y - measuredBottom.size.height / 2f))

    // 4. Left Complication: Current Hora
    val leftPos = Offset(center.x - radius * 0.54f, center.y)
    val horaText = "${syncData.horaSymbol} ${syncData.currentHora}"
    val measuredHora = textMeasurer.measure(
        text = horaText,
        style = TextStyle(color = Color(0xFF67E8F9), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    )
    drawText(measuredHora, topLeft = Offset(leftPos.x - measuredHora.size.width / 2f, leftPos.y - measuredHora.size.height / 2f))

    // 5. Right Complication: Rahu Kalam or Auspicious status
    val rightPos = Offset(center.x + radius * 0.54f, center.y)
    val rightText = if (syncData.isRahuActive) "⚠ RAHU" else if (syncData.isAuspicious) "शुभ ✓" else "राहु काल"
    val rightColor = if (syncData.isRahuActive) Color(0xFFEF4444) else if (syncData.isAuspicious) Color(0xFF10B981) else Color(0xFFFFB703)
    val measuredRight = textMeasurer.measure(
        text = rightText,
        style = TextStyle(color = rightColor, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
    drawText(measuredRight, topLeft = Offset(rightPos.x - measuredRight.size.width / 2f, rightPos.y - measuredRight.size.height / 2f))
}"""

comp_good = """private fun DrawScope.renderComplications(
    syncData: SyncData,
    currentTime: LocalTime,
    center: Offset,
    radius: Float,
    isAmbient: Boolean,
    textMeasurer: TextMeasurer
) {
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val timeStr = currentTime.format(timeFormatter)

    if (isAmbient) {
        val aodMeasured = textMeasurer.measure(
            text = timeStr,
            style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        )
        drawText(aodMeasured, topLeft = Offset(center.x - aodMeasured.size.width / 2f, center.y + radius * 0.15f))
        return
    }

    // Modern Elegant Complication Box Function
    fun drawElegantComplication(pos: Offset, width: Float, height: Float, color: Color) {
        drawRoundRect(
            color = Color(0xDD0F172A),
            topLeft = Offset(pos.x - width / 2f, pos.y - height / 2f),
            size = Size(width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2f)
        )
        val borderBrush = Brush.linearGradient(
            colors = listOf(color.copy(alpha = 0.8f), color.copy(alpha = 0.1f)),
            start = Offset(pos.x - width/2, pos.y - height/2),
            end = Offset(pos.x + width/2, pos.y + height/2)
        )
        drawRoundRect(
            brush = borderBrush,
            topLeft = Offset(pos.x - width / 2f, pos.y - height / 2f),
            size = Size(width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }

    // 1. Digital Time Readout
    val timePos = Offset(center.x, center.y + radius * 0.22f)
    drawElegantComplication(timePos, 88.dp.toPx(), 24.dp.toPx(), Color(0xFFFFD54F))
    val measuredTime = textMeasurer.measure(
        text = timeStr,
        style = TextStyle(color = Color(0xFFFACC15), fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
    )
    drawText(measuredTime, topLeft = Offset(timePos.x - measuredTime.size.width / 2f, timePos.y - measuredTime.size.height / 2f))

    // 2. Top: Muhurat Badge
    val topPos = Offset(center.x, center.y - radius * 0.55f)
    val badgeColor = if (syncData.isAuspicious) Color(0xFF34D399) else Color(0xFFF87171)
    val muhuratName = syncData.currentMuhuratDevanagari.ifEmpty { syncData.currentMuhurat }
    drawElegantComplication(topPos, 106.dp.toPx(), 26.dp.toPx(), badgeColor)
    val measuredMuhurat = textMeasurer.measure(
        text = "मुहूर्त: $muhuratName",
        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
    drawText(measuredMuhurat, topLeft = Offset(topPos.x - measuredMuhurat.size.width / 2f, topPos.y - measuredMuhurat.size.height / 2f))

    // 3. Bottom: Tithi & Nakshatra
    val bottomPos = Offset(center.x, center.y + radius * 0.58f)
    val tithiText = syncData.tithiDevanagari.ifEmpty { syncData.tithi }
    drawElegantComplication(bottomPos, 110.dp.toPx(), 22.dp.toPx(), Color.White)
    val measuredBottom = textMeasurer.measure(
        text = "$tithiText • ${syncData.nakshatra}",
        style = TextStyle(color = Color(0xFFE2E8F0), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    )
    drawText(measuredBottom, topLeft = Offset(bottomPos.x - measuredBottom.size.width / 2f, bottomPos.y - measuredBottom.size.height / 2f))

    // 4. Left: Hora
    val leftPos = Offset(center.x - radius * 0.58f, center.y)
    drawElegantComplication(leftPos, 64.dp.toPx(), 22.dp.toPx(), Color(0xFF67E8F9))
    val measuredHora = textMeasurer.measure(
        text = "${syncData.horaSymbol} ${syncData.currentHora}",
        style = TextStyle(color = Color(0xFF67E8F9), fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
    drawText(measuredHora, topLeft = Offset(leftPos.x - measuredHora.size.width / 2f, leftPos.y - measuredHora.size.height / 2f))

    // 5. Right: Rahu
    val rightPos = Offset(center.x + radius * 0.58f, center.y)
    val rightColor = if (syncData.isRahuActive) Color(0xFFEF4444) else if (syncData.isAuspicious) Color(0xFF10B981) else Color(0xFFFFB703)
    drawElegantComplication(rightPos, 64.dp.toPx(), 22.dp.toPx(), rightColor)
    val measuredRight = textMeasurer.measure(
        text = if (syncData.isRahuActive) "⚠ RAHU" else if (syncData.isAuspicious) "शुभ ✓" else "राहु",
        style = TextStyle(color = rightColor, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
    drawText(measuredRight, topLeft = Offset(rightPos.x - measuredRight.size.width / 2f, rightPos.y - measuredRight.size.height / 2f))
}"""

content = content.replace(comp_bad, comp_good)

with open("wear/src/main/java/com/example/wear/VedicWatchFaceCanvas.kt", "w") as f:
    f.write(content)
