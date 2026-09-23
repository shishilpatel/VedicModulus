package com.example.engines.festival

import com.example.core.model.PanchangaData
import com.example.core.model.TimingWindow
import java.time.ZonedDateTime

data class PujaTimingRecommendation(
    val pujaName: String,
    val recommendedWindow: TimingWindow?,
    val reasoning: String,
    val isHighlyAuspicious: Boolean
)

class PujaTimingEngine {
    
    fun getDailyPujaRecommendations(
        panchanga: PanchangaData, 
        brahmaMuhurta: TimingWindow,
        pradosha: TimingWindow
    ): List<PujaTimingRecommendation> {
        val recommendations = mutableListOf<PujaTimingRecommendation>()
        
        // 1. Daily Morning Puja
        recommendations.add(
            PujaTimingRecommendation(
                pujaName = "Daily Morning Puja (Sandhya Vandanam)",
                recommendedWindow = brahmaMuhurta,
                reasoning = "Brahma Muhurta is the most serene and sattvic time, ideal for meditation and morning prayers before sunrise.",
                isHighlyAuspicious = true
            )
        )
        
        // 2. Shiva Puja (especially during Pradosha)
        val isTrayodashi = panchanga.tithi.number == 13 || panchanga.tithi.number == 28
        recommendations.add(
            PujaTimingRecommendation(
                pujaName = "Shiva Puja",
                recommendedWindow = pradosha,
                reasoning = if (isTrayodashi) "Today is Trayodashi. Pradosha kala on this day is supremely auspicious for Lord Shiva." else "Evening twilight (Pradosha Kala) is the traditional time for Shiva worship.",
                isHighlyAuspicious = isTrayodashi
            )
        )
        
        // 3. Lakshmi Puja (Friday or Purnima/Amavasya specific)
        val isFriday = panchanga.vara.equals("Shukravāra", ignoreCase = true)
        val isDiwali = panchanga.masa.equals("Kartika", ignoreCase = true) && panchanga.tithi.number == 30
        if (isFriday || isDiwali || panchanga.tithi.number == 15) {
            recommendations.add(
                PujaTimingRecommendation(
                    pujaName = "Lakshmi Puja",
                    recommendedWindow = pradosha, // typically evening
                    reasoning = if (isDiwali) "Diwali Amavasya is the most significant night for Mahalakshmi Puja." else "Fridays and Full Moon days are highly favorable for Goddess Lakshmi.",
                    isHighlyAuspicious = true
                )
            )
        }
        
        return recommendations
    }
}
