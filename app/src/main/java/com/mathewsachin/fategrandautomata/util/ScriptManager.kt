package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.accessibility.showOverlayDialog
import com.mathewsachin.fategrandautomata.di.script.ScriptComponentBuilder
import com.mathewsachin.fategrandautomata.di.script.ScriptEntryPoint
import com.mathewsachin.fategrandautomata.scripts.SupportImageMakerExitException
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.support_img_namer.showSupportImageNamer
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.IScreenshotService
import com.mathewsachin.libautomata.ScriptAbortException
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

    fun stopScript(reason: ScriptAbortException) {
        scriptState.let { state ->
            if (state is ScriptState.Started) {
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
        val autoSkillItems = preferences.autoSkillPreferences
        val initialSelectedIndex = autoSkillItems.indexOfFirst { it.id == selected.id }

        fun DialogInterface.setOkBtnEnabled(enable: Boolean) {
            if (this is AlertDialog) {
                getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = enable
            }
        }

        showOverlayDialog(context) {
            setTitle(R.string.select_auto_skill_config)
                .apply {
                    if (autoSkillItems.isNotEmpty()) {
                        setSingleChoiceItems(
                            autoSkillItems.map { it.name }.toTypedArray(),
                            initialSelectedIndex
                        ) { dialog, choice ->
                            selected = autoSkillItems[choice]
                            dialog.setOkBtnEnabled(true)
                        }
                    } else setMessage(R.string.no_auto_skill_configs)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    preferences.selectedAutoSkillConfig = selected
                    entryPointRunner()
                }
        }.setOkBtnEnabled(initialSelectedIndex != -1)
    }
}