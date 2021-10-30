package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class AttackScreenLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
): IScriptAreaTransforms by scriptAreaTransforms {
    private fun clickLocation(card: CommandCard.Face) = when (card) {
        CommandCard.Face.A -> -980
        CommandCard.Face.B -> -530
        CommandCard.Face.C -> 20
        CommandCard.Face.D -> 520
        CommandCard.Face.E -> 1070
    }.let { x -> Location(x, 1000) }

    fun clickLocation(card: CommandCard) = when (card) {
        is CommandCard.Face -> clickLocation(card)
        CommandCard.NP.A -> Location(-280, 220)
        CommandCard.NP.B -> Location(20, 400)
        CommandCard.NP.C -> Location(460, 400)
    }.xFromCenter()

    private val faceCardDeltaY =
        Location(0, if (gameServer == GameServerEnum.Cn && isWide) -42 else 0)

    fun affinityRegion(card: CommandCard.Face) = when (card) {
        CommandCard.Face.A -> -985
        CommandCard.Face.B -> -470
        CommandCard.Face.C -> 41
        CommandCard.Face.D -> 554
        CommandCard.Face.E -> 1068
    }.let { x -> Region(x, 650, 250, 200) + faceCardDeltaY }.xFromCenter()

    fun typeRegion(card: CommandCard.Face) = when (card) {
        CommandCard.Face.A -> -1280
        CommandCard.Face.B -> -768
        CommandCard.Face.C -> -256
        CommandCard.Face.D -> 256
        CommandCard.Face.E -> 768
    }.let { x -> Region(x, 1060, 512, 200) + faceCardDeltaY }.xFromCenter()

    fun servantMatchRegion(card: CommandCard.Face) = when (card) {
        CommandCard.Face.A -> -1174
        CommandCard.Face.B -> -660
        CommandCard.Face.C -> -150
        CommandCard.Face.D -> 364
        CommandCard.Face.E -> 880
    }.let { x -> Region(x - 100, 700, 500, 400) + faceCardDeltaY }.xFromCenter()

    fun supportCheckRegion(card: CommandCard.Face) =
        affinityRegion(card) + Location(-50, 100)

    val backClick =
        (if (isWide)
            Location(-325, 1310)
        else Location(-160, 1370))
            .xFromRight()
}