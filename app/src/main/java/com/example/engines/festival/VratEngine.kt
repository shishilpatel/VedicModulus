package com.example.engines.festival

import com.example.core.model.PanchangaData
import com.example.core.model.Paksha
import com.example.engines.muhurta.MuhurtaEngine
import java.time.ZonedDateTime

data class Vrat(
    val name: String,
    val description: String,
    val recommendedPractice: String,
    val paranaWindow: String? = null // Fast breaking window
)

class VratEngine {

    fun calculateVrats(panchanga: PanchangaData, date: ZonedDateTime): List<Vrat> {
        val vrats = mutableListOf<Vrat>()
        val tithi = panchanga.tithi.number
        
        // Ekadashi Vrat
        if (tithi == 11 || tithi == 26) {
            vrats.add(Vrat(
                name = "Ekadashi Vrat",
                description = "Fasting observed on the 11th lunar day to cleanse the body and mind.",
                recommendedPractice = "Abstain from grains and beans. Focus on chanting and meditation.",
                paranaWindow = "Break fast on Dwadashi (next day) after sunrise but before end of Dwadashi Tithi."
            ))
        }
        
        // Pradosha Vrat
        if (tithi == 13 || tithi == 28) {
            vrats.add(Vrat(
                name = "Pradosha Vrat",
                description = "Observed on Trayodashi Tithi in honor of Lord Shiva.",
                recommendedPractice = "Fast from sunrise until sunset, break fast after evening Shiva Puja during Pradosha Kala."
            ))
        }
        
        // Purnima Vrat
        if (tithi == 15) {
            vrats.add(Vrat(
                name = "Purnima Vrat",
                description = "Full moon fasting.",
                recommendedPractice = "Ideal day for Satyanarayan Puja. Break fast after seeing the full moon or after evening puja."
            ))
        }
        
        // Amavasya Vrat
        if (tithi == 30) {
            vrats.add(Vrat(
                name = "Amavasya Vrat",
                description = "New moon fasting.",
                recommendedPractice = "Focus on charity, spiritual reflection, and offering respects to ancestors."
            ))
        }
        
        return vrats
    }
}
