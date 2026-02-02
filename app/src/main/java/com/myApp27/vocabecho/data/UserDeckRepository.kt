package com.myApp27.vocabecho.data

import com.myApp27.vocabecho.data.db.CardProgressDao
import com.myApp27.vocabecho.data.db.CardStatsDao
import com.myApp27.vocabecho.data.db.UserCardDao
import com.myApp27.vocabecho.data.db.UserCardEntity
import com.myApp27.vocabecho.data.db.UserDeckDao
import com.myApp27.vocabecho.data.db.UserDeckEntity
import com.myApp27.vocabecho.domain.model.Card
import com.myApp27.vocabecho.domain.model.CardInstance
import com.myApp27.vocabecho.domain.model.CardInstanceGenerator
import com.myApp27.vocabecho.domain.model.Deck
import com.myApp27.vocabecho.domain.time.TimeProvider
import java.util.UUID

class UserDeckRepository(
    private val deckDao: UserDeckDao,
    private val cardDao: UserCardDao,
    private val progressDao: CardProgressDao? = null,
    private val statsDao: CardStatsDao? = null
) {
    /**
     * Create a new deck with cards.
     * @param title Deck title
     * @param imageUri Optional cover image URI
     * @param cards List of (front, back) pairs
     * @return The created deck's ID
     */
    suspend fun createDeckWithCards(
        title: String,
        imageUri: String?,
        cards: List<Pair<String, String>>
    ): String {
        val today = TimeProvider.todayEpochDay()
        val deckId = UUID.randomUUID().toString()

        val deckEntity = UserDeckEntity(
            id = deckId,
            title = title.trim(),
            createdAtEpochDay = today,
            imageUri = imageUri
        )
        deckDao.insert(deckEntity)

        val cardEntities = cards.mapIndexed { index, (front, back) ->
            UserCardEntity(
                id = "${deckId}_card_$index",
                deckId = deckId,
                front = front.trim(),
                back = back.trim(),
                createdAtEpochDay = today
            )
        }
        cardDao.insertAll(cardEntities)

        return deckId
    }

    /**
     * Load a single deck by ID.
     */
    suspend fun loadDeck(deckId: String): Deck? {
        val deckEntity = deckDao.getById(deckId) ?: return null
        val cardEntities = cardDao.getByDeckId(deckId)

        return Deck(
            id = deckEntity.id,
            title = deckEntity.title,
            cards = cardEntities.map { Card(id = it.id, front = it.front, back = it.back) },
            imageUri = deckEntity.imageUri
        )
    }

    /**
     * Load all user-created decks.
     */
    suspend fun loadAllDecks(): List<Deck> {
        val deckEntities = deckDao.getAll()
        return deckEntities.map { deckEntity ->
            val cardEntities = cardDao.getByDeckId(deckEntity.id)
            Deck(
                id = deckEntity.id,
                title = deckEntity.title,
                cards = cardEntities.map { Card(id = it.id, front = it.front, back = it.back) },
                imageUri = deckEntity.imageUri
            )
        }
    }

    /**
     * Get a single card by ID.
     */
    suspend fun getCard(deckId: String, cardId: String): Card? {
        val entity = cardDao.getById(deckId, cardId) ?: return null
        return Card(id = entity.id, front = entity.front, back = entity.back)
    }

    /**
     * Update card front and back text.
     * @return true if update succeeded
     */
    suspend fun updateCard(deckId: String, cardId: String, front: String, back: String): Boolean {
        val rowsUpdated = cardDao.updateText(deckId, cardId, front.trim(), back.trim())
        return rowsUpdated == 1
    }

    /**
     * Delete a card and its related progress/stats.
     * @return true if delete succeeded
     */
    suspend fun deleteCard(deckId: String, cardId: String): Boolean {
        val rowsDeleted = cardDao.deleteById(deckId, cardId)
        // Clean up progress and stats (if DAOs are provided)
        progressDao?.deleteByCardId(cardId)
        statsDao?.deleteByCardId(cardId)
        return rowsDeleted == 1
    }

    /**
     * Delete a deck and all its cards, progress, and stats.
     * @return true if delete succeeded
     */
    suspend fun deleteDeck(deckId: String): Boolean {
        // Delete all cards for this deck
        cardDao.deleteByDeckId(deckId)
        // Clean up progress and stats (if DAOs are provided)
        progressDao?.deleteByDeckId(deckId)
        statsDao?.deleteByDeckId(deckId)
        // Delete the deck itself
        val rowsDeleted = deckDao.deleteById(deckId)
        return rowsDeleted == 1
    }

    // ========== CardInstance support for Anki-like card types ==========

    /**
     * Load CardInstances for a deck.
     * Generates instances based on card type (BASIC, BASIC_REVERSED, etc.)
     * 
     * Note: For BASIC_REVERSED, generates 2 instances per card.
     * Progress is currently tracked per noteId, not per instanceId.
     * TODO: Implement instance-level progress tracking in future increment.
     */
    suspend fun loadCardInstances(deckId: String): List<CardInstance> {
        val cardEntities = cardDao.getByDeckId(deckId)
        return CardInstanceGenerator.generateAll(cardEntities)
    }

    /**
     * Load raw card entities for a deck.
     * Useful when you need the original entity data.
     */
    suspend fun loadCardEntities(deckId: String): List<UserCardEntity> {
        return cardDao.getByDeckId(deckId)
    }
}
