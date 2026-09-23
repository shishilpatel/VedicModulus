package com.example.wear

import androidx.compose.ui.graphics.Color

data class AuspiciousPalette(
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val backgroundFillHex: Long
)

object NumerologyColorMapper {
    fun getPaletteForLifePathNumber(lifePathNumber: Int): AuspiciousPalette {
        return when (lifePathNumber) {
            1 -> AuspiciousPalette(0xFFEAB308, 0xFFF97316, 0xFF451A03) // Sun (Ruby/Gold/Orange)
            2 -> AuspiciousPalette(0xFFE2E8F0, 0xFF38BDF8, 0xFF0F172A) // Moon (Pearl/White/Silver)
            3 -> AuspiciousPalette(0xFFFDE047, 0xFFCA8A04, 0xFF422006) // Jupiter (Yellow Sapphire)
            4 -> AuspiciousPalette(0xFF94A3B8, 0xFF475569, 0xFF020617) // Rahu (Hessonite/Dark Blue/Gray)
            5 -> AuspiciousPalette(0xFF4ADE80, 0xFF10B981, 0xFF064E3B) // Mercury (Emerald/Green)
            6 -> AuspiciousPalette(0xFFF472B6, 0xFF38BDF8, 0xFF1E1E38) // Venus (Diamond/Pink/Light Blue)
            7 -> AuspiciousPalette(0xFFA78BFA, 0xFFC084FC, 0xFF2E1065) // Ketu (Cat's Eye/Multicolor/Purple)
            8 -> AuspiciousPalette(0xFF3B82F6, 0xFF1D4ED8, 0xFF172554) // Saturn (Blue Sapphire/Dark Blue)
            9 -> AuspiciousPalette(0xFFEF4444, 0xFFDC2626, 0xFF450A0A) // Mars (Coral/Red)
            else -> AuspiciousPalette(0xFFFFD166, 0xFFF43F5E, 0xFF1E293B) // Default Cosmic
        }
    }
}
