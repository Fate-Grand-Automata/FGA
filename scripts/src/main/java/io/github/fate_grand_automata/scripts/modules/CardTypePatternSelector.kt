package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.CardScore
import io.github.fate_grand_automata.scripts.models.CardTypePattern
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class CardTypePatternSelector @Inject constructor() {

    /**
     * Tries each pattern in priority order and returns the first one the hand can satisfy,
     * as [picked cards in pattern order] + [every other hand card, in hand order] —
     * the same "always return the whole hand" contract as [ApplyBraveChains.pick]. The
     * caller reads only the leading `3 - npUsage.nps.size` entries as the actual selection.
     *
     * Returns null when no pattern is satisfiable, so the caller falls back to the existing
     * CardPriority + ApplyBraveChains pipeline unchanged.
     */
    fun select(
        cards: List<ParsedCard>,
        patterns: List<CardTypePattern>,
        cardPriority: CardPriorityPerWave,
        stage: Int,
        npUsage: NPUsage = NPUsage.none
    ): List<ParsedCard>? {
        for (pattern in patterns) {
            val picked = matchPattern(cards, pattern, cardPriority, stage, npUsage) ?: continue
            val notPicked = cards - picked

            return picked + notPicked
        }

        return null
    }

    private fun matchPattern(
        cards: List<ParsedCard>,
        pattern: CardTypePattern,
        cardPriority: CardPriorityPerWave,
        stage: Int,
        npUsage: NPUsage
    ): List<ParsedCard>? {
        val requiredTypes = requiredFaceCardTypes(pattern, npUsage)
        val priorityOrder = cardPriority.atWave(stage)

        val remainingCards = cards.toMutableList()
        val picked = mutableListOf<ParsedCard>()

        for (requiredType in requiredTypes) {
            val candidates = remainingCards.filter { it.type == requiredType }
            if (candidates.isEmpty()) return null

            // minByOrNull cannot return null here since candidates is non-empty (checked
            // above); it also keeps the first element on ties, so candidates' hand order
            // becomes the tiebreaker once CardPriority itself is tied.
            val chosenCard = candidates.minByOrNull { priorityRank(it, priorityOrder) }!!

            picked += chosenCard
            remainingCards -= chosenCard
        }

        return picked
    }

    // CardPriority.of() only validates that a custom priority string names exactly 9 cards; it
    // does not validate that all 9 (type, affinity) combinations are distinct. If a card's score
    // is absent from priorityOrder, List.indexOf would return -1, which minByOrNull would then
    // treat as the *highest* priority. That contradicts this codebase's existing convention for
    // "not in the list" (CardPriorityPerWave.atWave() appends CardTypeEnum.Unknown at the end
    // specifically to give it minimum priority), so an absent score is ranked last here too.
    private fun priorityRank(card: ParsedCard, priorityOrder: List<CardScore>): Int =
        priorityOrder.indexOf(CardScore(card.type, card.affinity)).let {
            if (it == -1) priorityOrder.size else it
        }

    // A turn has exactly 3 attack slots. When npUsage.nps is non-empty, that many slots
    // are spent tapping NPs instead of face cards. npUsage.cardsBeforeNP only decides how
    // many of the *remaining* face cards are tapped before vs after those NP taps — it
    // doesn't change which face cards are needed, so it's intentionally not read here.
    // (Derivation: inserting npCount NP slots at index cardsBeforeNP and then keeping only
    // the first 3 always keeps pattern[0 until 3 - npCount) and drops pattern's tail,
    // regardless of cardsBeforeNP's value, because before-slice ++ after-slice telescopes
    // to that same contiguous range for any valid cardsBeforeNP.) Splitting the result into
    // before/after-NP groups is the responsibility of whoever wires this into Card.kt.
    private fun requiredFaceCardTypes(pattern: CardTypePattern, npUsage: NPUsage): List<CardTypeEnum> {
        val npCount = npUsage.nps.size

        return pattern.take(3 - npCount)
    }
}
