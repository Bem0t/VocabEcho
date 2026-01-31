package com.myApp27.vocabecho.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_decks")
data class UserDeckEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAtEpochDay: Long,
    val imageUri: String? = null
)
