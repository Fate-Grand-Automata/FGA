package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.setPadding
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerDialog
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.prefs.Preferences
import com.mathewsachin.fategrandautomata.scripts.clearSupportCache
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoFriendGacha
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoLottery
import com.mathewsachin.fategrandautomata.scripts.entrypoints.SupportImageMaker
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.ui.support_img_namer.showSupportImageNamer
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.IScreenshotService
import kotlin.time.seconds

class ScriptManager(val userInterface: ScriptRunnerUserInterface) {
    var scriptStarted = false
        private set

    private var entryPoint: EntryPoint? = null
    private var recording: AutoCloseable? = null

    private fun onScriptExit() {
        userInterface.setPlayIcon()

        clearSupportCache()

        entryPoint = null
        scriptStarted = false

        val rec = recording
        if (rec != null) {
            // record for 2 seconds more to show things like error messages
            userInterface.postDelayed(2.seconds) { rec.close() }
        }

        recording = null
    }

    private fun getEntryPoint(): EntryPoint = when (Preferences.scriptMode) {
        ScriptModeEnum.Lottery -> AutoLottery()
        ScriptModeEnum.FriendGacha -> AutoFriendGacha()
        ScriptModeEnum.SupportImageMaker -> SupportImageMaker(::supportImgMakerCallback)
        else -> AutoBattle()
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private fun supportImgMakerCallback() {
        handler.post { showSupportImageNamer(userInterface) }
    }

    fun startScript(context: Context, screenshotService: IScreenshotService) {
        if (scriptStarted) {
            return
        }

        getEntryPoint().apply {
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
        if (!scriptStarted) {
            return
        }

        entryPoint?.let {
            it.scriptExitListener = null
            it.stop()
        }

        onScriptExit()
    }

    private fun runEntryPoint(EntryPoint: EntryPoint, screenshotService: IScreenshotService) {
        if (scriptStarted) {
            return
        }

        scriptStarted = true
        entryPoint = EntryPoint

        if (Preferences.recordScreen) {
            recording = screenshotService.startRecording()
        }

        EntryPoint.scriptExitListener = ::onScriptExit

        userInterface.setStopIcon()
        if (recording != null) {
            userInterface.showAsRecording()
        }

        EntryPoint.run()
    }

    private fun autoSkillPicker(context: Context, EntryPointRunner: () -> Unit) {
        var selected = Preferences.selectedAutoSkillConfig

        val autoSkillItems = getAutoSkillEntries()

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

                    Preferences.selectedAutoSkillConfig = selected
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