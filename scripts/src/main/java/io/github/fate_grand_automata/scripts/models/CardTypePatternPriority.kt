package io.github.fate_grand_automata.scripts.models

/**
 * A priority-ordered list of [CardTypePattern]s. The first pattern the current hand can
 * satisfy wins; see [io.github.fate_grand_automata.scripts.modules.CardTypePatternSelector].
 */
class CardTypePatternPriority private constructor(
    private val patterns: List<CardTypePattern>
) : List<CardTypePattern> by patterns {
    override fun toString() = patterns.joinToString(", ")

    companion object {
        fun from(patterns: List<CardTypePattern>) = CardTypePatternPriority(patterns)

        // A blank string means "no type patterns for this wave", which is a valid,
        // meaningful state (nothing to try) rather than an error. Unlike
        // CardPriority.of(), there is no sensible non-empty default to fall back to,
        // so this doesn't mirror CardPriorityPerWave.of()'s isBlank()-to-default branch.
        fun of(priority: String): CardTypePatternPriority =
            if (priority.isBlank()) {
                CardTypePatternPriority(emptyList())
            } else {
                CardTypePatternPriority(priority.split(",").map { CardTypePattern.of(it) })
            }
    }
}
