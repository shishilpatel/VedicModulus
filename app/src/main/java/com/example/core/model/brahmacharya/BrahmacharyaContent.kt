package com.example.core.model.brahmacharya

/**
 * Educational pillars, practical exercises, and daily routines for Brahmacharya.
 * Strictly educational and spiritual discipline inspired by traditional Vedic philosophy.
 */
object BrahmacharyaContent {

    val PILLARS = listOf(
        BrahmacharyaPillar(
            id = "meaning_purpose",
            title = "Meaning & Purpose of Brahmacharya",
            sanskritTitle = "ब्रह्मचर्य स्वरूप एवं उद्देश्य",
            subtitle = "Walking in Divine Consciousness & Pranic Conservation",
            summary = "Etymologically, 'Brahma' (Supreme Cosmic Reality) + 'Charya' (Conduct/Walking). Brahmacharya means aligning one's life, thoughts, and actions with higher divinity.",
            detailedText = """
                In Vedic and Yogic philosophy, Brahmacharya is not mere physical suppression; it is the conscious redirection and elevation of vital energy (Prana) into spiritual radiance (Ojas) and intellectual sharpness (Tejas).
                
                Energy in the human body can flow in two directions:
                1. Adhogati (Downward flow): Dissipation through compulsive desires, indulgence, and unchecked sensory stimulation.
                2. Urdhvagati (Upward flow): Transmutation through meditation, Pranayama, pure diet, and selfless action towards higher mental and spiritual centers.
                
                By preserving physical and mental vitality, the mind gains extraordinary resilience, emotional composure, and inner serenity.
            """.trimIndent(),
            keyPractices = listOf(
                "Recognize all creative energy as sacred divine power (Shakti).",
                "Cultivate Urdhvaretas (upward transmutation of prana) through daily breathwork.",
                "Replace lower sensory cravings with higher intellectual and spiritual contemplation.",
                "Practice conscious presence and witness consciousness (Sakshi Bhava)."
            ),
            scripturalQuote = "ब्रह्मचर्येण तपसा देवा मृत्युमपाघ्नत ।",
            quoteSource = "Atharva Veda (11.5.19) — 'Through Brahmacharya and dedicated tapas, even divine beings overcome mortal decay.'",
            audienceNotes = mapOf(
                AudienceCategory.STUDENT to "For students, Brahmacharya builds unshakeable memory retention (Smriti), removes brain fog, and keeps the intellect laser-sharp for learning.",
                AudienceCategory.UNMARRIED to "For spiritual and career aspirants, it grants vitality to achieve ambitious life goals, develop self-mastery, and avoid emotional derailment.",
                AudienceCategory.MARRIED to "For Grihasthas, it fosters deep soul-level connection, mutual respect, peaceful home vibration, and dharmic family harmony."
            )
        ),
        BrahmacharyaPillar(
            id = "sense_control",
            title = "Self-Discipline & Sense Control",
            sanskritTitle = "इन्द्रिय निग्रह एवं संयम",
            subtitle = "Mastering the Senses (Indriya Nigraha) & Mental Gatekeeping",
            summary = "The mind wanders wherever unchecked senses wander. Controlling the sensory intake (Tanmatras) is the foundation of inner peace.",
            detailedText = """
                The Katha Upanishad compares the human being to a chariot:
                • The Atman (Soul) is the Master of the chariot.
                • The Buddhi (Intellect) is the Charioteer.
                • The Manas (Mind) is the Reins.
                • The Indriyas (Senses) are the wild Horses.
                • The Vishayas (Sensory Objects) are the Paths.
                
                When the intellect holds the reins firmly, the senses obey without turbulence. If the senses are left unguarded, they pull the chariot into restless agitation.
                
                Practical Sense Diet (Tanmatra Shuddhi):
                1. Eyes (Rupa): Conscious visual diet—avoid agitating screens, provocative imagery, and doom-scrolling.
                2. Ears (Shabda): Listen to elevating Vedic chants, calm music, and noble discussions.
                3. Tongue (Rasa): Practice Mitahara (moderation in food) with pure, non-stimulating ingredients.
            """.trimIndent(),
            keyPractices = listOf(
                "Digital Fasting: Keep devices away 1 hour before sleep and 1 hour after waking.",
                "Eye Restraint (Drishti Sanyama): Lower your gaze when in stimulating environments.",
                "Speech Moderation (Vak Tapas): Speak only what is truthful, kind, and necessary.",
                "Pause-and-Breathe: Whenever a craving arises, take 5 slow belly breaths before responding."
            ),
            scripturalQuote = "यदा संहरते चायं कूर्मोऽङ्गानीव सर्वशः । इन्द्रियाणीन्द्रियार्थेभ्यस्तस्य प्रज्ञा प्रतिष्ठिता ॥",
            quoteSource = "Bhagavad Gita (2.58) — 'When one withdraws the senses from sense objects, as a tortoise draws its limbs into its shell, wisdom is firmly established.'",
            audienceNotes = mapOf(
                AudienceCategory.STUDENT to "Avoid social media bingeing during exam periods; study in a clean, distraction-free environment.",
                AudienceCategory.UNMARRIED to "Audit your digital feeds, unsubscribe from provocative channels, and cultivate noble friendships (Satsang).",
                AudienceCategory.MARRIED to "Guard against comparison and external glamour by focusing love and presence entirely on your spouse."
            )
        ),
        BrahmacharyaPillar(
            id = "managing_distractions",
            title = "Managing Distractions & Urges",
            sanskritTitle = "काम विकार शमन एवं वेग निवारण",
            subtitle = "The Urge Surfing Technique & Overcoming Harmful Habits",
            summary = "An urge is simply a wave of energy in the nervous system. You do not have to fight it with panic; you can ride it with conscious awareness.",
            detailedText = """
                When a compulsive urge strikes, dopamine circuits create an illusion of immediate urgency. Yogic psychology provides the 'Urge Surfing' technique:
                
                Step 1: Witness without Judgment (Sakshi).
                Acknowledge: 'A thought or physical sensation has arisen in my body. I am the observer, not the urge.'
                
                Step 2: Thermal and Physical Reset.
                Splash cold water on your face, eyes, and wrists. This triggers the mammalian dive reflex, immediately slowing heart rate and resetting sympathetic excitement.
                
                Step 3: Cool the Nervous System.
                Perform Sheetali or Sitkari Pranayama (inhaling through folded tongue, exhaling through nose) to reduce physiological heat.
                
                Step 4: Shift Physical Space.
                Immediately leave the room, stand up, take a brisk walk, or do 15 gentle Surya Namaskars. Never remain lying down in isolated spaces when agitated.
            """.trimIndent(),
            keyPractices = listOf(
                "Use the 2-Minute Cold Splash Emergency Rule whenever triggered.",
                "Practice Sheetali Pranayama (10 rounds) to dissipate somatic heat.",
                "Chant Maha Mrityunjaya Mantra 11 times with focused attention on the third eye.",
                "Change environment immediately—go into open fresh air or among family/peers."
            ),
            scripturalQuote = "कामात् क्रोधोऽभिजायते । क्रोधाद्भवति संमोहः संमोहात्स्मृतिविभ्रमः ॥",
            quoteSource = "Bhagavad Gita (2.62-63) — 'From brooding on sense objects comes attachment, from attachment arises desire, from thwarted desire comes anger, and from anger arises delusion.'",
            audienceNotes = mapOf(
                AudienceCategory.STUDENT to "When study fatigue hits, avoid reaching for the phone; do 10 deep breaths or stretch instead.",
                AudienceCategory.UNMARRIED to "Keep nighttime screen usage strictly zero; charge phone across the room away from the bed.",
                AudienceCategory.MARRIED to "Channel intense emotional energies into creative collaborative projects, exercise, and spiritual study."
            )
        ),
        BrahmacharyaPillar(
            id = "lifestyle_and_diet",
            title = "Mind, Body & Lifestyle Discipline",
            sanskritTitle = "सात्त्विक आहार एवं दिनचर्या",
            subtitle = "Sattvic Nutrition, Mitahara & Circadian Alignment",
            summary = "What we eat becomes our mind (Annamaya Kosha shapes Manomaya Kosha). Pure, light, and digestible food keeps vital energies calm.",
            detailedText = """
                Dietary Principles for Brahmacharya (Sattvic Ahara):
                • Sattvic Foods: Fresh fruits, seasonal vegetables, whole grains (millet, oats, brown rice), almonds, soaked walnuts, pure milk/ghee (in moderation), herbal teas, tulsi, and honey.
                • Foods to Minimize (Rajasic & Tamasic): Excessively spicy, deeply fried, stale/reheated food, excessive garlic/onions, processed sugary treats, and high-caffeine energy drinks.
                • Mitahara (Eating to Half Full): Fill half the stomach with wholesome food, one quarter with water, and leave one quarter empty for digestion and prana circulation.
                
                Sleep & Circadian Hygiene:
                • Sleep by 10:00 PM to optimize the rejuvenating Pitta metabolic window.
                • Wake at Brahma Muhurta (approx. 96 to 48 minutes before sunrise) when atmospheric Sattva Guna is at its peak.
                • Sleep on a firm mattress and avoid sleeping on the stomach (prefer left side / Vamkukshi).
            """.trimIndent(),
            keyPractices = listOf(
                "Eat your dinner early—at least 2.5 to 3 hours before retiring for sleep.",
                "Take a warm foot wash and light head massage with coconut/sesame oil before bed.",
                "Avoid heavy liquids or stimulants immediately before bedtime.",
                "Practice Nishkama Karma (selfless service) to channel active physical stamina."
            ),
            scripturalQuote = "युक्ताहारविहारस्य युक्तचेष्टस्य कर्मसु । युक्तस्वप्नावबोधस्य योगो भवति दुःखहा ॥",
            quoteSource = "Bhagavad Gita (6.17) — 'For one who is temperate in eating and recreation, balanced in work, and regular in sleep and wakefulness, Yoga destroys all sorrow.'",
            audienceNotes = mapOf(
                AudienceCategory.STUDENT to "Replace sugary energy drinks and chips with soaked almonds, seasonal fruits, and fresh water during study marathons.",
                AudienceCategory.UNMARRIED to "Keep evening dinners light, plant-based, and completed before 7:30 PM for effortless morning awakening.",
                AudienceCategory.MARRIED to "Share mindful, peaceful meals with family without TV or smartphone distractions to nurture sattva in the home."
            )
        ),
        BrahmacharyaPillar(
            id = "yoga_pranayama",
            title = "Yogic Asanas, Pranayama & Mudras",
            sanskritTitle = "योग, प्राणायाम एवं मुद्रा",
            subtitle = "Channeling Pranic Flow into Higher Chakras",
            summary = "Physical yogic postures and lock gestures (Bandhas) tone the pelvic plexus, balance neuroendocrine secretions, and uplift prana.",
            detailedText = """
                Classical Yogic Techniques for Ojas Preservation:
                
                1. Sarvangasana (Shoulder Stand) & Viparita Karani:
                   Inverts blood circulation towards thyroid, pituitary, and brain, cooling the pelvic area and calming the nervous system.
                   
                2. Paschimottanasana (Seated Forward Bend):
                   Stretches the posterior spine and stimulates Sushumna Nadi, directing prana upwards.
                   
                3. Ashwini Mudra & Mula Bandha:
                   Gentle rhythmic contraction and release of the pelvic floor muscles. Tones the reproductive glands and sublimates vital energy.
                   
                4. Nadi Shodhana Pranayama (Alternate Nostril Breathing):
                   Balances Ida (cooling lunar) and Pingala (heating solar) nadis, producing profound mental equanimity.
            """.trimIndent(),
            keyPractices = listOf(
                "Perform 5-10 minutes of Nadi Shodhana every morning before meditation.",
                "Practice 20 gentle Ashwini Mudra repetitions during morning asana practice.",
                "Do 3 minutes of Viparita Karani (legs up the wall) before sleeping.",
                "Engage in Bhramari Pranayama (humming bee breath) to dissolve anxious mental chatter."
            ),
            scripturalQuote = "अश्विनीं मुद्रयेन्नित्यं मूलाधारं प्रबोधयेत् । ऊर्ध्वरेता भवेद्योगी...",
            quoteSource = "Gheranda Samhita — 'By regular practice of Ashwini Mudra, vital energy is awakened and the yogi ascends in pure consciousness.'",
            audienceNotes = mapOf(
                AudienceCategory.STUDENT to "Do 5 minutes of Nadi Shodhana before study sessions to instantly calm nervous test anxiety and sharpen focus.",
                AudienceCategory.UNMARRIED to "Incorporate regular vigorous Surya Namaskars and Mula Bandha into daily morning routines.",
                AudienceCategory.MARRIED to "Practice joint gentle breathing and stretching together to reduce daily stress and cultivate peaceful harmony."
            )
        ),
        BrahmacharyaPillar(
            id = "mantra_japa",
            title = "Mantra Japa & Sacred Contemplation",
            sanskritTitle = "मंत्र जप एवं ध्यान",
            subtitle = "Purifying Subconscious Impressions (Chitta Shuddhi)",
            summary = "A pure mind is built through continuous sacred sound vibrations. Repeating a mantra creates positive neuro-spiritual grooves (Samskaras).",
            detailedText = """
                The human mind cannot remain totally blank; it must be occupied with something sublime. Japa (rhythmic repetition of a sacred mantra) anchors the mind and purifies the subconscious Chitta.
                
                Potent Mantras for Brahmacharya:
                • Gayatri Mantra: Invokes the Supreme Solar Light (Savitur) to illuminate and purify the intellect.
                • Maha Mrityunjaya Mantra: Bestows physical vitality, overcomes fears, and heals inner afflictions.
                • Om Namah Shivaya: Calms the fires of desire and establishes stillness of Mount Kailash in the heart.
                • Hanuman Chalisa / Gayatri: Channels divine strength, devotion, and steadfast self-discipline.
            """.trimIndent(),
            keyPractices = listOf(
                "Chant 1 Mala (108 repetitions) of Gayatri Mantra during sunrise or Brahma Muhurta.",
                "Mentally repeat your Ishta Mantra whenever idle or waiting during the day.",
                "Write your mantra (Likhita Japa) for 5 minutes daily in a dedicated notebook.",
                "Use the in-app 108 Mantra Japa Counter for focused daily practice."
            ),
            scripturalQuote = "जपात् श्रान्तो जपेद् ध्यानं ध्यानात् श्रान्तो जपं चरेत् ।",
            quoteSource = "Panchadasi — 'When tired of chanting, enter meditation; when tired of meditation, resume japa.'",
            audienceNotes = mapOf(
                AudienceCategory.STUDENT to "Recite Saraswati Vandana or Gayatri Mantra before opening books for effortless concentration.",
                AudienceCategory.UNMARRIED to "Adopt daily 108 Gayatri or Hanuman Japa to cultivate unshakeable willpower and moral strength.",
                AudienceCategory.MARRIED to "Establish a daily family morning or evening lamp lighting (Deepa Sandhya) with Om chanting."
            )
        )
    )

