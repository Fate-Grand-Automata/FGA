package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.di.script.ScriptComponentBuilder
import com.mathewsachin.fategrandautomata.di.script.ScriptEntryPoint
import com.mathewsachin.fategrandautomata.scripts.SupportImageMakerExitException
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.support_img_namer.showSupportImageNamer
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.IScreenshotService
import com.mathewsachin.libautomata.ScriptAbortException
import dagger.hilt.EntryPoints
import dagger.hilt.android.scopes.ServiceScoped
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import kotlin.time.milliseconds

@ServiceScoped
class ScriptManager @Inject constructor(
    val userInterface: ScriptRunnerUserInterface,
    val imageLoader: ImageLoader,
    val preferences: IPreferences,
    val storageDirs: StorageDirs
) {
    var scriptState: ScriptState = ScriptState.Stopped
        private set

    private fun onScriptExit(e: Exception?) = handler.post {
        userInterface.setPlayIcon()
        userInterface.isPauseButtonVisible = false

        imageLoader.clearSupportCache()

        scriptState.let { prevState ->
            if (prevState is ScriptState.Started && prevState.recording != null) {
                prevState.recording.close()
            }
        }

        scriptState = ScriptState.Stopped

        if (e is SupportImageMakerExitException) {
            showSupportImageNamer(userInterface, storageDirs)
        }

        userInterface.postDelayed(250.milliseconds) {
            userInterface.playButtonEnabled(true)
        }
    }

    private fun getEntryPoint(entryPoint: ScriptEntryPoint): EntryPoint =
        when (preferences.scriptMode) {
            ScriptModeEnum.Other -> entryPoint.other()
            ScriptModeEnum.Battle -> entryPoint.battle()
        }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun togglePause() {
        scriptState.let { state ->
            if (state is ScriptState.Started) {
                if (state.paused) {
                    userInterface.setPauseIcon()
                    state.entryPoint.exitManager.resume()

                    state.paused = false
                } else {
                    userInterface.setResumeIcon()
                    state.entryPoint.exitManager.pause()

                    state.paused = true
                }
            }
        }
    }

    fun startScript(
        context: Context,
        screenshotService: IScreenshotService,
        componentBuilder: ScriptComponentBuilder
    ) {
        if (scriptState is ScriptState.Started) {
            return
        }

        userInterface.playButtonEnabled(false)

        val scriptComponent = componentBuilder
            .screenshotService(screenshotService)
            .build()

        val hiltEntryPoint = EntryPoints.get(scriptComponent, ScriptEntryPoint::class.java)
        val entryPointProvider = { getEntryPoint(hiltEntryPoint) }

        autoSkillPicker(context) {
            runEntryPoint(screenshotService, entryPointProvider)
        }
    }

    fun stopScript(reason: ScriptAbortException) {
        scriptState.let { state ->
            if (state is ScriptState.Started) {
                userInterface.isPauseButtonVisible = false
                userInterface.playButtonEnabled(false)
                state.entryPoint.stop(reason)
            }
        }
    }

    private fun runEntryPoint(screenshotService: IScreenshotService, entryPointProvider: () -> EntryPoint) {
        if (scriptState is ScriptState.Started) {
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

        userInterface.setStopIcon()
        if (preferences.canPauseScript) {
            userInterface.setPauseIcon()
            userInterface.isPauseButtonVisible = true
        }

        if (recording != null) {
            userInterface.showAsRecording()
        }

        entryPoint.run()
    }

    sealed class PickerItem(val name: String) {
        class Other(name: String) : PickerItem(name)
        class Battle(val autoSkill: IAutoSkillPreferences) : PickerItem(autoSkill.name)
    }

    private fun autoSkillPicker(context: Context, entryPointRunner: () -> Unit) {
        val selectedAutoSkill = preferences.selectedAutoSkillConfig
        val autoSkillItems = preferences.autoSkillPreferences
        val initialSelectedIndex =
            if (preferences.scriptMode == ScriptModeEnum.Battle)
                autoSkillItems.indexOfFirst { it.id == selectedAutoSkill.id } + 1
            else 0

        val other = PickerItem.Other(context.getString(R.string.other_scripts))
        val pickerItems = listOf(other) + autoSkillItems.map { PickerItem.Battle(it) }
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
                            preferences.selectedAutoSkillConfig = s.autoSkill

                            ScriptModeEnum.Battle
                        }
                    }

                    entryPointRunner()
                }
                .setOnDismissListener {
                    userInterface.playButtonEnabled(true)
                }
        }
    }
}