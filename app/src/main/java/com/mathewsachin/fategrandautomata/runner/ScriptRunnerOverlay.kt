package com.mathewsachin.fategrandautomata.runner

import android.annotation.SuppressLint
import android.app.Service
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import com.mathewsachin.fategrandautomata.di.script.ScriptComponentBuilder
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.ui.highlight.HighlightManager
import com.mathewsachin.fategrandautomata.ui.runner.ScriptRunnerUI
import com.mathewsachin.fategrandautomata.ui.runner.ScriptRunnerUIAction
import com.mathewsachin.fategrandautomata.ui.runner.ScriptRunnerUIStateHolder
import com.mathewsachin.fategrandautomata.util.DisplayHelper
import com.mathewsachin.fategrandautomata.util.FakedComposeView
import com.mathewsachin.fategrandautomata.util.ScriptState
import com.mathewsachin.fategrandautomata.util.overlayType
import com.mathewsachin.libautomata.Location
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject
import kotlin.math.roundToInt

@ServiceScoped
class ScriptRunnerOverlay @Inject constructor(
    private val service: Service,
    private val display: DisplayHelper,
    private val windowManager: WindowManager,
    private val highlightManager: HighlightManager,
    private val prefsCore: PrefsCore,
    private val uiStateHolder: ScriptRunnerUIStateHolder,
    private val scriptManager: ScriptManager,
    private val screenshotServiceHolder: ScreenshotServiceHolder,
    private val scriptComponentBuilder: ScriptComponentBuilder
) {
    private val layout: ComposeView

    private val scriptCtrlBtnLayoutParams = WindowManager.LayoutParams().apply {
        type = overlayType
        format = PixelFormat.TRANSLUCENT
        flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        @SuppressLint("RtlHardcoded")
        gravity = Gravity.LEFT or Gravity.TOP
        x = 0
        y = 0
        windowAnimations = android.R.style.Animation_Toast
    }

    init {
        require(service is ScriptRunnerService)

        layout = FakedComposeView(service) {
            ScriptRunnerUI(
                state = uiStateHolder.uiState,
                updateState = { act(it) },
                enabled = uiStateHolder.isPlayButtonEnabled,
                onDrag = { x, y -> onDrag(x, y) }
            )
        }.view

        // By default put the button on bottom-left corner
        val m = display.metrics
        scriptCtrlBtnLayoutParams.y = maxOf(m.widthPixels, m.heightPixels)
    }

    private fun setPlayBtnLocation(x: Int, y: Int) {
        val dragMaxLoc = getMaxBtnCoordinates()

        scriptCtrlBtnLayoutParams.x = x.coerceIn(0, dragMaxLoc.x)
        scriptCtrlBtnLayoutParams.y = y.coerceIn(0, dragMaxLoc.y)
    }

    private fun onDrag(x: Float, y: Float) {
        val targetX = scriptCtrlBtnLayoutParams.x + x.roundToInt()
        val targetY = scriptCtrlBtnLayoutParams.y + y.roundToInt()

        setPlayBtnLocation(targetX, targetY)

        windowManager.updateViewLayout(layout, scriptCtrlBtnLayoutParams)
    }

    private fun restorePlayButtonLocation() {
        prefsCore.playBtnLocation.get().let { location ->
            // top-left corner is fallback value. ignore
            if (location != Location()) {
                setPlayBtnLocation(location.x, location.y)
            }
        }
    }

    private fun savePlayButtonLocation() {
        prefsCore.playBtnLocation.set(
            Location(scriptCtrlBtnLayoutParams.x, scriptCtrlBtnLayoutParams.y)
        )
    }

    private var shown = false

    fun show() {
        if (!shown && Settings.canDrawOverlays(service)) {
            restorePlayButtonLocation()

            highlightManager.show()
            windowManager.addView(layout, scriptCtrlBtnLayoutParams)

            shown = true
        }
    }

    fun hide() {
        if (shown && Settings.canDrawOverlays(service)) {
            savePlayButtonLocation()

            windowManager.removeView(layout)
            highlightManager.hide()

            shown = false
        }
    }

    /**
     * Returns the maximum values of (X, Y) coordinates the [layout] can take.
     */
    private fun getMaxBtnCoordinates(): Location {
        val m = display.metrics

        val x = m.widthPixels - layout.measuredWidth
        val y = m.heightPixels - layout.measuredHeight

        return Location(x, y)
    }

    private fun act(action: ScriptRunnerUIAction) {
        when (action) {
            ScriptRunnerUIAction.Pause, ScriptRunnerUIAction.Resume -> {
                scriptManager.pause(ScriptManager.PauseAction.Toggle)
            }
            ScriptRunnerUIAction.Start -> {
                if (scriptManager.scriptState is ScriptState.Stopped) {
                    screenshotServiceHolder.screenshotService?.let {
                        scriptManager.startScript(service, it, scriptComponentBuilder)
                    }
                }
            }
            ScriptRunnerUIAction.Stop -> {
                if (scriptManager.scriptState is ScriptState.Started) {
                    scriptManager.stopScript()
                }
            }
            is ScriptRunnerUIAction.Status -> {
                scriptManager.showStatus(action.status)
            }
        }
    }
}