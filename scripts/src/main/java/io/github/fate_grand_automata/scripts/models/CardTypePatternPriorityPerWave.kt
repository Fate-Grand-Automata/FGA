package io.github.fate_grand_automata.scripts.models

/**
 * Per-wave wrapper around [CardTypePatternPriority], mirroring [CardPriorityPerWave]'s shape.
 */
class CardTypePatternPriorityPerWave private constructor(
    private val patternsPerWave: List<CardTypePatternPriority>
) : List<CardTypePatternPriority> by patternsPerWave {
    fun atWave(wave: Int) =
        patternsPerWave[wave.coerceIn(patternsPerWave.indices)]

    override fun toString() =
        patternsPerWave.joinToString(waveSeparator)

    companion object {
        private const val waveSeparator = "\n"

        fun from(patternsPerWave: List<CardTypePatternPriority>) = CardTypePatternPriorityPerWave(patternsPerWave)

        // Deliberately no isBlank() special case, unlike CardPriorityPerWave.of():
        // "".split(waveSeparator) already yields a single blank element, and
        // CardTypePatternPriority.of() already treats a blank element as "no patterns"
        // rather than a parse error (see above). So a fully blank config naturally
        // becomes one wave entry with an empty pattern list, and atWave() never
        // indexes an empty list.
        fun of(priority: String): CardTypePatternPriorityPerWave =
            CardTypePatternPriorityPerWave(
                priority.split(waveSeparator).map { CardTypePatternPriority.of(it) }
            )
    }
}
