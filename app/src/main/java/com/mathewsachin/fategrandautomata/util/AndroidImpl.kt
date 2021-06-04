package com.mathewsachin.fategrandautomata.util

import android.app.Service
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerNotification
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.highlight.HighlightManager
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.IPlatformPrefs
import com.mathewsachin.libautomata.Region
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration

class AndroidImpl @Inject constructor(
    val service: Service,
    val notification: ScriptRunnerNotification,
    val preferences: IPreferences,
    val cutoutManager: CutoutManager,
    val highlightManager: HighlightManager
) : IPlatformImpl {

    override val windowRegion get() = cutoutManager.getCutoutAppliedRegion()

    override val canLongSwipe =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    override val prefs: IPlatformPrefs
        get() = preferences.platformPrefs

    override fun toast(message: String) {
        handler.post {
            Toast
                .makeText(service, message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun notify(message: String) = notification.message(message)

    override fun getResizableBlankPattern(): IPattern = DroidCvPattern()

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun highlight(region: Region, duration: Duration, success: Boolean) {
        // We can't draw over the notch area
        val drawRegion = region - cutoutManager.getCutoutAppliedRegion().location

        GlobalScope.launch {
            highlightManager.add(drawRegion, success)
            delay(duration.inWholeMilliseconds)
            highlightManager.remove(drawRegion)
        }
    }
}