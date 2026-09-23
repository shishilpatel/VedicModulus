package com.example.engines.vastu

enum class VastuSpaceMode(val displayName: String, val sanskritTitle: String) {
    HOME("Home & Residence", "Griha Vastu (गृह वास्तु)"),
    OFFICE("Office & Workplace", "Vyapar Vastu (व्यापार वास्तु)")
}

enum class DirectionSuitability(val label: String, val hindiLabel: String, val ratingStars: String) {
    EXCELLENT("Highly Auspicious", "उत्तम (सर्वश्रेष्ठ)", "⭐⭐⭐"),
    ACCEPTABLE("Acceptable / Neutral", "मध्यम (सामान्य)", "⭐⭐"),
    DOSHA("Vastu Dosha (Defect)", "वास्तु दोष (वर्जित)", "⚠️")
}

data class RoomTypeInfo(
    val id: String,
    val name: String,
    val hindiName: String,
    val mode: VastuSpaceMode,
    val iconEmoji: String,
    val idealDirections: List<String>,
    val acceptableDirections: List<String>,
    val forbiddenDirections: List<String>,
    val guidanceSummary: String,
    val specificRemedies: Map<String, String> = emptyMap()
)

data class VastuEvaluationResult(
    val room: RoomTypeInfo,
    val direction: String,
    val suitability: DirectionSuitability,
    val explanation: String,
    val recommendedRemedy: String? = null,
    val bestAlternativeDirections: List<String>
)

object VastuSpaceOptimizationEngine {

