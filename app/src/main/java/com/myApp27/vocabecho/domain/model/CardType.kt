package com.myApp27.vocabecho.domain.model

/**
 * Anki-like card types for user-created cards.
 * Built-in decks continue to use simple front/back model.
 */
enum class CardType {
    /**
     * Basic card: front -> back (1 card generated)
     */
    BASIC,

    /**
     * Basic + reversed: generates 2 cards
     * - Forward: front -> back
     * - Reverse: back -> front
     */
    BASIC_REVERSED,

    /**
     * Basic with typed answer: front -> back, user must type answer
     */
    BASIC_TYPED,

    /**
     * Cloze deletion: text with [...] placeholder, user fills in the blank
     */
    CLOZE;

    companion object {
        /**
         * Parse type from string, defaulting to BASIC for null/unknown values.
         */
        fun fromString(value: String?): CardType {
            if (value == null) return BASIC
            return try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                BASIC
            }
        }
    }
}