    val EXERCISES = listOf(
        BrahmacharyaExercise(
            id = "ex_sheetali",
            title = "Sheetali Cooling Breath",
            sanskritName = "शीतली प्राणायाम",
            type = ExerciseType.PRANAYAMA,
            durationMinutes = 5,
            steps = listOf(
                "Sit comfortably with spine erect and shoulders relaxed.",
                "Curl the tongue into a tube shape protruding slightly past the lips.",
                "Inhale deeply and slowly through the tongue tube, feeling the ice-cool sensation entering the throat and chest.",
                "Close mouth, gently hold breath for 3 seconds (Kumbhaka).",
                "Exhale slowly and smoothly through both nostrils.",
                "Repeat for 10 to 15 rounds."
            ),
            benefits = listOf(
                "Instantly cools excess body heat (Pitta).",
                "Dissolves acute sensual cravings and nervous irritability.",
                "Calms high blood pressure and mental restlessness."
            ),
            precautions = "Avoid doing in extreme cold winter weather or if suffering from severe cold/cough."
        ),
        BrahmacharyaExercise(
            id = "ex_ashwini_mudra",
            title = "Ashwini Mudra (Pelvic Tone)",
            sanskritName = "अश्विनी मुद्रा",
            type = ExerciseType.MUDRA_BANDHA,
            durationMinutes = 4,
            steps = listOf(
                "Sit in Siddhasana, Padmasana, or cross-legged with spine straight.",
                "Close eyes and focus attention on the Muladhara (base chakra).",
                "Inhale normally, then gently contract the pelvic and anal sphincter muscles upward.",
                "Hold the contraction for 2-3 seconds without straining.",
                "Release and relax completely for 2 seconds.",
                "Perform 20 to 30 gentle rhythmic cycles."
            ),
            benefits = listOf(
                "Strengthens the pelvic diaphragm and reproductive glands.",
                "Aids in the upward transmutation (Urdhvagati) of vital energy.",
                "Prevents nocturnal involuntary emissions and lethargy."
            ),
            precautions = "Do not strain or squeeze forcefully; maintain gentle, rhythmic awareness."
        ),
        BrahmacharyaExercise(
            id = "ex_nadi_shodhana",
            title = "Nadi Shodhana (Alternate Nostril)",
            sanskritName = "नाड़ी शोधन प्राणायाम",
            type = ExerciseType.PRANAYAMA,
            durationMinutes = 7,
            steps = listOf(
                "Sit comfortably with left hand in Chin Mudra on the knee.",
                "Bring right hand into Vishnu Mudra (index and middle fingers folded).",
                "Close right nostril with thumb; inhale smoothly through left nostril for 4 counts.",
                "Close both nostrils with thumb and ring finger; hold gently for 4 counts.",
                "Open right nostril; exhale smoothly for 4 counts.",
                "Inhale through right nostril for 4 counts; hold for 4 counts; exhale through left nostril for 4 counts.",
                "This completes 1 round. Complete 10 rounds."
            ),
            benefits = listOf(
                "Harmonizes left (lunar) and right (solar) brain hemispheres.",
                "Clears mental chatter, anxiety, and obsessive thoughts.",
                "Purifies the 72,000 subtle nadis across the pranic body."
            ),
            precautions = "Never force the breath or hold to the point of gasping. Maintain effortless rhythm."
        ),
        BrahmacharyaExercise(
            id = "ex_sos_shield",
            title = "Emergency SOS Urge-Shield",
            sanskritName = "आपत्कालीन वेग-शमन उपाय",
            type = ExerciseType.EMERGENCY_SOS,
            durationMinutes = 3,
            steps = listOf(
                "Step 1: Instantly stand up and walk away from isolated rooms or electronic screens.",
                "Step 2: Splash cold tap water generously over your face, eyes, and nape of the neck 5 times.",
                "Step 3: Drink a glass of cool water in slow, measured sips.",
                "Step 4: Take 10 deep Sheetali breaths (or slow belly breaths through the nose).",
                "Step 5: Loudly or mentally recite the Maha Mrityunjaya Mantra 5 times while visualizing a pure golden white light at the center of the forehead."
            ),
            benefits = listOf(
                "Immediately breaks the dopamine trance cycle.",
                "Activates the parasympathetic vagal brake within 60 seconds.",
                "Reclaims conscious rational willpower over impulsive cravings."
            ),
            precautions = "Do not try to negotiate or debate with the mind during an urge; execute physical movement immediately."
        )
    )

