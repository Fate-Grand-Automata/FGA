package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.AttackPriorityEnum
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.fate_grand_automata.scripts.modules.ApplyBraveChains
import java.util.Collections

object AttackPriorityHandler {
    fun pick(
        cards: List<ParsedCard>,
        attackPriorityOrder: List<AttackPriorityEnum> = AttackPriorityEnum.defaultOrder,
        chainPriority: List<ChainTypeEnum> = ChainTypeEnum.defaultOrder,
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        rearrange: Boolean = false,
        priorityOrder: List<ParsedCard> = cards,
    ): List<ParsedCard> {
        // A saved priority can omit or repeat scores. Restore unranked faces from the parsed
        // hand and deduplicate by face button before counting cards for any chain.
        val rankedCards = (priorityOrder + cards).distinct()
        // Color and Mighty Chains require readable, playable cards. Keep the other faces
        // in hand order within each group so unreadable but playable faces fill slots first.
        val nonUnknownCards = rankedCards.filter { usability(it) == 0 }
        val finalFallback = nonUnknownCards + (cards.distinct() - nonUnknownCards).sortedBy(::usability)
        if (!AttackUtils.isChainable(
                cards = nonUnknownCards,
                npUsage = npUsage,
                npTypes = npTypes,
            )) {
            // Slot information can still form a Brave Chain when colors are missing, including for an NP.
            // Avoid uses the default picker here, which can reorder a hand even with too few readable colors.
            if (braveChainEnum != BraveChainEnum.Avoid) {
                val braveChainOrder = BraveChainHandler.pick(
                    cards = finalFallback,
                    braveChainEnum = braveChainEnum,
                    npUsage = npUsage,
                )
                if (braveChainOrder != null) {
                    return rearrange(braveChainOrder, rearrange, npUsage)
                }
            }
            val picked = ApplyBraveChains.pick(
                cards = finalFallback,
                braveChains = braveChainEnum,
                npUsage = npUsage,
            )
            if (!rearrange) return picked

            // The fallback's Avoid mode can change the selected faces when rearranging.
            // Compare with its unrearranged pick so that case gets the same usability guard.
            val rearranged = ApplyBraveChains.pick(finalFallback, braveChainEnum, rearrange = true, npUsage = npUsage)
            return keepUsableOrder(picked, rearranged)
        }

        // Color and Mighty planners count only the cards available to their search.
        val cardCountPerFieldSlotMap = AttackUtils.getCardsPerFieldSlotMap(nonUnknownCards, npUsage)
        val cardCountPerCardTypeMap = AttackUtils.getCardsPerCardTypeMap(nonUnknownCards, npTypes)

        var cardChainPlan: CardChainPriorityHandler.Plan? = null
        var braveChainFallback: List<ParsedCard>? = null
        for (attackPriority in attackPriorityOrder) {
            when (attackPriority) {
                AttackPriorityEnum.BraveChainPriority -> {
                    braveChainFallback = BraveChainHandler.pick(
                        cards = if (braveChainEnum == BraveChainEnum.Avoid) nonUnknownCards else finalFallback,
                        braveChainEnum = braveChainEnum,
                        npUsage = npUsage,
                    )
                }
                AttackPriorityEnum.CardChainPriority -> {
                    if (cardChainPlan != null) continue

                    val requiredBraveSlot = braveChainFallback?.firstOrNull()?.fieldSlot
                        ?.takeIf { braveChainEnum == BraveChainEnum.WithNP || braveChainEnum == BraveChainEnum.Always }
                    val candidate = CardChainPriorityHandler.pick(
                        cards = nonUnknownCards,
                        chainPriority = chainPriority,
                        braveChainEnum = braveChainEnum,
                        npUsage = npUsage,
                        npTypes = npTypes,
                        cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
                        cardCountPerCardTypeMap = cardCountPerCardTypeMap,
                        forceBraveChain = requiredBraveSlot != null,
                    )
                    // A color-based plan can miss the required Servant if their Brave Chain uses unreadable cards.
                    // Accept it only if the face cards it will click still complete that Brave Chain.
                    val cardsNeeded = 3 - npUsage.nps.size
                    cardChainPlan = candidate?.takeIf { (order) ->
                        requiredBraveSlot == null || (
                            order.size >= cardsNeeded && order.take(cardsNeeded).all { it.fieldSlot == requiredBraveSlot }
                        )
                    }
                }
                else -> continue
            }
        }

        // Append cards omitted by the selected planner in fallback order, which keeps stunned cards last.
        val pickedOrder = cardChainPlan?.cards ?: braveChainFallback ?: finalFallback
        val fullOrder = pickedOrder + (finalFallback - pickedOrder.toSet())
        val chainType = cardChainPlan?.chainType
        val npOwner = npUsage.nps.singleOrNull()?.toFieldSlot()
        val orderWithBackup = if (
            chainType != null && chainType != ChainTypeEnum.Avoid && npOwner != null &&
            (braveChainEnum == BraveChainEnum.WithNP || braveChainEnum == BraveChainEnum.Always)
        ) {
            keepBraveChainBackup(fullOrder, npOwner)
        } else {
            fullOrder
        }

        return rearrange(orderWithBackup, rearrange, npUsage)
    }

