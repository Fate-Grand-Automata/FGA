package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.modules.attack.CardSequenceHandler
import kotlin.test.Test

class CardSequenceHandlerTest {
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
    fun `duplicate types are selected in the configured order`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.B, CardTypeEnum.Buster),
            card(CommandCard.Face.B, FieldSlot.A, CardTypeEnum.Arts),
            card(CommandCard.Face.C, FieldSlot.A, CardTypeEnum.Buster),
            card(CommandCard.Face.D, FieldSlot.C, CardTypeEnum.Quick),
            card(CommandCard.Face.E, FieldSlot.A, CardTypeEnum.Buster),
        )

        val picked = CardSequenceHandler.pick(
            cards = cards,
            desiredOrder = listOf(CardTypeEnum.Buster, CardTypeEnum.Arts, CardTypeEnum.Buster),
        )!!.take(3).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.C,
        )
    }

    @Test
    fun `NP position overrides the configured color`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.B, CardTypeEnum.Arts),
            card(CommandCard.Face.B, FieldSlot.C, CardTypeEnum.Buster),
            card(CommandCard.Face.C, FieldSlot.A, CardTypeEnum.Quick),
        )

        val picked = CardSequenceHandler.pick(
            cards = cards,
            desiredOrder = listOf(CardTypeEnum.Arts, CardTypeEnum.Arts, CardTypeEnum.Buster),
            npUsage = NPUsage(setOf(CommandCard.NP.A), 1),
            npTypes = mapOf(FieldSlot.A to CardTypeEnum.Quick),
        )!!.take(2).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B)
    }

    @Test
    fun `with NP prefers matching servant for every required color`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.B, CardTypeEnum.Arts),
            card(CommandCard.Face.B, FieldSlot.A, CardTypeEnum.Arts),
            card(CommandCard.Face.C, FieldSlot.B, CardTypeEnum.Buster),
            card(CommandCard.Face.D, FieldSlot.A, CardTypeEnum.Buster),
        )

        val picked = CardSequenceHandler.pick(
            cards = cards,
            desiredOrder = listOf(CardTypeEnum.Arts, CardTypeEnum.Quick, CardTypeEnum.Buster),
            npUsage = NPUsage(setOf(CommandCard.NP.A), 1),
            npTypes = mapOf(FieldSlot.A to CardTypeEnum.Quick),
            braveChainEnum = BraveChainEnum.WithNP,
        )!!.take(2).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun `two NPs use first NP owner for the remaining face card`() {
        val cards = listOf(
            card(CommandCard.Face.A, FieldSlot.C, CardTypeEnum.Arts),
            card(CommandCard.Face.B, FieldSlot.A, CardTypeEnum.Arts),
        )

        val picked = CardSequenceHandler.pick(
            cards = cards,
            desiredOrder = listOf(CardTypeEnum.Arts, CardTypeEnum.Buster, CardTypeEnum.Quick),
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 1),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Buster,
                FieldSlot.B to CardTypeEnum.Quick,
            ),
            braveChainEnum = BraveChainEnum.WithNP,
        )!!.first().card

        assertThat(picked).isEqualTo(CommandCard.Face.B)
    }

    @Test
    fun `unknown NP type falls back to normal selection`() {
        val picked = CardSequenceHandler.pick(
            cards = listOf(
                card(CommandCard.Face.A, FieldSlot.A, CardTypeEnum.Arts),
                card(CommandCard.Face.B, FieldSlot.B, CardTypeEnum.Buster),
            ),
            desiredOrder = listOf(CardTypeEnum.Quick, CardTypeEnum.Arts, CardTypeEnum.Buster),
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(FieldSlot.A to CardTypeEnum.Unknown),
        )

        assertThat(picked).isNull()
    }
}
