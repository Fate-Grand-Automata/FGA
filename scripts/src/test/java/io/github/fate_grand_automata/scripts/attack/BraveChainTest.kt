package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.modules.attack.AttackUtils
import io.github.fate_grand_automata.scripts.modules.attack.BraveChainHandler
import kotlin.test.BeforeTest
import kotlin.test.Test

class BraveChainTest {
    lateinit var braveChainHandler: BraveChainHandler

    val braveChainEnums = BraveChainEnum.entries

    @BeforeTest
    fun init() {
        braveChainHandler = BraveChainHandler(
            utils = AttackUtils()
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama + 2Scathach)`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0)
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 1Kama-NP`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 2Scathach-NP`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 3Nero-NP`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.A,
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `BusterFocus - lineup01 (1KB,2KB,3NA,4NA,5KB)`() {
        val cards = AttackLineUps.BusterFocus.lineup01
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `BusterFocus - lineup01 (1KB,2KB,3NA,4NA,5KB) + 1 NP (2Kiyohime)`() {
        val cards = AttackLineUps.BusterFocus.lineup01
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.E,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `BusterFocus - lineup01 (1KB,2KB,3NA,4NA,5KB) + 1 NP (3Nero)`() {
        val cards = AttackLineUps.BusterFocus.lineup01
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `BusterFocus - lineup02 (1KB,5KB,2KB,3NA,4NA)`() {
        val cards = AttackLineUps.BusterFocus.lineup02
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `BusterFocus - lineup05 (1KB,2KB,3NA,4NB,5KB)`() {
        val cards = AttackLineUps.BusterFocus.lineup05
        for (braveChainEnum in braveChainEnums) {
            val picked = braveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }
}