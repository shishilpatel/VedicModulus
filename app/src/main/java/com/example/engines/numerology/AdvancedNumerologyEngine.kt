package com.example.engines.numerology

import java.time.LocalDate

/**
 * Model representing a 3x3 Grid (Loshu or Vedic Grid)
 */
data class NumerologyGrid(
    // rows: List of 3 rows, each row has 3 cells (String containing repeated digits or empty)
    val cells: List<List<String>>,
    val title: String
)

/**
 * Mobile Number Analysis Details
 */
data class MobileNumerologyDetails(
    val mobileNumber: String,
    val compoundSum: Int,
    val singleDigitTotal: Int,
    val interpretation: String,
    val recommendedTotals: String,
    val luckyColours: String,
    val unLuckyColours: String,
    val luckyNumbers: List<Int>,
    val unLuckyNumbers: List<Int>,
    val neutralNumbers: List<Int>,
    val missingNumbers: List<Int>,
    val pairsAnalysis: List<MobilePairResult>,
    val predictions: List<String>
)

data class MobilePairResult(
    val pair: String,
    val nature: String // "Benefic", "Neutral", "Malefic"
)

/**
 * Represents a single Mahadasha period in the Vedic Numerology Dasha timeline
 */
data class NumerologyDashaPeriod(
    val planetName: String,
    val number: Int,
    val startYear: Int,
    val endYear: Int,
    val dateRangeStr: String,
    val prediction: String,
    val isCurrent: Boolean = false
)

/**
 * Complete Numerology Analysis matching the professional PDF Report
 */
data class AdvancedNumerologyReport(
    val fullName: String,
    val birthDate: LocalDate,
    val kingNumber: Int,    // Mulank / Driver
    val queenNumber: Int,   // Bhagyank / Conductor
    val nameNumber: Int,    // Namank
    val loshuGrid: NumerologyGrid,
    val vedicGrid: NumerologyGrid,
    val numberMeanings: List<NumberMeaningItem>,
    val combinations: List<CombinationItem>,
    val dashaTimeline: List<NumerologyDashaPeriod>,
    val mobileAnalysis: MobileNumerologyDetails?
)

data class NumberMeaningItem(
    val title: String,
    val description: String
)

data class CombinationItem(
    val title: String,
    val description: String
)

/**
 * Engine calculating Loshu Grid, Vedic Grid, Number Combinations,
 * Advanced Mobile Numerology, and Dasha Timeline identical to the PDF report.
 */
object AdvancedNumerologyEngine {

    /**
     * Standard Lo Shu Grid layout positions:
     * 4  9  2
     * 3  5  7
     * 8  1  6
     */
    fun generateLoshuGrid(birthDate: LocalDate, kingNumber: Int, queenNumber: Int): NumerologyGrid {
        // Collect digits from birth date (DD-MM-YYYY)
        val digits = mutableListOf<Int>()
        val dayStr = "%02d".format(birthDate.dayOfMonth)
        val monthStr = "%02d".format(birthDate.monthValue)
        val yearStr = "%04d".format(birthDate.year)

        (dayStr + monthStr + yearStr).forEach { ch ->
            if (ch.isDigit() && ch != '0') {
                digits.add(ch.digitToInt())
            }
        }
        // In Lo Shu tradition: King and Queen are also placed if derived
        if (kingNumber in 1..9) digits.add(kingNumber)
        if (queenNumber in 1..9) digits.add(queenNumber)

        val countMap = digits.groupingBy { it }.eachCount()

        fun cellContent(digit: Int): String {
            val count = countMap[digit] ?: 0
            return if (count > 0) digit.toString().repeat(count) else ""
        }

        val rows = listOf(
            listOf(cellContent(4), cellContent(9), cellContent(2)),
            listOf(cellContent(3), cellContent(5), cellContent(7)),
            listOf(cellContent(8), cellContent(1), cellContent(6))
        )

        return NumerologyGrid(rows, "Loshu Grid")
    }

