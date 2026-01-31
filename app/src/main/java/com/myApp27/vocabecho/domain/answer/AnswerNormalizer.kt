package com.myApp27.vocabecho.domain.answer

object AnswerNormalizer {

    fun normalize(s: String): String =
        s
            .trim()
            .lowercase()
            .replace('ё', 'е')
            .replace(Regex("\\s+"), " ") // любые пробелы -> один пробел

    fun isCorrect(userAnswer: String, correctAnswer: String): Boolean =
        normalize(userAnswer) == normalize(correctAnswer)
}