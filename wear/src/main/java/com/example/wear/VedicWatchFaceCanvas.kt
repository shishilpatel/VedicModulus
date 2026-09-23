package com.example.wear

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

private val sharedWearCanvasPath = Path()
private val timeFmtShort = DateTimeFormatter.ofPattern("hh:mm a")

@OptIn(ExperimentalTextApi::class)
@Composable
fun VedicWatchFaceCanvas(
    syncData: SyncData,
    currentTime: LocalTime,
    modifier: Modifier = Modifier,
    isAmbientMode: Boolean = false,
    isLowPowerMode: Boolean = syncData.isLowPowerModeActive,
    onDialTap: (() -> Unit)? = null
) {
    val generator = remember { WatchFaceGenerator() }
    val style = remember(syncData.watchStyle) {
        try {
            WatchFaceStyle.valueOf(syncData.watchStyle)
        } catch (e: Exception) {
            WatchFaceStyle.COSMIC
        }
    }

    val dialModel = remember(style, syncData) {
        generator.generateDialModel(
            style = style,
            syncData = syncData
        )
    }

    val textMeasurer = rememberTextMeasurer()
    val wearTextCache = remember { mutableMapOf<String, TextLayoutResult>() }
    val doubleBuffer = remember { VedicDialDoubleBuffer() }

    DisposableEffect(Unit) {
        onDispose {
            doubleBuffer.release()
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val customBgBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = syncData.lastSyncTimestamp) {
        value = if (!syncData.isSafeModeEnabled && !isLowPowerMode) {
            withContext(VedicCalculationDispatcher.dispatcher) {
                try {
                    val bgFile = java.io.File(context.filesDir, "watchface_custom_bg.png")
                    if (bgFile.exists()) {
                        android.graphics.BitmapFactory.decodeFile(bgFile.absolutePath)?.asImageBitmap()
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            null
        }
    }

    val canvasBorderColor = when {
        syncData.isSafeModeEnabled -> Color(0xFF3B82F6)
        syncData.isThermalThrottled -> Color(0xFFEF4444)
        isLowPowerMode -> Color(0xFF10B981)
        else -> Color(0xFF1E293B)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(Color.Black)
            .border(2.dp, canvasBorderColor, CircleShape)
            .testTag("vedic_watch_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            if (isLowPowerMode) {
                WearFrameDropMonitor.markPhase("LOW_POWER_TICK")
            }

            if (syncData.isSafeModeEnabled) {
                // --- HARDWARE CANVAS SAFE MODE (Zero complex calculations, maximum performance) ---
                WearFrameDropMonitor.markPhase("SAFE_MODE_RENDER")
                renderSafeModeDial(
                    syncData = syncData,
                    currentTime = currentTime,
                    center = center,
                    radius = radius,
                    isAmbient = isAmbientMode,
                    textMeasurer = textMeasurer,
                    textCache = wearTextCache
                )
            } else {
                // --- STANDARD VEDIC CELESTIAL RENDERING (DOUBLE-BUFFERED) ---
                // 1. Double-Buffering: Render Complex Vedic Dial Geometry to Offscreen ImageBitmap once per tick/change
                if (!isAmbientMode) {
                    WearFrameDropMonitor.markPhase("DOUBLE_BUFFER_BLIT")
                    val cachedDialBitmap = doubleBuffer.getOrRenderDialBackground(
                        width = size.width.toInt(),
                        height = size.height.toInt(),
                        syncData = syncData,
                        dialModel = dialModel,
                        customBgBitmap = customBgBitmap,
                        textMeasurer = textMeasurer,
                        wearTextCache = wearTextCache
                    )
                    if (cachedDialBitmap != null) {
                        drawImage(
                            image = cachedDialBitmap,
                            dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                        )
                    }
                } else {
                    drawCircle(color = Color(0x33FFFFFF), radius = radius * 0.95f, center = center, style = Stroke(width = 1.dp.toPx()))
                }

                // 2. Render Vedic Complications Subdials (Top Muhurat, Digital Time, Bottom Tithi, Left Hora, Right Status)
                WearFrameDropMonitor.markPhase("COMPLICATIONS_RENDER")
                renderComplications(
                    syncData = syncData,
                    currentTime = currentTime,
                    center = center,
                    radius = radius,
                    isAmbient = isAmbientMode,
                    textMeasurer = textMeasurer,
                    textCache = wearTextCache
                )

                // 3. Render Watch Hands (Hours, Minutes, Seconds) - Omits second hand in Low-Power Mode
                WearFrameDropMonitor.markPhase("HANDS_RENDER")
                renderHands(
                    currentTime = currentTime,
                    center = center,
                    radius = radius,
                    dialModel = dialModel,
                    isAmbient = isAmbientMode,
                    isLowPower = isLowPowerMode
                )

                // 4. Golden Center Cap Pin
                drawCircle(
                    color = if (isAmbientMode) Color.White else Color(dialModel.centerCapColorHex),
                    radius = 4.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color.Black,
                    radius = 1.5.dp.toPx(),
                    center = center
                )
            }
            WearFrameDropMonitor.markPhase("FRAME_COMPLETE")
        }
    }
}

/**
 * Double-buffering offscreen render cache for Vedic Watch Face complex sacred geometry,
 * radial/sweep gradient shaders, and astronomical dials.
 * Renders once to a background Bitmap buffer and returns an ImageBitmap for single-blit rendering.
 */
class VedicDialDoubleBuffer {
    private var cachedBitmap: android.graphics.Bitmap? = null
    private var cachedImageBitmap: ImageBitmap? = null
    private var cachedWidth: Int = 0
    private var cachedHeight: Int = 0
    private var cachedKey: String = ""

    fun getOrRenderDialBackground(
        width: Int,
        height: Int,
        syncData: SyncData,
        dialModel: ProceduralWatchFaceModel,
        customBgBitmap: ImageBitmap?,
        textMeasurer: TextMeasurer,
        wearTextCache: MutableMap<String, TextLayoutResult>
    ): ImageBitmap? {
        if (width <= 0 || height <= 0) return null

        val cacheKey = "${syncData.watchStyle}_${syncData.primaryColorHex}_${syncData.accentColorHex}_${dialModel.elements.size}_${customBgBitmap?.hashCode() ?: 0}_${syncData.lastSyncTimestamp}"

        if (cachedBitmap != null && cachedImageBitmap != null && cachedWidth == width && cachedHeight == height && cachedKey == cacheKey) {
            return cachedImageBitmap
        }

        WearFrameDropMonitor.markPhase("DOUBLE_BUFFER_BACKGROUND_GEN")

        var bmp = cachedBitmap
        if (bmp == null || bmp.width != width || bmp.height != height || bmp.isRecycled) {
            bmp?.recycle()
            bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            cachedBitmap = bmp
        } else {
            bmp.eraseColor(android.graphics.Color.TRANSPARENT)
        }

        val imgBitmap = bmp.asImageBitmap()
        val composeCanvas = androidx.compose.ui.graphics.Canvas(imgBitmap)
        val canvasDrawScope = androidx.compose.ui.graphics.drawscope.CanvasDrawScope()
        val density = androidx.compose.ui.unit.Density(1f)

        canvasDrawScope.draw(
            density = density,
            layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr,
            canvas = composeCanvas,
            size = Size(width.toFloat(), height.toFloat())
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            // 1. Background image or procedural gradient
            val bgBmp = customBgBitmap
            if (bgBmp != null) {
                sharedWearCanvasPath.reset()
                sharedWearCanvasPath.addOval(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                clipPath(sharedWearCanvasPath) {
                    drawImage(
                        image = bgBmp,
                        dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                    )
                }
            } else {
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
            }

            // 2. Render complex vector dial elements (Sacred geometry, Yantra polygons, 12-Rashi markers, tick marks, concentric rings)
            renderDialElements(dialModel.elements, center, radius, textMeasurer, wearTextCache)
        }

        cachedWidth = width
        cachedHeight = height
        cachedKey = cacheKey
        cachedImageBitmap = imgBitmap

        return cachedImageBitmap
    }

    fun release() {
        try {
            cachedBitmap?.recycle()
        } catch (e: Exception) {
            // Ignore
        }
        cachedBitmap = null
        cachedImageBitmap = null
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.renderSafeModeDial(
    syncData: SyncData,
    currentTime: LocalTime,
    center: Offset,
    radius: Float,
    isAmbient: Boolean,
    textMeasurer: TextMeasurer,
    textCache: MutableMap<String, TextLayoutResult>
) {
    // Pure Hardware Canvas Basic Dial (0 Shaders, 0 Gradients, 0 Complex loops)
    drawCircle(color = Color.Black, radius = radius, center = center)
    drawCircle(color = Color(0xFF334155), radius = radius * 0.96f, center = center, style = Stroke(width = 2.dp.toPx()))

    // 12 High-contrast tick marks
    for (i in 0 until 12) {
        val angleRad = Math.toRadians((i * 30.0 - 90.0)).toFloat()
        val isCardinal = (i % 3 == 0)
        val startR = if (isCardinal) radius * 0.82f else radius * 0.88f
        val endR = radius * 0.94f
        val p1 = Offset(center.x + startR * cos(angleRad), center.y + startR * sin(angleRad))
        val p2 = Offset(center.x + endR * cos(angleRad), center.y + endR * sin(angleRad))
        drawLine(
            color = if (isCardinal) Color(0xFFFFD54F) else Color.White,
            start = p1,
            end = p2,
            strokeWidth = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Digital time
    val timeStr = currentTime.format(timeFmtShort)
    val timeLayout = textCache.getOrPut("safe_time_$timeStr") {
        textMeasurer.measure(
            text = timeStr,
            style = TextStyle(color = Color(0xFFFFD54F), fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        )
    }
    drawText(timeLayout, topLeft = Offset(center.x - timeLayout.size.width / 2f, center.y + radius * 0.25f))

    // Safe mode badge / Muhurta
    val muhuratStr = "SAFE • ${syncData.currentMuhurat}"
    val badgeLayout = textCache.getOrPut("safe_badge_$muhuratStr") {
        textMeasurer.measure(
            text = muhuratStr,
            style = TextStyle(color = Color(0xFF60A5FA), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        )
    }
    drawText(badgeLayout, topLeft = Offset(center.x - badgeLayout.size.width / 2f, center.y - radius * 0.45f))

    // Direct Hands
    val sec = currentTime.second.toFloat()
    val min = currentTime.minute.toFloat() + (sec / 60f)
    val hour = (currentTime.hour % 12).toFloat() + (min / 60f)

    val hourAngle = Math.toRadians((hour * 30.0 - 90.0)).toFloat()
    val minAngle = Math.toRadians((min * 6.0 - 90.0)).toFloat()

    // Hour Hand
    val hLen = radius * 0.5f
    drawLine(
        color = Color.White,
        start = center,
        end = Offset(center.x + hLen * cos(hourAngle), center.y + hLen * sin(hourAngle)),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Minute Hand
    val mLen = radius * 0.75f
    drawLine(
        color = Color(0xFFFFD54F),
        start = center,
        end = Offset(center.x + mLen * cos(minAngle), center.y + mLen * sin(minAngle)),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    if (!isAmbient) {
        val secAngle = Math.toRadians((sec * 6.0 - 90.0)).toFloat()
        val sLen = radius * 0.85f
        drawLine(
            color = Color(0xFFF43F5E),
            start = center,
            end = Offset(center.x + sLen * cos(secAngle), center.y + sLen * sin(secAngle)),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Center pin
    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = center)
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.renderDialElements(
    elements: List<VectorDialElement>,
    center: Offset,
    radius: Float,
    textMeasurer: TextMeasurer,
    textCache: MutableMap<String, TextLayoutResult>
) {
    for (elem in elements) {
        when (elem) {
            is VectorDialElement.CircleRing -> {
                val r = radius * elem.radiusRatio
                val stroke = if (elem.isDashed) {
                    Stroke(
                        width = elem.strokeWidthDp.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                } else {
                    Stroke(width = elem.strokeWidthDp.dp.toPx())
                }
                drawCircle(
                    color = Color(elem.colorHex),
                    radius = r,
                    center = center,
                    style = stroke
                )
            }
            is VectorDialElement.TickMarks -> {
                val step = 360f / elem.count
                for (i in 0 until elem.count) {
                    val angleDeg = i * step
                    val angleRad = Math.toRadians((angleDeg - 90.0)).toFloat()
                    val startR = radius * elem.startRadiusRatio
                    val endR = radius * elem.endRadiusRatio
                    val p1 = Offset(center.x + startR * cos(angleRad), center.y + startR * sin(angleRad))
                    val p2 = Offset(center.x + endR * cos(angleRad), center.y + endR * sin(angleRad))
                    drawLine(
                        color = Color(elem.colorHex),
                        start = p1,
                        end = p2,
                        strokeWidth = elem.strokeWidthDp.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            is VectorDialElement.GlyphMarker -> {
                val angleRad = Math.toRadians(elem.angleDeg.toDouble()).toFloat()
                val r = radius * elem.radiusRatio
                val pos = Offset(center.x + r * cos(angleRad), center.y + r * sin(angleRad))
                val glyphKey = "glyph_${elem.text}_${elem.colorHex}_${elem.fontSizeSp}"
                val measured = textCache.getOrPut(glyphKey) {
                    textMeasurer.measure(
                        text = elem.text,
                        style = TextStyle(
                            color = Color(elem.colorHex),
                            fontSize = elem.fontSizeSp.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    )
                }
                drawText(
                    measured,
                    topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f)
                )
            }
            is VectorDialElement.PolygonShape -> {
                sharedWearCanvasPath.reset()
                elem.normalizedVertices.forEachIndexed { idx, pt ->
                    val x = center.x + pt.first * radius
                    val y = center.y + pt.second * radius
                    if (idx == 0) sharedWearCanvasPath.moveTo(x, y) else sharedWearCanvasPath.lineTo(x, y)
                }
                sharedWearCanvasPath.close()
                if (elem.isFilled) {
                    drawPath(sharedWearCanvasPath, color = Color(elem.colorHex))
                } else {
                    drawPath(
                        sharedWearCanvasPath,
                        color = Color(elem.colorHex),
                        style = Stroke(width = elem.strokeWidthDp.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            is VectorDialElement.ArcSegment -> {
                val r = radius * elem.radiusRatio
                drawArc(
                    color = Color(elem.colorHex),
                    startAngle = elem.startAngleDeg,
                    sweepAngle = elem.sweepAngleDeg,
                    useCenter = false,
                    topLeft = Offset(center.x - r, center.y - r),
                    size = Size(r * 2f, r * 2f),
                    style = Stroke(width = elem.strokeWidthDp.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.renderComplications(
    syncData: SyncData,
    currentTime: LocalTime,
    center: Offset,
    radius: Float,
    isAmbient: Boolean,
    textMeasurer: TextMeasurer,
    textCache: MutableMap<String, TextLayoutResult>
) {
    val timeStr = currentTime.format(timeFmtShort)

    if (isAmbient) {
        val aodMeasured = textCache.getOrPut("aod_time_$timeStr") {
            textMeasurer.measure(
                text = timeStr,
                style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            )
        }
        drawText(aodMeasured, topLeft = Offset(center.x - aodMeasured.size.width / 2f, center.y + radius * 0.15f))
        return
    }

    // Modern Elegant Complication Box Function
    fun drawElegantComplicationBox(pos: Offset, width: Float, height: Float, color: Color, progress: Float? = null) {
        drawRoundRect(
            color = Color(0xDD0F172A),
            topLeft = Offset(pos.x - width / 2f, pos.y - height / 2f),
            size = Size(width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2f)
        )
        val borderBrush = Brush.linearGradient(
            colors = listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0.15f)),
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

        // Mini progress bar for Steps / Battery
        if (progress != null && progress > 0f) {
            val clampedProgress = progress.coerceIn(0f, 1f)
            val barW = (width - 12.dp.toPx()) * clampedProgress
            val barH = 2.dp.toPx()
            val barX = pos.x - width / 2f + 6.dp.toPx()
            val barY = pos.y + height / 2f - 4.dp.toPx()
            drawRoundRect(
                color = color.copy(alpha = 0.9f),
                topLeft = Offset(barX, barY),
                size = Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx())
            )
        }
    }

    fun renderSlot(slot: ComplicationSlotPosition, typeStr: String, pos: Offset, width: Float, height: Float) {
        when (typeStr) {
            "STEP_COUNT" -> {
                val steps = syncData.stepCount
                val progress = if (syncData.stepGoal > 0) steps.toFloat() / syncData.stepGoal else 0.5f
                val formattedSteps = if (steps >= 1000) "${steps / 1000}.${(steps % 1000) / 100}k" else "$steps"
                val displayText = "👟 $formattedSteps"
                val color = Color(0xFF10B981)
                drawElegantComplicationBox(pos, width, height, color, progress)
                val measured = textCache.getOrPut("comp_steps_${displayText}_$width") {
                    textMeasurer.measure(
                        text = displayText,
                        style = TextStyle(color = Color(0xFF6EE7B7), fontSize = 10.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
                drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f - 1.dp.toPx()))
            }
            "WEATHER" -> {
                val temp = syncData.weatherTemp
                val icon = syncData.weatherIcon
                val cond = syncData.weatherCondition
                val displayText = if (width > 80.dp.toPx()) "$icon $temp • $cond" else "$icon $temp"
                val color = Color(0xFF38BDF8)
                drawElegantComplicationBox(pos, width, height, color)
                val measured = textCache.getOrPut("comp_weather_${displayText}_$width") {
                    textMeasurer.measure(
                        text = displayText,
                        style = TextStyle(color = Color(0xFF7DD3FC), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
                drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f))
            }
            "BATTERY" -> {
                val pct = syncData.batteryPercent
                val progress = pct / 100f
                val displayText = "🔋 $pct%"
                val color = if (pct <= 20) Color(0xFFEF4444) else if (pct <= 45) Color(0xFFFBBF24) else Color(0xFF34D399)
                drawElegantComplicationBox(pos, width, height, color, progress)
                val measured = textCache.getOrPut("comp_bat_${displayText}_$width") {
                    textMeasurer.measure(
                        text = displayText,
                        style = TextStyle(color = Color.White, fontSize = 10.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
                drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f - 1.dp.toPx()))
            }
            "HEART_RATE" -> {
                val bpm = syncData.heartRateBpm
                val displayText = "❤️ $bpm bpm"
                val color = Color(0xFFF43F5E)
                drawElegantComplicationBox(pos, width, height, color)
                val measured = textCache.getOrPut("comp_hr_${displayText}_$width") {
                    textMeasurer.measure(
                        text = displayText,
                        style = TextStyle(color = Color(0xFFFDA4AF), fontSize = 10.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
                drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f))
            }
            "VEDIC_TITHI" -> {
                val tithiText = syncData.tithiDevanagari.ifEmpty { syncData.tithi }
                val displayText = if (width > 80.dp.toPx()) "$tithiText • ${syncData.nakshatra}" else tithiText
                val color = Color.White
                drawElegantComplicationBox(pos, width, height, color)
                val measured = textCache.getOrPut("comp_tithi_${displayText}_$width") {
                    textMeasurer.measure(
                        text = displayText,
                        style = TextStyle(color = Color(0xFFE2E8F0), fontSize = 9.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    )
                }
                drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f))
            }
            "PLANETARY_HORA" -> {
                val displayText = "${syncData.horaSymbol} ${syncData.currentHora}"
                val color = Color(0xFF67E8F9)
                drawElegantComplicationBox(pos, width, height, color)
                val measured = textCache.getOrPut("comp_hora_${displayText}_$width") {
                    textMeasurer.measure(
                        text = displayText,
                        style = TextStyle(color = Color(0xFF67E8F9), fontSize = 10.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
                drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f))
            }
            "RAHU_KAAL" -> {
                val rightColor = if (syncData.isRahuActive) Color(0xFFEF4444) else if (syncData.isAuspicious) Color(0xFF10B981) else Color(0xFFFFB703)
                val rahuText = if (syncData.isRahuActive) "⚠ RAHU" else if (syncData.isAuspicious) "शुभ ✓" else "राहु"
                drawElegantComplicationBox(pos, width, height, rightColor)
                val measured = textCache.getOrPut("comp_rahu_${rahuText}_${rightColor.value}") {
                    textMeasurer.measure(
                        text = rahuText,
                        style = TextStyle(color = rightColor, fontSize = 10.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
                drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f))
            }
            "SUNRISE_SUNSET" -> {
                val displayText = "☀️ ${syncData.sunrise} ↑"
                val color = Color(0xFFF59E0B)
                drawElegantComplicationBox(pos, width, height, color)
                val measured = textCache.getOrPut("comp_sun_${displayText}_$width") {
                    textMeasurer.measure(
                        text = displayText,
                        style = TextStyle(color = Color(0xFFFDE68A), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
                drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f))
            }
            "DATE_VARA" -> {
                val vara = syncData.currentVara.take(4)
                val dayVal = java.time.LocalDate.now().dayOfMonth
                val displayText = "📅 $vara $dayVal"
                val color = Color(0xFFA78BFA)
                drawElegantComplicationBox(pos, width, height, color)
                val measured = textCache.getOrPut("comp_date_${displayText}_$width") {
                    textMeasurer.measure(
                        text = displayText,
                        style = TextStyle(color = Color(0xFFDDD6FE), fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
                drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f))
            }
            else -> {
                // Default VEDIC_MUHURAT
                val badgeColor = if (syncData.isAuspicious) Color(0xFF34D399) else Color(0xFFF87171)
                val muhuratName = syncData.currentMuhuratDevanagari.ifEmpty { syncData.currentMuhurat }
                val displayText = "मुहूर्त: $muhuratName"
                drawElegantComplicationBox(pos, width, height, badgeColor)
                val measured = textCache.getOrPut("comp_muh_${displayText}_$width") {
                    textMeasurer.measure(
                        text = displayText,
                        style = TextStyle(color = Color.White, fontSize = 10.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
                drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f))
            }
        }
    }

    // 1. Digital Time Readout (Center Subdial)
    val timePos = Offset(center.x, center.y + radius * 0.22f)
    drawElegantComplicationBox(timePos, 88.dp.toPx(), 24.dp.toPx(), Color(0xFFFFD54F))
    val measuredTime = textCache.getOrPut("comp_time_$timeStr") {
        textMeasurer.measure(
            text = timeStr,
            style = TextStyle(color = Color(0xFFFACC15), fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
        )
    }
    drawText(measuredTime, topLeft = Offset(timePos.x - measuredTime.size.width / 2f, timePos.y - measuredTime.size.height / 2f))

    // 2. Top Complication Slot
    val topPos = Offset(center.x, center.y - radius * 0.55f)
    renderSlot(ComplicationSlotPosition.TOP, syncData.topComplicationType, topPos, 110.dp.toPx(), 26.dp.toPx())

    // 3. Bottom Complication Slot
    val bottomPos = Offset(center.x, center.y + radius * 0.58f)
    renderSlot(ComplicationSlotPosition.BOTTOM, syncData.bottomComplicationType, bottomPos, 114.dp.toPx(), 24.dp.toPx())

    // 4. Left Complication Slot
    val leftPos = Offset(center.x - radius * 0.58f, center.y)
    renderSlot(ComplicationSlotPosition.LEFT, syncData.leftComplicationType, leftPos, 70.dp.toPx(), 23.dp.toPx())

    // 5. Right Complication Slot
    val rightPos = Offset(center.x + radius * 0.58f, center.y)
    renderSlot(ComplicationSlotPosition.RIGHT, syncData.rightComplicationType, rightPos, 70.dp.toPx(), 23.dp.toPx())
}

private fun DrawScope.renderHands(
    currentTime: LocalTime,
    center: Offset,
    radius: Float,
    dialModel: ProceduralWatchFaceModel,
    isAmbient: Boolean,
    isLowPower: Boolean = false
) {
    val sec = if (isLowPower || isAmbient) 0f else currentTime.second.toFloat()
    val min = currentTime.minute.toFloat() + (currentTime.second.toFloat() / 60f)
    val hour = (currentTime.hour % 12).toFloat() + (min / 60f)

    val hourAngle = Math.toRadians((hour * 30.0 - 90.0)).toFloat()
    val minAngle = Math.toRadians((min * 6.0 - 90.0)).toFloat()
    val secAngle = Math.toRadians((sec * 6.0 - 90.0)).toFloat()

    // 1. Hour Hand (Chubby, distinctive tapering sword)
    val hLen = radius * 0.48f
    val hTarget = Offset(center.x + hLen * cos(hourAngle), center.y + hLen * sin(hourAngle))
    drawLine(
        color = if (isAmbient) Color.White else Color(dialModel.hourHandColorHex),
        start = center,
        end = hTarget,
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 2. Minute Hand (Sleeker, longer reach)
    val mLen = radius * 0.72f
    val mTarget = Offset(center.x + mLen * cos(minAngle), center.y + mLen * sin(minAngle))
    drawLine(
        color = if (isAmbient) Color.LightGray else Color(dialModel.minuteHandColorHex),
        start = center,
        end = mTarget,
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 3. Second Hand (Sweeping slender needle with counter-weight, omitted in Low-Power Mode and Ambient AOD)
    if (!isAmbient && !isLowPower) {
        val sLen = radius * 0.82f
        val sTail = radius * 0.16f
        val sTarget = Offset(center.x + sLen * cos(secAngle), center.y + sLen * sin(secAngle))
        val sTailTarget = Offset(center.x - sTail * cos(secAngle), center.y - sTail * sin(secAngle))
        drawLine(
            color = Color(dialModel.secondHandColorHex),
            start = sTailTarget,
            end = sTarget,
            strokeWidth = 1.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
