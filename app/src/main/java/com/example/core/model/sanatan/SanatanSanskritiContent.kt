package com.example.core.model.sanatan

/**
 * Original educational curriculum and knowledge base for Sanatan Sanskriti.
 * Covers Atma, Paramatma, Prakriti, Dharma, Karma, Yoga, Brahmanda Time, Devas,
 * Shastras, Guru-Shishya parampara, Ashramas, Purusharthas, 16 Samskaras, Festivals & Glossary.
 */
object SanatanSanskritiContent {

    val TOPICS = listOf(
        SanatanTopic(
            id = "topic_intro_sanatan",
            category = SanatanCategory.FOUNDATION,
            title = "Introduction to Sanatan Sanskriti",
            devanagariTitle = "सनातन संस्कृति परिचय",
            summary = "The eternal, timeless way of righteous living grounded in cosmic harmony, universal consciousness, and oneness of all existence.",
            fullText = """
                'Sanatana' means eternal, beginningless, and endless. 'Sanskriti' refers to refinement, culture, and purification of consciousness.
                
                Unlike systems defined by a single historical founder, Sanatan Sanskriti is a collective realization of eternal cosmic laws (Rta) perceived by ancient Rishis (seers) through deep meditation.
                
                Core Vision:
                • Oneness of All Life: 'Vasudhaiva Kutumbakam' (The whole world is one single family).
                • Freedom of Inquiry: Truth is supreme; paths to the Divine are diverse and respected.
                • Harmony with Nature: Reverence for rivers, mountains, trees, animals, celestial bodies, and the five great elements (Pancha Mahabhutas).
                • Holistic Well-being: Integrating physical health (Ayurveda), spatial harmony (Vastu), cosmic timing (Jyotish), mental stillness (Yoga), and spiritual liberation (Vedanta).
            """.trimIndent(),
            keyPillars = listOf(
                "Ekam Sat Vipra Bahudha Vadanti — Truth is One, the wise speak of It in many names.",
                "Sarve Bhavantu Sukhinah — May all living beings be happy and free from affliction.",
                "Ahimsa Paramo Dharmah — Non-harming and compassion are the supreme spiritual virtues.",
                "Direct Experience (Anubhuti) is valued above blind dogma."
            ),
            scripturalVerses = listOf(
                ScripturalVerse(
                    devanagari = "उदारचरितानां तु वसुधैव कुटुम्बकम् ॥",
                    transliteration = "Udara-charitanam tu vasudhaiva kutumbakam",
                    englishMeaning = "For the noble-minded, the entire earth is their single family.",
                    source = "Maha Upanishad (6.71)"
                ),
                ScripturalVerse(
                    devanagari = "एकं सद्विप्रा बहुधा वदन्ति ॥",
                    transliteration = "Ekam sad vipra bahudha vadanti",
                    englishMeaning = "The Ultimate Truth is One; the enlightened express It in varied ways.",
                    source = "Rig Veda (1.164.46)"
                )
            ),
            commonMisconceptions = listOf(
                "Misconception: Sanatan Dharma is rigid dogmatism. Reality: It is an evolving, living science of spiritual realization encouraging scientific questioning.",
                "Misconception: It represents polytheism. Reality: It recognizes One Supreme Non-Dual Consciousness (Brahman) manifesting in diverse divine forms."
            ),
            practicalLivingTip = "Start each day with gratitude for the sun, earth, water, and parents before stepping out into the world."
        ),
        SanatanTopic(
            id = "topic_atma_ishvara_prakriti",
            category = SanatanCategory.SOUL_GOD_NATURE,
            title = "Atma, Paramatma / Ishvara & Prakriti",
            devanagariTitle = "आत्मा, परमात्मा एवं प्रकृति",
            summary = "The tripartite reality in Vedic ontology: The eternal Soul (Atman), the Supreme Consciousness (Ishvara/Brahman), and the dynamic material matrix (Prakriti).",
            fullText = """
                Vedic philosophy illuminates existence through three foundational realities:
                
                1. Atma (The Individual Soul):
                   The eternal, birthless, changeless, and deathless core of every conscious entity. It is beyond the physical body (Sthula Sharira), subtle energy mind (Sukshma Sharira), and causal realm (Karana Sharira).
                   
                2. Paramatma / Ishvara / Brahman (The Supreme Cosmic Divine):
                   The unmanifest, infinite ground of all existence (Nirguna Brahman) and its personal manifest creative, sustaining, and dissolving aspect (Saguna Brahman / Ishvara).
                   
                3. Prakriti (Material Cosmic Nature):
                   The dynamic matrix composed of the Three Gunas (Sattva - clarity/purity, Rajas - activity/passion, Tamas - inertia/darkness) and the Five Elements (Akasha, Vayu, Agni, Jala, Prithvi).
                   
                The spiritual journey is the recognition of one's true nature as pure luminous consciousness (Sakshi / Witness), realizing that the inner Atman is non-different in essence from Paramatman ('Tat Tvam Asi').
            """.trimIndent(),
            keyPillars = listOf(
                "Atman is eternal, unburned by fire, unwetted by water, and untouched by weapons.",
                "Prakriti is ever-changing; Atman is the unchanging silent witness.",
                "The relationship between Atman and Paramatman is explored through non-dual (Advaita), qualified non-dual (Vishishtadvaita), and dual (Dvaita) philosophical frameworks.",
                "Liberation (Moksha) is awakening from the illusion of separate individuality (Ahamkara)."
            ),
            scripturalVerses = listOf(
                ScripturalVerse(
                    devanagari = "नैनं छिन्दन्ति शस्त्राणि नैनं दहति पावकः । न चैनं क्लेदयन्त्यापो न शोषयति मारुतः ॥",
                    transliteration = "Nainam chhindanti shastrani nainam dahati pavakah...",
                    englishMeaning = "Weapons cannot cut the soul, fire cannot burn it, water cannot wet it, nor can the wind wither it away.",
                    source = "Bhagavad Gita (2.23)"
                ),
                ScripturalVerse(
                    devanagari = "तत्त्वमसि ॥",
                    transliteration = "Tat Tvam Asi",
                    englishMeaning = "Thou Art That (You are that Supreme Eternal Consciousness).",
                    source = "Chandogya Upanishad (6.8.7)"
                )
            ),
            commonMisconceptions = listOf(
                "Misconception: You are your thoughts and physical body. Reality: You are the conscious observer who witnesses thoughts and bodily sensations.",
                "Misconception: Matter is evil or discarded. Reality: Prakriti is sacred divine energy (Shakti) meant to be respected and elevated."
            ),
            practicalLivingTip = "Practice 5 minutes of Sakshi Bhava: Sit silently and observe your thoughts passing by like clouds, without clinging or resisting."
        ),
        SanatanTopic(
            id = "topic_dharma_karma",
            category = SanatanCategory.DHARMA_KARMA,
            title = "Dharma & the Law of Karma",
            devanagariTitle = "धर्म एवं कर्म सिद्धान्त",
            summary = "Dharma is the cosmic matrix of order and righteousness; Karma is the inviolable law of ethical cause and consequence.",
            fullText = """
                Understanding Dharma:
                Dharma is derived from the root 'Dhr' (that which sustains, upholds, and preserves).
                • Samanya Dharma: Universal ethical duties applying to all humanity—truthfulness (Satya), non-violence (Ahimsa), purity (Shaucha), self-restraint (Dama), and charity (Dana).
                • Svadharma: One's unique, authentic duty based on natural talents, life stage (Ashrama), and vocation.
                
                The Triple Mechanics of Karma:
                Karma is the cosmic principle that every action, word, and thought leaves an energetic imprint (Samskara) that brings equivalent fruition.
                1. Sanchita Karma: The accumulated storehouse of all past karmas yet to fructify.
                2. Prarabdha Karma: The portion of past karma that has matured to shape this current lifetime, birth conditions, and constitution.
                3. Agami / Kriyamana Karma: The current actions performed in the present moment with free will, creating future outcomes.
                
                Nishkama Karma (The Art of Selfless Action):
                Performing one's best duty with full diligence while offering the fruits to the Divine, freeing the mind from anxiety, greed, and attachment.
            """.trimIndent(),
            keyPillars = listOf(
                "Dharmo Rakshati Rakshitah — Dharma protects those who protect and uphold Dharma.",
                "Every choice shapes the future; present free will (Purushartha) can overcome past tendencies.",
                "Nishkama Karma transforms routine work into spiritual worship (Seva).",
                "Compassion and forgiveness dissolve karmic knots (Granthis)."
            ),
            scripturalVerses = listOf(
                ScripturalVerse(
                    devanagari = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन । मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि ॥",
                    transliteration = "Karmany-evadhikaras te ma phaleshu kadachana...",
                    englishMeaning = "You have a right only to perform your prescribed duty, but never to the fruits of action. Never let the fruits be your motive, nor let there be attachment to inaction.",
                    source = "Bhagavad Gita (2.47)"
                ),
                ScripturalVerse(
                    devanagari = "धर्मो रक्षति रक्षितः ॥",
                    transliteration = "Dharmo rakshati rakshitah",
                    englishMeaning = "Dharma protects and upholds the one who preserves and stands by Dharma.",
                    source = "Mahabharata (Vana Parva)"
                )
            ),
            commonMisconceptions = listOf(
                "Misconception: Karma is fatalism/helpless destiny. Reality: Karma emphasizes supreme personal responsibility and empowerment through conscious present actions.",
                "Misconception: Dharma translates simply to religion. Reality: Dharma means cosmic order, righteous conduct, inherent nature, and universal ethics."
            ),
            practicalLivingTip = "Before starting any task, silently dedicate the effort to the welfare of all without obsessing over personal accolades."
        ),
        SanatanTopic(
            id = "topic_yoga_sadhana",
            category = SanatanCategory.YOGA_SADHANA,
            title = "Yoga & the Four Sadhana Paths",
            devanagariTitle = "योग एवं चतुर्विध साधना मार्ग",
            summary = "The four classical paths of spiritual union tailored to human temperaments: Jnana, Bhakti, Karma, and Raja Yoga.",
            fullText = """
                Yoga means 'union'—the reconnection of individual consciousness with universal cosmic consciousness. Swami Vivekananda summarized the four paths corresponding to human faculties:
                
                1. Jnana Yoga (The Path of Wisdom & Inquiry):
                   For the analytical mind. Employs Viveka (discernment between real and unreal), Vairagya (dispassion), and Neti-Neti ('Not this, not this') inquiry into 'Who Am I?'.
                   
                2. Bhakti Yoga (The Path of Devotion & Love):
                   For the emotional heart. Channels all feelings through divine love, prayer, singing (Kirtan), surrender (Sharanagati), and seeing the beloved Lord in every creature.
                   
                3. Karma Yoga (The Path of Selfless Service):
                   For the active doer. Acting without ego or selfish clinging, viewing all labor as service to humanity and the Divine.
                   
                4. Raja / Ashtanga Yoga (The Path of Meditative Mastery):
                   For the contemplative explorer. Mastered through Maharishi Patanjali's 8 Limbs: Yama (restraints), Niyama (observances), Asana (posture), Pranayama (breath control), Pratyahara (sense withdrawal), Dharana (concentration), Dhyana (meditation), and Samadhi (superconscious stillness).
            """.trimIndent(),
            keyPillars = listOf(
                "Yogas Chitta Vritti Nirodhah — Yoga is the stilling of the fluctuations of the mind.",
                "All four paths converge at the summit of Self-realization.",
                "A balanced life integrates devotion in the heart, wisdom in the intellect, and service in the hands.",
                "Daily meditation anchors the turbulent mind in deep inner peace."
            ),
            scripturalVerses = listOf(
                ScripturalVerse(
                    devanagari = "योगश्चित्तवृत्तिनिरोधः ॥",
                    transliteration = "Yogas chitta-vritti-nirodhah",
                    englishMeaning = "Yoga is the intentional calming and stillness of the mental modifications.",
                    source = "Patanjali Yoga Sutras (1.2)"
                ),
                ScripturalVerse(
                    devanagari = "योगः कर्मसु कौशलम् ॥",
                    transliteration = "Yogah karmasu kaushalam",
                    englishMeaning = "Yoga is supreme skill, mindfulness, and equanimity in action.",
                    source = "Bhagavad Gita (2.50)"
                )
            ),
            commonMisconceptions = listOf(
                "Misconception: Yoga is solely physical acrobatics or fitness stretching. Reality: Asanas are just one preparatory step toward meditation and inner liberation.",
                "Misconception: You must abandon family life to practice Yoga. Reality: Householders can reach the highest yogic realization through daily dedicated practice."
            ),
            practicalLivingTip = "Dedicate 15 minutes each day to sit still in meditation, practicing gentle breath awareness."
        ),
        SanatanTopic(
            id = "topic_brahmanda_cosmic_time",
            category = SanatanCategory.COSMIC_TIME,
            title = "Brahmanda & the Vedic Concept of Cosmic Time",
            devanagariTitle = "ब्रह्माण्ड एवं वैदिक काल गणना",
            summary = "The vast, cyclical cosmic time scale of creation, preservation, and dissolution measured across Yugas, Manvantaras, and Kalpas.",
            fullText = """
                Ancient Vedic cosmology envisioned time (Kala) not as a linear arrow, but as an eternal cyclical wheel (Kala Chakra) spanning billions of years:
                
                The Four Yugas (The Chaturyuga / Mahayuga Cycle):
                1. Satya Yuga (Golden Age of Truth): 1,728,000 solar years (Dharma stands on 4 legs).
                2. Treta Yuga (Silver Age): 1,296,000 solar years (Dharma stands on 3 legs).
                3. Dvapara Yuga (Bronze Age): 864,000 solar years (Dharma stands on 2 legs).
                4. Kali Yuga (Iron Age of Materialism): 432,000 solar years (Dharma stands on 1 leg).
                
                Cosmic Units of Scale:
                • 1 Mahayuga = 4.32 million solar years.
                • 71 Mahayugas = 1 Manvantara (ruled by a Manu, spanning ~306.72 million years).
                • 1000 Mahayugas = 1 Kalpa (One daytime period of Brahma = 4.32 billion solar years).
                • During Kalpa night (Pralaya), the cosmos enters latent rest before the next dawn of creation.
                
                This ancient scale astonishingly aligns with modern astrophysics and geological timescales for planetary lifecycles.
            """.trimIndent(),
            keyPillars = listOf(
                "Time is cyclical; dissolution is merely preparation for renewed creation.",
                "In Kali Yuga, the simplest and most potent spiritual practice is Nama Japa (chanting the divine name).",
                "Human life is an exceedingly rare cosmic privilege (Durlabha Manushya Janma) for conscious evolution.",
                "Cosmic cycles instill humility and patience amid temporary worldly upheavals."
            ),
            scripturalVerses = listOf(
                ScripturalVerse(
                    devanagari = "सहस्रयुगपर्यन्तमहर्यद्ब्रह्मणो विदुः । रात्रिं युगसहस्रान्तां तेऽहोरात्रविदो जनाः ॥",
                    transliteration = "Sahasra-yuga-paryantam ahar yad brahmano viduh...",
                    englishMeaning = "Those who know that Brahma's cosmic day spans a thousand Mahayugas, and his night lasts as long, truly understand the nature of time.",
                    source = "Bhagavad Gita (8.17)"
                )
            ),
            commonMisconceptions = listOf(
                "Misconception: Vedic texts believe the universe is only a few thousand years old. Reality: Vedic cosmology measures time in billions of years, matching modern astrophysics.",
                "Misconception: Kali Yuga means spiritual hopelessness. Reality: Scriptural texts celebrate Kali Yuga because small acts of sincere devotion yield immense spiritual fruit."
            ),
            practicalLivingTip = "When feeling overwhelmed by daily trivialities, contemplate the vast Kalpa timescale to regain serene perspective."
        ),
        SanatanTopic(
            id = "topic_devas_cosmic_energies",
            category = SanatanCategory.DEVAS_CONCEPTS,
            title = "Devas & Traditional Cosmic Concepts",
            devanagariTitle = "देव तत्व एवं दैव विज्ञान",
            summary = "Understanding Devas as luminous cosmic forces, archetypes, and functional expressions of the One Supreme Brahman.",
            fullText = """
                The word 'Deva' is derived from 'Div' (to shine, illuminate, or bestow). Devas are luminous divine beings and cosmic intelligences governing the laws of nature and the human micro-universe:
                
                The 33 Koti (Supreme Types / Categories) of Devas:
                In Vedic Sanskrit, 'Koti' means category or class (often misunderstood colloquially as numerical crores):
                1. 8 Vasus: Cosmic elemental principles (Prithvi - Earth, Agni - Fire, Vayu - Wind, Antariksha - Atmosphere, Aditya - Sun, Dyaus - Sky, Chandrama - Moon, Nakshatra - Stars).
                2. 11 Rudras: Vital pranic forces and inner transformative powers (10 sensory/pranic energies + mind).
                3. 12 Adityas: The twelve solar months representing dynamic cosmic sustenance.
                4. 2 Ashvins / Indra-Prajapati: Lords of healing, balance, and creative leadership.
                
                All 33 divine principles are manifestations of the One Infinite Reality (Brahman), functioning like different facets of a brilliant gem.
            """.trimIndent(),
            keyPillars = listOf(
                "Devas represent both macrocosmic natural forces (Adhidaivika) and microcosmic inner faculties (Adhyatmika).",
                "Agni is the inner digestive and mental fire; Vayu is vital prana; Surya is illuminating intellect.",
                "Yajna and Puja establish reciprocal harmony between humanity and cosmic nature.",
                "Reverence for diverse divine forms reflects profound pluralistic wisdom."
            ),
            scripturalVerses = listOf(
                ScripturalVerse(
                    devanagari = "देवान्भावयतानेन ते देवा भावयन्तु वः । परस्परं भावयन्तः श्रेयः परमवाप्स्यथ ॥",
                    transliteration = "Devan bhavayatanena te deva bhavayantu vah...",
                    englishMeaning = "Foster and honor the cosmic divine forces with your dedicated actions, and may they foster you. By sustaining one another, you shall attain the highest good.",
                    source = "Bhagavad Gita (3.11)"
                )
            ),
            commonMisconceptions = listOf(
                "Misconception: 33 Koti means 330 million separate competing gods. Reality: 'Koti' means 33 divine categories manifesting the One Supreme Reality.",
                "Misconception: Worshiping Devas is superstition. Reality: It is conscious communion with the subtle energetic laws and conscious forces upholding the universe."
            ),
            practicalLivingTip = "Offer a moment of quiet gratitude to the morning sun (Surya) and fresh water (Varuna) before starting daily chores."
        ),
        SanatanTopic(
            id = "topic_shastra_guru_parampara",
            category = SanatanCategory.SCRIPTURES_GURU,
            title = "Shastras & the Guru-Shishya Tradition",
            devanagariTitle = "शास्त्र एवं गुरु-शिष्य परम्परा",
            summary = "The vast repository of Vedic literature (Shruti & Smriti) and the living lineage of oral wisdom transmission.",
            fullText = """
                The Vedic Literary Tree:
                
                1. Shruti ('That which is directly heard/revealed'):
                   Primary eternal authority comprising:
                   • 4 Vedas: Rigveda (hymns), Samaveda (melodies), Yajurveda (rituals/ethics), Atharvaveda (daily life & healing).
                   • Each Veda consists of Samhitas (mantras), Brahmanas (explanations), Aranyakas (contemplations), and Upanishads (supreme philosophical essence / Vedanta).
                   
                2. Smriti ('That which is remembered / secondary literature'):
                   Practical applications over time:
                   • 6 Vedangas: Shiksha (phonetics), Chandas (meter), Vyakarana (grammar), Nirukta (etymology), Jyotisha (astronomy/time), Kalpa (rituals).
                   • 4 Upavedas: Ayurveda (health), Dhanurveda (defense), Gandharvaveda (arts/music), Sthapatyaveda (architecture/Vastu).
                   • Itihasas (Ramayana, Mahabharata / Bhagavad Gita), Puranas (18 sacred lore), and Dharma Shastras.
                   
                The Guru-Shishya Parampara:
                Wisdom is not merely academic data; it is an awakened flame passed from realized master (Guru) to sincere disciple (Shishya) through reverence, questioning, and dedicated service.
            """.trimIndent(),
            keyPillars = listOf(
                "Shruti is the unchanging foundational truth; Smriti adapts to historical eras (Yuga Dharma).",
                "Guru removes the darkness of ignorance (Gu = darkness, Ru = dispeller).",
                "Vedic learning requires triple steps: Shravana (hearing), Manana (reflective pondering), and Nididhyasana (meditative assimilation).",
                "Respect for lineage prevents corruption of sacred science."
            ),
            scripturalVerses = listOf(
                ScripturalVerse(
                    devanagari = "तद्विद्धि प्रणिपातेन परिप्रश्नेन सेवया । उपदेक्ष्यन्ति ते ज्ञानं ज्ञानिनस्तत्त्वदर्शिनः ॥",
                    transliteration = "Tad viddhi pranipatena pariprashnena sevaya...",
                    englishMeaning = "Acquire that wisdom by approaching the wise with humility, sincere inquiry, and dedicated service. The seers of Truth will guide you.",
                    source = "Bhagavad Gita (4.34)"
                ),
                ScripturalVerse(
                    devanagari = "गुरुर्ब्रह्मा गुरुर्विष्णुः गुरुर्देवो महेश्वरः । गुरुः साक्षात् परं ब्रह्म तस्मै श्रीगुरवे नमः ॥",
                    transliteration = "Gurur Brahma Gurur Vishnuh Gurur Devo Maheshvarah...",
                    englishMeaning = "The Guru represents creation, preservation, and transformation; the Guru is verily the living reflection of Supreme Brahman.",
                    source = "Guru Gita"
                )
            ),
            commonMisconceptions = listOf(
                "Misconception: Sanatan texts discourage questioning. Reality: The Upanishads and Bhagavad Gita are entirely dialogue-based inquiry (Samvada) between teacher and questioning student.",
                "Misconception: Puranas are literal historic journals only. Reality: Puranas use deep allegorical and symbolic storytelling to convey profound philosophical truths to all minds."
            ),
            practicalLivingTip = "Spend 10 minutes every evening reading a verse of the Bhagavad Gita or an Upanishad passage with its commentary."
        ),
        SanatanTopic(
            id = "topic_ashrama_purushartha",
            category = SanatanCategory.ASHRAMA_PURUSHARTHA,
            title = "The 4 Purusharthas & 4 Ashramas",
            devanagariTitle = "चतुर्विध पुरुषार्थ एवं चतुराश्रम",
            summary = "The timeless Sanatan blueprint for a complete, purposeful, balanced, and liberated human life.",
            fullText = """
                The Four Purusharthas (The Four Aims of Human Life):
                1. Dharma (Righteousness & Cosmic Order):
                   The ethical foundation governing all actions, honesty, duty, and moral integrity.
                2. Artha (Material Wealth & Security):
                   Acquiring prosperity, resources, and career success through ethical, honest, and beneficial means.
                3. Kama (Joy, Love & Aesthetic Pleasure):
                   Fulfilling wholesome desires, family happiness, arts, culture, and sensory beauty in harmony with Dharma.
                4. Moksha (Spiritual Liberation):
                   The ultimate culmination—transcending limitations, ending suffering, and realizing union with Supreme Consciousness.
                   
                The Four Ashramas (The Four Stages of Life):
                1. Brahmacharya (Student Life, 0–25 yrs): Focus on discipline, character building, physical vitality, and learning skills.
                2. Grihastha (Householder Life, 25–50 yrs): Earning wealth, supporting society, raising children, performing charitable duties.
                3. Vanaprastha (Contemplative Transition, 50–75 yrs): Gradual detachment from worldly pursuits, mentoring youth, deep spiritual study.
                4. Sannyasa (Renunciation & Total Dedication, 75+ yrs): Complete inner detachment, universal brotherhood, and meditation on Moksha.
            """.trimIndent(),
            keyPillars = listOf(
                "Artha and Kama must always be anchored within the bounds of Dharma to lead to Moksha.",
                "Grihastha Ashrama is the pillar of society that financially and practically sustains all other three stages.",
                "Life is viewed as a progressive journey of spiritual maturation, not aimless consumerism.",
                "Each stage of life possesses distinct duties and spiritual honors."
            ),
            scripturalVerses = listOf(
                ScripturalVerse(
                    devanagari = "धर्मार्थकाममोक्षाणां यस्यैकोऽपि न विद्यते । अजागलस्तनस्यैव तस्य जन्म निरर्थकम् ॥",
                    transliteration = "Dharmartha-kama-mokshanam yasyaiko'pi na vidyate...",
                    englishMeaning = "One whose life is devoid of all four aims—Dharma, Artha, Kama, and Moksha—lives in vain.",
                    source = "Chanakya Niti"
                )
            ),
            commonMisconceptions = listOf(
                "Misconception: Sanatan Dharma teaches poverty and renouncing all enjoyment immediately. Reality: Artha (wealth) and Kama (enjoyment) are celebrated when grounded in Dharma.",
                "Misconception: Sannyasa requires running away into forests. Reality: True Sannyasa is inner mental detachment from ego and possessiveness while serving all."
            ),
            practicalLivingTip = "Evaluate your weekly activities: Ensure you are dedicating time to career/wealth (Artha), family joy (Kama), ethical duty (Dharma), and meditation (Moksha)."
        ),
        SanatanTopic(
            id = "topic_samskaras",
            category = SanatanCategory.SAMSKARAS,
            title = "The 16 Sacred Samskaras (Shodasha Samskara)",
            devanagariTitle = "षोडश संस्कार (जीवन के १६ संस्कार)",
            summary = "Sacred transformative milestones designed to sanctify human life from conception to departure.",
            fullText = """
                A 'Samskara' is a sacred spiritual impression, refinement, and developmental milestone. Just as gold is purified through refining fire, the human soul is refined through the 16 traditional Samskaras:
                
                Prenatal & Infant Samskaras:
                1. Garbhadhana: Sacred conception with pure prayer and spiritual intention.
                2. Pumsavana: Sanctification for healthy embryonic growth during the 3rd month.
                3. Simantonnayana: Baby shower and mental peace rituals for the expectant mother.
                4. Jatakarma: Birth blessing with honey/ghee and reciting sacred prayers into the newborn's ear.
                5. Namakarana: The sacred naming ceremony aligned with astrological Nakshatra vibration.
                6. Nishkramana: First outing of the child to gaze upon the morning sun and nature.
                7. Annaprashana: First feeding of solid, wholesome cooked food (approx. 6th month).
                8. Chudakarana (Mundan): First tonsure/haircut to cleanse prenatal karma and stimulate brain vigor.
                9. Karnavedha: Ear piercing for energetic balance and acupuncture benefits.
                
                Educational & Social Samskaras:
                10. Vidyarambha: Commencing learning of alphabets, language, and knowledge.
                11. Upanayana (Yajnopavita): Sacred initiation into Gayatri Mantra and dedicated studenthood.
                12. Vedarambha: Commencing direct study of the sacred Vedas and scriptures.
                13. Keshanta (Godana): First shaving of beard marking physical adulthood.
                14. Samavartana: Graduation ceremony marking completion of formal studies.
                
                Adult & Departure Samskaras:
                15. Vivaha: The sacred matrimonial union solemnized before Agni and the community.
                16. Antyeshti: The final rites returning the five elements of the physical body back to cosmic nature.
            """.trimIndent(),
            keyPillars = listOf(
                "Samskaras purify genetic and subconscious impressions (Chitta Shuddhi).",
                "Every major life transition is honored with sacred gratitude, community blessing, and mantra vibrations.",
                "Vivaha is viewed not as a mere legal contract, but as a sacred spiritual partnership (Saha-Dharmini).",
                "Antyeshti acknowledges the immortality of the soul and returns the elements to nature."
            ),
            scripturalVerses = listOf(
                ScripturalVerse(
                    devanagari = "संस्कारात् द्विज उच्यते ॥",
                    transliteration = "Samskarat dvija uchyate",
                    englishMeaning = "By birth one is born in natural simplicity; through sacred refinement and education (Samskara), one attains second birth of wisdom.",
                    source = "Manu Smriti"
                )
            ),
            commonMisconceptions = listOf(
                "Misconception: Samskaras are obsolete superstition. Reality: They are profound psychological and community celebrations marking emotional milestones with grounding mindfulness.",
                "Misconception: Upanayana is only for a few. Reality: Ancient texts emphasize that anyone stepping sincerely into learning and wisdom is eligible for spiritual initiation."
            ),
            practicalLivingTip = "Celebrate your children's birthdays with a prayer lamp, tree planting, and charitable feeding rather than mere commercial distraction."
        ),
        SanatanTopic(
            id = "topic_festivals_practices",
            category = SanatanCategory.FESTIVALS_PRACTICES,
            title = "Festivals, Vrats & Traditional Practices",
            devanagariTitle = "पर्व, व्रत एवं सदाचार परम्परा",
            summary = "Living in rhythm with seasonal, lunar, and cosmic energies through joyful celebrations and purifying austerities.",
            fullText = """
                Sanatan festivals are not arbitrary holidays; they are precisely synchronized with astronomy, solar transits (Sankranti), lunar phases (Tithis), seasonal transitions (Ritus), and spiritual lessons:
                
                Major Cosmic & Spiritual Festivals:
                • Makar Sankranti: Sun enters Capricorn (Uttarayana), celebrating light, harvest, and new beginnings.
                • Maha Shivaratri: The darkest night of Phalguna, ideal for all-night meditation and overcoming ego.
                • Navaratri (Chaitra & Sharada): 9 nights honoring the Divine Mother (Durga, Lakshmi, Saraswati) celebrating inner victory over vices (Asuras).
                • Janmashtami: The descent of Bhagavan Krishna, celebrating divine love, joy, and the wisdom of the Gita.
                • Deepavali: The festival of lights celebrating the return of Sri Rama and illuminating the inner light over ignorance.
                
                Purifying Observances (Vrats & Sadachara):
                • Ekadashi Vrat: 11th lunar day fasting for physiological cellular cleansing (autophagy) and heightened meditation.
                • Sandhyavandanam: Morning, noon, and evening prayer honoring the sun and cosmic prana.
                • Pancha Maha Yajnas: 5 daily duties of gratitude (Brahma Yajna - study, Deva Yajna - nature, Pitru Yajna - ancestors, Manushya Yajna - serving guests/poor, Bhuta Yajna - feeding animals).
            """.trimIndent(),
            keyPillars = listOf(
                "Festivals unite family, community, art, classical music, culinary joy, and spirituality.",
                "Fasting on Ekadashi detoxifies the body and aligns with lunar gravitational tides.",
                "Pancha Maha Yajnas foster sustainable, zero-waste, compassionate ecological living.",
                "Traditions balance inner contemplation with vibrant communal joy."
            ),
            scripturalVerses = listOf(
                ScripturalVerse(
                    devanagari = "तमसो मा ज्योतिर्गमय । मृत्योर्माऽमृतं गमय ॥",
                    transliteration = "Tamaso ma jyotir gamaya, mrityor mamritam gamaya",
                    englishMeaning = "Lead us from the darkness of ignorance unto the light of knowledge; lead us from mortality unto eternal immortality.",
                    source = "Brihadaranyaka Upanishad (1.3.28)"
                )
            ),
            commonMisconceptions = listOf(
                "Misconception: Vrat/fasting is self-harm. Reality: Gentle intermittent fasting aligned with lunar cycles promotes mental clarity, cellular rejuvenation, and self-mastery.",
                "Misconception: Festivals are wasteful. Reality: Traditional festivals distribute wealth through charity, uplift local artisans, and renew community solidarity."
            ),
            practicalLivingTip = "Observe the upcoming Ekadashi with light fruit or satvik food, keeping dedicated time for mantra japa and quiet reading."
        )
    )

