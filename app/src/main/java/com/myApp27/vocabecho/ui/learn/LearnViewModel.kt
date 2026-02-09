package com.myApp27.vocabecho.ui.learn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myApp27.vocabecho.data.CombinedDeckRepository
import com.myApp27.vocabecho.data.DeckRepository
import com.myApp27.vocabecho.data.UserDeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import com.myApp27.vocabecho.data.progress.ProgressRepository
import com.myApp27.vocabecho.domain.model.Card
import com.myApp27.vocabecho.domain.model.CardInstance
import com.myApp27.vocabecho.domain.model.CardInstanceGenerator
import com.myApp27.vocabecho.domain.queue.CardQueueBuilder
import com.myApp27.vocabecho.domain.time.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LearnUiState(
    val isLoading: Boolean = true,
    val deckTitle: String = "",
    val currentCard: CardInstance? = null,
    val remaining: Int = 0,
    val dueToday: Int = 0,
    val newCount: Int = 0,
    val error: String? = null
)


class LearnViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DatabaseProvider.get(app.applicationContext)
    private val assetRepo = DeckRepository(app.applicationContext)
    private val userRepo = UserDeckRepository(db.userDeckDao(), db.userCardDao())
    private val deckRepo = CombinedDeckRepository(assetRepo, userRepo)
    private val progressRepo = ProgressRepository(db.cardProgressDao(), db.cardStatsDao())

    private val _state = MutableStateFlow(LearnUiState())
    val state: StateFlow<LearnUiState> = _state

    private var queue: List<CardInstance> = emptyList()
    private var index: Int = 0
    private var loadedDeckId: String? = null

    fun load(deckId: String) {
        viewModelScope.launch {
            if (deckId == loadedDeckId && queue.isNotEmpty() && _state.value.currentCard != null && !_state.value.isLoading) {
                return@launch
            }
            _state.value = LearnUiState(isLoading = true)

            val deck = deckRepo.loadDeck(deckId)
            if (deck == null) {
                _state.value = LearnUiState(isLoading = false, error = "Колода не найдена")
                return@launch
            }

            val today = TimeProvider.todayEpochDay()
            val progress = progressRepo.getAllForDeck(deckId)
            val progressById = progress.associateBy { it.cardId }

            val dueToday = deck.cards.count { card ->
                val p = progressById[card.id]
                p != null && p.dueEpochDay <= today
            }

            val newCount = deck.cards.count { card ->
                progressById[card.id] == null
            }

            val baseQueue: List<Card> = CardQueueBuilder.buildQueue(deck.cards, progress, today)
            queue = baseQueue.map { card -> CardInstanceGenerator.fromSimpleCard(card, deck.id) }
            index = 0
            loadedDeckId = deckId

            val current = queue.getOrNull(index)
            _state.value = LearnUiState(
                isLoading = false,
                deckTitle = deck.title,
                currentCard = current,
                remaining = (queue.size - index).coerceAtLeast(0),
                dueToday = dueToday,
                newCount = newCount
            )
        }
    }

    fun currentCardId(): String? = _state.value.currentCard?.id

    fun advanceToNextCard() {
        index++
        val current = queue.getOrNull(index)
        _state.value = _state.value.copy(
            currentCard = current,
            remaining = (queue.size - index).coerceAtLeast(0)
        )
    }

    fun moveNext() = advanceToNextCard()
}
