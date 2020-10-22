package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import java.util.*
import javax.inject.Inject

class MaterialImageLocator @Inject constructor(val imgLoader: IImageLoader) {
    private fun load(name: String) = imgLoader.loadRegionPattern("materials/$name.png")

    private val MaterialEnum.fileName: String
        get() = toString().toLowerCase(Locale.ROOT)

    operator fun get(mat: MaterialEnum) = load(mat.fileName)
}