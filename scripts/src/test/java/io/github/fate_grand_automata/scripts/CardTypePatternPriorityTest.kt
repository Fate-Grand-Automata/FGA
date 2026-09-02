package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.models.CardTypePatternPriority
import kotlin.test.Test
import kotlin.test.assertFailsWith

class CardTypePatternPriorityTest {
    @Test
    fun parsesMultiplePatternsInPriorityOrder() {
        val priority = CardTypePatternPriority.of("BAB, ABB")

        assertThat(priority.map { it.toString() }).containsExactly("BAB", "ABB")
    }

    @Test
    fun throwsWhenAnyPatternInTheListIsInvalid() {
        assertFailsWith<AutoBattle.BattleExitException> { CardTypePatternPriority.of("BAB, XYZ") }
    }

    @Test
    fun parsesBlankStringAsNoPatterns() {
        val priority = CardTypePatternPriority.of("")

        assertThat(priority.toList()).isEmpty()
    }

    @Test
    fun parsesWhitespaceOnlyStringAsNoPatterns() {
        val priority = CardTypePatternPriority.of("   ")

        assertThat(priority.toList()).isEmpty()
    }
}
