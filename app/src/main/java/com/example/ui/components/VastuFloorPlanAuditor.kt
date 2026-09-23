package com.example.ui.components

import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engines.vastu.DirectionSuitability
import com.example.engines.vastu.VastuSpaceOptimizationEngine
import com.example.ui.theme.*

enum class VastuZone(
    val id: String,
    val directionName: String,
    val sanskritName: String,
    val deity: String,
    val element: String,
    val gridRow: Int,
    val gridCol: Int
) {
    NORTH_WEST("NW", "North-West", "वायव्य (Vayavya)", "Vayu Dev", "Air (वायु)", 0, 0),
    NORTH("N", "North", "उत्तर (Uttara)", "Kubera / Mercury", "Water / Wealth (जल)", 0, 1),
    NORTH_EAST("NE", "North-East", "ईशान्य (Ishanya)", "Shiva / Jupiter", "Water / Ether (ईश)", 0, 2),

    WEST("W", "West", "पश्चिम (Pashchima)", "Varuna / Saturn", "Air / Space (वरुण)", 1, 0),
    BRAHMASTHAN("C", "Brahmasthan", "ब्रह्मस्थान (Kendra)", "Lord Brahma", "Cosmic Ether (आकाश)", 1, 1),
    EAST("E", "East", "पूर्व (Purva)", "Indra / Surya", "Fire / Light (इन्द्र)", 1, 2),

    SOUTH_WEST("SW", "South-West", "नैऋत्य (Nairutya)", "Nirriti / Rahu", "Earth / Prithvi (पृथ्वी)", 2, 0),
    SOUTH("S", "South", "दक्षिण (Dakshina)", "Yama / Mars", "Earth / Fire (यम)", 2, 1),
    SOUTH_EAST("SE", "South-East", "आग्नेय (Agneya)", "Agni Dev / Venus", "Fire (अग्नि)", 2, 2)
}

enum class FloorPlanRoomItem(
    val id: String,
    val displayName: String,
    val hindiName: String,
    val iconEmoji: String,
    val importanceWeight: Int
) {
    MANDIR("mandir", "Mandir / Puja", "पूजा घर", "🪔", 15),
    KITCHEN("kitchen", "Kitchen", "रसोई", "🔥", 15),
    MASTER_BED("master_bed", "Master Bedroom", "मुख्य शयनकक्ष", "🛏️", 12),
    TOILET("toilet", "Toilet / Bath", "शौचालय", "🚽", 15),
    ENTRANCE("entrance", "Main Entrance", "मुख्य द्वार", "🚪", 12),
    LIVING_ROOM("living", "Living Room", "बैठक कक्ष", "🛋️", 8),
    STUDY_ROOM("study", "Study / Kids", "अध्ययन कक्ष", "📚", 7),
    CASH_LOCKER("cash", "Cash Locker / Safe", "तिजोरी", "💰", 8),
    STAIRCASE("stairs", "Staircase / Heavy", "सीढ़ियां", "🪜", 8),
    OPEN_SPACE("open", "Open / Courtyard", "खुला आँगन", "🪴", 5)
}

