package com.example.engines.dincharya

import com.example.core.model.*
import com.example.core.model.dincharya.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object VedicDincharyaEngine {

    private val timeFmt = DateTimeFormatter.ofPattern("hh:mm a")

    /**
     * Generates a fully dynamic, personalized daily Vedic routine (Dincharya)
     * calculated from astronomical sunrise, sunset, solar noon, Muhurtas, and user's selected goal.
     */
    fun generateSchedule(
        dailyProfile: DailyVedicProfile,
        personalizedProfile: PersonalizedProfile?,
        birthProfile: BirthProfile?,
        selectedGoal: DincharyaGoal,
        completedIds: Set<String> = emptySet(),
        targetDate: LocalDate = dailyProfile.currentZonedDateTime.toLocalDate()
    ): DincharyaDailySchedule {
        val sunrise = dailyProfile.panchanga.sunrise
        val sunset = dailyProfile.panchanga.sunset
        val solarNoon = dailyProfile.panchanga.solarNoon
        val brahma = dailyProfile.brahmaMuhurta
        val abhijit = dailyProfile.abhijitMuhurta
        val rahu = dailyProfile.rahuKalam
        val yama = dailyProfile.yamaganda
        val gulika = dailyProfile.gulikaKalam
        val nakshatra = dailyProfile.panchanga.nakshatra

        // Determine ruling planetary deity of the day (Vara Lord)
        val dayOfWeek = targetDate.dayOfWeek
        val varaLord = when (dayOfWeek) {
            java.time.DayOfWeek.SUNDAY -> Graha.SURYA
            java.time.DayOfWeek.MONDAY -> Graha.CHANDRA
            java.time.DayOfWeek.TUESDAY -> Graha.MANGALA
            java.time.DayOfWeek.WEDNESDAY -> Graha.BUDHA
            java.time.DayOfWeek.THURSDAY -> Graha.GURU
            java.time.DayOfWeek.FRIDAY -> Graha.SHUKRA
            java.time.DayOfWeek.SATURDAY -> Graha.SHANI
        }

        val locationName = birthProfile?.birthLocation?.cityName 
            ?: birthProfile?.birthLocation?.talukaOrTown 
            ?: "Vedic Location"

        // Build activities list dynamically relative to sunrise/sunset
        val activities = mutableListOf<DincharyaActivity>()

        // 1. Brahma Muhurta Jagran (Awakening)
        val brahmaStart = brahma.startTime
        val brahmaEnd = brahma.endTime
        activities.add(
            DincharyaActivity(
                id = "act_brahma_muhurta",
                title = "Brahma Muhurta Awakening",
                sanskritTitle = "ब्राह्मे मुहूर्ते उत्तिष्ठेत्",
                category = DincharyaCategory.MEDITATION_DHYANA,
                phase = DincharyaTimePhase.BRAHMA_MUHURTA,
                startTime = brahmaStart,
                endTime = brahmaEnd,
                isTimeSensitive = true,
                muhurtaTag = "Brahma Muhurta (${brahmaStart.format(timeFmt)} - ${brahmaEnd.format(timeFmt)})",
                description = "Awakening during the 14th Muhurta before dawn when atmospheric Sattva and subtle prana are at peak purity.",
                instructions = listOf(
                    "Gently open your eyes and rub your palms together to generate warmth.",
                    "Recite Karadarshanam shloka looking at your palms.",
                    "Place your right foot down first on Mother Earth with Bhumi Vandana prayer.",
                    "Express gratitude for the gift of a fresh conscious breath."
                ),
                mantra = Mantra(
                    id = "mantra_karadarshanam",
                    sanskrit = "कराग्रे वसते लक्ष्मीः करमध्ये सरस्वती ।\nकरमूले तु गोविन्दः प्रभाते करदर्शनम् ॥",
                    transliteration = "Karāgre vasate Lakṣmīḥ karamadhye Sarasvatī |\nKaramūle tu Govindaḥ prabhāte karadarśanam ||",
                    language = "Sanskrit",
                    meaning = "At the tip of the hand resides Lakshmi, in the center Saraswati, and at the base Govinda. Thus look at your hands upon waking."
                ),
                ayurvedicPrinciple = "Waking in Vata time (before 6:00 AM) imparts lightness, mental clarity, and effortless bodily elimination before Kapha heaviness sets in.",
                goalBenefitTag = "Essential for deep spiritual receptivity and circadian harmony.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.SPIRITUAL_GROWTH || selectedGoal == DincharyaGoal.MENTAL_PEACE,
                isCompleted = completedIds.contains("act_brahma_muhurta")
            )
        )

        // 2. Ushapan (Dawn Water Elixir)
        val ushapanStart = brahmaEnd
        val ushapanEnd = brahmaEnd.plusMinutes(20)
        activities.add(
            DincharyaActivity(
                id = "act_ushapan",
                title = "Ushapan (Dawn Hydration)",
                sanskritTitle = "उषःपानम्",
                category = DincharyaCategory.PURIFICATION,
                phase = DincharyaTimePhase.PRATAH_SANDHYA,
                startTime = ushapanStart,
                endTime = ushapanEnd,
                isTimeSensitive = false,
                muhurtaTag = "Pre-Sunrise Cleanse",
                description = "Drinking room temperature or lukewarm water kept in a pure copper vessel overnight.",
                instructions = listOf(
                    "Drink 1 to 2 glasses of pure water while sitting calmly in Sukhasana.",
                    "Do not drink standing or in a rush.",
                    "Avoid ice-cold refrigerated water which suppresses the morning digestive fire (Agni)."
                ),
                mantra = null,
                ayurvedicPrinciple = "Cleanses the gastrointestinal tract, stimulates natural peristalsis, and flushes accumulated metabolic waste (Ama).",
                goalBenefitTag = "Ignites cellular detoxification and natural morning vitality.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.VITALITY_HEALTH || selectedGoal == DincharyaGoal.AYURVEDIC_BALANCE,
                isCompleted = completedIds.contains("act_ushapan")
            )
        )

        // 3. Saucha, Danta Dhavana & Jihwa Nirlekhana (Sensory Cleansing)
        val sauchaStart = ushapanEnd
        val sauchaEnd = sauchaStart.plusMinutes(25)
        activities.add(
            DincharyaActivity(
                id = "act_saucha_cleansing",
                title = "Saucha, Oral & Sensory Cleansing",
                sanskritTitle = "शौच, दन्तधावन एवं जिह्वा निर्लेखनम्",
                category = DincharyaCategory.PURIFICATION,
                phase = DincharyaTimePhase.PRATAH_SANDHYA,
                startTime = sauchaStart,
                endTime = sauchaEnd,
                isTimeSensitive = false,
                description = "Complete morning hygiene: bodily evacuation, tongue scraping, teeth cleaning, and eye splash.",
                instructions = listOf(
                    "Natural bowel and bladder evacuation without straining.",
                    "Scrape tongue gently from back to front 5-7 times with a copper/silver scraper.",
                    "Brush teeth with herbal powder or astringent toothpaste.",
                    "Splash eyes with cool rose water or clean water 7 times.",
                    "Apply 1-2 drops of warm sesame oil or Anu Taila into nostrils (Pratimarsa Nasya)."
                ),
                mantra = null,
                ayurvedicPrinciple = "Tongue scraping removes bacterial coating (Ama) and activates taste buds; Nasya protects the respiratory passage and sharpens the five senses.",
                goalBenefitTag = "Improves sensory sharpness and prevents morning sluggishness.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.AYURVEDIC_BALANCE,
                isCompleted = completedIds.contains("act_saucha_cleansing")
            )
        )

        // 4. Pratah Sandhya & Gayatri Meditation (Sunrise Twilight)
        val pratahSandhyaStart = sunrise.minusMinutes(24)
        val pratahSandhyaEnd = sunrise.plusMinutes(24)
        activities.add(
            DincharyaActivity(
                id = "act_pratah_sandhya",
                title = "Pratah Sandhya & Gayatri Dhyana",
                sanskritTitle = "प्रातः सन्ध्या वन्दनम् एवं गायत्री",
                category = DincharyaCategory.VEDIC_PRAYER,
                phase = DincharyaTimePhase.PRATAH_SANDHYA,
                startTime = pratahSandhyaStart,
                endTime = pratahSandhyaEnd,
                isTimeSensitive = true,
                muhurtaTag = "Sunrise Window (${sunrise.format(timeFmt)})",
                description = "Sacred dawn prayer at the transition juncture (Sandhya) between night and daylight.",
                instructions = listOf(
                    "Sit facing East towards the rising Sun in a calm meditative posture.",
                    "Offer mental or water Arghya to Lord Surya as the cosmic source of life and light.",
                    "Chant the Gayatri Mantra 24, 54, or 108 times with focused heart-center awareness.",
                    "Absorb the gentle golden rays (Balasurya) upon your forehead and chest."
                ),
                mantra = Mantra(
                    id = "mantra_gayatri_sandhya",
                    sanskrit = "ॐ भूर्भुवः स्वः तत्सवितुर्वरेण्यं\nभर्गो देवस्य धीमहि धियो यो नः प्रचोदयात् ॥",
                    transliteration = "Oṃ bhūr bhuvaḥ svaḥ tat savitur vareṇyaṃ\nbhargo devasya dhīmahi dhiyo yo naḥ pracodayāt ||",
                    language = "Sanskrit",
                    meaning = "We meditate on that glorious effulgence of the divine Sun, the Creator of all. May that divine light illuminate our intellect."
                ),
                ayurvedicPrinciple = "Dawn light regulates the pineal gland, synchronizes melatonin-serotonin cycles, and awakens Buddhi (higher discriminative intellect).",
                goalBenefitTag = "Supreme booster for spiritual elevation, calmness, and mental focus.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.SPIRITUAL_GROWTH || selectedGoal == DincharyaGoal.FOCUS_PRODUCTIVITY,
                isCompleted = completedIds.contains("act_pratah_sandhya")
            )
        )

        // 5. Surya Namaskar & Yoga Asanas
        val yogaStart = pratahSandhyaEnd
        val yogaEnd = yogaStart.plusMinutes(30)
        activities.add(
            DincharyaActivity(
                id = "act_surya_namaskar",
                title = "Surya Namaskar & Asana Flow",
                sanskritTitle = "सूर्य नमस्कार एवं योगाभ्यासः",
                category = DincharyaCategory.YOGA_PRANAYAMA,
                phase = DincharyaTimePhase.PRATAH_SANDHYA,
                startTime = yogaStart,
                endTime = yogaEnd,
                isTimeSensitive = false,
                description = "12 Classical Sun Salutations followed by spine-strengthening Asanas.",
                instructions = listOf(
                    "Perform 6 to 12 cycles of Surya Namaskar with synchronized breath.",
                    "Hold gentle spinal twists (Ardha Matsyendrasana) and heart-openers (Bhujangasana).",
                    "Maintain comfortable stillness (Sthira Sukham Asanam) without straining.",
                    "Rest for 3 minutes in Shavasana to integrate cosmic solar energy."
                ),
                mantra = Mantra(
                    id = "mantra_surya_bija",
                    sanskrit = "ॐ मित्राय नमः । ॐ रवये नमः । ॐ सूर्याय नमः ।\nॐ भानवे नमः । ॐ खगाय नमः । ॐ पूष्णे नमः ॥",
                    transliteration = "Oṃ Mitrāya Namaḥ | Oṃ Ravaye Namaḥ | Oṃ Sūryāya Namaḥ |\nOṃ Bhānave Namaḥ | Oṃ Khagāya Namaḥ | Oṃ Pūṣṇe Namaḥ ||",
                    language = "Sanskrit",
                    meaning = "Salutations to the Friend of all, the Radiant One, the Dispeller of darkness, the Illuminator, the Mover through skies, and the Nourisher."
                ),
                ayurvedicPrinciple = "Morning movement overcomes Kapha stagnation, enhances lymphatic flow, and fortifies muscle tissue (Mamsa Dhatu).",
                goalBenefitTag = "Builds physical stamina, flexibility, and radiant solar vitality.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.VITALITY_HEALTH,
                isCompleted = completedIds.contains("act_surya_namaskar")
            )
        )

        // 6. Pranayama & Nadi Shodhana (Harmonizing Breath)
        val pranayamaStart = yogaEnd
        val pranayamaEnd = pranayamaStart.plusMinutes(20)
        activities.add(
            DincharyaActivity(
                id = "act_pranayama",
                title = "Pranayama (Nadi Shodhana & Bhastrika)",
                sanskritTitle = "प्राणायाम एवं नाड़ी शोधन",
                category = DincharyaCategory.YOGA_PRANAYAMA,
                phase = DincharyaTimePhase.PURVAHNA,
                startTime = pranayamaStart,
                endTime = pranayamaEnd,
                isTimeSensitive = false,
                description = "Alternate nostril breathing and gentle energizing breaths to balance solar (Pingala) and lunar (Ida) channels.",
                instructions = listOf(
                    "Sit upright in Padmasana or Siddhasana with spine straight.",
                    "Kapalabhati (gentle passive inhales, active exhales) for 3 rounds of 30 strokes.",
                    "Nadi Shodhana (Alternate Nostril Breath) with 1:1 or 1:2 ratio for 10-15 cycles.",
                    "Conclude with 5 deep Bhramari (humming bee) exhalations to calm the brain."
                ),
                mantra = null,
                ayurvedicPrinciple = "Pranayama purifies the 72,000 Nadis, balances autonomic nervous system tone, and stabilizes Prana Vayu.",
                goalBenefitTag = "Instant relief from anxiety and overthinking; deep emotional balance.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.MENTAL_PEACE || selectedGoal == DincharyaGoal.FOCUS_PRODUCTIVITY,
                isCompleted = completedIds.contains("act_pranayama")
            )
        )

        // 7. Vara Deity & Planetary Japa (Today's Lord)
        val japaStart = pranayamaEnd
        val japaEnd = japaStart.plusMinutes(20)
        val planetaryMantra = getPlanetaryMantra(varaLord)
        activities.add(
            DincharyaActivity(
                id = "act_vara_japa",
                title = "${varaLord.displayName} Graha & Deity Japa",
                sanskritTitle = "${varaLord.sanskritName} ग्रह मन्त्र जपः",
                category = DincharyaCategory.MEDITATION_DHYANA,
                phase = DincharyaTimePhase.PURVAHNA,
                startTime = japaStart,
                endTime = japaEnd,
                isTimeSensitive = false,
                muhurtaTag = "Vara Lord: ${varaLord.displayName} (${varaLord.symbol})",
                description = "Personalized planetary alignment chanting corresponding to today's weekday ruler (${varaLord.displayName}) and transit star (${nakshatra.sanskritName}).",
                instructions = listOf(
                    "Hold your Tulsi, Rudraksha, or Sandalwood mala in the right hand.",
                    "Chant 108 repetitions with devotion and focused rhythm.",
                    "Dedicate the fruit of your chanting to universal peace (Loka Samasta)."
                ),
                mantra = planetaryMantra,
                ayurvedicPrinciple = "Sound resonance (Shabda Brahma) harmonizes cellular vibrations and aligns the subtle astrological astral body.",
                goalBenefitTag = "Mitigates planetary afflictions and amplifies auspicious cosmic grace.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.SPIRITUAL_GROWTH,
                isCompleted = completedIds.contains("act_vara_japa")
            )
        )

        // 8. Abhyanga & Snana (Sacred Bath)
        val snanaStart = japaEnd
        val snanaEnd = snanaStart.plusMinutes(25)
        activities.add(
            DincharyaActivity(
                id = "act_abhyanga_snana",
                title = "Abhyanga & Sattvic Bath",
                sanskritTitle = "अभ्यङ्ग एवं स्नानम्",
                category = DincharyaCategory.PURIFICATION,
                phase = DincharyaTimePhase.PURVAHNA,
                startTime = snanaStart,
                endTime = snanaEnd,
                isTimeSensitive = false,
                description = "Self-massage with warm herbal oil followed by a refreshing, grounding bath.",
                instructions = listOf(
                    "Warm a small amount of sesame or almond oil.",
                    "Apply in long strokes along limbs and circular strokes around joints.",
                    "Allow oil to absorb for 5-10 minutes.",
                    "Bathe with warm water, reciting sacred river names (Ganga, Yamuna, Godavari, Saraswati)."
                ),
                mantra = Mantra(
                    id = "mantra_snana_rivers",
                    sanskrit = "गङ्गे च यमुने चैव गोदावरि सरस्वति ।\nनर्मदे सिन्धु कावेरि जलेऽस्मिन् संनिधिं कुरु ॥",
                    transliteration = "Gaṅge ca Yamune caiva Godāvari Sarasvati |\nNarmade Sindhu Kāveri jale'smin saṃnidhiṃ kuru ||",
                    language = "Sanskrit",
                    meaning = "O Sacred Rivers Ganga, Yamuna, Godavari, Saraswati, Narmada, Sindhu, and Kaveri! Please sanctify this water with your divine presence."
                ),
                ayurvedicPrinciple = "Abhyanga pacifies Vata dosha, nourishes all seven Dhatus, promotes sound sleep later at night, and delays aging (Jara-hara).",
                goalBenefitTag = "Deeply grounding for the nervous system and skin rejuvenation.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.AYURVEDIC_BALANCE || selectedGoal == DincharyaGoal.VITALITY_HEALTH,
                isCompleted = completedIds.contains("act_abhyanga_snana")
            )
        )

        // 9. Pratah Ahara (Mindful Breakfast)
        val breakfastStart = snanaEnd
        val breakfastEnd = breakfastStart.plusMinutes(30)
        activities.add(
            DincharyaActivity(
                id = "act_pratah_ahara",
                title = "Sattvic Pratah Ahara (Breakfast)",
                sanskritTitle = "प्रातराहारः (सात्विक)",
                category = DincharyaCategory.AYURVEDA_NUTRITION,
                phase = DincharyaTimePhase.PURVAHNA,
                startTime = breakfastStart,
                endTime = breakfastEnd,
                isTimeSensitive = false,
                description = "Warm, easily digestible breakfast supporting the morning transition into daytime activity.",
                instructions = listOf(
                    "Sit in a peaceful environment without screens or disturbing news.",
                    "Express gratitude before eating.",
                    "Chew thoroughly and eat until 70% satisfied."
                ),
                mantra = null,
                ayurvedicPrinciple = "Morning Agni is moderate; consume warm, light foods like soaked almonds, warm oats, or stewed apples.",
                goalBenefitTag = "Sustained clean energy without morning food coma.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.AYURVEDIC_BALANCE,
                isCompleted = completedIds.contains("act_pratah_ahara")
            )
        )

        // 10. Purvahna Work & Auspicious Choghadiya Blocks
        val workStart = breakfastEnd
        val workEnd = solarNoon.minusMinutes(45)
        activities.add(
            DincharyaActivity(
                id = "act_purvahna_work",
                title = "Mindful Work & Auspicious Actions",
                sanskritTitle = "पूर्वाह्ण कर्म एवं शुभ समय",
                category = DincharyaCategory.MINDFUL_WORK,
                phase = DincharyaTimePhase.PURVAHNA,
                startTime = workStart,
                endTime = workEnd,
                isTimeSensitive = false,
                muhurtaTag = "Morning Peak Focus",
                description = "Dedicated deep work, professional endeavors, and creative creation aligned with favorable daytime Choghadiyas.",
                instructions = listOf(
                    "Tackle high-priority intellectual tasks while mind is calm and fresh.",
                    "Take 2-minute breath breaks every 45 minutes.",
                    "Practice Karma Yoga—focus on dedicated action without anxiety about immediate results."
                ),
                mantra = null,
                ayurvedicPrinciple = "Mid-morning (8:30 AM to 11:30 AM) is prime time for analytical clarity and productive execution.",
                goalBenefitTag = "Peak productivity and mental fortitude.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.FOCUS_PRODUCTIVITY,
                isCompleted = completedIds.contains("act_purvahna_work")
            )
        )

        // 11. Madhyahna Sandhya & Abhijit Muhurta Window
        val abhijitStart = abhijit?.startTime ?: solarNoon.minusMinutes(24)
        val abhijitEnd = abhijit?.endTime ?: solarNoon.plusMinutes(24)
        activities.add(
            DincharyaActivity(
                id = "act_abhijit_madhyahna",
                title = "Abhijit Muhurta & Madhyahna Reset",
                sanskritTitle = "अभिजित् मुहूर्त एवं मध्याह्न सन्ध्या",
                category = DincharyaCategory.VEDIC_PRAYER,
                phase = DincharyaTimePhase.MADHYAHNA,
                startTime = abhijitStart,
                endTime = abhijitEnd,
                isTimeSensitive = true,
                muhurtaTag = "Abhijit Muhurta (${abhijitStart.format(timeFmt)} - ${abhijitEnd.format(timeFmt)})",
                description = "The 8th Muhurta of the day centered on solar zenith, endowed with the special power to neutralize negative planetary doshas.",
                instructions = listOf(
                    "Pause worldly hustle for 5 minutes during this victorious window.",
                    "Recite a quiet prayer or positive affirmation for success in your noble goals.",
                    "Ideal window for important phone calls, inaugurations, or key decision-making."
                ),
                mantra = Mantra(
                    id = "mantra_madhyahna_surya",
                    sanskrit = "ॐ सूर्याय नमः । ॐ नमो भगवते वासुदेवाय ॥",
                    transliteration = "Oṃ Sūryāya Namaḥ | Oṃ Namo Bhagavate Vāsudevāya ||",
                    language = "Sanskrit",
                    meaning = "Salutations to Surya at zenith and Lord Vasudeva, the sustaining guardian of the cosmos."
                ),
                ayurvedicPrinciple = "Solar zenith aligns with maximum external and internal heat; brief pause balances the rising Pitta dosha.",
                goalBenefitTag = "Cosmic auspiciousness and obstacle removal for major endeavors.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.FOCUS_PRODUCTIVITY || selectedGoal == DincharyaGoal.SPIRITUAL_GROWTH,
                isCompleted = completedIds.contains("act_abhijit_madhyahna")
            )
        )

        // 12. Mukhya Madhyahna Bhojana (Main Lunch - Mitahara)
        val lunchStart = abhijitEnd
        val lunchEnd = lunchStart.plusMinutes(40)
        activities.add(
            DincharyaActivity(
                id = "act_madhyahna_lunch",
                title = "Mukhya Bhojana (Main Midday Meal)",
                sanskritTitle = "मध्याह्न मुख्य भोजनम् (मिताहार)",
                category = DincharyaCategory.AYURVEDA_NUTRITION,
                phase = DincharyaTimePhase.MADHYAHNA,
                startTime = lunchStart,
                endTime = lunchEnd,
                isTimeSensitive = false,
                description = "The primary and most substantial meal of the day, consumed when digestive fire (Jatharagni) is at absolute peak.",
                instructions = listOf(
                    "Follow the golden Vedic rule of Mitahara: 50% stomach solid food, 25% water/liquids, 25% empty for free Prana churning.",
                    "Include all 6 tastes (Shad Rasa): Sweet, Sour, Salty, Pungent, Bitter, and Astringent.",
                    "Sip warm water or fresh spiced buttermilk (Takra with cumin and ginger).",
                    "Do not drink iced water with meals."
                ),
                mantra = Mantra(
                    id = "mantra_annapurna_meal",
                    sanskrit = "अन्नपूर्णे सदा पूर्णे शङ्करप्राणवल्लभे ।\nज्ञानवैराग्यसिद्ध्यर्थं भिक्षां देहि च पार्वति ॥",
                    transliteration = "Annapūrṇe sadā pūrṇe Śaṅkaraprāṇavallabhe |\nJñānavairāgyasiddhyarthaṃ bhikṣāṃ dehi ca Pārvati ||",
                    language = "Sanskrit",
                    meaning = "O Mother Annapurna, always full and beloved of Shankara, bestow upon us the food of wisdom, detachment, and nourishment."
                ),
                ayurvedicPrinciple = "Digestive enzymes peak when the Sun is at peak elevation. Food eaten now is properly converted into pure Ojas rather than toxic Ama.",
                goalBenefitTag = "Optimal metabolic health, strength, and longevity.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.AYURVEDIC_BALANCE || selectedGoal == DincharyaGoal.VITALITY_HEALTH,
                isCompleted = completedIds.contains("act_madhyahna_lunch")
            )
        )

        // 13. Shatapadi & Vamkukshi (Gentle Stroll & Rest)
        val shatapadiStart = lunchEnd
        val shatapadiEnd = shatapadiStart.plusMinutes(20)
        activities.add(
            DincharyaActivity(
                id = "act_shatapadi",
                title = "Shatapadi & Vamkukshi (Gentle Walking)",
                sanskritTitle = "शतपदी एवं वामकुक्षि",
                category = DincharyaCategory.PURIFICATION,
                phase = DincharyaTimePhase.MADHYAHNA,
                startTime = shatapadiStart,
                endTime = shatapadiEnd,
                isTimeSensitive = false,
                description = "Walking 100 gentle steps followed by resting on the left side (Vamkukshi) for 10-15 minutes.",
                instructions = listOf(
                    "Take 100 slow, unhurried steps indoors or in fresh air.",
                    "Rest on your left lateral side for 10-15 minutes to facilitate stomach curvature digestion.",
                    "Avoid deep sleeping during daytime (Divasvapna), which aggravates Kapha and Pitta."
                ),
                mantra = null,
                ayurvedicPrinciple = "Lying on the left side opens the right nostril (Surya Nadi / Pingala), elevating internal thermal digestion.",
                goalBenefitTag = "Eliminates post-lunch sluggishness and indigestion.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.AYURVEDIC_BALANCE,
                isCompleted = completedIds.contains("act_shatapadi")
            )
        )

        // 14. Aparahna Productivity & Rahu Kalam Advisory
        val aparahnaStart = shatapadiEnd
        val aparahnaEnd = sunset.minusMinutes(45)
        val isRahuAfternoon = rahu.startTime.isAfter(lunchEnd) && rahu.startTime.isBefore(sunset)
        activities.add(
            DincharyaActivity(
                id = "act_aparahna_focus",
                title = "Aparahna Afternoon Flow & Cosmic Caution",
                sanskritTitle = "अपराह्ण कर्म एवं राहुकाल ध्यान",
                category = DincharyaCategory.MINDFUL_WORK,
                phase = DincharyaTimePhase.APARAHNA,
                startTime = aparahnaStart,
                endTime = aparahnaEnd,
                isTimeSensitive = false,
                muhurtaTag = "Rahu Kaal: ${rahu.startTime.format(timeFmt)} - ${rahu.endTime.format(timeFmt)}",
                description = "Mindful execution of daily tasks with awareness of afternoon Rahu Kalam and Yamaganda time-windows.",
                instructions = listOf(
                    "During Rahu Kaal (${rahu.startTime.format(timeFmt)} to ${rahu.endTime.format(timeFmt)}), avoid signing new contracts or commencing journeys.",
                    "Use Rahu Kaal for routine desk tasks, organizing, or inward quiet mantra.",
                    "Stay hydrated with warm water, herbal infusion, or coconut water."
                ),
                mantra = null,
                ayurvedicPrinciple = "Afternoon (2:00 PM - 6:00 PM) is dominated by Vata dosha; keep environment warm, grounded, and avoid irregular snacking.",
                goalBenefitTag = "Protects against astrological friction and sustains steady afternoon stamina.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.FOCUS_PRODUCTIVITY,
                isCompleted = completedIds.contains("act_aparahna_focus"),
                cautionNote = "Rahu Kaal window: ${rahu.startTime.format(timeFmt)} - ${rahu.endTime.format(timeFmt)}. Favor inward focus over external risk-taking."
            )
        )

        // 15. Sayam Sandhya & Evening Twilight Dhyana
        val sayamStart = sunset.minusMinutes(24)
        val sayamEnd = sunset.plusMinutes(24)
        activities.add(
            DincharyaActivity(
                id = "act_sayam_sandhya",
                title = "Sayam Sandhya, Deepam & Twilight Dhyana",
                sanskritTitle = "सायं सन्ध्या, दीप प्रज्वलन एवं ध्यानम्",
                category = DincharyaCategory.VEDIC_PRAYER,
                phase = DincharyaTimePhase.SAYAM_SANDHYA,
                startTime = sayamStart,
                endTime = sayamEnd,
                isTimeSensitive = true,
                muhurtaTag = "Sunset Window (${sunset.format(timeFmt)})",
                description = "The evening Sandhya transition from daylight to twilight. A sacred threshold for lighting the house lamp and meditating.",
                instructions = listOf(
                    "Light a pure cow ghee or sesame oil lamp (Deepam) at your altar or Northeast zone.",
                    "Wash hands, feet, and face.",
                    "Sit facing North or East for 10-15 minutes of quiet Japa or Om chanting.",
                    "Reflect peacefully on the day with gratitude and surrender."
                ),
                mantra = Mantra(
                    id = "mantra_deepa_jyoti",
                    sanskrit = "शुभं करोति कल्याणमारोग्यं धनसंपदः ।\nशत्रुबुद्धिविनाशाय दीपज्योतिर्नमोऽस्तु ते ॥",
                    transliteration = "Śubhaṃ karoti kalyāṇam ārogyaṃ dhanasaṃpadaḥ |\nŚatrubuddhivināśāya dīpajyotir namo'stu te ||",
                    language = "Sanskrit",
                    meaning = "Salutations to the light of the lamp that brings auspiciousness, health, prosperity, and destroys negative tendencies."
                ),
                ayurvedicPrinciple = "As dusk falls, Kapha and Vata transition; twilight meditation dissolves mental stress accumulated across the day.",
                goalBenefitTag = "Profound mental relaxation and spiritual sanctification of the living space.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.SPIRITUAL_GROWTH || selectedGoal == DincharyaGoal.MENTAL_PEACE,
                isCompleted = completedIds.contains("act_sayam_sandhya")
            )
        )

        // 16. Laghu Sandhya Bhojana (Light Early Dinner)
        val dinnerStart = sayamEnd.plusMinutes(15)
        val dinnerEnd = dinnerStart.plusMinutes(35)
        activities.add(
            DincharyaActivity(
                id = "act_laghu_dinner",
                title = "Laghu Bhojana (Light Early Dinner)",
                sanskritTitle = "लघु सन्ध्या भोजनम्",
                category = DincharyaCategory.AYURVEDA_NUTRITION,
                phase = DincharyaTimePhase.PRADOSHA,
                startTime = dinnerStart,
                endTime = dinnerEnd,
                isTimeSensitive = false,
                description = "A warm, light, and easily digestible dinner eaten at least 2.5 to 3 hours before sleep.",
                instructions = listOf(
                    "Consume warm vegetable soup, moong dal khichdi, or light steamed dishes.",
                    "Avoid heavy fried food, curd/yogurt at night, raw salads, and excess sweets.",
                    "Eat in a quiet peaceful atmosphere before 7:30 PM."
                ),
                mantra = null,
                ayurvedicPrinciple = "Digestive Agni wanes with the setting sun. Heavy meals at night lead to undigested toxins (Ama) and disturbed sleep.",
                goalBenefitTag = "Prevents morning sluggishness, acid reflux, and heavy toxins.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.AYURVEDIC_BALANCE,
                isCompleted = completedIds.contains("act_laghu_dinner")
            )
        )

        // 17. Svadhyaya & Digital Detox (Evening Satsang)
        val svadhyayaStart = dinnerEnd.plusMinutes(20)
        val svadhyayaEnd = svadhyayaStart.plusMinutes(45)
        activities.add(
            DincharyaActivity(
                id = "act_svadhyaya",
                title = "Svadhyaya (Sacred Study) & Digital Detox",
                sanskritTitle = "स्वाध्याय एवं सत्सङ्ग",
                category = DincharyaCategory.MEDITATION_DHYANA,
                phase = DincharyaTimePhase.PRADOSHA,
                startTime = svadhyayaStart,
                endTime = svadhyayaEnd,
                isTimeSensitive = false,
                description = "Reading elevating scriptures, spiritual contemplation, gentle conversation, and disconnecting from digital screens.",
                instructions = listOf(
                    "Turn off phones, laptops, and bright blue-light screens at least 1 hour before bed.",
                    "Spend 20 minutes reading Gita, Upanishads, or uplifting wisdom literature.",
                    "Spend quality, calm time with family members."
                ),
                mantra = null,
                ayurvedicPrinciple = "Absence of artificial blue light stimulates natural melatonin production and pacifies Sadhaka Pitta.",
                goalBenefitTag = "Calms nervous agitation and fosters deep mental peace.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.MENTAL_PEACE || selectedGoal == DincharyaGoal.SPIRITUAL_GROWTH,
                isCompleted = completedIds.contains("act_svadhyaya")
            )
        )

        // 18. Pada Abhyanga & Ratricharya (Rejuvenating Bedtime)
        val bedtimeStart = svadhyayaEnd.plusMinutes(15)
        val bedtimeEnd = bedtimeStart.plusMinutes(30)
        activities.add(
            DincharyaActivity(
                id = "act_ratricharya_sleep",
                title = "Pada Abhyanga, Golden Milk & Nidra",
                sanskritTitle = "पाद अभ्यङ्ग, सुवर्ण दुग्ध एवं सुख निद्रा",
                category = DincharyaCategory.BEDTIME_REST,
                phase = DincharyaTimePhase.RATRI_CHARYA,
                startTime = bedtimeStart,
                endTime = bedtimeEnd,
                isTimeSensitive = false,
                description = "Foot massage with warm ghee, sipping warm spiced milk, bedtime gratitude, and sleeping with head oriented South or East.",
                instructions = listOf(
                    "Massage the soles of your feet (Kansa bowl or hands) with warm cow ghee or sesame oil for 3 minutes.",
                    "Sip a small cup of warm milk prepared with a pinch of turmeric, nutmeg, and cardamom.",
                    "Lie down and practice 5 minutes of So-Hum breath awareness.",
                    "Orient your head towards the South (for deep rejuvenation) or East (for spiritual clarity); avoid North orientation.",
                    "Aim to be asleep by 10:00 PM - 10:30 PM (before Pitta nocturnal surge at 10 PM)."
                ),
                mantra = Mantra(
                    id = "mantra_ratricharya_nidra",
                    sanskrit = "या देवी सर्वभूतेषु निद्रारूपेण संस्थिता ।\nनमस्तस्यै नमस्तस्यै नमस्तस्यै नमो नमः ॥",
                    transliteration = "Yā devī sarvabhūteṣu nidrārūpeṇa saṃsthitā |\nNamastasyai namastasyai namastasyai namo namaḥ ||",
                    language = "Sanskrit",
                    meaning = "Salutations again and again to that Divine Mother who abides in all living beings in the sacred form of restful sleep."
                ),
                ayurvedicPrinciple = "Sleeping by 10 PM allows Pitta time (10 PM to 2 AM) to focus on cellular healing and liver detoxification rather than late-night digestion.",
                goalBenefitTag = "Deep REM & deep slow-wave restorative sleep, Ojas nourishment.",
                isRecommendedForGoal = selectedGoal == DincharyaGoal.MENTAL_PEACE || selectedGoal == DincharyaGoal.VITALITY_HEALTH || selectedGoal == DincharyaGoal.AYURVEDIC_BALANCE,
                isCompleted = completedIds.contains("act_ratricharya_sleep")
            )
        )

        // Calculate Ayurvedic meal schedule
        val meals = listOf(
            DincharyaMealRecommendation(
                mealType = "Ushapan (Dawn Elixir)",
                sanskritName = "उषःपानम्",
                suggestedTime = brahmaEnd.format(timeFmt),
                principle = "Flushes Ama, stimulates digestion and kidney filtration.",
                recommendedFoods = listOf("Warm copper-vessel water", "Lukewarm water with pinch of dry ginger", "Warm water with tulsi leaf"),
                avoidFoods = listOf("Ice water", "Cold milk", "Caffeine on empty stomach"),
                agniStatus = "Waking / Cleansing"
            ),
            DincharyaMealRecommendation(
                mealType = "Pratah Ahara (Morning Breakfast)",
                sanskritName = "प्रातराहारः",
                suggestedTime = breakfastStart.format(timeFmt),
                principle = "Light Sattvic fuel to transition out of morning Kapha heaviness.",
                recommendedFoods = listOf("Warm spiced oatmeal or porridge", "Steamed apples with cinnamon", "Soaked peeled almonds & walnuts", "Moong dal chilla with mint chutney"),
                avoidFoods = listOf("Heavy fried parathas", "Stale leftovers", "Excess cheese or cold yogurt"),
                agniStatus = "Moderate (Manda-to-Madhyama)"
            ),
            DincharyaMealRecommendation(
                mealType = "Madhyahna Bhojana (Main Midday Lunch)",
                sanskritName = "मध्याह्न मुख्य भोजनम्",
                suggestedTime = lunchStart.format(timeFmt),
                principle = "Largest meal of the day; matches peak solar heat and maximum Jatharagni.",
                recommendedFoods = listOf("Warm fresh rice / whole wheat roti with ghee", "Moong / Toor dal with cumin tadka", "Seasonal vegetables (Gourd, Pumpkin, Spinach)", "Spiced fresh Takra (buttermilk) with roasted jeera"),
                avoidFoods = listOf("Cold beverages with meals", "Incompatible food combinations (Viruddha Ahara e.g. fruit with milk)"),
                agniStatus = "Peak Solar Fire (Tikshna Agni)"
            ),
            DincharyaMealRecommendation(
                mealType = "Laghu Sandhya Bhojana (Light Early Dinner)",
                sanskritName = "लघु सन्ध्या भोजनम्",
                suggestedTime = dinnerStart.format(timeFmt),
                principle = "Light and early to allow complete digestion before sleep.",
                recommendedFoods = listOf("Moong dal & rice Khichdi with ghee", "Clear vegetable broth or bottle gourd soup", "Steamed seasonal vegetables", "Light millet porridge"),
                avoidFoods = listOf("Curd / Yogurt at night", "Heavy beans / Rajma / Chana", "Deep-fried sweets", "Late night eating post 8:00 PM"),
                agniStatus = "Subdued (Manda Agni)"
            ),
            DincharyaMealRecommendation(
                mealType = "Nidra Ahara (Bedtime Restorative Drink)",
                sanskritName = "रात्रि दुग्धम्",
                suggestedTime = bedtimeStart.format(timeFmt),
                principle = "Nourishes Ojas, grounds Vata, and induces deep rejuvenating sleep.",
                recommendedFoods = listOf("Warm A2 cow milk with pinch of nutmeg and turmeric", "Warm almond milk with cardamom", "Chamomile or brahmi herbal infusion"),
                avoidFoods = listOf("Black tea", "Coffee", "Carbonated drinks", "Cold liquids"),
                agniStatus = "Resting / Cellular Repair"
            )
        )

        val totalCount = activities.size
        val completedCount = activities.count { it.isCompleted }
        val completionPercentage = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

        return DincharyaDailySchedule(
            date = targetDate,
            locationName = locationName,
            selectedGoal = selectedGoal,
            rulingVaraGraha = varaLord,
            nakshatra = nakshatra,
            sunrise = sunrise,
            sunset = sunset,
            solarNoon = solarNoon,
            brahmaMuhurta = brahma,
            abhijitMuhurta = abhijit,
            rahuKalam = rahu,
            yamaganda = yama,
            gulikaKalam = gulika,
            activities = activities,
            meals = meals,
            completionPercentage = completionPercentage,
            completedCount = completedCount,
            totalCount = totalCount
        )
    }

    private fun getPlanetaryMantra(graha: Graha): Mantra {
        return when (graha) {
            Graha.SURYA -> Mantra(
                id = "mantra_graha_surya",
                sanskrit = "ॐ ह्रां ह्रीं ह्रौं सः सूर्याय नमः ॥",
                transliteration = "Oṃ hrāṃ hrīṃ hrauṃ saḥ Sūryāya namaḥ ||",
                language = "Sanskrit",
                meaning = "Bija mantra salutation to Lord Surya, the cosmic illuminator and soul of the universe."
            )
            Graha.CHANDRA -> Mantra(
                id = "mantra_graha_chandra",
                sanskrit = "ॐ श्रां श्रीं श्रौं सः चन्द्रमसे नमः ॥",
                transliteration = "Oṃ śrāṃ śrīṃ śrauṃ saḥ Candramase namaḥ ||",
                language = "Sanskrit",
                meaning = "Bija mantra salutation to Chandra, the ruler of mind, emotions, and subtle waters."
            )
            Graha.MANGALA -> Mantra(
                id = "mantra_graha_mangala",
                sanskrit = "ॐ क्रां क्रीं क्रौं सः भौमाय नमः ॥",
                transliteration = "Oṃ krāṃ krīṃ krauṃ saḥ Bhaumāya namaḥ ||",
                language = "Sanskrit",
                meaning = "Bija mantra salutation to Mangala (Mars), bestower of courage, energy, and auspicious drive."
            )
            Graha.BUDHA -> Mantra(
                id = "mantra_graha_budha",
                sanskrit = "ॐ ब्रां ब्रीं ब्रौं सः बुधाय नमः ॥",
                transliteration = "Oṃ brāṃ brīṃ brauṃ saḥ Budhāya namaḥ ||",
                language = "Sanskrit",
                meaning = "Bija mantra salutation to Budha (Mercury), master of speech, intellect, commerce, and discernment."
            )
            Graha.GURU -> Mantra(
                id = "mantra_graha_guru",
                sanskrit = "ॐ ग्रां ग्रीं ग्रौं सः गुरवे नमः ॥",
                transliteration = "Oṃ grāṃ grīṃ grauṃ saḥ Gurave namaḥ ||",
                language = "Sanskrit",
                meaning = "Bija mantra salutation to Guru Brihaspati, preceptor of gods, embodiment of wisdom, dharma, and expansion."
            )
            Graha.SHUKRA -> Mantra(
                id = "mantra_graha_shukra",
                sanskrit = "ॐ द्रां द्रीं द्रौं सः शुक्राय नमः ॥",
                transliteration = "Oṃ drāṃ drīṃ drauṃ saḥ Śukrāya namaḥ ||",
                language = "Sanskrit",
                meaning = "Bija mantra salutation to Shukra (Venus), lord of harmony, devotion, art, rejuvenation, and beauty."
            )
            Graha.SHANI -> Mantra(
                id = "mantra_graha_shani",
                sanskrit = "ॐ प्रां प्रीं प्रौं सः शनैश्चराय नमः ॥",
                transliteration = "Oṃ prāṃ prīṃ prauṃ saḥ Śanaiścarāya namaḥ ||",
                language = "Sanskrit",
                meaning = "Bija mantra salutation to Shani (Saturn), dispenser of karma, teacher of discipline, patience, and humility."
            )
            Graha.RAHU -> Mantra(
                id = "mantra_graha_rahu",
                sanskrit = "ॐ भ्रां भ्रीं भ्रौं सः राहवे नमः ॥",
                transliteration = "Oṃ bhrāṃ bhrīṃ bhrauṃ saḥ Rāhave namaḥ ||",
                language = "Sanskrit",
                meaning = "Bija mantra salutation to Rahu, transforming ambition into spiritual awakening."
            )
            Graha.KETU -> Mantra(
                id = "mantra_graha_ketu",
                sanskrit = "ॐ स्रां स्रीं स्रौं सः केतवे नमः ॥",
                transliteration = "Oṃ srāṃ srīṃ srauṃ saḥ Ketave namaḥ ||",
                language = "Sanskrit",
                meaning = "Bija mantra salutation to Ketu, the flag of spiritual detachment, intuition, and moksha."
            )
        }
    }
}
