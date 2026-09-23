package com.example.data.room.report

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kundli_reports")
data class KundliReportEntity(
    @PrimaryKey val reportId: String,
    val profileName: String,
    val birthDate: String, // YYYY-MM-DD
    val birthTime: String, // HH:MM
    val birthCity: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val ayanamsha: String,
    val calculationEngineVersion: String = "1.0.0",
    val reportTier: String = "PREMIUM", // BASIC, DETAILED, PREMIUM
    val selectedLanguage: String = "EN", // EN, HI, GU
    val status: String = "READY", // GENERATING, READY, FAILED
    val progressPercentage: Int = 100,
    val calculationSnapshotJson: String,
    val reportSectionsJson: String,
    val pdfPath: String? = null,
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "report_remedies")
data class ReportRemedyEntity(
    @PrimaryKey val remedyId: String,
    val category: String, // MANTRA, GEMSTONE, RUDRAKSHA, CHARITY, FASTING, PUJA, LIFESTYLE
    val targetGraha: String,
    val nameEn: String,
    val nameHi: String,
    val nameGu: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val descriptionGu: String,
    val mantraSanskrit: String? = null,
    val mantraTransliteration: String? = null,
    val gemstoneName: String? = null,
    val gemstoneSuitability: String? = null,
    val gemstoneCautions: String? = null,
    val rudrakshaMukhi: Int? = null,
    val classicalSource: String = "Brihat Parashara Hora Shastra"
)

@Entity(tableName = "report_configs")
data class ReportConfigEntity(
    @PrimaryKey val configKey: String,
    val configValue: String,
    val description: String = ""
)

@Entity(tableName = "report_audit_logs")
data class ReportAuditLogEntity(
    @PrimaryKey val logId: String,
    val reportId: String,
    val action: String, // GENERATED, REGENERATED_SECTION, PDF_EXPORTED, SHARED
    val aiModelUsed: String,
    val executionTimeMs: Long,
    val tokenCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
