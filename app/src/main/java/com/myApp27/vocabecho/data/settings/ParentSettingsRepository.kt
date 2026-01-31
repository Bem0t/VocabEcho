package com.myApp27.vocabecho.data.settings

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.myApp27.vocabecho.domain.model.ParentSettings

private val Context.dataStore by preferencesDataStore(name = "parent_settings")

class ParentSettingsRepository(private val context: Context) {

    private object Keys {
        val AGAIN = intPreferencesKey("again_days")
        val HARD = intPreferencesKey("hard_days")
        val EASY = intPreferencesKey("easy_days")
    }

    val settingsFlow: Flow<ParentSettings> = context.dataStore.data.map { prefs ->
        ParentSettings(
            againDays = prefs[Keys.AGAIN] ?: 0,
            hardDays = prefs[Keys.HARD] ?: 1,
            easyDays = prefs[Keys.EASY] ?: 3
        )
    }

    suspend fun update(againDays: Int, hardDays: Int, easyDays: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AGAIN] = againDays
            prefs[Keys.HARD] = hardDays
            prefs[Keys.EASY] = easyDays
        }
    }
}