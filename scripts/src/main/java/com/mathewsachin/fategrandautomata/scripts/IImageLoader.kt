package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.libautomata.IPattern

interface IImageLoader {
    operator fun get(img: Images): IPattern

    fun loadSupportPattern(kind: SupportImageKind, name: String): List<IPattern>

    fun loadMaterial(material: MaterialEnum): IPattern

    fun clearImageCache()

    fun clearSupportCache()
}