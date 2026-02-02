package com.myApp27.vocabecho.domain.model

import com.myApp27.vocabecho.data.db.UserCardEntity

/**
 * Generates CardInstance(s) from a UserCardEntity (note).
 * 
 * One note can generate multiple instances depending on its type:
 * - BASIC: 1 instance
 * - BASIC_REVERSED: 2 instances (forward + reverse)
 * - BASIC_TYPED: 1 instance
 * - CLOZE: 1 instance (for now; can be extended for multiple clozes)
 */
object CardInstanceGenerator {

    /**
     * Generate all CardInstance(s) from a single user card entity.
     */
    fun generate(entity: UserCardEntity): List<CardInstance> {
        val type = CardType.fromString(entity.type)
        val noteId = entity.id
        val deckId = entity.deckId

        return when (type) {
            CardType.BASIC -> listOf(
                CardInstance(
                    instanceId = "${noteId}#F",
                    noteId = noteId,
                    deckId = deckId,
                    type = CardType.BASIC,
                    questionText = entity.front,
                    answerText = entity.back,
                    expectsTyping = false,
                    direction = null
                )
            )

            CardType.BASIC_REVERSED -> listOf(
                // Forward: front -> back
                CardInstance(
                    instanceId = "${noteId}#F",
                    noteId = noteId,
                    deckId = deckId,
                    type = CardType.BASIC_REVERSED,
                    questionText = entity.front,
                    answerText = entity.back,
                    expectsTyping = false,
                    direction = CardInstance.DIRECTION_FORWARD
                ),
                // Reverse: back -> front
                CardInstance(
                    instanceId = "${noteId}#R",
                    noteId = noteId,
                    deckId = deckId,
                    type = CardType.BASIC_REVERSED,
                    questionText = entity.back,
                    answerText = entity.front,
                    expectsTyping = false,
                    direction = CardInstance.DIRECTION_REVERSE
                )
            )

            CardType.BASIC_TYPED -> listOf(
                CardInstance(
                    instanceId = "${noteId}#F",
                    noteId = noteId,
                    deckId = deckId,
                    type = CardType.BASIC_TYPED,
                    questionText = entity.front,
                    answerText = entity.back,
                    expectsTyping = true,
                    direction = null
                )
            )

            CardType.CLOZE -> {
                // Generate cloze instance
                val clozeText = entity.clozeText
                val clozeAnswer = entity.clozeAnswer

                if (clozeText.isNullOrBlank() || clozeAnswer.isNullOrBlank()) {
                    // Fallback to BASIC if cloze data is missing
                    return listOf(
                        CardInstance(
                            instanceId = "${noteId}#F",
                            noteId = noteId,
                            deckId = deckId,
                            type = CardType.BASIC,
                            questionText = entity.front,
                            answerText = entity.back,
                            expectsTyping = false,
                            direction = null
                        )
                    )
                }

                // Generate question with placeholder (case-insensitive, first occurrence only)
                val placeholder = if (!entity.clozeHint.isNullOrBlank()) {
                    "[${entity.clozeHint}]"
                } else {
                    "[...]"
                }
                val questionText = replaceFirstIgnoreCase(clozeText, clozeAnswer, placeholder)

                listOf(
                    CardInstance(
                        instanceId = "${noteId}#C1",
                        noteId = noteId,
                        deckId = deckId,
                        type = CardType.CLOZE,
                        questionText = questionText,
                        answerText = clozeAnswer,
                        expectsTyping = true, // Cloze typically expects typing
                        direction = null
                    )
                )
            }
        }
    }

    /**
     * Generate CardInstances from a list of user card entities.
     */
    fun generateAll(entities: List<UserCardEntity>): List<CardInstance> {
        return entities.flatMap { generate(it) }
    }

    /**
     * Convert a simple Card (from built-in deck) to a CardInstance.
     * Used for compatibility with existing code.
     */
    fun fromSimpleCard(card: Card, deckId: String): CardInstance {
        return CardInstance(
            instanceId = "${card.id}#F",
            noteId = card.id,
            deckId = deckId,
            type = CardType.BASIC,
            questionText = card.front,
            answerText = card.back,
            expectsTyping = false,
            direction = null
        )
    }

    /**
     * Replace first occurrence of target in text, ignoring case.
     * Returns original text if target not found.
     */
    private fun replaceFirstIgnoreCase(text: String, target: String, replacement: String): String {
        val idx = text.indexOf(target, ignoreCase = true)
        if (idx < 0) return text
        return text.substring(0, idx) + replacement + text.substring(idx + target.length)
    }
}
