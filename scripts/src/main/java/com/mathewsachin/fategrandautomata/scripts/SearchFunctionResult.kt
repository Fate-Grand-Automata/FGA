package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.libautomata.Region

sealed class SearchFunctionResult {
    object NotFound : SearchFunctionResult()
    open class Found(val Support: Region) : SearchFunctionResult()
    class FoundWithBounds(Support: Region, val Bounds: Region) : Found(Support)
}