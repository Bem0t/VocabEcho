package com.myApp27.vocabecho.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myApp27.vocabecho.data.UserDeckRepository
import com.myApp27.vocabecho.data.db.DatabaseProvider
import com.myApp27.vocabecho.domain.model.CardType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditUserCardUiState(
    // Card type
    val selectedType: CardType = CardType.BASIC,
    // Fields for BASIC / BASIC_TYPED
    val front: String = "",
    val back: String = "",
    // Fields for CLOZE
    val clozeText: String = "",
    val clozeAnswer: String = "",
    val clozeHint: String = "",
    // UI state
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
            val entity = userRepo.getCardEntity(deckId, cardId)
            if (entity != null) {
                val type = CardType.fromString(entity.type)
                // Map BASIC_REVERSED to BASIC for editing
                val displayType = if (type == CardType.BASIC_REVERSED) CardType.BASIC else type
                _state.value = when (displayType) {
                    CardType.CLOZE -> {
                        EditUserCardUiState(
                            selectedType = CardType.CLOZE,
                            front = "",
                            back = "",
                            clozeText = entity.clozeText ?: "",
                            clozeAnswer = entity.clozeAnswer ?: "",
                            clozeHint = entity.clozeHint ?: "",
                            isLoading = false,
                            notFound = false
                        )
                    }
                    else -> {
                        EditUserCardUiState(
                            selectedType = displayType,
                            front = entity.front,
                            back = entity.back,
                            clozeText = "",
                            clozeAnswer = "",
                            clozeHint = "",
                            isLoading = false,
                            notFound = false
                        )
                    }
                }
            } else {
                _state.value = EditUserCardUiState(
                    isLoading = false,
                    notFound = true
                )
            }
        }
    }

    fun onTypeChanged(type: CardType) {
        val current = _state.value
        _state.value = when (type) {
            CardType.CLOZE -> {
                // Switching to CLOZE: clear front/back, keep cloze fields
                current.copy(
                    selectedType = type,
                    front = "",
                    back = "",
                    errorMessage = null
                )
            }
            else -> {
                // Switching away from CLOZE: clear cloze fields, keep front/back
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
                    _state.value = _state.value.copy(errorMessage = "Скрываемая фраза не найдена в тексте. Проверьте написание.")
                    return
                }
            }
            else -> { /* BASIC_REVERSED deprecated, treat as BASIC */ }
        }

        _state.value = _state.value.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val success = userRepo.updateCardFull(
                    deckId = deckId,
                    cardId = cardId,
                    type = type,
                    front = _state.value.front,
                    back = _state.value.back,
                    clozeText = _state.value.clozeText.ifBlank { null },
                    clozeAnswer = _state.value.clozeAnswer.ifBlank { null },
                    clozeHint = _state.value.clozeHint.ifBlank { null }
                )
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
