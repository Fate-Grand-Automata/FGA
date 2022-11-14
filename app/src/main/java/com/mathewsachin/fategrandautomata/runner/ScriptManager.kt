package com.mathewsachin.fategrandautomata.runner

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
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
import com.mathewsachin.libautomata.ScreenshotService
import com.mathewsachin.libautomata.ScriptAbortException
import dagger.hilt.EntryPoints
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.milliseconds

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

    private suspend fun showBattleExit(
        context: Context,
        exception: AutoBattle.ExitException
    ) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
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

        scriptState.let { state ->
            if (state is ScriptState.Started) {
                scriptState = ScriptState.Stopping(state)
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
                val msg = when (val reason = e.reason) {
                    is AutoGiftBox.ExitReason.CannotSelectAnyMore -> context.getString(R.string.picked_exp_stacks, reason.pickedStacks)
                    AutoGiftBox.ExitReason.NoEmbersFound -> context.getString(R.string.no_embers_found)
                }

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
        delay(250.milliseconds)
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
            return false
        }
    }

    private fun updateGameServer() {
        val server = prefsCore.gameServerRaw.get()

        preferences.gameServer =
            if (server == PrefsCore.GameServerAutoDetect)
                (TapperService.instance?.detectedFgoServer ?: GameServerEnum.En).also {
                    Timber.d("Using auto-detected Game Server: $it")
                }
            else try {
                enumValueOf<GameServerEnum>(server).also {
                    Timber.d("Using Game Server: $it")
                }
            } catch (e: Exception) {
                Timber.e(e, "Game Server: Falling back to NA")

                GameServerEnum.En
            }
    }

    fun startScript(
        context: Context,
        screenshotService: ScreenshotService,
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
        val detectedMode = hiltEntryPoint.autoDetect().get()

        scope.launch {
            val resp = scriptPicker(context, detectedMode)

            uiStateHolder.isPlayButtonEnabled = true
            launcherResponseHandler.handle(resp)

            if (resp !is ScriptLauncherResponse.Cancel) {
                delay(500)
                runEntryPoint(
                    entryPointProvider = { getEntryPoint(hiltEntryPoint) }
                )
            }
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

    private fun runEntryPoint(entryPointProvider: () -> EntryPoint) {
        if (scriptState !is ScriptState.Stopped) {
            return
        }

        val entryPoint = entryPointProvider().apply {
            scriptExitListener = { onScriptExit(it) }
        }

        scriptState = ScriptState.Started(entryPoint)
        uiStateHolder.uiState = ScriptRunnerUIState.Running

        entryPoint.run()
    }

    private suspend fun scriptPicker(
        context: Context,
        detectedMode: ScriptModeEnum
    ) = withContext(Dispatchers.Main) {
        suspendCoroutine { continuation ->

            var dialog: DialogInterface? = null

            val composeView = FakedComposeView(context) {
                ScriptLauncher(
                    scriptMode = detectedMode,
                    onResponse = {
                        continuation.resume(it)
                        dialog?.dismiss()
                    },
                    prefs = preferences
                )
            }

            dialog = showOverlayDialog(context) {
                setView(composeView.view)

                setOnDismissListener {
                    uiStateHolder.isPlayButtonEnabled = true
                    composeView.close()
                    try {
                        continuation.resume(ScriptLauncherResponse.Cancel)
                    } catch (e: IllegalStateException) {
                        // Ignore exception on resuming twice
                    }
                }
            }
        }
    }

    fun showStatus(status: Exception) {
        if (status is AutoBattle.ExitException) {
            scope.launch { showBattleExit(service, status) }
        }
    }
}