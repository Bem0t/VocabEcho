package com.myApp27.vocabecho.ui.parent

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myApp27.vocabecho.R
import com.myApp27.vocabecho.domain.model.CardType
import com.myApp27.vocabecho.ui.components.pressScale
import com.myApp27.vocabecho.ui.components.rememberPressInteraction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AddDeckScreen(
    onBack: () -> Unit
) {
    val vm: AddDeckViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        vm.onImageSelected(uri?.toString())
    }

    // Load selected image preview
    val imageBitmap = rememberBitmapFromUri(state.selectedImageUri, context)

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
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            HeaderPill(text = "Новая колода")

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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Deck title input
                    Text(
                        text = "Название колоды",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B4AA2)
                    )
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = { vm.onTitleChanged(it) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Например: Цвета") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    // Image picker section
                    Text(
                        text = "Обложка (необязательно)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B4AA2)
                    )

                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Обложка колоды",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    SmallButton(
                        text = if (state.selectedImageUri != null) "Изменить картинку" else "Выбрать картинку",
                        background = Color(0xFF7B8CDE),
                        enabled = true,
                        onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    // Add card section
                    Text(
                        text = "Добавить карточку",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B4AA2)
                    )

                    // Card type selector
                    CardTypeSelector(
                        selectedType = state.selectedCardType,
                        onTypeSelected = { vm.onCardTypeChanged(it) }
                    )

                    // Type description
                    Text(
                        text = cardTypeDescription(state.selectedCardType),
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(4.dp))

                    // Dynamic form fields based on card type
                    when (state.selectedCardType) {
                        CardType.BASIC, CardType.BASIC_REVERSED, CardType.BASIC_TYPED -> {
                            OutlinedTextField(
                                value = state.currentFront,
                                onValueChange = { vm.onFrontChanged(it) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Лицевая сторона") },
                                placeholder = { Text("Слово (русский)") },
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = state.currentBack,
                                onValueChange = { vm.onBackChanged(it) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Оборотная сторона") },
                                placeholder = { Text("Перевод (английский)") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        CardType.CLOZE -> {
                            OutlinedTextField(
                                value = state.currentClozeText,
                                onValueChange = { vm.onClozeTextChanged(it) },
                                singleLine = false,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Текст предложения") },
                                placeholder = { Text("The capital of France is Paris.") },
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = state.currentClozeAnswer,
                                onValueChange = { vm.onClozeAnswerChanged(it) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Скрываемое слово/фраза") },
                                placeholder = { Text("Paris") },
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = state.currentClozeHint,
                                onValueChange = { vm.onClozeHintChanged(it) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Подсказка (необязательно)") },
                                placeholder = { Text("город") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Input error message
                    state.inputError?.let { error ->
                        Text(
                            text = error,
                            color = Color(0xFFCC3333),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    val canAddCard = when (state.selectedCardType) {
                        CardType.BASIC, CardType.BASIC_REVERSED, CardType.BASIC_TYPED ->
                            state.currentFront.isNotBlank() && state.currentBack.isNotBlank()
                        CardType.CLOZE ->
                            state.currentClozeText.isNotBlank() && state.currentClozeAnswer.isNotBlank()
                    }

                    SmallButton(
                        text = "Добавить карточку",
                        background = if (canAddCard) Color(0xFF66B05D) else Color(0xFFAAAAAA),
                        enabled = canAddCard,
                        onClick = { vm.addCard() }
                    )

                    // Cards list
                    if (state.draftCards.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Карточки (${state.draftCards.size}):",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B4AA2)
                        )

                        state.draftCards.forEachIndexed { index, draft ->
                            DraftCardItem(
                                index = index + 1,
                                draft = draft,
                                onDelete = { vm.removeCard(index) }
                            )
                        }
                    }

                    // Error message
                    state.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color(0xFFCC3333),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Bottom buttons
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

                val canSave = state.title.isNotBlank() && state.draftCards.isNotEmpty() && !state.isSaving
                ActionButton(
                    text = if (state.isSaving) "Сохранение..." else "Сохранить",
                    background = if (canSave) Color(0xFF3B87D9) else Color(0xFFAAAAAA),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (canSave) {
                            vm.saveDeck(onSuccess = onBack)
                        }
                    }
                )
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
private fun SmallButton(
    text: String,
    background: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = rememberPressInteraction()
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .pressScale(interactionSource)
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold
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

@Composable
private fun rememberBitmapFromUri(uriString: String?, context: android.content.Context): ImageBitmap? {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(uriString) {
        bitmap = if (uriString != null) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(uriString)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            null
        }
    }

    return bitmap
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
        CardType.BASIC_REVERSED to "x2",
        CardType.BASIC_TYPED to "TYPED",
        CardType.CLOZE to "CLOZE"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE8E4F0)),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        types.forEach { (type, label) ->
            val isSelected = selectedType == type
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
 * Draft card item in the preview list.
 */
@Composable
private fun DraftCardItem(
    index: Int,
    draft: DraftCard,
    onDelete: () -> Unit
) {
    val typeColor = when (draft.type) {
        CardType.BASIC -> Color(0xFF66B05D)
        CardType.BASIC_REVERSED -> Color(0xFF9A7DE8)
        CardType.BASIC_TYPED -> Color(0xFF4FA7E3)
        CardType.CLOZE -> Color(0xFFF4B63A)
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4EEF8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(typeColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = draft.typeLabel(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(8.dp))

            // Card content preview
            Text(
                text = "$index. ${draft.displayText()}",
                modifier = Modifier.weight(1f),
                color = Color(0xFF0B4AA2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )

            // Delete button
            TextButton(
                onClick = onDelete,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Удалить",
                    color = Color(0xFFCC3333),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Returns a short description for each card type.
 */
private fun cardTypeDescription(type: CardType): String = when (type) {
    CardType.BASIC -> "Показывает лицевую сторону, затем правильный ответ."
    CardType.BASIC_REVERSED -> "Две карточки: туда и обратно (вопрос/ответ меняются местами)."
    CardType.BASIC_TYPED -> "Нужно ввести ответ текстом, затем сравнить с правильным."
    CardType.CLOZE -> "Пропуск в предложении: нужно вписать скрытое слово/фразу."
}
