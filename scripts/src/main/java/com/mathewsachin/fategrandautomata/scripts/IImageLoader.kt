package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.libautomata.IPattern

interface IImageLoader {
    fun loadRegionPattern(path: String): IPattern

    fun loadSupportPattern(path: String): IPattern

    fun loadMaterial(material: MaterialEnum): IPattern

    fun clearImageCache()

    fun clearSupportCache()
}