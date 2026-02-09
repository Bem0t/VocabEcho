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
import com.myApp27.vocabecho.domain.model.CardType
import com.myApp27.vocabecho.domain.model.Deck
import com.myApp27.vocabecho.domain.time.TimeProvider
import com.myApp27.vocabecho.ui.parent.DraftCard
import java.util.UUID

class UserDeckRepository(
    private val deckDao: UserDeckDao,
    private val cardDao: UserCardDao,
    private val progressDao: CardProgressDao? = null,
    private val statsDao: CardStatsDao? = null
) {
    /**
     * Create a new deck with cards (legacy method for simple front/back pairs).
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
                createdAtEpochDay = today,
                type = CardType.BASIC.name
            )
        }
        cardDao.insertAll(cardEntities)

        return deckId
    }

    /**
     * Create a new deck with draft cards supporting all card types.
     * @param title Deck title
     * @param imageUri Optional cover image URI
     * @param draftCards List of DraftCard with type and fields
     * @return The created deck's ID
     */
    suspend fun createDeckWithDraftCards(
        title: String,
        imageUri: String?,
        draftCards: List<DraftCard>
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

        val cardEntities = draftCards.mapIndexed { index, draft ->
            when (draft.type) {
                CardType.CLOZE -> {
                    UserCardEntity(
                        id = "${deckId}_card_$index",
                        deckId = deckId,
                        front = "",
                        back = "",
                        createdAtEpochDay = today,
                        type = CardType.CLOZE.name,
                        clozeText = draft.clozeText?.trim(),
                        clozeAnswer = draft.clozeAnswer?.trim(),
                        clozeHint = draft.clozeHint?.trim()
                    )
                }
                else -> {
                    UserCardEntity(
                        id = "${deckId}_card_$index",
                        deckId = deckId,
                        front = draft.front.trim(),
                        back = draft.back.trim(),
                        createdAtEpochDay = today,
                        type = draft.type.name,
                        clozeText = null,
                        clozeAnswer = null,
                        clozeHint = null
                    )
                }
            }
        }
        cardDao.insertAll(cardEntities)

        return deckId
    }

    /**
     * Load a single deck by ID.
     * Cards are converted to Card model with proper question/answer based on type.
     */
    suspend fun loadDeck(deckId: String): Deck? {
        val deckEntity = deckDao.getById(deckId) ?: return null
        val cardEntities = cardDao.getByDeckId(deckId)

        return Deck(
            id = deckEntity.id,
            title = deckEntity.title,
            cards = cardEntities.map { entityToCard(it) },
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
                cards = cardEntities.map { entityToCard(it) },
                imageUri = deckEntity.imageUri
            )
        }
    }

    /**
     * Convert UserCardEntity to Card model with proper question/answer based on type.
     * This ensures all card types work in the learn flow.
     * Preserves card type so UI can determine if typing is expected.
     */
    private fun entityToCard(entity: UserCardEntity): Card {
        val type = CardType.fromString(entity.type)

        return when (type) {
            CardType.BASIC, CardType.BASIC_REVERSED -> {
                // Use front/back as-is, type determines no typing required
                Card(id = entity.id, front = entity.front, back = entity.back, type = CardType.BASIC)
            }
            CardType.BASIC_TYPED -> {
                // Use front/back as-is, type determines typing required
                Card(id = entity.id, front = entity.front, back = entity.back, type = CardType.BASIC_TYPED)
            }
            else -> {
                // Generate question with placeholder, answer is the hidden word
                val clozeText = entity.clozeText
                val clozeAnswer = entity.clozeAnswer

                if (clozeText.isNullOrBlank() || clozeAnswer.isNullOrBlank()) {
                    // Fallback if cloze data is missing - use BASIC_TYPED as safe default
                    Card(id = entity.id, front = entity.front, back = entity.back, type = CardType.BASIC_TYPED)
                } else {
                    val placeholder = if (!entity.clozeHint.isNullOrBlank()) {
                        "[${entity.clozeHint}]"
                    } else {
                        "[...]"
                    }
                    // Case-insensitive, first occurrence only
                    val questionText = replaceFirstIgnoreCase(clozeText, clozeAnswer, placeholder)
                    Card(
                        id = entity.id,
                        front = questionText,
                        back = clozeAnswer,
                        type = CardType.CLOZE,
                        clozeText = clozeText,
                        clozeAnswer = clozeAnswer,
                        clozeHint = entity.clozeHint
                    )
                }
            }
        }
    }

    /**
     * Replace first occurrence of target in text, ignoring case.
     * Returns original text if target not found.
     */
    private fun replaceFirstIgnoreCase(text: String, target: String, replacement: String): String {
        val idx = text.indexOf(target, ignoreCase = true)
        if (idx < 0) return text
        return text.substring(0, idx) + replacement + text.substring(idx + target.length)
    }

    /**
     * Get a single card by ID.
     * Returns Card with proper question/answer based on type.
     */
    suspend fun getCard(deckId: String, cardId: String): Card? {
        val entity = cardDao.getById(deckId, cardId) ?: return null
        return entityToCard(entity)
    }

    /**
     * Get a single card entity by ID (raw, with all fields).
     * Useful for editing cards.
     */
    suspend fun getCardEntity(deckId: String, cardId: String): UserCardEntity? {
        return cardDao.getById(deckId, cardId)
    }

    /**
     * Update card front and back text (legacy method).
     * @return true if update succeeded
     */
    suspend fun updateCard(deckId: String, cardId: String, front: String, back: String): Boolean {
        val rowsUpdated = cardDao.updateText(deckId, cardId, front.trim(), back.trim())
        return rowsUpdated == 1
    }

    /**
     * Update card with full type and field support.
     * @return true if update succeeded
     */
    suspend fun updateCardFull(
        deckId: String,
        cardId: String,
        type: CardType,
        front: String,
        back: String,
        clozeText: String?,
        clozeAnswer: String?,
        clozeHint: String?
    ): Boolean {
        val actualFront: String
        val actualBack: String
        val actualClozeText: String?
        val actualClozeAnswer: String?
        val actualClozeHint: String?

        when (type) {
            CardType.BASIC, CardType.BASIC_TYPED, CardType.BASIC_REVERSED -> {
                actualFront = front.trim()
                actualBack = back.trim()
                actualClozeText = null
                actualClozeAnswer = null
                actualClozeHint = null
            }
            CardType.CLOZE -> {
                actualFront = ""
                actualBack = ""
                actualClozeText = clozeText?.trim()
                actualClozeAnswer = clozeAnswer?.trim()
                actualClozeHint = clozeHint?.trim()?.ifBlank { null }
            }
        }

        val rowsUpdated = cardDao.updateCardFull(
            deckId = deckId,
            cardId = cardId,
            type = type.name,
            front = actualFront,
            back = actualBack,
            clozeText = actualClozeText,
            clozeAnswer = actualClozeAnswer,
            clozeHint = actualClozeHint
        )
        return rowsUpdated == 1
    }

    /**
     * Add a single card to an existing deck.
     * @return true if insert succeeded
     */
    suspend fun addCardToDeck(deckId: String, draft: DraftCard): Boolean {
        val today = TimeProvider.todayEpochDay()
        val cardId = UUID.randomUUID().toString()

        val entity = when (draft.type) {
            CardType.BASIC, CardType.BASIC_TYPED, CardType.BASIC_REVERSED -> {
                UserCardEntity(
                    id = cardId,
                    deckId = deckId,
                    front = draft.front.trim(),
                    back = draft.back.trim(),
                    createdAtEpochDay = today,
                    type = draft.type.name,
                    clozeText = null,
                    clozeAnswer = null,
                    clozeHint = null
                )
            }
            CardType.CLOZE -> {
                UserCardEntity(
                    id = cardId,
                    deckId = deckId,
                    front = "",
                    back = "",
                    createdAtEpochDay = today,
                    type = CardType.CLOZE.name,
                    clozeText = draft.clozeText?.trim(),
                    clozeAnswer = draft.clozeAnswer?.trim(),
                    clozeHint = draft.clozeHint?.trim()
                )
            }
        }

        return try {
            cardDao.insertAll(listOf(entity))
            true
        } catch (e: Exception) {
            false
        }
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
