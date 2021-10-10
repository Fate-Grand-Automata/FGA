package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import java.util.*

class Card(fgAutomataApi: IFgoAutomataApi) : IFgoAutomataApi by fgAutomataApi {
    private lateinit var autoSkill: AutoSkill
    private lateinit var battle: Battle

    private val cardPriority: CardPriorityPerWave by lazy { prefs.selectedBattleConfig.cardPriority }
    private val servantPriority: ServantPriorityPerWave? by lazy {
        if (prefs.selectedBattleConfig.useServantPriority)
            prefs.selectedBattleConfig.servantPriority
        else null
    }
    private var commandCards = emptyMap<CardScore, List<CommandCard.Face>>()

    fun init(AutoSkillModule: AutoSkill, BattleModule: Battle) {
        autoSkill = AutoSkillModule
        battle = BattleModule
    }

    private fun CommandCard.Face.affinity(): CardAffinityEnum {
        val region = game.affinityRegion(this)

        if (images[Images.Weak] in region) {
            return CardAffinityEnum.Weak
        }

        if (images[Images.Resist] in region) {
            return CardAffinityEnum.Resist
        }

        return CardAffinityEnum.Normal
    }

    private fun CommandCard.Face.isStunned(): Boolean {
        val stunRegion = game.typeRegion(this).copy(
            y = 930,
            width = 248,
            height = 188
        )

        return images[Images.Stun] in stunRegion
    }

    private fun CommandCard.Face.type(): CardTypeEnum {
        val region = game.typeRegion(this)

        if (images[Images.Buster] in region) {
            return CardTypeEnum.Buster
        }

        if (images[Images.Arts] in region) {
            return CardTypeEnum.Arts
        }

        if (images[Images.Quick] in region) {
            return CardTypeEnum.Quick
        }

        return CardTypeEnum.Unknown
    }

    private var faceCardsGroupedByServant: Map<ServantTracker.TeamSlot, List<CommandCard.Face>> = emptyMap()
    private var commandCardGroups: List<List<CommandCard.Face>> = emptyList()
    private var commandCardGroupedWithNp: Map<CommandCard.NP, List<CommandCard.Face>> = emptyMap()
    var atk: AutoSkillAction.Atk = AutoSkillAction.Atk.noOp()
    private var braveChainsThisTurn = BraveChainEnum.None
    private var rearrangeCardsThisTurn = false

    private fun getCommandCards(): Map<CardScore, List<CommandCard.Face>> {
        data class CardResult(
            val card: CommandCard.Face,
            val isStunned: Boolean,
            val type: CardTypeEnum,
            val affinity: CardAffinityEnum
        )

        val cards = CommandCard.Face.list
            .map {
                val stunned = it.isStunned()
                val type = if (stunned)
                    CardTypeEnum.Unknown
                else it.type()
                val affinity = if (type == CardTypeEnum.Unknown)
                    CardAffinityEnum.Normal // Couldn't detect card type, so don't care about affinity
                else it.affinity()

                CardResult(it, stunned, type, affinity)
            }

        val failedToDetermine = cards
            .filter { !it.isStunned && it.type == CardTypeEnum.Unknown }
            .map { it.card }

        if (failedToDetermine.isNotEmpty()) {
            messages.notify(
                ScriptNotify.FailedToDetermineCards(failedToDetermine)
            )
        }

        return cards
            .groupBy { CardScore(it.type, it.affinity) }
            .mapValues { (_, value) ->
                value.map { it.card }
            }
    }

    private fun <T> List<T>.inCurrentWave(default: T) =
        if (isNotEmpty())
            this[battle.state.stage.coerceIn(indices)]
        else default

    fun readCommandCards() {
        braveChainsThisTurn = prefs
            .selectedBattleConfig
            .braveChains
            .inCurrentWave(BraveChainEnum.None)

        rearrangeCardsThisTurn = prefs
            .selectedBattleConfig
            .rearrangeCards
            .inCurrentWave(false)

        useSameSnapIn {
            commandCards = getCommandCards()

            faceCardsGroupedByServant = battle.servantTracker.faceCardsGroupedByServant()

            commandCardGroups = faceCardsGroupedByServant.values.toList()
            commandCardGroupedWithNp =
                CommandCard.NP.list
                    .associateWith { np ->
                        val slot = when (np) {
                            CommandCard.NP.A -> ServantSlot.A
                            CommandCard.NP.B -> ServantSlot.B
                            CommandCard.NP.C -> ServantSlot.C
                        }

                        val teamSlot = battle.servantTracker.deployed[slot]

                        if (teamSlot == null)
                            listOf()
                        else faceCardsGroupedByServant[teamSlot] ?: listOf()
                    }
        }
    }

