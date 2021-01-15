package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.isWide
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import com.mathewsachin.libautomata.extensions.IAutomataExtensions
import com.mathewsachin.libautomata.extensions.ITransformationExtensions
import timber.log.Timber
import timber.log.debug
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.seconds

fun IFgoAutomataApi.needsToRetry() = images.retry in game.retryRegion

fun IFgoAutomataApi.retry() {
    game.retryRegion.click()

    2.seconds.wait()
}

@ScriptScope
class Game @Inject constructor(
    platformImpl: IPlatformImpl,
    val prefs: IPreferences,
    val images: ImageLocator,
    transformationExtensions: ITransformationExtensions,
    val gameAreaManager: GameAreaManager,
    val automataApi: IAutomataExtensions
) {
    val scriptArea =
        Region(
            Location(),
            gameAreaManager.gameArea.size * (1 / transformationExtensions.scriptToScreenScale())
        )

    private val isJp = prefs.gameServer == GameServerEnum.Jp
    val isWide = isJp && scriptArea.isWide()

    fun Location.xFromCenter() =
        this + Location(scriptArea.center.X, 0)

    fun Region.xFromCenter() =
        this + Location(scriptArea.center.X, 0)

    fun Location.xFromRight() =
        this + Location(scriptArea.right, 0)

    fun Region.xFromRight() =
        this + Location(scriptArea.right, 0)

    fun Location.yFromBottom() =
        this + Location(0, scriptArea.bottom)

    fun Region.yFromBottom() =
        this + Location(0, scriptArea.bottom)

    // Master Skills and Stage counter are right-aligned differently,
    // so we use locations relative to a matched location
    private val masterOffsetJp: Location by lazy {
        automataApi.run {
            Region(-400, 360, 400, 80)
                .xFromRight()
                .find(images.battleMenu)
                ?.Region
                ?.center
                ?.copy(Y = 0)
                ?: Location(-298, 0).xFromRight().also {
                    Timber.debug { "Defaulting master offset" }
                }
        }
    }

    val continueRegion = Region(120, 1000, 800, 200).xFromCenter()
    val continueBoostClick = Location(-20, 1120).xFromCenter()
    val continueClick = Location(370, 1120).xFromCenter()

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

    val withdrawRegion = Region(-880, 540, 1800, 190).xFromCenter()
    val withdrawAcceptClick = Location(485, 720).xFromCenter()
    val withdrawCloseClick = Location(-10, 1140).xFromCenter()

    val supportScreenRegion =
        if (isWide)
            Region(150, 0, 200, 400)
        else Region(0, 0, 200, 400)

    val supportExtraRegion =
        if (isWide)
            Region(1380, 200, 130, 130)
        else Region(1200, 200, 130, 130)

    val supportUpdateClick =
        if (isWide)
            Location(1870, 260)
        else Location(1670, 250)

    val supportListTopClick =
        (if (isWide)
            Location(-218, 360)
        else Location(-80, 360)).xFromRight()

    val supportFirstSupportClick = Location(0, 500).xFromCenter()

    val supportUpdateYesClick = Location(200, 1110).xFromCenter()

    // Support Screen offset
    // For wide-screen: centered in this region: 305 left to 270 right
    // For 16:9 - 94 left to 145 right
    val supportOffset =
        if (isWide) {
            val width = 2560 - 94 - 145
            val total = scriptArea.Width - 305 - 270
            val border = ((total - width) / 2.0).roundToInt()

            Location(305 + border, 0)
        } else Location(94, 0)

    val supportListRegion = Region(-24, 332, 378, 1091) + supportOffset

    val supportFriendRegion = Region(
        2140,
        supportListRegion.Y,
        120,
        supportListRegion.Height
    ) + supportOffset

    val supportFriendsRegion = Region(354, 332, 1210, 1091) + supportOffset

    val supportMaxAscendedRegion = Region(270, 0, 40, 120) + supportOffset
    val supportLimitBreakRegion = Region(270, 0, 40, 90) + supportOffset

    val supportRegionToolSearchRegion = Region(2006, 0, 370, 1440) + supportOffset
    val supportDefaultBounds = Region(-18, 0, 2356, 428) + supportOffset
    val supportDefaultCeBounds = Region(-18, 270, 378, 150) + supportOffset
    val supportNotFoundRegion = Region(374, 708, 100, 90) + supportOffset

    private val canLongSwipe = platformImpl.canLongSwipe
    val supportListSwipeStart = Location(-59, if (canLongSwipe) 1000 else 1190) + supportOffset
    val supportListSwipeEnd = Location(-89, if (canLongSwipe) 300 else 660) + supportOffset

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
        OrderChangeMember.Starting.A -> Location(-1000, 700)
        OrderChangeMember.Starting.B -> Location(-600, 700)
        OrderChangeMember.Starting.C -> Location(-200, 700)
        OrderChangeMember.Sub.A -> Location(200, 700)
        OrderChangeMember.Sub.B -> Location(600, 700)
        OrderChangeMember.Sub.C -> Location(1000, 700)
    }.xFromCenter()

    fun locate(servantTarget: ServantTarget) = when (servantTarget) {
        ServantTarget.A -> Location(-580, 880)
        ServantTarget.B -> Location(0, 880)
        ServantTarget.C -> Location(660, 880)
        ServantTarget.Left -> Location(-290, 880)
        ServantTarget.Right -> Location(330, 880)
    }.xFromCenter()

    fun locate(skill: Skill.Servant) = when (skill) {
        Skill.Servant.A1 -> Location(140, 1155)
        Skill.Servant.A2 -> Location(328, 1155)
        Skill.Servant.A3 -> Location(514, 1155)
        Skill.Servant.B1 -> Location(775, 1155)
        Skill.Servant.B2 -> Location(963, 1155)
        Skill.Servant.B3 -> Location(1150, 1155)
        Skill.Servant.C1 -> Location(1413, 1155)
        Skill.Servant.C2 -> Location(1600, 1155)
        Skill.Servant.C3 -> Location(1788, 1155)
    } + Location(if (isWide) 108 else 0, if (isWide) -22 else 0)

    fun locate(skill: Skill.Master) = when (skill) {
        Skill.Master.A -> Location(-740, 620)
        Skill.Master.B -> Location(-560, 620)
        Skill.Master.C -> Location(-400, 620)
    }.let {
        if (isJp)
            it + Location(178, 0) + masterOffsetJp
        else it.xFromRight()
    }

    fun locate(skill: Skill) = when (skill) {
        is Skill.Servant -> locate(skill)
        is Skill.Master -> locate(skill)
    }

    fun locate(supportClass: SupportClass) = when (supportClass) {
        SupportClass.None -> Location()
        SupportClass.All -> Location(184, 256)
        SupportClass.Saber -> Location(320, 256)
        SupportClass.Archer -> Location(454, 256)
        SupportClass.Lancer -> Location(568, 256)
        SupportClass.Rider -> Location(724, 256)
        SupportClass.Caster -> Location(858, 256)
        SupportClass.Assassin -> Location(994, 256)
        SupportClass.Berserker -> Location(1130, 256)
        SupportClass.Extra -> Location(1264, 256)
        SupportClass.Mix -> Location(1402, 256)
    } + Location(if (isWide) 171 else 0, 0)

    fun locate(enemy: EnemyTarget) = when (enemy) {
        EnemyTarget.A -> Location(90, 80)
        EnemyTarget.B -> Location(570, 80)
        EnemyTarget.C -> Location(1050, 80)
    } + Location(if (isWide) 183 else 0, 0)

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

    val battleStageCountRegion
        get() = when (prefs.gameServer) {
            GameServerEnum.Tw -> Region(1710, 25, 55, 60)
            GameServerEnum.Jp -> Region(if (isWide) -571 else -638, 23, 33, 53) + masterOffsetJp
            else -> Region(1722, 25, 46, 53)
        }

    val battleScreenRegion =
        (if (isWide)
            Region(-660, -210, 400, 175)
        else Region(-455, -181, 336, 116))
            .xFromRight()
            .yFromBottom()

    val battleAttackClick =
        (if (isWide)
            Location(-460, -230)
        else Location(-260, -240))
            .xFromRight()
            .yFromBottom()

    val battleMasterSkillOpenClick
        get() =
            if (isJp)
                Location(0, 640) + masterOffsetJp
            else Location(-180, 640).xFromRight()

    val battleSkillOkClick = Location(400, 850).xFromCenter()
    val battleOrderChangeOkClick = Location(0, 1260).xFromCenter()
    val battleExtraInfoWindowCloseClick = Location(-10, 10).xFromRight()

    val battleBack =
        (if (isWide)
            Location(-325, 1310)
        else Location(-160, 1370))
            .xFromRight()

    val menuStorySkipRegion = Region(960, 20, 300, 120).xFromCenter()
    val menuStorySkipClick = Location(1080, 80).xFromCenter()

    val resultFriendRequestRegion = Region(600, 150, 100, 94).xFromCenter()
    val resultFriendRequestRejectClick = Location(-680, 1200).xFromCenter()
    val resultMatRewardsRegion = Region(800, 1220, 280, 200).xFromCenter()
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

    val fpSummonCheck = Region(100, 1220, 75, 75).xFromCenter()
    val fpContinueSummonRegion = Region(-36, 1264, 580, 170).xFromCenter()
    val fpFirst10SummonClick = Location(120, 1120).xFromCenter()
    val fpOkClick = Location(320, 1120).xFromCenter()
    val fpContinueSummonClick = Location(320, 1325).xFromCenter()
    val fpSkipRapidClick = Location(1240, 1400).xFromCenter()

    val giftBoxSwipeStart = Location(120, if (canLongSwipe) 1200 else 1050).xFromCenter()
    val giftBoxSwipeEnd = Location(120, if (canLongSwipe) 350 else 575).xFromCenter()

    val lotteryFinishedRegion = Region(-780, 860, 180, 100).xFromCenter()
    val lotteryCheckRegion = Region(-1130, 800, 340, 230).xFromCenter()
    val lotterySpinClick = Location(-446, 860).xFromCenter()
    val lotteryFullPresentBoxRegion = Region(20, 860, 1000, 500).xFromCenter()
    val lotteryResetClick = Location(920, 480).xFromCenter()
    val lotteryResetConfirmationClick = Location(494, 1122).xFromCenter()
    val lotteryResetCloseClick = Location(-10, 1120).xFromCenter()

    val gudaFinalRewardsRegion = Region(-120, 1040, 228, 76).xFromCenter()

    fun clickLocation(card: CommandCard) = when (card) {
        CommandCard.Face.A -> Location(-980, 1000)
        CommandCard.Face.B -> Location(-530, 1000)
        CommandCard.Face.C -> Location(20, 1000)
        CommandCard.Face.D -> Location(520, 1000)
        CommandCard.Face.E -> Location(1070, 1000)
        CommandCard.NP.A -> Location(-280, 220)
        CommandCard.NP.B -> Location(20, 400)
        CommandCard.NP.C -> Location(460, 400)
    }.xFromCenter()

    fun affinityRegion(card: CommandCard.Face) = when (card) {
        CommandCard.Face.A -> Region(-985, 650, 250, 200)
        CommandCard.Face.B -> Region(-470, 650, 250, 200)
        CommandCard.Face.C -> Region(41, 650, 250, 200)
        CommandCard.Face.D -> Region(554, 650, 250, 200)
        CommandCard.Face.E -> Region(1068, 650, 250, 200)
    }.xFromCenter()

    fun typeRegion(card: CommandCard.Face) = when (card) {
        CommandCard.Face.A -> Region(-1280, 1060, 512, 200)
        CommandCard.Face.B -> Region(-768, 1060, 512, 200)
        CommandCard.Face.C -> Region(-256, 1060, 512, 200)
        CommandCard.Face.D -> Region(256, 1060, 512, 200)
        CommandCard.Face.E -> Region(768, 1060, 512, 200)
    }.xFromCenter()

    fun servantMatchRegion(card: CommandCard) = when (card) {
        CommandCard.Face.A -> Region(-1174, 800, 300, 200)
        CommandCard.Face.B -> Region(-660, 800, 300, 200)
        CommandCard.Face.C -> Region(-150, 800, 300, 200)
        CommandCard.Face.D -> Region(364, 800, 300, 200)
        CommandCard.Face.E -> Region(880, 800, 300, 200)
        CommandCard.NP.A -> Region(-602, 190, 300, 200)
        CommandCard.NP.B -> Region(-142, 190, 300, 200)
        CommandCard.NP.C -> Region(326, 190, 300, 200)
    }.xFromCenter()

    fun servantCropRegion(card: CommandCard) = when (card) {
        CommandCard.Face.A -> Region(-1080, 890, 115, 85)
        CommandCard.Face.B -> Region(-566, 890, 115, 85)
        CommandCard.Face.C -> Region(-56, 890, 115, 85)
        CommandCard.Face.D -> Region(458, 890, 115, 85)
        CommandCard.Face.E -> Region(974, 890, 115, 85)
        CommandCard.NP.A -> Region(-518, 290, 115, 65)
        CommandCard.NP.B -> Region(-50, 290, 115, 65)
        CommandCard.NP.C -> Region(414, 290, 115, 65)
    }.xFromCenter()

    fun supportCheckRegion(card: CommandCard) = when (card) {
        is CommandCard.Face -> affinityRegion(card) + Location(-50, 100)
        is CommandCard.NP -> (servantMatchRegion(card) + Location(110, -30)).copy(Height = 170)
    }
}
