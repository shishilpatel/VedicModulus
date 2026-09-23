package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PanchangaData
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.AuspiciousGreen
import com.example.ui.theme.InauspiciousRed

@Composable
fun PanchangaWidgetsGrid(panchanga: PanchangaData) {
    var expandedWidget by remember { mutableStateOf<String?>(null) }

    val widgets = listOf(
        WidgetData(
            title = "Tithi",
            value = panchanga.tithi.name,
            devanagari = panchanga.tithi.devanagari,
            description = "The lunar day. ${panchanga.tithi.name} belongs to ${panchanga.tithi.paksha.name} Paksha.",
            isAuspicious = panchanga.tithi.number !in listOf(4, 9, 14, 15, 30) // general rule of thumb for Rikta tithis and Amavasya
        ),
        WidgetData(
            title = "Nakshatra",
            value = panchanga.nakshatra.sanskritName,
            devanagari = "",
            description = "The lunar mansion. ${panchanga.nakshatra.sanskritName} is ruled by ${panchanga.nakshatra.rulingGraha.displayName}.",
            isAuspicious = true // Varies greatly, simplified here
        ),
        WidgetData(
            title = "Yoga",
            value = panchanga.yoga,
            devanagari = "",
            description = "The solar-lunar relationship. ${panchanga.yoga} yoga influences the day's overall mood and activities.",
            isAuspicious = panchanga.yoga !in listOf("Vaidhriti", "Vyatipata", "Vishkumbha", "Atiganda", "Shula", "Ganda", "Vyaghata", "Vajra", "Parigha")
        ),
        WidgetData(
            title = "Karana",
            value = panchanga.karana,
            devanagari = "",
            description = "Half of a Tithi. ${panchanga.karana} Karana suggests specific favorable actions for this half-day.",
            isAuspicious = panchanga.karana !in listOf("Vishti", "Shakuni", "Chatushpada", "Naga", "Kintughna") // Vishti/Bhadra is generally inauspicious
        )
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Today's Panchanga Elements",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaroonPrimary
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WidgetCard(
                data = widgets[0],
                isExpanded = expandedWidget == "Tithi",
                onClick = { expandedWidget = if (expandedWidget == "Tithi") null else "Tithi" },
                modifier = Modifier.weight(1f)
            )
            WidgetCard(
                data = widgets[1],
                isExpanded = expandedWidget == "Nakshatra",
                onClick = { expandedWidget = if (expandedWidget == "Nakshatra") null else "Nakshatra" },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WidgetCard(
                data = widgets[2],
                isExpanded = expandedWidget == "Yoga",
                onClick = { expandedWidget = if (expandedWidget == "Yoga") null else "Yoga" },
                modifier = Modifier.weight(1f)
            )
            WidgetCard(
                data = widgets[3],
                isExpanded = expandedWidget == "Karana",
                onClick = { expandedWidget = if (expandedWidget == "Karana") null else "Karana" },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

data class WidgetData(
    val title: String,
    val value: String,
    val devanagari: String,
    val description: String,
    val isAuspicious: Boolean
)

@Composable
private fun WidgetCard(
    data: WidgetData,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isExpanded) OrangeAccent else LightBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (data.isAuspicious) AuspiciousGreen else InauspiciousRed, shape = androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = data.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DarkTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = data.value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaroonPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                textAlign = TextAlign.Center
            )
            
            if (data.devanagari.isNotBlank()) {
                Text(
                    text = data.devanagari,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OrangeAccent,
                        fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
            
            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = LightBorder,
                    thickness = 0.5.dp
                )
                Text(
                    text = data.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (data.isAuspicious) "Favorable" else "Exercise Caution",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (data.isAuspicious) AuspiciousGreen else InauspiciousRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
