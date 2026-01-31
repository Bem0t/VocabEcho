package com.myApp27.vocabecho.data.progress

import com.myApp27.vocabecho.data.db.CardProgressDao
import com.myApp27.vocabecho.data.db.CardProgressEntity
import com.myApp27.vocabecho.domain.model.Grade
import com.myApp27.vocabecho.domain.model.ParentSettings
import com.myApp27.vocabecho.domain.srs.Scheduler
import com.myApp27.vocabecho.domain.time.TimeProvider

class ProgressRepository(
    private val dao: CardProgressDao
) {
    suspend fun gradeCard(
        deckId: String,
        cardId: String,
        todayEpochDay: Long,
        grade: Grade,
        settings: ParentSettings
    ) {
        val nextDue = Scheduler.nextDueEpochDay(todayEpochDay, grade, settings)
        val old = dao.getByCardId(cardId)

        dao.upsert(
            CardProgressEntity(
                cardId = cardId,
                deckId = deckId,
                dueEpochDay = nextDue,
                lastReviewedEpochDay = todayEpochDay,
                isNew = false
            )
        )
    }

    suspend fun getDueForDeck(deckId: String, todayEpochDay: Long): List<CardProgressEntity> =
        dao.getDueForDeck(deckId, todayEpochDay)

    suspend fun getAllForDeck(deckId: String): List<CardProgressEntity> =
        dao.getAllForDeck(deckId)

    suspend fun countReviewedToday(deckId: String): Int =
        dao.countReviewedToday(deckId, TimeProvider.todayEpochDay())
}