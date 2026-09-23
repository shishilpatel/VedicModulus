package com.example.ui.screens.tabs

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.core.model.BirthProfile
import com.example.core.model.Graha
import com.example.core.model.NumerologySystem
import com.example.core.model.PersonalizedProfile
import com.example.data.room.PredictionCacheEntity
import com.example.data.room.VedicDatabase
import com.example.engines.numerology.NumerologyEngine
import com.example.ui.components.VedicDatePickerField
import com.example.ui.theme.*
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumerologyAiTab(
    personalizedProfile: PersonalizedProfile,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val numerologyEngine = remember { NumerologyEngine() }

    val initialProfile = personalizedProfile.userBirthProfile
    var inputName by remember { mutableStateOf(initialProfile.fullName.ifBlank { "Seeker" }) }
    var activeDate by remember { mutableStateOf(initialProfile.birthDate) }
    var selectedSystem by remember { mutableStateOf(initialProfile.numerologySystem) }

    // Dynamic Live Numerology Calculations
    val calculatedProfile = remember(activeDate, inputName, selectedSystem) {
        numerologyEngine.calculateNumerology(
            birthDate = activeDate,
            fullName = inputName.ifBlank { "Seeker" },
            system = selectedSystem
        )
    }

    val lifePathNumber = calculatedProfile.lifePathNumber
    val expressionNumber = calculatedProfile.destinyNameNumber
    val mulankNumber = calculatedProfile.birthNumber

    // Gemini Generation State
    var aiReading by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var wasGeneratedFromGemini by remember { mutableStateOf(false) }

    // Generate Numerology Analysis function
    val generateAnalysis: () -> Unit = {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch (_: Exception) { "" }

            val systemName = if (selectedSystem == NumerologySystem.CHALDEAN) "Chaldean (Vedic)" else "Pythagorean (Western)"
            val lifePathGraha = getGrahaNameForNumber(lifePathNumber)
            val expressionGraha = getGrahaNameForNumber(expressionNumber)
            val mulankGraha = getGrahaNameForNumber(mulankNumber)

            val targetLanguageName = com.example.core.language.LanguageManager.getLanguagePromptName(currentLanguage)
            val variableSelectedLanguage = com.example.core.language.LanguageManager.getVariableSelectedLanguage(currentLanguage)
            val responseDirective = "RESPONSE SHOULD BE IN $variableSelectedLanguage"

            val systemPrompt = "You are a revered Vedic Astrologer and Master Numerologist specializing in both Indian Sankhya Shastra (Vedic Numerology) and Pythagorean numerology. STRICT LANGUAGE REQUIREMENT: $responseDirective. You MUST generate the ENTIRE response exclusively in $targetLanguageName ($currentLanguage) using authentic grammar and native script. Do not respond in English unless the requested language is English."
            
            val prompt = """
                Provide a deep, uplifting, insightful, and authentic personalized Numerology analysis for:
                - Name: ${inputName.trim()}
                - Date of Birth: ${activeDate.dayOfMonth}-${activeDate.monthValue}-${activeDate.year}
                - Life Path Number (Bhagyank / Destiny): $lifePathNumber (Calculated from Day + Month + Year sum)
                - Expression Number (Namank / Name Vibration): $expressionNumber (Calculated from full name using $systemName system)
                - Birth Number (Mulank / Soul Foundation): $mulankNumber (Calculated from day of birth)
                - Planetary Correspondences: Life Path is governed by $lifePathGraha, Expression by $expressionGraha, Birth Number by $mulankGraha.
                - Auspicious Colors: ${calculatedProfile.favorableColors.joinToString()}
                - Auspicious Days: ${calculatedProfile.favorableDays.joinToString()}
                - Favorable Numbers: ${calculatedProfile.favorableNumbers.joinToString()}
                
                Please structure your response with elegant Markdown headers:
                
                ### 1. 🌟 The Cosmic Trinity & Core Archetype
                Synthesize the combined cosmic energy of Birth Number $mulankNumber (Soul), Life Path Number $lifePathNumber (Destiny), and Expression Number $expressionNumber (Manifestation). What unique archetype and vibrational signature does this seeker embody?
                
                ### 2. 🧭 Life Path Number $lifePathNumber — The Soul's Highest Purpose
                Detail the primary life lessons, destiny direction, inner calling, and evolutionary path represented by Life Path Number $lifePathNumber.
                
                ### 3. 🎭 Expression Number $expressionNumber — Talents & Worldly Manifestation
                Explain how their Expression Number $expressionNumber shapes their external personality, verbal & creative talents, professional presence, and natural magnetism in the world.
                
                ### 4. ⚖️ Cosmic Synergy: Life Path ($lifePathNumber) & Expression ($expressionNumber)
                Analyze the interaction between their inner soul journey (Life Path $lifePathNumber) and their outer projection (Expression $expressionNumber). Provide practical guidance on aligning inner truth with outer action.
                
                ### 5. 💼 Career, Vocation & Material Prosperity
                Highlight optimal professional fields, leadership styles, and wealth-building approaches harmonious with numbers $lifePathNumber and $expressionNumber.
                
                ### 6. 🌈 Favorable Harmonies & Practical Spiritual Sadhana
                - Recommended Lucky Numbers, Supportive Colors, and Fortunate Days
                - Presiding Grahas ($lifePathGraha, $expressionGraha) and traditional mindful spiritual practices
                - Daily mindfulness reflection
                
                ### 7. 🕊️ Sacred Daily Affirmation
                Provide an empowering, poetic personal affirmation aligned with their numbers.
                
                Tone: Compassionate, dignified, grounded in Vedic wisdom. Never make deterministic or fearful claims. Remind the seeker that conscious effort (Purushartha) and spiritual awareness shape destiny.
                CRITICAL LANGUAGE INSTRUCTION: 
                $responseDirective
                The entire response MUST be written 100% in $targetLanguageName ($currentLanguage).
            """.trimIndent()

            try {
                var responseText: String? = null

                // Try OpenAI first
                if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
                    try {
                        responseText = withContext(Dispatchers.IO) {
                            com.example.api.generateWithOpenAI(
                                prompt = prompt,
                                apiKey = openAiKey,
                                systemPrompt = systemPrompt
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Fallback to Gemini
                if (responseText.isNullOrBlank() && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                    try {
                        val model = GenerativeModel(
                            modelName = "gemini-3.5-flash",
                            apiKey = apiKey
                        )
                        val geminiResponse = withContext(Dispatchers.IO) {
                            model.generateContent("$systemPrompt\n\n$prompt")
                        }
                        responseText = geminiResponse.text
                        if (!responseText.isNullOrBlank()) {
                            wasGeneratedFromGemini = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Authentic offline synthesis fallback if no API key or network error
                if (responseText.isNullOrBlank()) {
                    responseText = generateOfflineNumerologySynthesis(
                        name = inputName.ifBlank { "Seeker" },
                        birthDate = activeDate,
                        lifePath = lifePathNumber,
                        expression = expressionNumber,
                        mulank = mulankNumber,
                        systemName = systemName,
                        favorableColors = calculatedProfile.favorableColors,
                        favorableDays = calculatedProfile.favorableDays,
                        favorableNumbers = calculatedProfile.favorableNumbers
                    )
                    wasGeneratedFromGemini = false
                }

                aiReading = responseText

                // Cache reading to Room database for Past Readings
                withContext(Dispatchers.IO) {
                    try {
                        val dao = VedicDatabase.getDatabase(context).predictionCacheDao()
                        dao.insertPrediction(
                            PredictionCacheEntity(
                                title = "Numerology: Life Path $lifePathNumber • Expression $expressionNumber (${inputName.take(15)})",
                                predictionData = responseText ?: ""
                            )
                        )
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                errorMessage = "Unable to generate analysis: ${e.localizedMessage ?: "Unknown error"}. Please check your connection."
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2A1520), Color(0xFF160E18))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Gemini AI Numerology Analysis",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Input your date of birth and full name to calculate your sacred Life Path (Bhagyank) and Expression (Namank) numbers, and receive a comprehensive personalized Gemini reading.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE2D9E8),
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Input Controls Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Personal Birth Details",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )

                // Full Name Field (Crucial for Expression Number)
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Full Name (determines Expression Number)") },
                    placeholder = { Text("Enter your full birth name") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaroonPrimary)
                    },
                    trailingIcon = {
                        if (inputName.isNotEmpty()) {
                            IconButton(onClick = { inputName = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("numerology_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaroonPrimary,
                        focusedLabelColor = MaroonPrimary
                    )
                )

                // Date of Birth Section
                VedicDatePickerField(
                    date = activeDate,
                    onDateChange = { activeDate = it },
                    label = "Date of Birth (determines Life Path Number)",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "numerology_ai_date_picker"
                )

                // System Selector
                Column {
                    Text(
                        text = "Numerology Calculation System",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedSystem == NumerologySystem.CHALDEAN,
                            onClick = { selectedSystem = NumerologySystem.CHALDEAN },
                            label = { Text("Chaldean (Vedic)") },
                            leadingIcon = if (selectedSystem == NumerologySystem.CHALDEAN) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7),
                                selectedLabelColor = Color(0xFF92400E)
                            )
                        )
                        FilterChip(
                            selected = selectedSystem == NumerologySystem.PYTHAGOREAN,
                            onClick = { selectedSystem = NumerologySystem.PYTHAGOREAN },
                            label = { Text("Pythagorean (Western)") },
                            leadingIcon = if (selectedSystem == NumerologySystem.PYTHAGOREAN) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE0E7FF),
                                selectedLabelColor = Color(0xFF3730A3)
                            )
                        )
                    }
                }
            }
        }

        // Live Core Numbers Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Your Sacred Calculated Numbers",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )

                // Life Path Number Card (Prominent)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD97706)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lifePathNumber.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Life Path Number (Bhagyank)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Text(
                                text = "Soul Purpose & Core Destiny",
                                fontSize = 12.sp,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Formula: ${activeDate.dayOfMonth} + ${activeDate.monthValue} + ${activeDate.year} = $lifePathNumber • Ruled by ${getGrahaNameForNumber(lifePathNumber)}",
                                fontSize = 11.sp,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                }

                // Expression Number Card (Prominent)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEEF2FF),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4F46E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = expressionNumber.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Expression Number (Namank)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF3730A3)
                            )
                            Text(
                                text = "Natural Talents & Manifestation",
                                fontSize = 12.sp,
                                color = Color(0xFF4338CA),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Calculated from name '${inputName.take(15)}' in $selectedSystem • Ruled by ${getGrahaNameForNumber(expressionNumber)}",
                                fontSize = 11.sp,
                                color = Color(0xFF312E81)
                            )
                        }
                    }
                }

                // Birth Number (Mulank) Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mulankNumber.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Birth Number (Mulank)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "Innate Soul Persona & Temperament",
                                fontSize = 12.sp,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "From day of birth (${activeDate.dayOfMonth}) • Ruled by ${getGrahaNameForNumber(mulankNumber)}",
                                fontSize = 11.sp,
                                color = Color(0xFF14532D)
                            )
                        }
                    }
                }

                // Favorable Harmonies Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Lucky Numbers", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = calculatedProfile.favorableNumbers.joinToString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Favorable Days", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = calculatedProfile.favorableDays.joinToString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Auspicious Colors", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = calculatedProfile.favorableColors.firstOrNull() ?: "Gold",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }
                }
            }
        }

        // Action Button: Generate Personalized Numerology Analysis using Gemini
        Button(
            onClick = generateAnalysis,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("generate_numerology_ai_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Generating Gemini Analysis...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (aiReading != null) "Regenerate Analysis with Gemini" else "Generate Personalized Numerology Reading",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }

        // Error Banner if needed
        errorMessage?.let { errorText ->
            Surface(
                color = Color(0xFFFEE2E2),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFF87171)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = errorText, color = Color(0xFF991B1B), fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = generateAnalysis) {
                        Text("Retry", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Display AI Reading Result
        AnimatedVisibility(
            visible = aiReading != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            aiReading?.let { readingText ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title & Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Personalized Numerology Report",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaroonPrimary
                                    )
                                )
                                Text(
                                    text = if (wasGeneratedFromGemini) "✨ Powered by Gemini 3.5 Flash" else "🕉 Traditional Vedic Synthesis",
                                    fontSize = 11.sp,
                                    color = if (wasGeneratedFromGemini) Color(0xFF2563EB) else Color(0xFF059669),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Numerology Analysis", readingText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Analysis copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaroonPrimary)
                                }
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "My Vedic Numerology Analysis")
                                            putExtra(Intent.EXTRA_TEXT, "🕉 Vedic Numerology Report for $inputName:\n\nLife Path Number: $lifePathNumber\nExpression Number: $expressionNumber\nBirth Number: $mulankNumber\n\n$readingText")
                                        }
                                        val chooser = Intent.createChooser(shareIntent, "Share Numerology Reading").apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(chooser)
                                    }
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaroonPrimary)
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)

                        // Render Formatted Text
                        Text(
                            text = readingText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF1F2937),
                                lineHeight = 22.sp,
                                letterSpacing = 0.2.sp
                            )
                        )

                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)

                        // Disclaimer
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF9FAFB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Traditional Vedic Numerology (Sankhya Shastra) is intended for spiritual self-reflection, mindfulness, and contemplation. It does not provide deterministic guarantees.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF6B7280),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun getGrahaNameForNumber(num: Int): String {
    return when (num) {
        1 -> "Surya (Sun)"
        2 -> "Chandra (Moon)"
        3 -> "Guru (Jupiter)"
        4 -> "Rahu (North Node)"
        5 -> "Budha (Mercury)"
        6 -> "Shukra (Venus)"
        7 -> "Ketu (South Node)"
        8 -> "Shani (Saturn)"
        9 -> "Mangala (Mars)"
        else -> "Cosmic Alignment"
    }
}

