package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.modules.attack.MightyChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.AttackUtils
import kotlin.test.BeforeTest
import kotlin.test.Test

class MightyChainTest {
    lateinit var mightyChainHandler: MightyChainHandler
    lateinit var utils: AttackUtils

    val braveChainEnums = BraveChainEnum.entries

    @BeforeTest
    fun init() {
        mightyChainHandler = MightyChainHandler()
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.A,
                CommandCard.Face.B,
                CommandCard.Face.C,
                CommandCard.Face.D,
                CommandCard.Face.E,
            )
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.A,
                CommandCard.Face.E,
                CommandCard.Face.C,
                CommandCard.Face.B,
                CommandCard.Face.D,
            )
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            )?.map { it.card } ?: emptyList()

            // Cannot detect NP type, so unable to Mighty Chain
            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick
                )
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.A,
                CommandCard.Face.C,
                CommandCard.Face.B,
                CommandCard.Face.D,
                CommandCard.Face.E,
            )
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
            )?.map { it.card } ?: emptyList()

            // Unable to Mighty Chain with Scathach NP since type is unknown.
            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.B to CardTypeEnum.Quick
                )
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Always -> assertThat(picked, braveChainEnum.toString()).isEmpty()
                else ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.B,
                        CommandCard.Face.D,
                        CommandCard.Face.E,
                    )
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with Arts as npType`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.B to CardTypeEnum.Arts
                )
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.WithNP,
                BraveChainEnum.Always ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.E,
                    )
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0)
            )?.map { it.card } ?: emptyList()

            // Unable to Mighty Chain with Nero NP since type is unknown.
            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.C to CardTypeEnum.Arts
                )
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Always -> assertThat(picked, braveChainEnum.toString()).isEmpty()
                else ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.E,
                    )
            }
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with npTypes & forcedBraveChain`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.C to CardTypeEnum.Arts
                ),
                forceBraveChain = true
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 1Kama-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick
                ),
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.A,
                CommandCard.Face.C,
                CommandCard.Face.E,
                CommandCard.Face.B,
                CommandCard.Face.D,
            )
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 2Scathach-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.B to CardTypeEnum.Quick
                ),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Always -> assertThat(picked, braveChainEnum.toString()).isEmpty()
                else ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.D,
                    )
            }
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 2Scathach-NP, with Arts as npType`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.B to CardTypeEnum.Arts
                )
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.None,
                BraveChainEnum.WithNP,
                BraveChainEnum.Always ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
                else ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
            }
        }
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA) + 3Nero-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.C to CardTypeEnum.Arts
                ),
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Always -> assertThat(picked, braveChainEnum.toString()).isEmpty()
                else ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
            }
        }
    }

    @Test
    fun `SingleServantOnly - lineup1 (1B,2Q,3A,4B,5Q) + 3Nero-NP, with npTypes`() {
        val cards = AttackLineUps.SingleServantOnly.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.C to CardTypeEnum.Arts
                ),
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.A,
                CommandCard.Face.B,
                CommandCard.Face.C,
                CommandCard.Face.D,
                CommandCard.Face.E,
            )
        }
    }

    @Test
    fun `SingleServantOnly - lineup1 (1B,2Q,3A,4B,5Q) + Buster NP, with npTypes`() {
        val cards = AttackLineUps.SingleServantOnly.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.C to CardTypeEnum.Buster
                ),
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.B,
                CommandCard.Face.C,
                CommandCard.Face.A,
                CommandCard.Face.D,
                CommandCard.Face.E,
            )
        }
    }

    @Test
    fun `SingleServantOnly - lineup2 (1B,4B,5Q,2Q,3A)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.A,
                CommandCard.Face.E,
                CommandCard.Face.C,
                CommandCard.Face.D,
                CommandCard.Face.B,
            )
        }
    }

    @Test
    fun `SingleServantOnly - lineup3 (5Q,2Q,3A,1B,4B)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup3
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.E,
                CommandCard.Face.C,
                CommandCard.Face.A,
                CommandCard.Face.B,
                CommandCard.Face.D
            )
        }
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup4 (1B,2Q,3Q,4B,5Q) - No mighty chain`() {
        val cards = AttackLineUps.SingleServantOnly.lineup4
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup5 (1Q,2Q,3Q,4B,5B) - No mighty chain`() {
        val cards = AttackLineUps.SingleServantOnly.lineup5
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    /**
     * Two NP scenario
     */
    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (2Kama, 3Nero) - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B, CommandCard.NP.C), 0)
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 3Nero), with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick,
                    FieldSlot.C to CardTypeEnum.Arts
                )
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.A,
                CommandCard.Face.B,
                CommandCard.Face.C,
                CommandCard.Face.D,
                CommandCard.Face.E,
            )
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach) - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach), with npTypes - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick,
                    FieldSlot.B to CardTypeEnum.Quick
                )
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3 NP, with npTypes - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick,
                    FieldSlot.B to CardTypeEnum.Quick,
                    FieldSlot.C to CardTypeEnum.Arts,
                )
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    /**
     * Unknown handling
     */
    @Test
    fun `Unknown - lineup1 (1 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Unknown - lineup1 (1 Unknown) + 1 Valid Buster NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Buster
                )
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.B,
                CommandCard.Face.C,
                CommandCard.Face.D,
                CommandCard.Face.E,
                CommandCard.Face.A,
            )
        }
    }

    @Test
    fun `Unknown - lineup2 (2 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Unknown - lineup2 (2 Unknown) + 1 Valid Buster NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Buster
                )
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.C,
                CommandCard.Face.E,
                CommandCard.Face.D,
                CommandCard.Face.A,
                CommandCard.Face.B,
            )
        }
    }

    @Test
    fun `Unknown - lineup3 (3 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup3
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Unknown - lineup3 (3 Unknown) + 1 Valid Buster NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup3
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Buster
                )
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.D,
                CommandCard.Face.E,
                CommandCard.Face.A,
                CommandCard.Face.B,
                CommandCard.Face.C,
            )
        }
    }

    @Test
    fun `Unknown - lineup4 (5 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup4
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Unknown - lineup5 (1 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup5
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Unknown - lineup5 (1 Unknown) + 1 Valid Quick NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup5
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick
                )
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.A,
                CommandCard.Face.D,
                CommandCard.Face.B,
                CommandCard.Face.E,
                CommandCard.Face.C,
            )
        }
    }

    @Test
    fun `Unknown - lineup6 (1 Unknown)`() {
        val cards = AttackLineUps.Unknown.lineup6
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).isEmpty()
        }
    }

    @Test
    fun `Unknown - lineup6 (1 Unknown) + 1 Valid Quick NP, with npTypes`() {
        val cards = AttackLineUps.Unknown.lineup6
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick
                )
            )?.map { it.card } ?: emptyList()

            assertThat(picked, braveChainEnum.toString()).containsExactly(
                CommandCard.Face.B,
                CommandCard.Face.D,
                CommandCard.Face.E,
                CommandCard.Face.A,
                CommandCard.Face.C,
            )
        }
    }

    /**
     * Special edge cases
     */
    @Test
    fun `getMightyChainWithoutBraveChain, MightyOutlier - lineup1 (1SB,2SA,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.MightyOutlier.lineup1
        val picked = mightyChainHandler.getMightyChainWithoutBraveChain(
            cards = cards,
            selectedCards = AttackLineUps.MightyOutlier.lineup1_DefaultMightyChain
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `getMightyChainWithoutBraveChain, MightyOutlier - lineup1 (1SB,2SA,3NA,4NA,5SQ) + ScathachNP`() {
        val cards = AttackLineUps.MightyOutlier.lineup1
        val picked = mightyChainHandler.getMightyChainWithoutBraveChain(
            cards = cards,
            selectedCards = AttackLineUps.MightyOutlier.lineup1_DefaultMightyChain_WithQuickNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.C,
        )
    }

    @Test
    fun `getMightyChainWithoutBraveChain, MightyOutlier - lineup1 (1SB,2SA,3NA,4NA,5SQ) + Non-Scathach Buster NP`() {
        val cards = AttackLineUps.MightyOutlier.lineup1
        val picked = mightyChainHandler.getMightyChainWithoutBraveChain(
            cards = cards,
            selectedCards = AttackLineUps.MightyOutlier.lineup1_DefaultMightyChain_WithNonScathachBusterNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.B,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `getMightyChainWithoutBraveChain, MightyOutlier - lineup1 (1SB,2SA,3NA,4NA,5SQ) + Non-Scathach Arts NP`() {
        val cards = AttackLineUps.MightyOutlier.lineup1
        val picked = mightyChainHandler.getMightyChainWithoutBraveChain(
            cards = cards,
            selectedCards = AttackLineUps.MightyOutlier.lineup1_DefaultMightyChain_WithNonScathachArtsNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.E,
        )
    }

    @Test
    fun `getMightyChainWithoutBraveChain, MightyOutlier - lineup1 (1SB,2SA,3NA,4NA,5SQ) + Non-Scathach Quick NP`() {
        val cards = AttackLineUps.MightyOutlier.lineup1
        val picked = mightyChainHandler.getMightyChainWithoutBraveChain(
            cards = cards,
            selectedCards = AttackLineUps.MightyOutlier.lineup1_DefaultMightyChain_WithQuickNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
        )?.map { it.card } ?: emptyList()

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
        )
    }

    @Test
    fun `MightyOutlier - lineup1 (1SB,2SA,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.MightyOutlier.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.D,
                    )
                else ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.E,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
            }
        }
    }

    @Test
    fun `MightyOutlier - lineup1 (1SB,2SA,3NA,4NA,5SQ) + 1 NP (Scathach)`() {
        val cards = AttackLineUps.MightyOutlier.lineup1
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                npTypes = mapOf(
                    FieldSlot.A to CardTypeEnum.Quick,
                )
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.C,
                        CommandCard.Face.B,
                        CommandCard.Face.D,
                        CommandCard.Face.E,
                    )
                else ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                        CommandCard.Face.E,
                    )
            }
        }
    }


    @Test
    fun `MightyOutlier - lineup2 (1SB,5SQ,2SA,3NA,4NA)`() {
        val cards = AttackLineUps.MightyOutlier.lineup2
        for (braveChainEnum in braveChainEnums) {
            val picked = mightyChainHandler.pick(
                cards = cards,
                braveChainEnum = braveChainEnum,
            )?.map { it.card } ?: emptyList()

            when (braveChainEnum) {
                BraveChainEnum.Avoid ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.E,
                        CommandCard.Face.C,
                        CommandCard.Face.B,
                        CommandCard.Face.D,
                    )
                else ->
                    assertThat(picked, braveChainEnum.toString()).containsExactly(
                        CommandCard.Face.A,
                        CommandCard.Face.E,
                        CommandCard.Face.B,
                        CommandCard.Face.C,
                        CommandCard.Face.D,
                    )
            }
        }
    }
}