package com.mathewsachin.fategrandautomata.util

import android.app.Service
import android.os.Build
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.highlight.HighlightManager
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.IPlatformPrefs
import com.mathewsachin.libautomata.Region
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration

@ServiceScoped
class AndroidImpl @Inject constructor(
    val service: Service,
    val preferences: IPreferences,
    val cutoutManager: CutoutManager,
    val highlightManager: HighlightManager
) : IPlatformImpl {

    override val windowRegion get() = cutoutManager.getCutoutAppliedRegion()

    override val canLongSwipe =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    override val prefs: IPlatformPrefs
        get() = preferences.platformPrefs

    override fun getResizableBlankPattern(): IPattern = DroidCvPattern()

    override fun highlight(region: Region, duration: Duration, success: Boolean) {
        GlobalScope.launch {
            highlightManager.add(region, success)
            delay(duration.inWholeMilliseconds)
            highlightManager.remove(region)
        }
    }
}