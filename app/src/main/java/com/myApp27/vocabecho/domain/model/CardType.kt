package com.myApp27.vocabecho.domain.model

/**
 * Anki-like card types for user-created cards.
 * Built-in decks continue to use simple front/back model.
 */
enum class CardType {
    /**
     * Basic card: front -> back (1 card generated)
     * Recognition-only mode: user thinks, sees answer, then self-evaluates via Yes/No.
     * NO text input required.
     */
    BASIC,

    /**
     * Basic + reversed: generates 2 cards
     * - Forward: front -> back
     * - Reverse: back -> front
     */
    BASIC_REVERSED,

    /**
     * Basic with typed answer: front -> back, user must type answer.
     * Recall mode: user types answer, app compares with correct answer.
     * Text input IS required.
     */
    BASIC_TYPED,

    /**
     * Cloze deletion: text with [...] placeholder, user fills in the blank.
     * Text input IS required.
     */
    CLOZE;

    /**
     * Returns true if this card type requires user to type the answer.
     * - BASIC: false (recognition only, Yes/No buttons)
     * - BASIC_REVERSED: false (treated as BASIC)
     * - BASIC_TYPED: true (user types answer)
     * - CLOZE: true (user types the hidden word)
     */
    fun expectsTyping(): Boolean = when (this) {
        BASIC, BASIC_REVERSED -> false
        BASIC_TYPED, CLOZE -> true
    }

    companion object {
        /**
         * Parse type from string, defaulting to BASIC for null/unknown values.
         * Note: BASIC_REVERSED is mapped to BASIC for backward compatibility.
         */
        fun fromString(value: String?): CardType {
            if (value == null) return BASIC
            // Map BASIC_REVERSED to BASIC for backward compatibility
            if (value == "BASIC_REVERSED") return BASIC
            return try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                BASIC
            }
        }

        /**
         * Returns only the types that should be shown in UI selectors.
         * BASIC_REVERSED is excluded as it's deprecated.
         */
        fun selectableTypes(): List<CardType> = listOf(BASIC, BASIC_TYPED, CLOZE)
    }
}
