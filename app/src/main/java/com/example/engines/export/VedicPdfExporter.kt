package com.example.engines.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.core.model.Graha
import com.example.core.model.KundaliChartData
import com.example.core.model.PersonalizedProfile
import com.example.core.model.Rashi
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

object VedicPdfExporter {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points at 72 dpi
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points at 72 dpi

    fun exportKundliToPdf(
        context: Context,
        profile: BirthProfile,
        personalizedProfile: PersonalizedProfile,
        kundaliData: KundaliChartData
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        canvas.drawColor(Color.WHITE)

        // Decorative Double Border (Maroon & Gold)
        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(128, 0, 32) // Maroon
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(20f, 20f, (PAGE_WIDTH - 20).toFloat(), (PAGE_HEIGHT - 20).toFloat(), outerBorderPaint)

        val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(212, 160, 23) // Gold
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(24f, 24f, (PAGE_WIDTH - 24).toFloat(), (PAGE_HEIGHT - 24).toFloat(), innerBorderPaint)

        // Header Background Banner
        val headerBannerPaint = Paint().apply {
            color = Color.rgb(253, 248, 235) // Warm parchment
            style = Paint.Style.FILL
        }
        canvas.drawRect(25f, 25f, (PAGE_WIDTH - 25).toFloat(), 95f, headerBannerPaint)

        // Invocation
        paint.color = Color.rgb(180, 83, 9) // Amber/Saffron
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("॥ श्री गणेशाय नमः ॥", (PAGE_WIDTH / 2).toFloat(), 44f, paint)

        // Main Title
        paint.color = Color.rgb(128, 0, 32) // Maroon
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("JANAM KUNDLI (जन्म पत्रिका)", (PAGE_WIDTH / 2).toFloat(), 66f, paint)

        // Subtitle
        paint.color = Color.rgb(74, 85, 104)
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText(
            "Vedic Astrological Horoscope • Sidereal System • ${profile.ayanamsha.displayName} Ayanamsha",
            (PAGE_WIDTH / 2).toFloat(),
            82f,
            paint
        )

        // Horizontal divider
        paint.color = Color.rgb(212, 160, 23)
        paint.strokeWidth = 1.5f
        canvas.drawLine(25f, 95f, (PAGE_WIDTH - 25).toFloat(), 95f, paint)

        // Section 1: Natal Profile Details
        var currentY = 112f
        paint.textAlign = Paint.Align.LEFT

        val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(128, 0, 32)
            textSize = 11f
            isFakeBoldText = true
        }
        canvas.drawText("1. BIRTH PARTICULARS & GEOGRAPHICAL DATA", 35f, currentY, sectionTitlePaint)
        currentY += 16f

