package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.widget.Toast
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.di.script.ScriptComponentBuilder
import com.mathewsachin.fategrandautomata.di.script.ScriptEntryPoint
import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.entrypoints.SupportImageMaker
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
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
import kotlin.time.milliseconds

@ServiceScoped
class ScriptManager @Inject constructor(
    val userInterface: ScriptRunnerUserInterface,
    val imageLoader: ImageLoader,
    val preferences: IPreferences,
    val storageProvider: StorageProvider,
    val platformImpl: IPlatformImpl,
    val messages: IScriptMessages
) {
    var scriptState: ScriptState = ScriptState.Stopped
        private set

    // Show message box synchronously
    suspend fun message(Title: String, Message: String, Error: Exception? = null): Unit = suspendCancellableCoroutine {
        platformImpl.messageBox(Title, Message, Error) {
            it.resume(Unit)
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
                userInterface.postDelayed(500.milliseconds) {
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
                showSupportImageNamer(userInterface, storageProvider)
            }
            is ScriptAbortException -> {
                if (e.message.isNotBlank()) {
                    message(messages.scriptExited, e.message)
                }
            }
            is ScriptExitException -> {
                // Show the message box only if there is some message
                if (e.message.isNotBlank()) {
                    val msg = messages.scriptExited
                    platformImpl.notify(msg)

                    message(msg, e.message)
                }
            }
            else -> {
                println(e.messageAndStackTrace)

                val msg = messages.unexpectedError
                platformImpl.notify(msg)

                message(msg, e.messageAndStackTrace, e)
            }
        }

        scriptState = ScriptState.Stopped
        delay(250.milliseconds)
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

    sealed class PickerItem(val name: String) {
        class Other(name: String) : PickerItem(name)
        class Battle(val battleConfig: IBattleConfig) : PickerItem(battleConfig.name)
        class PresentBox(val maxGoldEmberSetSize: Int) : PickerItem(maxGoldEmberSetSize.toString())
    }

    val ScriptModeEnum.display
        get() =
            when (this) {
                ScriptModeEnum.Battle -> R.string.p_script_mode_battle
                ScriptModeEnum.FP -> R.string.p_script_mode_fp
                ScriptModeEnum.Lottery -> R.string.p_script_mode_lottery
                ScriptModeEnum.PresentBox -> R.string.p_script_mode_gift_box
                ScriptModeEnum.SupportImageMaker -> R.string.p_script_mode_support_image_maker
            }

    private fun scriptPicker(
        context: Context,
        otherMode: ScriptModeEnum,
        entryPointRunner: () -> Unit
    ) {
        val pickerItems = preferences.battleConfigs
            .map { PickerItem.Battle(it) }
            .let {
                val other = listOf(PickerItem.Other(context.getString(otherMode.display)))

                when (otherMode) {
                    ScriptModeEnum.Battle -> it
                    ScriptModeEnum.PresentBox ->
                        (1..4).map { m -> PickerItem.PresentBox(m) }
                    ScriptModeEnum.FP, ScriptModeEnum.Lottery ->
                        other
                    ScriptModeEnum.SupportImageMaker -> other + it
                }
            }

        val selectedBattleConfig = preferences.selectedBattleConfig

        val initialSelectedIndex =
            when (otherMode) {
                ScriptModeEnum.SupportImageMaker, ScriptModeEnum.Battle ->
                    pickerItems.indexOfFirst { it is PickerItem.Battle && it.battleConfig.id == selectedBattleConfig.id }
                ScriptModeEnum.FP, ScriptModeEnum.Lottery -> 0
                ScriptModeEnum.PresentBox ->
                    pickerItems.indexOfFirst { it is PickerItem.PresentBox && it.maxGoldEmberSetSize == preferences.maxGoldEmberSetSize }
            }.coerceIn(pickerItems.indices)

        var selected = pickerItems[initialSelectedIndex]

        val dialogTitle = if (otherMode == ScriptModeEnum.PresentBox)
            R.string.p_max_gold_ember_set_size
        else R.string.select_script

        showOverlayDialog(context) {
            setTitle(dialogTitle)
                .apply {
                    setSingleChoiceItems(
                        pickerItems.map { it.name }.toTypedArray(),
                        initialSelectedIndex
                    ) { _, choice -> selected = pickerItems[choice] }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    preferences.scriptMode = when (val s = selected) {
                        is PickerItem.Other -> otherMode
                        is PickerItem.PresentBox -> {
                            preferences.maxGoldEmberSetSize = s.maxGoldEmberSetSize

                            ScriptModeEnum.PresentBox
                        }
                        is PickerItem.Battle -> {
                            preferences.selectedBattleConfig = s.battleConfig

                            ScriptModeEnum.Battle
                        }
                    }

                    userInterface.postDelayed(500.milliseconds) {
                        entryPointRunner()
                    }
                }
                .setOnDismissListener {
                    userInterface.isPlayButtonEnabled = true
                }
        }
    }
}