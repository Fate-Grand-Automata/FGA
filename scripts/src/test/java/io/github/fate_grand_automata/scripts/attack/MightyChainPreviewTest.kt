package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNull
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.modules.attack.MightyChainHandler
import kotlin.test.Test

class MightyChainPreviewTest {
    private fun card(
        commandCard: CommandCard.Face,
        slot: FieldSlot,
        type: CardTypeEnum,
    ) = ParsedCard(
        card = commandCard,
        servant = when (slot) {
            FieldSlot.A -> TeamSlot.A
            FieldSlot.B -> TeamSlot.B
            FieldSlot.C -> TeamSlot.C
        },
        fieldSlot = slot,
        type = type,
    )

    @Test
    fun `required slot selects mighty brave before mixed mighty`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.B, CardTypeEnum.Buster),
            card(CommandCard.Face.B, FieldSlot.A, CardTypeEnum.Buster),
            card(CommandCard.Face.C, FieldSlot.A, CardTypeEnum.Arts),
            card(CommandCard.Face.D, FieldSlot.A, CardTypeEnum.Quick),
            card(CommandCard.Face.E, FieldSlot.C, CardTypeEnum.Quick),
        )

        val picked = MightyChainHandler.pick(
            cards = cards,
            requiredFieldSlot = FieldSlot.A,
        )!!.take(3).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.D,
        )
    }

    @Test
    fun `BBAAQ deck selects one card of each type in configured order`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.A, CardTypeEnum.Buster),
            card(CommandCard.Face.B, FieldSlot.A, CardTypeEnum.Buster),
            card(CommandCard.Face.C, FieldSlot.A, CardTypeEnum.Arts),
            card(CommandCard.Face.D, FieldSlot.A, CardTypeEnum.Arts),
            card(CommandCard.Face.E, FieldSlot.A, CardTypeEnum.Quick),
        )

        val picked = MightyChainHandler.pick(cards)!!.take(3).map { it.type }

        assertThat(picked).containsExactly(
            CardTypeEnum.Buster,
            CardTypeEnum.Arts,
            CardTypeEnum.Quick,
        )
    }

    @Test
    fun `manual NP color completes mighty chain`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.B, CardTypeEnum.Buster),
            card(CommandCard.Face.B, FieldSlot.C, CardTypeEnum.Arts),
            card(CommandCard.Face.C, FieldSlot.A, CardTypeEnum.Arts),
            card(CommandCard.Face.D, FieldSlot.A, CardTypeEnum.Buster),
            card(CommandCard.Face.E, FieldSlot.C, CardTypeEnum.Buster),
        )

        val picked = MightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(FieldSlot.A to CardTypeEnum.Quick),
        )!!.take(2).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B)
    }

    @Test
    fun `unknown NP type falls back to normal selection`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.A, CardTypeEnum.Buster),
            card(CommandCard.Face.B, FieldSlot.B, CardTypeEnum.Arts),
            card(CommandCard.Face.C, FieldSlot.C, CardTypeEnum.Quick),
        )

        val picked = MightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(FieldSlot.A to CardTypeEnum.Unknown),
        )

        assertThat(picked).isNull()
    }

    @Test
    fun `two NPs of the same type fall back to normal selection`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.C, CardTypeEnum.Arts),
            card(CommandCard.Face.B, FieldSlot.C, CardTypeEnum.Quick),
            card(CommandCard.Face.C, FieldSlot.C, CardTypeEnum.Buster),
        )

        val picked = MightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Buster,
                FieldSlot.B to CardTypeEnum.Buster,
            ),
        )

        assertThat(picked).isNull()
    }

    @Test
    fun `brave always keeps a mighty chain on the NP servant`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.B, CardTypeEnum.Buster),
            card(CommandCard.Face.B, FieldSlot.A, CardTypeEnum.Buster),
            card(CommandCard.Face.C, FieldSlot.A, CardTypeEnum.Quick),
            card(CommandCard.Face.D, FieldSlot.B, CardTypeEnum.Arts),
            card(CommandCard.Face.E, FieldSlot.C, CardTypeEnum.Quick),
        )

        val picked = MightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(FieldSlot.A to CardTypeEnum.Arts),
            braveChainEnum = BraveChainEnum.Always,
            forceBraveChain = true,
        )!!.take(2).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.B, CommandCard.Face.C)
    }
}
