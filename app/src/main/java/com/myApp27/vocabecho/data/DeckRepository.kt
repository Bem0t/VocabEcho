package com.myApp27.vocabecho.data

import android.content.Context
import com.myApp27.vocabecho.domain.model.Deck
import kotlinx.serialization.json.Json

class DeckRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val fileByDeckId = mapOf(
        "animals" to "animals.json",
        "food" to "food.json",
        "transport" to "transport.json",
        "home" to "home.json",
        "colors" to "colors.json",
        "family" to "family.json",
        "school" to "school.json"
    )

    fun loadDeck(deckId: String): Deck? {
        val fileName = fileByDeckId[deckId] ?: return null
        val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return json.decodeFromString(Deck.serializer(), text)
    }

    fun loadAllDecks(): List<Deck> =
        fileByDeckId.values.map { fileName ->
            val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
            json.decodeFromString(Deck.serializer(), text)
        }
}