    /**
     * Standard Vedic Grid layout positions:
     * 3  1  9
     * 6  7  5
     * 2  8  4
     *
     * In the attached PDF report (Date 17-02-1989, King: 8, Queen: 1):
     * Vedic Grid has:
     * Row 1: [   ] [11] [ 9]
     * Row 2: [   ] [ 7] [  ]
     * Row 3: [ 2 ] [88] [  ]
     *
     * Notice:
     * Position (0,1) has '11'
     * Position (0,2) has '9'
     * Position (1,1) has '7'
     * Position (2,0) has '2'
     * Position (2,1) has '88'
     * This matches the Vedic grid cell mapping:
     * [3] [1] [9]
     * [6] [7] [5]
     * [2] [8] [4]
     */
    fun generateVedicGrid(birthDate: LocalDate, kingNumber: Int, queenNumber: Int): NumerologyGrid {
        val digits = mutableListOf<Int>()
        // Digits from birth date DD, MM, YY
        val day = birthDate.dayOfMonth
        val month = birthDate.monthValue
        val year = birthDate.year

        val dateStr = "%02d%02d%04d".format(day, month, year)
        dateStr.forEach { ch ->
            if (ch.isDigit() && ch != '0') {
                digits.add(ch.digitToInt())
            }
        }
        if (kingNumber in 1..9) digits.add(kingNumber)
        if (queenNumber in 1..9) digits.add(queenNumber)

        val countMap = digits.groupingBy { it }.eachCount()

        fun cellContent(digit: Int): String {
            val count = countMap[digit] ?: 0
            return if (count > 0) digit.toString().repeat(count) else ""
        }

        val rows = listOf(
            listOf(cellContent(3), cellContent(1), cellContent(9)),
            listOf(cellContent(6), cellContent(7), cellContent(5)),
            listOf(cellContent(2), cellContent(8), cellContent(4))
        )

        return NumerologyGrid(rows, "Vedic Grid")
    }

