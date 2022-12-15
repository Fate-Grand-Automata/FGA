package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class BattleScreenLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms,
    val master: MasterLocations
) : IScriptAreaTransforms by scriptAreaTransforms {
    fun locate(orderChangeMember: OrderChangeMember) = when (orderChangeMember) {
        OrderChangeMember.Starting.A -> -1000
        OrderChangeMember.Starting.B -> -600
        OrderChangeMember.Starting.C -> -200
        OrderChangeMember.Sub.A -> 200
        OrderChangeMember.Sub.B -> 600
        OrderChangeMember.Sub.C -> 1000
    }.let { x -> Location(x, 700) }.xFromCenter()

    fun locate(servantTarget: ServantTarget) = when (servantTarget) {
        ServantTarget.A -> -580
        ServantTarget.B -> 0
        ServantTarget.C -> 660
        ServantTarget.Left -> -290
        ServantTarget.Right -> 330
    }.let { x -> Location(x, 880) }.xFromCenter()

    fun locate(skill: Skill.Servant) = when (skill) {
        Skill.Servant.A1 -> 148
        Skill.Servant.A2 -> 324
        Skill.Servant.A3 -> 500
        Skill.Servant.B1 -> 784
        Skill.Servant.B2 -> 960
        Skill.Servant.B3 -> 1136
        Skill.Servant.C1 -> 1418
        Skill.Servant.C2 -> 1594
        Skill.Servant.C3 -> 1770
    }.let { x -> Location(x + if (isWide) 108 else 0, if (isWide) 1117 else 1158) }

    fun locate(enemy: EnemyTarget) = when (enemy) {
        EnemyTarget.A -> 90
        EnemyTarget.B -> 570
        EnemyTarget.C -> 1050
    }.let { x -> Location(x + if (isWide) 183 else 0, 80) }

    fun dangerRegion(enemy: EnemyTarget) = when (enemy) {
        EnemyTarget.A -> Region(0, 0, 485, 220)
        EnemyTarget.B -> Region(485, 0, 482, 220)
        EnemyTarget.C -> Region(967, 0, 476, 220)
    } + Location(if (isWide) 150 else 0, 0)

    val screenCheckRegion =
        (if (isWide)
            Region(-660, -210, 400, 175)
        else Region(-455, -181, 336, 116))
            .xFromRight()
            .yFromBottom()

    fun servantPresentRegion(slot: FieldSlot) =
        slot.skill3().let {
            val skill3Location = locate(it)

            Region(
                skill3Location.x + 35,
                skill3Location.y + 67,
                120,
                120
            )
        }

    val attackClick =
        (if (isWide)
            Location(-460, -230)
        else Location(-260, -240))
            .xFromRight()
            .yFromBottom()

    val skillOkClick = Location(400, 850).xFromCenter()
    val orderChangeOkClick = Location(0, 1260).xFromCenter()
    val extraInfoWindowCloseClick = Location(-50, 50).xFromRight()

    fun servantOpenDetailsClick(slot: FieldSlot) =
        Location(locate(slot.skill2()).x, 810)

    fun servantChangeCheckRegion(slot: FieldSlot) =
        slot.skill2().let {
            val x = locate(it).x

            Region(x + 20, 865, 40, 80)
        }

    fun servantChangeSupportCheckRegion(slot: FieldSlot) =
        slot.skill2().let {
            val x = locate(it).x

            Region(x + 25, 710, 300, 170)
        }

    fun imageRegion(skill: Skill.Servant) =
        Region(30, 30, 30, 30) + locate(skill)

    val servantDetailsInfoClick = Location(-660, 110).xFromCenter()
    val servantDetailsFaceCardRegion = Region(-685, 330, 110, 60).xFromCenter()
}