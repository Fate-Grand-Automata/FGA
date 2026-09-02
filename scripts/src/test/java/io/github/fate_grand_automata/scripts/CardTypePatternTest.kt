package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.containsExactly
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CardTypePattern
import kotlin.test.Test
import kotlin.test.assertFailsWith

class CardTypePatternTest {
    @Test
    fun parsesThreeLetterPattern() {
        val pattern = CardTypePattern.of("BAB")

        assertThat(pattern.toList()).containsExactly(CardTypeEnum.Buster, CardTypeEnum.Arts, CardTypeEnum.Buster)
    }

    @Test
    fun parsesLowercasePatternAsUppercase() {
        val pattern = CardTypePattern.of("bab")

        assertThat(pattern.toList()).containsExactly(CardTypeEnum.Buster, CardTypeEnum.Arts, CardTypeEnum.Buster)
    }

    @Test
    fun throwsWhenPatternIsShorterThanThreeCharacters() {
        assertFailsWith<AutoBattle.BattleExitException> { CardTypePattern.of("BA") }
    }

    @Test
    fun throwsWhenPatternIsLongerThanThreeCharacters() {
        assertFailsWith<AutoBattle.BattleExitException> { CardTypePattern.of("BABQ") }
    }

    @Test
    fun throwsWhenPatternContainsAnInvalidCharacter() {
        assertFailsWith<AutoBattle.BattleExitException> { CardTypePattern.of("BAX") }
    }
}
