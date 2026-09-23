package com.example.core.model

/**
 * Live consultation availability status for professional astrologers.
 */
enum class AvailabilityStatus(val displayName: String, val badgeColorHex: Long) {
    ONLINE("Available Now (Online)", 0xFF10B981),
    BUSY("In Consultation (Busy)", 0xFFF59E0B),
    OFFLINE("Offline (Not Accepting Requests)", 0xFF64748B)
}

/**
 * Astrologer Registration Profile data model.
 */
data class AstrologerProfile(
    val astrologerId: String = "astrologer_default_01",
    val fullName: String = "Acharya Vikramaditya Shastri",
    val titleOrLicense: String = "Jyotish Ratna & Vastu Shastracharya (20+ Yrs Exp)",
    val phoneOrEmail: String = "acharyavikram@vedic.app",
    val experienceYears: Int = 22,
    val specializations: List<String> = listOf("Vedic Astrology", "Janam Kundali", "Vastu Shastra", "Kundali Matching", "Prashna Kundali"),
    val languagesSpoken: List<String> = listOf("Hindi", "English", "Sanskrit", "Gujarati"),
    val bio: String = "Classical Vedic Scholar specializing in Parashari Jyotish, Vimshottari Dasha timing of events, and non-destructive Vastu remedies.",
    
    // Consultation Rates
    val hourlyConsultationRateInr: Double = 499.0,
    val perQuestionRateInr: Double = 199.0,
    val detailedReportRateInr: Double = 499.0,
    
    // Live Availability Status
    val isAvailableForConsultation: Boolean = true,
    val availabilityStatus: AvailabilityStatus = AvailabilityStatus.ONLINE,
    
    // Ratings & Verification
    val rating: Double = 4.9,
    val reviewCount: Int = 348,
    val isVerifiedScholar: Boolean = true,
    
    // Active Consultation Queue Count
    val pendingQueryCount: Int = 3,
    val completedConsultationsCount: Int = 1240
)
