package com.myApp27.vocabecho.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "card_progress",
    indices = [Index(value = ["deckId", "dueEpochDay"])]
)
data class CardProgressEntity(
    @PrimaryKey val cardId: String,
    val deckId: String,
    val dueEpochDay: Long,
    val lastReviewedEpochDay: Long? = null,
    val isNew: Boolean = true
)