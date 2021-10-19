package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.locations.*
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.roundToInt

@ScriptScope
class Game @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms,
    val fp: FPLocations,
    val lottery: LotteryLocations,
    val master: MasterLocations,
    val support: SupportScreenLocations
): IScriptAreaTransforms by scriptAreaTransforms {

    val continueRegion = Region(120, 1000, 800, 200).xFromCenter()
    val continueBoostClick = Location(-20, 1120).xFromCenter()

    val inventoryFullRegion = Region(-280, 860, 560, 190).xFromCenter()

    val menuScreenRegion =
        (if (isWide)
            Region(-600, 1200, 600, 240)
        else Region(-460, 1200, 460, 240))
            .xFromRight()

    val menuSelectQuestClick =
        (if (isWide)
            Location(-410, 440)
        else Location(-270, 440))
            .xFromRight()

    val menuStartQuestClick =
        (if (isWide)
            Location(-350, -160)
        else Location(-160, -90))
            .xFromRight()
            .yFromBottom()

    val menuStorySkipYesClick = Location(320, 1100).xFromCenter()

    val retryRegion = Region(20, 1000, 700, 300).xFromCenter()

    val staminaScreenRegion = Region(-680, 200, 300, 300).xFromCenter()
    val staminaOkClick = Location(370, 1120).xFromCenter()
    val staminaCloseClick = Location(0, 1240).xFromCenter()

    val withdrawRegion = Region(-880, 540, 1800, 333).xFromCenter()
    val withdrawAcceptClick = Location(485, 720).xFromCenter()
    val withdrawCloseClick = Location(-10, 1140).xFromCenter()

    fun locate(refillResource: RefillResourceEnum) = when (refillResource) {
        RefillResourceEnum.Bronze -> 1140
        RefillResourceEnum.Silver -> 922
        RefillResourceEnum.Gold -> 634
        RefillResourceEnum.SQ -> 345
    }.let { y -> Location(-530, y).xFromCenter() }

    fun locate(boost: BoostItem.Enabled) = when (boost) {
        BoostItem.Enabled.Skip -> Location(1652, 1304)
        BoostItem.Enabled.BoostItem1 -> Location(1280, 418)
        BoostItem.Enabled.BoostItem2 -> Location(1280, 726)
        BoostItem.Enabled.BoostItem3 -> Location(1280, 1000)
    }.xFromCenter()

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

    val selectedPartyRegion = Region(-270, 62, 550, 72).xFromCenter()
    val partySelectionArray = (0..9).map {
        // Party indicators are center-aligned
        val x = ((it - 4.5) * 50).roundToInt()

        Location(x, 100).xFromCenter()
    }

    val battleScreenRegion =
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

    val battleAttackClick =
        (if (isWide)
            Location(-460, -230)
        else Location(-260, -240))
            .xFromRight()
            .yFromBottom()

    val battleSkillOkClick = Location(400, 850).xFromCenter()
    val battleOrderChangeOkClick = Location(0, 1260).xFromCenter()
    val battleExtraInfoWindowCloseClick = Location(-50, 50).xFromRight()

    val skipDeathAnimationClick = Location(-860, 200).xFromRight()

    val battleBack =
        (if (isWide)
            Location(-325, 1310)
        else Location(-160, 1370))
            .xFromRight()

    val menuStorySkipRegion = Region(960, 20, 300, 120).xFromCenter()
    val menuStorySkipClick = Location(1080, 80).xFromCenter()

    val resultFriendRequestRegion = Region(600, 150, 100, 94).xFromCenter()
    val resultFriendRequestRejectClick = Location(-680, 1200).xFromCenter()
    val resultMatRewardsRegion = Region(800, if (isNewUI) 1220 else 1290, 280, 130).xFromCenter()
    val resultClick = Location(320, 1350).xFromCenter()
    val resultQuestRewardRegion = Region(350, 140, 370, 250).xFromCenter()
    val resultDropScrollbarRegion = Region(980, 230, 100, 88).xFromCenter()
    val resultDropScrollEndClick = Location(1026, 1032).xFromCenter()
    val resultMasterExpRegion = Region(0, 350, 400, 110).xFromCenter()
    val resultMasterLvlUpRegion = Region(710, 160, 250, 270).xFromCenter()
    val resultScreenRegion = Region(-1180, 300, 700, 200).xFromCenter()
    val resultBondRegion = Region(720, 750, 120, 190).xFromCenter()

    val resultCeRewardRegion = Region(-230, 1216, 33, 28).xFromCenter()
    val resultCeRewardDetailsRegion = Region(if (isWide) 193 else 0, 512, 135, 115)
    val resultCeRewardCloseClick = Location(if (isWide) 265 else 80, 60)

    val giftBoxSwipeStart = Location(120, if (canLongSwipe) 1200 else 1050).xFromCenter()
    val giftBoxSwipeEnd = Location(120, if (canLongSwipe) 350 else 575).xFromCenter()

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

    fun servantOpenDetailsClick(slot: FieldSlot) =
        Location(locate(slot.skill2()).x, 810)

    fun servantChangeCheckRegion(slot: FieldSlot) =
        slot.skill2().let {
            val x = locate(it).x

            Region(x + 20, 930, 40, 80)
        }

    fun servantChangeSupportCheckRegion(slot: FieldSlot) =
        slot.skill2().let {
            val x = locate(it).x

            Region(x + 25, 710, 300, 170)
        }

    fun imageRegion(skill: Skill.Servant) =
        Region(30, 30, 30, 30) + locate(skill)

    val servantDetailsInfoClick = Location(-660, 110).xFromCenter()
    val servantDetailsFaceCardRegion = Region(-685, 280, 110, 80).xFromCenter()

    val ceEnhanceRegion = Region(200, 600, 400, 400)
    val ceEnhanceClick = Location(200, 600)
    val levelOneCERegion = Region(160, 380, 1840, 900)
}
