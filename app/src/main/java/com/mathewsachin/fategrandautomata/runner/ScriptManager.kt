package com.mathewsachin.fategrandautomata.runner

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.TapperService
import com.mathewsachin.fategrandautomata.di.script.ScriptComponentBuilder
import com.mathewsachin.fategrandautomata.di.script.ScriptEntryPoint
import com.mathewsachin.fategrandautomata.di.service.ServiceCoroutineScope
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.entrypoints.*
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.exit.BattleExit
import com.mathewsachin.fategrandautomata.ui.launcher.ScriptLauncher
import com.mathewsachin.fategrandautomata.ui.launcher.ScriptLauncherResponse
import com.mathewsachin.fategrandautomata.ui.runner.ScriptRunnerUIState
import com.mathewsachin.fategrandautomata.ui.runner.ScriptRunnerUIStateHolder
import com.mathewsachin.fategrandautomata.ui.support_img_namer.showSupportImageNamer
import com.mathewsachin.fategrandautomata.util.*
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.IScreenshotService
import com.mathewsachin.libautomata.ScriptAbortException
import dagger.hilt.EntryPoints
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.*
import timber.log.Timber
import timber.log.debug
import timber.log.error
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration

@ServiceScoped
class ScriptManager @Inject constructor(
    private val service: Service,
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
    private val preferences: IPreferences,
    private val prefsCore: PrefsCore,
    private val storageProvider: StorageProvider,
    private val messages: ScriptMessages,
    private val uiStateHolder: ScriptRunnerUIStateHolder,
    private val clipboardManager: ClipboardManager,
    private val messageBox: ScriptRunnerMessageBox,
    @ServiceCoroutineScope private val scope: CoroutineScope,
    private val launcherResponseHandler: ScriptLauncherResponseHandler
) {
    var scriptState: ScriptState = ScriptState.Stopped
        private set

    private fun runDelayedOnUiThread(
        delay: Duration = Duration.milliseconds(500),
        action: suspend () -> Unit
    ) {
        scope.launch {
            delay(500)

            withContext(Dispatchers.Main) {
                action()
            }
        }
    }

    private suspend fun showBattleExit(
        context: Context,
        exception: AutoBattle.ExitException
    ) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine<Unit> { continuation ->
            var dialog: DialogInterface? = null

            val composeView = FakedComposeView(context) {
                BattleExit(
                    exception = exception,
                    prefs = preferences,
                    prefsCore = prefsCore,
                    onClose = { dialog?.dismiss() },
                    onCopy = { clipboardManager.set(context, exception) }
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

    private fun onScriptExit(e: Exception) = scope.launch {
        uiStateHolder.uiState = ScriptRunnerUIState.Idle
        uiStateHolder.isPlayButtonEnabled = false
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
                runDelayedOnUiThread {
                    try {
                        recording.close()
                    } catch (e: Exception) {
                        val msg = "Failed to stop recording"
                        Toast.makeText(service, msg, Toast.LENGTH_SHORT).show()
                        Timber.error(e) { msg }
                    }

                    uiStateHolder.isRecording = false
                }
            }
        }

        val scriptExitedString by lazy { context.getString(R.string.script_exited) }

        when (e) {
            is SupportImageMaker.ExitException -> {
                when (e.reason) {
                    SupportImageMaker.ExitReason.NotFound -> {
                        val msg = context.getString(R.string.support_img_maker_not_found)

                        messages.notify(msg)
                        messageBox.show(scriptExitedString, msg)
                    }
                    SupportImageMaker.ExitReason.Success -> showSupportImageNamer(context, storageProvider)
                }
            }
            is AutoLottery.ExitException -> {
                val msg = when (e.reason) {
                    AutoLottery.ExitReason.PresentBoxFull -> context.getString(R.string.present_box_full)
                    AutoLottery.ExitReason.ResetDisabled -> context.getString(R.string.lottery_reset_disabled)
                }

                messages.notify(msg)
                messageBox.show(scriptExitedString, msg)
            }
            is AutoGiftBox.ExitException -> {
                val msg = context.getString(R.string.picked_exp_stacks, e.pickedStacks)

                messages.notify(msg)
                messageBox.show(scriptExitedString, msg)
            }
            is AutoFriendGacha.ExitException -> {
                val msg = when (val reason = e.reason) {
                    AutoFriendGacha.ExitReason.InventoryFull -> context.getString(R.string.inventory_full)
                    is AutoFriendGacha.ExitReason.Limit -> context.getString(R.string.times_rolled, reason.count)
                }

                messages.notify(msg)
                messageBox.show(scriptExitedString, msg)
            }
            is AutoBattle.ExitException -> {
                if (e.reason !is AutoBattle.ExitReason.Abort) {
                    messages.notify(scriptExitedString)
                }

                showBattleExit(service, e)
            }
            is ScriptAbortException -> {
                // user aborted. do nothing
            }
            is AutoCEBomb.ExitException -> {
                val msg = when (e.reason) {
                    AutoCEBomb.ExitReason.NoSuitableTargetCEFound -> "No suitable target CE found"
                }

                messages.notify(msg)
                messageBox.show(scriptExitedString, msg)
            }
            is KnownException -> {
                messages.notify(scriptExitedString)

                messageBox.show(scriptExitedString, e.reason.msg)
            }
            else -> {
                println(e.messageAndStackTrace)

                val msg = context.getString(R.string.unexpected_error)
                messages.notify(msg)

                messageBox.show(msg, e.messageAndStackTrace, e)
            }
        }

        scriptState = ScriptState.Stopped
        delay(Duration.milliseconds(250))
        uiStateHolder.isPlayButtonEnabled = true
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
                    uiStateHolder.uiState = ScriptRunnerUIState.Running
                    state.entryPoint.exitManager.resume()

                    state.paused = false

                    return true
                } else if (!state.paused && action != PauseAction.Resume) {
                    state.entryPoint.exitManager.pause()
                    uiStateHolder.uiState = ScriptRunnerUIState.Paused(state.entryPoint.pausedStatus())

                    state.paused = true

                    return true
                }
            }
        }

        return false
    }

    private fun updateGameServer() {
        val server = prefsCore.gameServerRaw.get()

        preferences.gameServer =
            if (server == PrefsCore.GameServerAutoDetect)
                (TapperService.instance?.detectedFgoServer ?: GameServerEnum.En).also {
                    Timber.debug { "Using auto-detected Game Server: $it" }
                }
            else try {
                enumValueOf<GameServerEnum>(server).also {
                    Timber.debug { "Using Game Server: $it" }
                }
            } catch (e: Exception) {
                Timber.error(e) { "Game Server: Falling back to NA" }

                GameServerEnum.En
            }
    }

    fun startScript(
        context: Context,
        screenshotService: IScreenshotService,
        componentBuilder: ScriptComponentBuilder
    ) {
        updateGameServer()

        if (scriptState !is ScriptState.Stopped) {
            return
        }

        uiStateHolder.isPlayButtonEnabled = false

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
                uiStateHolder.isPlayButtonEnabled = false
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
            val msg = context.getString(R.string.cannot_start_recording)
            Timber.error(e) { msg }
            Toast.makeText(service, msg, Toast.LENGTH_SHORT).show()

            null
        }

        val entryPoint = entryPointProvider()

        scriptState = ScriptState.Started(entryPoint, recording)

        entryPoint.scriptExitListener = { onScriptExit(it) }

        uiStateHolder.uiState = ScriptRunnerUIState.Running

        if (recording != null) {
            uiStateHolder.isRecording = true
        }

        entryPoint.run()
    }

    private fun scriptPicker(
        context: Context,
        detectedMode: ScriptModeEnum,
        entryPointRunner: () -> Unit
    ) {
        var dialog: DialogInterface? = null

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
                uiStateHolder.isPlayButtonEnabled = true
                composeView.close()
            }
        }
    }

    private fun onScriptLauncherResponse(resp: ScriptLauncherResponse, entryPointRunner: () -> Unit) {
        uiStateHolder.isPlayButtonEnabled = true

        launcherResponseHandler.handle(resp)

        if (resp !is ScriptLauncherResponse.Cancel) {
            runDelayedOnUiThread {
                entryPointRunner()
            }
        }
    }

    fun showStatus(status: Exception) {
        if (status is AutoBattle.ExitException) {
            scope.launch { showBattleExit(service, status) }
        }
    }
}