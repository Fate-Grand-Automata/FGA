package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.SupportStore
import com.mathewsachin.libautomata.IPattern

interface IImageLoader {
    fun loadRegionPattern(path: String): IPattern

    fun loadSupportPattern(support: SupportStore.SupportImage.File): IPattern

    fun clearImageCache()

    fun clearSupportCache()
}