data class ZoneEvaluation(
    val zone: VastuZone,
    val room: FloorPlanRoomItem,
    val suitability: DirectionSuitability,
    val scoreContribution: Double,
    val reason: String,
    val remedy: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VastuFloorPlanAuditor(
    modifier: Modifier = Modifier
) {
    // Current mapping of Zone to Room
    var zoneRoomMap by remember {
        mutableStateOf(
            mutableMapOf(
                VastuZone.NORTH_EAST to FloorPlanRoomItem.MANDIR,
                VastuZone.NORTH to FloorPlanRoomItem.LIVING_ROOM,
                VastuZone.NORTH_WEST to FloorPlanRoomItem.STUDY_ROOM,
                VastuZone.EAST to FloorPlanRoomItem.ENTRANCE,
                VastuZone.BRAHMASTHAN to FloorPlanRoomItem.OPEN_SPACE,
                VastuZone.WEST to FloorPlanRoomItem.STAIRCASE,
                VastuZone.SOUTH_EAST to FloorPlanRoomItem.KITCHEN,
                VastuZone.SOUTH to FloorPlanRoomItem.TOILET,
                VastuZone.SOUTH_WEST to FloorPlanRoomItem.MASTER_BED
            )
        )
    }

    var selectedZoneForEdit by remember { mutableStateOf<VastuZone?>(null) }

    // Evaluate all 9 zones
    val evaluations = remember(zoneRoomMap) {
        VastuZone.values().map { zone ->
            val room = zoneRoomMap[zone] ?: FloorPlanRoomItem.OPEN_SPACE
            evaluateZonePlacement(zone, room)
        }
    }

    val totalScore = remember(evaluations) {
        val totalWeight = evaluations.sumOf { it.room.importanceWeight.toDouble() }
        val earnedScore = evaluations.sumOf { it.scoreContribution * it.room.importanceWeight }
        if (totalWeight > 0) ((earnedScore / totalWeight) * 100.0).coerceIn(0.0, 100.0) else 50.0
    }

    val criticalDoshas = remember(evaluations) {
        evaluations.filter { it.suitability == DirectionSuitability.DOSHA }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Top Header Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "9-Grid Vastu Purusha Mandala",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Floor Plan & Architectural Energy Auditor",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                        )
                    }

                    Surface(
                        color = when {
                            totalScore >= 80 -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                            totalScore >= 60 -> OrangeAccent.copy(alpha = 0.15f)
                            else -> CrimsonError.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${totalScore.toInt()}%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        totalScore >= 80 -> Color(0xFF2E7D32)
                                        totalScore >= 60 -> Color(0xFFD97706)
                                        else -> CrimsonError
                                    }
                                )
                            )
                            Text(
                                text = if (totalScore >= 80) "Sattvic" else if (totalScore >= 60) "Moderate" else "Dosha Prone",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tap any grid sector below to assign room types and inspect elemental compatibility.",
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3x3 Mandala Floor Plan Visual Grid
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Label North indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "North",
                        tint = MaroonPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "NORTH (उत्तर / धन कुबेर)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3 Rows of 3 Columns
                for (row in 0..2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0..2) {
                            val zone = VastuZone.values().firstOrNull { it.gridRow == row && it.gridCol == col } ?: VastuZone.BRAHMASTHAN
                            val eval = evaluations.firstOrNull { it.zone == zone } ?: ZoneEvaluation(
                                zone = zone,
                                room = FloorPlanRoomItem.OPEN_SPACE,
                                suitability = DirectionSuitability.ACCEPTABLE,
                                scoreContribution = 5.0,
                                reason = "Balanced",
                                remedy = null
                            )
                            val isSelected = selectedZoneForEdit == zone

                            GridCellCard(
                                zone = zone,
                                evaluation = eval,
                                isSelected = isSelected,
                                onClick = { selectedZoneForEdit = zone },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(105.dp)
                            )
                        }
                    }
                }

                // South indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "South",
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SOUTH (दक्षिण / यम)",
                        fontSize = 10.sp,
                        color = DarkTextSecondary
                    )
                }
            }
        }

        // Room Assignment Picker Sheet / Dialog
        selectedZoneForEdit?.let { zone ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, OrangeAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Assign Room in ${zone.sanskritName}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaroonPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Element: ${zone.element} • Deity: ${zone.deity}",
                                style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                            )
                        }
                        IconButton(onClick = { selectedZoneForEdit = null }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_close), tint = DarkTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Flow of Room Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloorPlanRoomItem.values().forEach { roomItem ->
                            val isAssigned = (zoneRoomMap[zone] == roomItem)
                            FilterChip(
                                selected = isAssigned,
                                onClick = {
                                    val newMap = zoneRoomMap.toMutableMap()
                                    newMap[zone] = roomItem
                                    zoneRoomMap = newMap
                                },
                                label = {
                                    Text("${roomItem.iconEmoji} ${roomItem.displayName}")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = MaroonPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Critical Doshas and Remedies Section
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ARCHITECTURAL VASTU DOSHAS & REMEDIES",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaroonPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (criticalDoshas.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Exemplary Vastu Layout! No critical elemental conflicts detected in the 9-grid mandala.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        } else {
            criticalDoshas.forEach { dosha ->
                DoshaRemedyCard(dosha = dosha)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun GridCellCard(
    zone: VastuZone,
    evaluation: ZoneEvaluation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isSelected -> OrangeAccent
        evaluation.suitability == DirectionSuitability.EXCELLENT -> Color(0xFF2E7D32).copy(alpha = 0.6f)
        evaluation.suitability == DirectionSuitability.ACCEPTABLE -> Color(0xFFD97706).copy(alpha = 0.6f)
        else -> CrimsonError.copy(alpha = 0.7f)
    }

    val backgroundColor = when {
        isSelected -> OrangeAccent.copy(alpha = 0.15f)
        evaluation.suitability == DirectionSuitability.EXCELLENT -> Color(0xFF2E7D32).copy(alpha = 0.06f)
        evaluation.suitability == DirectionSuitability.ACCEPTABLE -> Color(0xFFD97706).copy(alpha = 0.06f)
        else -> CrimsonError.copy(alpha = 0.08f)
    }

    Surface(
        onClick = onClick,
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.testTag("vastu_grid_cell_${zone.id.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = zone.id,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaroonPrimary
                    )
                )
                Text(
                    text = evaluation.suitability.ratingStars,
                    fontSize = 10.sp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = evaluation.room.iconEmoji,
                    fontSize = 20.sp
                )
                Text(
                    text = evaluation.room.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
            }

            Text(
                text = zone.sanskritName.substringBefore(" "),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    color = DarkTextSecondary
                )
            )
        }
    }
}

