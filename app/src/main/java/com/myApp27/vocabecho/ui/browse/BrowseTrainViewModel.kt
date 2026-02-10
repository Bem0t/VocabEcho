package com.myApp27.vocabecho.ui.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myApp27.vocabecho.data.CombinedDeckRepository
import com.myApp27.vocabecho.data.DeckRepository
import com.myApp27.vocabecho.data.UserDeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import com.myApp27.vocabecho.domain.model.CardType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowseTrainViewModel(
    private val deckId: String,
    app: Application
) : AndroidViewModel(app) {

    private val db = DatabaseProvider.get(app.applicationContext)
    private val assetRepo = DeckRepository(app.applicationContext)
    private val userRepo = UserDeckRepository(db.userDeckDao(), db.userCardDao())
    private val deckRepo = CombinedDeckRepository(assetRepo, userRepo)

    private val _state = MutableStateFlow(BrowseTrainState())
    val state: StateFlow<BrowseTrainState> = _state

    private var cards: List<Pair<String, String>> = emptyList()

    init {
        loadDeck()
    }

    private fun loadDeck() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    deckTitle = "",
                    cardsTotal = 0,
                    currentIndex = 0,
                    frontText = "",
                    backText = "",
                    isFinished = false,
                    errorMessage = null
                )
            }

            val deck = deckRepo.loadDeck(deckId)
            if (deck == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "?????? ?? ???????"
                    )
                }
                return@launch
            }

            cards = deck.cards.map { card ->
                if (card.type == CardType.CLOZE &&
                    !card.clozeText.isNullOrBlank() &&
                    !card.clozeAnswer.isNullOrBlank()
                ) {
                    card.clozeAnswer!! to card.clozeText!!
                } else {
                    card.front to card.back
                }
            }

            val first = cards.firstOrNull()
            _state.update {
                it.copy(
                    isLoading = false,
                    deckTitle = deck.title,
                    cardsTotal = cards.size,
                    currentIndex = 0,
                    frontText = first?.first.orEmpty(),
                    backText = first?.second.orEmpty(),
                    isFinished = false,
                    errorMessage = null
                )
            }
        }
    }

    fun onKnow() = advance()

    fun onDontKnow() = advance()

    fun restart() {
        _state.update { current ->
            if (current.isLoading || current.errorMessage != null) {
                return@update current
            }
            if (current.cardsTotal == 0) {
                return@update current.copy(isFinished = false)
            }
            val first = cards.firstOrNull()
            current.copy(
                currentIndex = 0,
                frontText = first?.first.orEmpty(),
                backText = first?.second.orEmpty(),
                isFinished = false
            )
        }
    }

    private fun advance() {
        _state.update { current ->
            if (current.isLoading || current.errorMessage != null || current.isFinished || current.cardsTotal == 0) {
                return@update current
            }
            val nextIndex = current.currentIndex + 1
            if (nextIndex >= current.cardsTotal) {
                current.copy(
                    currentIndex = current.cardsTotal,
                    frontText = "",
                    backText = "",
                    isFinished = true
                )
            } else {
                val next = cards.getOrNull(nextIndex)
                current.copy(
                    currentIndex = nextIndex,
                    frontText = next?.first.orEmpty(),
                    backText = next?.second.orEmpty(),
                    isFinished = false
                )
            }
        }
    }
}

