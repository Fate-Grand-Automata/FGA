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
import com.mathewsachin.fategrandautomata.ui.exit.BattleExit
import com.mathewsachin.fategrandautomata.ui.launcher.ScriptLauncher
import com.mathewsachin.fategrandautomata.ui.launcher.ScriptLauncherResponse
import com.mathewsachin.fategrandautomata.ui.support_img_namer.showSupportImageNamer
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.IScreenshotService
import com.mathewsachin.libautomata.ScriptAbortException
import com.mathewsachin.libautomata.messageAndStackTrace
import dagger.hilt.EntryPoints
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.*
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

    private suspend fun showBattleExit(
        context: Context,
        exception: AutoBattle.ExitException
    ) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine<Unit> { continuation ->
            var dialog: AlertDialog? = null

            val composeView = FakedComposeView(context) {
                BattleExit(
                    exception = exception,
                    prefs = preferences,
                    onClose = { dialog?.dismiss() },
                    onCopy = { service.copyToClipboard(exception) }
                )
            }

            dialog = showOverlayDialog(context) {
                setView(composeView.view)

                setOnDismissListener {
                    composeView.close()
                    continuation.resume(Unit)
                }
            }
        }
    }

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
                        Toast.makeText(service, msg, Toast.LENGTH_SHORT).show()
                        Timber.error(e) { msg }
                    }
                }
            }
        }

        val scriptExitedString by lazy { service.getString(R.string.script_exited) }

        when (e) {
            is SupportImageMaker.ExitException -> {
                when (e.reason) {
                    SupportImageMaker.ExitReason.NotFound -> {
                        val msg = service.getString(R.string.support_img_maker_not_found)

                        messages.notify(msg)
                        message(scriptExitedString, msg)
                    }
                    SupportImageMaker.ExitReason.Success -> showSupportImageNamer(userInterface, storageProvider)
                }
            }
            is AutoLottery.ExitException -> {
                val msg = when (e.reason) {
                    AutoLottery.ExitReason.PresentBoxFull -> service.getString(R.string.present_box_full)
                    AutoLottery.ExitReason.ResetDisabled -> service.getString(R.string.lottery_reset_disabled)
                }

                messages.notify(msg)
                message(scriptExitedString, msg)
            }
            is AutoGiftBox.ExitException -> {
                val msg = service.getString(R.string.picked_exp_stacks, e.pickedStacks)

                messages.notify(msg)
                message(scriptExitedString, msg)
            }
            is AutoFriendGacha.ExitException -> {
                val msg = when (val reason = e.reason) {
                    AutoFriendGacha.ExitReason.InventoryFull -> service.getString(R.string.inventory_full)
                    is AutoFriendGacha.ExitReason.Limit -> service.getString(R.string.times_rolled, reason.count)
                }

                messages.notify(msg)
                message(scriptExitedString, msg)
            }
            is AutoBattle.ExitException -> {
                showBattleExit(service, e)
            }
            is ScriptAbortException -> {
                // user aborted. do nothing
            }
            is AutoCEBomb.ExitException -> {
                val msg = "No Level 1 target CE found"

                messages.notify(msg)
                message(scriptExitedString, msg)
            }
            else -> {
                println(e.messageAndStackTrace)

                val msg = service.getString(R.string.unexpected_error)
                messages.notify(msg)

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
            ScriptModeEnum.CEBomb -> entryPoint.ceBomb()
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
            val msg = service.getString(R.string.cannot_start_recording)
            Timber.error(e) { msg }
            Toast.makeText(service, msg, Toast.LENGTH_SHORT).show()

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
                    dialog?.dismiss()
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
            is ScriptLauncherResponse.CEBomb -> {
                preferences.ceBombTarget = resp.target

                ScriptModeEnum.CEBomb
            }
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