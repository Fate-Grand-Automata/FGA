package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import timber.log.Timber
import timber.log.debug
import java.util.*

class Card(fgAutomataApi: IFgoAutomataApi) : IFgoAutomataApi by fgAutomataApi {
    private lateinit var autoSkill: AutoSkill
    private lateinit var battle: Battle

    private lateinit var cardPriority: CardPriorityPerWave
    private var commandCards = emptyMap<CardScore, List<CommandCard.Face>>()

    fun init(AutoSkillModule: AutoSkill, BattleModule: Battle) {
        autoSkill = AutoSkillModule
        battle = BattleModule

        cardPriority = CardPriorityPerWave.of(
            prefs.selectedAutoSkillConfig.cardPriority
        )
    }

    private fun CommandCard.Face.affinity(): CardAffinityEnum {
        val region = affinityRegion

        if (images.weak in region) {
            return CardAffinityEnum.Weak
        }

        if (images.resist in region) {
            return CardAffinityEnum.Resist
        }

        return CardAffinityEnum.Normal
    }

    private fun CommandCard.Face.isStunned(): Boolean {
        val stunRegion = typeRegion.copy(
            Y = 930,
            Width = 248,
            Height = 188
        )

        return images.stun in stunRegion
    }

    private fun CommandCard.Face.type(): CardTypeEnum {
        val region = typeRegion

        if (images.buster in region) {
            return CardTypeEnum.Buster
        }

        if (images.art in region) {
            return CardTypeEnum.Arts
        }

        if (images.quick in region) {
            return CardTypeEnum.Quick
        }

        return CardTypeEnum.Unknown
    }

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
            val msg = messages.failedToDetermineCardType(failedToDetermine)
            toast(msg)

