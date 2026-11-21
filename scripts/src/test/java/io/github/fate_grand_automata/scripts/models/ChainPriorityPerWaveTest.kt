package io.github.fate_grand_automata.scripts.models

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum.Arts
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum.Avoid
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum.Buster
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum.Mighty
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum.Quick
import io.github.fate_grand_automata.scripts.models.battle.ChainPriorityItem
import io.github.fate_grand_automata.scripts.models.battle.ChainPriorityPerWave
import kotlin.test.Test

class ChainPriorityPerWaveTest {
    @Test
    fun `disabled chains keep their place`() {
        val priority = ChainPriorityPerWave.of("Mighty,!Buster,Arts,Quick,!Avoid")

        assertThat(priority.atWave(0)).containsExactly(
            ChainPriorityItem(Mighty),
            ChainPriorityItem(Buster, isEnabled = false),
            ChainPriorityItem(Arts),
            ChainPriorityItem(Quick),
            ChainPriorityItem(Avoid, isEnabled = false)
        )
        assertThat(priority.enabledAtWave(0)).containsExactly(Mighty, Arts, Quick)
    }

    @Test
    fun `round trip`() {
        val saved = "Avoid,!Mighty,!Buster,!Arts,!Quick\nBuster,Mighty,!Arts,!Quick,!Avoid"

        assertThat(ChainPriorityPerWave.of(saved).toString()).isEqualTo(saved)
    }

    @Test
    fun `missing chains are added disabled and unknown names skipped`() {
        val priority = ChainPriorityPerWave.of("Buster,Spam,Buster,!Arts")

        assertThat(priority.toString()).isEqualTo("Buster,!Arts,!Mighty,!Quick,!Avoid")
    }

    @Test
    fun `default tries every chain except Avoid`() {
        assertThat(ChainPriorityPerWave.default.enabledAtWave(0)).containsExactly(Mighty, Buster, Arts, Quick)
        assertThat(ChainPriorityPerWave.of("").toString()).isEqualTo("Mighty,Buster,Arts,Quick,!Avoid")
    }

    @Test
    fun `later waves use the last wave`() {
        val priority = ChainPriorityPerWave.of("Mighty,Buster,Arts,Quick,!Avoid\nAvoid,!Mighty,!Buster,!Arts,!Quick")

        assertThat(priority.enabledAtWave(2)).containsExactly(Avoid)
    }
}
