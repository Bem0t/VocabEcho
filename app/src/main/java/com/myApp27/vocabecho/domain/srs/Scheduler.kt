package com.myApp27.vocabecho.domain.srs

import com.myApp27.vocabecho.domain.model.Grade
import com.myApp27.vocabecho.domain.model.ParentSettings

object Scheduler {
    fun nextDueEpochDay(todayEpochDay: Long, grade: Grade, s: ParentSettings): Long {
        val addDays = when (grade) {
            Grade.AGAIN -> s.againDays
            Grade.HARD -> s.hardDays
            Grade.EASY -> s.easyDays
        }
        return todayEpochDay + addDays.toLong()
    }
}