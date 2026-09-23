package com.example.core.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import com.example.engines.festival.Festival
import com.example.engines.festival.Vrat
import com.example.engines.festival.PujaTimingRecommendation
import com.example.engines.reports.AscendantReport
import com.example.engines.reports.DashaPeriod

/**
 * Account Role classification in the system.
 */
enum class UserRole(val displayName: String, val description: String) {
    REGULAR("Regular Seeker", "Manage personal birth profile and explore individual horoscopes, numerology, and Vastu"),
    ASTROLOGER("Professional Astrologer", "Manage client directory, store client birth details, and provide consultations")
}

/**
 * Authenticated User Account details.
 */
data class UserAccount(
    val email: String,
    val fullName: String,
    val role: UserRole = UserRole.REGULAR,
    val astrologerTitleOrLicense: String = "",
    val isLoggedIn: Boolean = true
)

/**
 * Supported Ayanamsha systems for tropical to sidereal zodiac conversion.
 */
enum class AyanamshaSystem(val displayName: String, val offsetDegreesAtJ2000: Double) {
    LAHIRI("Chitrapaksha / Lahiri", 23.853056),
    KP("Krishnamurti Paddhati (KP)", 23.753056),
    RAMAN("B.V. Raman", 22.404444)
}

/**
 * Lunar month ending traditions.
 */
enum class CalendarTradition(val displayName: String) {
    AMANTA("Amanta (New Moon ending - Southern & Western India)"),
    PURNIMANTA("Purnimanta (Full Moon ending - Northern India)")
}

/**
 * Supported Numerology calculation methods.
 */
enum class NumerologySystem(val displayName: String) {
    CHALDEAN("Chaldean (Vedic Aligned)"),
    PYTHAGOREAN("Pythagorean (Western Modern)")
}

/**
 * UI display language and script preference for traditional Vedic terminology.
 */
enum class LanguageDisplay(val displayName: String) {
    ENGLISH("English (Transliterated)"),
    HINDI("Hindi (हिन्दी)"),
    GUJARATI("Gujarati (ગુજરાતી)"),
    SANSKRIT("Sanskrit (संस्कृतम्)"),
    MARATHI("Marathi (मराठी)"),
    TAMIL("Tamil (தமிழ்)"),
    TELUGU("Telugu (తెలుగు)"),
    KANNADA("Kannada (ಕನ್ನಡ)"),
    MALAYALAM("Malayalam (മലയാളം)"),
    BENGALI("Bengali (বাংলা)"),
    PUNJABI("Punjabi (ਪੰਜਾਬੀ)"),
    ODIA("Odia (ଓଡ଼ିଆ)"),
    ASSAMESE("Assamese (অসমীয়া)")
}

/**
 * Watch Face Visual Styles as outlined in specification.
 */
enum class WatchFaceStyle(val displayName: String, val description: String) {
    COSMIC("Cosmic", "Planetary orbits and celestial ring markers"),
    YANTRA("Yantra", "Sacred geometry and interlocking harmonic triangles"),
    CHAKRA("Chakra", "Seven-chakra energy vortex rings and lotus petals"),
    VASTU("Vastu", "8-directional elemental quadrant compass design"),
    PANCHANGA("Panchanga", "Traditional calendar dial with Devanagari numerals"),
    MINIMAL("Minimal", "Contemporary luxury with discreet Vedic accents"),
    ROYAL("Royal", "Opulent Mughal & Rajasthani heritage gold motifs"),
    MODERN("Modern", "Contemporary smartwatch dial layout with subtle aura and indices")
}

/**
 * App UI Theme color palettes.
 */
enum class AppThemePalette(val displayName: String, val description: String) {
    SOLAR_GOLD("Solar Gold", "Rich maroon and sunlit gold tones"),
    LUNAR_SILVER("Lunar Silver", "Cool slate and mystic moonlight tones"),
    TRADITIONAL_EARTH("Traditional Earth", "Warm terracotta and forest temple tones")
}

/**
 * User birth profile captured during onboarding.
 */
