package com.example.engines.festival

import com.example.core.model.PanchangaData
import com.example.core.model.Paksha
import java.time.ZonedDateTime

data class Festival(
    val name: String,
    val description: String,
    val isMajor: Boolean = false
)

class HinduFestivalEngine {
    
    fun calculateFestivals(panchanga: PanchangaData, date: ZonedDateTime): List<Festival> {
        val festivals = mutableListOf<Festival>()
        val tithiNumber = panchanga.tithi.number
        val paksha = panchanga.tithi.paksha
        val masa = panchanga.masa
        
        // Ekadashi (11th Tithi)
        if (tithiNumber == 11 || tithiNumber == 26) {
            val prefix = if (paksha == Paksha.SHUKLA) "Shukla" else "Krishna"
            festivals.add(Festival("Ekadashi ($prefix)", "Sacred day of Lord Vishnu, widely observed for fasting."))
        }
        
        // Purnima (Full Moon)
        if (tithiNumber == 15) {
            festivals.add(Festival("Purnima", "Full Moon day. Auspicious for Satyanarayan Puja and spiritual activities.", isMajor = true))
        }
        
        // Amavasya (New Moon)
        if (tithiNumber == 30) {
            festivals.add(Festival("Amavasya", "New Moon day. Traditional day for honoring ancestors (Pitru Tarpan)."))
        }
        
        // Major Festivals based on Tithi & Masa (Simplified for demonstration)
        if (masa.equals("Chaitra", ignoreCase = true) && paksha == Paksha.SHUKLA && tithiNumber == 9) {
            festivals.add(Festival("Rama Navami", "Appearance day of Lord Rama.", isMajor = true))
        }
        
        if (masa.equals("Shravana", ignoreCase = true) && tithiNumber == 15) {
            festivals.add(Festival("Raksha Bandhan", "Festival celebrating the bond between brothers and sisters.", isMajor = true))
        }
        
        if (masa.equals("Bhadrapada", ignoreCase = true) && paksha == Paksha.SHUKLA && tithiNumber == 4) {
            festivals.add(Festival("Ganesh Chaturthi", "Festival honoring Lord Ganesha.", isMajor = true))
        }
        
        if (masa.equals("Ashvina", ignoreCase = true) && paksha == Paksha.SHUKLA && tithiNumber == 10) {
            festivals.add(Festival("Dussehra (Vijayadashami)", "Triumph of good over evil.", isMajor = true))
        }
        
        if (masa.equals("Kartika", ignoreCase = true) && tithiNumber == 30) {
            festivals.add(Festival("Diwali (Deepavali)", "Festival of Lights.", isMajor = true))
        }
        
        if (masa.equals("Phalguna", ignoreCase = true) && tithiNumber == 15) {
            festivals.add(Festival("Holi", "Festival of Colors.", isMajor = true))
        }
        
        if (masa.equals("Magha", ignoreCase = true) && paksha == Paksha.KRISHNA && tithiNumber == 14) {
            festivals.add(Festival("Maha Shivaratri", "Great night of Lord Shiva.", isMajor = true))
        }

        return festivals
    }
}
