package com.myApp27.vocabecho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Data class representing the result of finding the cloze answer in the sentence.
 */
data class ClozeMatch(
    val found: Boolean,
    val startIndex: Int = -1,
    val endIndex: Int = -1
)

/**
 * Find the first occurrence of answer in sentence (case-insensitive).
 */
fun findClozeMatch(sentence: String, answer: String): ClozeMatch {
    if (sentence.isBlank() || answer.isBlank()) {
        return ClozeMatch(found = false)
    }
    val trimmedSentence = sentence.trim()
    val trimmedAnswer = answer.trim()
    if (trimmedAnswer.isEmpty()) {
        return ClozeMatch(found = false)
    }
    val index = trimmedSentence.indexOf(trimmedAnswer, ignoreCase = true)
    return if (index >= 0) {
        ClozeMatch(
            found = true,
            startIndex = index,
            endIndex = index + trimmedAnswer.length
        )
    } else {
        ClozeMatch(found = false)
    }
}

/**
 * Build the question text with blank replacing the first occurrence.
 */
fun buildQuestionPreview(sentence: String, match: ClozeMatch, hint: String?): String {
    if (!match.found) return ""
    val trimmedSentence = sentence.trim()
    val placeholder = if (!hint.isNullOrBlank()) {
        "[${hint.trim()}]"
    } else {
        "[...]"
    }
    return trimmedSentence.substring(0, match.startIndex) +
            placeholder +
            trimmedSentence.substring(match.endIndex)
}

/**
 * Composable that shows a live preview of CLOZE card:
 * 1. Sentence with highlighted hidden phrase
 * 2. Question preview with blank
 */
@Composable
fun ClozePreview(
    sentence: String,
    answer: String,
    hint: String?,
    modifier: Modifier = Modifier
) {
    val match = remember(sentence, answer) {
        findClozeMatch(sentence, answer)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF5F0FA))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!match.found) {
            // Neutral message when not found
            Text(
                text = "Превью появится, когда фраза найдена в тексте.",
                color = Color(0xFF888888),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            val trimmedSentence = sentence.trim()

            // 1. Preview with highlighted hidden phrase
            Text(
                text = "Превью (что будет скрыто):",
                color = Color(0xFF666666),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )

            val highlightedText = remember(trimmedSentence, match) {
                buildAnnotatedString {
                    // Before match
                    append(trimmedSentence.substring(0, match.startIndex))
                    // Highlighted match
                    withStyle(
                        SpanStyle(
                            background = Color(0xFF3B87D9).copy(alpha = 0.3f),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B4AA2)
                        )
                    ) {
                        append(trimmedSentence.substring(match.startIndex, match.endIndex))
                    }
                    // After match
                    append(trimmedSentence.substring(match.endIndex))
                }
            }

            Text(
                text = highlightedText,
                color = Color(0xFF333333),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))

            // 2. Question preview with blank
            Text(
                text = "Как увидит ребёнок:",
                color = Color(0xFF666666),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )

            val questionPreview = remember(trimmedSentence, match, hint) {
                buildQuestionPreview(sentence, match, hint)
            }

            Text(
                text = questionPreview,
                color = Color(0xFF333333),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