    val GLOSSARY = listOf(
        SanskritGlossaryTerm(
            id = "g_dharma",
            term = "Dharma",
            devanagari = "धर्म",
            pronunciation = "Dhar-ma",
            category = SanatanCategory.DHARMA_KARMA,
            rootEtymology = "From root 'Dhri' (to uphold, sustain)",
            conciseMeaning = "Cosmic order, righteousness, duty, ethics, and intrinsic nature.",
            inDepthDescription = "Dharma is that which upholds the cosmic order and social harmony. It is universal virtue as well as individual calling.",
            practicalContext = "Standing by truth and integrity even when difficult."
        ),
        SanskritGlossaryTerm(
            id = "g_karma",
            term = "Karma",
            devanagari = "कर्म",
            pronunciation = "Kar-ma",
            category = SanatanCategory.DHARMA_KARMA,
            rootEtymology = "From root 'Kri' (to do, act)",
            conciseMeaning = "Action, work, deed, and the universal law of ethical causality.",
            inDepthDescription = "Every action generates an imprint that bears future consequences. Divided into Sanchita, Prarabdha, and Agami.",
            practicalContext = "Acting with conscious mindfulness and selfless service."
        ),
        SanskritGlossaryTerm(
            id = "g_atman",
            term = "Atman",
            devanagari = "आत्मन्",
            pronunciation = "Aat-man",
            category = SanatanCategory.SOUL_GOD_NATURE,
            rootEtymology = "From 'An' (to breathe) or 'At' (to move constantly)",
            conciseMeaning = "The immortal, divine Self / Soul beyond body and mind.",
            inDepthDescription = "Pure consciousness that witnesses all thoughts, emotions, and physical changes without being tainted by them.",
            practicalContext = "Remembering your true identity as the peaceful witness."
        ),
        SanskritGlossaryTerm(
            id = "g_brahman",
            term = "Brahman",
            devanagari = "ब्रह्मन्",
            pronunciation = "Brah-man",
            category = SanatanCategory.SOUL_GOD_NATURE,
            rootEtymology = "From 'Brih' (to expand, grow infinite)",
            conciseMeaning = "The infinite, non-dual, Supreme Ground of all existence.",
            inDepthDescription = "Transcendent and immanent reality described as Sat-Chit-Ananda (Existence, Pure Consciousness, Infinite Bliss).",
            practicalContext = "Recognizing the divine spark in all living beings."
        ),
        SanskritGlossaryTerm(
            id = "g_moksha",
            term = "Moksha",
            devanagari = "मोक्ष",
            pronunciation = "Moke-sha",
            category = SanatanCategory.ASHRAMA_PURUSHARTHA,
            rootEtymology = "From 'Much' (to release, liberate)",
            conciseMeaning = "Spiritual liberation, freedom from the cycle of rebirth.",
            inDepthDescription = "The culmination of human life—transcending all ignorance, delusion, and sorrow to abide in eternal divine peace.",
            practicalContext = "Letting go of small selfish attachments for inner liberation."
        ),
        SanskritGlossaryTerm(
            id = "g_ojas",
            term = "Ojas",
            devanagari = "ओजस्",
            pronunciation = "Oh-jas",
            category = SanatanCategory.FOUNDATION,
            rootEtymology = "From 'Uj' (strength, vigor, vitality)",
            conciseMeaning = "Subtle biological and spiritual essence of vitality and radiance.",
            inDepthDescription = "The supreme refined byproduct of perfect digestion and Brahmacharya, conferring immunity, memory, and glowing aura.",
            practicalContext = "Preserved through self-restraint, clean nutrition, and meditation."
        ),
        SanskritGlossaryTerm(
            id = "g_prana",
            term = "Prana",
            devanagari = "प्राण",
            pronunciation = "Prah-na",
            category = SanatanCategory.YOGA_SADHANA,
            rootEtymology = "From 'Pra' (forth) + 'An' (to breathe)",
            conciseMeaning = "The universal life-force energy animating the body and mind.",
            inDepthDescription = "Flows through 72,000 subtle channels (Nadis) and is regulated through conscious breathing (Pranayama).",
            practicalContext = "Cultivated through morning breathwork, fresh air, and pure diet."
        ),
        SanskritGlossaryTerm(
            id = "g_samskara",
            term = "Samskara",
            devanagari = "संस्कार",
            pronunciation = "Sam-skaar",
            category = SanatanCategory.SAMSKARAS,
            rootEtymology = "From 'Sam' (together, perfect) + 'Kri' (to make, refine)",
            conciseMeaning = "Sacred refinement, milestone ceremony, and subconscious mental impression.",
            inDepthDescription = "Refers both to the 16 sacred life rites and to the neuro-spiritual memory impressions created by repetitive thoughts and habits.",
            practicalContext = "Creating noble daily habits to imprint positive samskaras in the mind."
        ),
        SanskritGlossaryTerm(
            id = "g_gunas",
            term = "Gunas (Triguna)",
            devanagari = "गुण (त्रिगुण)",
            pronunciation = "Gu-na",
            category = SanatanCategory.SOUL_GOD_NATURE,
            rootEtymology = "Qualities / Threads of material nature",
            conciseMeaning = "The 3 primal qualities: Sattva (clarity), Rajas (passion), Tamas (inertia).",
            inDepthDescription = "Everything in physical and mental nature is a dynamic blend of these three forces. Spiritual practice elevates Tamas to Rajas, and Rajas to Sattva.",
            practicalContext = "Cultivating Sattva through early rising, pure food, and peaceful habits."
        ),
        SanskritGlossaryTerm(
            id = "g_yajna",
            term = "Yajna",
            devanagari = "यज्ञ",
            pronunciation = "Yaj-na",
            category = SanatanCategory.FESTIVALS_PRACTICES,
            rootEtymology = "From 'Yaj' (to worship, offer, harmonize)",
            conciseMeaning = "Sacred fire offering, selfless sacrifice, and noble collaborative action.",
            inDepthDescription = "The spirit of offering one's best into the cosmic collective fire of life without selfish hoarding.",
            practicalContext = "Living in a way that contributes to the ecological and spiritual balance."
        )
    )

