package com.myApp27.vocabecho.ui

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myApp27.vocabecho.R
import com.myApp27.vocabecho.ui.browse.BrowseTrainViewModel
import com.myApp27.vocabecho.ui.browse.BrowseTrainViewModelFactory
import com.myApp27.vocabecho.ui.components.pressScale
import com.myApp27.vocabecho.ui.components.rememberPressInteraction

@Composable
fun BrowseDeckDetailScreen(
    deckId: String,
    onBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as Application
    val vm: BrowseTrainViewModel = viewModel(
        key = "browse_train_$deckId",
        factory = BrowseTrainViewModelFactory(deckId, app)
    )
    val state by vm.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_main),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        val headerText = when {
            state.deckTitle.isNotBlank() -> state.deckTitle
            state.isLoading -> "Загрузка..."
            else -> "Обучение"
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderPill(text = headerText)

            Spacer(Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEFFFFFFF)),
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        state.isLoading -> {
                            Text(
                                text = "Загрузка...",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        state.errorMessage != null -> {
                            Text(
                                text = "Ошибка: ${state.errorMessage}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB00020),
                                textAlign = TextAlign.Center
                            )
                        }
                        state.cardsTotal == 0 -> {
                            Text(
                                text = "Нет карточек",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF666666)
                            )
                        }
                        state.isFinished -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Готово!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0B4AA2)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Молодец!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF444444)
                                )
                            }
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Слово",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF666666)
                                )
                                Text(
                                    text = state.frontText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0B4AA2),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(14.dp))

                                Text(
                                    text = "Перевод",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF666666)
                                )
                                Text(
                                    text = state.backText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF444444),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = "${state.currentIndex + 1} / ${state.cardsTotal}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            when {
                state.isFinished -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TrainingButton(
                            text = "Начать заново",
                            background = Color(0xFF3B87D9),
                            modifier = Modifier.weight(1f),
                            onClick = { vm.restart() }
                        )
                        TrainingButton(
                            text = "Назад",
                            background = Color(0xFFF05A3A),
                            modifier = Modifier.weight(1f),
                            onClick = onBack
                        )
                    }
                }
                state.isLoading || state.errorMessage != null || state.cardsTotal == 0 -> {
                    TrainingButton(
                        text = "Назад",
                        background = Color(0xFF3B87D9),
                        modifier = Modifier.fillMaxWidth(0.7f),
                        onClick = onBack
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TrainingButton(
                            text = "Не знаю",
                            background = Color(0xFFF05A3A),
                            modifier = Modifier.weight(1f),
                            onClick = { vm.onDontKnow() }
                        )
                        TrainingButton(
                            text = "Знаю",
                            background = Color(0xFF3B87D9),
                            modifier = Modifier.weight(1f),
                            onClick = { vm.onKnow() }
                        )
                    }
                }
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
private fun TrainingButton(
    text: String,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shapeOuter = RoundedCornerShape(20.dp)
    val shapeInner = RoundedCornerShape(18.dp)
    val interactionSource = rememberPressInteraction()

    Box(
        modifier = modifier
            .height(60.dp)
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
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

