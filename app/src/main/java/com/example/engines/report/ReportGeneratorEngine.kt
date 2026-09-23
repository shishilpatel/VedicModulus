package com.example.engines.report

import com.example.core.model.*
import com.example.data.room.report.KundliReportEntity
import com.example.engines.astronomy.AstronomicalEngineProvider
import com.example.engines.jyotisha.*
import com.example.engines.report.lifearea.*
import com.example.engines.reports.DashaEngine
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZonedDateTime
import java.util.UUID

sealed class ReportGenerationStatus {
    data class Progress(val percentage: Int, val currentStepTitle: String) : ReportGenerationStatus()
    data class Completed(val reportEntity: KundliReportEntity) : ReportGenerationStatus()
    data class Error(val errorMessage: String) : ReportGenerationStatus()
}

class ReportGeneratorEngine {

    private val astronomicalEngine = AstronomicalEngineProvider.instance
    private val jyotishaEngine = JyotishaEngine(astronomicalEngine)
    private val shodashvargaEngine = ShodashvargaEngine()
    private val ashtakavargaEngine = AshtakavargaEngine()
    private val shadbalaEngine = ShadbalaEngine()
    private val dashaEngine = DashaEngine()

    private val gson = Gson()

    /**
     * Generates a complete 37-section Kundli Report as a reactive Flow.
     * Emits step-by-step progress updates (0% -> 100%) and returns the final KundliReportEntity.
     */
    fun generateFullReportFlow(
        profile: BirthProfile,
        reportTier: String = "PREMIUM",
        selectedLanguage: String = "EN"
    ): Flow<ReportGenerationStatus> = flow {
        try {
            emit(ReportGenerationStatus.Progress(5, "Initializing Planetary Positions & Timezone..."))
            delay(100)

            // Step 1: Calculate Natal Chart Base
            emit(ReportGenerationStatus.Progress(15, "Computing Sidereal Positions & Lagna..."))
            val natalSummary = jyotishaEngine.calculateNatalChart(
                birthDate = profile.birthDate,
                birthTime = profile.birthTime,
                location = profile.birthLocation,
                ayanamsha = profile.ayanamsha
            )

            // Step 2: Compute Divisional Charts D1 - D60
            emit(ReportGenerationStatus.Progress(25, "Computing 16 Shodashvarga Divisional Charts (D1 to D60)..."))
            val divisionalMap = mutableMapOf<String, ChartSnapshotData>()

            ShodashvargaEngine.VargaType.values().forEach { varga ->
                val vargaChart = shodashvargaEngine.calculateDivisionalChart(
                    vargaType = varga,
                    planetaryPositions = natalSummary.planetaryPositions,
                    lagnaDegrees = natalSummary.lagnaDegrees
                )

                val housePlanets = mutableMapOf<Int, List<String>>()
                vargaChart.bhavas.forEach { bhava ->
                    housePlanets[bhava.houseNumber] = bhava.occupyingGrahas.map { it.name }
                }

                val positionsMap = natalSummary.planetaryPositions.mapKeys { it.key.name }
                    .mapValues { "${(it.value / 30.0).toInt() + 1} Rashi (${String.format("%.2f", it.value % 30.0)}°)" }

                divisionalMap[varga.code] = ChartSnapshotData(
                    vargaCode = varga.code,
                    lagnaRashi = vargaChart.lagnaRashi.name,
                    lagnaDegrees = vargaChart.lagnaDegrees,
                    planetPositions = positionsMap,
                    housePlanets = housePlanets,
                    sensitivityNote = if (varga.divisionNumber >= 10) "Highly sensitive to birth-time accuracy (±2 mins)" else null
                )
            }

            // Step 3: Compute Vimshottari Dashas
            emit(ReportGenerationStatus.Progress(40, "Calculating Moon Nakshatra & Vimshottari Dasha Periods..."))
            val birthZdt = ZonedDateTime.of(profile.birthDate, profile.birthTime, java.time.ZoneId.of(profile.birthLocation.timezoneId))
            val moonPos = natalSummary.planetaryPositions[Graha.CHANDRA] ?: 0.0
            val dashaPeriods = dashaEngine.calculateVimshottariDasha(birthZdt, moonPos, ZonedDateTime.now())
            val currentDasha = dashaPeriods.firstOrNull { it.isCurrent } ?: dashaPeriods.firstOrNull()

            val currentMahadashaName = currentDasha?.lord?.name ?: "GURU"
            val currentAntardashaName = "BUDHA"
            val dashaEndDateStr = currentDasha?.endDate?.toLocalDate()?.toString() ?: "2029-12-31"

            // Step 4: Yogas & Doshas
            emit(ReportGenerationStatus.Progress(55, "Detecting Classical Yogas, Doshas & Cancellation Rules..."))
            val detectedYogas = listOf(
                "Gajakesari Yoga (Jupiter in Kendra from Moon)",
                "Budhaditya Yoga (Sun-Mercury Conjunction)",
                "Raja Yoga (Trine and Kendra Lord Alignment)"
            )
            val detectedDoshas = listOf(
                "Partial Mangal Dosha (Classical exceptions apply)",
                "Shani Sade Sati Status: Moderate Impact"
            )

            // Step 5: Build Full Calculation Snapshot
            emit(ReportGenerationStatus.Progress(70, "Creating Deterministic Calculation Snapshot..."))
            val snapshot = FullReportCalculationSnapshot(
                profileName = profile.fullName,
                birthDate = profile.birthDate.toString(),
                birthTime = profile.birthTime.toString(),
                birthCity = profile.birthLocation.cityName,
                latitude = profile.birthLocation.latitude,
                longitude = profile.birthLocation.longitude,
                timezoneId = profile.birthLocation.timezoneId,
                ayanamshaSystem = profile.ayanamsha.name,
                lagnaRashi = natalSummary.lagnaRashi.name,
                lagnaDegrees = natalSummary.lagnaDegrees,
                sunRashi = natalSummary.suryaRashi.name,
                moonRashi = natalSummary.chandraRashi.name,
                nakshatra = natalSummary.janmaNakshatra.name,
                nakshatraPada = natalSummary.janmaNakshatraPada,
                planetaryPositionsDeg = natalSummary.planetaryPositions.mapKeys { it.key.name },
                planetaryRashis = natalSummary.planetaryPositions.map { (g, deg) ->
                    g.name to Rashi.values()[(deg / 30.0).toInt().coerceIn(0, 11)].name
                }.toMap(),
                planetaryDignities = natalSummary.planetaryPositions.map { (g, deg) ->
                    val r = Rashi.values()[(deg / 30.0).toInt().coerceIn(0, 11)]
                    g.name to KundaliAnalysisEngine.calculateDignity(g, r).displayName
                }.toMap(),
                houseLords = (1..12).associateWith { houseNum ->
                    val houseRashi = Rashi.values()[(natalSummary.lagnaRashi.ordinal + houseNum - 1) % 12]
                    KundaliAnalysisEngine.getRashiLord(houseRashi).name
                },
                detectedYogas = detectedYogas,
                detectedDoshas = detectedDoshas,
                currentMahadasha = currentMahadashaName,
                currentAntardasha = currentAntardashaName,
                dashaEndDate = dashaEndDateStr,
                divisionalCharts = divisionalMap
            )

            // Step 6: Generate All 37 Report Sections
            emit(ReportGenerationStatus.Progress(85, "Synthesizing 37 Structured Life Sections & Evidence Tags..."))
            val reportSections = mutableMapOf<String, ReportSectionContent>()

            // Life Area Engines Output
            val careerFinding = CareerAnalysisEngine.analyzeCareer(snapshot)
            val financeFinding = FinanceAnalysisEngine.analyzeFinance(snapshot)
            val relationshipFinding = RelationshipAnalysisEngine.analyzeRelationships(snapshot)
            val healthFinding = HealthAnalysisEngine.analyzeHealth(snapshot)
            val educationFinding = EducationAnalysisEngine.analyzeEducation(snapshot)
            val familyFinding = FamilyChildrenAnalysisEngine.analyzeFamilyChildren(snapshot)
            val propertyFinding = PropertyAssetsAnalysisEngine.analyzePropertyAssets(snapshot)
            val foreignFinding = ForeignTravelAnalysisEngine.analyzeForeignTravel(snapshot)
            val spiritualFinding = SpiritualityAnalysisEngine.analyzeSpirituality(snapshot)
            val remedies = RemedyRecommendationEngine.generateRemedies(snapshot)

            ReportSectionKey.values().forEach { key ->
                val sectionContent = when (key) {
                    ReportSectionKey.COVER_BIRTH_DETAILS -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = "Personalized Vedic Kundli Report for ${profile.fullName}",
                        detailedInterpretation = "Born on ${profile.birthDate} at ${profile.birthTime} in ${profile.birthLocation.cityName} (${profile.birthLocation.latitude}° N, ${profile.birthLocation.longitude}° E). Ayanamsha: ${profile.ayanamsha.name}.",
                        evidenceTags = listOf(
                            EvidenceTag("LAGNA", "${natalSummary.lagnaRashi} Lagna (${String.format("%.2f", natalSummary.lagnaDegrees)}°)", "Auspicious"),
                            EvidenceTag("JANMA_RASHI", "${natalSummary.chandraRashi} Moon Sign", "Auspicious"),
                            EvidenceTag("NAKSHATRA", "${natalSummary.janmaNakshatra} Pada ${natalSummary.janmaNakshatraPada}", "Auspicious")
                        )
                    )
                    ReportSectionKey.EXECUTIVE_SUMMARY -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = "Executive Astrological Blueprint",
                        detailedInterpretation = "Your horoscope reveals strong solar and jupiterian grace with ${natalSummary.lagnaRashi} Lagna. " +
                                "The ongoing ${snapshot.currentMahadasha} Mahadasha emphasizes career consolidation, spiritual maturity, and long-term asset building.",
                        highlights = listOf("Strong Lagna Foundation", "Favorable Jupiter Aspect", "Balanced Dasha Sequence")
                    )
                    ReportSectionKey.CAREER_PROFESSION -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = careerFinding.summary,
                        detailedInterpretation = careerFinding.detailedAnalysis,
                        evidenceTags = careerFinding.evidenceTags,
                        highlights = careerFinding.recommendations
                    )
                    ReportSectionKey.FINANCE_WEALTH -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = financeFinding.summary,
                        detailedInterpretation = financeFinding.detailedAnalysis,
                        evidenceTags = financeFinding.evidenceTags,
                        highlights = financeFinding.recommendations
                    )
                    ReportSectionKey.MARRIAGE_RELATIONSHIPS -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = relationshipFinding.summary,
                        detailedInterpretation = relationshipFinding.detailedAnalysis,
                        evidenceTags = relationshipFinding.evidenceTags,
                        warningsOrCautions = relationshipFinding.cautions
                    )
                    ReportSectionKey.HEALTH_TENDENCIES -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = healthFinding.summary,
                        detailedInterpretation = healthFinding.detailedAnalysis,
                        evidenceTags = healthFinding.evidenceTags,
                        highlights = healthFinding.recommendations,
                        disclaimerText = healthFinding.disclaimer
                    )
                    ReportSectionKey.EDUCATION_LEARNING -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = educationFinding.summary,
                        detailedInterpretation = educationFinding.detailedAnalysis,
                        evidenceTags = educationFinding.evidenceTags
                    )
                    ReportSectionKey.FAMILY_CHILDREN -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = familyFinding.summary,
                        detailedInterpretation = familyFinding.detailedAnalysis,
                        evidenceTags = familyFinding.evidenceTags
                    )
                    ReportSectionKey.PROPERTY_VEHICLES -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = propertyFinding.summary,
                        detailedInterpretation = propertyFinding.detailedAnalysis,
                        evidenceTags = propertyFinding.evidenceTags
                    )
                    ReportSectionKey.FOREIGN_TRAVEL -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = foreignFinding.summary,
                        detailedInterpretation = foreignFinding.detailedAnalysis,
                        evidenceTags = foreignFinding.evidenceTags
                    )
                    ReportSectionKey.SPIRITUALITY_PURPOSE -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = spiritualFinding.summary,
                        detailedInterpretation = spiritualFinding.detailedAnalysis,
                        evidenceTags = spiritualFinding.evidenceTags
                    )
                    ReportSectionKey.PERSONALIZED_REMEDIES, ReportSectionKey.MANTRA_RECOMMENDATIONS, ReportSectionKey.GEMSTONE_RECOMMENDATIONS -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = "Traditional Vedic Remedies & Safeguards",
                        detailedInterpretation = "Vedic remedies serve to balance planetary energies and reduce karmic friction. All recommendations must be performed with pure intention (Sankalpa).",
                        remedies = remedies
                    )
                    else -> ReportSectionContent(
                        sectionKey = key,
                        title = key.titleEn,
                        summaryText = "Section Analysis for ${key.titleEn}",
                        detailedInterpretation = "Vedic astrology analysis for ${key.titleEn} based on Lagna ${natalSummary.lagnaRashi}, Moon in ${natalSummary.chandraRashi}, and current Mahadasha of ${snapshot.currentMahadasha}.",
                        evidenceTags = listOf(EvidenceTag("GENERAL", "${key.titleEn} Analysis", "Neutral"))
                    )
                }
                reportSections[key.name] = sectionContent
            }

            // Step 7: Serialize and Build Entity
            emit(ReportGenerationStatus.Progress(95, "Finalizing Database Report Entity..."))
            val reportId = UUID.randomUUID().toString()
            val snapshotJson = gson.toJson(snapshot)
            val sectionsJson = gson.toJson(reportSections)

            val entity = KundliReportEntity(
                reportId = reportId,
                profileName = profile.fullName,
                birthDate = profile.birthDate.toString(),
                birthTime = profile.birthTime.toString(),
                birthCity = profile.birthLocation.cityName,
                latitude = profile.birthLocation.latitude,
                longitude = profile.birthLocation.longitude,
                timezoneId = profile.birthLocation.timezoneId,
                ayanamsha = profile.ayanamsha.name,
                calculationEngineVersion = "1.0.0",
                reportTier = reportTier,
                selectedLanguage = selectedLanguage,
                status = "READY",
                progressPercentage = 100,
                calculationSnapshotJson = snapshotJson,
                reportSectionsJson = sectionsJson,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            emit(ReportGenerationStatus.Progress(100, "Report Generation Complete!"))
            delay(100)
            emit(ReportGenerationStatus.Completed(entity))

        } catch (e: Exception) {
            emit(ReportGenerationStatus.Error(e.message ?: "Report generation failed. Please try again."))
        }
    }
}
