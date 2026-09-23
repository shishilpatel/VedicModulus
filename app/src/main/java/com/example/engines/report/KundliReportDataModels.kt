package com.example.engines.report

import com.example.core.model.*

enum class ReportSectionKey(val sectionNumber: Int, val titleEn: String, val titleHi: String, val titleGu: String) {
    COVER_BIRTH_DETAILS(1, "Cover & Birth Details", "मुखपृष्ठ एवं जन्म विवरण", "કવર અને જન્મ વિગત"),
    EXECUTIVE_SUMMARY(2, "Executive Summary", "कार्यकारी सारांश", "કાર્યકારી સારાંશ"),
    BIRTH_CHART_OVERVIEW(3, "Birth Chart Overview", "जन्म कुण्डली अवलोकन", "જન્મ કુંડળી અવલોકન"),
    LAGNA_ANALYSIS(4, "Lagna / Ascendant Analysis", "लग्न भाव विश्लेषण", "લગ્ન ભાવ વિશ્લેષણ"),
    PERSONALITY_BEHAVIOR(5, "Personality & Behavioral Tendencies", "व्यक्तित्व एवं स्वभाव", "વ્યક્તિત્વ અને સ્વભાવ"),
    PLANETARY_STRENGTH(6, "Planetary Strength & Dignity", "ग्रह बल एवं दीप्ति (षड्बल)", "ગ્રહ બળ અને દીપ્તિ"),
    HOUSE_BY_HOUSE(7, "House-by-House Analysis (Bhavas 1-12)", "द्वादश भाव विश्लेषण (1-12)", "બાર ભાવ વિશ્લેષણ"),
    PLANET_BY_PLANET(8, "Planet-by-Planet Analysis (Navagrahas)", "नवग्रह विश्लेषण", "નવગ્રહ વિશ્લેષણ"),
    NAKSHATRA_ANALYSIS(9, "Nakshatra & Pada Analysis", "नक्षत्र एवं पद विश्लेषण", "નક્ષત્ર અને પદ વિશ્લેષણ"),
    IMPORTANT_YOGAS(10, "Important Yogas (Auspicious Combinations)", "शुभ योग विश्लेषण", "શુભ યોગ વિશ્લેષણ"),
    DOSHA_ANALYSIS(11, "Dosha & Affliction Analysis", "दोष एवं शांति विश्लेषण", "દોષ અને શાંતિ વિશ્લેષણ"),
    CAREER_PROFESSION(12, "Career & Profession (Dashamsha D10)", "आजीविका एवं करियर (D10)", "કારકિર્દી અને વ્યવસાય"),
    BUSINESS_ENTREPRENEURSHIP(13, "Business & Entrepreneurship", "व्यापार एवं उद्यमिता", "વેપાર અને ઉદ્યોગ"),
    FINANCE_WEALTH(14, "Finance & Wealth Tendencies (Hora D2)", "धन एवं संपत्ति (D2)", "નાણાં અને સંપત્તિ"),
    EDUCATION_LEARNING(15, "Education & Academic Success (D24)", "शिक्षा एवं ज्ञान (D24)", "શિક્ષણ અને જ્ઞાન"),
    MARRIAGE_RELATIONSHIPS(16, "Marriage & Relationships (Navamsha D9)", "विवाह एवं दाम्पत्य (D9)", "લગ્ન અને દંપતી"),
    FAMILY_CHILDREN(17, "Family & Children (Saptamsha D7)", "कुटुंब एवं संतान (D7)", "કુટુંબ અને સંતાન"),
    HEALTH_TENDENCIES(18, "Health & Vitality Tendencies (D30)", "स्वास्थ्य एवं तंदुरुस्ती (D30)", "આરોગ્ય અને સ્વાસ્થ્ય"),
    SPIRITUALITY_PURPOSE(19, "Spirituality & Life Purpose (D20)", "अध्यात्म एवं जीवन ध्येय (D20)", "અધ્યાત્મ અને જીવન હેતુ"),
    PROPERTY_VEHICLES(20, "Property, Vehicles & Fixed Assets (D4/D16)", "भवन, वाहन एवं संपत्ति (D4/D16)", "મકાન, વાહન અને મિલકત"),
    FOREIGN_TRAVEL(21, "Foreign Travel & Settlement Indicators", "विदेश यात्रा एवं बसने के योग", "વિદેશ પ્રવાસ અને સ્થાયી"),
    MAJOR_LIFE_THEMES(22, "Major Life Themes & Core Directives", "प्रमुख जीवन सूत्र", "મુખ્ય જીવન સૂત્રો"),
    VIMSHOTTARI_DASHA_TIMELINE(23, "Vimshottari Mahadasha Timeline", "विंशोत्तरी महादशा समयरेखा", "વિંશોત્તરી મહાદશા સમયરેખા"),
    ANTARDASHA_ANALYSIS(24, "Current Antardasha Deep Dive", "अन्तर्दशा सूक्ष्म विश्लेषण", "અંતર્દશા સૂક્ષ્મ વિશ્લેષણ"),
    CURRENT_PLANETARY_TRANSIT(25, "Current Planetary Transits (Gochara)", "वर्तमान गोचर विश्लेषण", "વર્તમાન ગોચર વિશ્લેષણ"),
    UPCOMING_TRANSITS(26, "Upcoming Important Transit Periods", "आगामी प्रमुख गोचर अवधि", "આગામી મુખ્યા ગોચર સમયગાળો"),
    FAVOURABLE_CHALLENGING_PERIODS(27, "Favourable & Challenging Time Windows", "शुभ एवं चुनौतीपूर्ण समय", "શુભ અને પડકારજનક સમય"),
    IMPORTANT_COMBINATIONS(28, "Special Planetary Combinations", "विशेष ग्रह योग युति", "વિશેષ ગ્રહ યોગ યુતિ"),
    PERSONALIZED_REMEDIES(29, "Personalized Traditional Remedies", "व्यक्तिगत पारंपरिक उपाय", "વ્યક્તિગત પરંપરાગત ઉપાય"),
    MANTRA_RECOMMENDATIONS(30, "Mantra Recommendations & Sanskrit Japa", "मंत्र साधना एवं जप", "મંત્ર સાધના અને જપ"),
    GEMSTONE_RECOMMENDATIONS(31, "Gemstone Suitability & Caution Protocol", "रत्न निर्धारण एवं सावधानी", "રત્ન નિર્ધારણ અને સાવચેતી"),
    RUDRAKSHA_RECOMMENDATIONS(32, "Rudraksha Recommendations", "रुद्राक्ष धारण परामर्श", "રુદ્રાક્ષ ધારણ સલાહ"),
    CHARITY_SERVICE_REMEDIES(33, "Charity & Service Practices (Dana)", "दान एवं सेवा विधान", "દાન અને સેવા વિધાન"),
    DAILY_SPIRITUAL_PRACTICES(34, "Daily Spiritual Routine (Dincharya)", "दैनिक आध्यात्मिक दिनचर्या", "દૈનિક આધ્યાત્મિક દિનચર્યા"),
    MONTHLY_YEARLY_GUIDANCE(35, "Monthly & Yearly Life Guidance", "मासिक एवं वार्षिक मार्गदर्शन", "માસિક અને વાર્ષિક માર્ગદર્શન"),
    IMPORTANT_DATES_PERIODS(36, "Important Key Dates & Windows", "महत्वपूर्ण तिथियां एवं काल", "महत्वपूर्ण तिथीओ अने काळ"),
    FINAL_SUMMARY(37, "Final Personalized Summary", "अंतिम व्यक्तिगत निष्कर्ष", "અંતિમ વ્યક્તિગત નિષ્કર્ષ")
}