    val HOME_ROOMS = listOf(
        RoomTypeInfo(
            id = "puja_room",
            name = "Mandir / Puja Room",
            hindiName = "पूजा घर (मंदिर)",
            mode = VastuSpaceMode.HOME,
            iconEmoji = "🪔",
            idealDirections = listOf("North-East", "North", "East"),
            acceptableDirections = listOf("West"),
            forbiddenDirections = listOf("South-West", "South", "South-East", "North-West"),
            guidanceSummary = "The sacred Mandir belongs in Ishanya (North-East), the divine confluence of water and cosmic ether. Face East or North while performing prayer.",
            specificRemedies = mapOf(
                "South-West" to "Severe Dosha. Place a Sphatik (quartz) Shree Yantra, keep the mandir on an elevated wooden pedestal, and use brass Swastikas on the door.",
                "South-East" to "Fire conflict. Install a silver bowl with holy Ganga water and camphor to balance the fiery Agni energy.",
                "South" to "Place a brass Surya yantra, light a daily mustard oil lamp facing East, and avoid sleeping right below the altar."
            )
        ),
        RoomTypeInfo(
            id = "kitchen",
            name = "Kitchen (Cooking Zone)",
            hindiName = "रसोई घर (अग्नि कोण)",
            mode = VastuSpaceMode.HOME,
            iconEmoji = "🔥",
            idealDirections = listOf("South-East", "North-West"),
            acceptableDirections = listOf("East"),
            forbiddenDirections = listOf("North-East", "South-West", "North"),
            guidanceSummary = "The kitchen belongs in Agneya (South-East), ruled by Lord Agni. The cook should always face East. Second alternative is Vayavya (North-West).",
            specificRemedies = mapOf(
                "North-East" to "Critical Dosha (leads to family health & financial issues). Never keep the stove directly in NE. Place a dark green marble slab under the stove and hang a copper Surya plate.",
                "South-West" to "Causes anxiety and marital friction. Place a yellow marble slab under burner and install a brass Vastu helix.",
                "North" to "Extinguishes Kubera's wealth energy. Paint the kitchen walls in light off-white and place an Agni pyramid near the stove."
            )
        ),
        RoomTypeInfo(
            id = "master_bedroom",
            name = "Master Bedroom",
            hindiName = "मुख्य शयनकक्ष",
            mode = VastuSpaceMode.HOME,
            iconEmoji = "🛏️",
            idealDirections = listOf("South-West", "South"),
            acceptableDirections = listOf("West"),
            forbiddenDirections = listOf("North-East", "South-East", "North-West"),
            guidanceSummary = "Belongs to Nairutya (South-West), the Earth element anchor providing authority, marital stability, and deep rest to the head of the family. Head should point South or East while sleeping.",
            specificRemedies = mapOf(
                "North-East" to "Causes mental restlessness, sleep disorders, and loss of authority. Head of family should shift if possible. Otherwise, place heavy brass weights, yellow curtains, and no mirrors facing the bed.",
                "South-East" to "Agni zone creates short temper and disputes. Use soothing pastel green or cream bedsheets; keep electronic gadgets outside.",
                "North-West" to "Air element causes frequent travel or instability. Place a heavy brass tortoise and earthy brown cushions."
            )
        ),
        RoomTypeInfo(
            id = "living_room",
            name = "Living / Drawing Room",
            hindiName = "बैठक कक्ष (ड्राइंग रूम)",
            mode = VastuSpaceMode.HOME,
            iconEmoji = "🛋️",
            idealDirections = listOf("North", "East", "North-East"),
            acceptableDirections = listOf("North-West"),
            forbiddenDirections = listOf("South-West"),
            guidanceSummary = "Welcoming zone for positive cosmic prana and guests. Keep heavier sofa sets in the South and West sides, keeping North and East open and light.",
            specificRemedies = mapOf(
                "South-West" to "Can make guests overstay or dominate the household. Keep the decor light and place an aromatic essential oil diffuser."
            )
        ),
        RoomTypeInfo(
            id = "study_room",
            name = "Children / Study Room",
            hindiName = "अध्ययन कक्ष (विद्या स्थान)",
            mode = VastuSpaceMode.HOME,
            iconEmoji = "📚",
            idealDirections = listOf("East", "North-East", "North", "West"),
            acceptableDirections = listOf("North-West"),
            forbiddenDirections = listOf("South-West", "South-East"),
            guidanceSummary = "Governed by Budha (Mercury) and Saraswati. The student's study desk should face East or North to maximize memory retention and concentration.",
            specificRemedies = mapOf(
                "South-West" to "Causes lethargy or disinterest in studies. Place a Saraswati Yantra and crystal globe on the study desk, facing North-East.",
                "South-East" to "Causes hyperactivity or restlessness. Place a green aventurine crystal and study facing East."
            )
        ),
        RoomTypeInfo(
            id = "cash_locker",
            name = "Cash & Valuables Locker",
            hindiName = "तिजोरी / धन स्थान",
            mode = VastuSpaceMode.HOME,
            iconEmoji = "💎",
            idealDirections = listOf("North", "South-West"),
            acceptableDirections = listOf("East"),
            forbiddenDirections = listOf("South-East", "South", "North-West"),
            guidanceSummary = "Locker placed along the South or South-West wall must open facing North (towards Lord Kubera) for continual multiplication of wealth.",
            specificRemedies = mapOf(
                "South-East" to "Fire element burns through savings quickly. Relocate locker to North or South-West opening North. Line the inside with red velvet.",
                "North-West" to "Air element causes money to fly away fast. Place a heavy silver coin and mirror inside the locker door reflecting currency."
            )
        ),
        RoomTypeInfo(
            id = "main_entrance",
            name = "Main Entrance (Mahadwar)",
            hindiName = "मुख्य प्रवेश द्वार (महाद्वार)",
            mode = VastuSpaceMode.HOME,
            iconEmoji = "🚪",
            idealDirections = listOf("North", "East", "North-East"),
            acceptableDirections = listOf("West", "North-West"),
            forbiddenDirections = listOf("South-West", "South-East", "South"),
            guidanceSummary = "The Mahadwar is the mouth of cosmic prana influx. Must be clean, well-lit, open inward clockwise, and adorned with auspicious symbols (Om, Swastika, Toran).",
            specificRemedies = mapOf(
                "South-West" to "Severe defect (Nairutya entrance causes heavy obstacles). Install two brass Swastikas on the doorframe, a lead helix above the threshold, and a yellow floor mat.",
                "South" to "Install a Panchmukhi Hanuman tile above the door and a red copper threshold strip.",
                "South-East" to "Place a copper pyramid and brass Trishul with Swastika on the exterior doorframe."
            )
        ),
        RoomTypeInfo(
            id = "toilet",
            name = "Toilet & Bathroom",
            hindiName = "शौचालय व स्नानगृह",
            mode = VastuSpaceMode.HOME,
            iconEmoji = "🚿",
            idealDirections = listOf("North-West", "West"),
            acceptableDirections = listOf("South"),
            forbiddenDirections = listOf("North-East", "South-West", "East"),
            guidanceSummary = "Waste disposal must be located in zones of dissolution (NW or W). Commode should face North or South, never East or West.",
            specificRemedies = mapOf(
                "North-East" to "Critical Vastu Dosha. Never keep toilet in NE. If unmovable: keep lid closed, place a bowl of unrefined sea salt changed weekly, install a Vastu zinc helix, and keep an indoor spider plant.",
                "South-West" to "Drains household savings and vitality. Place a bronze or lead plate outside the door and keep sea salt bowl inside.",
                "East" to "Blocks vital solar prana. Paint toilet door brown and place a mirror on the outside door reflecting back into hallway."
            )
        ),
        RoomTypeInfo(
            id = "staircase",
            name = "Staircase (Heavy Structure)",
            hindiName = "सीढ़ियाँ (भारी निर्माण)",
            mode = VastuSpaceMode.HOME,
            iconEmoji = "🪜",
            idealDirections = listOf("South", "South-West", "West"),
            acceptableDirections = listOf("North-West"),
            forbiddenDirections = listOf("North-East", "North", "East"),
            guidanceSummary = "Stairs represent weight and grounded earth energy. Must climb clockwise (Pradakshina). Never build stairs in the sacred North-East or Brahmasthan.",
            specificRemedies = mapOf(
                "North-East" to "Crushes divine Ishanya energy. Place a brass tortoise at bottom step, hang a concave Vastu mirror, and paint walls light cream.",
                "North" to "Blocks wealth inflow. Keep the staircase brightly lit and install a copper Swastika on the first riser."
            )
        ),
        RoomTypeInfo(
            id = "water_tank",
            name = "Overhead / Underground Water",
            hindiName = "जल संग्रह (टंकी / बोरवेल)",
            mode = VastuSpaceMode.HOME,
            iconEmoji = "💧",
            idealDirections = listOf("North-East", "North", "West"),
            acceptableDirections = listOf("East"),
            forbiddenDirections = listOf("South-East", "South-West", "South"),
            guidanceSummary = "Underground water / borewell is ideal in North-East or North. Overhead heavy water tanks belong strictly in South-West or West.",
            specificRemedies = mapOf(
                "South-East" to "Water-fire clash. If underground tank is here, cover it and paint lid green.",
                "South-West" to "Underground water in SW causes severe destabilization. Seal and relocate; if overhead tank, SW is actually auspicious!"
            )
        )
    )