@Composable
private fun DoshaRemedyCard(dosha: ZoneEvaluation) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚠️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${dosha.room.displayName} in ${dosha.zone.directionName} (${dosha.zone.sanskritName})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = CrimsonError,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = dosha.reason,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
            )

            dosha.remedy?.let { remedyText ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = OrangeAccent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, OrangeAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("🌿", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Classical Shastric Remedy:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary
                                )
                            )
                            Text(
                                text = remedyText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun evaluateZonePlacement(zone: VastuZone, room: FloorPlanRoomItem): ZoneEvaluation {
    return when (room) {
        FloorPlanRoomItem.MANDIR -> when (zone) {
            VastuZone.NORTH_EAST, VastuZone.NORTH, VastuZone.EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.EXCELLENT, 1.0,
                "The sacred Mandir belongs in Ishanya/North/East where cosmic light and sattva prana flourish.",
                null
            )
            VastuZone.WEST -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.7,
                "West is an acceptable alternative for Mandir if facing East while praying.",
                null
            )
            VastuZone.SOUTH_WEST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.0,
                "Mandir in Nairutya (Earth zone) creates spiritual dissonance and heavy energy.",
                "Elevate altar on a wooden platform, install a copper Shree Yantra, and light a ghee lamp facing East."
            )
            VastuZone.SOUTH_EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.2,
                "Fire zone creates mental agitation during quiet meditation.",
                "Place a silver pot of sacred water and avoid red altar cloths."
            )
            else -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.5,
                "Neutral placement. Ensure idols face West so devotee faces East.",
                null
            )
        }

        FloorPlanRoomItem.KITCHEN -> when (zone) {
            VastuZone.SOUTH_EAST, VastuZone.NORTH_WEST -> ZoneEvaluation(
                zone, room, DirectionSuitability.EXCELLENT, 1.0,
                "Agneya (SE) is ruled by Lord Agni; ideal for cooking. Cook faces East for vital health.",
                null
            )
            VastuZone.EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.7,
                "East is an auspicious secondary zone for the cooking stove.",
                null
            )
            VastuZone.NORTH_EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.0,
                "Kitchen in North-East clashes Water and Fire elements, depleting household vitality and finances.",
                "Place a dark green marble slab under the gas burner and hang a brass Surya Yantra on the East wall."
            )
            VastuZone.SOUTH_WEST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.1,
                "Fire in Nairutya causes family friction and emotional turbulence.",
                "Place a yellow Jaisalmer stone slab under the stove and install copper Vastu energy strips."
            )
            else -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.5,
                "Keep the stove positioned along the South or East counter so the cook faces East.",
                null
            )
        }

        FloorPlanRoomItem.TOILET -> when (zone) {
            VastuZone.NORTH_WEST, VastuZone.WEST, VastuZone.SOUTH -> ZoneEvaluation(
                zone, room, DirectionSuitability.EXCELLENT, 1.0,
                "Proper drainage zones for waste elimination according to classical texts.",
                null
            )
            VastuZone.NORTH_EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.0,
                "CRITICAL VASTU DOSHA: Toilet in Ishanya severely defiles the divine cosmic conduit, affecting overall well-being.",
                "Keep toilet seat cover closed. Place an open bowl of raw Himalayan rock salt in the toilet (replace weekly), and fix a zinc Vastu strip along the bathroom doorway."
            )
            VastuZone.BRAHMASTHAN -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.0,
                "CRITICAL DOSHA: Toilet in Brahmasthan pollutes the cosmic core and vitality of the entire building.",
                "Keep an active camphor lamp, diffuse holy basil/sage, and place four copper pyramids around the boundary."
            )
            VastuZone.SOUTH_EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.2,
                "Toilet in Agneya drains the life fire and financial liquidity.",
                "Fix a copper strip around the door threshold and paint walls in off-white or cream."
            )
            else -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.6,
                "Acceptable drainage. Keep toilet bowl oriented along North-South axis.",
                null
            )
        }

        FloorPlanRoomItem.MASTER_BED -> when (zone) {
            VastuZone.SOUTH_WEST, VastuZone.SOUTH -> ZoneEvaluation(
                zone, room, DirectionSuitability.EXCELLENT, 1.0,
                "Nairutya is the Earth anchor zone, granting stability, authority, and restful sleep to the head of the house.",
                null
            )
            VastuZone.WEST -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.8,
                "West is an auspicious bedroom zone bringing financial gains and contentment.",
                null
            )
            VastuZone.NORTH_EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.2,
                "Ishanya is overly sattvic and ethereal for couple's bedroom, leading to restless sleep and indecision.",
                "Keep bed in the South-West corner of the room, sleep with head towards South, and avoid mirrors facing the bed."
            )
            VastuZone.SOUTH_EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.3,
                "Fiery Agneya zone triggers irritability and short-tempered disagreements.",
                "Use soothing pastel green or pastel blue wall colors and keep electronics away from the nightstand."
            )
            else -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.6,
                "Ensure head faces South or East while sleeping.",
                null
            )
        }

        FloorPlanRoomItem.ENTRANCE -> when (zone) {
            VastuZone.NORTH, VastuZone.EAST, VastuZone.NORTH_EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.EXCELLENT, 1.0,
                "Mahadwara (Main Door) in North or East invites auspicious solar energy, prana, and prosperity.",
                null
            )
            VastuZone.WEST -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.7,
                "West entrance (3rd and 4th pada) brings commercial success.",
                null
            )
            VastuZone.SOUTH_WEST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.1,
                "Nairutya entrance leads to sudden obstacles and financial leakage.",
                "Install a Panchmukhi Hanuman sticker or brass Swastika outside the door, and place a lead metal strip under the threshold."
            )
            else -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.6,
                "Keep the entrance well-lit, clutter-free, and adorned with a traditional Toran.",
                null
            )
        }

        FloorPlanRoomItem.CASH_LOCKER -> when (zone) {
            VastuZone.NORTH, VastuZone.NORTH_EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.EXCELLENT, 1.0,
                "Treasury in North faces Lord Kubera; safe door opening towards North multiplies wealth.",
                null
            )
            VastuZone.SOUTH_WEST -> ZoneEvaluation(
                zone, room, DirectionSuitability.EXCELLENT, 0.95,
                "Locker placed along South-West wall opening towards North is considered supreme for wealth retention.",
                null
            )
            VastuZone.SOUTH_EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.2,
                "Fire zone causes rapid expenditure and cash burns.",
                "Place a green cloth inside the safe and keep a silver coin embossed with Goddess Lakshmi."
            )
            else -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.6,
                "Keep locker securely weighted and avoid placing under a structural beam.",
                null
            )
        }

        FloorPlanRoomItem.OPEN_SPACE -> when (zone) {
            VastuZone.BRAHMASTHAN -> ZoneEvaluation(
                zone, room, DirectionSuitability.EXCELLENT, 1.0,
                "The heart of the home (Brahmasthan) remains light, sacred, and unobstructed, nourishing all residents.",
                null
            )
            else -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.8,
                "Open airy spaces allow positive prana to circulate smoothly.",
                null
            )
        }

        FloorPlanRoomItem.STAIRCASE -> when (zone) {
            VastuZone.SOUTH_WEST, VastuZone.SOUTH, VastuZone.WEST -> ZoneEvaluation(
                zone, room, DirectionSuitability.EXCELLENT, 1.0,
                "Heavy staircase provides desirable mass and stability in the South and West quadrants.",
                null
            )
            VastuZone.NORTH_EAST -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.0,
                "Heavy staircase in Ishanya burdens the divine conduit, stunting mental and financial growth.",
                "Place brass Vastu pyramids under each step and keep a bright lamp illuminated in the corner."
            )
            VastuZone.BRAHMASTHAN -> ZoneEvaluation(
                zone, room, DirectionSuitability.DOSHA, 0.0,
                "Staircase in center punctures the naval of Vastu Purusha.",
                "Place eight crystal balls around the staircase and ensure immaculate cleanliness."
            )
            else -> ZoneEvaluation(
                zone, room, DirectionSuitability.ACCEPTABLE, 0.6,
                "Ensure steps ascend clockwise (pradakshina direction).",
                null
            )
        }

        else -> ZoneEvaluation(
            zone, room, DirectionSuitability.ACCEPTABLE, 0.7,
            "Harmonious room placement. Ensure proper natural lighting and ventilation.",
            null
        )
    }
}
