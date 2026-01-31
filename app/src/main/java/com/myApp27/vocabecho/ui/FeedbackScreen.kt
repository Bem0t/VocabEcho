package com.myApp27.vocabecho.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.myApp27.vocabecho.R
import com.myApp27.vocabecho.data.DeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import com.myApp27.vocabecho.data.progress.ProgressRepository
import com.myApp27.vocabecho.data.settings.ParentSettingsRepository
import com.myApp27.vocabecho.domain.answer.AnswerNormalizer
import com.myApp27.vocabecho.domain.model.Grade
import com.myApp27.vocabecho.domain.model.ParentSettings
import com.myApp27.vocabecho.domain.time.TimeProvider
import kotlinx.coroutines.launch

@Composable
fun FeedbackScreen(
    deckId: String,
    cardId: String,
    userAnswer: String,
    onNext: () -> Unit
) {
    val context = LocalContext.current

    val deckRepo = remember { DeckRepository(context) }
    val db = remember { DatabaseProvider.get(context) }
    val progressRepo = remember { ProgressRepository(db.cardProgressDao()) }
    val settingsRepo = remember { ParentSettingsRepository(context) }

    val settings by settingsRepo.settingsFlow.collectAsState(initial = ParentSettings())
    val scope = rememberCoroutineScope()

    val deck = remember(deckId) { deckRepo.loadDeck(deckId) }
    val card = remember(deck, cardId) { deck?.cards?.firstOrNull { it.id == cardId } }

    if (deck == null || card == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Ошибка: данные не найдены")
        }
        return
    }

    // ✅ Счётчик должен быть как на Learn: сколько слов "на сегодня", а не всего в колоде.
    // Берём из прогресса и dueEpochDay.
    var dueToday by remember { mutableStateOf(0) }
    var newCount by remember { mutableStateOf(0) }
    LaunchedEffect(deckId) {
        val today = TimeProvider.todayEpochDay()
        val progress = progressRepo.getAllForDeck(deckId)
        val byId = progress.associateBy { it.cardId }

        dueToday = deck.cards.count { c ->
            val p = byId[c.id]
            p != null && p.dueEpochDay <= today
        }

        newCount = deck.cards.count { c ->
            byId[c.id] == null
        }
    }

    val todayTotal = (dueToday + newCount).coerceAtLeast(1)

    // (приблизительно) "сделано сегодня" = сколько оценок уже есть + 1 текущая, но не больше todayTotal
    // Это не идеальный "done", но будет выглядеть адекватно и НЕ будет показывать total колоды.
    var learnedCount by remember { mutableStateOf(0) }
    LaunchedEffect(deckId) {
        learnedCount = progressRepo.getAllForDeck(deckId).size
    }
    val done = (learnedCount + 1).coerceAtMost(todayTotal)

    val correct = card.back
    val isCorrect = AnswerNormalizer.isCorrect(userAnswer, correct)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_main),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ теперь счётчик "на сегодня", а не total колоды
                Capsule(text = "${done}/${todayTotal}")

                Spacer(Modifier.weight(1f))

                Capsule(text = "${deckEmoji(deckId)} ${deck.title}")

                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(56.dp))
            }

            Spacer(Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E9D8)),
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .fillMaxWidth(0.86f)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ✅ 1) ответ ребёнка: фон НЕ красный/зелёный, а нейтральный
                    ResultRow(
                        text = buildLetterDiffAnnotated(
                            user = userAnswer,
                            correct = correct
                        ),
                        rightEmoji = if (isCorrect) "✅" else "❌",
                        container = Color(0x18FFFFFF) // нейтральный светлый
                    )

                    // ✅ 2) правильный ответ: можно оставить лёгкий зелёный (как подсказка)
                    ResultRow(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFF1E8E3E),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append(correct.trim().ifBlank { "—" })
                            }
                        },
                        rightEmoji = "✅",
                        container = Color(0x141E8E3E) // очень мягкий зелёный
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ✅ Кнопки сложности: такие же "красивые", как на Learn (белая рамка + тень)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CuteButton(
                    text = "😣 Снова",
                    background = Color(0xFFF05A3A),
                    modifier = Modifier.weight(1f),
                    enabled = true,
                    onClick = {
                        scope.launch {
                            progressRepo.gradeCard(
                                deckId = deckId,
                                cardId = cardId,
                                todayEpochDay = TimeProvider.todayEpochDay(),
                                grade = Grade.AGAIN,
                                settings = settings
                            )
                            onNext()
                        }
                    }
                )

                CuteButton(
                    text = "🙂 Сложно",
                    background = Color(0xFFF4B63A),
                    modifier = Modifier.weight(1f),
                    enabled = true,
                    onClick = {
                        scope.launch {
                            progressRepo.gradeCard(
                                deckId = deckId,
                                cardId = cardId,
                                todayEpochDay = TimeProvider.todayEpochDay(),
                                grade = Grade.HARD,
                                settings = settings
                            )
                            onNext()
                        }
                    }
                )

                CuteButton(
                    text = "😄 Легко",
                    background = Color(0xFF3B87D9),
                    modifier = Modifier.weight(1f),
                    enabled = true,
                    onClick = {
                        scope.launch {
                            progressRepo.gradeCard(
                                deckId = deckId,
                                cardId = cardId,
                                todayEpochDay = TimeProvider.todayEpochDay(),
                                grade = Grade.EASY,
                                settings = settings
                            )
                            onNext()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ResultRow(
    text: androidx.compose.ui.text.AnnotatedString,
    rightEmoji: String,
    container: Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = rightEmoji,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Подсветка букв: верные -> зелёные, неверные -> красные.
 * Сравнение по позиции.
 */
private fun buildLetterDiffAnnotated(user: String, correct: String) = buildAnnotatedString {
    val uDisplay = user.trim()
    val cDisplay = correct.trim()

    val c = AnswerNormalizer.normalize(cDisplay)

    if (uDisplay.isBlank()) {
        withStyle(SpanStyle(color = Color(0xFFCC3333), fontWeight = FontWeight.ExtraBold)) {
            append("—")
        }
        return@buildAnnotatedString
    }

    val raw = uDisplay
    val rawLower = raw.lowercase()

    for (i in raw.indices) {
        val ch = raw[i]
        val isSpace = ch.isWhitespace()

        val ok = if (isSpace) {
            true
        } else {
            val ul = rawLower.getOrNull(i)
            val cl = c.getOrNull(i)
            ul != null && cl != null && ul == cl
        }

        val color = when {
            isSpace -> Color(0xFF0B4AA2)
            ok -> Color(0xFF1E8E3E)
            else -> Color(0xFFCC3333)
        }

        withStyle(SpanStyle(color = color, fontWeight = FontWeight.ExtraBold)) {
            append(ch)
        }
    }
}

@Composable
private fun Capsule(text: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

/** Кнопка с белой рамкой + тенью (как на LearnScreen) */
@Composable
private fun CuteButton(
    text: String,
    background: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shapeOuter = RoundedCornerShape(18.dp)
    val shapeInner = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .height(52.dp)
            .shadow(12.dp, shapeOuter)
            .background(Color.White, shapeOuter)
            .padding(4.dp)
            .then(if (enabled) Modifier.clickableNoRipple(onClick) else Modifier)
    ) {
        Card(
            shape = shapeInner,
            colors = CardDefaults.cardColors(containerColor = background),
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

private fun deckEmoji(deckId: String): String =
    when (deckId) {
        "animals" -> "🐾"
        "food" -> "🍎"
        "transport" -> "🚗"
        "home" -> "🏠"
        else -> "📘"
    }

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    clickable(
        indication = null,
        interactionSource = MutableInteractionSource()
    ) { onClick() }
