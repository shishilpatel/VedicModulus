package com.example.core.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ConsultationStatus(val displayName: String, val badgeColorHex: Long) {
    PENDING("Awaiting Scholar", 0xFFEAB308),
    IN_REVIEW("In Astrologer Review", 0xFF3B82F6),
    RESPONDED("Scholar Responded", 0xFF10B981),
    CLOSED("Completed", 0xFF64748B)
}

data class ConsultationMessage(
    val id: String = System.currentTimeMillis().toString(),
    val senderName: String,
    val isAstrologer: Boolean,
    val textContent: String,
    val audioNoteUrl: String? = null,
    val audioDurationSeconds: Int = 0,
    val timestampMs: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        return SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date(timestampMs))
    }
}

data class ConsultationQuery(
    val id: String = "QUERY_${System.currentTimeMillis()}",
    val seekerName: String,
    val seekerEmail: String,
    val questionTopic: String,
    val detailedQuestion: String,
    val assignedAstrologerName: String = "Acharya Vikramaditya Shastri",
    val status: ConsultationStatus = ConsultationStatus.RESPONDED,
    val feeInr: Double = 199.0,
    val messages: List<ConsultationMessage> = listOf(
        ConsultationMessage(
            senderName = seekerName,
            isAstrologer = false,
            textContent = detailedQuestion,
            timestampMs = System.currentTimeMillis() - 86400000
        ),
        ConsultationMessage(
            senderName = assignedAstrologerName,
            isAstrologer = true,
            textContent = "Hari Om! Based on your Jupiter Mahadasha and current Moon transit in Taurus, the upcoming 3 months are highly favorable for career transition. Chant the Rahu Shanti Stotram daily after sunrise.",
            audioNoteUrl = "https://example.com/audio/scholar_guidance_1.mp3",
            audioDurationSeconds = 42,
            timestampMs = System.currentTimeMillis() - 3600000
        )
    )
)
