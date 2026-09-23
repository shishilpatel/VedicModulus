package com.example.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.example.core.language.MLKitTranslationService

/**
 * Remembers a translated string using on-device ML Kit Translate.
 */
@Composable
fun rememberMLKitTranslated(
    text: String,
    targetLanguage: String,
    sourceLanguage: String = "en"
): State<String> {
    return produceState(initialValue = text, key1 = text, key2 = targetLanguage) {
        if (targetLanguage == sourceLanguage || text.isBlank()) {
            value = text
        } else {
            value = MLKitTranslationService.translateText(
                text = text,
                targetLanguageCode = targetLanguage,
                sourceLanguageCode = sourceLanguage
            )
        }
    }
}

/**
 * A Jetpack Compose Text component that automatically translates its content
 * to the target language via Google ML Kit On-Device Translation.
 */
@Composable
fun MLKitTranslatedText(
    text: String,
    targetLanguage: String,
    modifier: Modifier = Modifier,
    sourceLanguage: String = "en",
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current
) {
    val translated = rememberMLKitTranslated(
        text = text,
        targetLanguage = targetLanguage,
        sourceLanguage = sourceLanguage
    )

    Text(
        text = translated.value,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        style = style
    )
}
