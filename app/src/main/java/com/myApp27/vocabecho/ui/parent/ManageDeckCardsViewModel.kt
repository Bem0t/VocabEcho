package com.myApp27.vocabecho.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myApp27.vocabecho.data.UserDeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import com.myApp27.vocabecho.domain.model.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ManageDeckCardsUiState(
    val deckTitle: String = "",
    val cards: List<Card> = emptyList(),
    val isLoading: Boolean = true
)

class ManageDeckCardsViewModel(
    app: Application,
    private val deckId: String
) : AndroidViewModel(app) {
    private val db = DatabaseProvider.get(app.applicationContext)
    private val userRepo = UserDeckRepository(db.userDeckDao(), db.userCardDao())

    private val _state = MutableStateFlow(ManageDeckCardsUiState())
    val state: StateFlow<ManageDeckCardsUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val deck = userRepo.loadDeck(deckId)
            _state.value = ManageDeckCardsUiState(
                deckTitle = deck?.title ?: "",
                cards = deck?.cards ?: emptyList(),
                isLoading = false
            )
        }
    }
}
