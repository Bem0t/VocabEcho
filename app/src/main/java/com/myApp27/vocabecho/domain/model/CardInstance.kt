package com.myApp27.vocabecho.domain.model

/**
 * A single study instance generated from a user card (note).
 * 
 * One UserCardEntity (note) can generate multiple CardInstances:
 * - BASIC: 1 instance (front -> back)
 * - BASIC_REVERSED: 2 instances (front -> back, back -> front)
 * - BASIC_TYPED: 1 instance (front -> back, with typing)
 * - CLOZE: 1 instance (cloze text with blank)
 * 
 * For built-in decks, we continue using the simple Card model.
 */
data class CardInstance(
    /**
     * Stable instance ID for tracking progress per instance.
     * Format: "${noteId}#F" (forward), "${noteId}#R" (reverse), "${noteId}#C1" (cloze 1)
     * 
     * Note: In this increment, progress is still tracked per noteId for simplicity.
     * TODO: Implement instance-level progress tracking in future increment.
     */
    val instanceId: String,

    /**
     * The source note ID (user_cards.id).
     * Used for progress tracking in current implementation.
     */
    val noteId: String,

    /**
     * The deck this instance belongs to.
     */
    val deckId: String,

    /**
     * The type of card this instance represents.
     */
    val type: CardType,

    /**
     * The question/prompt shown to the user.
     * For CLOZE: text with "[...]" or "[hint]" placeholder.
     */
    val questionText: String,

    /**
     * The answer to be revealed/checked.
     * For CLOZE: the hidden word/phrase.
     */
    val answerText: String,

    /**
     * Whether this card expects user to type the answer.
     * True for BASIC_TYPED and CLOZE (future typed cloze support).
     */
    val expectsTyping: Boolean,

    /**
     * Direction of the card for BASIC_REVERSED.
     * "forward" = front -> back
     * "reverse" = back -> front
     * null = not applicable (BASIC, BASIC_TYPED, CLOZE)
     */
    val direction: String? = null
) {
    /**
     * For compatibility with existing Card-based code.
     * Returns front (question) side.
     */
    val front: String get() = questionText

    /**
     * For compatibility with existing Card-based code.
     * Returns back (answer) side.
     */
    val back: String get() = answerText

    /**
     * For compatibility: use noteId as the card ID for progress tracking.
     * TODO: Switch to instanceId when implementing per-instance progress.
     */
    val id: String get() = noteId

    companion object {
        const val DIRECTION_FORWARD = "forward"
        const val DIRECTION_REVERSE = "reverse"
    }
}
