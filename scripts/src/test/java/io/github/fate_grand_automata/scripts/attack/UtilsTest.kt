package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.modules.attack.Utils
import kotlin.test.Test

class UtilsTest {
    val utils = Utils()

    @Test
    fun `getCardsPerFieldSlotMap, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getCardsPerFieldSlotMap(
            cards = cards,
        )

        assertThat(result.size, "Result.size").isEqualTo(3)
        assertThat(result[FieldSlot.A], "FieldSlot.A").isEqualTo(1)
        assertThat(result[FieldSlot.B], "FieldSlot.B").isEqualTo(2)
        assertThat(result[FieldSlot.C], "FieldSlot.C").isEqualTo(2)
    }

    @Test
    fun `getCardsPerFieldSlotMap, Standard - lineup2 (1SB,5SQ,2SQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        val result = utils.getCardsPerFieldSlotMap(
            cards = cards,
        )

        assertThat(result.size, "Result.size").isEqualTo(3)
        assertThat(result[FieldSlot.A], "FieldSlot.A").isEqualTo(1)
        assertThat(result[FieldSlot.B], "FieldSlot.B").isEqualTo(2)
        assertThat(result[FieldSlot.C], "FieldSlot.C").isEqualTo(2)
    }

    @Test
    fun `getCardsPerFieldSlotMap, BusterFocus - lineup01 (1KB,2KB,3NA,4NA,5KB)`() {
        val cards = AttackLineUps.BusterFocus.lineup01
        val result = utils.getCardsPerFieldSlotMap(
            cards = cards,
        )

        assertThat(result.size, "Result.size").isEqualTo(2)
        assertThat(result[FieldSlot.B], "FieldSlot.B").isEqualTo(3)
        assertThat(result[FieldSlot.C], "FieldSlot.C").isEqualTo(2)
    }

    @Test
    fun `getCardsPerFieldSlotMap, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1-KamaNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getCardsPerFieldSlotMap(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        )

        assertThat(result.size, "Result.size").isEqualTo(3)
        assertThat(result[FieldSlot.A], "FieldSlot.A").isEqualTo(2)
        assertThat(result[FieldSlot.B], "FieldSlot.B").isEqualTo(2)
        assertThat(result[FieldSlot.C], "FieldSlot.C").isEqualTo(2)
    }

    @Test
    fun `getCardsPerFieldSlotMap, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2-ScathachNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getCardsPerFieldSlotMap(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        )

        assertThat(result.size, "Result.size").isEqualTo(3)
        assertThat(result[FieldSlot.A], "FieldSlot.A").isEqualTo(1)
        assertThat(result[FieldSlot.B], "FieldSlot.B").isEqualTo(3)
        assertThat(result[FieldSlot.C], "FieldSlot.C").isEqualTo(2)
    }

    @Test
    fun `getFieldSlotsWithValidBraveChain, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getFieldSlotsWithValidBraveChain(
            cards = cards,
        )

        assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `getFieldSlotsWithValidBraveChain, Standard - lineup2 (1SB,5SQ,2SQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        val result = utils.getFieldSlotsWithValidBraveChain(
            cards = cards,
        )

        assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `getFieldSlotsWithValidBraveChain, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1-KamaNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getFieldSlotsWithValidBraveChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        )

        assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `getFieldSlotsWithValidBraveChain, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2-ScathachNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getFieldSlotsWithValidBraveChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        )

        assertThat(result.size, "Result.size").isEqualTo(1)
        assertThat(result[0], "FieldSlotB").isEqualTo(FieldSlot.B)
    }

    @Test
    fun `getCardsPerCardTypeMap, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getCardsPerCardTypeMap(
            cards = cards,
        )

        assertThat(result.size).isEqualTo(3)
        assertThat(result[CardTypeEnum.Arts], "Arts").isEqualTo(2)
        assertThat(result[CardTypeEnum.Quick], "Quick").isEqualTo(2)
        assertThat(result[CardTypeEnum.Buster], "Buster").isEqualTo(1)
    }

    @Test
    fun `getCardsPerCardTypeMap, Standard - lineup2 (1SB,5SQ,2SQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        val result = utils.getCardsPerCardTypeMap(
            cards = cards,
        )

        assertThat(result.size).isEqualTo(3)
        assertThat(result[CardTypeEnum.Arts], "Arts").isEqualTo(2)
        assertThat(result[CardTypeEnum.Quick], "Quick").isEqualTo(2)
        assertThat(result[CardTypeEnum.Buster], "Buster").isEqualTo(1)
    }

    @Test
    fun `getCardsPerCardTypeMap, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1-KamaNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getCardsPerCardTypeMap(
            cards = cards,
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            )
        )

        assertThat(result.size).isEqualTo(3)
        assertThat(result[CardTypeEnum.Arts], "Arts").isEqualTo(2)
        assertThat(result[CardTypeEnum.Quick], "Quick").isEqualTo(3)
        assertThat(result[CardTypeEnum.Buster], "Buster").isEqualTo(1)
    }

    @Test
    fun `getCardsPerCardTypeMap, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2-ScathachNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getCardsPerCardTypeMap(
            cards = cards,
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Quick
            )
        )

        assertThat(result.size, "Result.size").isEqualTo(3)
        assertThat(result[CardTypeEnum.Arts], "Arts").isEqualTo(2)
        assertThat(result[CardTypeEnum.Quick], "Quick").isEqualTo(3)
        assertThat(result[CardTypeEnum.Buster], "Buster").isEqualTo(1)
    }

    @Test
    fun `getCardsPerCardTypeMap, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3-NeroNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getCardsPerCardTypeMap(
            cards = cards,
            npTypes = mapOf(
                FieldSlot.C to CardTypeEnum.Arts
            )
        )

        assertThat(result.size, "Result.size").isEqualTo(3)
        assertThat(result[CardTypeEnum.Arts], "Arts").isEqualTo(3)
        assertThat(result[CardTypeEnum.Quick], "Quick").isEqualTo(2)
        assertThat(result[CardTypeEnum.Buster], "Buster").isEqualTo(1)
    }

    @Test
    fun `getCardsPerCardTypeMap, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1-KamaNP + 2-ScathachNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getCardsPerCardTypeMap(
            cards = cards,
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick
            )
        )

        assertThat(result.size, "Result.size").isEqualTo(3)
        assertThat(result[CardTypeEnum.Arts], "Arts").isEqualTo(2)
        assertThat(result[CardTypeEnum.Quick], "Quick").isEqualTo(4)
        assertThat(result[CardTypeEnum.Buster], "Buster").isEqualTo(1)
    }

    @Test
    fun `getCardsPerCardTypeMap, BusterFocus - lineup01 (1KB,2KB,3NA,4NA,5KB)`() {
        val cards = AttackLineUps.BusterFocus.lineup01
        val result = utils.getCardsPerCardTypeMap(
            cards = cards,
        )

        assertThat(result.size, "Result.size").isEqualTo(2)
        assertThat(result[CardTypeEnum.Arts], "Arts").isEqualTo(2)
        assertThat(result[CardTypeEnum.Quick], "Quick").isEqualTo(null)
        assertThat(result[CardTypeEnum.Buster], "Buster").isEqualTo(3)
    }
}