data class BirthProfile(
    val fullName: String,
    val birthDate: LocalDate,
    val birthTime: LocalTime,
    val birthLocation: LocationData,
    val ayanamsha: AyanamshaSystem = AyanamshaSystem.LAHIRI,
    val calendarTradition: CalendarTradition = CalendarTradition.AMANTA,
    val numerologySystem: NumerologySystem = NumerologySystem.CHALDEAN,
    val language: LanguageDisplay = LanguageDisplay.ENGLISH,
    val preferredStyle: WatchFaceStyle = WatchFaceStyle.COSMIC,
    val appTheme: AppThemePalette = AppThemePalette.SOLAR_GOLD,
    val hasSmartWatch: Boolean = false,
    val watchModel: String = "None"
)

/**
 * Geographic coordinates and timezone data with official LGD/Census metadata.
 */
data class LocationData(
    val country: String,
    val state: String,
    val district: String,
    val talukaOrTown: String,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val officialLocationCode: String? = null,
    val alternateNames: List<String> = emptyList()
) {
    val fullLocationString: String
        get() = "$talukaOrTown, $district, $state, $country"
}

/**
 * The nine Grahas (celestial influences) in Jyotisha.
 */
enum class Graha(val displayName: String, val sanskritName: String, val symbol: String) {
    SURYA("Sun", "Surya", "☉"),
    CHANDRA("Moon", "Chandra", "☽"),
    MANGALA("Mars", "Mangala", "♂"),
    BUDHA("Mercury", "Budha", "☿"),
    GURU("Jupiter", "Guru / Brihaspati", "♃"),
    SHUKRA("Venus", "Shukra", "♀"),
    SHANI("Saturn", "Shani", "♄"),
    RAHU("North Lunar Node", "Rahu", "☊"),
    KETU("South Lunar Node", "Ketu", "☋")
}

/**
 * 12 Rashis (Zodiac signs).
 */
enum class Rashi(
    val number: Int,
    val westernName: String,
    val sanskritName: String,
    val symbol: String,
    val rulingGraha: Graha
) {
    MESHA(1, "Aries", "Mesha", "♈", Graha.MANGALA),
    VRISHABHA(2, "Taurus", "Vrishabha", "♉", Graha.SHUKRA),
    MITHUNA(3, "Gemini", "Mithuna", "♊", Graha.BUDHA),
    KARKATA(4, "Cancer", "Karkata", "♋", Graha.CHANDRA),
    SIMHA(5, "Leo", "Simha", "♌", Graha.SURYA),
    KANYA(6, "Virgo", "Kanya", "♍", Graha.BUDHA),
    TULA(7, "Libra", "Tula", "♎", Graha.SHUKRA),
    VRISHCHIKA(8, "Scorpio", "Vrishchika", "♏", Graha.MANGALA),
    DHANUS(9, "Sagittarius", "Dhanus", "♐", Graha.GURU),
    MAKARA(10, "Capricorn", "Makara", "♑", Graha.SHANI),
    KUMBHA(11, "Aquarius", "Kumbha", "♒", Graha.SHANI),
    MEENA(12, "Pisces", "Meena", "♓", Graha.GURU)
}

/**
 * 27 Nakshatras (Lunar Mansions).
 */
