package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptLog
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.Skill
import io.github.fate_grand_automata.scripts.models.battle.BattleState
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class Battle @Inject constructor(
    api: IFgoAutomataApi,
    private val servantTracker: ServantTracker,
    private val state: BattleState,
    private val battleConfig: IBattleConfig,
    private val autoSkill: AutoSkill,
    private val caster: Caster,
    private val card: Card,
    private val skillSpam: SkillSpam,
    private val shuffleChecker: ShuffleChecker,
    private val stageTracker: StageTracker,
    private val autoChooseTarget: AutoChooseTarget
) : IFgoAutomataApi by api {
    init {
        prefs.stopAfterThisRun = false
        state.markStartTime()

        resetState()
    }

    fun resetState() {
        // Don't increment no. of runs if we're just clicking on quest again and again
        // This can happen due to lags introduced during some events
        if (state.stage != -1) {
            state.nextRun()

            servantTracker.nextRun()
        }

        if (prefs.stopAfterThisRun) {
            prefs.stopAfterThisRun = false
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.StopAfterThisRun)
        }

        if (prefs.selectedServerConfigPref.shouldLimitRuns && state.runs >= prefs.selectedServerConfigPref.limitRuns) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.LimitRuns(state.runs))
        }
    }

    fun isIdle() = images[Images.BattleScreen] in locations.battle.screenCheckRegion

    fun clickAttack(): List<ParsedCard> {
        locations.battle.attackClick.click()

        // Wait for Attack button to disappear
        locations.battle.screenCheckRegion.waitVanish(images[Images.BattleScreen], 5.seconds)

        prefs.waitBeforeCards.wait()

        return card.readCommandCards()
    }

    fun performBattle() {
        prefs.waitBeforeTurn.wait()

        onTurnStarted()
        servantTracker.beginTurn()

        val npUsage = autoSkill.execute(state.stage, state.turn)
        skillSpam.spamSkills()

        val cards = clickAttack()
            .takeUnless { shouldShuffle(it, npUsage) }
            ?: shuffleCards()

        card.clickCommandCards(cards, npUsage)

        0.5.seconds.wait()
    }

    private fun shouldShuffle(cards: List<ParsedCard>, npUsage: NPUsage): Boolean {
        // Not this wave
        if (state.stage != (battleConfig.shuffleCardsWave - 1)) {
            return false
        }

        // Already shuffled
        if (state.shuffled) {
            return false
        }

        return shuffleChecker.shouldShuffle(
            mode = battleConfig.shuffleCards,
            cards = cards,
            npUsage = npUsage
        )
    }

    private fun shuffleCards(): List<ParsedCard> {
        locations.attack.backClick.click()

        caster.castMasterSkill(Skill.Master.C)
        state.shuffled = true

        return clickAttack()
    }

    private fun onTurnStarted() = useSameSnapIn {
        stageTracker.checkCurrentStage()

        state.nextTurn()

        if (battleConfig.autoChooseTarget) {
            autoChooseTarget.choose()
        }

        trackSkipTurns()

        val outOfCommands = isOutOfCommand()
        val perServerConfigPrefs = prefs.selectedServerConfigPref

        if (outOfCommands && perServerConfigPrefs.exitOnOutOfCommands) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.ExitOnOutOfCommands)
        }
    }

    private fun trackSkipTurns() {
        if (!(state.stage > 0 && state.turn < 1)) return

        var commandTurnsUntilStage = autoSkill.commandTurnsUntilStage(state.stage)

        // add additional turn since it is checking at next stage/wave
        commandTurnsUntilStage += 1

        val skipTurns = max(0, commandTurnsUntilStage - (state.currentTurn + state.skipCommandTurns))

        messages.log(
            ScriptLog.TurnTrackingAtNewStage(
                wave = state.stage,
                currentTurn = state.currentTurn,
                skipTurn = state.skipCommandTurns
            )
        )

        state.addSkipCommandTurns(skipTurns)
    }

    private fun isOutOfCommand(): Boolean {
        val totalCommandTurns = autoSkill.getTotalCommandTurns

        return (state.currentTurn + state.skipCommandTurns) > totalCommandTurns
    }

}