            Timber.debug { msg }
        }

        return cards
            .groupBy { CardScore(it.type, it.affinity) }
            .mapValues { (_, value) ->
                value.map { it.card }
            }
    }

    private fun <T> List<T>.inCurrentWave(default: T) =
        if (isNotEmpty())
            this[battle.state.runState.stage.coerceIn(indices)]
        else default

    fun readCommandCards() {
        braveChainsThisTurn = prefs
            .selectedAutoSkillConfig
            .braveChains
            .inCurrentWave(BraveChainEnum.None)

        rearrangeCardsThisTurn = prefs
            .selectedAutoSkillConfig
            .rearrangeCards
            .inCurrentWave(false)

        screenshotManager.useSameSnapIn {
            commandCards = getCommandCards()

            if (braveChainsThisTurn != BraveChainEnum.None) {
                val supportGroup = CommandCard.Face.list
                    .filter { images.support in it.supportCheckRegion }
                commandCardGroups = groupByFaceCard(supportGroup)
                commandCardGroupedWithNp = groupNpsWithFaceCards(commandCardGroups, supportGroup)
            }
        }
    }

    private val canSpamNpCards: Boolean
        get() {
            val weCanSpam = prefs.castNoblePhantasm == BattleNoblePhantasmEnum.Spam
            val weAreInDanger = prefs.castNoblePhantasm == BattleNoblePhantasmEnum.Danger
                    && battle.state.runState.stageState.hasChosenTarget

            return (weCanSpam || weAreInDanger) && autoSkill.isFinished
        }

    private fun spamNpCards() = CommandCard.NP.list.forEach { it.pick() }

    private fun CommandCard.NP.pick() {
        clickLocation.click()

        Game.battleExtraInfoWindowCloseClick.click()
    }

    private fun pickCards(clicks: Int = 3): List<CommandCard.Face> {
        var clicksLeft = clicks.coerceAtLeast(0)
        val toClick = mutableListOf<CommandCard.Face>()
        val remainingCards = CommandCard.Face.list.toMutableSet()

        val cardsOrderedByPriority = cardPriority
            .atWave(battle.state.runState.stage)
            .mapNotNull { commandCards[it] }
            .flatten()

        fun pickCardsOrderedByPriority(
            clicks: Int = clicksLeft,
            filter: (CommandCard.Face) -> Boolean = { true }
        ): List<CommandCard.Face> {
            fun Sequence<CommandCard.Face>.addToClickList(): List<CommandCard.Face> {
                val asList = toList()

                toClick.addAll(asList)
                remainingCards.removeAll(asList)
                clicksLeft -= asList.size

                return asList
            }

            return cardsOrderedByPriority
                .asSequence()
                .filter { it in remainingCards && filter(it) }
                .take(clicks)
                .addToClickList()
        }

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
                    var lastGroup = emptyList<CommandCard.Face>()

                    do {
                        lastGroup = pickCardsOrderedByPriority(1) { it !in lastGroup }
                            .map { m -> commandCardGroups.firstOrNull { m in it } }
                            .firstOrNull() ?: emptyList()
                    } while (clicksLeft > 0 && lastGroup.isNotEmpty())
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
            // Skip if NP spamming because we don't know how many NPs might've been used
            && prefs.castNoblePhantasm == BattleNoblePhantasmEnum.None
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
                Timber.debug { "Rearranging cards" }

                return cards.toMutableList().also {
                    Collections.swap(it, cardsToRearrange[1], cardsToRearrange[0])
                }
            }
        }

        return cards
    }

    fun clickCommandCards() {
        if (canSpamNpCards) {
            spamNpCards()
        }

        val cards = pickCards()

        if (atk.cardsBeforeNP > 0) {
            cards
                .take(atk.cardsBeforeNP)
                .also { Timber.debug { "Clicking cards: $it" } }
                .forEach { it.clickLocation.click() }
        }

        if (atk.nps.isNotEmpty()) {
            atk.nps
                .also { Timber.debug { "Clicking NP(s): $it" } }
                .forEach { it.pick() }
        }

        cards
            .drop(atk.cardsBeforeNP)
            .also { Timber.debug { "Clicking cards: $it" } }
            .forEach { it.clickLocation.click() }

        atk = AutoSkillAction.Atk.noOp()
    }

    private fun groupNpsWithFaceCards(
        groups: List<List<CommandCard.Face>>,
        supportGroup: List<CommandCard.Face>
    ): Map<CommandCard.NP, List<CommandCard.Face>> {
        val npGroups = mutableMapOf<CommandCard.NP, List<CommandCard.Face>>()

        val supportNp = CommandCard.NP.list.firstOrNull {
            images.support in it.supportCheckRegion
        }

        if (supportNp != null) {
            npGroups[supportNp] = supportGroup
        }

        val otherNps = if (supportNp != null) {
            CommandCard.NP.list - supportNp
        } else CommandCard.NP.list

        val otherGroups = if (supportNp != null) {
            groups.minusElement(supportGroup)
        } else groups

        otherNps.associateWithTo(npGroups) {
            it.servantCropRegion.getPattern().tag("NP:$it").use { npCropped ->
                otherGroups
                    .associateWith { group ->
                        group.first()
                            .servantMatchRegion
                            .find(npCropped, 0.6)
                            ?.score
                    }
                    .filter { (_, score) -> score != null }
                    .maxByOrNull { (_, score) -> score ?: 0.0 }
                    ?.key
                    ?: emptyList()
            }
        }

        Timber.debug { "NPs grouped with Face-cards: $npGroups" }
        return npGroups
    }

    private fun groupByFaceCard(supportGroup: List<CommandCard.Face>): List<List<CommandCard.Face>> {
        val remaining = CommandCard.Face.list.toMutableSet()
        val groups = mutableListOf<List<CommandCard.Face>>()

        if (supportGroup.isNotEmpty()) {
            groups.add(supportGroup)
            remaining.removeAll(supportGroup)

            Timber.debug { "Support group: $supportGroup" }
        }

        while (remaining.isNotEmpty()) {
            val u = remaining.first()
            remaining.remove(u)

            val group = mutableListOf<CommandCard.Face>()
            group.add(u)

            if (remaining.isNotEmpty()) {
                val me = u.servantCropRegion
                    .getPattern()
                    .tag("Card:$u")

                me.use {
                    val matched = remaining.filter {
                        me in it.servantMatchRegion
                    }

                    remaining.removeAll(matched)
                    group.addAll(matched)
                }
            }

            groups.add(group)
        }

        Timber.debug { "Face-card groups: $groups" }

        return groups
    }
}