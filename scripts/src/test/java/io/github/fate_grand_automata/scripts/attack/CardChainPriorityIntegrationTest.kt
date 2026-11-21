package io.github.fate_grand_automata.scripts.attack

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.AttackPriorityEnum
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.ServantPriorityPerWave
import io.github.fate_grand_automata.scripts.modules.FaceCardPriority
import io.github.fate_grand_automata.scripts.modules.attack.MightyChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.AttackPriorityHandler
import io.github.fate_grand_automata.scripts.modules.attack.AvoidChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.BraveChainHandler
import io.github.fate_grand_automata.scripts.modules.attack.ColorChainHandler
import kotlin.test.Test

class CardChainPriorityIntegrationTest {
    fun getDefaultBraveChainResult (
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        rearrange: Boolean = false,
        npUsage: NPUsage = NPUsage.none,
    ): List<ParsedCard> {
        val results = BraveChainHandler.pick(
            cards = cards,
            braveChainEnum = braveChainEnum,
            npUsage = npUsage,
        ) ?: cards
        return AttackPriorityHandler.rearrange(
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
        val results = MightyChainHandler.pick(
            cards = cards,
            npUsage = npUsage,
            npTypes = npTypes,
            braveChainEnum = braveChainEnum,
        ) ?: cards
        return AttackPriorityHandler.rearrange(
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
        val results = ColorChainHandler.pick(
            cardType = cardType,
            cards = cards,
            npUsage = npUsage,
            npTypes = npTypes,
            braveChainEnum = braveChainEnum,
        ) ?: cards
        return AttackPriorityHandler.rearrange(
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
        val results = AvoidChainHandler.pick(
            cards = cards,
            braveChainEnum = braveChainEnum,
            npUsage = npUsage,
            npTypes = npTypes,
            avoidCardChains = true,
        ) ?: cards
        return AttackPriorityHandler.rearrange(
            cards = results,
            rearrange = rearrange,
            npUsage = npUsage,
        )
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ)`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        // Expect SB,KQ,NA,NA,SQ - 12345 - ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ), No Mighty allowed`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            chainPriority = ChainTypeEnum.entries.filter { it != ChainTypeEnum.Mighty },
        ).map { it.card }
        val pickedDefault = getDefaultAvoidChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ), No Mighty allowed, raw`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            chainPriority = ChainTypeEnum.entries.filter { it != ChainTypeEnum.Mighty },
        ).map { it.card }

        // Expect avoid SB,KQ,SQ,NA,NA - 12534 - ABECD
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ), No Mighty allowed, end before Avoid`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            chainPriority = ChainTypeEnum.defaultOrder.filter { it != ChainTypeEnum.Mighty },
        ).map { it.card }

