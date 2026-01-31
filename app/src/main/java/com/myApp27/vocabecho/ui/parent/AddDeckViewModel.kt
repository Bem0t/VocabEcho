package com.myApp27.vocabecho.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myApp27.vocabecho.data.UserDeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddDeckUiState(
    val title: String = "",
    val currentFront: String = "",
    val currentBack: String = "",
    val cards: List<Pair<String, String>> = emptyList(),
    val selectedImageUri: String? = null,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

class AddDeckViewModel(app: Application) : AndroidViewModel(app) {
    private val db = DatabaseProvider.get(app.applicationContext)
    private val userRepo = UserDeckRepository(db.userDeckDao(), db.userCardDao())

    private val _state = MutableStateFlow(AddDeckUiState())
    val state: StateFlow<AddDeckUiState> = _state

    fun onTitleChanged(value: String) {
        _state.value = _state.value.copy(title = value, errorMessage = null)
    }

    fun onFrontChanged(value: String) {
        _state.value = _state.value.copy(currentFront = value)
    }

    fun onBackChanged(value: String) {
        _state.value = _state.value.copy(currentBack = value)
    }

    fun onImageSelected(uri: String?) {
        _state.value = _state.value.copy(selectedImageUri = uri)
    }

    fun addCard() {
        val front = _state.value.currentFront.trim()
        val back = _state.value.currentBack.trim()

        if (front.isBlank() || back.isBlank()) return

        _state.value = _state.value.copy(
            cards = _state.value.cards + (front to back),
            currentFront = "",
            currentBack = ""
        )
    }

    fun removeLastCard() {
        if (_state.value.cards.isEmpty()) return
        _state.value = _state.value.copy(
            cards = _state.value.cards.dropLast(1)
        )
    }

    fun saveDeck(onSuccess: () -> Unit) {
        val title = _state.value.title.trim()
        val cards = _state.value.cards

        if (title.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Введите название колоды")
            return
        }
        if (cards.isEmpty()) {
            _state.value = _state.value.copy(errorMessage = "Добавьте хотя бы одну карточку")
            return
        }

        _state.value = _state.value.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                userRepo.createDeckWithCards(title, _state.value.selectedImageUri, cards)
                _state.value = _state.value.copy(isSaving = false, savedSuccessfully = true)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = "Ошибка сохранения: ${e.message}"
                )
            }
        }
    }
}
