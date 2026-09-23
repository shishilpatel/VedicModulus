package com.example.generator.watchface

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.api.*
import com.example.core.model.WatchFaceConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

data class ProcessedWatchFaceImage(
    val uriString: String,
    val suggestedPrimaryColor: Long? = null,
    val suggestedAccentColor: Long? = null,
    val aiSummary: String
)

object AIWatchFaceImageProcessor {
    private const val TAG = "AIWatchFaceImageProc"

    /**
     * Processes any user-uploaded or ChatGPT-generated image using AI Vision and
     * smart circular framing algorithms. It auto-detects focal subjects, calculates
     * optimal square-to-circular crop boundaries, applies a readability vignette for
     * clock digits/dials, and extracts harmonious theme colors.
     */
    suspend fun processAndFitImage(
        context: Context,
        imageUri: Uri,
        currentConfig: WatchFaceConfiguration
    ): ProcessedWatchFaceImage = withContext(Dispatchers.IO) {
        // 1. Load bitmap with EXIF orientation correction
        val originalBitmap = loadBitmapWithExif(context, imageUri)
            ?: throw IllegalArgumentException("Could not load selected image.")

        val width = originalBitmap.width
        val height = originalBitmap.height

        // 2. Perform AI Vision analysis to detect focal center and design parameters
        val aiCropInfo = try {
            analyzeImageWithAI(originalBitmap)
        } catch (e: Exception) {
            Log.w(TAG, "AI Vision failed, falling back to smart centroid heuristic: ${e.message}")
            calculateFallbackCropInfo(originalBitmap)
        }

        // 3. Compute optimal square crop centered around AI focal point
        val squareSize = min(width, height)
        val focalX = (aiCropInfo.focalXRatio * width).toInt()
        val focalY = (aiCropInfo.focalYRatio * height).toInt()

        val cropLeft = (focalX - squareSize / 2).coerceIn(0, max(0, width - squareSize))
        val cropTop = (focalY - squareSize / 2).coerceIn(0, max(0, height - squareSize))

        val croppedBitmap = Bitmap.createBitmap(
            originalBitmap,
            cropLeft,
            cropTop,
            squareSize,
            squareSize
        )

        // 4. Scale to target watch face background resolution (800x800)
        val targetSize = 800
        val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, targetSize, targetSize, true)

