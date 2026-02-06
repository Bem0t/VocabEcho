package com.myApp27.vocabecho.ui.components

import com.myApp27.vocabecho.domain.model.CardType

/** Full display name for card type (used in descriptions). */
fun CardType.displayNameRu(): String = when (this) {
    CardType.BASIC -> "Показать ответ"
    CardType.BASIC_TYPED -> "Ввести ответ"
    CardType.CLOZE -> "Пропуск в предложении"
    CardType.BASIC_REVERSED -> "Показать ответ"
}

/** Short label for segmented buttons (fits in narrow space). */
fun CardType.shortLabelRu(): String = when (this) {
    CardType.BASIC -> "Показать"
    CardType.BASIC_TYPED -> "Ввести"
    CardType.CLOZE -> "Пропуск"
    CardType.BASIC_REVERSED -> "Показать"
}

fun CardType.descriptionRu(): String = when (this) {
    CardType.BASIC ->
        "Режим узнавания. Ребёнок видит слово или вопрос, думает про себя и затем видит правильный ответ. Ввод текста не требуется. Подходит для знакомства с новыми словами."

    CardType.BASIC_TYPED ->
        "Режим проверки. Ребёнок должен сам написать ответ без подсказки. Приложение сравнивает введённый ответ с правильным. Подходит для закрепления знаний и тренировки памяти."

    CardType.CLOZE ->
        "Режим контекста. В предложении скрыто слово или фраза. Ребёнок должен вписать пропущенную часть. Помогает изучать слова в контексте предложения."

    CardType.BASIC_REVERSED ->
        "Режим узнавания. Ребёнок видит слово или вопрос, думает про себя и затем видит правильный ответ. Ввод текста не требуется. Подходит для знакомства с новыми словами."
}