    val OFFICE_ROOMS = listOf(
        RoomTypeInfo(
            id = "ceo_cabin",
            name = "Director / CEO / MD Cabin",
            hindiName = "निदेशक / मुख्य कार्यकारी कक्ष",
            mode = VastuSpaceMode.OFFICE,
            iconEmoji = "👔",
            idealDirections = listOf("South-West", "South"),
            acceptableDirections = listOf("West"),
            forbiddenDirections = listOf("North-East", "North-West", "South-East"),
            guidanceSummary = "The principal authority should sit in the South-West corner facing North or East. The back must be supported by a solid wall without glass windows.",
            specificRemedies = mapOf(
                "North-East" to "Makes the leader overly emotional or overwhelmed by micro-issues. Shift to SW if possible; keep a heavy brass paperweight and solid brown screen behind chair.",
                "North-West" to "Creates restless indecisiveness and frequent turnover in leadership. Anchor desk with a heavy earth crystal (yellow jasper).",
                "South-East" to "Causes aggressive confrontations and stress. Place a tranquil bamboo plant and face East."
            )
        ),
        RoomTypeInfo(
            id = "accounts_dept",
            name = "Accounts & Finance Desk",
            hindiName = "लेखा व वित्त विभाग (कुबेर स्थान)",
            mode = VastuSpaceMode.OFFICE,
            iconEmoji = "💰",
            idealDirections = listOf("North", "East"),
            acceptableDirections = listOf("North-East"),
            forbiddenDirections = listOf("South", "South-West", "South-East"),
            guidanceSummary = "Accounts team must be situated in the North zone, ruled by Kubera and Mercury. Financial records and cash safe should face North.",
            specificRemedies = mapOf(
                "South-East" to "Leads to excessive burn rate and unforeseen expenditures. Place a green table mat and silver coin under accountant's laptop.",
                "South" to "Stagnates financial liquidity. Hang a framed certificate of achievements and keep desk facing North."
            )
        ),
        RoomTypeInfo(
            id = "sales_marketing",
            name = "Sales & Business Development",
            hindiName = "विक्रय व विपणन विभाग (वायु कोण)",
            mode = VastuSpaceMode.OFFICE,
            iconEmoji = "📈",
            idealDirections = listOf("North-West", "North"),
            acceptableDirections = listOf("East"),
            forbiddenDirections = listOf("South-West", "South"),
            guidanceSummary = "Belongs in Vayavya (North-West), governed by the Air element. Stimulates active outreach, prompt client responses, and speedy sales deal closures.",
            specificRemedies = mapOf(
                "South-West" to "Sales team becomes too rigid, slow, or complacent. Place a brass horse figurine galloping inward on the sales manager's desk.",
                "South" to "Creates inertia in customer prospecting. Keep a spinning globe or metal desk mobile in the North-West corner of the hall."
            )
        ),
        RoomTypeInfo(
            id = "reception_entrance",
            name = "Main Reception & Entrance",
            hindiName = "स्वागत कक्ष व मुख्य प्रवेश",
            mode = VastuSpaceMode.OFFICE,
            iconEmoji = "🏢",
            idealDirections = listOf("East", "North", "North-East"),
            acceptableDirections = listOf("North-West"),
            forbiddenDirections = listOf("South-West", "South"),
            guidanceSummary = "First impression for clients and cosmic energy. Receptionist should sit facing East or North. Keep backdrop bright and clutter-free.",
            specificRemedies = mapOf(
                "South-West" to "Visitors may feel uneasy or aggressive. Place a soothing indoor water fountain and warm yellow lighting.",
                "South" to "Install a copper Swastika emblem and bright ambient spotlights."
            )
        ),
        RoomTypeInfo(
            id = "server_it_room",
            name = "IT Server & Power Equipment",
            hindiName = "सर्वर रूम व विद्युत उपकरण",
            mode = VastuSpaceMode.OFFICE,
            iconEmoji = "🖥️",
            idealDirections = listOf("South-East"),
            acceptableDirections = listOf("North-West"),
            forbiddenDirections = listOf("North-East", "North", "South-West"),
            guidanceSummary = "Electronic servers, electrical transformers, and UPS power units generate intense heat and belong to Agneya (South-East).",
            specificRemedies = mapOf(
                "North-East" to "Server crashes and frequent technical bugs due to water-fire clash. Move if possible; otherwise, install red accents and a copper pyramid.",
                "North" to "Blocks the flow of wealth opportunities. Paint rack enclosure light off-white and keep fire extinguishers readily accessible."
            )
        ),
        RoomTypeInfo(
            id = "conference_room",
            name = "Conference & Boardroom",
            hindiName = "सम्मेलन व बोर्डरूम",
            mode = VastuSpaceMode.OFFICE,
            iconEmoji = "🤝",
            idealDirections = listOf("North-West", "East", "West"),
            acceptableDirections = listOf("North"),
            forbiddenDirections = listOf("South-West", "South-East"),
            guidanceSummary = "North-West fosters cordial agreements and negotiation win-wins. Meeting table should be rectangular or oval, never L-shaped.",
            specificRemedies = mapOf(
                "South-East" to "Meetings end in hot debates and arguments. Place fresh flowers and crystal water glasses on the table center.",
                "South-West" to "Causes rigid deadlock where neither party concedes. Place an open world map on the East wall."
            )
        ),
        RoomTypeInfo(
            id = "staff_workstations",
            name = "Staff Workstations",
            hindiName = "कर्मचारी कार्यस्थल",
            mode = VastuSpaceMode.OFFICE,
            iconEmoji = "💻",
            idealDirections = listOf("North", "East"),
            acceptableDirections = listOf("West", "North-West"),
            forbiddenDirections = listOf("South-East"),
            guidanceSummary = "Employees should face North or East while typing for optimal focus, mental clarity, and energetic throughput without afternoon fatigue.",
            specificRemedies = mapOf(
                "South" to "Causes fatigue and burn-out. Ensure desk partition has light green or white pinboards; place small indoor jade plants."
            )
        ),
        RoomTypeInfo(
            id = "pantry_cafe",
            name = "Office Pantry & Cafeteria",
            hindiName = "पेंट्री व कैफेटेरिया",
            mode = VastuSpaceMode.OFFICE,
            iconEmoji = "☕",
            idealDirections = listOf("South-East"),
            acceptableDirections = listOf("North-West"),
            forbiddenDirections = listOf("North-East", "South-West"),
            guidanceSummary = "Coffee machines, microwaves, and kettles embody fire and belong in South-East. Water dispensers should sit on the North or East wall.",
            specificRemedies = mapOf(
                "North-East" to "Severe clash. Move microwave away from the NE corner; place a copper plate under the coffee station."
            )
        ),
        RoomTypeInfo(
            id = "dispatch_warehouse",
            name = "Finished Goods & Dispatch",
            hindiName = "तैयार माल व प्रेषण (डिस्पैच)",
            mode = VastuSpaceMode.OFFICE,
            iconEmoji = "📦",
            idealDirections = listOf("North-West"),
            acceptableDirections = listOf("North"),
            forbiddenDirections = listOf("South-West", "South"),
            guidanceSummary = "Finished goods ready for sale belong in Vayavya (North-West) so they move quickly and do not get stuck in dead inventory.",
            specificRemedies = mapOf(
                "South-West" to "Inventory stays stuck for months. Relocate dispatch inventory to North-West; keep heavy raw materials in South-West."
            )
        )
    )