/**
 * Authentic Vedic Numerology synthesis fallback used when offline or before API key setup.
 */
private fun generateOfflineNumerologySynthesis(
    name: String,
    birthDate: LocalDate,
    lifePath: Int,
    expression: Int,
    mulank: Int,
    systemName: String,
    favorableColors: List<String>,
    favorableDays: List<String>,
    favorableNumbers: List<Int>
): String {
    val lifePathGraha = getGrahaNameForNumber(lifePath)
    val expressionGraha = getGrahaNameForNumber(expression)
    val mulankGraha = getGrahaNameForNumber(mulank)

    val lifePathMeaning = when (lifePath) {
        1 -> "A pioneering soul destined for leadership, self-reliance, innovation, and courage. Your life path teaches you how to stand tall, initiate noble projects, and inspire others through exemplary self-mastery."
        2 -> "The path of the sacred peacemaker, diplomat, and intuitive healer. You are here to foster harmony, build bridges between conflicting views, and deepen emotional intelligence and partnership."
        3 -> "The vibrant path of the creative visionary, joyful communicator, and philosophical educator. Your mission is to uplift the world through creative expression, optimistic speech, and profound spiritual joy."
        4 -> "The path of the master architect, grounded builder, and disciplined organizer. You are called to establish solid, lasting foundations for society through integrity, perseverance, and practical structure."
        5 -> "The path of dynamic freedom, adventure, versatility, and progressive communication. You thrive in transformation, quick adaptation, and opening new doors for collective progress."
        6 -> "The path of unconditional love, harmonious responsibility, nurturing, and artistic aesthetics. Your mission centers on beautifying environments, caring for family and community, and balancing emotional giving."
        7 -> "The sacred path of the mystic thinker, researcher, spiritual introspector, and philosopher. You are destined to pierce the veil of appearance and discover deeper truth through meditation and study."
        8 -> "The path of karmic mastery, organizational authority, material abundance, and ethical justice. You learn to balance spiritual integrity with great material responsibility and leadership."
        else -> "The humanitarian path of universal compassion, wisdom, global consciousness, and selfless service. You are called to transcend narrow self-interest and radiate love for all living beings."
    }

    val expressionMeaning = when (expression) {
        1 -> "You naturally project commanding presence, original ideas, decisive action, and initiative. People look to you when courage and clear direction are required."
        2 -> "Your natural mode of expression is gracious, receptive, empathetic, and collaborative. Your gentle diplomacy and active listening open hearts effortlessly."
        3 -> "You express yourself through inspiring words, artistic flair, wit, and cheerful enthusiasm. You have a natural gift for writing, speaking, and artistic storytelling."
        4 -> "You project reliability, thoroughness, practicality, and meticulous attention to detail. Others trust your steadiness and capacity to get tangible things done."
        5 -> "Your expression is charismatic, magnetic, witty, and captivating. You are an exceptional networker, multi-tasker, and communicator who brings fresh ideas."
        6 -> "You naturally radiate warmth, empathy, aesthetic grace, and comforting reassurance. You excel in creating harmonious, welcoming spaces and solving relational discord."
        7 -> "Your presence carries depth, quiet wisdom, refinement, and intriguing mystery. You express yourself best through thoughtful analysis, research, and insightful observations."
        8 -> "You project strength, executive capability, executive acumen, and grounded ambition. You have a natural instinct for managing resources, strategy, and enterprise."
        else -> "Your natural vibration is expansive, generous, idealistic, and deeply philosophical. You inspire others by championing noble ideals and inclusive unity."
    }

    return """
### 1. 🌟 The Cosmic Trinity & Core Archetype
For $name, born on ${birthDate.dayOfMonth}-${birthDate.monthValue}-${birthDate.year}, your numerological identity is forged by a profound sacred trinity:
- **Birth Number $mulank (Soul Foundation)**: Governed by $mulankGraha, representing your essential instincts, emotional rhythm, and personal temperament.
- **Life Path Number $lifePath (Cosmic Destiny)**: Governed by $lifePathGraha, highlighting the overarching life journey, lessons, and soul purpose you came to embody.
- **Expression Number $expression (Outer Manifestation)**: Governed by $expressionGraha, calculated via the $systemName system, showing your natural gifts and how your talents manifest in the material world.

### 2. 🧭 Life Path Number $lifePath — The Soul's Highest Purpose
$lifePathMeaning

In Vedic Sankhya Shastra, Life Path $lifePath is ruled by $lifePathGraha. This planetary vibration bestows distinct karmic lessons. When you align your daily activities with the virtues of $lifePathGraha, your personal life and career flow with natural grace.

### 3. 🎭 Expression Number $expression — Talents & Worldly Manifestation
$expressionMeaning

Your name carries the vibrational frequency of Number $expression. This vibration acts as the bridge between your inner destiny ($lifePath) and the outer world. In professional life and relationships, consciously activating your $expression strengths will accelerate your success.

### 4. ⚖️ Cosmic Synergy: Life Path ($lifePath) & Expression ($expression)
The dynamic between Life Path $lifePath and Expression $expression creates your unique life rhythm. Your inner calling invites you to pursue the lessons of $lifePath, while your outward expression naturally utilizes the talents of $expression. When you coordinate both—expressing your true gifts in service of your deeper destiny—you unlock profound personal fulfillment.

### 5. 💼 Career, Vocation & Material Prosperity
With the combined influence of $lifePathGraha and $expressionGraha, you thrive in endeavors requiring integrity, clear vision, and creative execution. Build relationships based on transparency, avoid hurried decisions during retrograde transits, and invest steadily for long-term stability.

### 6. 🌈 Favorable Harmonies & Practical Spiritual Sadhana
- **Auspicious Numbers**: ${favorableNumbers.joinToString()}
- **Harmonious Days**: ${favorableDays.joinToString()}
- **Supportive Colors**: ${favorableColors.joinToString()}
- **Presiding Deities**: Connect with the benevolent energy of $lifePathGraha and $mulankGraha through morning contemplation or simple Gayatri mantras.
- **Mindful Habit**: Dedicate 10 minutes each morning in silent contemplation of your core values before checking notifications.

### 7. 🕊️ Sacred Daily Affirmation
*"I am in full harmony with the cosmic rhythms of my soul. I honor my Life Path of wisdom ($lifePath) and freely share my authentic expression ($expression) with dignity, love, and courage."*
    """.trimIndent()
}
