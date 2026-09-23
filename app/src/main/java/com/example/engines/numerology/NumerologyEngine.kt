package com.example.engines.numerology

import com.example.core.model.Graha
import com.example.core.model.NumerologyProfile
import com.example.core.model.NumerologySystem
import java.time.LocalDate

class NumerologyEngine {

    companion object {
        // Chaldean mapping table (1 to 8)
        private val CHALDEAN_MAP = mapOf(
            'A' to 1, 'I' to 1, 'J' to 1, 'Q' to 1, 'Y' to 1,
            'B' to 2, 'K' to 2, 'R' to 2,
            'C' to 3, 'G' to 3, 'L' to 3, 'S' to 3,
            'D' to 4, 'M' to 4, 'T' to 4,
            'E' to 5, 'H' to 5, 'N' to 5, 'X' to 5,
            'U' to 6, 'V' to 6, 'W' to 6,
            'O' to 7, 'Z' to 7,
            'F' to 8, 'P' to 8
        )

        // Pythagorean mapping table (1 to 9)
        private val PYTHAGOREAN_MAP = mapOf(
            'A' to 1, 'J' to 1, 'S' to 1,
            'B' to 2, 'K' to 2, 'T' to 2,
            'C' to 3, 'L' to 3, 'U' to 3,
            'D' to 4, 'M' to 4, 'V' to 4,
            'E' to 5, 'N' to 5, 'W' to 5,
            'F' to 6, 'O' to 6, 'X' to 6,
            'G' to 7, 'P' to 7, 'Y' to 7,
            'H' to 8, 'Q' to 8, 'Z' to 8,
            'I' to 9, 'R' to 9
        )
    }

    /**
     * Reduces any positive integer to a single digit between 1 and 9.
     */
    fun reduceToSingleDigit(num: Int): Int {
        var n = num
        while (n > 9) {
            var sum = 0
            while (n > 0) {
                sum += n % 10
                n /= 10
            }
            n = sum
        }
        return if (n == 0) 9 else n
    }

    /**
     * Computes the complete numerological profile for the given birth date and name.
     */
    fun calculateNumerology(
        birthDate: LocalDate,
        fullName: String,
        system: NumerologySystem = NumerologySystem.CHALDEAN
    ): NumerologyProfile {
        // 1. Birth Number (Mulank) = sum of digits of birth day
        val mulank = reduceToSingleDigit(birthDate.dayOfMonth)

        // 2. Life Path Number (Bhagyank) = sum of all digits of day + month + year
        val totalDateSum = birthDate.dayOfMonth + birthDate.monthValue + birthDate.year
        val bhagyank = reduceToSingleDigit(totalDateSum)

        // 3. Name Number (Namank)
        val lookup = if (system == NumerologySystem.CHALDEAN) CHALDEAN_MAP else PYTHAGOREAN_MAP
        val nameSum = fullName.uppercase()
            .filter { it in 'A'..'Z' }
            .sumOf { lookup[it] ?: 0 }
        val namank = reduceToSingleDigit(if (nameSum == 0) mulank else nameSum)

        // Astrological correspondences based on Mulank
        val rulingGraha = when (mulank) {
            1 -> Graha.SURYA
            2 -> Graha.CHANDRA
            3 -> Graha.GURU
            4 -> Graha.RAHU
            5 -> Graha.BUDHA
            6 -> Graha.SHUKRA
            7 -> Graha.KETU
            8 -> Graha.SHANI
            else -> Graha.MANGALA
        }

        val favorableNumbers = when (mulank) {
            1 -> listOf(1, 2, 4, 7)
            2 -> listOf(1, 2, 7)
            3 -> listOf(3, 6, 9)
            4 -> listOf(1, 4, 8)
            5 -> listOf(1, 5, 6)
            6 -> listOf(3, 6, 9)
            7 -> listOf(1, 2, 7)
            8 -> listOf(4, 8)
            else -> listOf(3, 6, 9)
        }

        val favorableColors = when (mulank) {
            1 -> listOf("Gold", "Saffron", "Solar Yellow")
            2 -> listOf("Pearl White", "Silver", "Ivory")
            3 -> listOf("Jupiter Gold", "Imperial Yellow", "Royal Violet")
            4 -> listOf("Electric Blue", "Obsidian Grey")
            5 -> listOf("Emerald Green", "Mint", "Jade")
            6 -> listOf("Rose Pink", "Diamond White", "Turquoise")
            7 -> listOf("Seafoam Green", "Moonstone White", "Mist")
            8 -> listOf("Deep Navy", "Midnight Indigo", "Steel")
            else -> listOf("Crimson Red", "Coral", "Maroon")
        }

        val favorableDays = when (mulank) {
            1 -> listOf("Sunday", "Monday")
            2 -> listOf("Monday", "Sunday")
            3 -> listOf("Thursday", "Tuesday")
            4 -> listOf("Sunday", "Saturday")
            5 -> listOf("Wednesday", "Friday")
            6 -> listOf("Friday", "Tuesday")
            7 -> listOf("Monday", "Wednesday")
            8 -> listOf("Saturday", "Friday")
            else -> listOf("Tuesday", "Thursday")
        }

        return NumerologyProfile(
            birthNumber = mulank,
            lifePathNumber = bhagyank,
            destinyNameNumber = namank,
            favorableNumbers = favorableNumbers,
            favorableColors = favorableColors,
            favorableDays = favorableDays,
            rulingPlanet = rulingGraha
        )
    }
}
