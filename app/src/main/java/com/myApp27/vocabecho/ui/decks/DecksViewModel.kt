package com.myApp27.vocabecho.ui.decks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.myApp27.vocabecho.data.DeckRepository
import com.myApp27.vocabecho.domain.model.Deck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DecksUiState(
    val decks: List<Deck> = emptyList()
)

class DecksViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = DeckRepository(app.applicationContext)

    private val _state = MutableStateFlow(DecksUiState())
    val state: StateFlow<DecksUiState> = _state

    init {
        _state.value = DecksUiState(decks = repo.loadAllDecks())
    }
}