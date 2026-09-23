package com.example.data

data class TerminologyItem(
    val id: String,
    val english: String,
    val hindi: String,
    val gujarati: String,
    val sanskrit: String,
    val shortLabel: String,
    val description: String
)

object VedicTerminologyRepository {
    private val terms = mapOf(
        // Tithis
        "tithi_pratipada" to TerminologyItem("tithi_pratipada", "Pratipada", "प्रतिपदा", "પ્રતિપદા", "प्रतिपदा", "Prat", "First lunar day of the lunar fortnight"),
        "tithi_dwitiya" to TerminologyItem("tithi_dwitiya", "Dwitiya", "द्वितीया", "દ્વિતીયા", "द्वितीया", "Dwi", "Second lunar day"),
        "tithi_tritiya" to TerminologyItem("tithi_tritiya", "Tritiya", "तृतीया", "તૃતીયા", "तृतीया", "Tri", "Third lunar day"),
        "tithi_chaturthi" to TerminologyItem("tithi_chaturthi", "Chaturthi", "चतुर्थी", "ચતુર્થી", "चतुर्थी", "Chat", "Fourth lunar day"),
        "tithi_panchami" to TerminologyItem("tithi_panchami", "Panchami", "पंचमी", "પંચમી", "पंचमी", "Pan", "Fifth lunar day"),
        "tithi_shashthi" to TerminologyItem("tithi_shashthi", "Shashthi", "षष्ठी", "ષષ્ઠી", "षष्ठी", "Sha", "Sixth lunar day"),
        "tithi_saptami" to TerminologyItem("tithi_saptami", "Saptami", "सप्तमी", "સપ્તમી", "सप्तमी", "Sap", "Seventh lunar day"),
        "tithi_ashtami" to TerminologyItem("tithi_ashtami", "Ashtami", "अष्टमी", "અષ્ટમી", "अष्टमी", "Ash", "Eighth lunar day"),
        "tithi_navami" to TerminologyItem("tithi_navami", "Navami", "नवमी", "નવમી", "नवमी", "Nav", "Ninth lunar day"),
        "tithi_dashami" to TerminologyItem("tithi_dashami", "Dashami", "दशमी", "દશમી", "दशमी", "Das", "Tenth lunar day"),
        "tithi_ekadashi" to TerminologyItem("tithi_ekadashi", "Ekadashi", "एकादशी", "એકાદશી", "एकादशी", "Eka", "Eleventh lunar day - sacred fasting day"),
        "tithi_dwadashi" to TerminologyItem("tithi_dwadashi", "Dwadashi", "द्वादशी", "દ્વાદશી", "द्वादशी", "Dwa", "Twelfth lunar day"),
        "tithi_trayodashi" to TerminologyItem("tithi_trayodashi", "Trayodashi", "त्रयोदशी", "ત્રયોદશી", "त्रयोदशी", "Tray", "Thirteenth lunar day"),
        "tithi_chaturdashi" to TerminologyItem("tithi_chaturdashi", "Chaturdashi", "चतुर्दशी", "ચતુર્દશી", "चतुर्दशी", "Chtd", "Fourteenth lunar day"),
        "tithi_purnima" to TerminologyItem("tithi_purnima", "Purnima", "पूर्णिमा", "પૂર્ણિમા", "पूर्णिमा", "Pur", "Full moon day"),
        "tithi_amavasya" to TerminologyItem("tithi_amavasya", "Amavasya", "अमावस्या", "અમાવસ્યા", "अमावस्या", "Ama", "New moon day"),

        // Paksha
        "paksha_shukla" to TerminologyItem("paksha_shukla", "Shukla Paksha", "शुक्ल पक्ष", "શુક્લ પક્ષ", "शुक्लपक्ष", "Shukla", "Waxing moon fortnight"),
        "paksha_krishna" to TerminologyItem("paksha_krishna", "Krishna Paksha", "कृष्ण पक्ष", "કૃષ્ણ પક્ષ", "कृष्णपक्ष", "Krishna", "Waning moon fortnight"),

        // Grahas (Planets)
        "graha_surya" to TerminologyItem("graha_surya", "Surya (Sun)", "सूर्य", "સૂર્ય", "सूर्य", "Sun", "The Soul, vitality, and authority"),
        "graha_chandra" to TerminologyItem("graha_chandra", "Chandra (Moon)", "चन्द्र", "ચંદ્ર", "चन्द्र", "Moon", "The Mind, emotions, and intuition"),
        "graha_mangal" to TerminologyItem("graha_mangal", "Mangal (Mars)", "मंगल", "મંગળ", "मङ्गळ", "Mars", "Energy, courage, and action"),
        "graha_budha" to TerminologyItem("graha_budha", "Budha (Mercury)", "बुध", "બુધ", "बुध", "Merc", "Intellect, communication, and commerce"),
        "graha_guru" to TerminologyItem("graha_guru", "Guru (Jupiter)", "गुरु", "ગુરુ", "गुरु", "Jup", "Wisdom, expansion, and grace"),
        "graha_shukra" to TerminologyItem("graha_shukra", "Shukra (Venus)", "शुक्र", "શુક્ર", "शुक्र", "Ven", "Love, harmony, and luxury"),
        "graha_shani" to TerminologyItem("graha_shani", "Shani (Saturn)", "शनि", "શનિ", "शनि", "Sat", "Discipline, karma, and perseverance"),
        "graha_rahu" to TerminologyItem("graha_rahu", "Rahu", "राहु", "રાહુ", "राहु", "Rahu", "Ambition, illusion, and innovation"),
        "graha_ketu" to TerminologyItem("graha_ketu", "Ketu", "केतु", "કેતુ", "केतु", "Ketu", "Spiritual liberation and intuition"),

        // Rashis (Zodiac Signs)
        "rashi_mesha" to TerminologyItem("rashi_mesha", "Mesha (Aries)", "मेष", "મેષ", "मेष", "Mesha", "Aries zodiac sign"),
        "rashi_vrishabha" to TerminologyItem("rashi_vrishabha", "Vrishabha (Taurus)", "वृषभ", "વૃષભ", "वृषभ", "Vrish", "Taurus zodiac sign"),
        "rashi_mithuna" to TerminologyItem("rashi_mithuna", "Mithuna (Gemini)", "मिथुन", "મિથુન", "मिथुन", "Mith", "Gemini zodiac sign"),
        "rashi_karka" to TerminologyItem("rashi_karka", "Karka (Cancer)", "कर्क", "કર્ક", "कर्क", "Karka", "Cancer zodiac sign"),
        "rashi_simha" to TerminologyItem("rashi_simha", "Simha (Leo)", "सिंह", "સિંહ", "सिंह", "Simha", "Leo zodiac sign"),
        "rashi_kanya" to TerminologyItem("rashi_kanya", "Kanya (Virgo)", "कन्या", "કન્યા", "कन्या", "Kanya", "Virgo zodiac sign"),
        "rashi_tula" to TerminologyItem("rashi_tula", "Tula (Libra)", "तुला", "તુલા", "तुला", "Tula", "Libra zodiac sign"),
        "rashi_vrischika" to TerminologyItem("rashi_vrischika", "Vrischika (Scorpio)", "वृश्चिक", "વૃશ્ચિક", "वृश्चिक", "Vrishc", "Scorpio zodiac sign"),
        "rashi_dhanu" to TerminologyItem("rashi_dhanu", "Dhanu (Sagittarius)", "धनु", "ધનુ", "धनु", "Dhanu", "Sagittarius zodiac sign"),
        "rashi_makara" to TerminologyItem("rashi_makara", "Makara (Capricorn)", "मकर", "મકર", "मकर", "Mak", "Capricorn zodiac sign"),
        "rashi_kumbha" to TerminologyItem("rashi_kumbha", "Kumbha (Aquarius)", "कुंभ", "કુંભ", "कुम्भ", "Kumbh", "Aquarius zodiac sign"),
        "rashi_meena" to TerminologyItem("rashi_meena", "Meena (Pisces)", "मीन", "મીન", "मीन", "Meena", "Pisces zodiac sign"),

        // Choghadiya
        "choghadiya_amrit" to TerminologyItem("choghadiya_amrit", "Amrit (Auspicious)", "अमृत", "અમૃત", "अमृत", "Amrit", "Most auspicious period for all undertakings"),
        "choghadiya_shubh" to TerminologyItem("choghadiya_shubh", "Shubh (Auspicious)", "शुभ", "શુભ", "शुभ", "Shubh", "Favorable period for ceremonies and travel"),
        "choghadiya_labh" to TerminologyItem("choghadiya_labh", "Labh (Gain)", "लाभ", "લાભ", "लाभ", "Labh", "Profitable period for business and education"),
        "choghadiya_char" to TerminologyItem("choghadiya_char", "Char (Neutral/Mobile)", "चल", "ચલ", "चल", "Char", "Favorable for travel and movement"),
        "choghadiya_rog" to TerminologyItem("choghadiya_rog", "Rog (Inauspicious)", "रोग", "રોગ", "रोग", "Rog", "Avoid new beginnings"),
        "choghadiya_kal" to TerminologyItem("choghadiya_kal", "Kal (Loss)", "काल", "કાલ", "काल", "Kal", "Period of obstacles"),
        "choghadiya_udveg" to TerminologyItem("choghadiya_udveg", "Udveg (Anxiety)", "उद्वेग", "ઉદ્વેગ", "उद्वेग", "Udveg", "Period of worry; avoid important tasks"),

        // Special Muhurtas
        "muhurta_brahma" to TerminologyItem("muhurta_brahma", "Brahma Muhurta", "ब्रह्म मुहूर्त", "બ્રહ્મ મુહૂર્ત", "ब्रह्ममुहूर्त", "Brahma", "Sacred pre-dawn window for meditation and study"),
        "muhurta_abhijit" to TerminologyItem("muhurta_abhijit", "Abhijit Muhurta", "अभिजित मुहूर्त", "અભિજિત મુહૂર્ત", "अभिजितमुहूर्त", "Abhijit", "Auspicious midday window"),
        "rahu_kalam" to TerminologyItem("rahu_kalam", "Rahu Kalam", "राहुकाल", "રાહુ કાળ", "राहुकाल", "Rahu", "Inauspicious period ruled by Rahu"),
        "yamaganda" to TerminologyItem("yamaganda", "Yamaganda", "यमगण्ड", "યમગંડ", "यमगण्ड", "Yama", "Period ruled by Yama"),
        "gulika" to TerminologyItem("gulika", "Gulika Kalam", "गुलिक काल", "ગુલિક કાળ", "गुलिककाल", "Gulika", "Period ruled by Saturn's son Gulika")
    )

    fun getTerm(id: String, languageCode: String): String {
        val item = terms[id] ?: return id
        return when (languageCode) {
            "hi" -> item.hindi
            "gu" -> item.gujarati
            "sa" -> item.sanskrit
            else -> item.english
        }
    }

    fun getShortLabel(id: String, languageCode: String): String {
        val item = terms[id] ?: return id
        return item.shortLabel
    }
}