    val QUIZZES = listOf(
        SanatanQuiz(
            id = "quiz_foundations",
            topicId = "topic_intro_sanatan",
            title = "Foundations of Sanatan Sanskriti",
            category = SanatanCategory.FOUNDATION,
            questions = listOf(
                QuizQuestion(
                    id = "q1",
                    question = "What is the literal meaning of the term 'Sanatana'?",
                    options = listOf("Ancient tradition only", "Eternal, timeless, and beginningless", "Ritualistic practice", "Regional culture"),
                    correctIndex = 1,
                    explanation = "Sanatana means eternal and timeless—pertaining to cosmic truths that remain valid across all eras.",
                    scripturalReference = "Rig Veda & Upanishads"
                ),
                QuizQuestion(
                    id = "q2",
                    question = "What does the famous Vedic dictum 'Vasudhaiva Kutumbakam' proclaim?",
                    options = listOf("Only my clan is important", "The entire world is one family", "Only seekers are worthy", "Nature must be conquered"),
                    correctIndex = 1,
                    explanation = "From the Maha Upanishad, it declares the universal brotherhood of all beings on earth.",
                    scripturalReference = "Maha Upanishad (6.71)"
                ),
                QuizQuestion(
                    id = "q3",
                    question = "What is the meaning of 'Ekam Sat Vipra Bahudha Vadanti'?",
                    options = listOf("Truth is complex and hidden", "Truth is One, the wise speak of It in many names", "Truth belongs only to one book", "There is no truth"),
                    correctIndex = 1,
                    explanation = "This foundational Rigvedic verse establishes the pluralistic harmony of Sanatan philosophy.",
                    scripturalReference = "Rig Veda (1.164.46)"
                )
            )
        ),
        SanatanQuiz(
            id = "quiz_dharma_karma",
            topicId = "topic_dharma_karma",
            title = "Dharma & the Science of Karma",
            category = SanatanCategory.DHARMA_KARMA,
            questions = listOf(
                QuizQuestion(
                    id = "q4",
                    question = "Which type of Karma represents the current actions we perform with free will in the present?",
                    options = listOf("Sanchita Karma", "Prarabdha Karma", "Agami / Kriyamana Karma", "Adhyatmika Karma"),
                    correctIndex = 2,
                    explanation = "Agami/Kriyamana Karma is the current action performed in the present moment through choice and willpower.",
                    scripturalReference = "Vedanta Shastras"
                ),
                QuizQuestion(
                    id = "q5",
                    question = "What is 'Nishkama Karma' as taught in the Bhagavad Gita?",
                    options = listOf("Refusing to do any work", "Action performed without selfish attachment to the fruits", "Working only for commercial wealth", "Acting only when praised"),
                    correctIndex = 1,
                    explanation = "Bhagavad Gita (2.47) teaches dedicated, skillful action without anxiety or greed for the results.",
                    scripturalReference = "Bhagavad Gita (2.47)"
                ),
                QuizQuestion(
                    id = "q6",
                    question = "What does 'Dharmo Rakshati Rakshitah' mean?",
                    options = listOf("Dharma protects those who protect and uphold Dharma", "Dharma is easily destroyed", "Dharma is ritual alone", "Dharma requires force"),
                    correctIndex = 0,
                    explanation = "Upholding righteousness, truth, and duty provides unshakeable spiritual and moral protection.",
                    scripturalReference = "Mahabharata"
                )
            )
        ),
        SanatanQuiz(
            id = "quiz_purusharthas",
            topicId = "topic_ashrama_purushartha",
            title = "Purusharthas & the Four Ashramas",
            category = SanatanCategory.ASHRAMA_PURUSHARTHA,
            questions = listOf(
                QuizQuestion(
                    id = "q7",
                    question = "Which of the following represents the Four Purusharthas in correct traditional order?",
                    options = listOf("Kama, Artha, Moksha, Dharma", "Dharma, Artha, Kama, Moksha", "Artha, Kama, Dharma, Moksha", "Moksha, Dharma, Artha, Kama"),
                    correctIndex = 1,
                    explanation = "Dharma (righteousness), Artha (honest wealth), Kama (wholesome delight), and Moksha (spiritual liberation).",
                    scripturalReference = "Dharma Shastras & Chanakya Niti"
                ),
                QuizQuestion(
                    id = "q8",
                    question = "Which Ashrama is considered the financial and practical support pillar for all other three stages?",
                    options = listOf("Brahmacharya", "Grihastha (Householder)", "Vanaprastha", "Sannyasa"),
                    correctIndex = 1,
                    explanation = "The Grihastha householder earns wealth ethically and provides charity and sustenance to students, contemplatives, and renunciates.",
                    scripturalReference = "Manu Smriti"
                )
            )
        ),
        SanatanQuiz(
            id = "quiz_devas_time",
            topicId = "topic_brahmanda_cosmic_time",
            title = "Cosmic Time & Devas",
            category = SanatanCategory.COSMIC_TIME,
            questions = listOf(
                QuizQuestion(
                    id = "q9",
                    question = "What does '33 Koti Devas' traditionally refer to in Vedic Sanskrit?",
                    options = listOf("330 million separate individuals", "33 Supreme categories / classes of divine cosmic forces", "33 temples in India", "33 historical kings"),
                    correctIndex = 1,
                    explanation = "In Vedic Sanskrit, 'Koti' means category/type (8 Vasus, 11 Rudras, 12 Adityas, 2 Ashvins).",
                    scripturalReference = "Brihadaranyaka Upanishad (3.9)"
                ),
                QuizQuestion(
                    id = "q10",
                    question = "What is the total duration of One Mahayuga (all 4 Yugas combined)?",
                    options = listOf("43,200 years", "432,000 years", "4.32 million solar years", "4.32 billion solar years"),
                    correctIndex = 2,
                    explanation = "1 Mahayuga = Satya (1.728M) + Treta (1.296M) + Dvapara (0.864M) + Kali (0.432M) = 4.32 million solar years.",
                    scripturalReference = "Surya Siddhanta & Puranas"
                )
            )
        )
    )
}
