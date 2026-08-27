package io.github.fate_grand_automata.ui.highlight

import android.graphics.PixelFormat
import android.os.Build
import android.view.WindowManager
import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.accessibility.TapperService
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.lib_automata.HighlightColor
import io.github.lib_automata.Region
import javax.inject.Inject

@ServiceScoped
class HighlightManager @Inject constructor(
    private val prefsCore: PrefsCore
) {
    private val tapperService by lazy {
        TapperService.instance ?: throw IllegalStateException("Accessibility service not running")
    }

    private val regionsToHighlight = mutableMapOf<Region, HighlightColor>()

    private val highlightView by lazy {
        HighlightView(tapperService, regionsToHighlight)
    }

    private val accessibilityWindowManager by lazy {
        tapperService.getSystemService(WindowManager::class.java)
    }

    private var highlightLayoutParams = WindowManager.LayoutParams().apply {
        type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        format = PixelFormat.TRANSLUCENT
        flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    private var shown = false

    /**
     * Only attaches the overlay in debug mode.
     *
     * The pref is read here rather than watched, so toggling debug mode while the service
     * runs only takes effect after a restart.
     */
    fun show() {
        if (shown || !prefsCore.debugMode.get()) return

        accessibilityWindowManager.addView(highlightView, highlightLayoutParams)
        shown = true
    }

    fun hide() {
        if (!shown) return

        accessibilityWindowManager.removeView(highlightView)
        shown = false
    }

    fun add(region: Region, color: HighlightColor) {
        highlightView.post {
            regionsToHighlight[region] = color

            highlightView.invalidate()
        }
    }

    fun remove(region: Region) {
        highlightView.post {
            regionsToHighlight.remove(region)

            highlightView.invalidate()
        }
    }
}