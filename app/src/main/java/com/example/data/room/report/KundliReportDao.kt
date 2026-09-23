package com.example.data.room.report

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KundliReportDao {

    @Query("SELECT * FROM kundli_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<KundliReportEntity>>

    @Query("SELECT * FROM kundli_reports WHERE reportId = :reportId LIMIT 1")
    suspend fun getReportById(reportId: String): KundliReportEntity?

    @Query("SELECT * FROM kundli_reports WHERE profileName = :profileName AND birthDate = :birthDate AND birthTime = :birthTime LIMIT 1")
    suspend fun findExistingReport(profileName: String, birthDate: String, birthTime: String): KundliReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReport(report: KundliReportEntity)

    @Query("UPDATE kundli_reports SET isBookmarked = :bookmarked WHERE reportId = :reportId")
    suspend fun updateBookmarkStatus(reportId: String, bookmarked: Boolean)

    @Query("UPDATE kundli_reports SET pdfPath = :pdfPath WHERE reportId = :reportId")
    suspend fun updatePdfPath(reportId: String, pdfPath: String)

    @Query("DELETE FROM kundli_reports WHERE reportId = :reportId")
    suspend fun deleteReportById(reportId: String)
}

@Dao
interface ReportRemedyDao {

    @Query("SELECT * FROM report_remedies")
    fun getAllRemedies(): Flow<List<ReportRemedyEntity>>

    @Query("SELECT * FROM report_remedies WHERE targetGraha = :graha")
    suspend fun getRemediesForGraha(graha: String): List<ReportRemedyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemedies(remedies: List<ReportRemedyEntity>)
}

@Dao
interface ReportConfigDao {

    @Query("SELECT * FROM report_configs")
    fun getAllConfigs(): Flow<List<ReportConfigEntity>>

    @Query("SELECT configValue FROM report_configs WHERE configKey = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: ReportConfigEntity)
}

@Dao
interface ReportAuditLogDao {

    @Query("SELECT * FROM report_audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogs(): Flow<List<ReportAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: ReportAuditLogEntity)
}
