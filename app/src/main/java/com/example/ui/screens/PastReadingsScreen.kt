package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.room.PredictionCacheDao
import com.example.data.room.PredictionCacheEntity
import com.example.data.room.VedicDatabase
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastReadingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { VedicDatabase.getDatabase(context) }
    val cacheDao = remember { database.predictionCacheDao() }
    val pastReadings by cacheDao.getAllPastPredictions().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var showClearDialog by remember { mutableStateOf(false) }
    var readingToDelete by remember { mutableStateOf<PredictionCacheEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredReadings = remember(pastReadings, searchQuery) {
        if (searchQuery.isBlank()) pastReadings
        else pastReadings.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.predictionData.contains(searchQuery, ignoreCase = true)
        }
    }

    // Confirmation dialog for Clear All
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.past_readings_clear_title),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "This will delete all locally cached AI life predictions from your device. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                cacheDao.clearAll()
                            }
                            showClearDialog = false
                            Toast.makeText(context, "All past readings cleared", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.past_readings_clear_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.past_readings_cancel))
                }
            }
        )
    }

    // Confirmation dialog for single delete
    readingToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { readingToDelete = null },
            title = { Text(stringResource(R.string.past_readings_delete_title), color = MaterialTheme.colorScheme.primary) },
            text = { Text(stringResource(R.string.past_readings_delete_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                cacheDao.deletePrediction(target.id)
                            }
                            readingToDelete = null
                            Toast.makeText(context, "Reading deleted", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.past_readings_delete_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { readingToDelete = null }) { Text(stringResource(R.string.past_readings_cancel)) }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.past_readings_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${pastReadings.size} cached reading${if (pastReadings.size == 1) "" else "s"} in local storage",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("action_back_past_readings")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (pastReadings.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("action_clear_all_readings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All Readings",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (pastReadings.isNotEmpty()) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.past_readings_search_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (pastReadings.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaroonPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HistoryEdu,
                                    contentDescription = null,
                                    tint = MaroonPrimary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.past_readings_empty),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.past_readings_empty_desc),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.past_readings_generate_btn), color = Color.White)
                        }
                    }
                }
            } else if (filteredReadings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No readings match '$searchQuery'",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredReadings, key = { it.id }) { reading ->
                        PastReadingCard(
                            entity = reading,
                            onDelete = { readingToDelete = reading }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PastReadingCard(
    entity: PredictionCacheEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    val formattedDate = remember(entity.timestamp) {
        try {
            val zdt = Instant.ofEpochMilli(entity.timestamp).atZone(ZoneId.systemDefault())
            zdt.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy • hh:mm a"))
        } catch (e: Exception) {
            "Saved Reading"
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = OrangeAccent.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.past_readings_vedic_reading),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = entity.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                    )
                }

                Row {
                    // Copy action
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Vedic Prediction", entity.predictionData))
                            Toast.makeText(context, "Copied reading to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Reading",
                            tint = DarkTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Share action
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, entity.title)
                                putExtra(Intent.EXTRA_TEXT, "${entity.title}\n$formattedDate\n\n${entity.predictionData}")
                            }
                            val chooser = Intent.createChooser(shareIntent, "Share Vedic Reading").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(chooser)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Reading",
                            tint = DarkTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete action
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Reading",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = LightBorder
            )

            // Content preview or full content
            val text = entity.predictionData.trim()
            val sections = text.split(Regex("(?=1\\.|2\\.|3\\.)"))

            if (!isExpanded) {
                // Show snippet
                Text(
                    text = text.replace(Regex("\\*\\*"), ""),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                // Expanded full formatted view
                if (sections.isEmpty() || sections.size == 1) {
                    Text(
                        text = text.replace(Regex("\\*\\*"), ""),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        sections.forEach { sectionText ->
                            val trimmed = sectionText.trim()
                            if (trimmed.isNotEmpty()) {
                                val lines = trimmed.split("\n")
                                val titleRaw = lines.firstOrNull() ?: "Reading"
                                val title = titleRaw.replace(Regex("^\\d+\\.\\s*\\*?\\*?|\\*?\\*?$"), "").trim()
                                val body = lines.drop(1).joinToString("\n").trim().replace("**", "")

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaroonPrimary
                                            )
                                        )
                                        if (body.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = body,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    lineHeight = 20.sp,
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
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expand/Collapse toggle button
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isExpanded) "Collapse Reading" else "Read Full Reading",
                    color = MaroonPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaroonPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
