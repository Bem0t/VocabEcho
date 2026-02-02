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
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp, bottom = 18.dp),
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

                    val canSave = state.front.isNotBlank() && state.back.isNotBlank() && !state.isSaving
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
