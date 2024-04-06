package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.CardScore
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.ServantPriorityPerWave
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class FaceCardPriority @Inject constructor(
    private val cardPriority: CardPriorityPerWave,
    private val servantPriority: ServantPriorityPerWave?
) {

    private fun applyCardPriority(
        cards: List<ParsedCard>,
        stage: Int
    ): List<ParsedCard> {
        val sortedCards = cards
            .sortedWith(
                compareByDescending<ParsedCard> { parsedCard ->
                    /**
                     * Added sorting criteria for critical stars.
                     * Cards with critical stars and has affinity of Weak are prioritized.
                     */
                    when {
                        parsedCard.affinity == CardAffinityEnum.Weak && parsedCard.criticalPercentage > 7 -> 4
                        parsedCard.affinity == CardAffinityEnum.Weak && parsedCard.criticalPercentage in 1..7 -> 3
                        parsedCard.affinity == CardAffinityEnum.Normal && parsedCard.criticalPercentage > 0 -> 2
                        else -> 1
                    }
                }.thenBy {
                    it.type
                }
            )

        val groupedByScore = sortedCards.groupBy { CardScore(it.type, it.affinity) }

        return cardPriority
            .atWave(wave = stage)
            .mapNotNull { cardScore ->
                groupedByScore[cardScore]
            }
            .flatten()
    }

    private fun applyServantPriority(
        cards: List<ParsedCard>,
        priority: ServantPriorityPerWave,
        stage: Int
    ): List<ParsedCard> {
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
                val notPicked = cards - picked

                picked + notPicked
            }
    }

    /**
     * Sorts the given list of [ParsedCard] objects based on the specified stage.
     *
     * @param cards The list of [ParsedCard] objects to be sorted.
     * @param stage The Wave number to determine the sorting criteria.
     * @return The sorted list of [ParsedCard] objects.
     */
    fun sort(
        cards: List<ParsedCard>,
        stage: Int
    ): List<ParsedCard> =
        servantPriority
            ?.let { applyServantPriority(cards, it, stage) }
            ?: applyCardPriority(cards, stage)
}