package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.roundToInt

@ScriptScope
class SupportScreenLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {
    private val headerOffset = Location(if (isWide) 171 else 0, 0)

    val screenCheckRegion = Region(0, 0, 200, 400) + headerOffset

    val extraRegion = Region(1200, 200, 130, 130) + headerOffset

    val updateClick =
        when (gameServer) {
            GameServerEnum.Tw -> 1700
            else -> 1865
        }.let { x -> Location(x, 260) + headerOffset }

    val listTopClick = Location(if (isWide) -218 else -80, 360).xFromRight()

    val firstSupportClick = Location(0, 500).xFromCenter()

    val updateYesClick = Location(200, 1110).xFromCenter()

    // Support Screen offset
    // For wide-screen: centered in this region: 305 left to 270 right
    // For 16:9 - 94 left to 145 right
    private val supportOffset =
        if (isWide) {
            val width = 2560 - 94 - 145
            val total = scriptArea.width - 305 - 270
            val border = ((total - width) / 2.0).roundToInt()

            Location(305 + border, 0)
        } else Location(94, 0)

    val listRegion = Region(-24, 332, 378, 1091) + supportOffset

    val friendRegion = Region(
        2140,
        listRegion.y,
        120,
        listRegion.height
    ) + supportOffset

    val friendsRegion = Region(354, 332, 1210, 1091) + supportOffset

    val maxAscendedRegion = Region(270, 0, 40, 120) + supportOffset
    val limitBreakRegion = Region(270, 0, 40, 90) + supportOffset

    val confirmSetupButtonRegion = Region(2006, 0, 370, 1440) + supportOffset
    val defaultBounds = Region(-18, 0, 2356, 428) + supportOffset
    val defaultCeBounds = Region(-18, 270, 378, 150) + supportOffset
    val notFoundRegion = Region(-140, 850, 280, 86).xFromCenter()

    val listSwipeStart = Location(-59, if (canLongSwipe) 1000 else 1190) + supportOffset
    val listSwipeEnd = Location(-89, if (canLongSwipe) 300 else 660) + supportOffset

    fun locate(supportClass: SupportClass) = when (supportClass) {
        SupportClass.None -> 0
        SupportClass.All -> 184
        SupportClass.Saber -> 320
        SupportClass.Archer -> 454
        SupportClass.Lancer -> 568
        SupportClass.Rider -> 724
        SupportClass.Caster -> 858
        SupportClass.Assassin -> 994
        SupportClass.Berserker -> 1130
        SupportClass.Extra -> 1264
        SupportClass.Mix -> 1402
    }.let { x -> Location(x, 256) + headerOffset }
}