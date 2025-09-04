package io.github.fate_grand_automata.util

import android.os.Build
import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.di.service.ServiceCoroutineScope
import io.github.fate_grand_automata.imaging.DroidCvPattern
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.highlight.HighlightManager
import io.github.lib_automata.HighlightColor
import io.github.lib_automata.Pattern
import io.github.lib_automata.PlatformImpl
import io.github.lib_automata.PlatformPrefs
import io.github.lib_automata.Region
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
) : PlatformImpl {
    override val windowRegion get() = cutoutManager.getCutoutAppliedRegion()

    override val canLongSwipe =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    override val prefs: PlatformPrefs
        get() = preferences.platformPrefs

    override fun getResizableBlankPattern(): Pattern = DroidCvPattern()

    override fun highlight(region: Region, duration: Duration, color: HighlightColor, text: String?) {
        scope.launch {
            try {
                highlightManager.add(region, color, text)
                delay(duration.inWholeMilliseconds)
            } finally {
                highlightManager.remove(region)
            }
        }
    }
}