package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.modules.attack.MightyChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.AttackUtils
import kotlin.test.BeforeTest
import kotlin.test.Test

class MightyChainTest {
    lateinit var mightyChainHandler: MightyChainHandler

    @BeforeTest
    fun init() {
        mightyChainHandler = MightyChainHandler(
            utils = AttackUtils()
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP
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
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        val picked = mightyChainHandler.pick(
            cards = cards
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.E,
            CommandCard.Face.C,
            CommandCard.Face.B,
            CommandCard.Face.D
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        )?.map { it.card } ?: emptyList()

        // Cannot detect NP type, so unable to Mighty Chain
        assertThat(picked).isEmpty()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
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
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        )?.map { it.card } ?: emptyList()

        // Unable to Mighty Chain with Scathach NP since type is unknown.
        assertThat(picked).isEmpty()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Quick
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
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0)
        )?.map { it.card } ?: emptyList()

        // Unable to Mighty Chain with Nero NP since type is unknown.
        assertThat(picked).isEmpty()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.C to CardTypeEnum.Arts
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
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 1Kama-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup2
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            ),
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.E,
            CommandCard.Face.B,
            CommandCard.Face.D,
        )
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 2Scathach-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup2
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Quick
            ),
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.E,
            CommandCard.Face.B,
            CommandCard.Face.D,
        )
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 3Nero-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup2
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.C to CardTypeEnum.Arts
            ),
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.E,
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.D,
        )
    }

    @Test
    fun `SingleServantOnly - lineup1 (1B,2Q,3A,4B,5Q)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
        )?.map { it.card } ?: emptyList()

        // Expect same result as input
        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `SingleServantOnly - lineup2 (1B,4B,5Q,2Q,3A)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup2
        val picked = mightyChainHandler.pick(
            cards = cards
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.E,
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.B,
        )
    }

    @Test
    fun `SingleServantOnly - lineup3 (5Q,2Q,3A,1B,4B)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup3
        val picked = mightyChainHandler.pick(
            cards = cards,
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.E,
            CommandCard.Face.C,
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.D
        )
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup4 (1B,2Q,3Q,4B,5Q) - No mighty chain`() {
        val cards = AttackLineUps.SingleServantOnly.lineup4
        val picked = mightyChainHandler.pick(
            cards = cards
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup5 (1Q,2Q,3Q,4B,5B) - No mighty chain`() {
        val cards = AttackLineUps.SingleServantOnly.lineup5
        val picked = mightyChainHandler.pick(
            cards = cards
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }

    /**
     * Two NP scenario
     */
    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (2Kama, 3Nero) - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.B, CommandCard.NP.C), 0)
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 3Nero), with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.C to CardTypeEnum.Arts
            )
        )?.map { it.card } ?: emptyList()

        // Expect SB,KQ,NA,NA,SQ - 12345 - ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach) - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach), with npTypes - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick
            )
        )?.map { it.card } ?: emptyList()

        assertThat(picked).isEmpty()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3 NP, with npTypes - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChainHandler.pick(
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