data class EvidenceTag(
    val factor: String,
    val subject: String,
    val significance: String
)

data class ReportSectionContent(
    val sectionKey: ReportSectionKey,
    val title: String,
    val summaryText: String,
    val detailedInterpretation: String,
    val evidenceTags: List<EvidenceTag> = emptyList(),
    val highlights: List<String> = emptyList(),
    val warningsOrCautions: List<String> = emptyList(),
    val remedies: List<String> = emptyList(),
    val disclaimerText: String? = null
)

data class ChartSnapshotData(
    val vargaCode: String,
    val lagnaRashi: String,
    val lagnaDegrees: Double,
    val planetPositions: Map<String, String>,
    val housePlanets: Map<Int, List<String>>,
    val sensitivityNote: String? = null
)

data class FullReportCalculationSnapshot(
    val profileName: String,
    val birthDate: String,
    val birthTime: String,
    val birthCity: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val ayanamshaSystem: String,
    val calculationVersion: String = "1.0.0",
    val lagnaRashi: String,
    val lagnaDegrees: Double,
    val sunRashi: String,
    val moonRashi: String,
    val nakshatra: String,
    val nakshatraPada: Int,
    val planetaryPositionsDeg: Map<String, Double>,
    val planetaryRashis: Map<String, String>,
    val planetaryDignities: Map<String, String>,
    val houseLords: Map<Int, String>,
    val detectedYogas: List<String>,
    val detectedDoshas: List<String>,
    val currentMahadasha: String,
    val currentAntardasha: String,
    val dashaEndDate: String,
    val divisionalCharts: Map<String, ChartSnapshotData>
)
