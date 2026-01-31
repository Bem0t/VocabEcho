package com.myApp27.vocabecho.ui.decks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myApp27.vocabecho.data.CombinedDeckRepository
import com.myApp27.vocabecho.data.DeckRepository
import com.myApp27.vocabecho.data.UserDeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import com.myApp27.vocabecho.domain.model.Deck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DecksUiState(
    val decks: List<Deck> = emptyList(),
    val isLoading: Boolean = true
)

class DecksViewModel(app: Application) : AndroidViewModel(app) {
    private val db = DatabaseProvider.get(app.applicationContext)
    private val assetRepo = DeckRepository(app.applicationContext)
    private val userRepo = UserDeckRepository(db.userDeckDao(), db.userCardDao())
    private val repo = CombinedDeckRepository(assetRepo, userRepo)

    private val _state = MutableStateFlow(DecksUiState())
    val state: StateFlow<DecksUiState> = _state

    init {
        loadDecks()
    }

    fun loadDecks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val decks = repo.loadAllDecks()
            _state.value = DecksUiState(decks = decks, isLoading = false)
        }
    }
}