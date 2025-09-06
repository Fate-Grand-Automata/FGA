package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.modules.attack.AttackUtils
import kotlin.test.Test

class AttackUtilsTest {
    val utils = AttackUtils()
    val braveChainEnums = BraveChainEnum.entries

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
    fun `getCardsPerFieldSlotMap, Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
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
    fun `getCardsPerFieldSlotMap, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3-NeroNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getCardsPerFieldSlotMap(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0)
        )

        assertThat(result.size, "Result.size").isEqualTo(3)
        assertThat(result[FieldSlot.A], "FieldSlot.A").isEqualTo(1)
        assertThat(result[FieldSlot.B], "FieldSlot.B").isEqualTo(2)
        assertThat(result[FieldSlot.C], "FieldSlot.C").isEqualTo(3)
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
    fun `getFieldSlotsWithValidBraveChain, Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
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
    fun `getFieldSlotsWithValidBraveChain, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3-NeroNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getFieldSlotsWithValidBraveChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0)
        )

        assertThat(result.size, "Result.size").isEqualTo(1)
        assertThat(result[0], "FieldSlotC").isEqualTo(FieldSlot.C)
    }

    @Test
    fun `getFieldSlotsWithValidBraveChain, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1-KamaNP + 2-ScathachNP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getFieldSlotsWithValidBraveChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0)
        )

        assertThat(result.size, "Result.size").isEqualTo(0)
    }

    @Test
    fun `getFieldSlotsWithValidBraveChain, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + All 3 NP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getFieldSlotsWithValidBraveChain(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0)
        )

        assertThat(result.size, "Result.size").isEqualTo(0)
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
    fun `getCardsPerCardTypeMap, Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
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

    @Test
    fun `getBraveChainFieldSlot, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.Standard.lineup1
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )

            assertThat(result, braveChainEnum.toString()).isNull()
        }
    }

    @Test
    fun `getBraveChainFieldSlot, Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.Standard.lineup2
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )

            assertThat(result, braveChainEnum.toString()).isNull()
        }
    }

    @Test
    fun `getBraveChainFieldSlot, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1-KamaNP`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.Standard.lineup2
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                braveChainEnum = braveChainEnum,
            )

            assertThat(result, braveChainEnum.toString()).isNull()
        }
    }

    @Test
    fun `getBraveChainFieldSlot, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2-ScathachNP`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.Standard.lineup1
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                braveChainEnum = braveChainEnum,
            )

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, -> assertThat(result, braveChainEnum.toString())
                    .isEqualTo(FieldSlot.B)
                else -> assertThat(result, braveChainEnum.toString()).isNull()
            }
        }
    }

    @Test
    fun `getBraveChainFieldSlot, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3-NeroNP`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.Standard.lineup1
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                braveChainEnum = braveChainEnum,
            )

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, -> assertThat(result, braveChainEnum.toString())
                    .isEqualTo(FieldSlot.C)
                else -> assertThat(result, braveChainEnum.toString()).isNull()
            }
        }
    }

    @Test
    fun `getBraveChainFieldSlot, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1-KamaNP + 2-ScathachNP`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.Standard.lineup1
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
                braveChainEnum = braveChainEnum,
            )

            assertThat(result, braveChainEnum.toString()).isNull()
        }
    }

    @Test
    fun `getBraveChainFieldSlot, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + All 3 NP`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.Standard.lineup1
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0),
                braveChainEnum = braveChainEnum,
            )

            assertThat(result, braveChainEnum.toString()).isNull()
        }
    }

    @Test
    fun `getBraveChainFieldSlot, BusterFocus - lineup01 (1KB,2KB,3NA,4NA,5KB)`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.BusterFocus.lineup01
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )

            when (braveChainEnum) {
                BraveChainEnum.Always -> assertThat(result, braveChainEnum.toString())
                    .isEqualTo(FieldSlot.B)
                else -> assertThat(result, braveChainEnum.toString()).isNull()
            }
        }
    }

    @Test
    fun `getBraveChainFieldSlot, BusterFocus - lineup03 (1KB,2KB,3NA,4NA,5NB)`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.BusterFocus.lineup03
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )

            when (braveChainEnum) {
                BraveChainEnum.Always -> assertThat(result, braveChainEnum.toString())
                    .isEqualTo(FieldSlot.C)
                else -> assertThat(result, braveChainEnum.toString()).isNull()
            }
        }
    }

    @Test
    fun `getBraveChainFieldSlot, BusterFocus - lineup03 (1KB,2KB,3NA,4NA,5NB) - 2Kiyo-NP`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.BusterFocus.lineup03
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                braveChainEnum = braveChainEnum,
            )

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, -> assertThat(result, braveChainEnum.toString())
                    .isEqualTo(FieldSlot.B)
                else -> assertThat(result, braveChainEnum.toString()).isNull()
            }
        }
    }

    @Test
    fun `getBraveChainFieldSlot, BusterFocus - lineup03 (1KB,2KB,3NA,4NA,5NB) - 3Nero-NP`() {
        for (braveChainEnum in braveChainEnums) {
            val cards = AttackLineUps.BusterFocus.lineup03
            val result = utils.getBraveChainFieldSlot(
                cards = cards,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                braveChainEnum = braveChainEnum,
            )

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, -> assertThat(result, braveChainEnum.toString())
                    .isEqualTo(FieldSlot.C)
                else -> assertThat(result, braveChainEnum.toString()).isNull()
            }
        }
    }

    @Test
    fun `getValidNonUnknownCards, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.getValidNonUnknownCards(
            cards = cards,
        )

        assertThat(result.size).isEqualTo(5)
    }

    @Test
    fun `getValidNonUnknownCards, Unknown - lineup1 (1 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup1
        val result = utils.getValidNonUnknownCards(
            cards = cards,
        )

        assertThat(result.size).isEqualTo(4)
    }

    @Test
    fun `getValidNonUnknownCards, Unknown - lineup2 (2 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup2
        val result = utils.getValidNonUnknownCards(
            cards = cards,
        )

        assertThat(result.size).isEqualTo(3)
    }

    @Test
    fun `getValidNonUnknownCards, Unknown - lineup3 (3 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup3
        val result = utils.getValidNonUnknownCards(
            cards = cards,
        )

        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun `getValidNonUnknownCards, Unknown - lineup4 (5 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup4
        val result = utils.getValidNonUnknownCards(
            cards = cards,
        )

        assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `isChainable, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.isChainable(
            cards = cards,
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1 Valid NP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1 Valid NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            ),
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1 Unknown NP`() {
        val cards = AttackLineUps.Standard.lineup1
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Unknown
            ),
        )

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `isChainable, Unknown - lineup1 (1 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup1
        val result = utils.isChainable(
            cards = cards,
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Unknown - lineup1 (1 Unknown) + 1 Valid NP`() {
        val cards = AttackLineUps.Unknown.lineup1
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Unknown - lineup1 (1 Unknown) + 1 Valid NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup1
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            ),
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Unknown - lineup1 (1 Unknown) + 1 Unknown NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup1
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Unknown
            ),
        )

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `isChainable, Unknown - lineup2 (2 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup2
        val result = utils.isChainable(
            cards = cards,
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Unknown - lineup3 (3 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup3
        val result = utils.isChainable(
            cards = cards,
        )

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `isChainable, Unknown - lineup3 (3 Unknown) + Valid NP`() {
        val cards = AttackLineUps.Unknown.lineup3
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Unknown - lineup3 (3 Unknown) + Valid NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup3
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            ),
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Unknown - lineup4 (5 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup4
        val result = utils.isChainable(
            cards = cards,
        )

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `isChainable, Unknown - lineup4 (5 Unknown) + 1 Valid NP`() {
        val cards = AttackLineUps.Unknown.lineup4
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
        )

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `isChainable, Unknown - lineup4 (5 Unknown) + 1 Valid NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup4
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            ),
        )

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `isChainable, Unknown - lineup4 (5 Unknown) + 2 Valid NP`() {
        val cards = AttackLineUps.Unknown.lineup4
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
        )

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `isChainable, Unknown - lineup4 (5 Unknown) + 2 Valid NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup4
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick,
            ),
        )

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `isChainable, Unknown - lineup4 (5 Unknown) + 3 Valid NP`() {
        val cards = AttackLineUps.Unknown.lineup4
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0),
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Unknown - lineup4 (5 Unknown) + 3 Valid NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup4
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Arts,
            ),
        )

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `isChainable, Unknown - lineup4 (5 Unknown) + 2 Valid NP + 1 Unknown NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup4
        val result = utils.isChainable(
            cards = cards,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Unknown,
            ),
        )

        assertThat(result).isEqualTo(false)
    }
}