package com.myApp27.vocabecho.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myApp27.vocabecho.data.UserDeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditUserCardUiState(
    val front: String = "",
    val back: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val notFound: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

class EditUserCardViewModel(
    app: Application,
    private val deckId: String,
    private val cardId: String
) : AndroidViewModel(app) {
    private val db = DatabaseProvider.get(app.applicationContext)
    private val userRepo = UserDeckRepository(db.userDeckDao(), db.userCardDao())

    private val _state = MutableStateFlow(EditUserCardUiState())
    val state: StateFlow<EditUserCardUiState> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val card = userRepo.getCard(deckId, cardId)
            if (card != null) {
                _state.value = EditUserCardUiState(
                    front = card.front,
                    back = card.back,
                    isLoading = false,
                    notFound = false
                )
            } else {
                _state.value = EditUserCardUiState(
                    isLoading = false,
                    notFound = true
                )
            }
        }
    }

    fun onFrontChanged(value: String) {
        _state.value = _state.value.copy(front = value, errorMessage = null)
    }

    fun onBackChanged(value: String) {
        _state.value = _state.value.copy(back = value, errorMessage = null)
    }

    fun save(onSuccess: () -> Unit) {
        val front = _state.value.front.trim()
        val back = _state.value.back.trim()

        if (front.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Введите лицевую сторону")
            return
        }
        if (back.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Введите оборотную сторону")
            return
        }

        _state.value = _state.value.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val success = userRepo.updateCard(deckId, cardId, front, back)
                if (success) {
                    _state.value = _state.value.copy(isSaving = false, savedSuccessfully = true)
                    onSuccess()
                } else {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = "Не удалось сохранить"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = "Ошибка: ${e.message}"
                )
            }
        }
    }
}
