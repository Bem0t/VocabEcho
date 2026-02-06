package com.myApp27.vocabecho.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Deck(
    val id: String,
    val title: String,
    val cards: List<Card>,
    val imageUri: String? = null
)

@Serializable
data class Card(
    val id: String,
    val front: String = "",
    val back: String = "",
    /**
     * Card type determining learn behavior.
     * Default is BASIC_TYPED for built-in decks.
     */
    val type: CardType = CardType.BASIC_TYPED,
    /**
     * For CLOZE: the full sentence containing the answer.
     */
    val clozeText: String? = null,
    /**
     * For CLOZE: the hidden word/phrase.
     */
    val clozeAnswer: String? = null,
    /**
     * For CLOZE: optional hint shown in brackets.
     */
    val clozeHint: String? = null
) {
    /**
     * Whether this card expects user to type the answer.
     * Derived from card type for centralized logic.
     */
    val expectsTyping: Boolean get() = type.expectsTyping()
}