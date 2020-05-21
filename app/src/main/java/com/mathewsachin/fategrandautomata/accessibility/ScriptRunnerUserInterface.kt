package com.mathewsachin.fategrandautomata.accessibility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.postDelayed
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.core.Location
import com.mathewsachin.fategrandautomata.core.Stopwatch
import com.mathewsachin.fategrandautomata.ui.highlightView
import kotlin.math.roundToInt
import kotlin.time.Duration

class ScriptRunnerUserInterface(Service: ScriptRunnerService) {
    private val layout = FrameLayout(Service)

    private val windowManager = Service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val layoutParams = WindowManager.LayoutParams().apply {
        type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        format = PixelFormat.TRANSLUCENT
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        @SuppressLint("RtlHardcoded")
        gravity = Gravity.LEFT or Gravity.TOP
        x = 0
        y = 0
    }

    private var highlightLayoutParams = WindowManager.LayoutParams().apply {
        type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        format = PixelFormat.TRANSLUCENT
        flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
    }

    private var scriptCtrlBtn: ImageButton
    val metrics = DisplayMetrics()

    init {
        val inflater = LayoutInflater.from(Service)
        inflater.inflate(R.layout.script_runner, layout)

        scriptCtrlBtn = layout.findViewById<ImageButton>(R.id.script_toggle_btn).also {
            Service.registerScriptCtrlBtnListeners(it)

            it.setOnTouchListener(::scriptCtrlBtnOnTouch)
        }

        windowManager.defaultDisplay.getRealMetrics(metrics)

        // Retrieve images in Landscape
        if (metrics.heightPixels > metrics.widthPixels) {
            metrics.let {
                val temp = it.widthPixels
                it.widthPixels = it.heightPixels
                it.heightPixels = temp
            }
        }

        layoutParams.y = metrics.widthPixels
    }

    fun show() {
        windowManager.addView(highlightView, highlightLayoutParams)
        windowManager.addView(layout, layoutParams)
    }

    fun hide() {
        windowManager.removeView(layout)
        windowManager.removeView(highlightView)
    }

    fun setPlayIcon() {
        scriptCtrlBtn.post {
            scriptCtrlBtn.setImageResource(R.drawable.ic_play)
        }
    }

    fun setStopIcon() {
        scriptCtrlBtn.setImageResource(R.drawable.ic_stop)
    }

    fun showAsRecording() {
        scriptCtrlBtn.drawable.colorFilter = BlendModeColorFilterCompat
            .createBlendModeColorFilterCompat(Color.RED, BlendModeCompat.SRC_ATOP)
    }

    fun postDelayed(Delay: Duration, Action: () -> Unit) {
        scriptCtrlBtn.postDelayed(Delay.toLongMilliseconds(), Action)
    }

    private fun getMaxBtnCoordinates(): Location {
        val rotation = windowManager.defaultDisplay.rotation
        val rotate = rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180

        val x = (if (rotate) {
            metrics.heightPixels
        } else metrics.widthPixels) - layout.measuredWidth
        val y = (if (rotate) {
            metrics.widthPixels
        } else metrics.heightPixels) - layout.measuredHeight

        return Location(x, y)
    }

    private val dragStopwatch = Stopwatch()
    private var dX = 0f
    private var dY = 0f
    private var lastAction = 0

    private fun scriptCtrlBtnOnTouch(_View: View, Event: MotionEvent): Boolean {
        return when (Event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val (maxX, maxY) = getMaxBtnCoordinates()
                dX = layoutParams.x.coerceIn(0, maxX) - Event.rawX
                dY = layoutParams.y.coerceIn(0, maxY) - Event.rawY
                lastAction = MotionEvent.ACTION_DOWN
                dragStopwatch.start()

                false
            }
            MotionEvent.ACTION_MOVE -> {
                val newX = Event.rawX + dX
                val newY = Event.rawY + dY

                if (dragStopwatch.elapsedMs > ViewConfiguration.getLongPressTimeout()) {
                    val (mX, mY) = getMaxBtnCoordinates()
                    layoutParams.x = newX.roundToInt().coerceIn(0, mX)
                    layoutParams.y = newY.roundToInt().coerceIn(0, mY)

                    windowManager.updateViewLayout(layout, layoutParams)

                    lastAction = MotionEvent.ACTION_MOVE
                }

                true
            }
            MotionEvent.ACTION_UP -> lastAction == MotionEvent.ACTION_MOVE
            else -> false
        }
    }
}