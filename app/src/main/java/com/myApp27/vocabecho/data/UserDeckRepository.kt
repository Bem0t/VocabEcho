package com.myApp27.vocabecho.data

import com.myApp27.vocabecho.data.db.UserCardDao
import com.myApp27.vocabecho.data.db.UserCardEntity
import com.myApp27.vocabecho.data.db.UserDeckDao
import com.myApp27.vocabecho.data.db.UserDeckEntity
import com.myApp27.vocabecho.domain.model.Card
import com.myApp27.vocabecho.domain.model.Deck
import com.myApp27.vocabecho.domain.time.TimeProvider
import java.util.UUID

class UserDeckRepository(
    private val deckDao: UserDeckDao,
    private val cardDao: UserCardDao
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
}
