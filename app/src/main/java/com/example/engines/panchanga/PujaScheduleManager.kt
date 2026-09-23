package com.example.engines.panchanga

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

enum class PujaCategory(val displayName: String) {
    EKADASHI("Ekadashi (एकादशी)"),
    PRADOSHAM("Pradosham (प्रदोष)"),
    PURNIMA("Purnima (पूर्णिमा)"),
    AMAVASYA("Amavasya (अमावस्या)"),
    SANKASHTI("Chaturthi (चतुर्थी)"),
    SANKRANTI("Sankranti (संक्रान्ति)"),
    SPECIAL_PUJA("Special Puja (विशेष पूजा)")
}

data class UpcomingPujaEvent(
    val id: String,
    val name: String,
    val devanagari: String,
    val category: PujaCategory,
    val deity: String,
    val tithiName: String,
    val date: ZonedDateTime,
    val pujaWindow: String,
    val vrataRules: List<String>,
    val sacredMantra: String,
    val spiritualSignificance: String
)

object PujaScheduleManager {

    /**
     * Generates the upcoming list of sacred observances starting from reference date.
     */
    fun getUpcomingPujas(
        referenceDate: ZonedDateTime = ZonedDateTime.now(),
        daysAhead: Int = 30
    ): List<UpcomingPujaEvent> {
        val zone = referenceDate.zone
        val list = mutableListOf<UpcomingPujaEvent>()

        // 1. Ekadashi (Upcoming Shukla / Krishna Ekadashi)
        val nextEkadashiDate = referenceDate.plusDays(4).withHour(6).withMinute(0).withSecond(0)
        list.add(
            UpcomingPujaEvent(
                id = "ekadashi_upcoming",
                name = "Ekadashi Vrata",
                devanagari = "पवित्र एकादशी व्रत",
                category = PujaCategory.EKADASHI,
                deity = "Bhagavan Vishnu / Sri Krishna",
                tithiName = "Ekadashi (11th Lunar Day)",
                date = nextEkadashiDate,
                pujaWindow = "All-day Fasting • Parana Next Dawn: 06:12 AM – 08:30 AM",
                vrataRules = listOf(
                    "Strict abstinence from grains, rice, and pulses",
                    "Sattvic fruits, milk, water, or Nirjala (waterless)",
                    "Chant 108 malas of Mahamantra or Vishnu Sahasranama",
                    "Charity to the needy on Dwadashi morning before Parana"
                ),
                sacredMantra = "ॐ नमो भगवते वासुदेवाय",
                spiritualSignificance = "Cleanses all accrued karmas, heightens spiritual awareness, and pleases Lord Hari."
            )
        )

        // 2. Pradosham Vrata (Trayodashi twilight)
        val nextPradoshamDate = referenceDate.plusDays(6).withHour(18).withMinute(15).withSecond(0)
        list.add(
            UpcomingPujaEvent(
                id = "pradosham_upcoming",
                name = "Shani Pradosham",
                devanagari = "शनि प्रदोष व्रत",
                category = PujaCategory.PRADOSHAM,
                deity = "Bhagavan Mahadeva Shiva & Maa Parvati",
                tithiName = "Trayodashi (13th Lunar Day)",
                date = nextPradoshamDate,
                pujaWindow = "Pradosha Sandhya Kalam: 06:15 PM – 08:35 PM",
                vrataRules = listOf(
                    "Fasting from sunrise until the twilight Pradosha Puja",
                    "Perform Shiva Abhishekam with Bilva leaves, milk, and honey",
                    "Recite Shiva Tandava Stotram or Maha Mrityunjaya Mantra 108 times",
                    "Break fast after evening Darshan with light Satvik Prasad"
                ),
                sacredMantra = "ॐ नमः शिवाय • ॐ त्र्यम्बकं यजामहे",
                spiritualSignificance = "Dissolves planetary afflictions, grants relief from Saturn (Shani) afflictions, and bestows peace."
            )
        )

        // 3. Satyanarayan Purnima
        val nextPurnimaDate = referenceDate.plusDays(8).withHour(17).withMinute(30).withSecond(0)
        list.add(
            UpcomingPujaEvent(
                id = "purnima_upcoming",
                name = "Sri Satyanarayan Purnima",
                devanagari = "श्री सत्यनारायण पूर्णिमा",
                category = PujaCategory.PURNIMA,
                deity = "Sri Satyanarayan Swami & Chandra Deva",
                tithiName = "Purnima (15th Lunar Day - Full Moon)",
                date = nextPurnimaDate,
                pujaWindow = "Moonrise Puja: 06:45 PM – 09:15 PM",
                vrataRules = listOf(
                    "Prepare Sheera (Sooji Halwa) with panchamrit, banana, and tulasi",
                    "Read the 5 sacred chapters of Sri Satyanarayan Katha with family",
                    "Offer Arghya to the rising Full Moon with raw milk and water",
                    "Meditation on the Sahasrara Chakra under moonlight"
                ),
                sacredMantra = "ॐ श्रीं सत्यनारायणाय नमः",
                spiritualSignificance = "Brings harmony, prosperity, health, and fulfillment of family endeavors."
            )
        )

        // 4. Sankashti Chaturthi
        val nextChaturthiDate = referenceDate.plusDays(12).withHour(20).withMinute(45).withSecond(0)
        list.add(
            UpcomingPujaEvent(
                id = "sankashti_upcoming",
                name = "Sankashti Ganesha Chaturthi",
                devanagari = "संकष्टी श्री गणेश चतुर्थी",
                category = PujaCategory.SANKASHTI,
                deity = "Bhagavan Sri Ganesha (Vighnaharta)",
                tithiName = "Krishna Chaturthi (4th Lunar Day)",
                date = nextChaturthiDate,
                pujaWindow = "Moonrise Darshan & Puja: 08:50 PM – 10:10 PM",
                vrataRules = listOf(
                    "Fast from sunrise until sighting of the Moon (Chandra Darshan)",
                    "Offer 21 blades of Durva grass and Modaks or Laddus to Lord Ganesha",
                    "Chant Sankata Nashana Ganesha Stotram 3 times",
                    "Arghya to the Moon with water, chandan, and akshat"
                ),
                sacredMantra = "ॐ गं गणपतये नमः",
                spiritualSignificance = "Removes immovable obstacles, financial impediments, and restores auspicious progress."
            )
        )

        // 5. Amavasya Pitru Tarpan
        val nextAmavasyaDate = referenceDate.plusDays(22).withHour(11).withMinute(30).withSecond(0)
        list.add(
            UpcomingPujaEvent(
                id = "amavasya_upcoming",
                name = "Somvati Amavasya & Pitru Tarpan",
                devanagari = "सोमवती अमावस्या पितृ तर्पण",
                category = PujaCategory.AMAVASYA,
                deity = "Pitru Devatas (Ancestors) & Lord Shiva",
                tithiName = "Amavasya (New Moon Day)",
                date = nextAmavasyaDate,
                pujaWindow = "Aparahna Pitru Muhurta: 11:45 AM – 02:15 PM",
                vrataRules = listOf(
                    "Perform water, black sesame (Kala Til), and Kusha grass Tarpan",
                    "Feed cows (Gau Seva), crows, and provide meals to Brahmins or the poor",
                    "Circumambulate the sacred Peepal tree (Ashvattha Vriksha) 108 times",
                    "Light a mustard oil deepam facing South in the evening"
                ),
                sacredMantra = "ॐ पितृभ्यो नमः • ॐ नमो भगवते वासुदेवाय",
                spiritualSignificance = "Grants eternal peace to departed ancestors, pacifies Pitru Dosha, and blesses progeny."
            )
        )

        // 6. Surya Sankranti
        val nextSankrantiDate = referenceDate.plusDays(17).withHour(7).withMinute(15).withSecond(0)
        list.add(
            UpcomingPujaEvent(
                id = "sankranti_upcoming",
                name = "Surya Sankranti Punya Kaal",
                devanagari = "सूर्य संक्रान्ति पुण्यकाल",
                category = PujaCategory.SANKRANTI,
                deity = "Surya Bhagavan (Aditya)",
                tithiName = "Solar Ingress into New Rashi",
                date = nextSankrantiDate,
                pujaWindow = "Mahapunya Kaal: 06:10 AM – 10:30 AM",
                vrataRules = listOf(
                    "Sacred dawn snana (bath) in flowing water or with gangajal",
                    "Offer Arghya to the rising Sun with copper kalash, red chandan, and roli",
                    "Donate food grains, copper, jaggery, or warm clothing",
                    "Chant Aditya Hridaya Stotram with devotion"
                ),
                sacredMantra = "ॐ घृणिः सूर्याय नमः",
                spiritualSignificance = "Boosts vitality, career leadership, solar energy, and removes bodily fatigue."
            )
        )

        return list.sortedBy { it.date }
    }
}
