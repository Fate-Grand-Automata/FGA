package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.models.BoostItem
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.roundToInt

@ScriptScope
class Locations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms,
    val fp: FPLocations,
    val lottery: LotteryLocations,
    val support: SupportScreenLocations,
    val attack: AttackScreenLocations,
    val battle: BattleScreenLocations
) : IScriptAreaTransforms by scriptAreaTransforms {

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
            Location(-460, 440)
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

    fun locate(refillResource: RefillResourceEnum): List<Location> {
        //scroll bar click location
        val scrollBarLoc = when (refillResource) {
            RefillResourceEnum.Copper -> 1040
            else -> 300
        }.let { y -> Location(750, y).xFromCenter() }

        val resourceLoc = when (refillResource) {
            RefillResourceEnum.Copper -> 980
            RefillResourceEnum.Bronze -> 1140
            RefillResourceEnum.Silver -> 922
            RefillResourceEnum.Gold -> 634
            RefillResourceEnum.SQ -> 345
        }.let { y -> Location(-530, y).xFromCenter() }
        return listOf(scrollBarLoc, resourceLoc)
    }

    fun locate(boost: BoostItem.Enabled) = when (boost) {
        BoostItem.Enabled.Skip -> Location(1652, 1304)
        BoostItem.Enabled.BoostItem1 -> Location(1280, 418)
        BoostItem.Enabled.BoostItem2 -> Location(1280, 726)
        BoostItem.Enabled.BoostItem3 -> Location(1280, 1000)
    }.xFromCenter()

    val selectedPartyRegion = Region(-270, 62, 550, 72).xFromCenter()
    val partySelectionArray = (0..9).map {
        // Party indicators are center-aligned
        val x = ((it - 4.5) * 50).roundToInt()

        Location(x, 100).xFromCenter()
    }

    val menuStorySkipRegion = Region(960, 20, 300, 120).xFromCenter()
    val menuStorySkipClick = Location(1080, 80).xFromCenter()

    val resultFriendRequestRegion = Region(600, 150, 100, 94).xFromCenter()
    val resultFriendRequestRejectClick = Location(-680, 1200).xFromCenter()
    val resultMatRewardsRegion = Region(800, 1220, 280, 130).xFromCenter()
    val resultClick = Location(320, 1350).xFromCenter()
    val resultQuestRewardRegion = Region(350, 140, 370, 250).xFromCenter()
    val resultDropScrollbarRegion = Region(980, 167, 100, 88).xFromCenter()
    val resultDropScrollEndClick = Location(1030, 968).xFromCenter()
    val resultMasterExpRegion = Region(0, 350, 400, 110).xFromCenter()
    val resultMasterLvlUpRegion = Region(710, 160, 250, 270).xFromCenter()
    val resultScreenRegion = Region(-1180, 300, 700, 200).xFromCenter()
    val resultBondRegion = Region(720, 750, 120, 190).xFromCenter()

    val resultCeRewardRegion = Region(-230, 1216, 33, 28).xFromCenter()
    val resultCeRewardDetailsRegion = Region(if (isWide) 193 else 0, 512, 135, 115)
    val resultCeRewardCloseClick = Location(if (isWide) 265 else 80, 60)

    val giftBoxSwipeStart = Location(120, if (canLongSwipe) 1200 else 1050).xFromCenter()
    val giftBoxSwipeEnd = Location(120, if (canLongSwipe) 350 else 575).xFromCenter()

    val ceEnhanceRegion = Region(200, 600, 400, 400)
    val ceEnhanceClick = Location(200, 600)
    val levelOneCERegion = Region(160, 380, 1840, 900)

    val npStartedRegion = Region(-400, 500, 800, 400).xFromCenter()
}
