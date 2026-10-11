package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot

/** Selects face cards which complete the configured three-card attack sequence. */
object CardSequenceHandler {
    private const val ATTACK_CARD_COUNT = 3

    fun pick(
        cards: List<ParsedCard>,
        desiredOrder: List<CardTypeEnum>,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
    ): List<ParsedCard>? {
        val validTypes = setOf(CardTypeEnum.Buster, CardTypeEnum.Arts, CardTypeEnum.Quick)
        if (desiredOrder.size != ATTACK_CARD_COUNT || desiredOrder.any { it !in validTypes }) {
            return null
        }

        val nps = npUsage.nps.toList()
        if (nps.size > ATTACK_CARD_COUNT || nps.any { npTypes[it.toFieldSlot()] !in validTypes }) {
            return null
        }

        val faceCardCount = ATTACK_CARD_COUNT - nps.size
        if (npUsage.cardsBeforeNP !in 0..faceCardCount) return null

        val npStart = npUsage.cardsBeforeNP
        val npPositions = nps.indices.map { npStart + it }.toSet()
        val effectiveOrder = desiredOrder.mapIndexed { index, configuredType ->
            val npIndex = index - npStart
            if (index in npPositions) npTypes.getValue(nps[npIndex].toFieldSlot())
            else configuredType
        }
        val requiredFaceTypes = effectiveOrder.filterIndexed { index, _ -> index !in npPositions }
        val validCards = AttackUtils.getValidNonUnknownCards(cards)
        val candidates = combinations(validCards, requiredFaceTypes)
        if (candidates.isEmpty()) return null

        val selected = when (braveChainEnum) {
            BraveChainEnum.None -> candidates.first()
            BraveChainEnum.WithNP -> pickWithNp(candidates, nps.firstOrNull()?.toFieldSlot())
            BraveChainEnum.Avoid -> pickAvoidingBrave(candidates, nps.map { it.toFieldSlot() })
        }

        return selected + (cards - selected)
    }

    private fun combinations(
        cards: List<ParsedCard>,
        requiredTypes: List<CardTypeEnum>,
    ): List<List<ParsedCard>> {
        fun build(
            position: Int,
            remaining: List<ParsedCard>,
            selected: List<ParsedCard>,
        ): List<List<ParsedCard>> {
            if (position == requiredTypes.size) return listOf(selected)

            return remaining
                .filter { it.type == requiredTypes[position] }
                .flatMap { card ->
                    build(
                        position = position + 1,
                        remaining = remaining - card,
                        selected = selected + card,
                    )
                }
        }

        return build(0, cards, emptyList())
    }

    private fun pickWithNp(
        candidates: List<List<ParsedCard>>,
        firstNpSlot: FieldSlot?,
    ): List<ParsedCard> {
        if (firstNpSlot == null) return candidates.first()

        return candidates.withIndex().maxWith(
            compareBy<IndexedValue<List<ParsedCard>>> { (_, candidate) ->
                candidate.count { it.fieldSlot == firstNpSlot }
            }.thenBy { (index, _) -> -index }
        ).value
    }

    private fun pickAvoidingBrave(
        candidates: List<List<ParsedCard>>,
        npSlots: List<FieldSlot>,
    ): List<ParsedCard> = candidates.firstOrNull { candidate ->
        val allSlots = candidate.mapNotNull { it.fieldSlot } + npSlots
        allSlots.distinct().size > 1
    } ?: candidates.first()
}
