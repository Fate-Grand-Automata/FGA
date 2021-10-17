package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.libautomata.dagger.ScriptScope
import java.util.*
import javax.inject.Inject

@ScriptScope
class Card @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val servantTracker: ServantTracker,
    private val state: BattleState,
    private val spamConfig: SpamConfigPerTeamSlot,
    private val caster: Caster,
    private val parser: CardParser,
    private val priority: FaceCardPriority,
    private val braveChains: ApplyBraveChains,
    private val battleConfig: IBattleConfig
) : IFgoAutomataApi by fgAutomataApi {

    fun readCommandCards(): List<ParsedCard> {
        return useSameSnapIn {
            parser.parse()
        }
    }

    private val spamNps: Set<CommandCard.NP> get() =
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
        atk: AutoSkillAction.Atk
    ): List<CommandCard.Face> {
        val cardsOrderedByPriority = priority.sort(cards, state.stage)

        fun <T> List<T>.inCurrentWave(default: T) =
            if (isNotEmpty())
                this[state.stage.coerceIn(indices)]
            else default

        val braveChainsPerWave = battleConfig.braveChains
        val rearrangeCardsPerWave = battleConfig.rearrangeCards

        return braveChains.pick(
            cards = cardsOrderedByPriority,
            atk = atk,
            braveChains = braveChainsPerWave.inCurrentWave(BraveChainEnum.None),
            rearrange = rearrangeCardsPerWave.inCurrentWave(false)
        ).map { it.card }
    }

    fun clickCommandCards(
        cards: List<ParsedCard>,
        atk: AutoSkillAction.Atk
    ) {
        val pickedCards = pickCards(cards, atk)
            .take(3)

        if (atk.cardsBeforeNP > 0) {
            pickedCards
                .take(atk.cardsBeforeNP)
                .also { messages.log(ScriptLog.ClickingCards(it)) }
                .forEach { caster.use(it) }
        }

        val nps = atk.nps + spamNps

        if (nps.isNotEmpty()) {
            nps
                .also { messages.log(ScriptLog.ClickingNPs(it)) }
                .forEach { caster.use(it) }
        }

        pickedCards
            .drop(atk.cardsBeforeNP)
            .also { messages.log(ScriptLog.ClickingCards(it)) }
            .forEach { caster.use(it) }
    }
}