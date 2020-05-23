package com.mathewsachin.fategrandautomata.accessibility

import android.graphics.PixelFormat
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.IdRes
import com.mathewsachin.fategrandautomata.R

class ScriptRunnerDialog(val UI: ScriptRunnerUserInterface) {
    private val frame = object : FrameLayout(UI.Service) {
        override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
            return when (event?.keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    hide()
                    true
                }
                else -> super.dispatchKeyEvent(event)
            }
        }
    }

    private val layoutParams: WindowManager.LayoutParams

    init {
        val inflater = LayoutInflater.from(UI.Service)
        inflater.inflate(R.layout.script_runner_dialog, frame)

        layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            windowAnimations = android.R.style.Animation_Dialog
        }

        val baseFrame = frame.findViewById<ViewGroup>(R.id.script_runner_dialog_base)
        baseFrame.setOnClickListener { hide() }
    }

    fun show() {
        UI.windowManager.addView(frame, layoutParams)
    }

    fun hide() {
        UI.windowManager.removeView(frame)
    }

    fun setTitle(Title: String) {
        val textView = frame.findViewById<TextView>(R.id.script_runner_dialog_title)
        textView.text = Title
    }

    fun setMessage(Message: String) {
        val textView = frame.findViewById<TextView>(R.id.script_runner_dialog_message)
        textView.text = Message
    }

    private fun setButton(@IdRes ButtonId: Int, Message: String, OnClick: () -> Unit) {
        val btn = frame.findViewById<Button>(ButtonId)

        btn.visibility = View.VISIBLE
        btn.text = Message

        btn.setOnClickListener {
            OnClick()

            hide()
        }
    }

    fun setPositiveButton(Message: String, OnClick: () -> Unit) {
        setButton(R.id.script_runner_dialog_btn_positive, Message, OnClick)
    }

    fun setNegativeButton(Message: String, OnClick: () -> Unit) {
        setButton(R.id.script_runner_dialog_btn_negative, Message, OnClick)
    }

    fun setNeutralButton(Message: String, OnClick: () -> Unit) {
        setButton(R.id.script_runner_dialog_btn_neutral, Message, OnClick)
    }

    fun setView(View: View) {
        val content = frame.findViewById<ViewGroup>(R.id.script_runner_dialog_content)

        content.removeAllViews()
        content.addView(View)
    }
}