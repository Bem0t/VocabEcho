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
    val isLoading: Boolean = true,
    val showDeleteConfirmForDeckId: String? = null,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null
)

class ManageDecksViewModel(app: Application) : AndroidViewModel(app) {
    private val db = DatabaseProvider.get(app.applicationContext)
    private val userRepo = UserDeckRepository(
        db.userDeckDao(),
        db.userCardDao(),
        db.cardProgressDao(),
        db.cardStatsDao()
    )

    private val _state = MutableStateFlow(ManageDecksUiState())
    val state: StateFlow<ManageDecksUiState> = _state

    init {
        loadDecks()
    }

    fun loadDecks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val decks = userRepo.loadAllDecks()
            _state.value = _state.value.copy(decks = decks, isLoading = false)
        }
    }

    fun requestDelete(deckId: String) {
        _state.value = _state.value.copy(showDeleteConfirmForDeckId = deckId, errorMessage = null)
    }

    fun cancelDelete() {
        _state.value = _state.value.copy(showDeleteConfirmForDeckId = null)
    }

    fun confirmDelete() {
        val deckId = _state.value.showDeleteConfirmForDeckId ?: return
        _state.value = _state.value.copy(isDeleting = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val success = userRepo.deleteDeck(deckId)
                if (success) {
                    _state.value = _state.value.copy(
                        showDeleteConfirmForDeckId = null,
                        isDeleting = false
                    )
                    loadDecks()
                } else {
                    _state.value = _state.value.copy(
                        showDeleteConfirmForDeckId = null,
                        isDeleting = false,
                        errorMessage = "Не удалось удалить колоду"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    showDeleteConfirmForDeckId = null,
                    isDeleting = false,
                    errorMessage = "Ошибка: ${e.message}"
                )
            }
        }
    }
}
