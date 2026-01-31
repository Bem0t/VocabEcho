package com.myApp27.vocabecho.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "card_stats")
data class CardStatsEntity(
    @PrimaryKey val cardId: String,
    val deckId: String,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val correctStreak: Int = 0,
    val lastAnsweredEpochDay: Long? = null
)