    val DAILY_HABITS = listOf(
        BrahmacharyaDailyHabit(
            id = "habit_brahma_muhurta",
            title = "Brahma Muhurta Awakening",
            sanskritName = "ब्रह्म मुहूर्त जागरण",
            description = "Rise before sunrise during the sacred Sattvic hour for meditation and quiet study.",
            timing = "04:30 AM – 06:00 AM",
            points = 20
        ),
        BrahmacharyaDailyHabit(
            id = "habit_cold_splash_bath",
            title = "Cooling Morning Snana",
            sanskritName = "शीतल प्रातः स्नान",
            description = "Take a refreshing bath to invigorate the nervous system and cleanse the physical body.",
            timing = "06:00 AM – 07:00 AM",
            points = 15
        ),
        BrahmacharyaDailyHabit(
            id = "habit_pranayama_asana",
            title = "15-Min Pranayama & Asana",
            sanskritName = "प्राणायाम एवं योगाभ्यास",
            description = "Practice Nadi Shodhana, Sheetali, and Ashwini Mudra to sublimate subtle prana.",
            timing = "06:30 AM – 08:00 AM",
            points = 20
        ),
        BrahmacharyaDailyHabit(
            id = "habit_japa_108",
            title = "108 Repetition Sacred Japa",
            sanskritName = "१०८ गायत्री / महामृत्युंजय जप",
            description = "Chant your chosen Ishta Mantra or Gayatri on a mala or using the digital counter.",
            timing = "Morning or Evening Sandhya",
            points = 20
        ),
        BrahmacharyaDailyHabit(
            id = "habit_sattvic_diet",
            title = "Sattvic Mitahara Diet",
            sanskritName = "सात्त्विक मिताहार",
            description = "Consume pure vegetarian meals in moderation; finish dinner 3 hours prior to sleep.",
            timing = "Throughout the Day",
            points = 15
        ),
        BrahmacharyaDailyHabit(
            id = "habit_digital_fasting",
            title = "Night Digital Detox & Swadhyaya",
            sanskritName = "रात्रि स्वाध्याय एवं स्क्रीन त्याग",
            description = "Switch off screens 1 hour before bed; read 10 minutes of inspiring scripture or noble literature.",
            timing = "09:00 PM – 10:00 PM",
            points = 10
        )
    )
}