enum class Nakshatra(
    val number: Int,
    val sanskritName: String,
    val devanagari: String,
    val rulingGraha: Graha
) {
    ASHWINI(1, "Ashwini", "अश्विनी", Graha.KETU),
    BHARANI(2, "Bharani", "भरणी", Graha.SHUKRA),
    KRITTIKA(3, "Krittika", "कृत्तिका", Graha.SURYA),
    ROHINI(4, "Rohini", "रोहिणी", Graha.CHANDRA),
    MRIGASHIRA(5, "Mrigashira", "मृगशिरा", Graha.MANGALA),
    ARDRA(6, "Ardra", "आर्द्रा", Graha.RAHU),
    PUNARVASU(7, "Punarvasu", "पुनर्वसु", Graha.GURU),
    PUSHYA(8, "Pushya", "पुष्य", Graha.SHANI),
    ASHLESHA(9, "Ashlesha", "आश्लेषा", Graha.BUDHA),
    MAGHA(10, "Magha", "मघा", Graha.KETU),
    PURVA_PHALGUNI(11, "Purva Phalguni", "पूर्व फाल्गुनी", Graha.SHUKRA),
    UTTARA_PHALGUNI(12, "Uttara Phalguni", "उत्तर फाल्गुनी", Graha.SURYA),
    HASTA(13, "Hasta", "हस्त", Graha.CHANDRA),
    CHITRA(14, "Chitra", "चित्रा", Graha.MANGALA),
    SWATI(15, "Swati", "स्वाती", Graha.RAHU),
    VISHAKHA(16, "Vishakha", "विशाखा", Graha.GURU),
    ANURADHA(17, "Anuradha", "अनुराधा", Graha.SHANI),
    JYESHTHA(18, "Jyeshtha", "ज्येष्ठा", Graha.BUDHA),
    MULA(19, "Mula", "मूल", Graha.KETU),
    PURVA_ASHADHA(20, "Purva Ashadha", "पूर्वाषाढ़ा", Graha.SHUKRA),
    UTTARA_ASHADHA(21, "Uttara Ashadha", "उत्तराषाढ़ा", Graha.SURYA),
    SHRAVANA(22, "Shravana", "श्रवण", Graha.CHANDRA),
    DHANISHTHA(23, "Dhanishtha", "धनिष्ठा", Graha.MANGALA),
    SHATABHISHA(24, "Shatabhisha", "शतभिषा", Graha.RAHU),
    PURVA_BHADRAPADA(25, "Purva Bhadrapada", "पूर्व भाद्रपद", Graha.GURU),
    UTTARA_BHADRAPADA(26, "Uttara Bhadrapada", "उत्तर भाद्रपद", Graha.SHANI),
    REVATI(27, "Revati", "रेवती", Graha.BUDHA)
}

/**
 * 30 Tithis (Lunar phases).
 */
enum class Paksha(val displayName: String, val devanagari: String) {
    SHUKLA("Shukla Paksha (Waxing)", "शुक्ल पक्ष"),
    KRISHNA("Krishna Paksha (Waning)", "कृष्ण पक्ष")
}

data class TithiInfo(
    val number: Int, // 1 to 30
    val name: String,
    val devanagari: String,
    val paksha: Paksha
)

/**
 * 7 Choghadiya types.
 */
enum class ChoghadiyaType(
    val displayName: String,
    val devanagari: String,
    val rulingGraha: Graha,
    val isAuspicious: Boolean,
    val quality: String
) {
    SHUBH("Shubh", "शुभ", Graha.GURU, true, "Auspicious & Holy"),
    LABH("Labh", "लाभ", Graha.BUDHA, true, "Gainful & Prosperous"),
    AMRIT("Amrit", "अमृत", Graha.CHANDRA, true, "Nectar & Ultimate Good"),
    CHAL("Chal", "चल", Graha.SHUKRA, true, "Neutral / Favorable for Travel"),
    ROG("Rog", "रोग", Graha.MANGALA, false, "Disease & Inauspicious"),
    KAAL("Kaal", "काल", Graha.SHANI, false, "Destructive & Inauspicious"),
    UDVEG("Udveg", "उद्वेग", Graha.SURYA, false, "Anxiety & Loss")
}

data class ChoghadiyaSlot(
    val type: ChoghadiyaType,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val isNight: Boolean
)

data class HoraSlot(
    val rulingGraha: Graha,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val horaNumber: Int // 1 to 24
)


data class Mantra(
    val id: String,
    val sanskrit: String,
    val transliteration: String,
    val language: String,
    val meaning: String,
    val associatedDeity: String? = null,
    val associatedContext: String? = null,
    val audioAsset: String? = null,
    val sourceTradition: String? = null
)

data class MuhurtaDeity(
    val deityName: String,
    val sanskritName: String,
    val transliteration: String,
    val description: String,
    val associatedMuhurta: String,
    val iconography: String? = null,
    val associatedMantra: Mantra? = null
)

data class TimingWindow(
    val name: String,
    val devanagari: String,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val isAuspicious: Boolean,
    val deity: MuhurtaDeity? = null
)

data class PanchangaData(
    val vara: String,
    val varaDevanagari: String,
    val tithi: TithiInfo,
    val nakshatra: Nakshatra,
    val nakshatraPada: Int,
    val yoga: String,
    val karana: String,
    val masa: String,
    val ritu: String,
    val ayana: String,
    val vikramSamvat: Int,
    val shakaSamvat: Int,
    val sunrise: ZonedDateTime,
    val sunset: ZonedDateTime,
    val solarNoon: ZonedDateTime
)

