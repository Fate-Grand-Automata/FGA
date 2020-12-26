package com.mathewsachin.fategrandautomata.util

import android.app.Service
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerNotification
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

class AndroidImpl @Inject constructor(
    service: Service,
    val notification: ScriptRunnerNotification,
    val preferences: IPreferences,
    val cutoutManager: CutoutManager,
    val highlightManager: HighlightManager
) : IPlatformImpl {
    val service = service as ScriptRunnerService

    override val windowRegion get() = cutoutManager.getCutoutAppliedRegion()

    override val prefs: IPlatformPrefs
        get() = preferences.platformPrefs

    override fun toast(Message: String) {
        handler.post {
            Toast
                .makeText(service, Message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun notify(message: String) = notification.message(message)

    override fun getResizableBlankPattern(): IPattern {
        return DroidCvPattern()
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun messageBox(Title: String, Message: String, Error: Exception?, onDismiss: () -> Unit) {
        handler.post {
            service.showMessageBox(Title, Message, Error, onDismiss)
        }
    }

    override fun highlight(Region: Region, Duration: Duration, success: Boolean) {
        // We can't draw over the notch area
        val region = Region - cutoutManager.getCutoutAppliedRegion().location

        GlobalScope.launch {
            highlightManager.add(region, success)
            delay(Duration.toLongMilliseconds())
            highlightManager.remove(region)
        }
    }
}