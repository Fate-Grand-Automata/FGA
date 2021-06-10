package com.mathewsachin.fategrandautomata.util

import android.app.Service
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.di.script.ScriptComponentBuilder
import com.mathewsachin.fategrandautomata.di.script.ScriptEntryPoint
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.entrypoints.*
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.launcher.ScriptLauncher
import com.mathewsachin.fategrandautomata.ui.launcher.ScriptLauncherResponse
import com.mathewsachin.fategrandautomata.ui.support_img_namer.showSupportImageNamer
import com.mathewsachin.libautomata.*
import dagger.hilt.EntryPoints
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration

@ServiceScoped
class ScriptManager @Inject constructor(
    service: Service,
    val userInterface: ScriptRunnerUserInterface,
    val imageLoader: ImageLoader,
    val preferences: IPreferences,
    val prefsCore: PrefsCore,
    val storageProvider: StorageProvider,
    val platformImpl: IPlatformImpl,
    val messages: ScriptMessages
) {
    private val service = service as ScriptRunnerService

    var scriptState: ScriptState = ScriptState.Stopped
        private set

    // Show message box synchronously
    suspend fun message(Title: String, Message: String, Error: Exception? = null): Boolean = suspendCancellableCoroutine {
        service.showMessageBox(Title, Message, Error) {
            it.resume(true)
        }
    }

    private fun AutoBattle.ExitReason.text(): String = when (this) {
        AutoBattle.ExitReason.Abort -> messages.stoppedByUser
        is AutoBattle.ExitReason.Unexpected -> "${messages.unexpectedError}: ${e.message}"
        AutoBattle.ExitReason.CEGet -> messages.ceGet
        AutoBattle.ExitReason.CEDropped -> messages.ceDropped
        is AutoBattle.ExitReason.LimitMaterials -> messages.farmedMaterials(count)
        AutoBattle.ExitReason.WithdrawDisabled -> messages.withdrawDisabled
        AutoBattle.ExitReason.APRanOut -> messages.apRanOut
        AutoBattle.ExitReason.InventoryFull -> messages.inventoryFull
        is AutoBattle.ExitReason.LimitRuns -> messages.timesRan(count)
        AutoBattle.ExitReason.SupportSelectionManual -> messages.supportSelectionManual
        AutoBattle.ExitReason.SupportSelectionFriendNotSet -> messages.supportSelectionFriendNotSet
        AutoBattle.ExitReason.SupportSelectionPreferredNotSet -> messages.supportSelectionPreferredNotSet
        is AutoBattle.ExitReason.SkillCommandParseError -> "AutoSkill Parse error:\n\n${e.message}"
        is AutoBattle.ExitReason.CardPriorityParseError -> msg
    }

    // Making the exit message in the UI side will allow us to go for a custom UI later if needed
    private fun makeExitMessage(reason: AutoBattle.ExitReason, state: AutoBattle.ExitState) = buildString {
        appendLine(reason.text())
        appendLine()

        messages.makeRefillAndRunsMessage(
            timesRan = state.timesRan,
            timesRefilled = state.timesRefilled
        ).let { msg ->
            if (msg.isNotBlank()) {
                appendLine(msg)
            }
        }

        if (!preferences.stopOnCEDrop && state.ceDropCount > 0) {
            appendLine("${state.ceDropCount} ${messages.ceDropped}")
            appendLine()
        }

        if (preferences.selectedBattleConfig.materials.isNotEmpty()) {
            appendLine(messages.materials(state.materials))
            appendLine()
        }

        appendLine(messages.time(state.totalTime))

        if (state.timesRan > 1) {
            appendLine(messages.avgTimePerRun(state.averageTimePerRun))

            appendLine(
                messages.turns(
                    min = state.minTurnsPerRun,
                    avg = state.averageTurnsPerRun,
                    max = state.maxTurnsPerRun
                )
            )
        } else if (state.timesRan == 1) {
            appendLine(messages.turns(state.minTurnsPerRun))
        }

        if (state.withdrawCount > 0) {
            appendLine(messages.timesWithdrew(state.withdrawCount))
        }
    }.trimEnd()

    private fun onScriptExit(e: Exception) = GlobalScope.launch {
        userInterface.setPlayIcon()
        userInterface.isPlayButtonEnabled = false
        userInterface.isPauseButtonVisible = false
        imageLoader.clearSupportCache()

        // Stop recording
        scriptState.let { state ->
            if (state is ScriptState.Started) {
                scriptState = ScriptState.Stopping(state)
            }

            val recording = when (state) {
                ScriptState.Stopped -> null
                is ScriptState.Started -> state.recording
                is ScriptState.Stopping -> state.start.recording
            }

            if (recording != null) {
                // A little bit of delay so the exit message can be recorded
                userInterface.postDelayed(Duration.milliseconds(500)) {
                    try {
                        recording.close()
                    } catch (e: Exception) {
                        val msg = "Failed to stop recording"
                        Toast.makeText(userInterface.Service, msg, Toast.LENGTH_SHORT).show()
                        Timber.error(e) { msg }
                    }
                }
            }
        }

        when (e) {
            is SupportImageMaker.ExitException -> {
                when (e.reason) {
                    SupportImageMaker.ExitReason.NotFound -> {
                        val msg = messages.supportImageMakerNotFound

                        platformImpl.notify(msg)
                        message(messages.scriptExited, msg)
                    }
                    SupportImageMaker.ExitReason.Success -> showSupportImageNamer(userInterface, storageProvider)
                }
            }
            is AutoLottery.ExitException -> {
                val msg = when (e.reason) {
                    AutoLottery.ExitReason.PresentBoxFull -> messages.lotteryPresentBoxFull
                    AutoLottery.ExitReason.ResetDisabled -> messages.lotteryBoxResetIsDisabled
                }

                platformImpl.notify(msg)
                message(messages.scriptExited, msg)
            }
            is AutoGiftBox.ExitException -> {
                val msg = messages.pickedExpStack(e.pickedStacks)

                platformImpl.notify(msg)
                message(messages.scriptExited, msg)
            }
            is AutoFriendGacha.ExitException -> {
                val msg = when (val reason = e.reason) {
                    AutoFriendGacha.ExitReason.InventoryFull -> messages.inventoryFull
                    is AutoFriendGacha.ExitReason.Limit -> messages.timesRolled(reason.count)
                }

                platformImpl.notify(msg)
                message(messages.scriptExited, msg)
            }
            is AutoBattle.ExitException -> {
                if (e.reason != AutoBattle.ExitReason.Abort) {
                    platformImpl.notify(messages.scriptExited)
                }

                val msg = makeExitMessage(e.reason, e.state)
                message(messages.scriptExited, msg)
            }
            is ScriptAbortException -> {
                // user aborted. do nothing
            }
            else -> {
                println(e.messageAndStackTrace)

                val msg = messages.unexpectedError
                platformImpl.notify(msg)

                message(msg, e.messageAndStackTrace, e)
            }
        }

        scriptState = ScriptState.Stopped
        delay(Duration.milliseconds(250))
        userInterface.isPlayButtonEnabled = true
    }

    private fun getEntryPoint(entryPoint: ScriptEntryPoint): EntryPoint =
        when (preferences.scriptMode) {
            ScriptModeEnum.Battle -> entryPoint.battle()
            ScriptModeEnum.FP -> entryPoint.fp()
            ScriptModeEnum.Lottery -> entryPoint.lottery()
            ScriptModeEnum.PresentBox -> entryPoint.giftBox()
            ScriptModeEnum.SupportImageMaker -> entryPoint.supportImageMaker()
        }

    enum class PauseAction {
        Pause, Resume, Toggle
    }

    fun pause(action: PauseAction): Boolean {
        scriptState.let { state ->
            if (state is ScriptState.Started) {
                if (state.paused && action != PauseAction.Pause) {
                    userInterface.setPauseIcon()
                    state.entryPoint.exitManager.resume()

                    state.paused = false

                    return true
                } else if (!state.paused && action != PauseAction.Resume) {
                    userInterface.setResumeIcon()
                    state.entryPoint.exitManager.pause()

                    state.paused = true

                    return true
                }
            }
        }

        return false
    }

    fun startScript(
        context: Context,
        screenshotService: IScreenshotService,
        componentBuilder: ScriptComponentBuilder
    ) {
        if (scriptState !is ScriptState.Stopped) {
            return
        }

        userInterface.isPlayButtonEnabled = false

        val scriptComponent = componentBuilder
            .screenshotService(screenshotService)
            .build()

        val hiltEntryPoint = EntryPoints.get(scriptComponent, ScriptEntryPoint::class.java)
        val entryPointProvider = { getEntryPoint(hiltEntryPoint) }

        val otherMode = hiltEntryPoint.autoDetect().get()

        scriptPicker(context, otherMode) {
            runEntryPoint(screenshotService, entryPointProvider)
        }
    }

    fun stopScript() {
        scriptState.let { state ->
            if (state is ScriptState.Started) {
                userInterface.isPauseButtonVisible = false
                userInterface.isPlayButtonEnabled = false
                scriptState = ScriptState.Stopping(state)
                state.entryPoint.stop()
            }
        }
    }

    private fun runEntryPoint(screenshotService: IScreenshotService, entryPointProvider: () -> EntryPoint) {
        if (scriptState !is ScriptState.Stopped) {
            return
        }

        val recording = try {
            if (preferences.recordScreen) {
                screenshotService.startRecording()
            } else null
        } catch (e: Exception) {
            val msg = userInterface.Service.getString(R.string.cannot_start_recording)
            Timber.error(e) { msg }
            Toast.makeText(userInterface.Service, msg, Toast.LENGTH_SHORT).show()

            null
        }

        val entryPoint = entryPointProvider()

        scriptState = ScriptState.Started(entryPoint, recording)

        entryPoint.scriptExitListener = { onScriptExit(it) }

        userInterface.apply {
            setStopIcon()
            setPauseIcon()
            isPauseButtonVisible = true

            if (recording != null) {
                showAsRecording()
            }
        }

        entryPoint.run()
    }

    private fun scriptPicker(
        context: Context,
        detectedMode: ScriptModeEnum,
        entryPointRunner: () -> Unit
    ) {
        var dialog: AlertDialog? = null

        val composeView = FakedComposeView(context) {
            ScriptLauncher(
                scriptMode = detectedMode,
                onResponse = {
                    dialog?.hide()
                    onScriptLauncherResponse(it, entryPointRunner)
                },
                prefs = preferences
            )
        }

        dialog = showOverlayDialog(context) {
            setView(composeView.view)

            setOnDismissListener {
                userInterface.isPlayButtonEnabled = true
                composeView.close()
            }
        }
    }

    private fun onScriptLauncherResponse(resp: ScriptLauncherResponse, entryPointRunner: () -> Unit) {
        userInterface.isPlayButtonEnabled = true

        preferences.scriptMode = when (resp) {
            ScriptLauncherResponse.Cancel -> return
            is ScriptLauncherResponse.FP -> {
                preferences.shouldLimitFP = resp.limit != null
                resp.limit?.let { preferences.limitFP = it }

                ScriptModeEnum.FP
            }
            is ScriptLauncherResponse.Lottery -> {
                preferences.preventLotteryBoxReset = resp.preventBoxReset

                ScriptModeEnum.Lottery
            }
            is ScriptLauncherResponse.GiftBox -> {
                preferences.maxGoldEmberSetSize = resp.maxGoldEmberStackSize

                ScriptModeEnum.PresentBox
            }
            ScriptLauncherResponse.SupportImageMaker -> ScriptModeEnum.SupportImageMaker
            is ScriptLauncherResponse.Battle -> {
                preferences.selectedBattleConfig = resp.config

                preferences.refill.updateResources(resp.refillResources)
                preferences.refill.repetitions = resp.refillCount

                preferences.refill.shouldLimitRuns = resp.limitRuns != null
                resp.limitRuns?.let { preferences.refill.limitRuns = it }

                preferences.refill.shouldLimitMats = resp.limitMats != null
                resp.limitMats?.let { preferences.refill.limitMats = it }

                preferences.waitAPRegen = resp.waitApRegen

                ScriptModeEnum.Battle
            }
        }

        if (resp !is ScriptLauncherResponse.Cancel) {
            userInterface.postDelayed(Duration.milliseconds(500)) {
                entryPointRunner()
            }
        }
    }
}

val ScriptModeEnum.stringRes
    get() =
        when (this) {
            ScriptModeEnum.Battle -> R.string.p_script_mode_battle
            ScriptModeEnum.FP -> R.string.p_script_mode_fp
            ScriptModeEnum.Lottery -> R.string.p_script_mode_lottery
            ScriptModeEnum.PresentBox -> R.string.p_script_mode_gift_box
            ScriptModeEnum.SupportImageMaker -> R.string.p_script_mode_support_image_maker
        }