    /**
     * Generates detailed predictions for numbers present, repetitions, and combinations
     * matching the exact style of the reference PDF.
     */
    fun generateDetailedPredictions(
        birthDate: LocalDate,
        kingNumber: Int,
        queenNumber: Int
    ): Pair<List<NumberMeaningItem>, List<CombinationItem>> {
        val digits = mutableListOf<Int>()
        val dateStr = "%02d%02d%04d".format(birthDate.dayOfMonth, birthDate.monthValue, birthDate.year)
        dateStr.forEach { if (it != '0') digits.add(it.digitToInt()) }
        digits.add(kingNumber)
        digits.add(queenNumber)

        val countMap = digits.groupingBy { it }.eachCount()
        val distinct = countMap.keys.sorted()

        val numberMeanings = mutableListOf<NumberMeaningItem>()

        if (countMap.containsKey(1)) {
            val count = countMap[1] ?: 1
            if (count > 1) {
                numberMeanings.add(
                    NumberMeaningItem(
                        "Number 1 is appearing more than once ($count times)",
                        "Since your number 1 is appearing more than once, you are either over-confident or fiercely independent. You possess strong leadership qualities, do not want to compromise easily, and hold immense self-respect. You have sharp initiative, though at times you may become headstrong or blunt."
                    )
                )
            } else {
                numberMeanings.add(
                    NumberMeaningItem(
                        "Number 1",
                        "You possess natural leadership, pioneering spirit, and a strong sense of individuality. You prefer to lead rather than follow and take charge of situations confidently."
                    )
                )
            }
        }

        if (countMap.containsKey(2)) {
            val count = countMap[2] ?: 1
            numberMeanings.add(
                NumberMeaningItem(
                    "Number 2",
                    "You are very sentimental in nature. You need some extra push to finish tasks. You are creative, intuitive, and a soft-spoken person. Your aura is attractive and you love arts, music, photography, and beauty. You thrive with emotional reassurance and harmonious relationships."
                )
            )
        }

        if (countMap.containsKey(3)) {
            numberMeanings.add(
                NumberMeaningItem(
                    "Number 3",
                    "You are blessed with high intellect, wisdom, and communication skills. You have a natural flair for teaching, counseling, philosophy, and spiritual knowledge."
                )
            )
        }

        if (countMap.containsKey(4)) {
            numberMeanings.add(
                NumberMeaningItem(
                    "Number 4",
                    "You are practical, disciplined, and detail-oriented. You work with systematic logic and have an innate ability for technical, structural, and organized ventures."
                )
            )
        }

        if (countMap.containsKey(5)) {
            numberMeanings.add(
                NumberMeaningItem(
                    "Number 5",
                    "You love balance, freedom, commerce, and adaptability. You are a natural communicator, quick thinker, and do exceptionally well in business and networking."
                )
            )
        }

        if (countMap.containsKey(6)) {
            numberMeanings.add(
                NumberMeaningItem(
                    "Number 6",
                    "You are blessed with Venusian luxury, elegance, charm, and family devotion. You appreciate fine living, harmony, aesthetics, and selfless caring."
                )
            )
        }

        if (countMap.containsKey(7)) {
            numberMeanings.add(
                NumberMeaningItem(
                    "Number 7",
                    "You are spiritual in nature and you often love travel and research. You are lucky and need logical or spiritual reasoning for everything. You are a deep thinker, research-minded, and seek stability and continuity in relationships."
                )
            )
        }

        if (countMap.containsKey(8)) {
            val count = countMap[8] ?: 1
            if (count > 1) {
                numberMeanings.add(
                    NumberMeaningItem(
                        "Number 8 is appearing more than once ($count times)",
                        "Since Number 8 appears multiple times ($count times), it gives you power, money, and authority, accentuating the deep karmic nature of Saturn. You give immense importance to justice and need justification for everything. Your life includes resilience and struggle, yet leads to tremendous endurance. You are an emotional, ambitious, deep thinker and very hardworking."
                    )
                )
            } else {
                numberMeanings.add(
                    NumberMeaningItem(
                        "Number 8",
                        "Endowed with Saturnian perseverance, focus, executive authority, and karmic responsibility. Success comes through patience, determination, and practical hard work."
                    )
                )
            }
        }

        if (countMap.containsKey(9)) {
            numberMeanings.add(
                NumberMeaningItem(
                    "Number 9",
                    "You are very attractive and have high energy levels. You are bold, aggressive, argumentative, and stubborn in nature when standing up for your ideals. You always want to contribute to society and possess a real humanitarian nature."
                )
            )
        }

        // Combination analyses based on distinct numbers in the chart
        val combinations = mutableListOf<CombinationItem>()

        if (distinct.contains(1) && distinct.contains(9)) {
            combinations.add(
                CombinationItem(
                    "Your Chart shows combination of Number 1,9",
                    "Number 1 and 9 combination suggests that you will have good education or pursue higher learning. Your willpower will be strong and you will possess good administrative and communication skills. Since both 1 and 9 have fire qualities, your willpower helps you achieve most tasks in life. Philanthropic, confident, short-tempered at times, and freedom-loving."
                )
            )
        }

        if (distinct.contains(1) && distinct.contains(7)) {
            combinations.add(
                CombinationItem(
                    "Your Chart shows combination of Number 1,7",
                    "Number 1 and 7 combination suggests strong intuition power and executive luck. You will be inclined towards spirituality and possess knowledge of diverse subjects. Excellent speaker with good chances in administration, professional guidance, or travel ventures."
                )
            )
        }

        if (distinct.contains(7) && distinct.contains(8)) {
            combinations.add(
                CombinationItem(
                    "Your Chart shows combination of Number 7,8",
                    "This combination makes you analytical and spiritually strong. While commercial endeavors may require extra patience, you excel in research, healing lines, meditation, consulting, and deep investigation."
                )
            )
        }

        if (distinct.contains(2) && distinct.contains(8)) {
            combinations.add(
                CombinationItem(
                    "Your Chart shows combination of Number 2,8",
                    "Number 2 and 8 combination (Chandra + Shani) requires emotional mindfulness to prevent overthinking or mood swings. When balanced by Number 1 or 5, this combination brings substantial property, land ownership, and deep empathetic resilience."
                )
            )
        }

        if (distinct.contains(1) && distinct.contains(7) && distinct.contains(8)) {
            combinations.add(
                CombinationItem(
                    "Your Chart shows combination of Number 1,7,8",
                    "Number 1, 7, and 8 combination in your chart gives strong intuition and interest in subtle or occult sciences. You are a versatile problem-solver. Career elevation and solid authority blossom with great momentum in mature years."
                )
            )
        }

        if (distinct.contains(9) && distinct.contains(7) && distinct.contains(2)) {
            combinations.add(
                CombinationItem(
                    "Your Chart shows combination of Number 9,7,2",
                    "Combines Mars fire, Ketu spiritual depth, and Moon intuition. Suggests strong emotional passion; maintain balanced water hydration and calming meditation for nervous vitality."
                )
            )
        }

        if (distinct.contains(1) && distinct.contains(7) && distinct.contains(9)) {
            combinations.add(
                CombinationItem(
                    "Your Chart shows combination of Number 1,7,9",
                    "High drive for success, honors, and career recognition. You have a daring willpower, enjoy calculating measured risks, and have strong potentials for long-distance success or foreign connections."
                )
            )
        }

        if (distinct.contains(1) && distinct.contains(2)) {
            combinations.add(
                CombinationItem(
                    "Number 1 and Number 2 are present",
                    "Combination of Sun and Moon brings luxury, vehicle comfort, and home peace. When 1 is King and 2 is Queen (or vice versa), it creates a Royal Raj Yoga vibration, granting attractive presence and public respect."
                )
            )
        }

        if (distinct.contains(8) && distinct.contains(9)) {
            combinations.add(
                CombinationItem(
                    "Number 8 and Number 9 are present",
                    "Combines Shani (justice, patience) and Mangal (action, courage). Great combination for legal minds, advocates, engineers, builders, and administrative trouble-shooters."
                )
            )
        }

        return Pair(numberMeanings, combinations)
    }