        val dtfDate = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US)
        val dtfTime = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
            isFakeBoldText = true
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            isFakeBoldText = false
        }

        // Row 1
        canvas.drawText("Name:", 35f, currentY, labelPaint)
        canvas.drawText(profile.fullName, 85f, currentY, valuePaint)
        canvas.drawText("Date of Birth:", 310f, currentY, labelPaint)
        canvas.drawText(profile.birthDate.format(dtfDate), 385f, currentY, valuePaint)
        currentY += 15f

        // Row 2
        canvas.drawText("Time:", 35f, currentY, labelPaint)
        canvas.drawText(profile.birthTime.format(dtfTime), 85f, currentY, valuePaint)
        canvas.drawText("Place of Birth:", 310f, currentY, labelPaint)
        canvas.drawText("${profile.birthLocation.cityName}, ${profile.birthLocation.state}", 385f, currentY, valuePaint)
        currentY += 15f

        // Row 3
        canvas.drawText("Coordinates:", 35f, currentY, labelPaint)
        val latStr = String.format(Locale.US, "%.3f° %s", Math.abs(profile.birthLocation.latitude), if (profile.birthLocation.latitude >= 0) "N" else "S")
        val lonStr = String.format(Locale.US, "%.3f° %s", Math.abs(profile.birthLocation.longitude), if (profile.birthLocation.longitude >= 0) "E" else "W")
        canvas.drawText("$latStr, $lonStr", 105f, currentY, valuePaint)
        canvas.drawText("Timezone:", 310f, currentY, labelPaint)
        canvas.drawText(profile.birthLocation.timezoneId, 385f, currentY, valuePaint)
        currentY += 22f

        // Section 2: Core Vedic Markers (Panchanga / Key Signs)
        canvas.drawText("2. CORE JYOTISH MARKERS & SENSITIVE POINTS", 35f, currentY, sectionTitlePaint)
        currentY += 16f

        // Row 1 markers
        canvas.drawText("Lagna (Ascendant):", 35f, currentY, labelPaint)
        canvas.drawText("${personalizedProfile.natalLagnaRashi.sanskritName} (${personalizedProfile.natalLagnaRashi.westernName}) ${String.format(Locale.US, "%.1f°", kundaliData.lagnaDegrees)}", 130f, currentY, valuePaint)
        canvas.drawText("Janma Nakshatra:", 310f, currentY, labelPaint)
        canvas.drawText("${personalizedProfile.natalNakshatra.sanskritName} (Pada ${personalizedProfile.natalNakshatraPada})", 410f, currentY, valuePaint)
        currentY += 15f

        // Row 2 markers
        canvas.drawText("Chandra Rashi (Moon):", 35f, currentY, labelPaint)
        canvas.drawText("${personalizedProfile.natalChandraRashi.sanskritName} (${personalizedProfile.natalChandraRashi.westernName})", 145f, currentY, valuePaint)
        canvas.drawText("Surya Rashi (Sun):", 310f, currentY, labelPaint)
        canvas.drawText("${personalizedProfile.natalSuryaRashi.sanskritName} (${personalizedProfile.natalSuryaRashi.westernName})", 410f, currentY, valuePaint)
        currentY += 24f

        // Section 3: Kundali Diagram (Left) & Planetary Positions Table (Right)
        canvas.drawText("3. LAGNA KUNDLI (D1 CHART) & PLANETARY POSITIONS", 35f, currentY, sectionTitlePaint)
        currentY += 14f

        // Draw North Indian Kundli on the left side
        val chartBoxLeft = 35f
        val chartBoxTop = currentY
        val chartBoxSize = 220f
        drawNorthIndianKundaliOnCanvas(canvas, chartBoxLeft, chartBoxTop, chartBoxSize, kundaliData)

        // Draw Planetary Positions Table on the right side
        val tableLeft = 275f
        val tableTop = currentY
        drawPlanetaryTable(canvas, tableLeft, tableTop, kundaliData)

        currentY = chartBoxTop + chartBoxSize + 24f

        // Section 4: Bhava Significations & Vimshottari Dasha Summary
        canvas.drawText("4. VIMSHOTTARI DASHA TIMELINE", 35f, currentY, sectionTitlePaint)
        currentY += 16f

        if (personalizedProfile.dashaPeriods.isNotEmpty()) {
            val dashaHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(128, 0, 32)
                textSize = 8.5f
                isFakeBoldText = true
            }
            val dashaTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(30, 41, 59)
                textSize = 8.5f
            }

            var dashaX = 35f
            personalizedProfile.dashaPeriods.take(6).forEach { dasha ->
                val bgPaint = Paint().apply {
                    color = if (dasha.isCurrent) Color.rgb(254, 243, 199) else Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint().apply {
                    color = if (dasha.isCurrent) Color.rgb(217, 119, 6) else Color.rgb(226, 232, 240)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                canvas.drawRect(dashaX, currentY, dashaX + 82f, currentY + 36f, bgPaint)
                canvas.drawRect(dashaX, currentY, dashaX + 82f, currentY + 36f, borderPaint)

                val activeTag = if (dasha.isCurrent) " [ACTIVE]" else ""
                canvas.drawText("${dasha.lord.displayName}$activeTag", dashaX + 4f, currentY + 12f, dashaHeaderPaint)
                canvas.drawText("${dasha.startDate.year} – ${dasha.endDate.year}", dashaX + 4f, currentY + 26f, dashaTextPaint)
                dashaX += 87f
            }
            currentY += 46f
        }

        // Section 5: Traditional Guidance and Disclaimer
        val footerBg = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        canvas.drawRect(25f, (PAGE_HEIGHT - 65).toFloat(), (PAGE_WIDTH - 25).toFloat(), (PAGE_HEIGHT - 25).toFloat(), footerBg)

        val footerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "॥ धर्मो रक्षति रक्षितः ॥ • Vedic Jyotish is a sacred Vedanga providing spiritual guidance and introspection.",
            (PAGE_WIDTH / 2).toFloat(),
            (PAGE_HEIGHT - 48).toFloat(),
            footerTextPaint
        )
        canvas.drawText(
            "Generated authentically via Vedic Astrology Suite. Planetary calculations based on precise astronomical models and Nirayana Lahiri Ayanamsha.",
            (PAGE_WIDTH / 2).toFloat(),
            (PAGE_HEIGHT - 34).toFloat(),
            footerTextPaint
        )

        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val cleanName = profile.fullName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val outputFile = File(context.cacheDir, "Janam_Kundli_${cleanName}.pdf")
        try {
            val fos = FileOutputStream(outputFile)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    private fun drawNorthIndianKundaliOnCanvas(
        canvas: Canvas,
        left: Float,
        top: Float,
        size: Float,
        kundaliData: KundaliChartData
    ) {
        val right = left + size
        val bottom = top + size
        val midX = left + size / 2f
        val midY = top + size / 2f

        // Box border
        val chartBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(128, 0, 32) // Maroon
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val bgPaint = Paint().apply {
            color = Color.rgb(255, 253, 248)
            style = Paint.Style.FILL
        }
        canvas.drawRect(left, top, right, bottom, bgPaint)
        canvas.drawRect(left, top, right, bottom, chartBorderPaint)

        // Inner Diamond
        val diamondPath = Path().apply {
            moveTo(midX, top)
            lineTo(right, midY)
            lineTo(midX, bottom)
            lineTo(left, midY)
            close()
        }
        canvas.drawPath(diamondPath, chartBorderPaint)

        // Diagonals
        val thinLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(180, 83, 9)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        canvas.drawLine(left, top, right, bottom, thinLinePaint)
        canvas.drawLine(left, bottom, right, top, thinLinePaint)

        // House Coordinates for North Indian Kundli (12 houses)
        val houseCenters = listOf(
            Pair(midX, top + size * 0.22f),               // House 1 (Top diamond)
            Pair(left + size * 0.25f, top + size * 0.12f),// House 2 (Top-left triangle)
            Pair(left + size * 0.12f, top + size * 0.25f),// House 3 (Left-top triangle)
            Pair(left + size * 0.22f, midY),              // House 4 (Left diamond)
            Pair(left + size * 0.12f, top + size * 0.75f),// House 5 (Left-bottom triangle)
            Pair(left + size * 0.25f, top + size * 0.88f),// House 6 (Bottom-left triangle)
            Pair(midX, top + size * 0.78f),               // House 7 (Bottom diamond)
            Pair(left + size * 0.75f, top + size * 0.88f),// House 8 (Bottom-right triangle)
            Pair(left + size * 0.88f, top + size * 0.75f),// House 9 (Right-bottom triangle)
            Pair(left + size * 0.78f, midY),              // House 10 (Right diamond)
            Pair(left + size * 0.88f, top + size * 0.25f),// House 11 (Right-top triangle)
            Pair(left + size * 0.75f, top + size * 0.12f) // House 12 (Top-right triangle)
        )

        val rashiNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(180, 83, 9) // Saffron/amber
            textSize = 8.5f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(128, 0, 32) // Maroon
            textSize = 7.5f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        kundaliData.bhavas.forEachIndexed { idx, bhava ->
            if (idx < houseCenters.size) {
                val center = houseCenters[idx]
                // Rashi number in small font
                canvas.drawText("${bhava.rashi.number}", center.first, center.second - 4f, rashiNumberPaint)

                // Planets in house
                if (bhava.occupyingGrahas.isNotEmpty()) {
                    val planetSymbols = bhava.occupyingGrahas.joinToString(" ") { grahaAbbreviation(it) }
                    canvas.drawText(planetSymbols, center.first, center.second + 8f, planetPaint)
                }
            }
        }
    }

    private fun drawPlanetaryTable(
        canvas: Canvas,
        left: Float,
        top: Float,
        kundaliData: KundaliChartData
    ) {
        val tableWidth = 285f
        val right = left + tableWidth

        // Header Box
        val headerPaint = Paint().apply {
            color = Color.rgb(128, 0, 32)
            style = Paint.Style.FILL
        }
        canvas.drawRect(left, top, right, top + 18f, headerPaint)

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 8f
            isFakeBoldText = true
        }

        canvas.drawText("GRAHA", left + 6f, top + 12f, headerTextPaint)
        canvas.drawText("RASHI", left + 70f, top + 12f, headerTextPaint)
        canvas.drawText("DEGREE", left + 140f, top + 12f, headerTextPaint)
        canvas.drawText("BHAVA", left + 200f, top + 12f, headerTextPaint)
        canvas.drawText("STATUS", left + 245f, top + 12f, headerTextPaint)

        var rowY = top + 18f
        val rowHeight = 17f
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }
        val altBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        val cellTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8f
        }
        val boldCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(128, 0, 32)
            textSize = 8f
            isFakeBoldText = true
        }

        // Ascendant / Lagna row
        val lagnaRashi = kundaliData.lagnaRashi
        val lagnaDegInRashi = kundaliData.lagnaDegrees % 30.0
        canvas.drawRect(left, rowY, right, rowY + rowHeight, altBgPaint)
        canvas.drawLine(left, rowY + rowHeight, right, rowY + rowHeight, borderPaint)
        canvas.drawText("Lagna (Asc)", left + 6f, rowY + 12f, boldCellPaint)
        canvas.drawText(lagnaRashi.sanskritName, left + 70f, rowY + 12f, cellTextPaint)
        canvas.drawText(String.format(Locale.US, "%.1f°", lagnaDegInRashi), left + 140f, rowY + 12f, cellTextPaint)
        canvas.drawText("House 1", left + 200f, rowY + 12f, cellTextPaint)
        canvas.drawText("Lagna", left + 245f, rowY + 12f, cellTextPaint)
        rowY += rowHeight

        // 9 Grahas
        val allGrahas = listOf(
            Graha.SURYA, Graha.CHANDRA, Graha.MANGALA, Graha.BUDHA,
            Graha.GURU, Graha.SHUKRA, Graha.SHANI, Graha.RAHU, Graha.KETU
        )

        allGrahas.forEachIndexed { i, graha ->
            val deg = kundaliData.grahaPositions[graha] ?: 0.0
            val rashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)
            val rashi = Rashi.values()[rashiIdx]
            val degInRashi = deg % 30.0
            val houseNum = ((rashiIdx - kundaliData.lagnaRashi.ordinal + 12) % 12) + 1

            if (i % 2 == 1) {
                canvas.drawRect(left, rowY, right, rowY + rowHeight, altBgPaint)
            }
            canvas.drawLine(left, rowY + rowHeight, right, rowY + rowHeight, borderPaint)

            canvas.drawText("${graha.symbol} ${graha.displayName}", left + 6f, rowY + 12f, boldCellPaint)
            canvas.drawText(rashi.sanskritName, left + 70f, rowY + 12f, cellTextPaint)
            canvas.drawText(String.format(Locale.US, "%.1f°", degInRashi), left + 140f, rowY + 12f, cellTextPaint)
            canvas.drawText("House $houseNum", left + 200f, rowY + 12f, cellTextPaint)
            val dignity = when (graha) {
                Graha.SURYA -> if (rashi == Rashi.MESHA) "Exalted" else if (rashi == Rashi.TULA) "Debilitated" else "Direct"
                Graha.CHANDRA -> if (rashi == Rashi.VRISHABHA) "Exalted" else if (rashi == Rashi.VRISHCHIKA) "Debilitated" else "Direct"
                Graha.GURU -> if (rashi == Rashi.KARKATA) "Exalted" else if (rashi == Rashi.MAKARA) "Debilitated" else "Direct"
                Graha.SHANI -> if (rashi == Rashi.TULA) "Exalted" else if (rashi == Rashi.MESHA) "Debilitated" else "Direct"
                Graha.RAHU, Graha.KETU -> "Retrograde"
                else -> "Direct"
            }
            canvas.drawText(dignity, left + 245f, rowY + 12f, cellTextPaint)

            rowY += rowHeight
        }

        // Outer border for table
        val tableBorder = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(left, top, right, rowY, tableBorder)
    }

    private fun grahaAbbreviation(graha: Graha): String {
        return when (graha) {
            Graha.SURYA -> "Su"
            Graha.CHANDRA -> "Mo"
            Graha.MANGALA -> "Ma"
            Graha.BUDHA -> "Me"
            Graha.GURU -> "Ju"
            Graha.SHUKRA -> "Ve"
            Graha.SHANI -> "Sa"
            Graha.RAHU -> "Ra"
            Graha.KETU -> "Ke"
        }
    }

    fun exportDailyPanchangToPdf(
        context: Context,
        dailyProfile: DailyVedicProfile,
        cityName: String
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)

        // Borders
        val outerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(128, 0, 32)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(20f, 20f, (PAGE_WIDTH - 20).toFloat(), (PAGE_HEIGHT - 20).toFloat(), outerBorder)

        val innerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(212, 160, 23)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(24f, 24f, (PAGE_WIDTH - 24).toFloat(), (PAGE_HEIGHT - 24).toFloat(), innerBorder)

        // Header Banner
        val bannerPaint = Paint().apply {
            color = Color.rgb(253, 248, 235)
            style = Paint.Style.FILL
        }
        canvas.drawRect(25f, 25f, (PAGE_WIDTH - 25).toFloat(), 95f, bannerPaint)

        paint.color = Color.rgb(180, 83, 9)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("॥ श्री सूर्यदेवाय नमः ॥", (PAGE_WIDTH / 2).toFloat(), 44f, paint)

        paint.color = Color.rgb(128, 0, 32)
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("DAINIK VEDIC PANCHANG (दैनिक पञ्चाङ्ग)", (PAGE_WIDTH / 2).toFloat(), 66f, paint)

        paint.color = Color.rgb(74, 85, 104)
        paint.textSize = 9f
        paint.isFakeBoldText = false
        val dateFormatted = dailyProfile.currentZonedDateTime.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))
        canvas.drawText("$dateFormatted • Location: $cityName", (PAGE_WIDTH / 2).toFloat(), 82f, paint)

        val divPaint = Paint().apply {
            color = Color.rgb(212, 160, 23)
            strokeWidth = 1.5f
        }
        canvas.drawLine(25f, 95f, (PAGE_WIDTH - 25).toFloat(), 95f, divPaint)

        // Section: The Five Limbs (Panch-Anga)
        var y = 120f
        val secPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(128, 0, 32)
            textSize = 12f
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("THE FIVE SACRED LIMBS (पञ्चाङ्ग तत्व)", 40f, y, secPaint)
        y += 18f

        val timeFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        val p = dailyProfile.panchanga

        val items = listOf(
            "1. Tithi (तिथि)" to "${p.tithi.name} (${p.tithi.paksha.displayName})",
            "2. Vaara (वार)" to "${p.vara} (${p.varaDevanagari})",
            "3. Nakshatra (नक्षत्र)" to "${p.nakshatra.sanskritName} (Pada ${p.nakshatraPada})",
            "4. Yoga (योग)" to p.yoga,
            "5. Karana (करण)" to p.karana,
            "Vikram Samvat" to "${p.vikramSamvat} | Shaka: ${p.shakaSamvat}",
            "Masa / Ritu / Ayana" to "${p.masa} • ${p.ritu} • ${p.ayana}",
            "Sunrise & Sunset" to "Sunrise: ${p.sunrise.format(timeFmt)} | Sunset: ${p.sunset.format(timeFmt)}"
        )

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(55, 65, 81)
            textSize = 10f
            isFakeBoldText = true
        }
        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(17, 24, 39)
            textSize = 10f
        }

        items.chunked(2).forEach { row ->
            val first = row[0]
            canvas.drawText(first.first + ":", 45f, y, labelPaint)
            canvas.drawText(first.second, 185f, y, valPaint)

            if (row.size > 1) {
                val second = row[1]
                canvas.drawText(second.first + ":", 310f, y, labelPaint)
                canvas.drawText(second.second, 440f, y, valPaint)
            }
            y += 18f
        }

        y += 15f
        canvas.drawLine(40f, y, (PAGE_WIDTH - 40).toFloat(), y, divPaint)
        y += 20f

        // Auspicious & Inauspicious Muhurtas Section
        canvas.drawText("SHUBHA & ASHUBHA MUHURTAS (शुभ एवं अशुभ काल)", 40f, y, secPaint)
        y += 18f

        val brahma = "${dailyProfile.brahmaMuhurta.startTime.format(timeFmt)} - ${dailyProfile.brahmaMuhurta.endTime.format(timeFmt)}"
        val abhijit = dailyProfile.abhijitMuhurta?.let { "${it.startTime.format(timeFmt)} - ${it.endTime.format(timeFmt)}" } ?: "N/A"
        val rahu = "${dailyProfile.rahuKalam.startTime.format(timeFmt)} - ${dailyProfile.rahuKalam.endTime.format(timeFmt)}"
        val yama = "${dailyProfile.yamaganda.startTime.format(timeFmt)} - ${dailyProfile.yamaganda.endTime.format(timeFmt)}"
        val gulika = "${dailyProfile.gulikaKalam.startTime.format(timeFmt)} - ${dailyProfile.gulikaKalam.endTime.format(timeFmt)}"

        val muhurtaRows = listOf(
            "Brahma Muhurta (Meditation / Study)" to brahma,
            "Abhijit Muhurta (Universal Victory)" to abhijit,
            "Rahu Kaalam (Avoid New Beginnings)" to rahu,
            "Yamaganda Kalam" to yama,
            "Gulika Kalam" to gulika
        )

        muhurtaRows.forEach { (name, time) ->
            canvas.drawText(name + ":", 45f, y, labelPaint)
            canvas.drawText(time, 275f, y, valPaint)
            y += 16f
        }

        y += 15f
        canvas.drawLine(40f, y, (PAGE_WIDTH - 40).toFloat(), y, divPaint)
        y += 20f

        // Choghadiya & Hora Status
        canvas.drawText("ACTIVE CHOGHADIYA & HORA (चौघड़िया एवं होरा)", 40f, y, secPaint)
        y += 18f

        val activeChoghadiya = "${dailyProfile.currentChoghadiya.type.displayName} (${dailyProfile.currentChoghadiya.type.quality})"
        val activeHora = "${dailyProfile.currentHora.rulingGraha.displayName} (${dailyProfile.currentHora.rulingGraha.sanskritName})"

        canvas.drawText("Current Active Choghadiya:", 45f, y, labelPaint)
        canvas.drawText(activeChoghadiya, 220f, y, valPaint)
        y += 16f

        canvas.drawText("Current Ruling Planetary Hora:", 45f, y, labelPaint)
        canvas.drawText(activeHora, 220f, y, valPaint)
        y += 20f

        // Active Festival / Vrat
        val activeFest = dailyProfile.festivals.firstOrNull()
        val activeVrat = dailyProfile.vrats.firstOrNull()
        if (activeFest != null || activeVrat != null) {
            canvas.drawLine(40f, y, (PAGE_WIDTH - 40).toFloat(), y, divPaint)
            y += 20f
            canvas.drawText("SACRED VRAT & UTSAV (त्योहार एवं व्रत)", 40f, y, secPaint)
            y += 18f

            activeFest?.let { fest ->
                canvas.drawText("Festival: ${fest.name}", 45f, y, labelPaint)
                y += 14f
                canvas.drawText(fest.description, 45f, y, valPaint)
                y += 18f
            }

            activeVrat?.let { vrat ->
                canvas.drawText("Sacred Vrat: ${vrat.name}", 45f, y, labelPaint)
                y += 14f
                canvas.drawText(vrat.description, 45f, y, valPaint)
                y += 18f
            }
        }

        // Footer disclaimer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(107, 114, 128)
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Calculated via authentic Sidereal Lahiri astronomical algorithms • Sanatana Vedic Astrology", (PAGE_WIDTH / 2).toFloat(), (PAGE_HEIGHT - 35).toFloat(), footerPaint)

        pdfDocument.finishPage(page)

        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val dateStr = dailyProfile.currentZonedDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val outputFile = File(exportDir, "Dainik_Panchang_$dateStr.pdf")
            val outputStream = FileOutputStream(outputFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            outputFile
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }

    fun sharePdf(
        context: Context,
        pdfFile: File,
        subject: String = "Vedic Astrological Report",
        message: String = "Here is the authentic Vedic document."
    ) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share PDF Document")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
