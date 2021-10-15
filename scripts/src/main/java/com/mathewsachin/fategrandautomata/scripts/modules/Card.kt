package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.libautomata.dagger.ScriptScope
import java.util.*
import javax.inject.Inject

@ScriptScope
class Card @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val servantTracker: ServantTracker,
    private val state: BattleState,
    private val battleConfig: IBattleConfig,
    private val spamConfig: SpamConfigPerTeamSlot,
    private val caster: Caster,
    private val parser: CardParser
) : IFgoAutomataApi by fgAutomataApi {
    private val cardPriority: CardPriorityPerWave by lazy { battleConfig.cardPriority }
    private val servantPriority: ServantPriorityPerWave? by lazy {
        if (battleConfig.useServantPriority)
            battleConfig.servantPriority
        else null
    }
    private var commandCards = emptyMap<CardScore, List<CommandCard.Face>>()

    private var faceCardsGroupedByServant: Map<TeamSlot, List<CommandCard.Face>> = emptyMap()
    private var commandCardGroups: List<List<CommandCard.Face>> = emptyList()
    private var commandCardGroupedWithNp: Map<CommandCard.NP, List<CommandCard.Face>> = emptyMap()
    private var braveChainsThisTurn = BraveChainEnum.None
    private var rearrangeCardsThisTurn = false

    private fun <T> List<T>.inCurrentWave(default: T) =
        if (isNotEmpty())
            this[state.stage.coerceIn(indices)]
        else default

    fun readCommandCards() {
        braveChainsThisTurn = battleConfig
            .braveChains
            .inCurrentWave(BraveChainEnum.None)

        rearrangeCardsThisTurn = battleConfig
            .rearrangeCards
            .inCurrentWave(false)

        useSameSnapIn {
            val parsedCards = parser.parse()

            commandCards = parsedCards.groupBy(
                keySelector = { CardScore(it.type, it.affinity) },
                valueTransform = { it.card }
            )

            faceCardsGroupedByServant = parsedCards.groupBy(
                keySelector = { it.servant },
                valueTransform = { it.card }
            )

            commandCardGroups = faceCardsGroupedByServant.values.toList()
            commandCardGroupedWithNp =
                CommandCard.NP.list
                    .associateWith { np ->
                        val slot = np.toFieldSlot()

                        servantTracker.deployed[slot]
                            ?.let { teamSlot ->
                                faceCardsGroupedByServant[teamSlot]
                            }
                            ?: emptyList()
                    }
        }
    }

    private val spamNps: Set<CommandCard.NP> get() =
        (FieldSlot.list.zip(CommandCard.NP.list))
            .mapNotNull { (servantSlot, np) ->
                val teamSlot = servantTracker.deployed[servantSlot] ?: return@mapNotNull null
                val npSpamConfig = spamConfig[teamSlot].np

                if (caster.canSpam(npSpamConfig.spam) && (state.stage + 1) in npSpamConfig.waves)
                    np
                else null
            }
            .toSet()

    private fun cardsOrderedByPriority(): List<CommandCard.Face> {
        fun applyPriority(cards: Map<CardScore, List<CommandCard.Face>>) =
            cardPriority
                .atWave(state.stage)
                .mapNotNull { cards[it] }
                .flatten()

        servantPriority?.let { servantPriority ->
            return servantPriority
                .atWave(state.stage)
                .mapNotNull { faceCardsGroupedByServant[it] }
                .map { cards ->
                    val cardsGroupedByScore = cards
                        .groupBy { card ->
                            commandCards.entries
                                .first { (_, list) -> list.contains(card) }
                                .key // score
                        }
                        .filterKeys { it.type != CardTypeEnum.Unknown } // Stunned cards at the end

                    applyPriority(cardsGroupedByScore)
                }
                .flatten()
                .let { picked ->
                    // In case less than 3 cards are picked
                    val notPicked = CommandCard.Face.list.filter { it !in picked }
                    if (notPicked.isNotEmpty()) {
                        messages.log(ScriptLog.CardsNotPickedByServantPriority(notPicked))
                    }

                    picked + notPicked
                }
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
                val chainFaceCount = commandCardGroupedWithNp[state.atk.nps.firstOrNull()]?.let { npGroup ->
                    pickCardsOrderedByPriority {
                        it in npGroup
                    }.size
                }

                // Pick more cards if needed
                pickCardsOrderedByPriority()

                // When there is 1 NP, 1 Card before NP, only 1 matching face-card,
                // we want the matching face-card after NP.
                if (rearrangeCardsThisTurn
                    && listOf(state.atk.nps.size, state.atk.cardsBeforeNP, chainFaceCount).all { it == 1 }
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
            && state.atk.cardsBeforeNP == 0
            // If there are more than 1 NPs, only 1 card after NPs at max
            && state.atk.nps.size <= 1
        ) {
            val cardsToRearrange = cards
                .mapIndexed { index, _ -> index }
                .take((3 - state.atk.nps.size).coerceAtLeast(0))
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

    fun shouldShuffle(): Boolean {
        // Not this wave
        if (state.stage != (battleConfig.shuffleCardsWave - 1)) {
            return false
        }

        // Already shuffled
        if (state.shuffled) {
            return false
        }

        return when (battleConfig.shuffleCards) {
            ShuffleCardsEnum.None -> false
            ShuffleCardsEnum.NoEffective -> {
                val effectiveCardCount = commandCards
                    .filterKeys { it.affinity == CardAffinityEnum.Weak }
                    .map { it.value.size }
                    .sum()

                effectiveCardCount == 0
            }
            ShuffleCardsEnum.NoNPMatching -> {
                if (state.atk.nps.isEmpty()) {
                    false
                } else {
                    val matchingCount = state.atk.nps
                        .mapNotNull { commandCardGroupedWithNp[it]?.size }
                        .sum()

                    matchingCount == 0
                }
            }
        }
    }

    fun clickCommandCards() {
        val cards = pickCards()

        if (state.atk.cardsBeforeNP > 0) {
            cards
                .take(state.atk.cardsBeforeNP)
                .also { messages.log(ScriptLog.ClickingCards(it)) }
                .forEach { caster.use(it) }
        }

        val nps = state.atk.nps + spamNps

        if (nps.isNotEmpty()) {
            nps
                .also { messages.log(ScriptLog.ClickingNPs(it)) }
                .forEach { caster.use(it) }
        }

        cards
            .drop(state.atk.cardsBeforeNP)
            .also { messages.log(ScriptLog.ClickingCards(it)) }
            .forEach { caster.use(it) }
    }
}