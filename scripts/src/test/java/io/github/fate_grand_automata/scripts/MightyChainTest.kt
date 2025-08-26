package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.containsExactly
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.modules.ApplyBraveChains
import kotlin.test.Test

class MightyChainTest {
    companion object {
        val scathach1WB = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val scathach2WQ = ParsedCard(
            card = CommandCard.Face.B,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Quick,
            affinity = CardAffinityEnum.Weak
        )
        val scathach3WA = ParsedCard(
            card = CommandCard.Face.C,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Arts,
            affinity = CardAffinityEnum.Weak
        )
        val scathach4WB = ParsedCard(
            card = CommandCard.Face.D,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val scathach5WQ = ParsedCard(
            card = CommandCard.Face.E,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Quick,
            affinity = CardAffinityEnum.Weak
        )

        val lineup1 = listOf(scathach1WB, scathach2WQ, scathach3WA, scathach4WB, scathach5WQ)

        val lineup2 = listOf(scathach1WB, scathach4WB, scathach5WQ, scathach2WQ, scathach3WA)

        val lineup3 = listOf(scathach5WQ, scathach2WQ, scathach3WA, scathach1WB, scathach4WB)
    }

    @Test
    fun mightyChain_lineup1() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_lineup1_rearrange() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            rearrange = true
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_lineup2() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun mightyChain_lineup2_rearrange() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            rearrange = true
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun mightyChain_lineup1_withNp_fieldSlotA() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        ).map { it.card }

        // Unable to Brave Chain. Will ignore and return result of withNp
        assertThat(picked).containsExactly(CommandCard.Face.B, CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_lineup1_withNp_fieldSlotA_rearrange() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            rearrange = true
        ).map { it.card }

        // Unable to Brave Chain. Will ignore and return result of withNp
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_lineup1_withNp_fieldSlotB() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        ).map { it.card }

        // Attempt to Brave chain with Scathach cards
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun mightyChain_lineup1_withNp_fieldSlotB_rearrange() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            rearrange = true
        ).map { it.card }

        // Attempt to Brave chain with Scathach cards
        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun mightyChain_lineup1_withNp_fieldSlotC() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0)
        ).map { it.card }

        // Unable to Mighty Chain. Will ignore and return result of withNp
        assertThat(picked).containsExactly(CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_lineup1_withNp_fieldSlotC_rearrange() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            rearrange = true
        ).map { it.card }

        // Unable to Mighty Chain. Will ignore and return result of withNp
        assertThat(picked).containsExactly(CommandCard.Face.D, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_fullCards_lineup1() {
        val braveChains = ApplyBraveChains()

        val cards = lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_fullCards_lineup1_rearrange() {
        val braveChains = ApplyBraveChains()

        val cards = lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            rearrange = true
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_fullCards_lineup2() {
        val braveChains = ApplyBraveChains()

        val cards = lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.B)
    }

    @Test
    fun mightyChain_fullCards_lineup2_rearrange() {
        val braveChains = ApplyBraveChains()

        val cards = lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            rearrange = true
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.E, CommandCard.Face.D, CommandCard.Face.B)
    }

    @Test
    fun mightyChain_fullCards_lineup3() {
        val braveChains = ApplyBraveChains()

        val cards = lineup3
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun mightyChain_fullCards_lineup3_rearrange() {
        val braveChains = ApplyBraveChains()

        val cards = lineup3
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            rearrange = true
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
    }
}