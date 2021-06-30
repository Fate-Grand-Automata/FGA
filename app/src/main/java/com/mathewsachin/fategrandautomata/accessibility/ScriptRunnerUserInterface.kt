package com.mathewsachin.fategrandautomata.accessibility

import android.annotation.SuppressLint
import android.app.Service
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.postDelayed
import com.mathewsachin.fategrandautomata.ui.highlight.HighlightManager
import com.mathewsachin.fategrandautomata.util.FakedComposeView
import com.mathewsachin.libautomata.Location
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.Duration

@ServiceScoped
class ScriptRunnerUserInterface @Inject constructor(
    val Service: Service,
    val highlightManager: HighlightManager,
    val windowManager: WindowManager
) {
    companion object {
        val overlayType: Int
            get() {
                @Suppress("DEPRECATION")
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else WindowManager.LayoutParams.TYPE_PHONE
            }
    }

    val metrics: DisplayMetrics
        get() {
            val res = DisplayMetrics()

            windowManager.defaultDisplay.getRealMetrics(res)

            return res
        }

    /**
     * Used with MediaProjection so that we only get landscape images,
     * since the frame size can't be changed during a projection.
     */
    val mediaProjectionMetrics: DisplayMetrics
        get() {
            val res = metrics

            // Retrieve images in Landscape
            if (res.heightPixels > res.widthPixels) {
                res.let {
                    val temp = it.widthPixels
                    it.widthPixels = it.heightPixels
                    it.heightPixels = temp
                }
            }

            return res
        }

    private val layout: ComposeView
    var uiState by mutableStateOf<ScriptRunnerUIState>(ScriptRunnerUIState.Idle)
    var isRecording by mutableStateOf(false)
    var isPlayButtonEnabled by mutableStateOf(true)

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

    private var highlightLayoutParams = WindowManager.LayoutParams().apply {
        type = overlayType
        format = PixelFormat.TRANSLUCENT
        flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
    }

    init {
        require(Service is ScriptRunnerService)

        layout = FakedComposeView(Service) {
            ScriptRunnerUI(
                state = uiState,
                updateState = { Service.act(it) },
                isRecording = isRecording,
                enabled = isPlayButtonEnabled,
                onDrag = { x, y -> onDrag(x, y) }
            )
        }.view

        // By default put the button on bottom-left corner
        val m = metrics
        scriptCtrlBtnLayoutParams.y = maxOf(m.widthPixels, m.heightPixels)
    }

    private fun onDrag(x: Float, y: Float) {
        val targetX = scriptCtrlBtnLayoutParams.x + x.roundToInt()
        val targetY = scriptCtrlBtnLayoutParams.y + y.roundToInt()

        val dragMaxLoc = getMaxBtnCoordinates()

        scriptCtrlBtnLayoutParams.x = targetX.coerceIn(0, dragMaxLoc.X)
        scriptCtrlBtnLayoutParams.y = targetY.coerceIn(0, dragMaxLoc.Y)

        windowManager.updateViewLayout(layout, scriptCtrlBtnLayoutParams)
    }

    private var shown = false

    fun show() {
        if (!shown && Settings.canDrawOverlays(Service)) {
            windowManager.addView(highlightManager.highlightView, highlightLayoutParams)
            windowManager.addView(layout, scriptCtrlBtnLayoutParams)

            shown = true
        }
    }

    fun hide() {
        if (shown && Settings.canDrawOverlays(Service)) {
            windowManager.removeView(layout)
            windowManager.removeView(highlightManager.highlightView)

            shown = false
        }
    }

    fun postDelayed(Delay: Duration, Action: () -> Unit) {
        layout.postDelayed(Delay.inWholeMilliseconds, Action)
    }

    /**
     * Returns the maximum values of (X, Y) coordinates the [layout] can take.
     */
    private fun getMaxBtnCoordinates(): Location {
        val m = metrics

        val x = m.widthPixels - layout.measuredWidth
        val y = m.heightPixels - layout.measuredHeight

        return Location(x, y)
    }
}