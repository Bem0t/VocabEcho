package com.myApp27.vocabecho.ui.browse

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BrowseTrainViewModelFactory(
    private val deckId: String,
    private val app: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrowseTrainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BrowseTrainViewModel(deckId, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

