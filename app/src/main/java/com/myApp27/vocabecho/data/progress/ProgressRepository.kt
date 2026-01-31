package com.myApp27.vocabecho.data.progress

import com.myApp27.vocabecho.data.db.CardProgressDao
import com.myApp27.vocabecho.data.db.CardProgressEntity
import com.myApp27.vocabecho.data.db.CardStatsDao
import com.myApp27.vocabecho.data.db.CardStatsEntity
import com.myApp27.vocabecho.domain.model.Grade
import com.myApp27.vocabecho.domain.model.ParentSettings
import com.myApp27.vocabecho.domain.srs.Scheduler

class ProgressRepository(
    private val dao: CardProgressDao,
    private val statsDao: CardStatsDao
) {
    suspend fun gradeCard(
        deckId: String,
        cardId: String,
        todayEpochDay: Long,
        grade: Grade,
        settings: ParentSettings
    ) {
        val nextDue = Scheduler.nextDueEpochDay(todayEpochDay, grade, settings)

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

    suspend fun countReviewedToday(deckId: String, todayEpochDay: Long): Int =
        dao.countReviewedToday(deckId, todayEpochDay)

    /**
     * Apply answer result: update stats, compute grade automatically, and schedule next due.
     */
    suspend fun applyAnswerResult(
        deckId: String,
        cardId: String,
        todayEpochDay: Long,
        isCorrect: Boolean,
        settings: ParentSettings
    ) {
        // 1. Load or create stats
        val oldStats = statsDao.getByCardId(cardId)
        val stats = oldStats ?: CardStatsEntity(
            cardId = cardId,
            deckId = deckId
        )

        // 2. Update stats based on correctness
        val updatedStats = if (isCorrect) {
            stats.copy(
                correctCount = stats.correctCount + 1,
                correctStreak = stats.correctStreak + 1,
                lastAnsweredEpochDay = todayEpochDay
            )
        } else {
            stats.copy(
                wrongCount = stats.wrongCount + 1,
                correctStreak = 0,
                lastAnsweredEpochDay = todayEpochDay
            )
        }
        statsDao.upsert(updatedStats)

        // 3. Determine grade automatically
        val grade = if (!isCorrect) {
            Grade.AGAIN
        } else {
            // isCorrect == true
            if (updatedStats.correctStreak >= 3) {
                Grade.EASY
            } else {
                Grade.HARD
            }
        }

        // 4. Schedule next due using existing gradeCard logic
        gradeCard(deckId, cardId, todayEpochDay, grade, settings)
    }
}