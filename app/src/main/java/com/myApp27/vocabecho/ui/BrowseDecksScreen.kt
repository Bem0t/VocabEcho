package com.myApp27.vocabecho.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myApp27.vocabecho.R
import com.myApp27.vocabecho.data.CombinedDeckRepository
import com.myApp27.vocabecho.data.DeckRepository
import com.myApp27.vocabecho.data.UserDeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import com.myApp27.vocabecho.domain.model.Deck
import com.myApp27.vocabecho.ui.components.pressScale
import com.myApp27.vocabecho.ui.components.rememberPressInteraction

@Composable
fun BrowseDecksScreen(
    onDeckClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.get(context) }
    val assetRepo = remember { DeckRepository(context) }
    val userRepo = remember { UserDeckRepository(db.userDeckDao(), db.userCardDao()) }
    val deckRepo = remember { CombinedDeckRepository(assetRepo, userRepo) }

    var decks by remember { mutableStateOf<List<Deck>>(emptyList()) }

    LaunchedEffect(Unit) {
        decks = deckRepo.loadAllDecks()
    }

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
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            HeaderPill(text = "Обучение")

            Spacer(Modifier.height(18.dp))

            // Deck list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(decks) { deck ->
                    BrowseDeckItem(
                        deck = deck,
                        onClick = { onDeckClick(deck.id) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Back button
            BackButton(onClick = onBack)
        }
    }
}

@Composable
private fun BrowseDeckItem(
    deck: Deck,
    onClick: () -> Unit
) {
    val interactionSource = rememberPressInteraction()
    val shape = RoundedCornerShape(14.dp)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color(0xEFFFFFFF)),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .shadow(6.dp, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B4AA2)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${deck.cards.size} ${cardWord(deck.cards.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }

            Text(
                text = ">",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFAAAAAA)
            )
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
private fun BackButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    val interactionSource = rememberPressInteraction()

    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(52.dp)
            .pressScale(interactionSource)
            .shadow(10.dp, shape)
            .background(Color.White, shape)
            .padding(4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF)),
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Назад",
                    color = Color(0xFF0B4AA2),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

private fun cardWord(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..14 -> "карточек"
        mod10 == 1 -> "карточка"
        mod10 in 2..4 -> "карточки"
        else -> "карточек"
    }
}
