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
    val isLoading: Boolean = true,
    val showDeleteConfirmForCardId: String? = null,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null
)

class ManageDeckCardsViewModel(
    app: Application,
    private val deckId: String
) : AndroidViewModel(app) {
    private val db = DatabaseProvider.get(app.applicationContext)
    private val userRepo = UserDeckRepository(
        db.userDeckDao(),
        db.userCardDao(),
        db.cardProgressDao(),
        db.cardStatsDao()
    )

    private val _state = MutableStateFlow(ManageDeckCardsUiState())
    val state: StateFlow<ManageDeckCardsUiState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val deck = userRepo.loadDeck(deckId)
            _state.value = _state.value.copy(
                deckTitle = deck?.title ?: "",
                cards = deck?.cards ?: emptyList(),
                isLoading = false
            )
        }
    }

    fun requestDelete(cardId: String) {
        _state.value = _state.value.copy(showDeleteConfirmForCardId = cardId, errorMessage = null)
    }

    fun cancelDelete() {
        _state.value = _state.value.copy(showDeleteConfirmForCardId = null)
    }

    fun confirmDelete() {
        val cardId = _state.value.showDeleteConfirmForCardId ?: return
        _state.value = _state.value.copy(isDeleting = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val success = userRepo.deleteCard(deckId, cardId)
                if (success) {
                    _state.value = _state.value.copy(
                        showDeleteConfirmForCardId = null,
                        isDeleting = false
                    )
                    load()
                } else {
                    _state.value = _state.value.copy(
                        showDeleteConfirmForCardId = null,
                        isDeleting = false,
                        errorMessage = "Не удалось удалить карточку"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    showDeleteConfirmForCardId = null,
                    isDeleting = false,
                    errorMessage = "Ошибка: ${e.message}"
                )
            }
        }
    }
}
