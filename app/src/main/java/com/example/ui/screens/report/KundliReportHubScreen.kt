package com.example.ui.screens.report

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.core.model.BirthProfile
import com.example.data.CityRepository
import com.example.data.repository.KundliReportRepository
import com.example.data.room.report.KundliReportEntity
import com.example.engines.report.ReportGenerationStatus
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliReportHubScreen(
    currentProfile: BirthProfile? = null,
    onOpenReportViewer: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { KundliReportRepository(context) }

    val savedReports by repository.getAllReports().collectAsState(initial = emptyList())

    var isGenerating by remember { mutableStateOf(false) }
    var generationProgress by remember { mutableStateOf(0) }
    var currentStepTitle by remember { mutableStateOf("") }

    val activeProfile = currentProfile ?: BirthProfile(
        fullName = "Devavratha Sharma",
        birthDate = LocalDate.of(1995, 8, 15),
        birthTime = LocalTime.of(7, 30),
        birthLocation = CityRepository.getDefaultCity()
    )

    var selectedTier by remember { mutableStateOf("PREMIUM") }
    var selectedLang by remember { mutableStateOf("EN") }
    var showAdminDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("kundli_report_hub_screen")
    ) {
        // Hero Header Banner
        Surface(
            color = MaroonPrimary,
            contentColor = OnMaroon,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GoldPrimary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, GoldPrimary),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Premium Kundli Report", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White))
                            Text("37-Section Astro Arun Pandit Style Blueprint", style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary))
                        }
                    }

                    IconButton(onClick = { showAdminDialog = true }) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Config", tint = GoldPrimary)
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Birth Time Accuracy Status Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Birth Time Accuracy Indicator", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Calculated for ${activeProfile.fullName} (${activeProfile.birthTime} • ${activeProfile.birthLocation.cityName}). Divisional charts (D9/D10/D60) require ±2 min precision.",
                                fontSize = 11.sp,
                                color = DarkTextSecondary
                            )
                        }
                    }
                }
            }

            // Create New Report Generator Box
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaroonPrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Generate New Detailed Report", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Generates 37 sections including D1 to D60 charts, Vimshottari Dasha, and personalized remedies.", fontSize = 12.sp, color = DarkTextSecondary)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tier Selection Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("BASIC" to "Basic (Free)", "DETAILED" to "Detailed", "PREMIUM" to "Premium 37-Sec").forEach { (tier, label) ->
                                val isSelected = selectedTier == tier
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTier = tier },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = DarkText
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isGenerating) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LinearProgressIndicator(
                                    progress = { generationProgress / 100f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = GoldPrimary,
                                    trackColor = MaroonPrimary.copy(alpha = 0.2f),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(currentStepTitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaroonPrimary)
                                    Text("$generationProgress%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    isGenerating = true
                                    coroutineScope.launch {
                                        repository.generateAndSaveReport(
                                            profile = activeProfile,
                                            reportTier = selectedTier,
                                            selectedLanguage = selectedLang
                                        ).collect { status ->
                                            when (status) {
                                                is ReportGenerationStatus.Progress -> {
                                                    generationProgress = status.percentage
                                                    currentStepTitle = status.currentStepTitle
                                                }
                                                is ReportGenerationStatus.Completed -> {
                                                    isGenerating = false
                                                    Toast.makeText(context, "Report Generated Successfully!", Toast.LENGTH_SHORT).show()
                                                    onOpenReportViewer(status.reportEntity.reportId)
                                                }
                                                is ReportGenerationStatus.Error -> {
                                                    isGenerating = false
                                                    Toast.makeText(context, status.errorMessage, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("generate_report_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Full Kundli Report Now", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Saved Reports Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saved Kundli Reports (${savedReports.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            if (savedReports.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, LightBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Saved Reports Yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Click 'Generate Full Kundli Report Now' above to create your first report.", fontSize = 12.sp, color = DarkTextSecondary)
                        }
                    }
                }
            } else {
                items(savedReports) { report ->
                    SavedReportCard(
                        report = report,
                        onOpen = { onOpenReportViewer(report.reportId) },
                        onSharePdf = {
                            coroutineScope.launch {
                                val pdfFile = repository.exportPdfForReport(report.reportId)
                                if (pdfFile != null && pdfFile.exists()) {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Kundli PDF"))
                                }
                            }
                        },
                        onDelete = {
                            coroutineScope.launch { repository.deleteReport(report.reportId) }
                        }
                    )
                }
            }
        }

        if (showAdminDialog) {
            ReportAdminSettingsDialog(onDismiss = { showAdminDialog = false })
        }
    }
}

@Composable
private fun SavedReportCard(
    report: KundliReportEntity,
    onOpen: () -> Unit,
    onSharePdf: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable { onOpen() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaroonPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(report.reportTier, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaroonPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(report.profileName, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Row {
                    IconButton(onClick = onSharePdf) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = MaroonPrimary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Birth: ${report.birthDate} ${report.birthTime} • ${report.birthCity}", fontSize = 11.sp, color = DarkTextSecondary)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("37 Sections Complete", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AuspiciousGreen)
                Text("Tap to View Blueprint →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
            }
        }
    }
}

@Composable
private fun ReportAdminSettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Module Admin Config", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AI Engine Model: gemini-3.5-flash", fontSize = 12.sp)
                Text("Calculation Engine: Deterministic Vedic Jyotisha Engine 1.0.0", fontSize = 12.sp)
                Text("Enabled Report Sections: All 37 Sections Active", fontSize = 12.sp)
                Text("Tier Prices: Basic (Free), Detailed ($4.99), Premium ($9.99)", fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MaroonPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}
