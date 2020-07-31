package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.CardScore
import com.mathewsachin.fategrandautomata.scripts.CommandCard
import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.libautomata.ScriptExitException
import mu.KotlinLogging

private const val dummyNormalAffinityChar = 'X'
private const val cardPriorityErrorString = "Battle_CardPriority Error at '"

const val cardPriorityStageSeparator = "\n"

fun getCardScores(Priority: String): List<CardScore> {
    val scores = Priority
        .splitToSequence(',')
        .map { it.trim().toUpperCase() }
        .map {
            when (it.length) {
                1 -> "$dummyNormalAffinityChar$it"
                2 -> it
                else -> throw ScriptExitException("$cardPriorityErrorString${it}': Invalid card length.")
            }
        }
        .map {
            val cardType = when (it[1]) {
                'B' -> CardTypeEnum.Buster
                'A' -> CardTypeEnum.Arts
                'Q' -> CardTypeEnum.Quick
                else -> throw ScriptExitException("$cardPriorityErrorString${it[1]}': Only 'B', 'A' and 'Q' are valid card types.")
            }

            val cardAffinity = when (it[0]) {
                'W' -> CardAffinityEnum.Weak
                'R' -> CardAffinityEnum.Resist
                dummyNormalAffinityChar -> CardAffinityEnum.Normal
                else -> throw ScriptExitException("$cardPriorityErrorString${it[0]}': Only 'W', and 'R' are valid card affinities.")
            }

            CardScore(
                cardType,
                cardAffinity
            )
        }
        .toList()

    if (scores.size != 9) {
        throw ScriptExitException("$cardPriorityErrorString': Expected 9 cards, but ${scores.size} found.")
    }

    return scores
}

private val logger = KotlinLogging.logger {}

class Card(fgAutomataApi: IFGAutomataApi) : IFGAutomataApi by fgAutomataApi {
    private lateinit var autoSkill: AutoSkill
    private lateinit var battle: Battle

    private lateinit var cardPriorityArray: List<List<CardScore>>

    private val commandCards = mutableMapOf<CardScore, MutableList<CommandCard>>()
    private val remainingCards = mutableSetOf<CommandCard>()

    fun init(AutoSkillModule: AutoSkill, BattleModule: Battle) {
        autoSkill = AutoSkillModule
        battle = BattleModule

        initCardPriorityArray()
        resetCommandCards()
    }

    private fun initCardPriorityArray() {
        val priority = prefs.selectedAutoSkillConfig.cardPriority

        if (priority.length == 3) {
            initCardPriorityArraySimple(priority)
        } else initCardPriorityArrayDetailed(priority)
    }

    private fun initCardPriorityArraySimple(Priority: String) {
        val detailedPriority = Priority
            .map { "W$it, $dummyNormalAffinityChar$it, R$it" }
            .joinToString()

        initCardPriorityArrayDetailed(detailedPriority)
    }

    private fun initCardPriorityArrayDetailed(Priority: String) {
        cardPriorityArray = Priority
            .split(cardPriorityStageSeparator)
            .map {
                getCardScores(it)
                    // Give minimum priority to unknown
                    .plus(CardScore(CardTypeEnum.Unknown, CardAffinityEnum.Normal))
            }
    }

    private fun getCardAffinity(commandCard: CommandCard): CardAffinityEnum {
        val region = commandCard.affinityRegion

        if (region.exists(images.weak)) {
            return CardAffinityEnum.Weak
        }

        if (region.exists(images.resist)) {
            return CardAffinityEnum.Resist
        }

        return CardAffinityEnum.Normal
    }

    private fun getCardType(commandCard: CommandCard): CardTypeEnum {
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

    fun readCommandCards() {
        commandCards.clear()

        screenshotManager.useSameSnapIn {
            for (cardSlot in CommandCard.list) {
                val type = getCardType(cardSlot)
                val affinity =
                    if (type == CardTypeEnum.Unknown)
                        CardAffinityEnum.Normal // Couldn't detect card type, so don't care about affinity
                    else getCardAffinity(cardSlot)

                val score = CardScore(
                    type,
                    affinity
                )

                if (!commandCards.containsKey(score)) {
                    commandCards[score] = mutableListOf()
                }

                commandCards[score]?.add(cardSlot)
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
        for (npCard in game.battleNpCardClickArray) {
            npCard.click()
        }
    }

    fun clickCommandCards(Clicks: Int) {
        val cardPriorityIndex = battle.currentStage.coerceIn(cardPriorityArray.indices)

        cardPriorityArray[cardPriorityIndex]
            .mapNotNull { commandCards[it] }
            .flatten()
            .filter { it in remainingCards }
            .take(Clicks)
            .forEach {
                it.clickLocation.click()

                remainingCards.remove(it)
            }
    }

    fun resetCommandCards() {
        commandCards.clear()

        remainingCards.addAll(CommandCard.list)
    }
}