package com.myApp27.vocabecho.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserCardDao {

    @Query("SELECT * FROM user_cards WHERE deckId = :deckId")
    suspend fun getByDeckId(deckId: String): List<UserCardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<UserCardEntity>)
}
