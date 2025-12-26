package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.modules.attack.AvoidChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.BraveChainHandler
import kotlin.test.Test

class BraveChainTest {
    val braveChainEnums = BraveChainEnum.entries

    fun assertDefaultAvoidChain (
        cards: List<ParsedCard>,
        picked: List<CommandCard.Face>,
        braveChainEnum: BraveChainEnum,
        npUsage: NPUsage = NPUsage.none,
    ) {
        val defaultAvoid = AvoidChainHandler.pick(
            cards = cards,
            braveChainEnum = braveChainEnum,
            npUsage = npUsage,
            avoidCardChains = false,
        )?.map { it.card } ?: emptyList()
        assertThat(picked, BraveChainEnum.Avoid.toString()).isEqualTo(defaultAvoid)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                    picked = picked,
                )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                    picked = picked,
                )
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
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                    picked = picked,
                )
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
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
                    picked = picked,
                )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 1Kama-NP`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                    picked = picked,
                )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 2Scathach-NP`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                    picked = picked,
                )
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
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                    picked = picked,
                )
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
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                )
                BraveChainEnum.Always ->
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
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                    picked = picked,
                )
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
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                    picked = picked,
                )
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
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                )
                BraveChainEnum.Always ->
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
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                )
                BraveChainEnum.Always ->
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
    fun `Unknown - lineup1 (1 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Unknown - lineup1 (1 Unknown) + 1 Valid NP`() {
        val cards = AttackLineUps.Unknown.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                    npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                )
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.A,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Unknown - lineup2 (2 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Unknown - lineup2 (2 Unknown) + 1 Valid NP`() {
        val cards = AttackLineUps.Unknown.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                    npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                )
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.E,
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Unknown - lineup3 (3 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup3
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Unknown - lineup5 (1 Unknown, Brave Chain available)`() {
        val cards = AttackLineUps.Unknown.lineup5
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                )
                BraveChainEnum.Always ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.D,
                        CommandCard.Face.C,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Unknown - lineup5 (1 Unknown) + 1 Valid NP`() {
        val cards = AttackLineUps.Unknown.lineup5
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                    npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                )
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.D,
                        CommandCard.Face.E,
                        CommandCard.Face.C,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Unknown - lineup6 (2 Unknown, no Brave Chain available)`() {
        val cards = AttackLineUps.Unknown.lineup6
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }

    @Test
    fun `Unknown - lineup6 (2 Unknown) + 1 Valid NP`() {
        val cards = AttackLineUps.Unknown.lineup6
        for (braveChainEnum in braveChainEnums) {
            val picked = BraveChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid -> assertDefaultAvoidChain(
                    cards = cards,
                    braveChainEnum = braveChainEnum,
                    picked = picked,
                    npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                )
                BraveChainEnum.WithNP,
                BraveChainEnum.Always, ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.D,
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                    )
                else -> assertThat(picked, braveChainEnum.toString()).isEmpty()
            }
        }
    }
}