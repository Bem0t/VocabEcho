package com.myApp27.vocabecho.domain.queue


import com.myApp27.vocabecho.data.db.CardProgressEntity
import com.myApp27.vocabecho.domain.model.Card

object CardQueueBuilder {

    fun buildQueue(
        cards: List<Card>,
        progress: List<CardProgressEntity>,
        todayEpochDay: Long
    ): List<Card> {
        val progressById = progress.associateBy { it.cardId }

        val due = cards
            .mapNotNull { card ->
                val p = progressById[card.id] ?: return@mapNotNull null
                if (p.dueEpochDay <= todayEpochDay) card else null
            }

        val newCards = cards.filter { card -> progressById[card.id] == null }

        return due + newCards
    }
}