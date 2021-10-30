package com.mathewsachin.fategrandautomata.ui.highlight

import android.content.Context
import android.view.View
import com.mathewsachin.libautomata.HighlightColor
import com.mathewsachin.libautomata.Region
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HighlightManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val regionsToHighlight = mutableMapOf<Region, HighlightColor>()

    val highlightView: View by lazy { HighlightView(context, regionsToHighlight) }

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