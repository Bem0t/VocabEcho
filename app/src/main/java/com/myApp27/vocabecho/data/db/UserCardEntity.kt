package com.myApp27.vocabecho.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User-created card (note) entity.
 * 
 * Supports multiple card types:
 * - BASIC: front/back fields
 * - BASIC_REVERSED: front/back fields (generates 2 cards)
 * - BASIC_TYPED: front/back fields (typed answer)
 * - CLOZE: clozeText/clozeAnswer/clozeHint fields
 */
@Entity(
    tableName = "user_cards",
    indices = [Index(value = ["deckId"])]
)
data class UserCardEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    val createdAtEpochDay: Long,

    /**
     * Card type: BASIC, BASIC_REVERSED, BASIC_TYPED, CLOZE.
     * Stored as string. Null defaults to BASIC.
     */
    @ColumnInfo(defaultValue = "BASIC")
    val type: String? = "BASIC",

    /**
     * For CLOZE: the full text with cloze marker.
     * Example: "The capital of France is {{c1::Paris}}."
     */
    val clozeText: String? = null,

    /**
     * For CLOZE: the hidden answer.
     * Example: "Paris"
     */
    val clozeAnswer: String? = null,

    /**
     * For CLOZE: optional hint shown in brackets.
     * Example: "city" -> "[city]"
     */
    val clozeHint: String? = null
)
