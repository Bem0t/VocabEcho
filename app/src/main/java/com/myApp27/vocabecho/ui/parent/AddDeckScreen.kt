package com.myApp27.vocabecho.ui.parent

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myApp27.vocabecho.R

@Composable
fun AddDeckScreen(
    onBack: () -> Unit
) {
    val vm: AddDeckViewModel = viewModel()
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

                    // Add card section
                    Text(
                        text = "Добавить карточку",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B4AA2)
                    )

                    OutlinedTextField(
                        value = state.currentFront,
                        onValueChange = { vm.onFrontChanged(it) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Слово (русский)") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.currentBack,
                        onValueChange = { vm.onBackChanged(it) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Перевод (английский)") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    val canAddCard = state.currentFront.isNotBlank() && state.currentBack.isNotBlank()
                    SmallButton(
                        text = "Добавить карточку",
                        background = if (canAddCard) Color(0xFF66B05D) else Color(0xFFAAAAAA),
                        enabled = canAddCard,
                        onClick = { vm.addCard() }
                    )

                    // Cards list
                    if (state.cards.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Карточки (${state.cards.size}):",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B4AA2)
                        )

                        state.cards.forEachIndexed { index, (front, back) ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4EEF8))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}. $front — $back",
                                        modifier = Modifier.weight(1f),
                                        color = Color(0xFF0B4AA2)
                                    )
                                }
                            }
                        }

                        SmallButton(
                            text = "Удалить последнюю",
                            background = Color(0xFFF05A3A),
                            enabled = true,
                            onClick = { vm.removeLastCard() }
                        )
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

                val canSave = state.title.isNotBlank() && state.cards.isNotEmpty() && !state.isSaving
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
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .then(if (enabled) Modifier.clickableNoRipple(onClick) else Modifier)
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

    Box(
        modifier = modifier
            .height(52.dp)
            .shadow(12.dp, shapeOuter)
            .background(Color.White, shapeOuter)
            .padding(4.dp)
            .clickableNoRipple(onClick)
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
