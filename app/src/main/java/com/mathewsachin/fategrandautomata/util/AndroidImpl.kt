package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.os.*
import android.widget.Toast
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.HighlightManager
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.IPlatformPrefs
import com.mathewsachin.libautomata.Region
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.milliseconds

class AndroidImpl @Inject constructor(
    private val Service: ScriptRunnerService,
    val preferences: IPreferences,
    val cutoutManager: CutoutManager,
    val highlightManager: HighlightManager
) : IPlatformImpl {
    override val windowRegion get() = cutoutManager.getCutoutAppliedRegion()

    override val prefs: IPlatformPrefs
        get() = preferences.platformPrefs

    override fun toast(Message: String) {
        handler.post {
            Toast
                .makeText(Service, Message, Toast.LENGTH_SHORT)
                .show()
        }
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
        val region = Region - cutoutManager.getCutoutAppliedRegion().location

        GlobalScope.launch {
            highlightManager.add(region)
            delay(Duration.toLongMilliseconds())
            highlightManager.remove(region)
        }
    }
}