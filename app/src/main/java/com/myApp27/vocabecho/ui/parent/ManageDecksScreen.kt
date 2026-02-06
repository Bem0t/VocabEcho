package com.myApp27.vocabecho.ui.parent

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myApp27.vocabecho.R
import com.myApp27.vocabecho.domain.model.Deck
import com.myApp27.vocabecho.ui.components.pressScale
import com.myApp27.vocabecho.ui.components.rememberPressInteraction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ManageDecksScreen(
    onBack: () -> Unit,
    onOpenDeck: (deckId: String) -> Unit
) {
    val vm: ManageDecksViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    // Reload on resume
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

    // Delete confirmation dialog
    if (state.showDeleteConfirmForDeckId != null) {
        AlertDialog(
            onDismissRequest = { if (!state.isDeleting) vm.cancelDelete() },
            title = { Text("Удалить колоду?") },
            text = { Text("Будут удалены все карточки и прогресс.") },
            confirmButton = {
                TextButton(
                    onClick = { vm.confirmDelete() },
                    enabled = !state.isDeleting
                ) {
                    Text(if (state.isDeleting) "Удаление..." else "Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { vm.cancelDelete() },
                    enabled = !state.isDeleting
                ) {
                    Text("Отмена")
                }
            }
        )
    }

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
            HeaderPill(text = "Мои колоды")

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
                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Загрузка...")
                    }
                } else if (state.decks.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Нет созданных колод",
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.decks.forEach { deck ->
                            DeckListItem(
                                deck = deck,
                                context = context,
                                onClick = { onOpenDeck(deck.id) },
                                onDelete = { vm.requestDelete(deck.id) }
                            )
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

            Spacer(Modifier.height(14.dp))

            BackPill(text = "Назад", onClick = onBack)
        }
    }
}

@Composable
private fun DeckListItem(
    deck: Deck,
    context: android.content.Context,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val imageBitmap = rememberBitmapFromUri(deck.imageUri, context)
    val interactionSource = rememberPressInteraction()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4EEF8)),
        modifier = Modifier
            .pressScale(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or title placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF7B8CDE)),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = deck.title.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.title,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B4AA2),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${deck.cards.size} ${cardWord(deck.cards.size)}",
                    color = Color(0xFF666666),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TextButton(
                onClick = onDelete,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Удалить",
                    color = Color(0xFFCC3333),
                    style = MaterialTheme.typography.bodySmall
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