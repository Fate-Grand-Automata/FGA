package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.CardScore
import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.BattleNoblePhantasmEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.ScriptExitException

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

class Card(fgAutomataApi: IFGAutomataApi) : IFGAutomataApi by fgAutomataApi {
    private lateinit var autoSkill: AutoSkill
    private lateinit var battle: Battle

    private lateinit var cardPriorityArray: List<List<CardScore>>

    private val commandCards = mutableMapOf<CardScore, MutableList<Int>>()
    private var cardsClickedSoFar = 0

    fun init(AutoSkillModule: AutoSkill, BattleModule: Battle) {
        autoSkill = AutoSkillModule
        battle = BattleModule

        initCardPriorityArray()
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
                getCardScores(
                    it
                )
            }
    }

    private fun getCardAffinity(Region: Region): CardAffinityEnum {
        if (Region.exists(images.weak)) {
            return CardAffinityEnum.Weak
        }

        if (Region.exists(images.resist)) {
            return CardAffinityEnum.Resist
        }

        return CardAffinityEnum.Normal
    }

    private fun getCardType(Region: Region): CardTypeEnum {
        if (Region.exists(images.buster)) {
            return CardTypeEnum.Buster
        }

        if (Region.exists(images.art)) {
            return CardTypeEnum.Arts
        }

        if (Region.exists(images.quick)) {
            return CardTypeEnum.Quick
        }

        toast("Failed to determine Card type $Region")

        return CardTypeEnum.Buster
    }

    fun readCommandCards() {
        commandCards.clear()

        screenshotManager.useSameSnapIn {
            for (cardSlot in 0..4) {
                val affinity = getCardAffinity(game.BattleCardAffinityRegionArray[cardSlot])
                val type = getCardType(game.BattleCardTypeRegionArray[cardSlot])

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
        for (npCard in game.BattleNpCardClickArray) {
            npCard.click()
        }
    }

    fun clickCommandCards(Clicks: Int) {
        var i = 1

        val cardPriorityIndex = battle.currentStage.coerceIn(cardPriorityArray.indices)

        for (cardPriority in cardPriorityArray[cardPriorityIndex]) {
            if (!commandCards.containsKey(cardPriority))
                continue

            val currentCardTypeStorage = commandCards[cardPriority]
                ?: continue

            for (cardSlot in currentCardTypeStorage) {
                if (Clicks < i) {
                    cardsClickedSoFar = i - 1
                    return
                }

                if (i > cardsClickedSoFar) {
                    game.BattleCommandCardClickArray[cardSlot].click()
                }

                ++i
            }
        }
    }

    fun resetCommandCards() {
        commandCards.clear()
        cardsClickedSoFar = 0
    }
}