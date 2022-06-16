package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

interface IScriptAreaTransforms {
    val scriptArea: Region
    val isWide: Boolean
    val gameServer: GameServerEnum
    val canLongSwipe: Boolean
    fun Location.xFromCenter(): Location
    fun Region.xFromCenter(): Region
    fun Location.xFromRight(): Location
    fun Region.xFromRight(): Region
    fun Location.yFromBottom(): Location
    fun Region.yFromBottom(): Region
}