package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import kotlin.test.Test
import io.github.fate_grand_automata.scripts.modules.attack.AvoidChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.AttackUtils

class AvoidChainTest {
    val avoidChainHandler = AvoidChainHandler(utils = AttackUtils())

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = avoidChainHandler.pick(
            cards = cards,
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.E,
            CommandCard.Face.C,
            CommandCard.Face.D,
        )
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        val picked = avoidChainHandler.pick(
            cards = cards
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.E,
            CommandCard.Face.C,
            CommandCard.Face.D,
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = avoidChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = avoidChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            )
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = avoidChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Quick
            )
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = avoidChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.C to CardTypeEnum.Arts
            )
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.B,
            CommandCard.Face.D,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `SingleServantOnly - lineup1 (1B,2Q,3A,4B,5Q)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup1
        val picked = avoidChainHandler.pick(
            cards = cards,
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.D,
            CommandCard.Face.C,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `SingleServantOnly - lineup2 (1B,4B,5Q,2Q,3A)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup2
        val picked = avoidChainHandler.pick(
            cards = cards
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.D,
            CommandCard.Face.E,
            CommandCard.Face.B,
            CommandCard.Face.C,
        )
    }

    @Test
    fun `SingleServantOnly - lineup3 (5Q,2Q,3A,1B,4B)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup3
        val picked = avoidChainHandler.pick(
            cards = cards,
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.E,
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.A,
            CommandCard.Face.D,
        )
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup4 (1B,2Q,3Q,4B,5Q)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup4
        val picked = avoidChainHandler.pick(
            cards = cards
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.E,
        )
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup5 (5Q,2Q,3Q,1B,4B)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup5
        val picked = avoidChainHandler.pick(
            cards = cards
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.E,
            CommandCard.Face.B,
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.D,
        )
    }

    /**
     * Two NP scenario
     */
    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 3Nero), with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = avoidChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.C to CardTypeEnum.Arts
            )
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.B,
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach), with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = avoidChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick
            )
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1B, 2B), with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = avoidChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Buster,
                FieldSlot.B to CardTypeEnum.Buster
            )
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.B,
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3 NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = avoidChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick,
                FieldSlot.C to CardTypeEnum.Arts,
            )
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }
}