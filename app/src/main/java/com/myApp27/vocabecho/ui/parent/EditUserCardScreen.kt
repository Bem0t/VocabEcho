package com.myApp27.vocabecho.ui.parent

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myApp27.vocabecho.R
import com.myApp27.vocabecho.domain.model.CardType
import com.myApp27.vocabecho.ui.components.ClozePreview
import com.myApp27.vocabecho.ui.components.pressScale
import com.myApp27.vocabecho.ui.components.rememberPressInteraction

@Composable
fun EditUserCardScreen(
    deckId: String,
    cardId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vm: EditUserCardViewModel = viewModel(
        factory = EditUserCardViewModelFactory(
            context.applicationContext as Application,
            deckId,
            cardId
        )
    )
    val state by vm.state.collectAsState()

    Box(Modifier.fillMaxSize()) {
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
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderPill(text = "Редактировать")

            Spacer(Modifier.height(18.dp))

            // Main content card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEFFFFFFF)),
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth(0.95f)
                    .weight(1f)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
            ) {
                when {
                    state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Загрузка...")
                        }
                    }
                    state.notFound -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Карточка не найдена",
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Card type selector
                            Text(
                                text = "Тип карточки",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B4AA2)
                            )

                            CardTypeSelector(
                                selectedType = state.selectedType,
                                onTypeSelected = { vm.onTypeChanged(it) }
                            )

                            // Type description
                            Text(
                                text = cardTypeDescription(state.selectedType),
                                color = Color(0xFF666666),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(Modifier.height(4.dp))

                            // Dynamic form fields based on card type
                            when (state.selectedType) {
                                CardType.BASIC, CardType.BASIC_REVERSED, CardType.BASIC_TYPED -> {
                                    Text(
                                        text = "Лицевая сторона",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B4AA2)
                                    )
                                    OutlinedTextField(
                                        value = state.front,
                                        onValueChange = { vm.onFrontChanged(it) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Слово") },
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = "Оборотная сторона",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B4AA2)
                                    )
                                    OutlinedTextField(
                                        value = state.back,
                                        onValueChange = { vm.onBackChanged(it) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Перевод") },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                CardType.CLOZE -> {
                                    Text(
                                        text = "Текст предложения",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B4AA2)
                                    )
                                    OutlinedTextField(
                                        value = state.clozeText,
                                        onValueChange = { vm.onClozeTextChanged(it) },
                                        singleLine = false,
                                        maxLines = 3,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("The capital of France is Paris.") },
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = "Скрываемое слово/фраза",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B4AA2)
                                    )
                                    OutlinedTextField(
                                        value = state.clozeAnswer,
                                        onValueChange = { vm.onClozeAnswerChanged(it) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Paris") },
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = "Подсказка (необязательно)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B4AA2)
                                    )
                                    OutlinedTextField(
                                        value = state.clozeHint,
                                        onValueChange = { vm.onClozeHintChanged(it) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("город") },
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    // Live preview
                                    ClozePreview(
                                        sentence = state.clozeText,
                                        answer = state.clozeAnswer,
                                        hint = state.clozeHint.ifBlank { null }
                                    )
                                }
                            }

                            // Error message
                            state.errorMessage?.let { error ->
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = error,
                                    color = Color(0xFFCC3333),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Bottom buttons
            if (!state.isLoading && !state.notFound) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        text = "Назад",
                        background = Color(0x66FFFFFF),
                        textColor = Color(0xFF0B4AA2),
                        modifier = Modifier.weight(1f),
                        onClick = onBack
                    )

                    val canSave = when (state.selectedType) {
                        CardType.BASIC, CardType.BASIC_REVERSED, CardType.BASIC_TYPED ->
                            state.front.isNotBlank() && state.back.isNotBlank() && !state.isSaving
                        CardType.CLOZE ->
                            state.clozeText.isNotBlank() && state.clozeAnswer.isNotBlank() && !state.isSaving
                    }
                    ActionButton(
                        text = if (state.isSaving) "Сохранение..." else "Сохранить",
                        background = if (canSave) Color(0xFF3B87D9) else Color(0xFFAAAAAA),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (canSave) {
                                vm.save(onSuccess = onBack)
                            }
                        }
                    )
                }
            } else {
                BackPill(text = "Назад", onClick = onBack)
            }
        }
    }
}

@Composable
private fun HeaderPill(text: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0B4AA2)
        )
    }
}

@Composable
private fun BackPill(text: String, onClick: () -> Unit) {
    val interactionSource = rememberPressInteraction()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF)),
        modifier = Modifier
            .widthIn(max = 220.dp)
            .fillMaxWidth(0.6f)
            .height(46.dp)
            .pressScale(interactionSource)
            .shadow(8.dp, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0B4AA2)
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shapeOuter = RoundedCornerShape(18.dp)
    val shapeInner = RoundedCornerShape(16.dp)
    val interactionSource = rememberPressInteraction()

    Box(
        modifier = modifier
            .height(52.dp)
            .pressScale(interactionSource)
            .shadow(12.dp, shapeOuter)
            .background(Color.White, shapeOuter)
            .padding(4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
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
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    clickable(
        indication = null,
        interactionSource = MutableInteractionSource()
    ) { onClick() }

class EditUserCardViewModelFactory(
    private val app: Application,
    private val deckId: String,
    private val cardId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditUserCardViewModel(app, deckId, cardId) as T
    }
}

/**
 * Card type selector with segmented button style.
 */
@Composable
private fun CardTypeSelector(
    selectedType: CardType,
    onTypeSelected: (CardType) -> Unit
) {
    val types = listOf(
        CardType.BASIC to "BASIC",
        CardType.BASIC_TYPED to "TYPED",
        CardType.CLOZE to "CLOZE"
    )

    // Map BASIC_REVERSED to BASIC for display
    val displayType = if (selectedType == CardType.BASIC_REVERSED) CardType.BASIC else selectedType

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE8E4F0)),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        types.forEach { (type, label) ->
            val isSelected = displayType == type
            val interactionSource = rememberPressInteraction()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .pressScale(interactionSource)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color(0xFF3B87D9) else Color.Transparent)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onTypeSelected(type) }
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color(0xFF0B4AA2),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Returns a short description for each card type.
 */
private fun cardTypeDescription(type: CardType): String = when (type) {
    CardType.BASIC, CardType.BASIC_REVERSED -> "Показывает лицевую сторону, затем правильный ответ."
    CardType.BASIC_TYPED -> "Нужно ввести ответ текстом, затем сравнить с правильным."
    CardType.CLOZE -> "Пропуск в предложении: нужно вписать скрытое слово/фразу."
}
