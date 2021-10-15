package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class FaceCardPriority @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val cardPriority: CardPriorityPerWave,
    private val servantPriority: ServantPriorityPerWave?
) : IFgoAutomataApi by fgAutomataApi {

    private fun applyCardPriority(
        cards: List<ParsedCard>,
        stage: Int
    ): List<CommandCard.Face> {
        val groupedByScore = cards.groupBy(
            keySelector = { CardScore(it.type, it.affinity) },
            valueTransform = { it.card }
        )

        return cardPriority
            .atWave(stage)
            .mapNotNull { groupedByScore[it] }
            .flatten()
    }

    private fun applyServantPriority(
        cards: List<ParsedCard>,
        priority: ServantPriorityPerWave,
        stage: Int
    ): List<CommandCard.Face> {
        val groupedByServant = cards.groupBy { it.servant }

        return priority
            .atWave(stage)
            .mapNotNull { groupedByServant[it] }
            .map { servantCards ->
                applyCardPriority(
                    // Stunned cards at the end
                    cards = servantCards.filter { it.type != CardTypeEnum.Unknown },
                    stage = stage
                )
            }
            .flatten()
            .let { picked ->
                // In case less than 3 cards are picked
                val notPicked = cards
                    .filter { it.card !in picked }
                    .map { it.card }

                if (notPicked.isNotEmpty()) {
                    messages.log(ScriptLog.CardsNotPickedByServantPriority(notPicked))
                }

                picked + notPicked
            }
    }

    fun sort(
        cards: List<ParsedCard>,
        stage: Int
    ): List<CommandCard.Face> =
        servantPriority
            ?.let { applyServantPriority(cards, it, stage) }
            ?: applyCardPriority(cards, stage)
}