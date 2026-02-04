package com.myApp27.vocabecho.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myApp27.vocabecho.R
import com.myApp27.vocabecho.domain.model.Deck
import com.myApp27.vocabecho.ui.components.pressScale
import com.myApp27.vocabecho.ui.components.rememberPressInteraction
import com.myApp27.vocabecho.ui.decks.DecksViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DecksScreen(
    onDeckClick: (String) -> Unit,
    onParentsClick: () -> Unit
) {
    val vm: DecksViewModel = viewModel()
    val state by vm.state.collectAsState()

    // Reload decks on every resume (to pick up newly created decks)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.loadDecks()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                .padding(top = 22.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BubbleTitle(text = "Выбери тему")

            Spacer(Modifier.height(18.dp))

            // Scrollable deck grid
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Arrange decks in rows of 2
                    val decks = state.decks
                    val rows = decks.chunked(2)

                    rows.forEach { rowDecks ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val context = LocalContext.current
                            rowDecks.forEach { deck ->
                                DeckTile(
                                    ui = deckToTileUi(deck),
                                    context = context,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onDeckClick(deck.id) }
                                )
                            }
                            // Fill empty space if odd number of decks
                            if (rowDecks.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            ParentsButton(
                text = "Родителям",
                onClick = onParentsClick
            )
        }
    }
}

/**
 * Convert domain Deck to UI tile model.
 * Built-in decks have specific colors and images; user decks get defaults.
 */
private fun deckToTileUi(deck: Deck): DeckTileUi {
    val cardCountText = "${deck.cards.size} ${cardWord(deck.cards.size)}"

    return when (deck.id) {
        "animals" -> DeckTileUi(
            id = deck.id,
            title = deck.title,
            countText = cardCountText,
            tileColor = Color(0xFF66B05D),
            imageRes = R.drawable.deck_animals,
            imageUri = null,
            isBuiltIn = true
        )
        "food" -> DeckTileUi(
            id = deck.id,
            title = deck.title,
            countText = cardCountText,
            tileColor = Color(0xFFF4B63A),
            imageRes = R.drawable.deck_food,
            imageUri = null,
            isBuiltIn = true
        )
        "transport" -> DeckTileUi(
            id = deck.id,
            title = deck.title,
            countText = cardCountText,
            tileColor = Color(0xFF4FA7E3),
            imageRes = R.drawable.deck_transport,
            imageUri = null,
            isBuiltIn = true
        )
        "home" -> DeckTileUi(
            id = deck.id,
            title = deck.title,
            countText = cardCountText,
            tileColor = Color(0xFF9A7DE8),
            imageRes = R.drawable.deck_house,
            imageUri = null,
            isBuiltIn = true
        )
        else -> DeckTileUi(
            id = deck.id,
            title = deck.title,
            countText = cardCountText,
            tileColor = Color(0xFF7B8CDE),
            imageRes = null,
            imageUri = deck.imageUri,
            isBuiltIn = false
        )
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
    val imageRes: Int?,
    val imageUri: String?,
    val isBuiltIn: Boolean
)

@Composable
private fun DeckTile(
    ui: DeckTileUi,
    context: android.content.Context,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val interactionSource = rememberPressInteraction()

    // Load image from URI for user decks
    val userImageBitmap = if (!ui.isBuiltIn && ui.imageUri != null) {
        rememberBitmapFromUri(ui.imageUri, context)
    } else {
        null
    }

    // Белая рамка + тень + press scale
    Box(
        modifier = modifier
            .height(140.dp)
            .pressScale(interactionSource)
            .shadow(10.dp, shape)
            .background(Color.White, shape)
            .padding(3.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = ui.tileColor),
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(Modifier.fillMaxSize()) {
                when {
                    // Built-in deck: show drawable
                    ui.isBuiltIn && ui.imageRes != null -> {
                        Image(
                            painter = painterResource(ui.imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // User deck with image: show from URI
                    !ui.isBuiltIn && userImageBitmap != null -> {
                        Image(
                            bitmap = userImageBitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // User deck without image: show large title
                    !ui.isBuiltIn -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ui.title,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Title overlay (only for built-in or decks with images)
                if (ui.isBuiltIn || userImageBitmap != null) {
                    Text(
                        text = ui.title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                    )
                }

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

@Composable
private fun ParentsButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    val interactionSource = rememberPressInteraction()

    // Белая рамка + тень + press scale
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(60.dp)
            .pressScale(interactionSource)
            .shadow(14.dp, shape)
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
