package com.myApp27.vocabecho.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserCardDao {

    @Query("SELECT * FROM user_cards WHERE deckId = :deckId")
    suspend fun getByDeckId(deckId: String): List<UserCardEntity>

    @Query("SELECT * FROM user_cards WHERE id = :cardId AND deckId = :deckId LIMIT 1")
    suspend fun getById(deckId: String, cardId: String): UserCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<UserCardEntity>)

    @Query("UPDATE user_cards SET front = :front, back = :back WHERE id = :cardId AND deckId = :deckId")
    suspend fun updateText(deckId: String, cardId: String, front: String, back: String): Int

    @Query("""
        UPDATE user_cards
        SET type = :type,
            front = :front,
            back = :back,
            clozeText = :clozeText,
            clozeAnswer = :clozeAnswer,
            clozeHint = :clozeHint
        WHERE id = :cardId AND deckId = :deckId
    """)
    suspend fun updateCardFull(
        deckId: String,
        cardId: String,
        type: String?,
        front: String,
        back: String,
        clozeText: String?,
        clozeAnswer: String?,
        clozeHint: String?
    ): Int

    @Query("DELETE FROM user_cards WHERE id = :cardId AND deckId = :deckId")
    suspend fun deleteById(deckId: String, cardId: String): Int

    @Query("DELETE FROM user_cards WHERE deckId = :deckId")
    suspend fun deleteByDeckId(deckId: String): Int
}
