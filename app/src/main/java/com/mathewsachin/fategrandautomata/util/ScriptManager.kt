package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.setPadding
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerDialog
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.dagger.script.ScreenshotModule
import com.mathewsachin.fategrandautomata.dagger.service.ScriptRunnerServiceComponent
import com.mathewsachin.fategrandautomata.dagger.service.ServiceScope
import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoFriendGacha
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoLottery
import com.mathewsachin.fategrandautomata.scripts.entrypoints.SupportImageMaker
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.support_img_namer.showSupportImageNamer
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.IScreenshotService
import java.io.File
import javax.inject.Inject
import kotlin.time.seconds

data class ScriptLaunchParams @Inject constructor(
    val exitManager: ExitManager,
    val fgAutomataApi: IFGAutomataApi
)

@ServiceScope
class ScriptManager @Inject constructor(
    val userInterface: ScriptRunnerUserInterface,
    val imageLoader: ImageLoader,
    val preferences: IPreferences,
    val storageDirs: StorageDirs,
    val platformImpl: IPlatformImpl
) {
    var scriptStarted = false
        private set

    private var entryPoint: EntryPoint? = null
    private var recording: AutoCloseable? = null

    private fun onScriptExit() {
        userInterface.setPlayIcon()

        imageLoader.clearSupportCache()

        entryPoint = null
        scriptStarted = false

        val rec = recording
        if (rec != null) {
            // record for 2 seconds more to show things like error messages
            userInterface.postDelayed(2.seconds) { rec.close() }
        }

        recording = null
    }

    private fun getSupportImagTempDir(context: Context): File {
        val dir = File(context.cacheDir, "support")

        if (!dir.exists()) {
            dir.mkdirs()
        }

        return dir
    }

    private fun getEntryPoint(
        exitManager: ExitManager,
        fgAutomataApi: IFGAutomataApi,
        context: Context
    ): EntryPoint = when (preferences.scriptMode) {
        ScriptModeEnum.Lottery -> AutoLottery(exitManager, platformImpl, fgAutomataApi)
        ScriptModeEnum.FriendGacha -> AutoFriendGacha(exitManager, platformImpl, fgAutomataApi)
        ScriptModeEnum.SupportImageMaker -> {
            val tempDir = getSupportImagTempDir(context)

            SupportImageMaker(
                tempDir,
                exitManager,
                platformImpl,
                fgAutomataApi
            ) {
                supportImgMakerCallback(tempDir)
            }
        }
        else -> AutoBattle(exitManager, platformImpl, fgAutomataApi)
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private fun supportImgMakerCallback(tempDir: File) {
        handler.post { showSupportImageNamer(userInterface, storageDirs, tempDir) }
    }

    fun startScript(
        context: Context,
        screenshotService: IScreenshotService,
        component: ScriptRunnerServiceComponent
    ) {
        if (scriptStarted) {
            return
        }

        val (exitManager, fgAutomataApi) = component
            .scriptComponent()
            .screenshotModule(
                ScreenshotModule(
                    screenshotService
                )
            )
            .build()
            .getScriptLaunchParams()

        getEntryPoint(exitManager, fgAutomataApi, context).apply {
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

        if (preferences.recordScreen) {
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