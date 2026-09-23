package com.example.generator.watchface

import android.graphics.*
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.example.core.model.WatchFaceConfiguration
import kotlin.math.cos
import kotlin.math.sin

object WatchFaceBitmapRenderer {

    private val cache = java.util.concurrent.ConcurrentHashMap<String, Bitmap>()

    /**
     * Renders a high-resolution, pixel-perfect 450x450 Bitmap representing the custom
     * Watch Face visual design created in the mobile app.
     * This bitmap is transmitted as an Asset to Wear OS 7 / Galaxy Watch to be applied
     * as the active Watch Face canvas / background.
     */
    fun renderWatchFaceBitmap(
        config: WatchFaceConfiguration,
        personalizedProfile: PersonalizedProfile,
        dailyProfile: DailyVedicProfile?,
        size: Int = 450,
        context: android.content.Context? = null
    ): Bitmap {
        val cacheKey = "${config.style.name}_${config.primaryColorHex}_${config.accentColorHex}_${config.showCelestialClock}_${config.customBackgroundUri}_$size"
        cache[cacheKey]?.let { cached ->
            if (!cached.isRecycled) return cached
        }

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        val radius = size / 2f

        val generator = WatchFaceGenerator()
        val model = generator.generateDialModel(config, personalizedProfile, dailyProfile)

        // Draw deep AMOLED black background
        val bgPaint = Paint().apply {
            color = model.backgroundFillHex.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(center, center, radius, bgPaint)

        // Draw custom or AI-fitted background image if present
        if (config.customBackgroundUri.isNotBlank()) {
            try {
                val customUri = android.net.Uri.parse(config.customBackgroundUri)
                val bgBitmap: Bitmap? = when (customUri.scheme) {
                    "file" -> BitmapFactory.decodeFile(customUri.path)
                    "content" -> {
                        context?.contentResolver?.openInputStream(customUri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                    else -> {
                        if (customUri.path != null) BitmapFactory.decodeFile(customUri.path) else null
                    }
                }
                if (bgBitmap != null) {
                    val clipPath = Path().apply {
                        addCircle(center, center, radius, Path.Direction.CCW)
                    }
                    canvas.save()
                    canvas.clipPath(clipPath)
                    val srcRect = Rect(0, 0, bgBitmap.width, bgBitmap.height)
                    val dstRect = Rect(0, 0, size, size)
                    val imgPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
                    canvas.drawBitmap(bgBitmap, srcRect, dstRect, imgPaint)
                    canvas.restore()
                }
            } catch (e: Exception) {
                // Fallback to procedural background
            }
        }

        // Draw procedural vector elements (rings, ticks, glyphs, sacred geometry)
        for (element in model.elements) {
            when (element) {
                is VectorDialElement.CircleRing -> {
                    val paint = Paint().apply {
                        color = element.colorHex.toInt()
                        style = Paint.Style.STROKE
                        strokeWidth = element.strokeWidthDp * (size / 225f)
                        isAntiAlias = true
                        if (element.isDashed) {
                            pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
                        }
                    }
                    canvas.drawCircle(center, center, radius * element.radiusRatio, paint)
                }

                is VectorDialElement.TickMarks -> {
                    val paint = Paint().apply {
                        color = element.colorHex.toInt()
                        strokeWidth = element.strokeWidthDp * (size / 225f)
                        strokeCap = Paint.Cap.ROUND
                        isAntiAlias = true
                    }
                    val step = 360.0 / element.count
                    for (i in 0 until element.count) {
                        val angleRad = Math.toRadians(i * step - 90.0)
                        val startX = center + (radius * element.startRadiusRatio * cos(angleRad)).toFloat()
                        val startY = center + (radius * element.startRadiusRatio * sin(angleRad)).toFloat()
                        val endX = center + (radius * element.endRadiusRatio * cos(angleRad)).toFloat()
                        val endY = center + (radius * element.endRadiusRatio * sin(angleRad)).toFloat()
                        canvas.drawLine(startX, startY, endX, endY, paint)
                    }
                }

                is VectorDialElement.GlyphMarker -> {
                    val paint = Paint().apply {
                        color = element.colorHex.toInt()
                        textSize = element.fontSizeSp * (size / 225f)
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    val angleRad = Math.toRadians(element.angleDeg.toDouble())
                    val x = center + (radius * element.radiusRatio * cos(angleRad)).toFloat()
                    val y = center + (radius * element.radiusRatio * sin(angleRad)).toFloat() + (paint.textSize / 3f)
                    canvas.drawText(element.text, x, y, paint)
                }

                is VectorDialElement.PolygonShape -> {
                    val paint = Paint().apply {
                        color = element.colorHex.toInt()
                        style = if (element.isFilled) Paint.Style.FILL else Paint.Style.STROKE
                        strokeWidth = element.strokeWidthDp * (size / 225f)
                        isAntiAlias = true
                    }
                    val path = Path()
                    element.normalizedVertices.forEachIndexed { index, (vx, vy) ->
                        val px = center + (vx * radius * 0.9f)
                        val py = center + (vy * radius * 0.9f)
                        if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    path.close()
                    canvas.drawPath(path, paint)
                }

                is VectorDialElement.ArcSegment -> {
                    val paint = Paint().apply {
                        color = element.colorHex.toInt()
                        style = Paint.Style.STROKE
                        strokeWidth = element.strokeWidthDp * (size / 225f)
                        strokeCap = Paint.Cap.ROUND
                        isAntiAlias = true
                    }
                    val arcRadius = radius * element.radiusRatio
                    val rect = RectF(center - arcRadius, center - arcRadius, center + arcRadius, center + arcRadius)
                    canvas.drawArc(rect, element.startAngleDeg, element.sweepAngleDeg, false, paint)
                }
            }
        }

        cache[cacheKey] = bitmap
        return bitmap
    }
}
