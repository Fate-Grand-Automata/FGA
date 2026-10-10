package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.ScriptLog
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.SpamConfigPerTeamSlot
import io.github.fate_grand_automata.scripts.models.battle.BattleState
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.fate_grand_automata.scripts.modules.attack.MightyChainHandler
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
        parser.parse()
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

        fun <T> List<T>.inCurrentWave(default: T) =
            if (isNotEmpty())
                this[state.stage.coerceIn(indices)]
            else default

        val braveChainsPerWave = battleConfig.braveChains
        val rearrangeCardsPerWave = battleConfig.rearrangeCards

        if (battleConfig.useMightyChain) {
            val mightyNpUsage = NPUsage(npUsage.nps + spamNps, npUsage.cardsBeforeNP)
            val braveChain = braveChainsPerWave.inCurrentWave(BraveChainEnum.None)
            val mightyOrder = battleConfig.mightyChainOrders
                .inCurrentWave(
                    listOf(CardTypeEnum.Buster, CardTypeEnum.Arts, CardTypeEnum.Quick)
                )
            val cardsOrderedForMighty = cardsOrderedByPriority.sortedBy { card ->
                mightyOrder.indexOf(card.type).takeIf { it >= 0 } ?: Int.MAX_VALUE
            }
            val npTypes = mightyNpUsage.nps.associate { np ->
                val slot = np.toFieldSlot()
                slot to battleConfig.npCardTypes.getOrElse(slot.position - 1) {
                    CardTypeEnum.Unknown
                }
            }

            val mighty = MightyChainHandler.pick(
                cards = cardsOrderedForMighty,
                npUsage = mightyNpUsage,
                npTypes = npTypes,
                braveChainEnum = braveChain,
                forceBraveChain = braveChain == BraveChainEnum.Always,
            )

            if (mighty != null) {
                return braveChains.pick(
                    cards = mighty,
                    npUsage = mightyNpUsage,
                    braveChains = BraveChainEnum.None,
                    rearrange = rearrangeCardsPerWave.inCurrentWave(false),
                ).map { it.card }
            }
        }

        return braveChains.pick(
            cards = cardsOrderedByPriority,
            npUsage = npUsage,
            braveChains = braveChainsPerWave.inCurrentWave(BraveChainEnum.None),
            rearrange = rearrangeCardsPerWave.inCurrentWave(false)
        ).map { it.card }
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
