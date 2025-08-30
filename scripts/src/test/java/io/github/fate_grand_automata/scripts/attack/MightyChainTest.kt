package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNull
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.modules.attack.ApplyMightyChains
import kotlin.test.Test

class MightyChainTest {
    val mightyChain = ApplyMightyChains()

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards
        )?.map { it.card }

        // Expect SB,KQ,NA,NA,SQ - 12345 - ABCDE
        assertThat(picked ?: emptyList()).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2SQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        val picked = mightyChain.getMightyChain(
            cards = cards
        )?.map { it.card }

        // Expect 1SB,2KQ,3NA,5SQ,4NA - 15324 - AECBD
        assertThat(picked ?: emptyList()).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        )?.map { it.card }

        // Unable to Brave Chain with Kama. Will ignore and return result of withNp
        assertThat(picked).isNull()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            )
        )?.map { it.card }

        // Expect 1SB,3NA,2KQ,4NA,5SQ - 13245 - ACBDE
        assertThat(picked ?: emptyList()).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        )?.map { it.card }

        // Unable to Mighty Chain with Scathach NP since type is unknown.
        assertThat(picked).isNull()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Quick
            )
        )?.map { it.card }

        // Expect 1SB,3NA,2KQ,4NA,5SQ - 13245 - ACBDE
        assertThat(picked ?: emptyList()).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0)
        )?.map { it.card }

        // Unable to Mighty Chain with Nero NP since type is unknown.
        assertThat(picked).isNull()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.C to CardTypeEnum.Arts
            )
        )?.map { it.card }

        // Expect 1SB,2KQ,3NA,4NA,5SQ - 12345 - ABCDE
        assertThat(picked ?: emptyList()).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `SingleServantOnly - lineup1 (BQABQ)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
        )?.map { it.card }

        // Expect same result as input
        // Expect BQABQ / 12345 / ABCDE
        assertThat(picked ?: emptyList()).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `SingleServantOnly - lineup2 (BBQQA)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup2
        val picked = mightyChain.getMightyChain(
            cards = cards
        )?.map { it.card }

        // Expect 1SB,5SQ,3NA,4NA,2KQ / 15342 / AECDB
        assertThat(picked ?: emptyList()).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.B)
    }

    @Test
    fun `SingleServantOnly - lineup3 (QQABB)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup3
        val picked = mightyChain.getMightyChain(
            cards = cards,
        )?.map { it.card }

        // Expect QABQB / 53124 / ECABD
        assertThat(picked ?: emptyList()).containsExactly(CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.D)
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup4 (BQQBQ) - No mighty chain`() {
        val cards = AttackLineUps.SingleServantOnly.lineup4
        val picked = mightyChain.getMightyChain(
            cards = cards
        )?.map { it.card }

        assertThat(picked).isNull()
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup5 (QQQBB) - No mighty chain`() {
        val cards = AttackLineUps.SingleServantOnly.lineup5
        val picked = mightyChain.getMightyChain(
            cards = cards
        )?.map { it.card }

        assertThat(picked).isNull()
    }

    /**
     * Two NP scenario
     */
    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (2Kama, 3Nero) - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.B, CommandCard.NP.C), 0)
        )?.map { it.card }

        assertThat(picked).isNull()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 3Nero), with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.C to CardTypeEnum.Arts
            )
        )?.map { it.card }

        // Expect SB,KQ,NA,NA,SQ - 12345 - ABCDE
        assertThat(picked ?: emptyList()).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach) - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
        )?.map { it.card }

        assertThat(picked).isNull()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach), with npTypes - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick
            )
        )?.map { it.card }

        assertThat(picked).isNull()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3 NP, with npTypes - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = mightyChain.getMightyChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick,
                FieldSlot.C to CardTypeEnum.Arts,
            )
        )?.map { it.card }

        assertThat(picked).isNull()
    }
}