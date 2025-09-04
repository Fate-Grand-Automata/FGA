package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import kotlin.test.Test
import io.github.fate_grand_automata.scripts.modules.attack.ColorChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.AttackUtils

class ColorChainTest {
    val colorChain = ColorChainHandler(utils = AttackUtils())
    val cardTypeList = CardTypeEnum.entries

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick
                ),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with npTypes & forceBraveChain`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick
                ),
                forceBraveChain = true,
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.B to CardTypeEnum.Quick
                ),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes & forceBraveChain`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.B to CardTypeEnum.Quick
                ),
                forceBraveChain = true,
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.C to CardTypeEnum.Arts
                ),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Arts ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with npTypes & forceBraveChain`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.C to CardTypeEnum.Arts
                ),
                forceBraveChain = true,
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `SingleServantOnly - lineup1 (1B,2Q,3A,4B,5Q)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `SingleServantOnly - lineup2 (1B,4B,5Q,2Q,3A)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup2
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `SingleServantOnly - lineup3 (5Q,2Q,3A,1B,4B)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup3
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `SingleServantOnly - lineup4 (1B,2Q,3Q,4B,5Q)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup4
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.E,
                        CommandCard.Face.A,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup5 (5Q,2Q,3Q,1B,4B)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup5
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.A,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    /**
     * Two NP scenario
     */
    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 3Nero), with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick,
                    FieldSlot.C to CardTypeEnum.Arts
                ),
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach), with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick,
                    FieldSlot.B to CardTypeEnum.Quick,
                ),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1B, 2B), with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Buster,
                    FieldSlot.B to CardTypeEnum.Buster,
                ),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Buster ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.E,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3 NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick,
                    FieldSlot.B to CardTypeEnum.Quick,
                    FieldSlot.C to CardTypeEnum.Arts,
                ),
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `BusterFocus - lineup1 (1KB,2KB,3NA,4NA,5KB)`() {
        val cards = AttackLineUps.BusterFocus.lineup01
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Buster ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `BusterFocus - lineup1 (1KB,2KB,3NA,4NA,5KB) + 1 NP (2Kiyohime), with npTypes`() {
        val cards = AttackLineUps.BusterFocus.lineup01
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.B to CardTypeEnum.Buster,
                ),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Buster ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `BusterFocus - lineup1 (1KB,2KB,3NA,4NA,5KB) + 1 NP (3Nero), with npTypes`() {
        val cards = AttackLineUps.BusterFocus.lineup01
        for (cardType in cardTypeList) {
            val result = colorChain.pick(
                cards = cards,
                cardType = cardType,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.C to CardTypeEnum.Arts,
                ),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Arts ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    /**
     * Special cases
     */
    @Test
    fun `getCardsForAvoidBraveChain, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = cards.filter { it.type == cardType },
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `getCardsForAvoidBraveChain, Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        for (cardType in cardTypeList) {
            val result = colorChain.getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = cards.filter { it.type == cardType },
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `getCardsForAvoidBraveChain, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + Slot 1 NP`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = cards.filter { it.type == cardType },
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `getCardsForAvoidBraveChain, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + Slot 2 NP`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = cards.filter { it.type == cardType },
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `getCardsForAvoidBraveChain, Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + Slot 3 NP`() {
        val cards = AttackLineUps.Standard.lineup1
        for (cardType in cardTypeList) {
            val result = colorChain.getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = cards.filter { it.type == cardType },
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `getCardsForAvoidBraveChain, Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + Slot 1 NP`() {
        val cards = AttackLineUps.Standard.lineup2
        for (cardType in cardTypeList) {
            val result = colorChain.getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = cards.filter { it.type == cardType },
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `getCardsForAvoidBraveChain, Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + Slot 2 NP`() {
        val cards = AttackLineUps.Standard.lineup2
        for (cardType in cardTypeList) {
            val result = colorChain.getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = cards.filter { it.type == cardType },
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `getCardsForAvoidBraveChain, Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + Slot 3 NP`() {
        val cards = AttackLineUps.Standard.lineup2
        for (cardType in cardTypeList) {
            val result = colorChain.getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = cards.filter { it.type == cardType },
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            )?.map { it.card } ?: emptyList()

            when (cardType) {
                CardTypeEnum.Quick ->
                    assertThat(result, cardType.toString()).containsExactly(
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(result, cardType.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `getCardsForAvoidBraveChain, BusterFocus - lineup01 (1KB,2KB,3NA,4NA,5KB)`() {
        val cards = AttackLineUps.BusterFocus.lineup01
        for (cardType in cardTypeList) {
            val result = colorChain.getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = cards.filter { it.type == cardType },
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }

    @Test
    fun `getCardsForAvoidBraveChain, BusterFocus - lineup02 (1KB,5KB,2KB,3NA,4NA)`() {
        val cards = AttackLineUps.BusterFocus.lineup02
        for (cardType in cardTypeList) {
            val result = colorChain.getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = cards.filter { it.type == cardType },
            )?.map { it.card } ?: emptyList()

            assertThat(result, cardType.toString()).isEmpty()
        }
    }
}