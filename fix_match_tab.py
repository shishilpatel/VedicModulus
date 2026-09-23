import sys

path = "app/src/main/java/com/example/ui/screens/tabs/CompatibilityMatchTab.kt"
with open(path, "r") as f:
    content = f.read()

replacement = """import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.model.BirthProfile
import com.example.core.model.PersonalizedProfile
import com.example.engines.personalization.VedicPersonalizationEngine
import com.example.engines.jyotisha.KundliMatchingEngine
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.DarkTextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun CompatibilityMatchTab(
    profile: BirthProfile,
    personalizationEngine: VedicPersonalizationEngine,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var partnerName by remember { mutableStateOf("") }
    
    var pDay by remember { mutableStateOf("01") }
    var pMonth by remember { mutableStateOf("01") }
    var pYear by remember { mutableStateOf("1990") }
    var pHour by remember { mutableStateOf("12") }
    var pMinute by remember { mutableStateOf("00") }
    
    var isCalculating by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Kundli Match & Synergy",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaroonPrimary
            )
        )
        Text(
            text = "Compare your chart with a partner to check Ashtakoota compatibility.",
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = partnerName,
            onValueChange = { partnerName = it },
            label = { Text("Partner's Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("Partner's Date of Birth", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = pDay,
                onValueChange = { if(it.length <= 2) pDay = it },
                label = { Text("DD") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = pMonth,
                onValueChange = { if(it.length <= 2) pMonth = it },
                label = { Text("MM") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = pYear,
                onValueChange = { if(it.length <= 4) pYear = it },
                label = { Text("YYYY") },
                modifier = Modifier.weight(1.2f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text("Partner's Time of Birth", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = pHour,
                onValueChange = { if(it.length <= 2) pHour = it },
                label = { Text("HH (24h)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = pMinute,
                onValueChange = { if(it.length <= 2) pMinute = it },
                label = { Text("MM") },
                modifier = Modifier.weight(1f)
            )
        }
            
        Spacer(modifier = Modifier.height(24.dp))
        
        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }
            
        Button(
            onClick = {
                val y = pYear.toIntOrNull()
                val m = pMonth.toIntOrNull()
                val d = pDay.toIntOrNull()
                val hh = pHour.toIntOrNull()
                val mm = pMinute.toIntOrNull()
                
                if (y == null || m == null || d == null || hh == null || mm == null) {
                    errorMessage = "Please enter valid date and time."
                    return@Button
                }
                errorMessage = null
                isCalculating = true
                scope.launch {
                    try {
                        val pDate = LocalDate.of(y, m, d)
                        val pTime = LocalTime.of(hh, mm)
                        val partnerBirthProfile = profile.copy(
                            fullName = if (partnerName.isBlank()) "Partner" else partnerName,
                            birthDate = pDate,
                            birthTime = pTime
                        )
                        
                        val myPers = personalizationEngine.createPersonalizedProfile(profile)
                        val pPers = personalizationEngine.createPersonalizedProfile(partnerBirthProfile)
                        
                        val matchRes = KundliMatchingEngine.matchKundlis(
                            boyRashi = myPers.natalChandraRashi,
                            boyNak = myPers.natalNakshatra,
                            boyPada = myPers.natalNakshatraPada,
                            girlRashi = pPers.natalChandraRashi,
                            girlNak = pPers.natalNakshatra,
                            girlPada = pPers.natalNakshatraPada,
                            boyLagna = myPers.natalLagnaRashi,
                            girlLagna = pPers.natalLagnaRashi
                        )
                        
                        val scoreText = matchRes.scores.joinToString("\\n") { "${it.kootaName}: ${it.pointsScored}/${it.maxPoints}" }
                        
                        resultText = "Ashtakoota Match Score for ${profile.fullName.split(" ").firstOrNull() ?: "You"} & ${partnerBirthProfile.fullName}:\\n\\n" +
                            "Score: ${matchRes.totalPoints} / 36 (${matchRes.compatibilityPercentage}%)\\n\\n" +
                            scoreText + "\\n\\n" +
                            "Manglik Compatibility: ${matchRes.manglikCompatibilityVerdict}\\n\\n" +
                            "Overall Compatibility: ${matchRes.ratingCategory}. ${matchRes.overallInterpretation}"
                    } catch (e: Exception) {
                        errorMessage = "Calculation Error: ${e.message}"
                    } finally {
                        isCalculating = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCalculating
        ) {
            Text(if (isCalculating) "Calculating Stars..." else "Calculate Match")
        }

        if (resultText.isNotBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}"""

import re
content = re.sub(r'import androidx.compose.foundation.layout.*?\n@Composable', replacement.split('@Composable')[0] + '\n@Composable', content, flags=re.DOTALL)
content = re.sub(r'@Composable\nfun CompatibilityMatchTab.*?^}', replacement.split('@Composable')[1], content, flags=re.DOTALL | re.MULTILINE)

with open(path, "w") as f:
    f.write(content)
