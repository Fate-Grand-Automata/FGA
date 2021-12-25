package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.libautomata.Pattern

interface IImageLoader {
    operator fun get(img: Images): Pattern

    fun loadSupportPattern(kind: SupportImageKind, name: String): List<Pattern>

    fun loadMaterial(material: MaterialEnum): Pattern

    fun clearImageCache()

    fun clearSupportCache()
}