    // Lower ranks are safer to click: unreadable faces can still be selected, stunned faces cannot.
    private fun usability(card: ParsedCard): Int = when {
        card.isStunned -> 2
        card.type == CardTypeEnum.Unknown -> 1
        else -> 0
    }

    // TODO: Share these rearrangement rules with ApplyBraveChains.rearrange if the default picker can adopt these rules.
    internal fun rearrange(cards: List<ParsedCard>, rearrange: Boolean, npUsage: NPUsage): List<ParsedCard> {
        if (!rearrange || cards.size < 2) return cards

        val swapWith = when {
            // Keep the highest ranked or matching face directly after the NP in every Brave Chains mode.
            npUsage.nps.size == 1 && npUsage.cardsBeforeNP == 1 -> {
                if (cards[1].isStunned) return cards
                1
            }
            // Move the next strongest face to the final position. If the NP opens, move the strongest there.
            npUsage.nps.size <= 1 && npUsage.cardsBeforeNP == 0 -> minOf(cards.size, 3 - npUsage.nps.size) - 1
            else -> return cards
        }
        val reordered = cards.toMutableList().also { Collections.swap(it, swapWith - 1, swapWith) }
        return keepUsableOrder(cards, reordered)
    }

    private fun keepUsableOrder(cards: List<ParsedCard>, rearranged: List<ParsedCard>): List<ParsedCard> {
        // Spam NPs can consume a slot even when their portraits weren't detected for planning.
        // Reject a rearrangement that puts a worse face ahead of a better one that was earlier.
        val newPositions = rearranged.withIndex().associate { (index, card) -> card to index }
        for ((index, earlier) in cards.withIndex()) {
            val displacesEarlier = cards.drop(index + 1).any { later ->
                usability(later) > usability(earlier) &&
                    newPositions.getValue(later) < newPositions.getValue(earlier)
            }
            if (displacesEarlier) return cards
        }
        return rearranged
    }

    private fun keepBraveChainBackup(cards: List<ParsedCard>, npOwner: FieldSlot): List<ParsedCard> {
        // A single NP leaves two face-card slots. If those cards share its owner, a playable
        // third card from that Servant preserves the Brave Chain when the NP fails to select.
        val selected = cards.take(2)
        if (selected.size < 2 || selected.any { it.fieldSlot != npOwner }) return cards

        val remaining = cards.drop(2)
        val backup = remaining.firstOrNull { !it.isStunned && it.fieldSlot == npOwner } ?: return cards
        // Rearrange Cards can swap the two planned faces with one NP, leaving this backup third.
        return selected + backup + (remaining - backup)
    }
}
