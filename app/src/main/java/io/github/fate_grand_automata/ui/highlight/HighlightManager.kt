package io.github.fate_grand_automata.ui.highlight

import android.graphics.PixelFormat
import android.os.Build
import android.view.WindowManager
import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.accessibility.TapperService
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.lib_automata.HighlightColor
import io.github.lib_automata.Region
import timber.log.Timber
import javax.inject.Inject

@ServiceScoped
class HighlightManager @Inject constructor(
    private val prefsCore: PrefsCore
) {
    private val regionsToHighlight = mutableMapOf<Region, HighlightColor>()

    /**
     * The accessibility service the overlay is currently attached to, or `null` when it isn't
     * attached. This is what owns the window token of a TYPE_ACCESSIBILITY_OVERLAY window, and
     * the system binds/unbinds it independently of the script runner service, so it must never
     * be cached beyond a single attach.
     */
    private var attachedTo: TapperService? = null

    @Volatile
    private var highlightView: HighlightView? = null

    private val highlightLayoutParams = WindowManager.LayoutParams().apply {
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

    /**
     * Only attaches the overlay in debug mode.
     *
     * The pref is read here rather than watched, so toggling debug mode while the service
     * runs only takes effect after a restart.
     */
    fun show() {
        if (!prefsCore.debugMode.get()) return

        // Resolved fresh every time: the system unbinds and rebinds the accessibility service on
        // its own (the user toggling it, a settings change, a crash, ...) and adding a window with
        // the token of a dead one throws BadTokenException.
        val service = TapperService.instance

        if (service == null) {
            Timber.w("Can't show the highlight overlay, the accessibility service isn't running")
            return
        }

        if (attachedTo === service) return

        if (attachedTo != null) {
            // We were attached to a service that has since been replaced. Its window died with it,
            // but drop our references before adding a new one.
            hide()
        }

        val view = HighlightView(service, regionsToHighlight)

        try {
            service.getSystemService(WindowManager::class.java)
                .addView(view, highlightLayoutParams)
        } catch (e: Exception) {
            // BadTokenException if the service got unbound in the meantime, and some OEM ROMs
            // throw on overlays for their own reasons. Debug highlights aren't worth a crash.
            Timber.w(e, "Failed to add the highlight overlay")
            return
        }

        highlightView = view
        attachedTo = service
    }

    fun hide() {
        val view = highlightView ?: return
        val service = attachedTo

        // Drop the references first: if removal fails there is nothing left to retry with, and
        // holding on would keep a dead service (and its whole context) alive.
        highlightView = null
        attachedTo = null

        runCatching {
            service?.getSystemService(WindowManager::class.java)?.removeView(view)
        }.onFailure { Timber.w(it, "Failed to remove the highlight overlay") }
    }

    fun add(region: Region, color: HighlightColor) {
        val view = highlightView ?: return

        view.post {
            regionsToHighlight[region] = color

            view.invalidate()
        }
    }

    fun remove(region: Region) {
        val view = highlightView ?: return

        view.post {
            regionsToHighlight.remove(region)

            view.invalidate()
        }
    }
}
