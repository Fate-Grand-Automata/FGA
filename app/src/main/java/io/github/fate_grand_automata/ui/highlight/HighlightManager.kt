package io.github.fate_grand_automata.ui.highlight

import android.graphics.PixelFormat
import android.os.Build
import android.view.WindowManager
import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.accessibility.TapperService
import io.github.lib_automata.HighlightColor
import io.github.lib_automata.Region
import javax.inject.Inject

data class HighlightItem(val color: HighlightColor, val text: String? = null)

@ServiceScoped
class HighlightManager @Inject constructor() {
    private val tapperService by lazy {
        TapperService.instance ?: throw IllegalStateException("Accessibility service not running")
    }

    private val regionsToHighlight = mutableMapOf<Region, HighlightItem>()

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

    fun show() {
        accessibilityWindowManager.addView(highlightView, highlightLayoutParams)
    }

    fun hide() {
        accessibilityWindowManager.removeView(highlightView)
    }

    fun add(region: Region, color: HighlightColor, text: String?) {
        highlightView.post {
            regionsToHighlight[region] = HighlightItem(color, text)

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