    fun evaluateRoomDirection(room: RoomTypeInfo, directionName: String): VastuEvaluationResult {
        val suitability = when {
            room.idealDirections.contains(directionName) -> DirectionSuitability.EXCELLENT
            room.acceptableDirections.contains(directionName) -> DirectionSuitability.ACCEPTABLE
            else -> DirectionSuitability.DOSHA
        }

        val explanation = when (suitability) {
            DirectionSuitability.EXCELLENT -> {
                "Optimal alignment! $directionName provides natural harmonic resonance for ${room.name}. ${room.guidanceSummary}"
            }
            DirectionSuitability.ACCEPTABLE -> {
                "Permissible alignment. $directionName is functional for ${room.name}, provided clutter is controlled and primary functional axes face North or East."
            }
            DirectionSuitability.DOSHA -> {
                "Vastu Incompatibility detected. Positioning ${room.name} in $directionName creates energetic friction with the presiding elemental forces."
            }
        }

        val remedy = if (suitability == DirectionSuitability.DOSHA) {
            room.specificRemedies[directionName]
                ?: "Classical Shastra remedy: Place a copper Swastika or elemental crystal pyramid in this quadrant, keep the area clean and well-ventilated, and align internal seating/activity facing East or North."
        } else null

        return VastuEvaluationResult(
            room = room,
            direction = directionName,
            suitability = suitability,
            explanation = explanation,
            recommendedRemedy = remedy,
            bestAlternativeDirections = room.idealDirections
        )
    }

    fun getRoomsForMode(mode: VastuSpaceMode): List<RoomTypeInfo> {
        return when (mode) {
            VastuSpaceMode.HOME -> HOME_ROOMS
            VastuSpaceMode.OFFICE -> OFFICE_ROOMS
        }
    }
}