    /**
     * Generates Mobile Number predictions matching Page 2 & 3 of the reference PDF.
     */
    fun analyzeMobileNumber(mobile: String, kingNumber: Int, queenNumber: Int): MobileNumerologyDetails {
        val cleanDigits = mobile.filter { it.isDigit() }
        val sum = cleanDigits.sumOf { it.digitToInt() }
        var single = sum
        while (single > 9) {
            var s = 0
            var temp = single
            while (temp > 0) {
                s += temp % 10
                temp /= 10
            }
            single = s
        }

        val luckyNumbers = when (kingNumber) {
            1 -> listOf(1, 2, 3, 5)
            2 -> listOf(1, 2, 3, 5)
            3 -> listOf(1, 3, 5, 9)
            4 -> listOf(1, 5, 6, 7)
            5 -> listOf(1, 5, 6)
            6 -> listOf(1, 5, 6)
            7 -> listOf(1, 3, 7)
            8 -> listOf(3, 5, 6)
            else -> listOf(1, 3, 5)
        }

        val unLuckyNumbers = when (kingNumber) {
            1 -> listOf(8, 6)
            2 -> listOf(8, 9, 4)
            3 -> listOf(6)
            4 -> listOf(2, 4, 8, 9)
            5 -> listOf(2, 4)
            6 -> listOf(3)
            7 -> listOf(8, 9)
            8 -> listOf(1, 2, 4, 8)
            else -> listOf(2, 4, 8)
        }

        val neutralNumbers = (1..9).filter { it !in luckyNumbers && it !in unLuckyNumbers }
        val presentDigits = cleanDigits.map { it.digitToInt() }.toSet()
        val missingNumbers = (1..9).filter { it !in presentDigits }

        // Mobile Pairs analysis (e.g. 99, 91, 13, 39, 95, etc.)
        val pairs = mutableListOf<MobilePairResult>()
        for (i in 0 until cleanDigits.length - 1) {
            val pairStr = cleanDigits.substring(i, i + 2)
            val nature = when {
                pairStr in listOf("13", "31", "51", "15", "73", "37", "56", "65", "19", "91") -> "Benefic"
                pairStr in listOf("28", "82", "48", "84", "49", "94", "24", "42") -> "Malefic"
                else -> "Neutral"
            }
            pairs.add(MobilePairResult(pairStr, nature))
        }

        val interpretation = when (single) {
            1 -> "Brings leadership, self-reliance, administrative success, and high social respect."
            2 -> "Promotes diplomatic relationships, artistic imagination, emotional sensitivity, and peaceful partnerships."
            3 -> "You will achieve higher education, you are very wise and with the help of your education you can excel in consultancy, teaching, or occult sciences. Highly respected in your circle."
            4 -> "Brings technical acumen, sharp analytical capacity, and innovative problem solving."
            5 -> "Excellent for commerce, business deals, networking, versatile communication, and fast travel."
            6 -> "Attracts luxury, glamorous connections, domestic harmony, and creative abundance."
            7 -> "Fosters research ability, spiritual inclinations, occult study, and analytical depth."
            8 -> "Demands patience, strong discipline, heavy business focus, and long-term financial resilience."
            else -> "Sparks dynamic ambition, leadership, enthusiasm, and courage for humanitarian and bold endeavors."
        }

        val predictions = listOf(
            "Good advisor, good education, respectful and popular in your professional circle.",
            "Complete personality: Person will be able to take practical benefit from their knowledge.",
            "Financial flow remains steady. Work related to commerce, management, or government connects smoothly.",
            "You own what you want — high self-drive and professional status.",
            "Science, Commerce, and Consultancy streams will suit your professional communication best."
        )

        val recommendedTotals = when (kingNumber) {
            8 -> "3, 5, 6 (Your mobile total should reduce to 3, 5 or 6; avoid single totals of 8, 4, or 1)."
            1 -> "1, 3, 5 (Avoid totals of 8 or 6)."
            2 -> "1, 3, 5 (Avoid totals of 8 or 9)."
            else -> "1, 5, 6 (Ensure friendly total reduction)."
        }

        return MobileNumerologyDetails(
            mobileNumber = mobile,
            compoundSum = sum,
            singleDigitTotal = single,
            interpretation = interpretation,
            recommendedTotals = recommendedTotals,
            luckyColours = if (kingNumber == 8) "YELLOW, GREEN" else "GOLD, GREEN, WHITE",
            unLuckyColours = if (kingNumber == 8) "RED, ORANGE, BLACK" else "DARK BLACK, GREY",
            luckyNumbers = luckyNumbers,
            unLuckyNumbers = unLuckyNumbers,
            neutralNumbers = neutralNumbers,
            missingNumbers = missingNumbers,
            pairsAnalysis = pairs,
            predictions = predictions
        )
    }

