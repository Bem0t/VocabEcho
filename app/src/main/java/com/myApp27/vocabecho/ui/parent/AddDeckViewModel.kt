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

/**
 * Draft card data for creating new cards with different types.
 */
data class DraftCard(
    val type: CardType,
    // For BASIC / BASIC_REVERSED / BASIC_TYPED
    val front: String = "",
    val back: String = "",
    // For CLOZE
    val clozeText: String? = null,
    val clozeAnswer: String? = null,
    val clozeHint: String? = null
) {
    /**
     * Display text for the card list preview.
     */
    fun displayText(): String {
        return when (type) {
            CardType.CLOZE -> {
                val question = clozeText?.replace(clozeAnswer ?: "", "[...]") ?: ""
                question
            }
            else -> "$front — $back"
        }
    }

    /**
     * Short type label for badges.
     */
    fun typeLabel(): String = when (type) {
        CardType.BASIC -> "BASIC"
        CardType.BASIC_REVERSED -> "x2"
        CardType.BASIC_TYPED -> "TYPED"
        CardType.CLOZE -> "CLOZE"
    }
}

data class AddDeckUiState(
    val title: String = "",
    // Card type selection
    val selectedCardType: CardType = CardType.BASIC,
    // Fields for BASIC / BASIC_REVERSED / BASIC_TYPED
    val currentFront: String = "",
    val currentBack: String = "",
    // Fields for CLOZE
    val currentClozeText: String = "",
    val currentClozeAnswer: String = "",
    val currentClozeHint: String = "",
    // Draft cards list
    val draftCards: List<DraftCard> = emptyList(),
    val selectedImageUri: String? = null,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null,
    val inputError: String? = null // For validation errors on current input
)

class AddDeckViewModel(app: Application) : AndroidViewModel(app) {
    private val db = DatabaseProvider.get(app.applicationContext)
    private val userRepo = UserDeckRepository(db.userDeckDao(), db.userCardDao())

    private val _state = MutableStateFlow(AddDeckUiState())
    val state: StateFlow<AddDeckUiState> = _state

    fun onTitleChanged(value: String) {
        _state.value = _state.value.copy(title = value, errorMessage = null)
    }

    fun onCardTypeChanged(type: CardType) {
        _state.value = _state.value.copy(
            selectedCardType = type,
            inputError = null
        )
    }

    fun onFrontChanged(value: String) {
        _state.value = _state.value.copy(currentFront = value, inputError = null)
    }

    fun onBackChanged(value: String) {
        _state.value = _state.value.copy(currentBack = value, inputError = null)
    }

    fun onClozeTextChanged(value: String) {
        _state.value = _state.value.copy(currentClozeText = value, inputError = null)
    }

    fun onClozeAnswerChanged(value: String) {
        _state.value = _state.value.copy(currentClozeAnswer = value, inputError = null)
    }

    fun onClozeHintChanged(value: String) {
        _state.value = _state.value.copy(currentClozeHint = value, inputError = null)
    }

    fun onImageSelected(uri: String?) {
        _state.value = _state.value.copy(selectedImageUri = uri)
    }

    fun addCard() {
        val type = _state.value.selectedCardType

        when (type) {
            CardType.BASIC, CardType.BASIC_REVERSED, CardType.BASIC_TYPED -> {
                val front = _state.value.currentFront.trim()
                val back = _state.value.currentBack.trim()

                if (front.isBlank()) {
                    _state.value = _state.value.copy(inputError = "Введите лицевую сторону")
                    return
                }
                if (back.isBlank()) {
                    _state.value = _state.value.copy(inputError = "Введите оборотную сторону")
                    return
                }

                val draft = DraftCard(
                    type = type,
                    front = front,
                    back = back
                )

                _state.value = _state.value.copy(
                    draftCards = _state.value.draftCards + draft,
                    currentFront = "",
                    currentBack = "",
                    inputError = null
                )
            }

            CardType.CLOZE -> {
                val clozeText = _state.value.currentClozeText.trim()
                val clozeAnswer = _state.value.currentClozeAnswer.trim()
                val clozeHint = _state.value.currentClozeHint.trim().ifBlank { null }

                if (clozeText.isBlank()) {
                    _state.value = _state.value.copy(inputError = "Введите текст предложения")
                    return
                }
                if (clozeAnswer.isBlank()) {
                    _state.value = _state.value.copy(inputError = "Введите скрываемое слово/фразу")
                    return
                }
                if (!clozeText.contains(clozeAnswer)) {
                    _state.value = _state.value.copy(inputError = "Скрываемое слово не найдено в тексте")
                    return
                }

                val draft = DraftCard(
                    type = CardType.CLOZE,
                    clozeText = clozeText,
                    clozeAnswer = clozeAnswer,
                    clozeHint = clozeHint
                )

                _state.value = _state.value.copy(
                    draftCards = _state.value.draftCards + draft,
                    currentClozeText = "",
                    currentClozeAnswer = "",
                    currentClozeHint = "",
                    inputError = null
                )
            }
        }
    }

    fun removeCard(index: Int) {
        if (index < 0 || index >= _state.value.draftCards.size) return
        _state.value = _state.value.copy(
            draftCards = _state.value.draftCards.filterIndexed { i, _ -> i != index }
        )
    }

    fun saveDeck(onSuccess: () -> Unit) {
        val title = _state.value.title.trim()
        val draftCards = _state.value.draftCards

        if (title.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Введите название колоды")
            return
        }
        if (draftCards.isEmpty()) {
            _state.value = _state.value.copy(errorMessage = "Добавьте хотя бы одну карточку")
            return
        }

        _state.value = _state.value.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                userRepo.createDeckWithDraftCards(title, _state.value.selectedImageUri, draftCards)
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
