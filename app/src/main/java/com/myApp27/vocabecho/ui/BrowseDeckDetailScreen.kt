package com.myApp27.vocabecho.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.myApp27.vocabecho.domain.model.Card as DomainCard
import com.myApp27.vocabecho.domain.model.Deck
import com.myApp27.vocabecho.ui.components.pressScale
import com.myApp27.vocabecho.ui.components.rememberPressInteraction

@Composable
fun BrowseDeckDetailScreen(
    deckId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.get(context) }
    val assetRepo = remember { DeckRepository(context) }
    val userRepo = remember { UserDeckRepository(db.userDeckDao(), db.userCardDao()) }
    val deckRepo = remember { CombinedDeckRepository(assetRepo, userRepo) }

    var deck by remember { mutableStateOf<Deck?>(null) }

    LaunchedEffect(deckId) {
        deck = deckRepo.loadDeck(deckId)
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
            HeaderPill(text = deck?.title ?: "Загрузка...")

            Spacer(Modifier.height(18.dp))

            // Cards list
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEFFFFFFF)),
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
            ) {
                val currentDeck = deck
                if (currentDeck == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Загрузка...")
                    }
                } else if (currentDeck.cards.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Нет карточек",
                            color = Color(0xFF666666)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                    ) {
                        itemsIndexed(currentDeck.cards) { index, card ->
                            WordRow(card = card)
                            if (index < currentDeck.cards.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color(0x1A000000)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Back button
            BackButton(onClick = onBack)
        }
    }
}

@Composable
private fun WordRow(card: DomainCard) {
    // For CLOZE cards, show clozeText — clozeAnswer; for others, show front — back
    val left: String
    val right: String

    if (card.type == com.myApp27.vocabecho.domain.model.CardType.CLOZE &&
        !card.clozeText.isNullOrBlank() && !card.clozeAnswer.isNullOrBlank()
    ) {
        left = card.clozeAnswer!!
        right = card.clozeText!!
    } else {
        left = card.front
        right = card.back
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = left,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B4AA2),
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = right,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF444444),
            modifier = Modifier.weight(1f)
        )
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
