package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.setPadding
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerDialog
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.dagger.script.ScreenshotModule
import com.mathewsachin.fategrandautomata.dagger.script.ScriptComponent
import com.mathewsachin.fategrandautomata.dagger.service.ScriptRunnerServiceComponent
import com.mathewsachin.fategrandautomata.dagger.service.ServiceScope
import com.mathewsachin.fategrandautomata.scripts.SupportImageMakerExitException
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.support_img_namer.showSupportImageNamer
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.IScreenshotService
import mu.KotlinLogging
import javax.inject.Inject
import kotlin.time.seconds

private val logger = KotlinLogging.logger {}

@ServiceScope
class ScriptManager @Inject constructor(
    val userInterface: ScriptRunnerUserInterface,
    val imageLoader: ImageLoader,
    val preferences: IPreferences,
    val storageDirs: StorageDirs
) {
    var scriptState: ScriptState = ScriptState.Stopped
        private set

    private fun onScriptExit(e: Exception?) {
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
            handler.post { showSupportImageNamer(userInterface, storageDirs) }
        }
    }

    private fun getEntryPoint(component: ScriptComponent): EntryPoint =
        when (preferences.scriptMode) {
            ScriptModeEnum.Lottery -> component.lottery()
            ScriptModeEnum.FriendGacha -> component.friendGacha()
            ScriptModeEnum.SupportImageMaker -> component.supportImageMaker()
            else -> component.battle()
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
        component: ScriptRunnerServiceComponent
    ) {
        if (scriptState is ScriptState.Started) {
            return
        }

        val scriptComponent = component
            .scriptComponent()
            .screenshotModule(
                ScreenshotModule(
                    screenshotService
                )
            )
            .build()

        getEntryPoint(scriptComponent).apply {
            if (this is AutoBattle) {
                autoSkillPicker(context) {
                    runEntryPoint(this, screenshotService)
                }
            } else {
                runEntryPoint(this, screenshotService)
            }
        }
    }

    fun stopScript() {
        scriptState.let { state ->
            if (state is ScriptState.Started) {
                state.entryPoint.scriptExitListener = { }
                state.entryPoint.stop()

                onScriptExit(null)
            }
        }
    }

    private fun runEntryPoint(EntryPoint: EntryPoint, screenshotService: IScreenshotService) {
        if (scriptState is ScriptState.Started) {
            return
        }

        val recording = try {
            if (preferences.recordScreen) {
                screenshotService.startRecording()
            } else null
        } catch (e: Exception) {
            val msg = "Couldn't start recording"
            logger.error(msg, e)
            Toast.makeText(userInterface.Service, msg, Toast.LENGTH_SHORT).show()

            null
        }

        scriptState = ScriptState.Started(EntryPoint, recording)

        EntryPoint.scriptExitListener = ::onScriptExit

        userInterface.setStopIcon()
        if (preferences.canPauseScript) {
            userInterface.setPauseIcon()
            userInterface.isPauseButtonVisibile = true
        }

        if (recording != null) {
            userInterface.showAsRecording()
        }

        EntryPoint.run()
    }

    private fun autoSkillPicker(context: Context, EntryPointRunner: () -> Unit) {
        var selected = preferences.selectedAutoSkillConfig

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
            }
        }

        ScriptRunnerDialog(userInterface).apply {
            setTitle("Select AutoSkill Config")
            setPositiveButton(context.getString(android.R.string.ok)) {
                val selectedIndex = radioGroup.checkedRadioButtonId
                if (selectedIndex in autoSkillItems.indices) {
                    selected = autoSkillItems[selectedIndex]

                    preferences.selectedAutoSkillConfig = selected
                }

                EntryPointRunner()
            }
            setNegativeButton(context.getString(android.R.string.cancel)) { }

            if (autoSkillItems.isEmpty()) {
                setMessage("No AutoSkill Configs")
            } else setView(radioGroup)

            show()
        }
    }
}