package com.myApp27.vocabecho.data

import android.content.Context
import com.myApp27.vocabecho.domain.model.Card
import com.myApp27.vocabecho.domain.model.CardType
import com.myApp27.vocabecho.domain.model.Deck
import kotlinx.serialization.json.Json

class DeckRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val fileByDeckId = mapOf(
        "animals" to "animals.json",
        "food" to "food.json",
        "transport" to "transport.json",
        "home" to "home.json"
    )

    fun loadDeck(deckId: String): Deck? {
        val fileName = fileByDeckId[deckId] ?: return null
        val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val rawDeck = json.decodeFromString(Deck.serializer(), text)
        // Built-in decks use BASIC_TYPED for all cards (requires typing)
        return rawDeck.copy(cards = rawDeck.cards.map { it.withType(CardType.BASIC_TYPED) })
    }

    fun loadAllDecks(): List<Deck> =
        fileByDeckId.values.map { fileName ->
            val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val rawDeck = json.decodeFromString(Deck.serializer(), text)
            // Built-in decks use BASIC_TYPED for all cards (requires typing)
            rawDeck.copy(cards = rawDeck.cards.map { it.withType(CardType.BASIC_TYPED) })
        }

    /**
     * Helper to create a copy of Card with specified type.
     * Used because Card.copy() doesn't work well with @Transient fields from JSON.
     */
    private fun Card.withType(cardType: CardType): Card =
        Card(id = id, front = front, back = back, type = cardType)
}