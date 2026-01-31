package com.myApp27.vocabecho.data

import com.myApp27.vocabecho.domain.model.Deck

/**
 * Combines built-in (asset) decks with user-created decks.
 */
class CombinedDeckRepository(
    private val assetRepo: DeckRepository,
    private val userRepo: UserDeckRepository
) {
    // Built-in deck IDs that are reserved
    private val builtInDeckIds = setOf("animals", "food", "transport", "home")

    /**
     * Load all decks: built-in first, then user-created.
     */
    suspend fun loadAllDecks(): List<Deck> {
        val assetDecks = assetRepo.loadAllDecks()
        val userDecks = userRepo.loadAllDecks()
        return assetDecks + userDecks
    }

    /**
     * Load a single deck by ID.
     * Try built-in first, then user-created.
     */
    suspend fun loadDeck(deckId: String): Deck? {
        // Try built-in first
        if (deckId in builtInDeckIds) {
            return assetRepo.loadDeck(deckId)
        }
        // Then try user deck
        return userRepo.loadDeck(deckId)
    }
}
