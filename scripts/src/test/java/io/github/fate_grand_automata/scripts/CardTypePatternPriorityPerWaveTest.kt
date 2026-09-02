package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.github.fate_grand_automata.scripts.models.CardTypePatternPriorityPerWave
import kotlin.test.Test

class CardTypePatternPriorityPerWaveTest {
    @Test
    fun returnsThePatternsConfiguredForEachWave() {
        val perWave = CardTypePatternPriorityPerWave.of("BAB\nABB")

        assertThat(perWave.atWave(0).map { it.toString() }).containsExactly("BAB")
        assertThat(perWave.atWave(1).map { it.toString() }).containsExactly("ABB")
    }

    @Test
    fun clampsAWaveNumberPastTheLastConfiguredWaveToTheLastWave() {
        val perWave = CardTypePatternPriorityPerWave.of("BAB\nABB")

        assertThat(perWave.atWave(99).map { it.toString() }).containsExactly("ABB")
    }

    @Test
    fun blankConfigurationLeavesEveryWaveWithNoPatterns() {
        val perWave = CardTypePatternPriorityPerWave.of("")

        assertThat(perWave.atWave(0).toList()).isEmpty()
        assertThat(perWave.atWave(99).toList()).isEmpty()
    }
}
