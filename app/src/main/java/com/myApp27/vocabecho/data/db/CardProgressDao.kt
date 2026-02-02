package com.myApp27.vocabecho.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: CardProgressEntity)

    @Query("SELECT * FROM card_progress WHERE cardId = :cardId LIMIT 1")
    suspend fun getByCardId(cardId: String): CardProgressEntity?

    // Карточки, которые пора повторять сегодня или раньше
    @Query("""
        SELECT * FROM card_progress 
        WHERE deckId = :deckId AND dueEpochDay <= :todayEpochDay
        ORDER BY dueEpochDay ASC
    """)
    suspend fun getDueForDeck(deckId: String, todayEpochDay: Long): List<CardProgressEntity>

    // Прогресс всех карточек колоды (чтобы понимать, какие NEW)
    @Query("SELECT * FROM card_progress WHERE deckId = :deckId")
    suspend fun getAllForDeck(deckId: String): List<CardProgressEntity>

    // Количество карточек, повторённых сегодня
    @Query("SELECT COUNT(*) FROM card_progress WHERE deckId = :deckId AND lastReviewedEpochDay = :todayEpochDay")
    suspend fun countReviewedToday(deckId: String, todayEpochDay: Long): Int

    @Query("DELETE FROM card_progress WHERE cardId = :cardId")
    suspend fun deleteByCardId(cardId: String): Int
}