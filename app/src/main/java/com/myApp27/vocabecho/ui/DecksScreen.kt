package com.myApp27.vocabecho.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myApp27.vocabecho.R

@Composable
fun DecksScreen(
    onDeckClick: (String) -> Unit,
    onParentsClick: () -> Unit
) {
    val decks = listOf(
        DeckTileUi(
            id = "animals",
            title = "Животные",
            countText = "20 карточек",
            tileColor = Color(0xFF66B05D),
            imageRes = R.drawable.deck_animals
        ),
        DeckTileUi(
            id = "food",
            title = "Еда",
            countText = "20 карточек",
            tileColor = Color(0xFFF4B63A),
            imageRes = R.drawable.deck_food
        ),
        DeckTileUi(
            id = "transport",
            title = "Транспорт",
            countText = "20 карточек",
            tileColor = Color(0xFF4FA7E3),
            imageRes = R.drawable.deck_transport
        ),
        DeckTileUi(
            id = "home",
            title = "Дом",
            countText = "20 карточек",
            tileColor = Color(0xFF9A7DE8),
            imageRes = R.drawable.deck_house
        ),
    )

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
                .padding(top = 22.dp, bottom = 22.dp), // чуть больше снизу
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ВЫПУКЛЫЙ ЗАГОЛОВОК (двойной текст)
            BubbleTitle(text = "Выбери тему")

            Spacer(Modifier.height(18.dp))

            // Блок колод по центру
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DeckTile(
                            ui = decks[0],
                            modifier = Modifier.weight(1f),
                            onClick = { onDeckClick(decks[0].id) }
                        )
                        DeckTile(
                            ui = decks[1],
                            modifier = Modifier.weight(1f),
                            onClick = { onDeckClick(decks[1].id) }
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DeckTile(
                            ui = decks[2],
                            modifier = Modifier.weight(1f),
                            onClick = { onDeckClick(decks[2].id) }
                        )
                        DeckTile(
                            ui = decks[3],
                            modifier = Modifier.weight(1f),
                            onClick = { onDeckClick(decks[3].id) }
                        )
                    }
                }
            }

            // ОТСТУП МЕЖДУ КОЛОДАМИ И КНОПКОЙ (как на референсе)
            Spacer(Modifier.height(26.dp))

            ParentsButton(
                text = "Родителям",
                onClick = onParentsClick
            )
        }
    }
}

@Composable
private fun BubbleTitle(text: String) {
    Box(contentAlignment = Alignment.Center) {
        // Тень/обводка
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0x66000000),
            modifier = Modifier.offset(y = 2.dp)
        )
        // Основной белый текст
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

private data class DeckTileUi(
    val id: String,
    val title: String,
    val countText: String,
    val tileColor: Color,
    val imageRes: Int
)

@Composable
private fun DeckTile(
    ui: DeckTileUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    // Белая рамка + тень
    Box(
        modifier = modifier
            .height(140.dp)
            .shadow(10.dp, shape)
            .background(Color.White, shape)
            .padding(3.dp)
            .clickable { onClick() }
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = ui.tileColor),
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(ui.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = ui.title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x66000000)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = ui.countText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentsButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)

    // Белая рамка + тень, как у плиток, но сильнее
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(60.dp)
            .shadow(14.dp, shape)
            .background(Color.White, shape)
            .padding(4.dp)
            .clickable { onClick() }
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA62B)),
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("🔒", fontSize = MaterialTheme.typography.titleLarge.fontSize)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
