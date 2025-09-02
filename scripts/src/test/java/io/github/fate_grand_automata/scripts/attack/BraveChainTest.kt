package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.modules.attack.Utils
import io.github.fate_grand_automata.scripts.modules.attack.BraveChainHandler
import kotlin.test.BeforeTest
import kotlin.test.Test

class BraveChainTest {
    lateinit var braveChainHandler: BraveChainHandler

    @BeforeTest
    fun init() {
        braveChainHandler = BraveChainHandler(
            utils = Utils()
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ), None - No Brave chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = braveChainHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.None
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ), WithNP - No Brave chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = braveChainHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, WithNP - No Brave chain` () {
        val cards = AttackLineUps.Standard.lineup1
        val picked = braveChainHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, WithNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = braveChainHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        )?.map { it.card } ?: emptyList()

        // Expect 1SB,2KQ,3NA,5SQ,4NA - 15324 - AECBD
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun `BusterFocus - lineup1 (1KB,2KB,3NA,4NA,5KB) + 1 NP (3Nero)`() {
        val cards = AttackLineUps.BusterFocus.lineup01
        val picked = braveChainHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ), Avoid`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = braveChainHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.Avoid
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA), Avoid`() {
        val cards = AttackLineUps.Standard.lineup2
        val picked = braveChainHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.Avoid
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }
}