data class NumerologyProfile(
    val birthNumber: Int, // Mulank (1-9)
    val lifePathNumber: Int, // Bhagyank (1-9)
    val destinyNameNumber: Int, // Namank (1-9)
    val favorableNumbers: List<Int>,
    val favorableColors: List<String>,
    val favorableDays: List<String>,
    val rulingPlanet: Graha
)

data class VastuDirectionInfo(
    val directionName: String,
    val sanskritName: String,
    val presidingDeity: String,
    val mahabhutaElement: String,
    val elementalColor: String,
    val degrees: Double
)


data class CosmicScore(
    val luck: Int,
    val energy: Int,
    val calm: Int,
    val overall: Int
)

data class DailyVedicProfile(
    val currentZonedDateTime: ZonedDateTime,
    val panchanga: PanchangaData,
    val currentChoghadiya: ChoghadiyaSlot,
    val currentHora: HoraSlot,
    val currentVedicPeriod: String,
    val rahuKalam: TimingWindow,
    val yamaganda: TimingWindow,
    val gulikaKalam: TimingWindow,
    val abhijitMuhurta: TimingWindow?,
    val brahmaMuhurta: TimingWindow,
    val moonPhaseProgress: Float, // 0.0 (New Moon) to 1.0 (Full Moon) back to 0.0
    val cosmicScore: CosmicScore? = null,
    val festivals: List<Festival> = emptyList(),
    val vrats: List<Vrat> = emptyList(),
    val pujaRecommendations: List<PujaTimingRecommendation> = emptyList()
)

data class PersonalizedProfile(
    val userBirthProfile: BirthProfile,
    val natalLagnaRashi: Rashi,
    val natalSuryaRashi: Rashi,
    val natalChandraRashi: Rashi,
    val natalNakshatra: Nakshatra,
    val natalNakshatraPada: Int,
    val numerology: NumerologyProfile,
    val dominantVastuElement: String,
    val favorableColorsHex: List<Long>,
    val ascendantReport: AscendantReport? = null,
    val dashaPeriods: List<DashaPeriod> = emptyList()
)

data class WatchFaceConfiguration(
    val style: WatchFaceStyle,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val surfaceColorHex: Long,
    val showDevanagari: Boolean,
    val showChoghadiyaSubdial: Boolean,
    val showHoraBadge: Boolean,
    val showRahuRing: Boolean,
    val showMoonPhaseRing: Boolean,
    val showSacredGeometry: Boolean,
    val personalizedBirthNumber: Int,
    val zodiacSymbol: String,
    val nakshatraName: String,
    val showCelestialClock: Boolean = true,
    val celestialRotationSpeed: Float = 1.0f,
    val customBackgroundUri: String = ""
)

/**
 * 9 Tara Bala types calculated from Janma Nakshatra to transit Moon Nakshatra.
 */
enum class TaraType(
    val number: Int,
    val sanskritName: String,
    val displayName: String,
    val isAuspicious: Boolean,
    val description: String
) {
    JANMA(1, "Janma (जन्म)", "Birth / Physical", false, "Body & self vitality; avoid surgery or critical financial risks"),
    SAMPAT(2, "Sampat (सम्पत्)", "Wealth & Prosperity", true, "Highly auspicious for investments, new deals, commerce & prosperity"),
    VIPAT(3, "Vipat (विपत्)", "Obstacles & Losses", false, "Challenging energy; avoid litigation, disputes or initiating projects"),
    KSHEMA(4, "Kshema (क्षेम)", "Well-being & Safety", true, "Favorable for security, peace, healing, travel and celebrations"),
    PRATYAK(5, "Pratyak (प्रत्यक्)", "Setbacks & Resistance", false, "Adverse results; maintain patience and delay critical negotiations"),
    SADHAKA(6, "Sadhaka (साधक)", "Achievement & Success", true, "Excellent for completing goals, learning, exams, rituals and sadhana"),
    VADHA(7, "Vadha / Naidhana (वध)", "Destruction & Conflict", false, "Most unfavorable; strictly avoid major beginnings, ceremonies and contracts"),
    MITRA(8, "Mitra (मित्र)", "Friendly & Harmonious", true, "Beneficial for meetings, networking, friendly collaborations and agreements"),
    PARAMA_MITRA(9, "Parama Mitra (परम मित्र)", "Supreme Benefactor", true, "Supreme grace and blessing; great success in all major life endeavors")
}

