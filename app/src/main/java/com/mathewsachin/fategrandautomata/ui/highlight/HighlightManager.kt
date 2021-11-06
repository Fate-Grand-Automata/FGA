package com.mathewsachin.fategrandautomata.ui.highlight

import android.view.View
import com.mathewsachin.fategrandautomata.accessibility.TapperService
import com.mathewsachin.libautomata.HighlightColor
import com.mathewsachin.libautomata.Region
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class HighlightManager @Inject constructor() {
    private val regionsToHighlight = mutableMapOf<Region, HighlightColor>()

    val highlightView: View by lazy {
        HighlightView(
            Context = TapperService.instance ?: throw IllegalStateException("Accessibility service not running"),
            regionsToHighlight = regionsToHighlight
        )
    }

    fun add(Region: Region, color: HighlightColor) {
        highlightView.post {
            regionsToHighlight[Region] = color

            highlightView.invalidate()
        }
    }

    fun remove(Region: Region) {
        highlightView.post {
            regionsToHighlight.remove(Region)

            highlightView.invalidate()
        }
    }
}