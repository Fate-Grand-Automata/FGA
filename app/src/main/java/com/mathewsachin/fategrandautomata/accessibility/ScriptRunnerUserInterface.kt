package com.mathewsachin.fategrandautomata.accessibility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.postDelayed
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.core.Location
import com.mathewsachin.fategrandautomata.ui.highlightView
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.TimeSource.Monotonic
import kotlin.time.milliseconds

class ScriptRunnerUserInterface(val Service: ScriptRunnerService) {
    val overlayType: Int get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        else WindowManager.LayoutParams.TYPE_PHONE
    }

    val windowManager = Service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val metrics: DisplayMetrics get() {
        val res = DisplayMetrics()

        windowManager.defaultDisplay.getRealMetrics(res)

        return res
    }

    /**
     * Used with MediaProjection so that we only get landscape images,
     * since the frame size can't be changed during a projection.
     */
    val mediaProjectionMetrics: DisplayMetrics get() {
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

    private val scriptCtrlBtnLayout = FrameLayout(Service)
    private var scriptCtrlBtn: ImageButton

    private val scriptCtrlBtnLayoutParams = WindowManager.LayoutParams().apply {
        type = overlayType
        format = PixelFormat.TRANSLUCENT
        flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
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
        val inflater = LayoutInflater.from(Service)
        inflater.inflate(R.layout.script_runner, scriptCtrlBtnLayout)

        scriptCtrlBtn = scriptCtrlBtnLayout.findViewById<ImageButton>(R.id.script_toggle_btn).also {
            Service.registerScriptCtrlBtnListeners(it)

            it.setOnTouchListener(::scriptCtrlBtnOnTouch)
        }

        // By default put the button on bottom-left corner
        val m = metrics
        scriptCtrlBtnLayoutParams.y = maxOf(m.widthPixels, m.heightPixels)
    }

    fun show() {
        windowManager.addView(highlightView, highlightLayoutParams)
        windowManager.addView(scriptCtrlBtnLayout, scriptCtrlBtnLayoutParams)
    }

    fun hide() {
        windowManager.removeView(scriptCtrlBtnLayout)
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

    /**
     * Returns the maximum values of (X, Y) coordinates the [scriptCtrlBtn] can take.
     */
    private fun getMaxBtnCoordinates(): Location {
        val m = metrics

        val x = m.widthPixels - scriptCtrlBtnLayout.measuredWidth
        val y = m.heightPixels - scriptCtrlBtnLayout.measuredHeight

        return Location(x, y)
    }

    private var dX = 0f
    private var dY = 0f
    private var lastAction = 0
    private var dragTimeMark = Monotonic.markNow()
    private var dragMaxLoc = Location()

    private fun scriptCtrlBtnOnTouch(_View: View, Event: MotionEvent): Boolean {
        return when (Event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragMaxLoc = getMaxBtnCoordinates()
                dX = scriptCtrlBtnLayoutParams.x.coerceIn(0, dragMaxLoc.X) - Event.rawX
                dY = scriptCtrlBtnLayoutParams.y.coerceIn(0, dragMaxLoc.Y) - Event.rawY
                lastAction = MotionEvent.ACTION_DOWN
                dragTimeMark = Monotonic.markNow()

                false
            }
            MotionEvent.ACTION_MOVE -> {
                val newX = Event.rawX + dX
                val newY = Event.rawY + dY

                if (dragTimeMark.elapsedNow() > ViewConfiguration.getLongPressTimeout().milliseconds) {
                    scriptCtrlBtnLayoutParams.x = newX.roundToInt().coerceIn(0, dragMaxLoc.X)
                    scriptCtrlBtnLayoutParams.y = newY.roundToInt().coerceIn(0, dragMaxLoc.Y)

                    windowManager.updateViewLayout(scriptCtrlBtnLayout, scriptCtrlBtnLayoutParams)

                    lastAction = MotionEvent.ACTION_MOVE
                }

                true
            }
            MotionEvent.ACTION_UP -> lastAction == MotionEvent.ACTION_MOVE
            else -> false
        }
    }
}