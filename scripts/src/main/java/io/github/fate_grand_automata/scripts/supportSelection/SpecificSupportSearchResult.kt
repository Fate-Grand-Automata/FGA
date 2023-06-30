package io.github.fate_grand_automata.scripts.supportSelection

import io.github.lib_automata.Region

sealed class SpecificSupportSearchResult {
    object NotFound : SpecificSupportSearchResult()
    open class Found(val Support: Region) : SpecificSupportSearchResult()
    class FoundWithBounds(Support: Region, val Bounds: Region) : Found(Support)
}