package com.myApp27.vocabecho.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardStatsDao {

    @Query("SELECT * FROM card_stats WHERE cardId = :cardId LIMIT 1")
    suspend fun getByCardId(cardId: String): CardStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CardStatsEntity)
}
