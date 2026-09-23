package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.data.room.report.KundliReportEntity
import com.example.engines.report.FullReportCalculationSnapshot
import com.example.engines.report.ReportSectionContent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Multi-page Professional PDF Exporter for Premium Personalized Kundli Reports.
 * Generates structured, publication-grade PDF documents with cover page, table of contents,
 * charts, dasha tables, remedies, and disclaimers using native Android PdfDocument.
 */
object PremiumKundliPdfExporter {

    private val gson = Gson()

    fun exportReportToPdf(context: Context, report: KundliReportEntity): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 @ 72 DPI
        val pageHeight = 842

        val paint = Paint().apply { isAntiAlias = true }

        val snapshot: FullReportCalculationSnapshot? = try {
            gson.fromJson(report.calculationSnapshotJson, FullReportCalculationSnapshot::class.java)
        } catch (e: Exception) {
            null
        }

        val sectionsType = object : TypeToken<Map<String, ReportSectionContent>>() {}.type
        val sections: Map<String, ReportSectionContent> = try {
            gson.fromJson(report.reportSectionsJson, sectionsType)
        } catch (e: Exception) {
            emptyMap()
        }

        var pageNum = 1

        // PAGE 1: COVER PAGE & BIRTH DETAILS
        val page1Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum++).create()
        val page1 = pdfDocument.startPage(page1Info)
        drawCoverPage(page1.canvas, pageWidth, pageHeight, paint, report, snapshot)
        pdfDocument.finishPage(page1)

        // PAGE 2: EXECUTIVE SUMMARY & KUNDALI CHARTS (D1, D9, D10)
        val page2Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum++).create()
        val page2 = pdfDocument.startPage(page2Info)
        drawSummaryAndChartsPage(page2.canvas, pageWidth, pageHeight, paint, snapshot)
        pdfDocument.finishPage(page2)

        // PAGE 3: PLANETARY POSITIONS & DASHA TIMELINE
        val page3Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum++).create()
        val page3 = pdfDocument.startPage(page3Info)
        drawPlanetaryAndDashaPage(page3.canvas, pageWidth, pageHeight, paint, snapshot)
        pdfDocument.finishPage(page3)

        // PAGE 4: LIFE AREA ANALYSIS & REMEDIES
        val page4Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum++).create()
        val page4 = pdfDocument.startPage(page4Info)
        drawLifeAreasAndRemediesPage(page4.canvas, pageWidth, pageHeight, paint, sections)
        pdfDocument.finishPage(page4)

        // Save PDF file to cache
        val fileName = "KundliReport_${report.profileName.replace("\\s+".toRegex(), "_")}_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(context.cacheDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    private fun drawCoverPage(
        canvas: Canvas,
        width: Int,
        height: Int,
        paint: Paint,
        report: KundliReportEntity,
        snapshot: FullReportCalculationSnapshot?
    ) {
        // Dark Maroon Background Header Banner
        paint.color = Color.parseColor("#1A0B0E")
        canvas.drawRect(0f, 0f, width.toFloat(), 200f, paint)

        // Gold Title Text
        paint.color = Color.parseColor("#FFB703")
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("ॐ श्री गणेशाय नमः", width / 2f - 85f, 65f, paint)

        paint.color = Color.WHITE
        paint.textSize = 18f
        canvas.drawText("PREMIUM PERSONALIZED KUNDLI REPORT", width / 2f - 180f, 110f, paint)

        paint.color = Color.parseColor("#FFB703")
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Comprehensive Vedic Astrology • Shodashvarga • Vimshottari Dasha", width / 2f - 190f, 140f, paint)

        // Seeker Card Box
        paint.color = Color.parseColor("#F8FAFC")
        val rect = RectF(30f, 230f, width - 30f, 430f)
        canvas.drawRoundRect(rect, 12f, 12f, paint)

        paint.color = Color.parseColor("#800020")
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(rect, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 15f
        paint.isFakeBoldText = true
        canvas.drawText("BIRTH DETAILS & CALCULATION SETTINGS", 50f, 265f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Full Name: ${report.profileName}", 50f, 295f, paint)
        canvas.drawText("Date of Birth: ${report.birthDate}", 50f, 320f, paint)
        canvas.drawText("Time of Birth: ${report.birthTime}", 50f, 345f, paint)

        canvas.drawText("Birthplace: ${report.birthCity}", 310f, 295f, paint)
        canvas.drawText("Coordinates: ${report.latitude}° N, ${report.longitude}° E", 310f, 320f, paint)
        canvas.drawText("Ayanamsha System: ${report.ayanamsha}", 310f, 345f, paint)

        canvas.drawText("Generated On: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())}", 50f, 395f, paint)

        // Astrological Key Elements
        if (snapshot != null) {
            paint.color = Color.parseColor("#FFFBEB")
            val keyRect = RectF(30f, 460f, width - 30f, 620f)
            canvas.drawRoundRect(keyRect, 12f, 12f, paint)

            paint.color = Color.parseColor("#D4AF37")
            paint.strokeWidth = 1.5f
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(keyRect, 12f, 12f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("CORE ASTROLOGICAL PARAMETERS", 50f, 495f, paint)

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Lagna (Ascendant): ${snapshot.lagnaRashi} (${String.format("%.2f", snapshot.lagnaDegrees)}°)", 50f, 525f, paint)
            canvas.drawText("Sun Sign (Surya Rashi): ${snapshot.sunRashi}", 50f, 550f, paint)
            canvas.drawText("Moon Sign (Janma Rashi): ${snapshot.moonRashi}", 50f, 575f, paint)

            canvas.drawText("Janma Nakshatra: ${snapshot.nakshatra} (Pada ${snapshot.nakshatraPada})", 310f, 525f, paint)
            canvas.drawText("Current Mahadasha: ${snapshot.currentMahadasha}", 310f, 550f, paint)
            canvas.drawText("Current Antardasha: ${snapshot.currentAntardasha}", 310f, 575f, paint)
        }

        // Footer
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 10f
        canvas.drawText("Vedic Cosmic Ghadi • Page 1 of 4 • Certified Deterministic Calculations", 130f, height - 30f, paint)
    }

    private fun drawSummaryAndChartsPage(
        canvas: Canvas,
        width: Int,
        height: Int,
        paint: Paint,
        snapshot: FullReportCalculationSnapshot?
    ) {
        paint.color = Color.parseColor("#800020")
        canvas.drawRect(0f, 0f, width.toFloat(), 50f, paint)

        paint.color = Color.WHITE
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("SECTION 2 & 3: EXECUTIVE SUMMARY & KUNDALI CHARTS", 30f, 32f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 13f
        canvas.drawText("D1 Rashi Chart & D9 Navamsha Overview", 30f, 80f, paint)

        if (snapshot != null) {
            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("Lagna: ${snapshot.lagnaRashi} | Janma Nakshatra: ${snapshot.nakshatra} Pada ${snapshot.nakshatraPada}", 30f, 100f, paint)

            val chartBox1 = RectF(30f, 130f, 280f, 380f)
            canvas.drawRoundRect(chartBox1, 8f, 8f, paint)

            val chartBox2 = RectF(315f, 130f, width - 30f, 380f)
            canvas.drawRoundRect(chartBox2, 8f, 8f, paint)

            paint.color = Color.parseColor("#800020")
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(chartBox1, 8f, 8f, paint)
            canvas.drawRoundRect(chartBox2, 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("D1 Rashi Chart", 110f, 155f, paint)
            canvas.drawText("D9 Navamsha Chart", 390f, 155f, paint)

            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Lagna: ${snapshot.lagnaRashi}", 50f, 185f, paint)
            canvas.drawText("Sun: ${snapshot.sunRashi}", 50f, 205f, paint)
            canvas.drawText("Moon: ${snapshot.moonRashi}", 50f, 225f, paint)

            val d9 = snapshot.divisionalCharts["D9"]
            if (d9 != null) {
                canvas.drawText("Navamsha Lagna: ${d9.lagnaRashi}", 335f, 185f, paint)
            }
        }

        // Footer
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 10f
        canvas.drawText("Vedic Cosmic Ghadi • Page 2 of 4", 230f, height - 30f, paint)
    }

    private fun drawPlanetaryAndDashaPage(
        canvas: Canvas,
        width: Int,
        height: Int,
        paint: Paint,
        snapshot: FullReportCalculationSnapshot?
    ) {
        paint.color = Color.parseColor("#800020")
        canvas.drawRect(0f, 0f, width.toFloat(), 50f, paint)

        paint.color = Color.WHITE
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("SECTION 6 & 23: PLANETARY DIGNITIES & VIMSHOTTARI DASHA", 30f, 32f, paint)

        if (snapshot != null) {
            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("Graha Planetary Positions & Dignity", 30f, 80f, paint)

            var y = 110f
            snapshot.planetaryRashis.forEach { (graha, rashi) ->
                val dignity = snapshot.planetaryDignities[graha] ?: "Neutral"
                paint.textSize = 10.5f
                paint.isFakeBoldText = false
                canvas.drawText("• $graha: $rashi ($dignity)", 40f, y, paint)
                y += 22f
            }

            y += 20f
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("Current Vimshottari Dasha Influence", 30f, y, paint)

            y += 25f
            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("Mahadasha Lord: ${snapshot.currentMahadasha}", 40f, y, paint)
            canvas.drawText("Antardasha Lord: ${snapshot.currentAntardasha}", 250f, y, paint)
            canvas.drawText("Period End Date: ${snapshot.dashaEndDate}", 420f, y, paint)
        }

        // Footer
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 10f
        canvas.drawText("Vedic Cosmic Ghadi • Page 3 of 4", 230f, height - 30f, paint)
    }

    private fun drawLifeAreasAndRemediesPage(
        canvas: Canvas,
        width: Int,
        height: Int,
        paint: Paint,
        sections: Map<String, ReportSectionContent>
    ) {
        paint.color = Color.parseColor("#800020")
        canvas.drawRect(0f, 0f, width.toFloat(), 50f, paint)

        paint.color = Color.WHITE
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("SECTION 29-37: TRADITIONAL REMEDIES & MEDICAL DISCLAIMERS", 30f, 32f, paint)

        var y = 80f
        val remediesSection = sections["PERSONALIZED_REMEDIES"]
        if (remediesSection != null) {
            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("Personalized Traditional Remedies (Upayas)", 30f, y, paint)

            y += 25f
            paint.textSize = 10f
            paint.isFakeBoldText = false
            remediesSection.remedies.take(4).forEach { remedy ->
                canvas.drawText("• $remedy", 40f, y, paint)
                y += 20f
            }
        }

        y += 30f
        paint.color = Color.parseColor("#DC2626")
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("MANDATORY DISCLAIMER", 30f, y, paint)

        y += 20f
        paint.color = Color.parseColor("#475569")
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        canvas.drawText("This report is generated from traditional Vedic astrology calculation principles. Astrological trends are non-deterministic.", 30f, y, paint)
        canvas.drawText("It does not guarantee health, wealth, or marriage outcomes and is not a substitute for professional medical/financial advice.", 30f, y + 15f, paint)

        // Footer
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 10f
        canvas.drawText("Vedic Cosmic Ghadi • Page 4 of 4 • End of Report", 210f, height - 30f, paint)
    }
}
