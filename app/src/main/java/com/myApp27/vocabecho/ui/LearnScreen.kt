package com.myApp27.vocabecho.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myApp27.vocabecho.R
import com.myApp27.vocabecho.ui.components.animatedBorderColor
import com.myApp27.vocabecho.ui.components.clickableWithScale
import com.myApp27.vocabecho.ui.components.pressScale
import com.myApp27.vocabecho.ui.components.rememberFocusInteraction
import com.myApp27.vocabecho.ui.components.rememberPressInteraction
import com.myApp27.vocabecho.ui.learn.LearnViewModel

@Composable
fun LearnScreen(
    deckId: String,
    onChecked: (cardId: String, answer: String) -> Unit,
    onBack: () -> Unit
) {
    val vm: LearnViewModel = viewModel()
    LaunchedEffect(deckId) { vm.load(deckId) }

    val state by vm.state.collectAsState()
    var userAnswer by remember { mutableStateOf("") }

    LaunchedEffect(state.currentCard?.id) { userAnswer = "" }

    val card = state.currentCard
    val totalShown = (donePlusRemaining(state.remaining)).coerceAtLeast(1)
    val done = (totalShown - state.remaining).coerceAtLeast(0)

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
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp, bottom = 16.dp)
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                BackCapsule(onClick = onBack)

                Spacer(Modifier.weight(1f))

                Capsule(text = "${done}/${(done + state.remaining).coerceAtLeast(1)}")

                Spacer(Modifier.weight(1f))

                Capsule(text = state.deckTitle.ifBlank { "Тема" })
            }

            Spacer(Modifier.height(14.dp))

            // Title changes based on card type
            val titleText = if (card?.expectsTyping == true) "Введи перевод" else "Вспомни ответ"
            Text(
                text = titleText,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(18.dp))

            // Центральная карточка
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E9D8)),
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth(0.78f)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (state.isLoading) {
                            Text("Загрузка...")
                            return@Column
                        }
                        state.error?.let {
                            Text("Ошибка: $it")
                            return@Column
                        }
                        if (card == null) {
                            Text("На сегодня всё!")
                            return@Column
                        }

                        // Показываем вопрос
                        Text(
                            text = card.front,
                            color = Color(0xFF0B4AA2),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        // Only show input field if card expects typing (BASIC_TYPED, CLOZE)
                        if (card.expectsTyping) {
                            Spacer(Modifier.height(10.dp))

                            // Поле ввода с animated focus border
                            val (answerInteraction, isAnswerFocused) = rememberFocusInteraction()
                            val answerBorderColor = animatedBorderColor(
                                isFocused = isAnswerFocused,
                                focusedColor = Color(0xFF0B4AA2),
                                unfocusedColor = Color(0xFFD0C8BE)
                            )

                            OutlinedTextField(
                                value = userAnswer,
                                onValueChange = { userAnswer = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("") },
                                shape = RoundedCornerShape(12.dp),
                                interactionSource = answerInteraction,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF7F3EC),
                                    unfocusedContainerColor = Color(0xFFF7F3EC),
                                    disabledContainerColor = Color(0xFFF7F3EC),

                                    focusedBorderColor = answerBorderColor,
                                    unfocusedBorderColor = answerBorderColor,
                                    disabledBorderColor = Color(0xFFD0C8BE),

                                    cursorColor = Color(0xFF0B4AA2),
                                    focusedTextColor = Color(0xFF0B4AA2),
                                    unfocusedTextColor = Color(0xFF0B4AA2)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Кнопки: меняются в зависимости от типа карточки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // For BASIC (no typing): button shows answer directly
                // For BASIC_TYPED/CLOZE: button checks typed answer
                val checkButtonText = if (card?.expectsTyping == true) "Проверить" else "Показать"
                val checkEnabled = if (card?.expectsTyping == true) {
                    card != null && userAnswer.isNotBlank()
                } else {
                    card != null
                }

                CuteButton(
                    text = checkButtonText,
                    background = Color(0xFFF05A3A),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val c = card ?: return@CuteButton
                        // For BASIC cards, pass empty answer (user will self-evaluate)
                        onChecked(c.id, userAnswer)
                    },
                    enabled = checkEnabled
                )

                CuteButton(
                    text = "Пропустить",
                    background = Color(0xFF3B87D9),
                    modifier = Modifier.weight(1f),
                    onClick = { vm.moveNext() },
                    enabled = card != null
                )
            }
        }
    }
}

/** маленький хак, чтобы не ругался линтер на (remaining + 0) */
private fun donePlusRemaining(remaining: Int) = remaining

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

@Composable
private fun BackCapsule(onClick: () -> Unit) {
    val interactionSource = rememberPressInteraction()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
        modifier = Modifier
            .pressScale(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = "← Назад",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

/**
 * Красивая "мультяшная" кнопка:
 * - белая рамка
 * - тень
 * - цветная внутренняя часть
 * - без ripple (как в твоём стиле)
 * - с press scale анимацией
 */
@Composable
private fun CuteButton(
    text: String,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val shapeOuter = RoundedCornerShape(18.dp)
    val shapeInner = RoundedCornerShape(16.dp)
    val interactionSource = rememberPressInteraction()

    Box(
        modifier = modifier
            .height(54.dp)
            .pressScale(interactionSource)
            .shadow(12.dp, shapeOuter)
            .background(Color.White, shapeOuter)
            .padding(4.dp)
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
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