        assertThat(picked).isEqualTo(cards.map{ it.card })
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ), with rearrange`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        // Expect 1SB,2KQ,3NA,5SQ,4NA - 15324 - AECBD
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun `Standard - lineup2 (1SB,5SQ,2KQ,3NA,4NA), with rearrange`() {
        val cards = AttackLineUps.Standard.lineup2
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with npTypes using default chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.C to CardTypeEnum.Arts
            ),
        ).map { it.card }
        val pickedDefault = getDefaultBraveChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0),
        ).map { it.card }

        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 3Nero-NP, with rearrange & npTypes`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        // Expect 1SB,5SQ,3NA,4NA,2KQ / 15342 / AECDB
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.B)
    }

    @Test
    fun `SingleServantOnly - lineup2 (BBQQA), with rearranged`() {
        val cards = AttackLineUps.SingleServantOnly.lineup2
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            rearrange = true
        ).map { it.card }

        // Same as above but swap position 2 and 3)
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.E, CommandCard.Face.D, CommandCard.Face.B)
    }

    @Test
    fun `SingleServantOnly - lineup3 (5Q,2Q,3A,1B,4B)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup3
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
        ).map { it.card }

        // Expect QABQB / 53124 / ECABD
        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun `SingleServantOnly - lineup3 (5Q,2Q,3A,1B,4B) - Buster priority`() {
        val cards = AttackLineUps.SingleServantOnly.lineup3
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            chainPriority = listOf(ChainTypeEnum.Buster, ChainTypeEnum.Mighty),
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Buster
            ),
        ).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.D,
            CommandCard.Face.E,
            CommandCard.Face.B,
            CommandCard.Face.C,
        )
    }

    @Test
    fun `SingleServantOnly - lineup3 (5Q,2Q,3A,1B,4B), with rearranged`() {
        val cards = AttackLineUps.SingleServantOnly.lineup3
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.C), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.C to CardTypeEnum.Arts
            )
        ).map { it.card }

        // Fall back to Mighty chain
        val pickedDefault = getDefaultMightyChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
                FieldSlot.B to CardTypeEnum.Quick
            )
        ).map { it.card }

        // Expect SB,KQ,NA,NA,SQ - 12345 - ABCDE
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
        // Default behaviour check
        assertThat(picked).isEqualTo(pickedDefault)
    }

    @Test
    fun `Standard - lineup1 (1SB,2KQ,3NA,4NA,5SQ) + 2 NP (1Kama, 2Scathach) - No mighty chain`() {
        val cards = AttackLineUps.Standard.lineup1
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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
        val picked = AttackPriorityHandler.pick(
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

    // Test case for issue found with BB Worm farming map
    // Scenario: All cards are BB, all are weak against enemy.
    // Expected to be a Mighty Chain of A/NP/B, but becomes B/NP/A instead.
    @Test
    fun `SingleServantOnly - lineup6 (3A,5A,1B,4B,2Q)`() {
        val cards = AttackLineUps.SingleServantOnly.lineup6_special
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 1),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
            )
        ).map { it.card }

        // Uses Mighty Chain
        val pickedDefault = getDefaultMightyChainResult(
            cards = cards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 1),
            npTypes = mapOf(
                FieldSlot.A to CardTypeEnum.Quick,
            )
        ).map { it.card }

        assertThat(picked).isEqualTo(pickedDefault)
        assertThat(picked).containsExactly(
            CommandCard.Face.C,
            CommandCard.Face.A,
            CommandCard.Face.E,
            CommandCard.Face.D,
            CommandCard.Face.B,
        )
    }

    @Test
    fun `MightyOutlier - lineup1 with a stunned card still makes a Mighty Chain`() {
        // 1WB, 2WA, 3RA, 4 stunned (Unknown), 5WQ: the stunned card must not block the Mighty Chain
        val cards = listOf(
            AttackLineUps.MightyOutlier.scathach1WB,
            AttackLineUps.MightyOutlier.scathach2WA,
            AttackLineUps.MightyOutlier.nero3RA,
            AttackLineUps.Unknown.nero4Unknown.copy(isStunned = true),
            AttackLineUps.MightyOutlier.scathach5WQ,
        )
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            braveChainEnum = BraveChainEnum.None,
        ).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.B,
            CommandCard.Face.E,
            CommandCard.Face.C,
            CommandCard.Face.D,
        )
    }

    @Test
    fun `Too few readable colors leave unreadable cards ahead of stunned cards`() {
        val (first, second, third, fourth, fifth) = AttackLineUps.MightyOutlier.lineup1
        val cards = listOf(
            first,
            second.copy(type = CardTypeEnum.Unknown, isStunned = true),
            third,
            fourth.copy(type = CardTypeEnum.Unknown, isStunned = true),
            fifth.copy(type = CardTypeEnum.Unknown),
        )

        val picked = AttackPriorityHandler.pick(cards).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.D,
        )
    }

    @Test
    fun `Rearranging a fallback cannot move a stunned card ahead of an unreadable card`() {
        val (first, second, third, fourth, fifth) = AttackLineUps.MightyOutlier.lineup1
        val cards = listOf(
            first,
            second.copy(type = CardTypeEnum.Unknown, isStunned = true),
            third.copy(type = CardTypeEnum.Unknown, isStunned = true),
            fourth.copy(type = CardTypeEnum.Unknown, isStunned = true),
            fifth.copy(type = CardTypeEnum.Unknown),
        )

        val picked = AttackPriorityHandler.pick(cards, rearrange = true).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D,
        )
    }

    @Test
    fun `Rearranging keeps both readable faces available after an undetected spam NP`() {
        val (first, second, third, fourth, fifth) = AttackLineUps.MightyOutlier.lineup1
        val cards = listOf(
            first,
            second,
            third.copy(type = CardTypeEnum.Unknown, isStunned = true),
            fourth.copy(type = CardTypeEnum.Unknown),
            fifth.copy(type = CardTypeEnum.Unknown, isStunned = true),
        )

        // The clicker still attempts the spam NP even though the planner sees no NPs.
        val picked = AttackPriorityHandler.pick(cards, rearrange = true).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.C, CommandCard.Face.E,
        )
    }

    @Test
    fun `Chain Priority recovers omitted faces and uses repeated scores only once`() {
        val (first, second, third, fourth, fifth) = AttackLineUps.MightyOutlier.lineup1
        val cards = listOf(
            first,
            second,
            third.copy(type = CardTypeEnum.Quick, affinity = CardAffinityEnum.Weak),
            fourth.copy(type = CardTypeEnum.Unknown, affinity = CardAffinityEnum.Resist),
            fifth.copy(type = CardTypeEnum.Unknown, affinity = CardAffinityEnum.Normal, isStunned = true),
        )
        val priority = FaceCardPriority(CardPriorityPerWave.of("WB, WA, WA, B, A, Q, RB, RA, RQ"), ServantPriorityPerWave.default)
        val ranked = priority.sort(cards, stage = 0, useServantPriority = false)
        assertThat(ranked.map { it.card }).containsExactly(
            CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.B, CommandCard.Face.E,
        )

        val picked = AttackPriorityHandler.pick(cards, priorityOrder = ranked).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E,
        )
    }

    @Test
    fun `Always finds Kiyohime's Brave Chain when only Nero's card colors are readable`() {
        val cards = AttackLineUps.Unknown.twoReadableNeroCardsAndThreeUnreadableKiyohimeCards
        val picked = AttackPriorityHandler.pick(cards, braveChainEnum = BraveChainEnum.Always).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E,
            CommandCard.Face.C, CommandCard.Face.D,
        )
    }

    @Test
    fun `Always keeps priority order when no Servant has three cards with known slots`() {
        val hand = AttackLineUps.Unknown.twoReadableNeroCardsAndThreeUnreadableKiyohimeCards
        val cards = hand.dropLast(1) + hand.last().copy(fieldSlot = null)
        val picked = AttackPriorityHandler.pick(cards, braveChainEnum = BraveChainEnum.Always).map { it.card }

        assertThat(picked).isEqualTo(cards.map { it.card })
    }

    @Test
    fun `Always excludes stunned cards from the Brave Chain and places them last`() {
        val hand = AttackLineUps.Unknown.twoReadableNeroCardsAndThreeUnreadableKiyohimeCards
        val cards = hand.take(2) + hand[2].copy(isStunned = true) + hand.drop(3)
        val picked = AttackPriorityHandler.pick(cards, braveChainEnum = BraveChainEnum.Always).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.B,
            CommandCard.Face.E, CommandCard.Face.A,
        )
    }

    @Test
    fun `A readable color chain cannot override a required Brave Chain using unreadable cards`() {
        val hand = AttackLineUps.Unknown.twoReadableNeroCardsAndThreeUnreadableKiyohimeCards
        val cards = listOf(
            AttackLineUps.BusterFocus.nero3BAlt,
            AttackLineUps.BusterFocus.nero4BAlt,
            AttackLineUps.BusterFocus.kiyohime1B,
        ) + hand.takeLast(2)
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            chainPriority = listOf(ChainTypeEnum.Buster),
            braveChainEnum = BraveChainEnum.Always,
        ).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E,
            CommandCard.Face.C, CommandCard.Face.D,
        )
    }

    @Test
    fun `An NP Brave Chain keeps a third matching face card if the NP fails`() {
        val lineup = AttackLineUps.BusterFocus
        val cards = listOf(lineup.nero3RA, lineup.nero4RA, lineup.kiyohime1B, lineup.kiyohime2B, lineup.kiyohime5B)

        // An unreadable NP uses the early fallback. A readable Arts NP reaches the chain planner,
        // where only Nero's face cards are Arts, so the required Brave Chain takes precedence.
        for (npType in listOf(CardTypeEnum.Unknown, CardTypeEnum.Arts)) {
            for (braveChain in listOf(BraveChainEnum.WithNP, BraveChainEnum.Always)) {
                val picked = AttackPriorityHandler.pick(
                    cards = cards,
                    chainPriority = listOf(ChainTypeEnum.Arts),
                    braveChainEnum = braveChain,
                    npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                    npTypes = mapOf(FieldSlot.B to npType),
                    rearrange = false,
                ).map { it.card }

                assertThat(picked, "$braveChain, $npType NP").containsExactly(
                    CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E,
                    CommandCard.Face.C, CommandCard.Face.D,
                )
            }
        }
    }

    @Test
    fun `With NP uses the slot of an unknown-type NP and rearranges its two face cards`() {
        val picked = AttackPriorityHandler.pick(
            cards = AttackLineUps.Unknown.twoReadableNeroCardsAndThreeUnreadableKiyohimeCards,
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 1),
            npTypes = mapOf(FieldSlot.B to CardTypeEnum.Unknown),
            rearrange = true,
        ).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.B, CommandCard.Face.A, CommandCard.Face.E,
            CommandCard.Face.C, CommandCard.Face.D,
        )
    }

    @Test
    fun `A Mighty Brave Chain keeps Scathach's third face card if her Quick NP fails`() {
        val picked = AttackPriorityHandler.pick(
            cards = AttackLineUps.MightyOutlier.lineup1,
            chainPriority = listOf(ChainTypeEnum.Mighty),
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(FieldSlot.B to CardTypeEnum.Quick),
        ).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E,
            CommandCard.Face.C, CommandCard.Face.D,
        )
    }

    @Test
    fun `A Buster NP Brave Chain can use a different or unreadable color as backup`() {
        val hand = AttackLineUps.BusterFocus.lineup01
        for (backupType in listOf(CardTypeEnum.Arts, CardTypeEnum.Unknown)) {
            val picked = AttackPriorityHandler.pick(
                cards = hand.dropLast(1) + hand.last().copy(type = backupType),
                chainPriority = listOf(ChainTypeEnum.Buster),
                braveChainEnum = BraveChainEnum.Always,
                npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
                npTypes = mapOf(FieldSlot.B to CardTypeEnum.Buster),
            ).map { it.card }

            assertThat(picked, "$backupType backup").containsExactly(
                CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E,
                CommandCard.Face.C, CommandCard.Face.D,
            )
        }
    }

    @Test
    fun `A Mighty NP Brave Chain keeps its original order when the only spare is stunned`() {
        val hand = AttackLineUps.MightyOutlier.lineup1
        val cards = hand.dropLast(1) + hand.last().copy(isStunned = true)
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            chainPriority = listOf(ChainTypeEnum.Mighty),
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(FieldSlot.B to CardTypeEnum.Quick),
        )

        assertThat(picked).isEqualTo(cards)
    }

    @Test
    fun `Rearrange keeps the third face card for a Mighty NP Brave Chain without forced Brave priority`() {
        for (cardsBeforeNP in 0..1) {
            val picked = AttackPriorityHandler.pick(
                cards = AttackLineUps.MightyOutlier.lineup1,
                attackPriorityOrder = listOf(AttackPriorityEnum.CardChainPriority, AttackPriorityEnum.BraveChainPriority),
                chainPriority = listOf(ChainTypeEnum.Mighty),
                braveChainEnum = BraveChainEnum.WithNP,
                npUsage = NPUsage(setOf(CommandCard.NP.B), cardsBeforeNP),
                npTypes = mapOf(FieldSlot.B to CardTypeEnum.Quick),
                rearrange = true,
            ).map { it.card }

            assertThat(picked, "$cardsBeforeNP cards before NP").containsExactly(
                CommandCard.Face.B, CommandCard.Face.A, CommandCard.Face.E,
                CommandCard.Face.C, CommandCard.Face.D,
            )
        }
    }

    @Test
    fun `An Avoid card chain plan does not promote an unreadable Brave backup`() {
        val hand = AttackLineUps.MightyOutlier.lineup1
        val cards = hand.dropLast(1) + hand.last().copy(type = CardTypeEnum.Unknown)
        val picked = AttackPriorityHandler.pick(
            cards = cards,
            chainPriority = listOf(ChainTypeEnum.Avoid),
            braveChainEnum = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(FieldSlot.B to CardTypeEnum.Buster),
        )

        assertThat(picked).isEqualTo(cards)
    }

    @Test
    fun `BusterFocus - lineup01 (1KB,2KB,3NA,4NA,5KB) + 2Kiyo-NP of unknown type, Avoid Brave Chains`() {
        // With an unreadable NP color, the fallback can still split the selected face cards across Servants.
        val picked = AttackPriorityHandler.pick(
            cards = AttackLineUps.BusterFocus.lineup01,
            braveChainEnum = BraveChainEnum.Avoid,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0),
            npTypes = mapOf(
                FieldSlot.B to CardTypeEnum.Unknown,
            ),
        ).map { it.card }

        assertThat(picked).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.B,
            CommandCard.Face.D,
            CommandCard.Face.E,
        )
    }
}