    private val spamNps: Set<CommandCard.NP> get() =
        (ServantSlot.list.zip(CommandCard.NP.list))
            .mapNotNull { (servantSlot, np) ->
                val teamSlot = battle.servantTracker.deployed[servantSlot] ?: return@mapNotNull null
                val npSpamConfig = battle.spamConfig
                    .getOrElse(teamSlot.position - 1) { ServantSpamConfig() }
                    .np

                if (autoSkill.canSpam(npSpamConfig.spam) && (battle.state.stage + 1) in npSpamConfig.waves)
                    np
                else null
            }
            .toSet()

    private fun CommandCard.NP.pick() {
        game.clickLocation(this).click()

        game.battleExtraInfoWindowCloseClick.click()
    }

    private fun cardsOrderedByPriority(): List<CommandCard.Face> {
        fun applyPriority(cards: Map<CardScore, List<CommandCard.Face>>) =
            cardPriority
                .atWave(battle.state.stage)
                .mapNotNull { cards[it] }
                .flatten()

        servantPriority?.let { servantPriority ->
            return servantPriority
                .atWave(battle.state.stage)
                .mapNotNull { faceCardsGroupedByServant[it] }
                .map { cards ->
                    val cardsGroupedByScore = cards
                        .groupBy { card ->
                            commandCards
                                .filterValues { it.contains(card) }
                                .map { (score, _) -> score }
                                .first()
                        }
                        .filterKeys { it.CardType != CardTypeEnum.Unknown } // Stunned cards at the end

                    applyPriority(cardsGroupedByScore)
                }
                .plus(
                    // Stunned cards
                    commandCards
                        .filterKeys { it.CardType == CardTypeEnum.Unknown }
                        .map { it.value }
                )
                .flatten()
        }

        return applyPriority(commandCards)
    }

    private fun pickCards(clicks: Int = 3): List<CommandCard.Face> {
        var clicksLeft = clicks.coerceAtLeast(0)
        val toClick = mutableListOf<CommandCard.Face>()
        val remainingCards = CommandCard.Face.list.toMutableSet()

        val cardsOrderedByPriority = cardsOrderedByPriority()

        fun addToClickList(vararg cards: CommandCard.Face) = cards.apply {
            toClick.addAll(cards)
            remainingCards.removeAll(cards)
            clicksLeft -= cards.size
        }

        fun pickCardsOrderedByPriority(
            clicks: Int = clicksLeft,
            filter: (CommandCard.Face) -> Boolean = { true }
        ) =
            cardsOrderedByPriority
                .asSequence()
                .filter { it in remainingCards && filter(it) }
                .take(clicks)
                .toList()
                .let { addToClickList(*(it.toTypedArray())) }

        return when (braveChainsThisTurn) {
            BraveChainEnum.WithNP -> {
                val chainFaceCount = commandCardGroupedWithNp[atk.nps.firstOrNull()]?.let { npGroup ->
                    pickCardsOrderedByPriority {
                        it in npGroup
                    }.size
                }

                // Pick more cards if needed
                pickCardsOrderedByPriority()

                // When there is 1 NP, 1 Card before NP, only 1 matching face-card,
                // we want the matching face-card after NP.
                if (rearrangeCardsThisTurn
                    && listOf(atk.nps.size, atk.cardsBeforeNP, chainFaceCount).all { it == 1 }
                ) {
                    Collections.swap(toClick, 0, 1)
                }

                rearrange(toClick)
            }
            BraveChainEnum.Avoid -> {
                if (commandCardGroups.size > 1
                    && remainingCards.isNotEmpty()
                    && clicksLeft > 1
                ) {
                    if (rearrangeCardsThisTurn) {
                        // Top 3 priority cards grouped by servant
                        val topGrouped = cardsOrderedByPriority
                            .take(clicksLeft)
                            .groupBy { commandCardGroups.indexOfFirst { group -> it in group } }
                            .map { it.value }

                        when (topGrouped.size) {
                            // All 3 cards of same servant
                            1 -> {
                                val group = commandCardGroups.first { topGrouped[0][0] in it }

                                // Check if there's another servant
                                val otherCard = cardsOrderedByPriority
                                    .firstOrNull { it !in group }

                                // If there's no other servant, this will fallback to default card picker
                                if (otherCard != null) {
                                    addToClickList(
                                        topGrouped[0][0],
                                        otherCard,
                                        topGrouped[0][1]
                                    )
                                }
                            }
                            // Ideal case. 2 servant cards
                            2 -> {
                                // servant with 2 cards in first place
                                val topSorted = topGrouped
                                    .sortedByDescending { it.size }

                                addToClickList(
                                    topSorted[0][0],
                                    topSorted[1][0],
                                    topSorted[0][1]
                                )
                            }
                            // Brave chain will already be avoided, but we can rearrange to optimize
                            3 -> {
                                addToClickList(
                                    topGrouped[0][0],
                                    topGrouped[2][0],
                                    topGrouped[1][0]
                                )
                            }
                        }
                    } else {
                        var lastGroup = emptyList<CommandCard.Face>()

                        do {
                            lastGroup = pickCardsOrderedByPriority(1) { it !in lastGroup }
                                .map { m -> commandCardGroups.firstOrNull { m in it } }
                                .firstOrNull() ?: emptyList()
                        } while (clicksLeft > 0 && lastGroup.isNotEmpty())
                    }
                }

                // Pick more cards if needed
                pickCardsOrderedByPriority()

                toClick
            }
            BraveChainEnum.None -> {
                pickCardsOrderedByPriority()

                rearrange(toClick)
            }
        }
    }

