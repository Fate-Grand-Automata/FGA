package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.modules.ApplyBraveChains
import kotlin.collections.mapOf
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
        val scathach3WAltQ = ParsedCard(
            card = CommandCard.Face.C,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Quick,
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

        // 3 Quick, 2 Buster; Buster starting card
        val lineup4 = listOf(scathach1WB, scathach2WQ, scathach3WAltQ, scathach4WB, scathach5WQ)
        // 3 Quick, 2 Buster; Quick starting card
        val lineup5 = listOf(scathach5WQ, scathach2WQ, scathach3WAltQ, scathach1WB, scathach4WB)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        // Expect SB,KQ,NA,NA,SQ - 12345 - ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ), with rearrange`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Mixed cards - lineup2 (1SB,5SQ,2SQ,3NA,4NA)`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        // Expect 1SB,2KQ,3NA,5SQ,4NA - 15324 - AECBD
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun `Mixed cards - lineup2 (1SB,5SQ,2SQ,3NA,4NA), with rearrange`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        ).map { it.card }
        val pickedDefault = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        ).map { it.card }

        // Unable to Brave Chain with Kama. Will ignore and return result of withNp
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with rearrange`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            rearrange = true
        ).map { it.card }
        val pickedDefault = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with npTypes`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            )
        ).map { it.card }

        // Expect 1SB,3NA,2KQ,4NA,5SQ - 13245 - ACBDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with rearrange & npTypes`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            ),
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 1 and 2) // because NP
        assertThat(picked).containsExactly(CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        ).map { it.card }
        val pickedDefault = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        ).map { it.card }

        // Unable to Brave Chain with Scathach. Will ignore and return result of withNp
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with rearrange`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Quick
            )
        ).map { it.card }

        // Expect 1SB,3NA,2KQ,4NA,5SQ - 13245 - ACBDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0)
        ).map { it.card }

        // Attempt to Brave chain with Nero NP
        // Expect 3NA,4NA,1SB,2KQ,5SQ - 34125 - CDABE
        assertThat(picked).containsExactly(CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, rearranged=true`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.D, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with npTypes`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.C to CardTypeEnum.Arts
            )
        ).map { it.card }

        // Expect 1SB,2KQ,3NA,4NA,5SQ - 12345 - ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with rearrange & npTypes`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.C to CardTypeEnum.Arts
            ),
            rearrange = true
        ).map { it.card }

        // Expect 2KQ,1SB,3NA,4NA,5SQ - 21345 - BACDE
        assertThat(picked).containsExactly(CommandCard.Face.B, CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Full cards - lineup1 (BQABQ)`() {
        val braveChains = ApplyBraveChains()

        val cards = lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        // Expect same result as input
        // Expect BQABQ / 12345 / ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Full cards - lineup1 (BQABQ), rearranged=true`() {
        val braveChains = ApplyBraveChains()

        val cards = lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Full cards - lineup2 (BBQQA)`() {
        val braveChains = ApplyBraveChains()

        val cards = lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        // Expect 1SB,5SQ,3NA,4NA,2KQ / 15342 / AECDB
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.B)
    }

    @Test
    fun `Full cards - lineup2 (BBQQA), rearranged=true`() {
        val braveChains = ApplyBraveChains()

        val cards = lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.E, CommandCard.Face.D, CommandCard.Face.B)
    }

    @Test
    fun `Full cards - lineup3 (QQABB)`() {
        val braveChains = ApplyBraveChains()

        val cards = lineup3
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        // Expect QABQB / 53124 / ECABD
        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun `Full cards - lineup3 (QQABB), rearranged=true`() {
        val braveChains = ApplyBraveChains()

        val cards = lineup3
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `Full cards - lineup4 (BQQBQ) - No mighty chain`() {
        val braveChains = ApplyBraveChains()

        val cards = lineup4
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        // Expect same result as input
        // Expect BQQBQ / 12345 / ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `Full cards - lineup5 (QQQBB) - No mighty chain`() {
        val braveChains = ApplyBraveChains()

        val cards = lineup5
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        // Expect same result as input
        // Expect QQQBB / 52314 / EBCAD
        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.D)
    }

    /**
     * Two NP scenario
     */
    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (2Kama, 3Nero)`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.B, CommandCard.NP.C), 0)
        ).map { it.card }
        val pickedDefault = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B, CommandCard.NP.C), 0)
        ).map { it.card }

        // Should fall back to default behaviour
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 3Nero), with npTypes`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.B, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.C to CardTypeEnum.Arts
            )
        ).map { it.card }

        // Expect SB,KQ,NA,NA,SQ - 12345 - ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach)`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
        ).map { it.card }
        val pickedDefault = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0)
        ).map { it.card }

        // Should fall back to default behaviour
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach), with npTypes`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick
            )
        ).map { it.card }
        val pickedDefault = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0)
        ).map { it.card }

        // Should fall back to default behaviour
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Mixed cards - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3 NP, with npTypes`() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick,
                FieldSlot.C to CardTypeEnum.Arts,
            )
        ).map { it.card }
        val pickedDefault = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0)
        ).map { it.card }

        // Should fall back to default behaviour
        assertThat(picked).isEqualTo(pickedDefault)
    }
}