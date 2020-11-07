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
            ScriptModeEnum.Other -> entryPoint.other()
            ScriptModeEnum.Battle -> entryPoint.battle()
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

        scriptPicker(context) {
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
    }

    private fun scriptPicker(context: Context, entryPointRunner: () -> Unit) {
        val selectedBattleConfig = preferences.selectedBattleConfig
        val battleConfigs = preferences.battleConfigs
        val initialSelectedIndex =
            if (preferences.scriptMode == ScriptModeEnum.Battle)
                battleConfigs.indexOfFirst { it.id == selectedBattleConfig.id } + 1
            else 0

        val other = PickerItem.Other(context.getString(R.string.other_scripts))
        val pickerItems = listOf(other) + battleConfigs.map { PickerItem.Battle(it) }
        var selected = pickerItems[initialSelectedIndex]

        showOverlayDialog(context) {
            setTitle(R.string.select_script)
                .apply {
                    setSingleChoiceItems(
                        pickerItems.map { it.name }.toTypedArray(),
                        initialSelectedIndex
                    ) { _, choice -> selected = pickerItems[choice] }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    preferences.scriptMode = when (val s = selected) {
                        is PickerItem.Other -> ScriptModeEnum.Other
                        is PickerItem.Battle -> {
                            preferences.selectedBattleConfig = s.battleConfig

                            ScriptModeEnum.Battle
                        }
                    }

                    entryPointRunner()
                }
                .setOnDismissListener {
                    userInterface.isPlayButtonEnabled = true
                }
        }
    }
}