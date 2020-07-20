package com.mathewsachin.fategrandautomata.ui

import android.content.Context
import android.view.View
import com.mathewsachin.libautomata.Region
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HighlightManager @Inject constructor(context: Context) {
    private val regionsToHighlight = mutableSetOf<Region>()

    val highlightView: View by lazy { HighlightView(context, regionsToHighlight) }

    fun add(Region: Region) {
        highlightView.post {
            regionsToHighlight.add(Region)

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