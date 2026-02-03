package com.myApp27.vocabecho.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myApp27.vocabecho.data.UserDeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import com.myApp27.vocabecho.domain.model.CardType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddCardToDeckUiState(
    val selectedType: CardType = CardType.BASIC,
    // Fields for BASIC / BASIC_TYPED
    val front: String = "",
    val back: String = "",
    // Fields for CLOZE
    val clozeText: String = "",
    val clozeAnswer: String = "",
    val clozeHint: String = "",
    // UI state
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

class AddCardToDeckViewModel(
    app: Application,
    private val deckId: String
) : AndroidViewModel(app) {
    private val db = DatabaseProvider.get(app.applicationContext)
    private val userRepo = UserDeckRepository(db.userDeckDao(), db.userCardDao())

    private val _state = MutableStateFlow(AddCardToDeckUiState())
    val state: StateFlow<AddCardToDeckUiState> = _state

    fun onTypeChanged(type: CardType) {
        val current = _state.value
        _state.value = when (type) {
            CardType.CLOZE -> {
                current.copy(
                    selectedType = type,
                    front = "",
                    back = "",
                    errorMessage = null
                )
            }
            else -> {
                current.copy(
                    selectedType = type,
                    clozeText = "",
                    clozeAnswer = "",
                    clozeHint = "",
                    errorMessage = null
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

    fun onClozeTextChanged(value: String) {
        _state.value = _state.value.copy(clozeText = value, errorMessage = null)
    }

    fun onClozeAnswerChanged(value: String) {
        _state.value = _state.value.copy(clozeAnswer = value, errorMessage = null)
    }

    fun onClozeHintChanged(value: String) {
        _state.value = _state.value.copy(clozeHint = value, errorMessage = null)
    }

    fun save(onSuccess: () -> Unit) {
        val type = _state.value.selectedType

        // Validate based on type
        when (type) {
            CardType.BASIC, CardType.BASIC_TYPED -> {
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
            }
            CardType.CLOZE -> {
                val clozeText = _state.value.clozeText.trim()
                val clozeAnswer = _state.value.clozeAnswer.trim()

                if (clozeText.isBlank()) {
                    _state.value = _state.value.copy(errorMessage = "Введите текст предложения")
                    return
                }
                if (clozeAnswer.isBlank()) {
                    _state.value = _state.value.copy(errorMessage = "Введите скрываемое слово/фразу")
                    return
                }
                // Case-insensitive check
                if (!clozeText.contains(clozeAnswer, ignoreCase = true)) {
                    _state.value = _state.value.copy(errorMessage = "Скрываемое слово не найдено в тексте")
                    return
                }
            }
            // BASIC_REVERSED is not selectable, but handle for exhaustive when
            CardType.BASIC_REVERSED -> {
                val front = _state.value.front.trim()
                val back = _state.value.back.trim()
                if (front.isBlank() || back.isBlank()) {
                    _state.value = _state.value.copy(errorMessage = "Заполните все поля")
                    return
                }
            }
        }

        _state.value = _state.value.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val draft = DraftCard(
                    type = type,
                    front = _state.value.front,
                    back = _state.value.back,
                    clozeText = _state.value.clozeText.ifBlank { null },
                    clozeAnswer = _state.value.clozeAnswer.ifBlank { null },
                    clozeHint = _state.value.clozeHint.ifBlank { null }
                )

                val success = userRepo.addCardToDeck(deckId, draft)
                if (success) {
                    _state.value = _state.value.copy(isSaving = false, savedSuccessfully = true)
                    onSuccess()
                } else {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = "Не удалось сохранить карточку"
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

class AddCardToDeckViewModelFactory(
    private val app: Application,
    private val deckId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddCardToDeckViewModel(app, deckId) as T
    }
}
