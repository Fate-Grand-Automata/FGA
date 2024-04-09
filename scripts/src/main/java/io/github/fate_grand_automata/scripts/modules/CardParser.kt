package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptNotify
import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class CardParser @Inject constructor(
    api: IFgoAutomataApi,
    private val servantTracker: ServantTracker
) : IFgoAutomataApi by api {

    private fun CommandCard.Face.affinity(): CardAffinityEnum {
        val region = locations.attack.affinityRegion(this)

        if (images[Images.Weak] in region) {
            return CardAffinityEnum.Weak
        }

        if (images[Images.Resist] in region) {
            return CardAffinityEnum.Resist
        }

        return CardAffinityEnum.Normal
    }

    private fun CommandCard.Face.hasCriticalStar(): Boolean {
        val starRegion = locations.attack.starRegion(this)

        return starRegion.exists(
            images[Images.CriticalStarExist],
            similarity = 0.7
        )
    }

    private fun CommandCard.Face.readCriticalStarPercentage(): Int {
        var percentage = ""
        useColor {
            val starPercentageRegion = locations.attack.starPercentageRegion(this)
            percentage = starPercentageRegion.detectNumVarBg()
        }

        val regex = "\\d".toRegex()
        val matchResult = regex.find(percentage)
        val digit = matchResult?.value?.toInt()
        return if (digit == 0) 10 else digit ?: 1
    }

    private fun CommandCard.Face.isStunned(): Boolean {
        val stunRegion = locations.attack.typeRegion(this).copy(
            y = 930,
            width = 248,
            height = 188
        )

        return listOf(
            images[Images.Stun],
            images[Images.Immobilized],
            images[Images.StunBuster],
            images[Images.StunArts],
            images[Images.StunQuick],
        ) in stunRegion
    }

    private fun CommandCard.Face.type(): CardTypeEnum {
        val region = locations.attack.typeRegion(this)

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

    fun parseCommandCards(readCriticalStar: Boolean = false): List<ParsedCard> {
        val cardsGroupedByServant = servantTracker.faceCardsGroupedByServant()

        val cards = CommandCard.Face.list
            .map {
                val stunned = it.isStunned()

                val hasCriticalStar = it.hasCriticalStar()
                val starPercentage = when {
                    stunned -> 0
                    hasCriticalStar && readCriticalStar -> it.readCriticalStarPercentage()
                    hasCriticalStar -> 1
                    else -> 0
                }

                val type = when {
                    stunned -> CardTypeEnum.Unknown
                    else -> it.type()
                }

                val affinity = when (type) {
                    CardTypeEnum.Unknown -> CardAffinityEnum.Normal
                    else -> {
                        val currentAffinity = it.affinity()
                        when {
                            currentAffinity == CardAffinityEnum.Normal && starPercentage > 7 ->
                                CardAffinityEnum.NormalCritical

                            currentAffinity == CardAffinityEnum.Weak && starPercentage > 7 ->
                                CardAffinityEnum.WeakCritical

                            else -> currentAffinity
                        }
                    }
                }

                val servant = cardsGroupedByServant
                    .filterValues { cards -> it in cards }
                    .keys
                    .firstOrNull()
                    ?: TeamSlot.Unknown

                val fieldSlot = servantTracker.deployed
                    .entries
                    .firstOrNull { (_, teamSlot) -> teamSlot == servant }
                    ?.key

                ParsedCard(
                    card = it,
                    isStunned = stunned,
                    type = type,
                    affinity = affinity,
                    servant = servant,
                    fieldSlot = fieldSlot,
                    criticalPercentage = starPercentage
                )
            }

        var unknownCardTypes = false
        var unknownServants = false
        val failedToDetermine = cards
            .filter {
                when {
                    it.isStunned -> false
                    it.type == CardTypeEnum.Unknown -> {
                        unknownCardTypes = true
                        true
                    }

                    it.servant is TeamSlot.Unknown && !prefs.skipServantFaceCardCheck -> {
                        unknownServants = true
                        true
                    }

                    else -> false
                }
            }
            .map { it.card }

        if (failedToDetermine.isNotEmpty()) {
            messages.notify(
                ScriptNotify.FailedToDetermineCards(failedToDetermine, unknownCardTypes, unknownServants)
            )
        }

        return cards
    }
}