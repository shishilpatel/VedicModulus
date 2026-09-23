package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import com.example.ui.theme.DarkText
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.core.model.*
import com.example.generator.watchface.ProceduralWatchFaceModel
import com.example.generator.watchface.VectorDialElement
import com.example.generator.watchface.WatchFaceGenerator
import com.example.generator.watchface.CelestialClockEngine
import com.example.generator.watchface.CelestialClockState
import com.example.generator.watchface.CelestialPlanetMarker
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

private val timeFmtShort = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
private val timeFmtLong = java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a")

@OptIn(ExperimentalTextApi::class)
@Composable
fun GalaxyWatchFaceCanvas(
    config: WatchFaceConfiguration,
    personalizedProfile: PersonalizedProfile,
    dailyProfile: DailyVedicProfile?,
    modifier: Modifier = Modifier,
    isAmbientMode: Boolean = false,
    currentTimeProvider: () -> LocalTime = { LocalTime.of(10, 10, 30) },
    stepsCount: Int = 7420,
    batteryPercent: Int = 86,
    complications: ComplicationCustomization = ComplicationCustomization(),
    externalCelestialClockState: CelestialClockState? = null
) {
    val generator = remember { WatchFaceGenerator() }
    val dialModel = remember(config, personalizedProfile, dailyProfile) {
        generator.generateDialModel(config, personalizedProfile, dailyProfile)
    }

    val textMeasurer = rememberTextMeasurer()

    // Pre-measure time-related characters for the digital clock to avoid per-second measurement overhead
    val timeCharCache = remember(textMeasurer) {
        val chars = "0123456789: APM"
        chars.associate { char ->
            char to textMeasurer.measure(
                text = char.toString(),
                style = TextStyle(color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            )
        }
    }

    val aodTimeCharCache = remember(textMeasurer) {
        val chars = "0123456789: APM"
        chars.associate { char ->
            char to textMeasurer.measure(
                text = char.toString(),
                style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            )
        }
    }

    val measuredGlyphs = remember(dialModel, config) {
        dialModel.elements.filterIsInstance<com.example.generator.watchface.VectorDialElement.GlyphMarker>().associate { elem ->
            elem to textMeasurer.measure(
                text = elem.text,
                style = androidx.compose.ui.text.TextStyle(
                    color = androidx.compose.ui.graphics.Color(elem.colorHex),
                    fontSize = elem.fontSizeSp.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }
    }

    // Cache for complication text strings to avoid measurement jitter and CPU load
    val complicationTextCache = remember { mutableMapOf<String, TextLayoutResult>() }

    val zoneId = remember(dailyProfile) {
        try {
            dailyProfile?.currentZonedDateTime?.zone ?: java.time.ZoneId.systemDefault()
        } catch (e: Exception) {
            java.time.ZoneId.systemDefault()
        }
    }

    var liveCurrentTime by remember {
        mutableStateOf(currentTimeProvider())
    }

    // Celestial Clockwork State from Swiss Ephemeris
    val celestialEngine = remember { CelestialClockEngine() }
    val currentZdt = remember(dailyProfile, liveCurrentTime) {
        val base = dailyProfile?.currentZonedDateTime ?: ZonedDateTime.now()
        try {
            base.withHour(liveCurrentTime.hour)
                .withMinute(liveCurrentTime.minute)
                .withSecond(liveCurrentTime.second)
        } catch (e: Exception) {
            base
        }
    }
    val loc = personalizedProfile.userBirthProfile.birthLocation
    val internalCelestialClockState by produceState<CelestialClockState?>(initialValue = null, key1 = externalCelestialClockState, key2 = currentZdt.minute, key3 = loc.latitude) {
        if (externalCelestialClockState != null) {
            value = externalCelestialClockState
        } else {
            value = withContext(Dispatchers.Default) {
                celestialEngine.calculateClockState(
                    dateTime = currentZdt,
                    latitude = loc.latitude,
                    longitude = loc.longitude
                )
            }
        }
    }
    val celestialClockState = externalCelestialClockState ?: internalCelestialClockState

    // Pre-measure 12 Zodiac Rashi glyphs
    val rashiGlyphCache = remember(textMeasurer) {
        val symbols = listOf("♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐", "♑", "♒", "♓")
        symbols.associateWith { sym ->
            textMeasurer.measure(
                text = sym,
                style = TextStyle(
                    color = Color(0xFFFFD54F),
                    fontSize = 9.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }
    }

    // Pre-measure 9 Navagraha planetary glyphs and retrograde symbol
    val planetGlyphCache = remember(textMeasurer) {
        val symbols = listOf("☉", "☽", "♂", "☿", "♃", "♀", "♄", "☊", "☋", "℞")
        symbols.associateWith { sym ->
            textMeasurer.measure(
                text = sym,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }
    }

    LaunchedEffect(isAmbientMode, zoneId) {
        while (true) {
            liveCurrentTime = currentTimeProvider()
            if (isAmbientMode) {
                val nowMs = System.currentTimeMillis()
                val remainingInMin = 60000L - (nowMs % 60000L)
                kotlinx.coroutines.delay(if (remainingInMin <= 50L) 60000L else remainingInMin)
            } else {
                val nowMs = System.currentTimeMillis()
                val remainingInSec = 1000L - (nowMs % 1000L)
                kotlinx.coroutines.delay(if (remainingInSec <= 20L) 1000L else remainingInSec)
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val customBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = config.customBackgroundUri) {
        value = if (config.customBackgroundUri.isNotBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = android.net.Uri.parse(config.customBackgroundUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        android.graphics.BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(16.dp, CircleShape)
            .border(5.dp, Color(0xFF1E293B), CircleShape)
            .clip(CircleShape)
            .background(Color.Black)
            .testTag("galaxy_watch_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentTime = liveCurrentTime
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            val bmp = customBitmap
            if (bmp != null) {
                val imgW = bmp.width.toFloat()
                val imgH = bmp.height.toFloat()
                val scale = kotlin.math.max(size.width / imgW, size.height / imgH)
                val scaledW = imgW * scale
                val scaledH = imgH * scale
                val offsetX = (size.width - scaledW) / 2f
                val offsetY = (size.height - scaledH) / 2f

                val clipPathObj = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                }
                clipPath(clipPathObj) {
                    drawImage(
                        image = bmp,
                        dstOffset = androidx.compose.ui.unit.IntOffset(offsetX.toInt(), offsetY.toInt()),
                        dstSize = androidx.compose.ui.unit.IntSize(scaledW.toInt(), scaledH.toInt())
                    )
                }
            } else {
                // Atmospheric Background Gradient based on Style (Vibrant & Luminous)
                val bgColors = when (config.style) {
                    WatchFaceStyle.COSMIC -> listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF020408))
                    WatchFaceStyle.ROYAL -> listOf(Color(0xFF78350F), Color(0xFF451A03), Color(0xFF1A0A02))
                    WatchFaceStyle.CHAKRA -> listOf(Color(0xFF581C87), Color(0xFF3B0764), Color(0xFF0F051D))
                    WatchFaceStyle.YANTRA -> listOf(Color(0xFF991B1B), Color(0xFF7F1D1D), Color(0xFF1F0404))
                    WatchFaceStyle.MINIMAL -> listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
                    WatchFaceStyle.VASTU -> listOf(Color(0xFF065F46), Color(0xFF064E3B), Color(0xFF022C22))
                    else -> listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF020408))
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = bgColors,
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                // Draw decorative background celestial rings/mandala aura (omitted for Yantra to keep sacred geometry pure)
                if (config.style != WatchFaceStyle.YANTRA) {
                    drawCircle(
                        color = Color(0x33FFD54F),
                        radius = radius * 0.85f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    )
                    drawCircle(
                        color = Color(0x22FFFFFF),
                        radius = radius * 0.65f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // 1. Render Procedural Dial Elements (Skipped or dimmed in Ambient AOD)
            if (!isAmbientMode) {
                renderDialElements(
                    elements = dialModel.elements,
                    center = center,
                    radius = radius,
                    textMeasurer = textMeasurer,
                    measuredGlyphs = measuredGlyphs,
                    style = config.style
                )

                // Real-Time Astronomical Celestial Clockwork layer (calculated by Swiss Ephemeris)
                // In Vedic Shastras, Sri Yantra geometry must remain eternal and unmodified, so celestial gear rings are omitted for Yantra
                val clockState = celestialClockState
                if (clockState != null && config.showCelestialClock && config.style != WatchFaceStyle.YANTRA) {
                    renderCelestialClockwork(
                        celestialClock = clockState,
                        center = center,
                        radius = radius,
                        rashiGlyphCache = rashiGlyphCache,
                        planetGlyphCache = planetGlyphCache,
                        primaryColor = Color(config.primaryColorHex),
                        accentColor = Color(config.accentColorHex)
                    )
                }
            } else {
                // Ambient Mode: Minimal power consumption (<10% OPR)
                drawCircle(
                    color = Color(0x22FFFFFF),
                    radius = radius * 0.95f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 2. Render Vedic Complications Subdials (Top, Bottom, Left, Right) & Digital Time
            renderComplications(
                config = config,
                dailyProfile = dailyProfile,
                personalizedProfile = personalizedProfile,
                complications = complications,
                center = center,
                radius = radius,
                isAmbient = isAmbientMode,
                textMeasurer = textMeasurer,
                currentTime = currentTime,
                battery = batteryPercent,
                steps = stepsCount,
                timeCharCache = if (isAmbientMode) aodTimeCharCache else timeCharCache,
                complicationTextCache = complicationTextCache
            )

            // 3. Render Watch Hands (Hours, Minutes, Seconds)
            renderHands(
                currentTime = currentTime,
                center = center,
                radius = radius,
                dialModel = dialModel,
                isAmbient = isAmbientMode
            )

            // 4. Center Cap Pin
            drawCircle(
                color = if (isAmbientMode) DarkText else Color(dialModel.centerCapColorHex),
                radius = 4.dp.toPx(),
                center = center
            )
        }
    }
}

private fun DrawScope.renderDialElements(
    elements: List<VectorDialElement>,
    center: Offset,
    radius: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    measuredGlyphs: Map<com.example.generator.watchface.VectorDialElement.GlyphMarker, androidx.compose.ui.text.TextLayoutResult>,
    style: WatchFaceStyle
) {
    for (elem in elements) {
        when (elem) {
            is VectorDialElement.CircleRing -> {
                val r = radius * elem.radiusRatio
                drawCircle(
                    color = Color(elem.colorHex),
                    radius = r,
                    center = center,
                    style = Stroke(
                        width = elem.strokeWidthDp.dp.toPx(),
                        pathEffect = if (elem.isDashed) PathEffect.dashPathEffect(floatArrayOf(8f, 8f), phase = 0f) else null
                    )
                )
            }

            is VectorDialElement.TickMarks -> {
                // Dial markings (hour & minute ticks) are fixed and stationary
                val stepAngle = 360.0 / elem.count
                for (i in 0 until elem.count) {
                    val angleRad = Math.toRadians(i * stepAngle - 90.0)
                    val rStart = radius * elem.startRadiusRatio
                    val rEnd = radius * elem.endRadiusRatio
                    val p1 = Offset(center.x + (rStart * cos(angleRad)).toFloat(), center.y + (rStart * sin(angleRad)).toFloat())
                    val p2 = Offset(center.x + (rEnd * cos(angleRad)).toFloat(), center.y + (rEnd * sin(angleRad)).toFloat())
                    drawLine(
                        color = Color(elem.colorHex),
                        start = p1,
                        end = p2,
                        strokeWidth = elem.strokeWidthDp.dp.toPx()
                    )
                }
            }

            is VectorDialElement.GlyphMarker -> {
                // Dial glyphs and hour markers are fixed and stationary
                val angleRad = Math.toRadians(elem.angleDeg.toDouble())
                val r = radius * elem.radiusRatio
                val pos = Offset(center.x + (r * cos(angleRad)).toFloat(), center.y + (r * sin(angleRad)).toFloat())
                val layoutResult = measuredGlyphs[elem] ?: textMeasurer.measure(
                    text = elem.text,
                    style = TextStyle(
                        color = Color(elem.colorHex),
                        fontSize = elem.fontSizeSp.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = Offset(pos.x - layoutResult.size.width / 2f, pos.y - layoutResult.size.height / 2f)
                )
            }

            is VectorDialElement.PolygonShape -> {
                // Dial polygon shapes (Yantra/Sacred Geometry/Dial accents) are fixed and stationary
                val polyPath = Path()
                elem.normalizedVertices.forEachIndexed { index, pair ->
                    val px = center.x + pair.first * radius
                    val py = center.y + pair.second * radius
                    if (index == 0) polyPath.moveTo(px, py) else polyPath.lineTo(px, py)
                }
                polyPath.close()
                drawPath(
                    path = polyPath,
                    color = Color(elem.colorHex),
                    style = if (elem.isFilled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = elem.strokeWidthDp.dp.toPx())
                )
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

/**
 * Renders the Real-Time Astronomical Celestial Clockwork layer (calculated by Swiss Ephemeris).
 * Displays:
 * 1. Stationary Outer 12-Rashi Zodiac Ring with spokes and glyphs
 * 2. Navagraha Concentric Planetary Orbits with accurate astronomical coordinates
 * 3. Exact Real-time Planetary Spheres (Surya, Chandra, Mangala, Budha, Guru, Shukra, Shani, Rahu, Ketu)
 * 4. Rahu-Ketu Karmic Axis and Soli-Lunar Ray
 */
private fun DrawScope.renderCelestialClockwork(
    celestialClock: CelestialClockState,
    center: Offset,
    radius: Float,
    rashiGlyphCache: Map<String, TextLayoutResult>,
    planetGlyphCache: Map<String, TextLayoutResult>,
    primaryColor: Color,
    accentColor: Color
) {
    // 1. Outer 12-Rashi Zodiac Ring - Fixed & Stationary (No rotation)
    val zodiacAngle = 0.0
    val outerR = radius * 0.94f
    val innerR = radius * 0.84f

    // Outer and inner rim guides
    drawCircle(
        color = Color(0x33FFD54F),
        radius = outerR,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
    drawCircle(
        color = Color(0x22FFD54F),
        radius = innerR,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // 12 Sector Dividers and Rashi Glyphs (Fixed around the dial)
    val rashiSymbols = listOf("♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐", "♑", "♒", "♓")
    for (i in 0 until 12) {
        val spokeRad = Math.toRadians((zodiacAngle + i * 30.0 - 90.0))
        val p1 = Offset(center.x + (innerR * cos(spokeRad)).toFloat(), center.y + (innerR * sin(spokeRad)).toFloat())
        val p2 = Offset(center.x + (outerR * cos(spokeRad)).toFloat(), center.y + (outerR * sin(spokeRad)).toFloat())
        drawLine(
            color = Color(0x44FFD54F),
            start = p1,
            end = p2,
            strokeWidth = 1.dp.toPx()
        )

        val midRad = Math.toRadians((zodiacAngle + i * 30.0 + 15.0 - 90.0))
        val sym = rashiSymbols[i]
        rashiGlyphCache[sym]?.let { layout ->
            val gx = center.x + ((radius * 0.89f) * cos(midRad)).toFloat()
            val gy = center.y + ((radius * 0.89f) * sin(midRad)).toFloat()
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(gx - layout.size.width / 2f, gy - layout.size.height / 2f)
            )
        }
    }

    // Eastern Horizon / Lagna Indicator Marker at Top (12 o'clock)
    val lagnaPointerY = center.y - radius * 0.95f
    val lagnaPath = Path().apply {
        moveTo(center.x, lagnaPointerY)
        lineTo(center.x - 3.dp.toPx(), lagnaPointerY - 5.dp.toPx())
        lineTo(center.x + 3.dp.toPx(), lagnaPointerY - 5.dp.toPx())
        close()
    }
    drawPath(path = lagnaPath, color = Color(0xFFFFD54F))

    // 2. Rahu-Ketu Karmic Axis Line
    val rahu = celestialClock.rahu
    val ketu = celestialClock.ketu
    val rahuAngleRad = Math.toRadians(rahu.longitudeDeg - celestialClock.lagnaLongitudeDeg - 90.0)
    val ketuAngleRad = Math.toRadians(ketu.longitudeDeg - celestialClock.lagnaLongitudeDeg - 90.0)
    val rNodeDist = radius * rahu.orbitRadiusRatio
    val pRahu = Offset(center.x + (rNodeDist * cos(rahuAngleRad)).toFloat(), center.y + (rNodeDist * sin(rahuAngleRad)).toFloat())
    val pKetu = Offset(center.x + (rNodeDist * cos(ketuAngleRad)).toFloat(), center.y + (rNodeDist * sin(ketuAngleRad)).toFloat())
    drawLine(
        color = Color(0x338B5CF6),
        start = pRahu,
        end = pKetu,
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))
    )

    // 3. Soli-Lunar Harmony Ray between Surya and Chandra
    val surya = celestialClock.surya
    val chandra = celestialClock.chandra
    val sRad = Math.toRadians(surya.longitudeDeg - celestialClock.lagnaLongitudeDeg - 90.0)
    val cRad = Math.toRadians(chandra.longitudeDeg - celestialClock.lagnaLongitudeDeg - 90.0)
    val pSurya = Offset(center.x + (radius * surya.orbitRadiusRatio * cos(sRad)).toFloat(), center.y + (radius * surya.orbitRadiusRatio * sin(sRad)).toFloat())
    val pChandra = Offset(center.x + (radius * chandra.orbitRadiusRatio * cos(cRad)).toFloat(), center.y + (radius * chandra.orbitRadiusRatio * sin(cRad)).toFloat())
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(Color(0x55FFD700), Color(0x55E0F2FE)),
            start = pSurya,
            end = pChandra
        ),
        start = pSurya,
        end = pChandra,
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 4f))
    )

    // 4. Navagraha Concentric Planetary Orbits & Real-Time Moving Spheres
    celestialClock.planets.forEach { p ->
        val orbR = radius * p.orbitRadiusRatio

        // Orbital Ring with clean static dashed guide
        drawCircle(
            color = Color(0x18FFFFFF),
            radius = orbR,
            center = center,
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 6f), phase = 0f)
            )
        )

        // Exact real-time astronomical coordinate
        val basePlanetAngle = p.longitudeDeg - celestialClock.lagnaLongitudeDeg - 90.0
        val currentPlanetRad = Math.toRadians(basePlanetAngle)
        val px = center.x + (orbR * cos(currentPlanetRad)).toFloat()
        val py = center.y + (orbR * sin(currentPlanetRad)).toFloat()

        // Planet Outer Aura / Glowing Halo
        val glowRadius = p.markerRadiusDp.dp.toPx() * 2.2f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(p.glowColorHex), Color.Transparent),
                center = Offset(px, py),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(px, py)
        )

        // Planet 3D Sphere Body
        val sphereRadius = p.markerRadiusDp.dp.toPx()
        drawCircle(
            color = Color(p.colorHex),
            radius = sphereRadius,
            center = Offset(px, py)
        )
        // Specular 3D Highlight
        drawCircle(
            color = Color(0x99FFFFFF),
            radius = sphereRadius * 0.35f,
            center = Offset(px - sphereRadius * 0.25f, py - sphereRadius * 0.25f)
        )

        // Planet Astrological Glyph
        planetGlyphCache[p.symbol]?.let { glyphLayout ->
            drawText(
                textLayoutResult = glyphLayout,
                topLeft = Offset(px - glyphLayout.size.width / 2f, py - glyphLayout.size.height / 2f)
            )
        }

        // Retrograde Indicator Badge
        if (p.isRetrograde) {
            drawCircle(
                color = Color(0xFFEF4444),
                radius = sphereRadius + 2.dp.toPx(),
                center = Offset(px, py),
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f)))
            )
            planetGlyphCache["℞"]?.let { rxLayout ->
                drawText(
                    textLayoutResult = rxLayout,
                    topLeft = Offset(px + sphereRadius, py - sphereRadius - 2.dp.toPx())
                )
            }
        }
    }
}

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
    steps: Int,
    timeCharCache: Map<Char, TextLayoutResult>,
    complicationTextCache: MutableMap<String, TextLayoutResult>
) {
    // Digital Time Readout
    val timeFormatter = if (isAmbient) timeFmtShort else timeFmtLong
    val timeStr = currentTime.format(timeFormatter)
    val timePos = Offset(center.x, center.y + radius * (if (isAmbient) 0.15f else 0.22f))

    if (isAmbient) {
        var currentX = center.x - (timeStr.sumOf { timeCharCache[it]?.size?.width ?: 0 }) / 2f
        timeStr.forEach { char ->
            timeCharCache[char]?.let { layout ->
                drawText(layout, topLeft = Offset(currentX, timePos.y))
                currentX += layout.size.width
            }
        }
        return
    }

    // Active Digital Time Pill
    val textWidth = timeStr.sumOf { timeCharCache[it]?.size?.width ?: 0 }
    val timePillWidth = maxOf(86.dp.toPx(), textWidth + 18.dp.toPx())
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

    var currentX = timePos.x - (timeStr.sumOf { timeCharCache[it]?.size?.width ?: 0 }) / 2f
    timeStr.forEach { char ->
        timeCharCache[char]?.let { layout ->
            drawText(layout, topLeft = Offset(currentX, timePos.y - layout.size.height / 2f))
            currentX += layout.size.width
        }
    }

    val topPos = Offset(center.x, center.y - radius * 0.52f)
    val bottomPos = Offset(center.x, center.y + radius * 0.52f)
    val leftPos = Offset(center.x - radius * 0.54f, center.y)
    val rightPos = Offset(center.x + radius * 0.54f, center.y)
    
    drawComplicationSlot(complications.topSlot, topPos, config, dailyProfile, personalizedProfile, textMeasurer, battery, steps, currentTime, complicationTextCache)
    drawComplicationSlot(complications.bottomSlot, bottomPos, config, dailyProfile, personalizedProfile, textMeasurer, battery, steps, currentTime, complicationTextCache)
    drawComplicationSlot(complications.leftSlot, leftPos, config, dailyProfile, personalizedProfile, textMeasurer, battery, steps, currentTime, complicationTextCache)
    drawComplicationSlot(complications.rightSlot, rightPos, config, dailyProfile, personalizedProfile, textMeasurer, battery, steps, currentTime, complicationTextCache)
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
    currentTime: LocalTime,
    complicationTextCache: MutableMap<String, TextLayoutResult>
) {
    if (dailyProfile == null || personalizedProfile == null) return

    var textStr = ""
    var color = Color(0xFF38BDF8)
    var isBadge = false
    var badgeBgColor = Color.Transparent
    
    when (type) {
        ComplicationType.SUNRISE_SUNSET -> {
            val now = dailyProfile.currentZonedDateTime
            val isBeforeSunrise = now.isBefore(dailyProfile.panchanga.sunrise)
            val isBeforeSunset = now.isBefore(dailyProfile.panchanga.sunset)
            textStr = if (isBeforeSunrise) {
                "↑ Rise ${dailyProfile.panchanga.sunrise.format(timeFmtShort)}"
            } else if (isBeforeSunset) {
                "↓ Set ${dailyProfile.panchanga.sunset.format(timeFmtShort)}"
            } else {
                "↑ Rise ${dailyProfile.panchanga.sunrise.format(timeFmtShort)}"
            }
            color = Color(0xFFFFB74D)
        }
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
        ComplicationType.BRAHMA_MUHURTA -> {
            val now = dailyProfile.currentZonedDateTime
            val brahma = dailyProfile.brahmaMuhurta
            if (now.isAfter(brahma.startTime) && now.isBefore(brahma.endTime)) {
                isBadge = true
                badgeBgColor = Color(0xFF10B981)
                color = Color.White
                textStr = "✨ Brahma Active"
            } else if (now.isBefore(brahma.startTime)) {
                textStr = "Brahma: ${brahma.startTime.format(timeFmtShort)}"
                color = Color(0xFFFBBF24)
            } else {
                textStr = "Brahma: ${brahma.startTime.format(timeFmtShort)}"
                color = Color(0xFF94A3B8)
            }
        }
        ComplicationType.ABHIJIT_MUHURTA -> {
            val abhijit = dailyProfile.abhijitMuhurta
            val now = dailyProfile.currentZonedDateTime
            if (abhijit != null && now.isAfter(abhijit.startTime) && now.isBefore(abhijit.endTime)) {
                isBadge = true
                badgeBgColor = Color(0xFFF59E0B)
                color = Color.White
                textStr = "🌟 Abhijit Active"
            } else if (abhijit != null) {
                textStr = "Abhijit: ${abhijit.startTime.format(timeFmtShort)}"
                color = Color(0xFFFCD34D)
            } else {
                textStr = "Abhijit: N/A"
                color = Color(0xFF94A3B8)
            }
        }
        ComplicationType.GHATI_PALA -> {
            val sunrise = dailyProfile.panchanga.sunrise
            val now = dailyProfile.currentZonedDateTime
            val secondsElapsed = if (now.isAfter(sunrise)) {
                java.time.Duration.between(sunrise, now).seconds
            } else {
                java.time.Duration.between(sunrise.minusDays(1), now).seconds
            }
            val ghati = (secondsElapsed / 1440) % 60
            val pala = (secondsElapsed % 1440) / 24
            textStr = if (config.showDevanagari) "घटी $ghati : पल $pala" else "Ghati $ghati : $pala"
            color = Color(0xFFF472B6)
        }
        ComplicationType.TARA_BALA -> {
            val taraName = dailyProfile.panchanga.nakshatra.sanskritName ?: "Mitra"
            textStr = "Tara: $taraName"
            color = Color(0xFF10B981)
        }
        ComplicationType.HORA_GRAHA -> {
            val horaSymbol = dailyProfile.currentHora.rulingGraha.symbol
            textStr = "Hora $horaSymbol"
            color = Color(0xFF38BDF8)
        }
        ComplicationType.DASHA_PERIOD -> {
            val dashaLord = personalizedProfile.dashaPeriods.firstOrNull { it.isCurrent }?.lord 
                ?: personalizedProfile.dashaPeriods.firstOrNull()?.lord 
                ?: Graha.GURU
            textStr = "Dasha: ${dashaLord.displayName} ${dashaLord.symbol}"
            color = Color(0xFF67E8F9)
        }
        ComplicationType.COSMIC_SCORE -> {
            val score = dailyProfile.cosmicScore?.overall ?: 88
            textStr = "⚡ Cosmic: $score/100"
            color = Color(0xFF34D399)
        }
        ComplicationType.TITHI_PHASE -> {
            textStr = if (config.showDevanagari) {
                dailyProfile.panchanga.tithi.devanagari
            } else {
                "${dailyProfile.panchanga.tithi.name} (${if (dailyProfile.panchanga.tithi.paksha == Paksha.SHUKLA) "Wax" else "Wan"})"
            }
            color = Color(0xFFFFD54F)
        }
        ComplicationType.MOON_RASHI -> {
            val rashiName = personalizedProfile.natalChandraRashi.sanskritName
            val symbol = personalizedProfile.natalChandraRashi.symbol
            textStr = "☽ $rashiName $symbol"
            color = Color(0xFFE2E8F0)
        }
        ComplicationType.YOGA_KARANA -> {
            val yoga = dailyProfile.panchanga.yoga
            val karana = dailyProfile.panchanga.karana
            textStr = "$yoga | $karana"
            color = Color(0xFFA78BFA)
        }
        ComplicationType.JANMA_STAR -> {
            textStr = "★ ${personalizedProfile.natalNakshatra.sanskritName}"
            color = Color(0xFFFBBF24)
        }
        ComplicationType.VASTU_ELEMENT -> {
            val element = when (dailyProfile.currentHora.rulingGraha) {
                Graha.MANGALA, Graha.SURYA -> "Fire (Agni)"
                Graha.CHANDRA, Graha.SHUKRA -> "Water (Jala)"
                Graha.BUDHA -> "Earth (Prithvi)"
                Graha.SHANI, Graha.RAHU -> "Air (Vayu)"
                else -> "Space (Akasha)"
            }
            textStr = "☤ $element"
            color = Color(0xFF8B5CF6)
        }
        ComplicationType.VASTU_COMPASS -> {
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
            val birthPath = config.personalizedBirthNumber
            val todayDay = dailyProfile.currentZonedDateTime.dayOfMonth
            val todayMonth = dailyProfile.currentZonedDateTime.monthValue
            val todayYear = dailyProfile.currentZonedDateTime.year
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
        ComplicationType.MANTRA_JAPA -> {
            textStr = "📿 Japa: 108 Mala"
            color = Color(0xFFFB923C)
        }
        ComplicationType.FESTIVAL_COUNTDOWN -> {
            val festival = dailyProfile.festivals.firstOrNull()?.name 
                ?: dailyProfile.vrats.firstOrNull()?.name 
                ?: "Ekadashi Vrat"
            textStr = "🎉 $festival"
            color = Color(0xFFF43F5E)
        }
        ComplicationType.MINDFULNESS_RING -> {
            textStr = "🧘 Sadhana 100%"
            color = Color(0xFF2DD4BF)
        }
        ComplicationType.BATTERY_STEPS -> {
            textStr = "$battery% ⚡ | $steps 🚶"
            color = Color(0xFF94A3B8)
        }
    }
    
    // Prevent infinite map growth and GC pressure from dynamic strings
    if (complicationTextCache.size > 30) {
        complicationTextCache.clear()
    }

    // Cache key includes color to ensure correct tinting if it changes
    val cacheKey = "${textStr}_${color.toArgb()}"
    val measured = complicationTextCache.getOrPut(cacheKey) {
        textMeasurer.measure(
            text = textStr,
            style = TextStyle(color = color, fontSize = 9.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        )
    }
    
    val pillWidth = measured.size.width + 16.dp.toPx()
    val pillHeight = measured.size.height + 8.dp.toPx()
    
    if (isBadge) {
        drawRoundRect(
            color = badgeBgColor.copy(alpha = 0.35f),
            topLeft = Offset(pos.x - pillWidth / 2f, pos.y - pillHeight / 2f),
            size = Size(pillWidth, pillHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
        )
        drawRoundRect(
            color = badgeBgColor,
            topLeft = Offset(pos.x - pillWidth / 2f, pos.y - pillHeight / 2f),
            size = Size(pillWidth, pillHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
            style = Stroke(width = 1.2.dp.toPx())
        )
    } else {
        // High-contrast semi-transparent pill container for dark theme readability
        drawRoundRect(
            color = Color(0xEB020617),
            topLeft = Offset(pos.x - pillWidth / 2f, pos.y - pillHeight / 2f),
            size = Size(pillWidth, pillHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
        )
        drawRoundRect(
            color = color.copy(alpha = 0.45f),
            topLeft = Offset(pos.x - pillWidth / 2f, pos.y - pillHeight / 2f),
            size = Size(pillWidth, pillHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
    }
    
    drawText(measured, topLeft = Offset(pos.x - measured.size.width / 2f, pos.y - measured.size.height / 2f))
}

private fun DrawScope.renderHands(
    currentTime: LocalTime,
    center: Offset,
    radius: Float,
    dialModel: ProceduralWatchFaceModel,
    isAmbient: Boolean
) {
    val nanoFrac = currentTime.nano / 1_000_000_000.0
    val exactSec = currentTime.second + nanoFrac
    val exactMin = currentTime.minute + (exactSec / 60.0)
    val exactHour = (currentTime.hour % 12) + (exactMin / 60.0)

    val hourAngle = Math.toRadians((exactHour * 30.0) - 90.0)
    val minAngle = Math.toRadians((exactMin * 6.0) - 90.0)
    val secAngle = Math.toRadians((exactSec * 6.0) - 90.0)

    // Hour Hand
    val hourLen = radius * 0.50f
    val hourEnd = Offset(center.x + (hourLen * cos(hourAngle)).toFloat(), center.y + (hourLen * sin(hourAngle)).toFloat())
    drawLine(
        color = if (isAmbient) DarkText else Color(dialModel.hourHandColorHex),
        start = center,
        end = hourEnd,
        strokeWidth = if (isAmbient) 3.dp.toPx() else 4.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Minute Hand
    val minLen = radius * 0.72f
    val minEnd = Offset(center.x + (minLen * cos(minAngle)).toFloat(), center.y + (minLen * sin(minAngle)).toFloat())
    drawLine(
        color = if (isAmbient) Color.LightGray else Color(dialModel.minuteHandColorHex),
        start = center,
        end = minEnd,
        strokeWidth = if (isAmbient) 2.dp.toPx() else 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Second Hand (Completely omitted in Ambient AOD to preserve OLED screen & battery)
    if (!isAmbient) {
        val secLen = radius * 0.82f
        val secEnd = Offset(center.x + (secLen * cos(secAngle)).toFloat(), center.y + (secLen * sin(secAngle)).toFloat())
        drawLine(
            color = Color(dialModel.secondHandColorHex),
            start = center,
            end = secEnd,
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
