package com.example.core.model

/**
 * Classical and regional Kundali (horoscope) chart styles used across India.
 */
enum class KundaliChartStyle(
    val title: String,
    val hindiTitle: String,
    val region: String,
    val description: String
) {
    NORTH_INDIAN(
        title = "North Indian",
        hindiTitle = "उत्तर भारतीय (चतुरस्र)",
        region = "North & Central India",
        description = "Fixed Houses, rotating signs. House 1 (Lagna) is always the top-center diamond."
    ),
    SOUTH_INDIAN(
        title = "South Indian",
        hindiTitle = "दक्षिण भारतीय (स्थिर राशि)",
        region = "Tamil Nadu, Andhra, Karnataka, Kerala",
        description = "Fixed Signs in a clockwise perimeter grid. Lagna is marked with diagonal lines (//)."
    ),
    EAST_INDIAN(
        title = "East Indian",
        hindiTitle = "पूर्व भारतीय (बंगाली / उड़िया)",
        region = "Bengal, Odisha, Assam, Mithila",
        description = "Fixed Signs running counter-clockwise. Aries (Mesha) is fixed at the top chamber."
    ),
    CIRCULAR(
        title = "Circular Chakra",
        hindiTitle = "भाव चक्र (वृत्ताकार)",
        region = "Classical & Modern Jyotisha",
        description = "360° concentric wheel chart displaying all 12 Bhavas, planetary transits, and aspects."
    )
}
