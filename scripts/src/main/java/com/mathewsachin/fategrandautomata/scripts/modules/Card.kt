package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

class Card(fgAutomataApi: IFGAutomataApi) : IFGAutomataApi by fgAutomataApi {
    private lateinit var autoSkill: AutoSkill
    private lateinit var battle: Battle

    private lateinit var cardPriority: CardPriorityPerWave

    private var commandCards = emptyMap<CardScore, List<CommandCard.Face>>()
    private val remainingCards = mutableSetOf<CommandCard.Face>()
    private val remainingNps = mutableSetOf<CommandCard.NP>()
    private val noOfCardsToClick
        get() = (3 - (5 - remainingCards.size) - (3 - remainingNps.size))
            .coerceAtLeast(0)

    fun init(AutoSkillModule: AutoSkill, BattleModule: Battle) {
        autoSkill = AutoSkillModule
        battle = BattleModule

        cardPriority = CardPriorityPerWave.of(
            prefs.selectedAutoSkillConfig.cardPriority
        )
    }

    private fun getCardAffinity(commandCard: CommandCard.Face): CardAffinityEnum {
        val region = commandCard.affinityRegion

        if (region.exists(images.weak)) {
            return CardAffinityEnum.Weak
        }

        if (region.exists(images.resist)) {
            return CardAffinityEnum.Resist
        }

        return CardAffinityEnum.Normal
    }

    private fun getCardType(commandCard: CommandCard.Face): CardTypeEnum {
        val region = commandCard.typeRegion

        val stunRegion = region.copy(
            Y = 930,
            Width = 248,
            Height = 188
        )

        if (stunRegion.exists(images.stun)) {
            return CardTypeEnum.Unknown
        }

        if (region.exists(images.buster)) {
            return CardTypeEnum.Buster
        }

        if (region.exists(images.art)) {
            return CardTypeEnum.Arts
        }

        if (region.exists(images.quick)) {
            return CardTypeEnum.Quick
        }

        val msg = "Failed to determine Card type $region"
        toast(msg)
        logger.debug(msg)

        return CardTypeEnum.Unknown
    }

    private var commandCardGroups: List<List<CommandCard.Face>> = emptyList()
    private var commandCardGroupedWithNp: Map<CommandCard.NP, List<CommandCard.Face>> = emptyMap()
    private var firstNp: CommandCard.NP? = null

    fun readCommandCards() {
        remainingCards.addAll(CommandCard.Face.list)
        remainingNps.addAll(CommandCard.NP.list)
        firstNp = null

        screenshotManager.useSameSnapIn {
            commandCards = CommandCard.Face.list
                .groupBy {
                    val type = getCardType(it)
                    val affinity =
                        if (type == CardTypeEnum.Unknown)
                            CardAffinityEnum.Normal // Couldn't detect card type, so don't care about affinity
                        else getCardAffinity(it)

                    CardScore(type, affinity)
                }

            if (prefs.braveChains != BraveChainEnum.None) {
                val supportGroup = CommandCard.Face.list
                    .filter { it.supportCheckRegion.exists(images.support) }
                commandCardGroups = groupByFaceCard(supportGroup)
                commandCardGroupedWithNp = groupNpsWithFaceCards(commandCardGroups, supportGroup)
            }
        }
    }

    val canClickNpCards: Boolean
        get() {
            val weCanSpam = prefs.castNoblePhantasm == BattleNoblePhantasmEnum.Spam
            val weAreInDanger = prefs.castNoblePhantasm == BattleNoblePhantasmEnum.Danger
                    && battle.hasChosenTarget

            return (weCanSpam || weAreInDanger) && autoSkill.isFinished
        }

    fun clickNpCards() {
        for (npCard in CommandCard.NP.list) {
            npCard.clickLocation.click()
        }
    }

    fun clickNp(np: CommandCard.NP) {
        if (np in remainingNps) {
            np.clickLocation.click()
            remainingNps.remove(np)

            if (firstNp == null) {
                firstNp = np
            }
        }
    }

    fun clickCommandCards(Clicks: Int = noOfCardsToClick) {
        var clicksLeft = Clicks.coerceAtLeast(0)
        val toClick = mutableListOf<CommandCard.Face>()

        val cardsOrderedByPriority = cardPriority.atWave(battle.currentStage)
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

        when (prefs.braveChains) {
            BraveChainEnum.AfterNP -> {
                commandCardGroupedWithNp[firstNp]?.let { npGroup ->
                    pickCardsOrderedByPriority {
                        it in npGroup
                    }
                }
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
            }
        }

        // Pick more cards if needed
        pickCardsOrderedByPriority()

        val isBeforeNP = firstNp == null

        // When clicking 3 cards, move the card with 2nd highest priority to last position to amplify its effect
        // Do the same when clicking 2 cards unless they're used before NPs.
        // Skip if NP spamming because we don't know how many NPs might've been used
        if (prefs.rearrangeCards
            && prefs.braveChains != BraveChainEnum.Avoid // Avoid: consecutive cards to be of different servants
            && prefs.castNoblePhantasm == BattleNoblePhantasmEnum.None
            && (toClick.size == 3 || (toClick.size == 2 && !isBeforeNP))
        ) {
            logger.info("Rearranging cards")

            Collections.swap(toClick, toClick.lastIndex - 1, toClick.lastIndex)
        }

        if (!isBeforeNP && Clicks < 3) {
            // Also click on remaining cards,
            // since some people may put NPs in AutoSkill which aren't charged yet
            pickCardsOrderedByPriority(3 - Clicks)
        }

        logger.info("Clicking cards: $toClick")
        toClick.forEach { it.clickLocation.click() }
    }

    private fun groupNpsWithFaceCards(
        groups: List<List<CommandCard.Face>>,
        supportGroup: List<CommandCard.Face>
    ): Map<CommandCard.NP, List<CommandCard.Face>> {
        val npGroups = mutableMapOf<CommandCard.NP, List<CommandCard.Face>>()

        val supportNp = CommandCard.NP.list.firstOrNull {
            it.supportCheckRegion.exists(images.support)
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
            it.servantCropRegion.getPattern().use { npCropped ->
                otherGroups.maxBy { group ->
                    group.first()
                        .servantMatchRegion
                        .findAll(npCropped, 0.4)
                        .firstOrNull()?.score ?: 0.0
                } ?: emptyList()
            }
        }

        logger.info("NPs grouped with Face-cards: $npGroups")
        return npGroups
    }

    private fun groupByFaceCard(supportGroup: List<CommandCard.Face>): List<List<CommandCard.Face>> {
        val remaining = CommandCard.Face.list.toMutableSet()
        val groups = mutableListOf<List<CommandCard.Face>>()

        if (supportGroup.isNotEmpty()) {
            groups.add(supportGroup)
            remaining.removeAll(supportGroup)

            logger.info("Support group: $supportGroup")
        }

        while (remaining.isNotEmpty()) {
            val u = remaining.first()
            remaining.remove(u)

            val group = mutableListOf<CommandCard.Face>()
            group.add(u)

            if (remaining.isNotEmpty()) {
                val me = u.servantCropRegion.getPattern()

                me.use {
                    val matched = remaining.filter {
                        val region = it.servantMatchRegion
                        region.exists(me)
                    }

                    remaining.removeAll(matched)
                    group.addAll(matched)
                }
            }

            groups.add(group)
        }

        logger.info("Face-card groups: $groups")

        return groups
    }
}