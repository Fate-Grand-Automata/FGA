package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.modules.attack.MightyChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.AttackPriorityHandler
import io.github.fate_grand_automata.scripts.modules.attack.AvoidChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.Utils
import io.github.fate_grand_automata.scripts.modules.attack.BraveChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.CardChainPriorityHandler
import io.github.fate_grand_automata.scripts.modules.attack.ColorChainHandler
import kotlin.test.BeforeTest
import kotlin.test.Test

class CardChainPriorityIntegrationTest {
    lateinit var attackPriorityHandler: AttackPriorityHandler

    lateinit var braveChainHandler: BraveChainHandler
    lateinit var mightyChainHandler: MightyChainHandler
    lateinit var colorChainHandler: ColorChainHandler
    lateinit var avoidChainHandler: AvoidChainHandler

    @BeforeTest
    fun init() {
        val utils = Utils()
        braveChainHandler = BraveChainHandler(utils)
        mightyChainHandler = MightyChainHandler(utils)
        colorChainHandler = ColorChainHandler(utils)
        avoidChainHandler = AvoidChainHandler(utils)

        attackPriorityHandler = AttackPriorityHandler(
            braveChainHandler = braveChainHandler,
            utils = utils,
            cardChainPriorityHandler = CardChainPriorityHandler(
                mightyChainHandler = mightyChainHandler,
                colorChainHandler = colorChainHandler,
                avoidChainHandler = avoidChainHandler,
                utils = utils,
            ),
        )
    }

    fun getDefaultBraveChainResult (
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        rearrange: Boolean = false,
        npUsage: NPUsage = NPUsage.none,
    ): List<ParsedCard> {
        val results = braveChainHandler.pick(
            cards = cards,
            braveChainEnum = braveChainEnum,
            npUsage = npUsage,
        ) ?: cards
        return attackPriorityHandler.rearrange(
            cards = results,
            rearrange = rearrange,
            npUsage = npUsage,
        )
    }

    fun getDefaultMightyChainResult (
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        rearrange: Boolean = false,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
    ): List<ParsedCard> {
        val results = mightyChainHandler.pick(
            cards = cards,
            npUsage = npUsage,
            npTypes = npTypes,
            braveChainEnum = braveChainEnum,
            forceBraveChain = braveChainEnum == BraveChainEnum.Always,
        ) ?: cards
        return attackPriorityHandler.rearrange(
            cards = results,
            rearrange = rearrange,
            npUsage = npUsage,
        )
    }

    fun getDefaultColorChainResult (
        cardType: CardTypeEnum,
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        rearrange: Boolean = false,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
    ): List<ParsedCard> {
        val results = colorChainHandler.pick(
            cardType = cardType,
            cards = cards,
            npUsage = npUsage,
            npTypes = npTypes,
            braveChainEnum = braveChainEnum,
            forceBraveChain = braveChainEnum == BraveChainEnum.Always,
        ) ?: cards
        return attackPriorityHandler.rearrange(
            cards = results,
            rearrange = rearrange,
            npUsage = npUsage,
        )
    }

    fun getDefaultBusterChainResult (
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        rearrange: Boolean = false,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
    ): List<ParsedCard> {
        return getDefaultColorChainResult(
            cardType = CardTypeEnum.Buster,
            cards = cards,
            npUsage = npUsage,
            npTypes = npTypes,
            braveChainEnum = braveChainEnum,
            rearrange = rearrange,
        )
    }

    fun getDefaultArtsChainResult (
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        rearrange: Boolean = false,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
    ): List<ParsedCard> {
        return getDefaultColorChainResult(
            cardType = CardTypeEnum.Arts,
            cards = cards,
            npUsage = npUsage,
            npTypes = npTypes,
            braveChainEnum = braveChainEnum,
            rearrange = rearrange,
        )
    }

    fun getDefaultQuickChainResult (
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        rearrange: Boolean = false,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
    ): List<ParsedCard> {
        return getDefaultColorChainResult(
            cardType = CardTypeEnum.Quick,
            cards = cards,
            npUsage = npUsage,
            npTypes = npTypes,
            braveChainEnum = braveChainEnum,
            rearrange = rearrange,
        )
    }

