package com.example.ui.screens.tabs

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.example.core.model.sanatan.*
import com.example.data.sanatan.SanatanSanskritiRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class SanskritiViewSection(val title: String) {
    CHAPTERS("Curriculum"),
    GLOSSARY("Vedic Dictionary"),
    QUIZZES("Quizzes & Tests")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanatanSanskritiTab(
    dailyProfile: DailyVedicProfile?,
    personalizedProfile: PersonalizedProfile?,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { SanatanSanskritiRepository.getInstance(context) }

    val completedTopics by repo.completedTopicsFlow.collectAsState(initial = emptySet())
    val bookmarkedTerms by repo.bookmarkedTermsFlow.collectAsState(initial = emptySet())

    var activeSection by remember { mutableStateOf(SanskritiViewSection.CHAPTERS) }
    var selectedCategoryFilter by remember { mutableStateOf<SanatanCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedTopicId by remember { mutableStateOf<String?>("topic_intro_sanatan") }
    var activeQuiz by remember { mutableStateOf<SanatanQuiz?>(null) }

    val filteredTopics = remember(searchQuery, selectedCategoryFilter) {
        SanatanSanskritiContent.TOPICS.filter { topic ->
            val matchesCategory = selectedCategoryFilter == null || topic.category == selectedCategoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                    topic.title.contains(searchQuery, ignoreCase = true) ||
                    topic.devanagariTitle.contains(searchQuery, ignoreCase = true) ||
                    topic.summary.contains(searchQuery, ignoreCase = true) ||
                    topic.fullText.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val filteredGlossary = remember(searchQuery, selectedCategoryFilter) {
        SanatanSanskritiContent.GLOSSARY.filter { item ->
            val matchesCategory = selectedCategoryFilter == null || item.category == selectedCategoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                    item.term.contains(searchQuery, ignoreCase = true) ||
                    item.devanagari.contains(searchQuery, ignoreCase = true) ||
                    item.conciseMeaning.contains(searchQuery, ignoreCase = true) ||
                    item.inDepthDescription.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("sanatan_sanskriti_tab_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaroonPrimary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "ॐ",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Sanatan Sanskriti",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "सनातन संस्कृति — Timeless Cosmic Wisdom",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Foundational philosophy of the Vedas, Upanishads, Dharma, Karma, Atman, Cosmic Cycles, 16 Samskaras, and the Guru-Shishya tradition.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Metric Bar
                    val totalTopics = SanatanSanskritiContent.TOPICS.size
                    val progressRatio = (completedTopics.size.toFloat() / totalTopics).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Learning Progress: ${completedTopics.size}/$totalTopics Lessons",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${(progressRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaffronAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SaffronAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // 2. Section Navigation: Curriculum, Glossary, Quizzes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SanskritiViewSection.values().forEach { sec ->
                    val isSelected = activeSection == sec
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeSection = sec },
                        label = {
                            Text(
                                text = sec.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                when (sec) {
                                    SanskritiViewSection.CHAPTERS -> Icons.Default.MenuBook
                                    SanskritiViewSection.GLOSSARY -> Icons.Default.Spellcheck
                                    SanskritiViewSection.QUIZZES -> Icons.Default.Quiz
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sanatan_search_bar"),
                placeholder = {
                    Text(
                        when (activeSection) {
                            SanskritiViewSection.CHAPTERS -> "Search topics, Atman, Karma, Yugas, Vedas..."
                            SanskritiViewSection.GLOSSARY -> "Search Sanskrit terms (Dharma, Samskara, Ojas)..."
                            SanskritiViewSection.QUIZZES -> "Search quizzes..."
                        }
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                } else null,
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // 4. Horizontal Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All Categories") }
                    )
                }
                items(SanatanCategory.values(), key = { it.name }) { cat ->
                    val isSel = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCategoryFilter = if (isSel) null else cat },
                        label = { Text(cat.displayName) }
                    )
                }
            }
        }

        // 5. Dynamic Content Rendering
        when (activeSection) {
            SanskritiViewSection.CHAPTERS -> {
                item {
                    Text(
                        text = "Curriculum & Foundational Chapters (${filteredTopics.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(filteredTopics, key = { it.id }) { topic ->
                    val isExpanded = expandedTopicId == topic.id
                    val isDone = completedTopics.contains(topic.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topic_card_${topic.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (isExpanded) MaroonPrimary else LightBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedTopicId = if (isExpanded) null else topic.id
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = topic.category.displayName,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (isDone) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Completed",
                                                tint = AuspiciousGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = topic.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = topic.devanagariTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaroonPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(onClick = { expandedTopicId = if (isExpanded) null else topic.id }) {
                                    Icon(
                                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = topic.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    HorizontalDivider(color = LightBorder)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = topic.fullText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Scriptural Shlokas
                                    if (topic.scripturalVerses.isNotEmpty()) {
                                        Text(
                                            text = "Scriptural References & Shlokas:",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaroonPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        topic.scripturalVerses.forEach { verse ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = OffWhiteSurface,
                                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(
                                                        text = verse.devanagari,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaroonPrimary,
                                                        fontFamily = FontFamily.Serif
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = verse.englishMeaning,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = DarkText
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "— ${verse.source}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = GrayText
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Core Pillars
                                    Text(
                                        text = "Core Principles:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    topic.keyPillars.forEach { p ->
                                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                            Text("• ", color = SaffronAccent, fontWeight = FontWeight.Bold)
                                            Text(p, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                        }
                                    }

                                    // Misconceptions
                                    if (topic.commonMisconceptions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = "Misconceptions Clarified:",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = OrangeAccent
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                topic.commonMisconceptions.forEach { m ->
                                                    Text("⚡ $m", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Practical Living Tip
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AuspiciousGreen.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Spa, contentDescription = null, tint = AuspiciousGreen, modifier = Modifier.size(20.dp))
                                            Column {
                                                Text("Practical Integration:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AuspiciousGreen)
                                                Text(topic.practicalLivingTip, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Mark as Complete Button
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                repo.toggleTopicCompleted(topic.id)
                                                Toast.makeText(context, if (!isDone) "Marked as completed!" else "Progress updated", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDone) AuspiciousGreen else MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(if (isDone) Icons.Default.Check else Icons.Default.BookmarkBorder, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (isDone) "Completed (Tap to Undo)" else "Mark Chapter as Studied")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SanskritiViewSection.GLOSSARY -> {
                item {
                    Text(
                        text = "Vedic Terminology & Sanskrit Dictionary (${filteredGlossary.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(filteredGlossary, key = { it.id }) { term ->
                    val isBookmarked = bookmarkedTerms.contains(term.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("glossary_card_${term.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, LightBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = term.term,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "(${term.devanagari})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaroonPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(onClick = { scope.launch { repo.toggleBookmarkTerm(term.id) } }) {
                                    Icon(
                                        if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isBookmarked) GoldPrimary else GrayText
                                    )
                                }
                            }

                            Text(
                                text = "Pronunciation: ${term.pronunciation} • ${term.rootEtymology}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GrayText
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = term.conciseMeaning,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = term.inDepthDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = OffWhiteSurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Practical Context: ${term.practicalContext}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(8.dp),
                                    color = DarkText
                                )
                            }
                        }
                    }
                }
            }

            SanskritiViewSection.QUIZZES -> {
                item {
                    Text(
                        text = "Interactive Knowledge Tests & Quizzes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(SanatanSanskritiContent.QUIZZES, key = { it.id }) { quiz ->
                    val highestScore by repo.getQuizScoreFlow(quiz.id).collectAsState(initial = null)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_card_${quiz.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, LightBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = quiz.category.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GrayText
                                )
                                Text(
                                    text = quiz.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${quiz.questions.size} Questions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (highestScore != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Best Score: $highestScore%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (highestScore!! >= 80) AuspiciousGreen else OrangeAccent
                                    )
                                }
                            }

                            Button(
                                onClick = { activeQuiz = quiz },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (highestScore != null) "Retake" else "Start Quiz")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Interactive Quiz Dialog Runner
    activeQuiz?.let { quiz ->
        QuizRunnerDialog(
            quiz = quiz,
            onDismiss = { activeQuiz = null },
            onSaveScore = { percentage ->
                scope.launch {
                    repo.saveQuizScore(quiz.id, percentage)
                }
            }
        )
    }
}

@Composable
private fun QuizRunnerDialog(
    quiz: SanatanQuiz,
    onDismiss: () -> Unit,
    onSaveScore: (Int) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    val currentQ = quiz.questions[currentIndex]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(quiz.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (!isQuizCompleted) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${quiz.questions.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText
                    )
                }
            }
        },
        text = {
            if (isQuizCompleted) {
                val percentage = ((score.toFloat() / quiz.questions.size) * 100).toInt()
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (percentage >= 70) Icons.Default.EmojiEvents else Icons.Default.School,
                        contentDescription = null,
                        tint = if (percentage >= 70) GoldPrimary else MaroonPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Quiz Completed!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You scored $score out of ${quiz.questions.size} ($percentage%)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (percentage >= 70) AuspiciousGreen else DarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (percentage >= 70) "Excellent comprehension of Sanatan philosophy!" else "Keep reading the curriculum chapters to deepen your mastery.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = currentQ.question,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    currentQ.options.forEachIndexed { optIndex, optionText ->
                        val isChosen = selectedOption == optIndex
                        val isCorrect = optIndex == currentQ.correctIndex

                        val containerColor = when {
                            !isAnswerSubmitted && isChosen -> MaterialTheme.colorScheme.primaryContainer
                            isAnswerSubmitted && isCorrect -> AuspiciousGreen.copy(alpha = 0.2f)
                            isAnswerSubmitted && isChosen && !isCorrect -> InauspiciousRed.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val borderColor = when {
                            !isAnswerSubmitted && isChosen -> MaterialTheme.colorScheme.primary
                            isAnswerSubmitted && isCorrect -> AuspiciousGreen
                            isAnswerSubmitted && isChosen && !isCorrect -> InauspiciousRed
                            else -> LightBorder
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = containerColor,
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !isAnswerSubmitted) {
                                    selectedOption = optIndex
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A' + optIndex)}. ",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnswerSubmitted && isCorrect) AuspiciousGreen else DarkText
                                )
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (isAnswerSubmitted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OffWhiteSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (selectedOption == currentQ.correctIndex) "✓ Correct!" else "✗ Explanation:",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedOption == currentQ.correctIndex) AuspiciousGreen else InauspiciousRed,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = currentQ.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkText
                                )
                                Text(
                                    text = "Source: ${currentQ.scripturalReference}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GrayText
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isQuizCompleted) {
                Button(onClick = {
                    val percentage = ((score.toFloat() / quiz.questions.size) * 100).toInt()
                    onSaveScore(percentage)
                    onDismiss()
                }) {
                    Text("Finish & Save")
                }
            } else if (!isAnswerSubmitted) {
                Button(
                    onClick = {
                        if (selectedOption != null) {
                            isAnswerSubmitted = true
                            if (selectedOption == currentQ.correctIndex) {
                                score++
                            }
                        }
                    },
                    enabled = selectedOption != null
                ) {
                    Text("Submit Answer")
                }
            } else {
                Button(onClick = {
                    if (currentIndex < quiz.questions.size - 1) {
                        currentIndex++
                        selectedOption = null
                        isAnswerSubmitted = false
                    } else {
                        isQuizCompleted = true
                    }
                }) {
                    Text(if (currentIndex < quiz.questions.size - 1) "Next Question" else "View Results")
                }
            }
        },
        dismissButton = {
            if (!isQuizCompleted) {
                TextButton(onClick = onDismiss) {
                    Text("Quit")
                }
            }
        }
    )
}