/**
 * Daily personal cosmic harmony computed from Tarabala and Chandrabala.
 */
data class DailyHarmonyScore(
    val taraType: TaraType,
    val chandrabalaHouse: Int, // 1 to 12 from Janma Rashi
    val isChandrabalaFavorable: Boolean,
    val overallScorePercent: Int, // 0 to 100
    val advice: String
)

/**
 * Vedic Bhava (House) data for the North Indian Diamond Kundali chart.
 */
data class BhavaDetail(
    val houseNumber: Int, // 1 to 12
    val traditionalName: String,
    val devanagariName: String,
    val rashi: Rashi,
    val occupyingGrahas: List<Graha>,
    val significations: String,
    val karakaGraha: Graha
)

/**
 * Complete Natal or Transit Kundali horoscope chart.
 */
data class KundaliChartData(
    val lagnaRashi: Rashi,
    val lagnaDegrees: Double,
    val bhavas: List<BhavaDetail>,
    val grahaPositions: Map<Graha, Double>
)

/**
 * Galaxy Watch Complication Slot types.
 */
enum class ComplicationType(val displayName: String, val subtitle: String) {
    SUNRISE_SUNSET("Sunrise & Sunset", "Solar dawn and dusk times according to sidereal calculation"),
    CHOGHADIYA("Active Choghadiya", "Real-time Shubh/Labh/Amrit status"),
    RAHU_COUNTDOWN("Rahu Kalam Countdown", "Time until next inauspicious window"),
    BRAHMA_MUHURTA("Brahma Muhurta", "Sacred pre-dawn meditation window & countdown"),
    ABHIJIT_MUHURTA("Abhijit Muhurta", "Midday victorious auspicious window around solar noon"),
    GHATI_PALA("Vedic Ghati-Pala", "Ancient 60 Ghatis & 60 Palas clock since sunrise"),
    TARA_BALA("Daily Tara Bala", "Personal transit lunar harmony"),
    HORA_GRAHA("Planetary Hora", "Active planetary hour lord & symbol"),
    DASHA_PERIOD("Vimshottari Dasha", "Active Mahadasha & Antardasha ruling planets"),
    COSMIC_SCORE("Cosmic Vitality Score", "Daily transit harmony & vitality gauge (0-100)"),
    TITHI_PHASE("Tithi & Lunar Phase", "Current lunar day & waxing/waning progress"),
    MOON_RASHI("Moon Rashi & Sign", "Current lunar transit sign & sidereal degrees"),
    YOGA_KARANA("Yoga & Karana", "Active 4th & 5th limbs of Panchanga"),
    JANMA_STAR("Janma Nakshatra", "Natal moon mansion & ruling planet"),
    VASTU_ELEMENT("Vastu Element", "Dominant Mahabhuta & compass resonance"),
    VASTU_COMPASS("Vastu Compass", "Live directional resonance & ruling deity"),
    NUMEROLOGY_PERSONAL_DAY("Personal Day", "Daily numerological ruling number"),
    NUMEROLOGY_LUCKY_ALIGNMENT("Lucky Align", "Current hour alignment with Life Path"),
    MANTRA_JAPA("Japa Mala Counter", "Digital tap-to-chant 108 mantra repetition counter"),
    FESTIVAL_COUNTDOWN("Festival & Vrat", "Next upcoming Ekadashi, fast, or sacred festival"),
    MINDFULNESS_RING("Mindfulness & Sadhana", "Daily prayer, meditation & hydration alignment ring"),
    BATTERY_STEPS("Battery & Steps", "Smartwatch battery level & daily step telemetry")
}

data class ComplicationCustomization(
    val topSlot: ComplicationType = ComplicationType.CHOGHADIYA,
    val bottomSlot: ComplicationType = ComplicationType.RAHU_COUNTDOWN,
    val leftSlot: ComplicationType = ComplicationType.TARA_BALA,
    val rightSlot: ComplicationType = ComplicationType.HORA_GRAHA
)
