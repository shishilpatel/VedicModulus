package com.example.engines.reports

import com.example.core.model.Rashi

data class AscendantReport(
    val lagnaName: String,
    val rulingPlanet: String,
    val coreNature: String,
    val physicalTraits: String,
    val mentalTraits: String,
    val luckyColors: List<String>,
    val luckyDays: List<String>
)

class AscendantReportEngine {
    fun generateReport(lagna: Rashi): AscendantReport {
        return when (lagna) {
            Rashi.MESHA -> AscendantReport(
                lagnaName = "Aries (Mesha)",
                rulingPlanet = "Mars (Mangala)",
                coreNature = "Active, pioneering, and courageous. You are a natural leader with a strong drive to initiate projects.",
                physicalTraits = "Athletic build, prominent eyebrows, quick movements.",
                mentalTraits = "Independent, sometimes impulsive, highly competitive, and honest.",
                luckyColors = listOf("Red", "Coral"),
                luckyDays = listOf("Tuesday", "Sunday")
            )
            Rashi.VRISHABHA -> AscendantReport(
                lagnaName = "Taurus (Vrishabha)",
                rulingPlanet = "Venus (Shukra)",
                coreNature = "Stable, sensual, and grounded. You value security, art, and the finer things in life.",
                physicalTraits = "Solid stature, expressive eyes, attractive facial features.",
                mentalTraits = "Patient, determined, sometimes stubborn, and very loyal.",
                luckyColors = listOf("White", "Pink", "Light Blue"),
                luckyDays = listOf("Friday", "Wednesday")
            )
            Rashi.MITHUNA -> AscendantReport(
                lagnaName = "Gemini (Mithuna)",
                rulingPlanet = "Mercury (Budha)",
                coreNature = "Communicative, intellectual, and adaptable. You thrive on information, variety, and networking.",
                physicalTraits = "Tall, active, youthful appearance, expressive hands.",
                mentalTraits = "Witty, curious, versatile, but can be restless or indecisive.",
                luckyColors = listOf("Green", "Yellow"),
                luckyDays = listOf("Wednesday", "Friday")
            )
            Rashi.KARKATA -> AscendantReport(
                lagnaName = "Cancer (Karka)",
                rulingPlanet = "Moon (Chandra)",
                coreNature = "Nurturing, emotional, and intuitive. You are deeply connected to home, family, and inner feelings.",
                physicalTraits = "Round face, pale complexion, gentle eyes.",
                mentalTraits = "Empathetic, protective, highly imaginative, sometimes moody.",
                luckyColors = listOf("White", "Silver", "Sea Green"),
                luckyDays = listOf("Monday", "Thursday")
            )
            Rashi.SIMHA -> AscendantReport(
                lagnaName = "Leo (Simha)",
                rulingPlanet = "Sun (Surya)",
                coreNature = "Regal, confident, and dramatic. You shine in the spotlight and possess a warm, generous heart.",
                physicalTraits = "Broad shoulders, authoritative presence, prominent mane of hair.",
                mentalTraits = "Charismatic, proud, creative, and highly principled.",
                luckyColors = listOf("Gold", "Orange", "Ruby Red"),
                luckyDays = listOf("Sunday", "Tuesday")
            )
            Rashi.KANYA -> AscendantReport(
                lagnaName = "Virgo (Kanya)",
                rulingPlanet = "Mercury (Budha)",
                coreNature = "Analytical, practical, and service-oriented. You seek perfection and have a keen eye for detail.",
                physicalTraits = "Neat appearance, slender, thoughtful expression.",
                mentalTraits = "Logical, methodical, helpful, sometimes overly critical or anxious.",
                luckyColors = listOf("Emerald Green", "Earthy Tones"),
                luckyDays = listOf("Wednesday", "Friday")
            )
            Rashi.TULA -> AscendantReport(
                lagnaName = "Libra (Tula)",
                rulingPlanet = "Venus (Shukra)",
                coreNature = "Diplomatic, harmonious, and relationship-focused. You seek balance and aesthetics in all areas.",
                physicalTraits = "Well-proportioned, charming smile, graceful.",
                mentalTraits = "Fair-minded, sociable, peace-loving, can be indecisive.",
                luckyColors = listOf("Opal", "Pastels", "Light Blue"),
                luckyDays = listOf("Friday", "Wednesday")
            )
            Rashi.VRISHCHIKA -> AscendantReport(
                lagnaName = "Scorpio (Vrishchika)",
                rulingPlanet = "Mars (Mangala) & Ketu",
                coreNature = "Intense, transformative, and secretive. You possess immense emotional depth and resilience.",
                physicalTraits = "Piercing gaze, robust, magnetic presence.",
                mentalTraits = "Determined, intuitive, passionate, sometimes guarded or possessive.",
                luckyColors = listOf("Deep Red", "Black", "Maroon"),
                luckyDays = listOf("Tuesday", "Thursday")
            )
            Rashi.DHANUS -> AscendantReport(
                lagnaName = "Sagittarius (Dhanu)",
                rulingPlanet = "Jupiter (Guru)",
                coreNature = "Optimistic, philosophical, and freedom-loving. You are a truth-seeker with a love for travel and learning.",
                physicalTraits = "Tall, athletic, open and friendly face.",
                mentalTraits = "Idealistic, adventurous, honest to a fault, sometimes tactless.",
                luckyColors = listOf("Yellow", "Saffron", "Light Blue"),
                luckyDays = listOf("Thursday", "Sunday")
            )
            Rashi.MAKARA -> AscendantReport(
                lagnaName = "Capricorn (Makara)",
                rulingPlanet = "Saturn (Shani)",
                coreNature = "Disciplined, ambitious, and pragmatic. You climb the mountain of success slowly but steadily.",
                physicalTraits = "Bony structure, serious demeanor, ages gracefully.",
                mentalTraits = "Patient, responsible, structured, sometimes pessimistic.",
                luckyColors = listOf("Navy Blue", "Dark Brown", "Black"),
                luckyDays = listOf("Saturday", "Wednesday")
            )
            Rashi.KUMBHA -> AscendantReport(
                lagnaName = "Aquarius (Kumbha)",
                rulingPlanet = "Saturn (Shani) & Rahu",
                coreNature = "Innovative, humanitarian, and eccentric. You march to the beat of your own drum and envision the future.",
                physicalTraits = "Unique style, friendly yet detached aura.",
                mentalTraits = "Original, intellectual, progressive, sometimes rebellious.",
                luckyColors = listOf("Electric Blue", "Grey", "Violet"),
                luckyDays = listOf("Saturday", "Friday")
            )
            Rashi.MEENA -> AscendantReport(
                lagnaName = "Pisces (Meena)",
                rulingPlanet = "Jupiter (Guru)",
                coreNature = "Compassionate, mystical, and artistic. You are deeply connected to the spiritual and unseen realms.",
                physicalTraits = "Soft features, dreamy eyes, gentle aura.",
                mentalTraits = "Empathetic, imaginative, adaptable, sometimes escapist.",
                luckyColors = listOf("Sea Green", "Yellow", "Lavender"),
                luckyDays = listOf("Thursday", "Monday")
            )
        }
    }
}