    fun getDefaultAvoidChainResult (
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        rearrange: Boolean = false,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
    ): List<ParsedCard> {
        val results = avoidChainHandler.pick(
            cards = cards,
            npUsage = npUsage,
            npTypes = npTypes,
            avoidBraveChains = braveChainEnum == BraveChainEnum.Avoid,
            avoidCardChains = true,
        ) ?: cards
        return attackPriorityHandler.rearrange(
            cards = results,
            rearrange = rearrange,
            npUsage = npUsage,
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        // Expect SB,KQ,NA,NA,SQ - 12345 - ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ), with rearrange`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA)`() {
        val cards = AttackLineUps.Standard.lineup2
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        // Expect 1SB,2KQ,3NA,5SQ,4NA - 15324 - AECBD
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA), with rearrange`() {
        val cards = AttackLineUps.Standard.lineup2
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        ).map { it.card }
        val pickedDefault = getDefaultBraveChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        ).map { it.card }

        // Unable to Brave Chain with Kama. Will ignore and return result of withNp
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with rearrange`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            rearrange = true
        ).map { it.card }
        val pickedDefault = getDefaultBraveChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 1 and 2 because of NP
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            )
        ).map { it.card }

        // Expect 1SB,3NA,2KQ,4NA,5SQ - 13245 - ACBDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 1Kama-NP, with rearrange & npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick
            ),
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 1 and 2 because of NP
        assertThat(picked).containsExactly(CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
        ).map { it.card }
        val pickedDefault = getDefaultBraveChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
        ).map { it.card }

        // No mighty chain. Expect NP Brave Chain output
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with rearrange`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            rearrange = true
        ).map { it.card }
        val pickedDefault = getDefaultBraveChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            rearrange = true
        ).map { it.card }

        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Quick
            )
        ).map { it.card }
        val pickedDefault = getDefaultBraveChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
        ).map { it.card }

        // No mighty chain. Expect NP Brave Chain output
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes, raw`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Quick
            )
        ).map { it.card }

        // No mighty chain. Expect NP Brave Chain output
        // Expect 1SB,5SQ,2KQ,3NA,4NA - 15234 - AEBCD
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes & rearrange`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Quick
            ),
            rearrange = true
        ).map { it.card }
        val pickedDefault = getDefaultBraveChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2Scathach-NP, with npTypes & rearrange, raw`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Quick
            ),
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0)
        ).map { it.card }

        // Attempt to Brave chain with Nero NP
        // Expect 3NA,4NA,1SB,2KQ,5SQ - 34125 - CDABE
        assertThat(picked).containsExactly(CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with rearranged`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 1 and 2 because of NP
        assertThat(picked).containsExactly(CommandCard.Face.D, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.C to CardTypeEnum.Arts
            )
        ).map { it.card }

        // Expect 3NA,4NA,1SB,2KQ,5SQ - 34125 - CDABE
        assertThat(picked).containsExactly(CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with rearrange & npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.C to CardTypeEnum.Arts
            ),
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 1 and 2 because of NP
        assertThat(picked).containsExactly(CommandCard.Face.D, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E)
    }

    @Test
    fun `SingleServantOnly - lineup1 (BQABQ)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        // Expect same result as input
        // Expect BQABQ / 12345 / ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `SingleServantOnly - lineup1 (BQABQ), with rearranged`() {
        val cards = AttackLineUps.SingleServantOnly.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `SingleServantOnly - lineup2 (BBQQA)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup2
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        // Expect 1SB,5SQ,3NA,4NA,2KQ / 15342 / AECDB
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.B)
    }

    @Test
    fun `SingleServantOnly - lineup2 (BBQQA), with rearranged`() {
        val cards = AttackLineUps.SingleServantOnly.lineup2
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.E, CommandCard.Face.D, CommandCard.Face.B)
    }

    @Test
    fun `SingleServantOnly - lineup3 (QQABB)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup3
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        // Expect QABQB / 53124 / ECABD
        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun `SingleServantOnly - lineup3 (QQABB), with rearranged`() {
        val cards = AttackLineUps.SingleServantOnly.lineup3
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup4 (1B,2Q,3Q,4B,5Q)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup4
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }
        val pickedDefault = getDefaultQuickChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        assertThat(picked).isEqualTo(pickedDefault)
    }

    // Scenario for when 2 card types are found but not the 3rd
    @Test
    fun `SingleServantOnly - lineup5 (5Q,2Q,3Q,1B,4B)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup5
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }
        val pickedDefault = getDefaultQuickChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        assertThat(picked).isEqualTo(pickedDefault)
    }

    /**
     * Two NP scenario
     */
    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (2Kama, 3Nero) - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B, CommandCard.NP.C), 0)
        ).map { it.card }
        val pickedDefault = getDefaultBraveChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B, CommandCard.NP.C), 0)
        ).map { it.card }

        // Should fall back to default behaviour
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 3Nero), with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.C to CardTypeEnum.Arts
            )
        ).map { it.card }

        // Expect SB,KQ,NA,NA,SQ - 12345 - ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach) - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
        ).map { it.card }
        val pickedDefault = getDefaultBraveChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0)
        ).map { it.card }

        // Should fall back to default behaviour
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach), with npTypes - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick
            )
        ).map { it.card }

        // Fall back to Quick chain
        val pickedDefault = getDefaultQuickChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick
            )
        ).map { it.card }

        // Should fall back to default behaviour
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3 NP, with npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = attackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick,
                FieldSlot.C to CardTypeEnum.Arts,
            )
        ).map { it.card }
        val pickedDefault = getDefaultBraveChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0)
        ).map { it.card }

        // Should fall back to default behaviour
        assertThat(picked).isEqualTo(pickedDefault)
    }
}