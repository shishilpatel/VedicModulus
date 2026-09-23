import re

with open('app/src/main/java/com/example/ui/screens/tabs/LifePredictionsTab.kt', 'r') as f:
    text = f.read()

# Add imports
imports = """import androidx.compose.ui.platform.LocalContext
import com.example.data.room.VedicDatabase
import com.example.data.room.PredictionCacheEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
"""
text = text.replace("import kotlinx.coroutines.launch\n", "import kotlinx.coroutines.launch\n" + imports)

# Setup context and dao
setup = """    val scope = rememberCoroutineScope()
    
    val context = LocalContext.current
    val cacheDao = remember { VedicDatabase.getDatabase(context).predictionCacheDao() }"""
text = text.replace("    val scope = rememberCoroutineScope()", setup)

# Update generatePrediction success path
target_success = """                    predictionData = responseText ?: "Could not generate prediction. Please try again."
                } catch (e: Exception) {"""

replacement_success = """                    predictionData = responseText ?: "Could not generate prediction. Please try again."
                    if (responseText != null) {
                        withContext(Dispatchers.IO) {
                            cacheDao.insertPrediction(PredictionCacheEntity(predictionData = responseText))
                        }
                    }
                } catch (e: Exception) {"""
text = text.replace(target_success, replacement_success)

# Update generatePrediction failure path
target_failure = """                } catch (e: Exception) {
                    errorMessage = null // Silently provide fallback
                    predictionData = generateFallbackPrediction(profile, personalizedProfile)
                } finally {"""

replacement_failure = """                } catch (e: Exception) {
                    val cached = withContext(Dispatchers.IO) { cacheDao.getLatestPrediction().firstOrNull() }
                    if (cached != null) {
                        predictionData = cached.predictionData
                        errorMessage = null // Silent fallback to cache
                    } else {
                        errorMessage = null // Silently provide fallback
                        predictionData = generateFallbackPrediction(profile, personalizedProfile)
                    }
                } finally {"""
text = text.replace(target_failure, replacement_failure)

# Update LaunchedEffect
target_effect = """    LaunchedEffect(profile) {
        if (predictionData == null && !isLoading && errorMessage == null) {
            generatePrediction()
        }
    }"""

replacement_effect = """    LaunchedEffect(profile) {
        if (predictionData == null && !isLoading && errorMessage == null) {
            val cached = withContext(Dispatchers.IO) { cacheDao.getLatestPrediction().firstOrNull() }
            if (cached != null) {
                predictionData = cached.predictionData
            } else {
                generatePrediction()
            }
        }
    }"""
text = text.replace(target_effect, replacement_effect)

with open('app/src/main/java/com/example/ui/screens/tabs/LifePredictionsTab.kt', 'w') as f:
    f.write(text)

