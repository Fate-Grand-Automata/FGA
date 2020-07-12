package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.os.*
import android.widget.Toast
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.Region
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import com.mathewsachin.fategrandautomata.ui.HighlightManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlin.time.Duration
import kotlin.time.milliseconds

class AndroidImpl(
    private val Service: ScriptRunnerService,
    val highlightManager: HighlightManager
) : IPlatformImpl {
    override val windowRegion get() = getCutoutAppliedRegion()

    override val debugMode get() = Preferences.DebugMode

    override fun toast(Message: String) {
        handler.post {
            Toast
                .makeText(Service, Message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun loadPattern(Stream: InputStream): IPattern {
        return DroidCvPattern(Stream)
    }

    override fun getResizableBlankPattern(): IPattern {
        return DroidCvPattern()
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private fun vibrate(Duration: Duration) {
        val v = Service.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createOneShot(
                    Duration.toLongMilliseconds(),
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            v.vibrate(Duration.toLongMilliseconds())
        }
    }

    override fun messageBox(Title: String, Message: String, Error: Exception?) {
        handler.post {
            Service.showMessageBox(Title, Message, Error)
        }

        vibrate(100.milliseconds)
    }

    override fun highlight(Region: Region, Duration: Duration) {
        // We can't draw over the notch area
        val region = Region - getCutoutAppliedRegion().location

        GlobalScope.launch {
            highlightManager.add(region)
            delay(Duration.toLongMilliseconds())
            highlightManager.remove(region)
        }
    }
}