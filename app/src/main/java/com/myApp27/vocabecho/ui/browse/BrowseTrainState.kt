package com.myApp27.vocabecho.ui.browse

data class BrowseTrainState(
    val isLoading: Boolean = true,
    val deckTitle: String = "",
    val cardsTotal: Int = 0,
    val currentIndex: Int = 0,
    val frontText: String = "",
    val backText: String = "",
    val isFinished: Boolean = false,
    val errorMessage: String? = null
)

