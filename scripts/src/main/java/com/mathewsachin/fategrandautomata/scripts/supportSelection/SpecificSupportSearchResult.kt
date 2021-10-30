package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.libautomata.Region

sealed class SpecificSupportSearchResult {
    object NotFound : SpecificSupportSearchResult()
    open class Found(val Support: Region) : SpecificSupportSearchResult()
    class FoundWithBounds(Support: Region, val Bounds: Region) : Found(Support)
}