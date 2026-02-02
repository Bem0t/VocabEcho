package com.myApp27.vocabecho.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDeckDao {

    @Query("SELECT * FROM user_decks ORDER BY createdAtEpochDay DESC")
    suspend fun getAll(): List<UserDeckEntity>

    @Query("SELECT * FROM user_decks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserDeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deck: UserDeckEntity)

    @Query("DELETE FROM user_decks WHERE id = :deckId")
    suspend fun deleteById(deckId: String): Int
}
