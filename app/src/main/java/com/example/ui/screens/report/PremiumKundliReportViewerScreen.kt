package com.example.ui.screens.report

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.core.model.Graha
import com.example.core.model.KundaliChartData
import com.example.data.repository.KundliReportRepository
import com.example.data.room.report.KundliReportEntity
import com.example.engines.report.FullReportCalculationSnapshot
import com.example.engines.report.ReportSectionContent
import com.example.engines.report.ReportSectionKey
import com.example.ui.components.NorthIndianKundaliCanvas
import com.example.ui.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumKundliReportViewerScreen(
    reportId: String,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { KundliReportRepository(context) }
    val gson = remember { Gson() }

    var reportEntity by remember { mutableStateOf<KundliReportEntity?>(null) }
    var selectedSectionKey by remember { mutableStateOf(ReportSectionKey.COVER_BIRTH_DETAILS) }
    var isExportingPdf by remember { mutableStateOf(false) }

    LaunchedEffect(reportId) {
        reportEntity = repository.getReportById(reportId)
    }

    val report = reportEntity

    if (report == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(color = MaroonPrimary)
        }
        return
    }

    val snapshot: FullReportCalculationSnapshot? = remember(report) {
        try {
            gson.fromJson(report.calculationSnapshotJson, FullReportCalculationSnapshot::class.java)
        } catch (e: Exception) {
            null
        }
    }

    val sectionsMap: Map<String, ReportSectionContent> = remember(report) {
        try {
            val type = object : TypeToken<Map<String, ReportSectionContent>>() {}.type
            gson.fromJson(report.reportSectionsJson, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("premium_kundli_report_viewer")
    ) {
        // Sticky Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(report.profileName, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                    Text("37-Section Kundli Blueprint", fontSize = 11.sp, color = GoldPrimary)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            isExportingPdf = true
                            val pdfFile = repository.exportPdfForReport(report.reportId)
                            isExportingPdf = false
                            if (pdfFile != null && pdfFile.exists()) {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Report PDF"))
                            } else {
                                Toast.makeText(context, "PDF Export Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    if (isExportingPdf) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = GoldPrimary)
                    } else {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = GoldPrimary)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaroonPrimary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
        )

        // Section Jump Bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            items(ReportSectionKey.values()) { key ->
                val isSelected = selectedSectionKey == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSectionKey = key },
                    label = { Text("${key.sectionNumber}. ${key.titleEn}", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaroonPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // Active Section Content Display
        val activeSection = sectionsMap[selectedSectionKey.name] ?: ReportSectionContent(
            sectionKey = selectedSectionKey,
            title = selectedSectionKey.titleEn,
            summaryText = "Section Content",
            detailedInterpretation = "Interpretation loading..."
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Section Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaroonPrimary.copy(alpha = 0.15f)
                        ) {
                            Text("Section ${selectedSectionKey.sectionNumber} of 37", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaroonPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(activeSection.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(activeSection.summaryText, style = MaterialTheme.typography.bodyMedium.copy(color = GoldPrimary, fontWeight = FontWeight.SemiBold))
                    }
                }
            }

            // Evidence Tags Section
            if (activeSection.evidenceTags.isNotEmpty()) {
                item {
                    Text("Astrological Calculation Evidence:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(activeSection.evidenceTags) { tag ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AuspiciousGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${tag.factor}: ${tag.subject}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // Detailed Interpretation Text Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Detailed Veda Interpretation", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(activeSection.detailedInterpretation, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp))

                        if (activeSection.disclaimerText != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Text(activeSection.disclaimerText!!, fontSize = 11.sp, color = Color(0xFF991B1B), modifier = Modifier.padding(10.dp))
                            }
                        }
                    }
                }
            }

            // Highlights & Remedies
            if (activeSection.highlights.isNotEmpty() || activeSection.remedies.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, LightBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (activeSection.highlights.isNotEmpty()) {
                                Text("Key Guidance Points", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                activeSection.highlights.forEach { point ->
                                    Text("• $point", fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }

                            if (activeSection.remedies.isNotEmpty()) {
                                if (activeSection.highlights.isNotEmpty()) Spacer(modifier = Modifier.height(12.dp))
                                Text("Prescribed Traditional Remedies", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = AuspiciousGreen))
                                Spacer(modifier = Modifier.height(6.dp))
                                activeSection.remedies.forEach { remedy ->
                                    Text("• $remedy", fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
