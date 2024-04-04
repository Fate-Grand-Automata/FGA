package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.scripts.models.BoostItem
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt

@ScriptScope
class Locations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms,
    val fp: FPLocations,
    val lottery: LotteryLocations,
    val support: SupportScreenLocations,
    val attack: AttackScreenLocations,
    val battle: BattleScreenLocations,
    val servant: ServantLevelLocations,
) : IScriptAreaTransforms by scriptAreaTransforms {

    // 9th anniversary changes the repeat screen and extends to 15 parties
    private val afterAnni9 = gameServer is GameServer.Jp || gameServer is GameServer.Cn

    val continueRegion = if (afterAnni9)
        Region(120, 1100, 800, 200).xFromCenter()
    else
        Region(120, 1000, 800, 200).xFromCenter()

    val continueBoostClick = Location(-20, 1120).xFromCenter()

    val inventoryFullRegion = Region(-280, 860, 560, 190).xFromCenter()

    val ordealCallOutOfPodsRegion = Region(-112, 1088, 219, 72).xFromCenter()

    val ordealCallOutOfPodsClick = Location(-2, 1124).xFromCenter()

    val interludeCloseClick = Location(-399, 1125).xFromCenter()
    val interludeEndScreenClose = Region(-515, 1080, 230, 90).xFromCenter()

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

    val selectedPartyRegion = if (afterAnni9)
        Region(-370, 62, 740, 72).xFromCenter()
    else
        Region(-270, 62, 550, 72).xFromCenter()
    
    val partySelectionArray: List<Location> = (0..14).map {
        val x = if (afterAnni9) {
            // Party 8 is on the center
            ((it - 7) * 50)
        } else {
            // Party indicators are center-aligned
            // Party 11-15 are going to be on party 10 just in case
            ((min(it, 9) - 4.5) * 50).roundToInt()
        }
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
    val resultBondRegion = Region(720, 600, 120, 400).xFromCenter()

    val resultCeRewardRegion = Region(-230, 1216, 33, 28).xFromCenter()
    val resultCeRewardDetailsRegion = Region(if (isWide) 193 else 0, 512, 135, 115)
    val resultCeRewardCloseClick = Location(if (isWide) 265 else 80, 60)

    val giftBoxSwipeStart = Location(120, if (canLongSwipe) 1200 else 1050).xFromCenter()
    val giftBoxSwipeEnd = Location(120, if (canLongSwipe) 350 else 575).xFromCenter()

    val emptyEnhanceRegion = when (isWide) {
        true -> Region(-1100, 600, 400, 400).xFromCenter()
        false -> Region(200, 600, 400, 400)
    }
    val ceEnhanceClick = Location(200, 600)
    val levelOneCERegion = Region(160, 380, 1840, 900)

    val npStartedRegion = Region(-400, 500, 800, 400).xFromCenter()

    val rankUpRegion = Region(270, 730, 220, 340).xFromCenter()

    val middleOfScreenClick = Location(0, 720).xFromCenter()

    /**
     * The following region are used for the various enhancement screen listed below:
     * Skill Upgrade, Ascension, Append Upgrade and Grail
     */
    val enhancementBannerRegion = when(isWide) {
        true -> Region(-412, 282, 241, 37).xFromCenter()
        false -> Region(-413, 324, 241, 37).xFromCenter()
    }

    val enhancementClick = when (isWide) {
        false -> Location(-281, 1343).xFromRight()
        true -> Location(-396, 1284).xFromRight()
    }

    val tempServantEnhancementRegion = Region(252, 1096, 301, 57).xFromCenter()

    val enhancementSkipRapidClick = Location(0, 1400).xFromCenter()

    val tempServantEnhancementLocation = Location(402, 1124).xFromCenter()

    val animationSkipLocation = menuStorySkipClick - Location(200, 0)
}
