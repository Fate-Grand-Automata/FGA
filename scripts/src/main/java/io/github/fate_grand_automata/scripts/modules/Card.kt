package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.ScriptLog
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.SpamConfigPerTeamSlot
import io.github.fate_grand_automata.scripts.models.battle.BattleState
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class Card @Inject constructor(
    api: IFgoAutomataApi,
    private val servantTracker: ServantTracker,
    private val state: BattleState,
    private val spamConfig: SpamConfigPerTeamSlot,
    private val caster: Caster,
    private val parser: CardParser,
    private val priority: FaceCardPriority,
    private val braveChains: ApplyBraveChains,
    private val battleConfig: IBattleConfig
) : IFgoAutomataApi by api {

    fun readCommandCards(): List<ParsedCard> = useSameSnapIn {
        parser.parseCommandCards(
            readCriticalStar = battleConfig.readCriticalStarPriority
        )
    }

    private val spamNps: Set<CommandCard.NP>
        get() =
            (FieldSlot.list.zip(CommandCard.NP.list))
                .mapNotNull { (servantSlot, np) ->
                    val teamSlot = servantTracker.deployed[servantSlot] ?: return@mapNotNull null
                    val npSpamConfig = spamConfig[teamSlot].np

                    if (caster.canSpam(npSpamConfig.spam) && (state.stage + 1) in npSpamConfig.waves)
                        np
                    else null
                }
                .toSet()

    private fun pickCards(
        cards: List<ParsedCard>,
        npUsage: NPUsage
    ): List<CommandCard.Face> {
        val cardsOrderedByPriority = priority.sort(cards, state.stage)

        /**
         * Returns the element at the current wave index in the list, or the default value if the list is empty.
         *
         * @param default The default value to return if the list is empty.
         * @return The element at the current wave index in the list, or the default value if the list is empty.
         */
        fun <T> List<T>.inCurrentWave(default: T): T = when (isNotEmpty()) {
            true -> this[state.stage.coerceIn(indices)]
            false -> default
        }

        val braveChainsThisWave = battleConfig.braveChains.inCurrentWave(default = BraveChainEnum.None)
        val rearrangeCardsThisWave = battleConfig.rearrangeCards.inCurrentWave(default = false)

        return braveChains.pick(
            cards = cardsOrderedByPriority,
            npUsage = npUsage,
            braveChains = braveChainsThisWave,
            rearrange = rearrangeCardsThisWave
        ).map { parsedCard ->
            parsedCard.card
        }
    }

    fun clickCommandCards(
        cards: List<ParsedCard>,
        npUsage: NPUsage
    ) {
        val pickedCards = pickCards(cards, npUsage)
            .take(3)

        if (npUsage.cardsBeforeNP > 0) {
            pickedCards
                .take(npUsage.cardsBeforeNP)
                .also { messages.log(ScriptLog.ClickingCards(it)) }
                .forEach { caster.use(it) }
        }

        val nps = npUsage.nps + spamNps

        if (nps.isNotEmpty()) {
            nps
                .also { messages.log(ScriptLog.ClickingNPs(it)) }
                .forEach { caster.use(it) }
        }

        pickedCards
            .drop(npUsage.cardsBeforeNP)
            .also { messages.log(ScriptLog.ClickingCards(it)) }
            .forEach { caster.use(it) }
    }
}