package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.libautomata.IPattern

interface WhitePixelsProvider {
    fun getWhitePixelMask(threshold: Int): IPattern
}