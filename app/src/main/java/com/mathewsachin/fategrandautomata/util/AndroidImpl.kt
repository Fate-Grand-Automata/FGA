package com.mathewsachin.fategrandautomata.util

import android.os.Build
import com.mathewsachin.fategrandautomata.di.service.ServiceCoroutineScope
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.highlight.HighlightManager
import com.mathewsachin.libautomata.*
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration

@ServiceScoped
class AndroidImpl @Inject constructor(
    private val preferences: IPreferences,
    private val cutoutManager: CutoutManager,
    private val highlightManager: HighlightManager,
    @ServiceCoroutineScope private val scope: CoroutineScope
) : IPlatformImpl {
    override val windowRegion get() = cutoutManager.getCutoutAppliedRegion()

    override val canLongSwipe =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    override val prefs: IPlatformPrefs
        get() = preferences.platformPrefs

    override fun getResizableBlankPattern(): IPattern = DroidCvPattern()

    override fun highlight(region: Region, duration: Duration, color: HighlightColor) {
        scope.launch {
            try {
                highlightManager.add(region, color)
                delay(duration.inWholeMilliseconds)
            } finally {
                highlightManager.remove(region)
            }
        }
    }
}