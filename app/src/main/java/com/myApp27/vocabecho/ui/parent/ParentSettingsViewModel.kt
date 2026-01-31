package com.myApp27.vocabecho.ui.parent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myApp27.vocabecho.data.settings.ParentSettingsRepository
import com.myApp27.vocabecho.domain.model.ParentSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ParentSettingsUiState(
    val againDays: String = "",
    val hardDays: String = "",
    val easyDays: String = "",
    val savedMessage: String? = null
)

class ParentSettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ParentSettingsRepository(app.applicationContext)

    private val _state = MutableStateFlow(ParentSettingsUiState())
    val state: StateFlow<ParentSettingsUiState> = _state

    init {
        viewModelScope.launch {
            repo.settingsFlow.collect { s ->
                _state.value = _state.value.copy(
                    againDays = s.againDays.toString(),
                    hardDays = s.hardDays.toString(),
                    easyDays = s.easyDays.toString(),
                    savedMessage = null
                )
            }
        }
    }

    fun onAgainChanged(v: String) { _state.value = _state.value.copy(againDays = v, savedMessage = null) }
    fun onHardChanged(v: String) { _state.value = _state.value.copy(hardDays = v, savedMessage = null) }
    fun onEasyChanged(v: String) { _state.value = _state.value.copy(easyDays = v, savedMessage = null) }

    fun save() {
        val a = _state.value.againDays.toIntOrNull()
        val h = _state.value.hardDays.toIntOrNull()
        val e = _state.value.easyDays.toIntOrNull()

        // простая валидация (для диплома можно описать как "предотвращение ошибок ввода")
        if (a == null || h == null || e == null) {
            _state.value = _state.value.copy(savedMessage = "Введите целые числа")
            return
        }
        if (a < 0 || h < 0 || e < 0) {
            _state.value = _state.value.copy(savedMessage = "Интервалы не могут быть отрицательными")
            return
        }

        viewModelScope.launch {
            repo.update(againDays = a, hardDays = h, easyDays = e)
            _state.value = _state.value.copy(savedMessage = "Сохранено")
        }
    }
}