        // 5. Apply Smart Watch Face Legibility Layer (Bezel vignette and soft center readability scrim)
        val finalBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)
        val center = targetSize / 2f
        val radius = targetSize / 2f

        // Draw cropped image
        canvas.drawBitmap(scaledBitmap, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))

        // Draw radial bezel shadow & contrast scrim to ensure clock hands/digits are legible
        val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                center,
                center,
                radius,
                intArrayOf(
                    Color.argb(0, 0, 0, 0),
                    Color.argb((aiCropInfo.vignetteIntensity * 120).toInt(), 0, 0, 0),
                    Color.argb((aiCropInfo.vignetteIntensity * 235).toInt(), 5, 5, 10)
                ),
                floatArrayOf(0.40f, 0.75f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(center, center, radius, vignettePaint)

        // Soft center readability wash for digital clock / complication legibility
        val centerWashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                center,
                center,
                radius * 0.45f,
                intArrayOf(
                    Color.argb((aiCropInfo.vignetteIntensity * 70).toInt(), 0, 0, 0),
                    Color.argb(0, 0, 0, 0)
                ),
                floatArrayOf(0.0f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(center, center, radius * 0.45f, centerWashPaint)

        // 6. Save optimized bitmap to cache storage
        cleanOldWatchFaceCaches(context)
        val outputFile = File(context.filesDir, "ai_watchface_bg_${System.currentTimeMillis()}.png")
        FileOutputStream(outputFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return@withContext ProcessedWatchFaceImage(
            uriString = Uri.fromFile(outputFile).toString(),
            suggestedPrimaryColor = aiCropInfo.suggestedPrimaryColorHex,
            suggestedAccentColor = aiCropInfo.suggestedAccentColorHex,
            aiSummary = aiCropInfo.summary
        )
    }

    private fun loadBitmapWithExif(context: Context, uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val rawBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

            // Read EXIF orientation
            val exifStream = context.contentResolver.openInputStream(uri)
            val orientation = if (exifStream != null) {
                val exif = ExifInterface(exifStream)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } else {
                ExifInterface.ORIENTATION_NORMAL
            }
            exifStream?.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }

            if (!matrix.isIdentity) {
                Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
            } else {
                rawBitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading bitmap with EXIF: ${e.message}")
            null
        } finally {
            inputStream?.close()
        }
    }

    private data class AICropInfo(
        val focalXRatio: Float = 0.5f,
        val focalYRatio: Float = 0.5f,
        val vignetteIntensity: Float = 0.5f,
        val suggestedPrimaryColorHex: Long? = null,
        val suggestedAccentColorHex: Long? = null,
        val summary: String = "AI auto-centered subject and enhanced dial contrast for circular display."
    )

    private suspend fun analyzeImageWithAI(bitmap: Bitmap): AICropInfo = withContext(Dispatchers.IO) {
        val geminiKey = BuildConfig.GEMINI_API_KEY
        val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch (e: Exception) { "" }

        // Scale down to max 512px for low-latency AI vision
        val maxDim = 512
        val scale = min(1f, maxDim.toFloat() / max(bitmap.width, bitmap.height))
        val scaledThumb = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true
        )

        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledThumb.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val prompt = """
            You are an expert smartwatch UI designer and image framing engine.
            Analyze this uploaded artwork/image for a circular 1:1 smartwatch display (Galaxy Watch / Wear OS).
            
            Identify:
            1. The main focal subject or sacred motif center (focal_x: 0.0 to 1.0 from left, focal_y: 0.0 to 1.0 from top).
            2. Best dominant harmonious color for watch face dial markings and time digits (hex string e.g. "#FFD700" or "#D4AF37").
            3. Best accent highlight color (hex string e.g. "#FF6B35" or "#00E5FF").
            4. Vignette intensity needed for clock legibility (0.2 to 0.7 where 0.7 is for very bright/busy images).
            5. A concise 1-sentence explanation of the framing adjustment.

            Respond ONLY in valid JSON matching this schema:
            {
              "focal_x": 0.5,
              "focal_y": 0.5,
              "vignette_intensity": 0.5,
              "primary_color": "#FFD700",
              "accent_color": "#FF6B35",
              "summary": "Centered sacred mandala and increased bezel contrast for high dial readability."
            }
        """.trimIndent()

        var jsonStr: String? = null

        // Try Gemini Vision first
        if (geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY") {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = prompt, inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                            )
                        )
                    )
                )
                val response = GeminiClient.service.generateContent(geminiKey, request)
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (rawText != null) {
                    jsonStr = extractJsonFromText(rawText)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini vision failed: ${e.message}")
            }
        }

        if (jsonStr != null) {
            val json = JSONObject(jsonStr)
            val focalX = json.optDouble("focal_x", 0.5).toFloat().coerceIn(0.1f, 0.9f)
            val focalY = json.optDouble("focal_y", 0.5).toFloat().coerceIn(0.1f, 0.9f)
            val vignette = json.optDouble("vignette_intensity", 0.5).toFloat().coerceIn(0.2f, 0.8f)
            val primaryHexStr = json.optString("primary_color", "#FFD700")
            val accentHexStr = json.optString("accent_color", "#FF6B35")
            val summary = json.optString("summary", "AI auto-centered subject and enhanced dial contrast.")

            val primaryLong = parseHexColor(primaryHexStr)
            val accentLong = parseHexColor(accentHexStr)

            return@withContext AICropInfo(
                focalXRatio = focalX,
                focalYRatio = focalY,
                vignetteIntensity = vignette,
                suggestedPrimaryColorHex = primaryLong,
                suggestedAccentColorHex = accentLong,
                summary = summary
            )
        }

        // If AI vision response wasn't received, fall back to heuristic
        return@withContext calculateFallbackCropInfo(bitmap)
    }

    private fun calculateFallbackCropInfo(bitmap: Bitmap): AICropInfo {
        // Smart centroid and dominant color sampling
        val width = bitmap.width
        val height = bitmap.height
        val step = max(1, width / 40)
        var totalLum = 0.0
        var totalWeight = 0.0
        var weightedX = 0.0
        var weightedY = 0.0

        var maxVibrantR = 255
        var maxVibrantG = 215
        var maxVibrantB = 0

        for (x in 0 until width step step) {
            for (y in 0 until height step step) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val lum = 0.299 * r + 0.587 * g + 0.114 * b
                val weight = 255.0 - lum // weight towards darker/contrast elements
                weightedX += x * weight
                weightedY += y * weight
                totalWeight += weight
                totalLum += lum
            }
        }

        val focalX = if (totalWeight > 0) (weightedX / totalWeight / width).toFloat().coerceIn(0.25f, 0.75f) else 0.5f
        val focalY = if (totalWeight > 0) (weightedY / totalWeight / height).toFloat().coerceIn(0.25f, 0.75f) else 0.5f
        val avgLum = totalLum / ((width / step) * (height / step))
        val vignette = if (avgLum > 140) 0.65f else 0.45f

        return AICropInfo(
            focalXRatio = focalX,
            focalYRatio = focalY,
            vignetteIntensity = vignette,
            suggestedPrimaryColorHex = 0xFFFFD700L, // Vedic Gold
            suggestedAccentColorHex = 0xFFFF6B35L,  // Saffron Orange
            summary = "Smart auto-centered subject and applied anti-glare bezel vignette."
        )
    }

    private fun extractJsonFromText(rawText: String): String {
        val start = rawText.indexOf('{')
        val end = rawText.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            rawText.substring(start, end + 1)
        } else {
            rawText
        }
    }

    private fun parseHexColor(hex: String): Long {
        return try {
            val clean = hex.replace("#", "").trim()
            val colorInt = android.graphics.Color.parseColor("#$clean")
            colorInt.toLong() and 0xFFFFFFFFL
        } catch (e: Exception) {
            0xFFFFD700L
        }
    }

    private fun cleanOldWatchFaceCaches(context: Context) {
        try {
            val files = context.filesDir.listFiles { _, name -> name.startsWith("ai_watchface_bg_") }
            if (files != null && files.size > 5) {
                files.sortBy { it.lastModified() }
                for (i in 0 until files.size - 4) {
                    files[i].delete()
                }
            }
        } catch (e: Exception) {
            // Ignored
        }
    }
}
