package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engines.jyotisha.ShodashvargaEngine
import com.example.ui.theme.*

@Composable
fun ShodashvargaGridSelectorCard(
    selectedVarga: ShodashvargaEngine.VargaType,
    onVargaSelected: (ShodashvargaEngine.VargaType) -> Unit,
    modifier: Modifier = Modifier
) {
    val allVargas = remember { ShodashvargaEngine.VargaType.values().toList() }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("shodashvarga_grid_selector_card")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.shodashvarga_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "16 Divisional Charts",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DarkTextSecondary,
                            fontSize = 9.5.sp
                        )
                    )
                }

                Surface(
                    color = MaroonPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, MaroonPrimary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${selectedVarga.code} • ${selectedVarga.title}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                }
            }

            // Horizontally scrollable single-row compact chips of all 16 Vargas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allVargas.forEach { varga ->
                    val isSelected = varga == selectedVarga
                    Surface(
                        color = if (isSelected) MaroonPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = if (isSelected) MaroonPrimary else LightBorder
                        ),
                        modifier = Modifier.clickable { onVargaSelected(varga) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = varga.code,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else MaroonPrimary
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = varga.title,
                                fontSize = 9.5.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Compact 1-line significance description
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${selectedVarga.sanskritName}: ${selectedVarga.signification}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSecondary,
                        fontSize = 9.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
