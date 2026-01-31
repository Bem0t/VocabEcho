package com.myApp27.vocabecho.domain.time

import java.time.LocalDate

object TimeProvider {
    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()
}