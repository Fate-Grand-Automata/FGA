package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.setPadding
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerDialog
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.di.script.ScriptComponentBuilder
import com.mathewsachin.fategrandautomata.di.script.ScriptEntryPoint
import com.mathewsachin.fategrandautomata.scripts.SupportImageMakerExitException
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.support_img_namer.showSupportImageNamer
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.IScreenshotService
import dagger.hilt.EntryPoints
import dagger.hilt.android.scopes.ServiceScoped
import mu.KotlinLogging
import javax.inject.Inject
import kotlin.time.seconds

private val logger = KotlinLogging.logger {}

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
        userInterface.isPauseButtonVisibile = false

        imageLoader.clearSupportCache()

        scriptState.let { prevState ->
            if (prevState is ScriptState.Started && prevState.recording != null) {
                // record for 2 seconds more to show things like error messages
                userInterface.postDelayed(2.seconds) {
                    prevState.recording.close()
                }
            }
        }

        scriptState = ScriptState.Stopped

        if (e is SupportImageMakerExitException) {
            showSupportImageNamer(userInterface, storageDirs)
        }
    }

    private fun getEntryPoint(entryPoint: ScriptEntryPoint): EntryPoint =
        when (preferences.scriptMode) {
            ScriptModeEnum.Lottery -> entryPoint.lottery()
            ScriptModeEnum.FriendGacha -> entryPoint.friendGacha()
            ScriptModeEnum.SupportImageMaker -> entryPoint.supportImageMaker()
            ScriptModeEnum.GiftBox -> entryPoint.giftBox()
            else -> entryPoint.battle()
        }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun pauseScript() {
        scriptState.let { state ->
            if (state is ScriptState.Started) {
                if (!state.paused) {
                    userInterface.setResumeIcon()
                    state.entryPoint.exitManager.pause()

                    state.paused = true
                }
            }
        }
    }

    fun resumeScript() {
        scriptState.let { state ->
            if (state is ScriptState.Started) {
                if (state.paused) {
                    userInterface.setPauseIcon()
                    state.entryPoint.exitManager.resume()

                    state.paused = false
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

        val scriptComponent = componentBuilder
            .screenshotService(screenshotService)
            .build()

        val hiltEntryPoint = EntryPoints.get(scriptComponent, ScriptEntryPoint::class.java)
        val entryPointProvider = { getEntryPoint(hiltEntryPoint) }

        if (preferences.scriptMode == ScriptModeEnum.Battle) {
            autoSkillPicker(context) {
                runEntryPoint(screenshotService, entryPointProvider)
            }
        } else runEntryPoint(screenshotService, entryPointProvider)
    }

    fun stopScript() {
        scriptState.let { state ->
            if (state is ScriptState.Started) {
                state.entryPoint.stop()
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
            logger.error(msg, e)
            Toast.makeText(userInterface.Service, msg, Toast.LENGTH_SHORT).show()

            null
        }

        val entryPoint = entryPointProvider()

        scriptState = ScriptState.Started(entryPoint, recording)

        entryPoint.scriptExitListener = { onScriptExit(it) }

        userInterface.setStopIcon()
        if (preferences.canPauseScript) {
            userInterface.setPauseIcon()
            userInterface.isPauseButtonVisibile = true
        }

        if (recording != null) {
            userInterface.showAsRecording()
        }

        entryPoint.run()
    }

    private fun autoSkillPicker(context: Context, entryPointRunner: () -> Unit) {
        var selected = preferences.selectedAutoSkillConfig
        var isSelected = false

        val autoSkillItems = preferences.autoSkillPreferences

        val radioGroup = RadioGroup(context).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(20)
        }

        for ((index, item) in autoSkillItems.withIndex()) {
            val radioBtn = RadioButton(context).apply {
                text = item.name
                id = index
            }

            radioGroup.addView(radioBtn)

            if (selected.id == item.id) {
                radioGroup.check(index)
                isSelected = true
            }
        }

        ScriptRunnerDialog(userInterface).apply {
            setTitle(context.getString(R.string.select_auto_skill_config))
            setPositiveButton(context.getString(android.R.string.ok)) {
                if (autoSkillItems.isNotEmpty()) {
                    val selectedIndex = radioGroup.checkedRadioButtonId
                    if (selectedIndex in autoSkillItems.indices) {
                        selected = autoSkillItems[selectedIndex]

                        preferences.selectedAutoSkillConfig = selected
                    }

                    entryPointRunner()
                }
            }.apply {
                if (autoSkillItems.isNotEmpty()) {
                    isEnabled = isSelected

                    radioGroup.children.forEach {
                        it.setOnClickListener { isEnabled = true }
                    }
                }
            }
            if (autoSkillItems.isNotEmpty()) {
                setNegativeButton(context.getString(android.R.string.cancel)) { }
            }

            if (autoSkillItems.isEmpty()) {
                setMessage(context.getString(R.string.no_auto_skill_configs))
            } else setView(radioGroup)

            show()
        }
    }
}