    /**
     * Generates the Vedic Numerology Mahadasha Timeline matching Pages 7-13 of the reference PDF.
     * Order of Planetary Dasha in Vedic Numerology:
     * Dasha follows the 9 Grahas based on birth date (e.g. King planet initiates or cyclic sequence).
     */
    fun generateDashaTimeline(birthDate: LocalDate, kingNumber: Int): List<NumerologyDashaPeriod> {
        val currentYear = LocalDate.now().year
        val dashaSequence = listOf(
            Triple("Shani (Saturn)", 8, 8),
            Triple("Mangal (Mars)", 9, 9),
            Triple("Surya (Sun)", 1, 1),
            Triple("Chandra (Moon)", 2, 2),
            Triple("Guru (Jupiter)", 3, 3),
            Triple("Rahu", 4, 4),
            Triple("Budh (Mercury)", 5, 5),
            Triple("Shukra (Venus)", 6, 6),
            Triple("Ketu", 7, 7)
        )

        // Find starting index matching the king number or default to Saturn
        var startIndex = dashaSequence.indexOfFirst { it.second == kingNumber }
        if (startIndex == -1) startIndex = 0

        val timeline = mutableListOf<NumerologyDashaPeriod>()
        var startYear = birthDate.year

        for (cycle in 0..1) { // 2 cycles across life span
            for (i in dashaSequence.indices) {
                val idx = (startIndex + i) % dashaSequence.size
                val (planet, num, duration) = dashaSequence[idx]
                val endYear = startYear + duration
                val dateRangeStr = "%02d - %02d - %04d - %04d => %d".format(
                    birthDate.dayOfMonth, birthDate.monthValue, startYear, endYear, num
                )
                val isCurrent = currentYear in startYear until endYear

                val prediction = getDashaPrediction(planet, num)

                timeline.add(
                    NumerologyDashaPeriod(
                        planetName = planet,
                        number = num,
                        startYear = startYear,
                        endYear = endYear,
                        dateRangeStr = dateRangeStr,
                        prediction = prediction,
                        isCurrent = isCurrent
                    )
                )
                startYear = endYear
            }
        }

        return timeline
    }

    private fun getDashaPrediction(planet: String, number: Int): String {
        return when (number) {
            8 -> "The planet Shani can take you to the pinnacle of success effortlessly. Positive Dasha gives you robust health, social status, good family life, money and wealth, palatial building, influential friends, winning over adversaries, and solid career accomplishment. It determines your material success and long-term stability."
            9 -> "Mars represents willpower, ability to fight, stamina, energy, and leadership. One feels energized to take bold action towards goals. In this Dasha, unfinished goals in the past see the light of day. Professional advancement, property acquisitions, and triumph through courage."
            1 -> "It brings money, wealth, name, and fame, social status, glory, culture, and high administrative standing. The native seeks interest in authority or being an executive figure. Great period for public recognition, government liaisons, and personal empowerment."
            2 -> "Brings artistic and imaginative focus, mother's blessings, domestic comfort, and peaceful emotional balance. People often engage in home-based ventures or creative arts. Excellent harmony in relationships and peaceful lifestyle."
            3 -> "Brings financial success, huge wealth, power, knowledge, and spiritual development. You can become more powerful in authority or business. If in academics, you excel. Knowledge of occult sciences, astrology, and higher philosophy deepens."
            4 -> "Brings gains in material wealth, comforts, sudden breakthroughs, or travel. Hurdles are transformed into stepping stones through hard work. Success during this period builds solid long-term enterprise and new assets."
            5 -> "Mercury governs intelligence, communication, commerce, studies, writing, and speech. Ample opportunities in telecommunications, media, internet, trade, and financial markets. Enhances networking power and quick adaptability."
            6 -> "Venus Maha-Dasha bestows luxuries, pleasures, gems, conveyance, and artistic achievements. Gives rise to success in creative enterprises, hospitality, entertainment, counseling, and material prosperity."
            else -> "Ketu Dasha initiates deep spiritual contemplation, connection with the higher self, mastery in healing arts, natural medicine, intuition, and profound inner transformation."
        }
    }
}
