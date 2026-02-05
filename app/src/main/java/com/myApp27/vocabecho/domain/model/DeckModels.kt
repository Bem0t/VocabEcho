package com.myApp27.vocabecho.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    val front: String,
    val back: String,
    /**
     * Card type determining learn behavior.
     * Transient because JSON assets don't have this field.
     * Default is BASIC, but will be overridden to BASIC_TYPED for built-in decks.
     */
    @Transient
    val type: CardType = CardType.BASIC
) {
    /**
     * Whether this card expects user to type the answer.
     * Derived from card type for centralized logic.
     */
    val expectsTyping: Boolean get() = type.expectsTyping()
}