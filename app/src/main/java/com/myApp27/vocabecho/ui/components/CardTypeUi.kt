package com.myApp27.vocabecho.ui.components

import com.myApp27.vocabecho.domain.model.CardType

/** Full display name for card type (used in descriptions). */
fun CardType.displayNameRu(): String = when (this) {
    CardType.BASIC -> "Устный ответ"
    CardType.BASIC_TYPED -> "Ответ с вводом"
    CardType.CLOZE -> "Ответ с пропуском"
    CardType.BASIC_REVERSED -> "Устный ответ"
}

/** Short label for segmented buttons (fits in narrow space). */
fun CardType.shortLabelRu(): String = when (this) {
    CardType.BASIC -> "Устный"
    CardType.BASIC_TYPED -> "Ввод"
    CardType.CLOZE -> "Пропуск"
    CardType.BASIC_REVERSED -> "Устный"
}

fun CardType.descriptionRu(): String = when (this) {
    CardType.BASIC ->
        "Режим устного ответа. Ребёнок видит слово или вопрос, думает про себя и затем видит правильный ответ. Ввод текста не требуется. Подходит для знакомства с новыми словами."

    CardType.BASIC_TYPED ->
        "Режим ответа с вводом. Ребёнок должен сам написать ответ без подсказки. Приложение сравнивает введённый ответ с правильным. Подходит для закрепления знаний и тренировки памяти."

    CardType.CLOZE ->
        "Режим ответа с пропуском. В предложении скрыто слово или фраза. Ребёнок должен вписать пропущенную часть. Помогает изучать слова в контексте предложения."

    CardType.BASIC_REVERSED ->
        "Режим устного ответа. Ребёнок видит слово или вопрос, думает про себя и затем видит правильный ответ. Ввод текста не требуется. Подходит для знакомства с новыми словами."
}
