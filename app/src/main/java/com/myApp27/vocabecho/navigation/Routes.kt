package com.myApp27.vocabecho.navigation

import android.net.Uri

object Routes {
    const val DECKS = "decks"
    const val LEARN = "learn/{deckId}"
    const val FEEDBACK = "feedback/{deckId}/{cardId}/{answer}"
    const val PARENT = "parent"
    const val ADD_DECK = "add_deck"
    const val MANAGE_DECKS = "manage_decks"

    fun learn(deckId: String) = "learn/$deckId"

    fun feedback(deckId: String, cardId: String, answer: String): String {
        val safe = Uri.encode(answer)
        return "feedback/$deckId/$cardId/$safe"
    }
}