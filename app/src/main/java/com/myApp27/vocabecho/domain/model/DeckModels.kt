package com.myApp27.vocabecho.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Deck(
    val id: String,
    val title: String,
    val cards: List<Card>,
    val imageUri: String? = null
)

@Serializable
data class Card(
    val id: String,
    val front: String,
    val back: String
)