package com.myApp27.vocabecho.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myApp27.vocabecho.data.UserDeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import com.myApp27.vocabecho.domain.model.Deck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ManageDecksUiState(
    val decks: List<Deck> = emptyList(),
    val isLoading: Boolean = true
)

class ManageDecksViewModel(app: Application) : AndroidViewModel(app) {
    private val db = DatabaseProvider.get(app.applicationContext)
    private val userRepo = UserDeckRepository(db.userDeckDao(), db.userCardDao())

    private val _state = MutableStateFlow(ManageDecksUiState())
    val state: StateFlow<ManageDecksUiState> = _state

    init {
        loadDecks()
    }

    fun loadDecks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val decks = userRepo.loadAllDecks()
            _state.value = ManageDecksUiState(decks = decks, isLoading = false)
        }
    }
}
