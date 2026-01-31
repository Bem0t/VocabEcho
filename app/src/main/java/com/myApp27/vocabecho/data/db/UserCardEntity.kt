package com.myApp27.vocabecho.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_cards",
    indices = [Index(value = ["deckId"])]
)
data class UserCardEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    val createdAtEpochDay: Long
)
