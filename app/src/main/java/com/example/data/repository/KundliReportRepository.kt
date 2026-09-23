package com.example.data.repository

import android.content.Context
import com.example.core.model.BirthProfile
import com.example.data.room.VedicDatabase
import com.example.data.room.report.KundliReportEntity
import com.example.data.room.report.ReportAuditLogEntity
import com.example.engines.report.ReportGenerationStatus
import com.example.engines.report.ReportGeneratorEngine
import com.example.pdf.PremiumKundliPdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class KundliReportRepository(private val context: Context) {

    private val db = VedicDatabase.getDatabase(context)
    private val reportDao = db.kundliReportDao()
    private val auditLogDao = db.reportAuditLogDao()
    private val generatorEngine = ReportGeneratorEngine()

    fun getAllReports(): Flow<List<KundliReportEntity>> {
        return reportDao.getAllReports().flowOn(Dispatchers.IO)
    }

    suspend fun getReportById(reportId: String): KundliReportEntity? {
        return withContext(Dispatchers.IO) {
            reportDao.getReportById(reportId)
        }
    }

    /**
     * Triggers complete generation of a Kundli Report and saves it to Room.
     */
    fun generateAndSaveReport(
        profile: BirthProfile,
        reportTier: String = "PREMIUM",
        selectedLanguage: String = "EN"
    ): Flow<ReportGenerationStatus> = flow {
        val startTime = System.currentTimeMillis()

        generatorEngine.generateFullReportFlow(profile, reportTier, selectedLanguage).collect { status ->
            when (status) {
                is ReportGenerationStatus.Completed -> {
                    val reportEntity = status.reportEntity
                    withContext(Dispatchers.IO) {
                        reportDao.insertOrUpdateReport(reportEntity)
                        auditLogDao.insertAuditLog(
                            ReportAuditLogEntity(
                                logId = UUID.randomUUID().toString(),
                                reportId = reportEntity.reportId,
                                action = "GENERATED",
                                aiModelUsed = "gemini-3.5-flash",
                                executionTimeMs = System.currentTimeMillis() - startTime,
                                tokenCount = 1200,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    emit(ReportGenerationStatus.Completed(reportEntity))
                }
                else -> emit(status)
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun toggleBookmark(reportId: String, isBookmarked: Boolean) {
        withContext(Dispatchers.IO) {
            reportDao.updateBookmarkStatus(reportId, isBookmarked)
        }
    }

    suspend fun exportPdfForReport(reportId: String): File? {
        return withContext(Dispatchers.IO) {
            val report = reportDao.getReportById(reportId) ?: return@withContext null
            val pdfFile = PremiumKundliPdfExporter.exportReportToPdf(context, report)
            reportDao.updatePdfPath(reportId, pdfFile.absolutePath)
            pdfFile
        }
    }

    suspend fun deleteReport(reportId: String) {
        withContext(Dispatchers.IO) {
            reportDao.deleteReportById(reportId)
        }
    }
}
