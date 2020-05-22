package com.mathewsachin.fategrandautomata.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.fategrandautomata.core.IPattern
import com.mathewsachin.fategrandautomata.core.IPlatformImpl
import com.mathewsachin.fategrandautomata.core.Region
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.ui.addRegionToHighlight
import com.mathewsachin.fategrandautomata.ui.removeRegionToHighlight
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlin.time.Duration

class AndroidImpl(private val Service: ScriptRunnerService) : IPlatformImpl {
    override val windowRegion: Region
        get() = getCutoutAppliedRegion()

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

    override fun messageBox(Title: String, Message: String) {
        handler.post {
            Service.showMessageBox(Title, Message)
        }
    }

    override fun highlight(Region: Region, Duration: Duration) {
        // We can't draw over the notch area
        val cutoutAppliedRegion = getCutoutAppliedRegion()
        val region = Region.copy(
            X = Region.X - cutoutAppliedRegion.X,
            Y = Region.Y - cutoutAppliedRegion.Y
        )

        GlobalScope.launch {
            addRegionToHighlight(region)
            delay(Duration.toLongMilliseconds())
            removeRegionToHighlight(region)
        }
    }
}