    private fun rearrange(cards: List<CommandCard.Face>): List<CommandCard.Face> {
        if (rearrangeCardsThisTurn
            // If there are cards before NP, at max there's only 1 card after NP
            && atk.cardsBeforeNP == 0
            // If there are more than 1 NPs, only 1 card after NPs at max
            && atk.nps.size <= 1
        ) {
            val cardsToRearrange = cards
                .mapIndexed { index, _ -> index }
                .take((3 - atk.nps.size).coerceAtLeast(0))
                .reversed()

            // When clicking 3 cards, move the card with 2nd highest priority to last position to amplify its effect
            // Do the same when clicking 2 cards unless they're used before NPs.
            if (cardsToRearrange.size in 2..3) {
                messages.log(ScriptLog.RearrangingCards)

                return cards.toMutableList().also {
                    Collections.swap(it, cardsToRearrange[1], cardsToRearrange[0])
                }
            }
        }

        return cards
    }

    private fun shouldShuffle(): Boolean {
        // Not this wave
        if (battle.state.stage != (prefs.selectedBattleConfig.shuffleCardsWave - 1)) {
            return false
        }

        // Already shuffled
        if (battle.state.shuffled) {
            return false
        }

        return when (prefs.selectedBattleConfig.shuffleCards) {
            ShuffleCardsEnum.None -> false
            ShuffleCardsEnum.NoEffective -> {
                val effectiveCardCount = commandCards
                    .filterKeys { it.CardAffinity == CardAffinityEnum.Weak }
                    .map { it.value.size }
                    .sum()

                effectiveCardCount == 0
            }
            ShuffleCardsEnum.NoNPMatching -> {
                if (atk.nps.isEmpty()) {
                    false
                } else {
                    val matchingCount = atk.nps
                        .mapNotNull { commandCardGroupedWithNp[it]?.size }
                        .sum()

                    matchingCount == 0
                }
            }
        }
    }

    private fun shuffleCards() {
        if (shouldShuffle()) {
            game.battleBack.click()

            autoSkill.castMasterSkill(Skill.Master.list[2])

            battle.state.hasClickedAttack = false

            battle.clickAttack()

            battle.state.shuffled = true
        }
    }

    fun clickCommandCards() {
        shuffleCards()

        val cards = pickCards()

        if (atk.cardsBeforeNP > 0) {
            cards
                .take(atk.cardsBeforeNP)
                .also { messages.log(ScriptLog.ClickingCards(it)) }
                .forEach { game.clickLocation(it).click() }
        }

        val nps = atk.nps + spamNps

        if (nps.isNotEmpty()) {
            nps
                .also { messages.log(ScriptLog.ClickingNPs(it)) }
                .forEach { it.pick() }
        }

        cards
            .drop(atk.cardsBeforeNP)
            .also { messages.log(ScriptLog.ClickingCards(it)) }
            .forEach { game.clickLocation(it).click() }

        atk = AutoSkillAction.Atk.noOp()
    }
}