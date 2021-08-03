package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.IImageLoader
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.isWide
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.isNewUI
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
import kotlin.time.Duration

fun IFgoAutomataApi.needsToRetry() = images[Images.Retry] in game.retryRegion

fun IFgoAutomataApi.retry() {
    game.retryRegion.click()

    Duration.seconds(2).wait()
}

@ScriptScope
class Game @Inject constructor(
    platformImpl: IPlatformImpl,
    val prefs: IPreferences,
    val images: IImageLoader,
    transformationExtensions: ITransformationExtensions,
    gameAreaManager: GameAreaManager,
    val automataApi: IAutomataExtensions
) {
    val scriptArea =
        Region(
            Location(),
            gameAreaManager.gameArea.size * (1 / transformationExtensions.scriptToScreenScale())
        )

    val isWide = prefs.isNewUI && scriptArea.isWide()

    fun Location.xFromCenter() =
        this + Location(scriptArea.center.x, 0)

    fun Region.xFromCenter() =
        this + Location(scriptArea.center.x, 0)

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
    private val masterOffsetNewUI: Location by lazy {
        automataApi.run {
            Region(-400, 360, 400, 80)
                .xFromRight()
                .find(images[Images.BattleMenu])
                ?.region
                ?.center
                ?.copy(y = 0)
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

    val withdrawRegion = Region(-880, 540, 1800, 333).xFromCenter()
    val withdrawAcceptClick = Location(485, 720).xFromCenter()
    val withdrawCloseClick = Location(-10, 1140).xFromCenter()

    val supportScreenRegion = Region(if (isWide) 150 else 0, 0, 200, 400)

    val supportExtraRegion = Region(if (isWide) 1380 else 1200, 200, 130, 130)

    val supportUpdateClick =
        if (isWide)
            Location(1870, 260)
        else Location(1670, 250)

    val supportListTopClick = Location(if (isWide) -218 else -80, 360).xFromRight()

    val supportFirstSupportClick = Location(0, 500).xFromCenter()

    val supportUpdateYesClick = Location(200, 1110).xFromCenter()

    // Support Screen offset
    // For wide-screen: centered in this region: 305 left to 270 right
    // For 16:9 - 94 left to 145 right
    val supportOffset =
        if (isWide) {
            val width = 2560 - 94 - 145
            val total = scriptArea.width - 305 - 270
            val border = ((total - width) / 2.0).roundToInt()

            Location(305 + border, 0)
        } else Location(94, 0)

    val supportListRegion = Region(-24, 332, 378, 1091) + supportOffset

    val supportFriendRegion = Region(
        2140,
        supportListRegion.y,
        120,
        supportListRegion.height
    ) + supportOffset

    val supportFriendsRegion = Region(354, 332, 1210, 1091) + supportOffset

    val supportMaxAscendedRegion = Region(270, 0, 40, 120) + supportOffset
    val supportLimitBreakRegion = Region(270, 0, 40, 90) + supportOffset

    val supportRegionToolSearchRegion = Region(2006, 0, 370, 1440) + supportOffset
    val supportDefaultBounds = Region(-18, 0, 2356, 428) + supportOffset
    val supportDefaultCeBounds = Region(-18, 270, 378, 150) + supportOffset
    val supportNotFoundRegion = Region(324, 708, 150, 90) + supportOffset

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
        Skill.Servant.A1 -> 140
        Skill.Servant.A2 -> 328
        Skill.Servant.A3 -> 514
        Skill.Servant.B1 -> 775
        Skill.Servant.B2 -> 963
        Skill.Servant.B3 -> 1150
        Skill.Servant.C1 -> 1413
        Skill.Servant.C2 -> 1600
        Skill.Servant.C3 -> 1788
    }.let { x -> Location(x + if (isWide) 108 else 0, if (isWide) 1117 else 1155) }

    fun locate(skill: Skill.Master) = when (skill) {
        Skill.Master.A -> -740
        Skill.Master.B -> -560
        Skill.Master.C -> -400
    }.let { x ->
        val location = Location(x, 620)

        if (prefs.isNewUI)
            location + Location(178, 0) + masterOffsetNewUI
        else location.xFromRight()
    }

    fun locate(skill: Skill) = when (skill) {
        is Skill.Servant -> locate(skill)
        is Skill.Master -> locate(skill)
    }

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
    }.let { x -> Location(x + if (isWide) 171 else 0, 256) }

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

    val battleStageCountRegion
        get() = when {
            prefs.isNewUI -> Region(if (isWide) -571 else -638, 23, 33, 53) + masterOffsetNewUI
            prefs.gameServer == GameServerEnum.Tw -> Region(1710, 25, 55, 60)
            else -> Region(1722, 25, 46, 53)
        }

    val battleScreenRegion =
        (if (isWide)
            Region(-660, -210, 400, 175)
        else Region(-455, -181, 336, 116))
            .xFromRight()
            .yFromBottom()

    val servantDeathCheckRegions =
        ServantSlot.list
            .map {
                when (it) {
                    ServantSlot.A -> Skill.Servant.A3
                    ServantSlot.B -> Skill.Servant.B3
                    ServantSlot.C -> Skill.Servant.C3
                }
            }
            .map {
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

    val battleMasterSkillOpenClick
        get() =
            if (prefs.isNewUI)
                Location(0, 640) + masterOffsetNewUI
            else Location(-180, 640).xFromRight()

    val battleSkillOkClick = Location(400, 850).xFromCenter()
    val battleOrderChangeOkClick = Location(0, 1260).xFromCenter()
    val battleExtraInfoWindowCloseClick = Location(-10, 10).xFromRight()

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
    val resultMatRewardsRegion = Region(800, if (prefs.isNewUI) 1220 else 1290, 280, 130).xFromCenter()
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

    // height of the summon banners at the bottom, only relevant for JP right now
    private val summonBannerHeight = if (prefs.gameServer == GameServerEnum.Jp) 68 else 0
    val fpSummonCheck = Region(100, 1220 - summonBannerHeight, 75, 75).xFromCenter()
    val fpContinueSummonRegion = Region(-36, 1264, 580, 170).xFromCenter()
    val fpFirst10SummonClick = Location(120, 1120 - summonBannerHeight).xFromCenter()
    val fpOkClick = Location(320, 1120).xFromCenter()
    val fpContinueSummonClick = Location(320, 1325).xFromCenter()
    val fpSkipRapidClick = Location(1240, 1400).xFromCenter()

    val giftBoxSwipeStart = Location(120, if (canLongSwipe) 1200 else 1050).xFromCenter()
    val giftBoxSwipeEnd = Location(120, if (canLongSwipe) 350 else 575).xFromCenter()

    val lotteryFinishedRegion = Region(-780, 860, 180, 100).xFromCenter()
    val lotteryCheckRegion = Region(-1130, 800, 340, 230).xFromCenter()
    val lotterySpinClick = Location(-446, 860).xFromCenter()
    val lotteryFullPresentBoxRegion = Region(20, 860, 1000, 500).xFromCenter()
    val lotteryResetClick = Location(if (isWide) 1160 else 920, 480).xFromCenter()
    val lotteryResetConfirmationClick = Location(494, 1122).xFromCenter()
    val lotteryResetCloseClick = Location(-10, 1120).xFromCenter()

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

    fun affinityRegion(card: CommandCard.Face) = when (card) {
        CommandCard.Face.A -> -985
        CommandCard.Face.B -> -470
        CommandCard.Face.C -> 41
        CommandCard.Face.D -> 554
        CommandCard.Face.E -> 1068
    }.let { x -> Region(x, 650, 250, 200) }.xFromCenter()

    fun typeRegion(card: CommandCard.Face) = when (card) {
        CommandCard.Face.A -> -1280
        CommandCard.Face.B -> -768
        CommandCard.Face.C -> -256
        CommandCard.Face.D -> 256
        CommandCard.Face.E -> 768
    }.let { x -> Region(x, 1060, 512, 200) }.xFromCenter()

    private fun servantMatchRegion(card: CommandCard.Face) = when (card) {
        CommandCard.Face.A -> -1174
        CommandCard.Face.B -> -660
        CommandCard.Face.C -> -150
        CommandCard.Face.D -> 364
        CommandCard.Face.E -> 880
    }.let { x -> Region(x, 800, 300, 200) }.xFromCenter()

    private fun servantMatchRegion(card: CommandCard.NP) = when (card) {
        CommandCard.NP.A -> -602
        CommandCard.NP.B -> -142
        CommandCard.NP.C -> 326
    }.let { x -> Region(x, 190, 300, 200) }.xFromCenter()

    fun servantMatchRegion(card: CommandCard) = when (card) {
        is CommandCard.Face -> servantMatchRegion(card)
        is CommandCard.NP -> servantMatchRegion(card)
    }

    private fun servantCropRegion(card: CommandCard.Face) = when (card) {
        CommandCard.Face.A -> -1080
        CommandCard.Face.B -> -566
        CommandCard.Face.C -> -56
        CommandCard.Face.D -> 458
        CommandCard.Face.E -> 974
    }.let { x -> Region(x, 890, 115, 85) }.xFromCenter()

    private fun servantCropRegion(card: CommandCard.NP) = when (card) {
        CommandCard.NP.A -> -518
        CommandCard.NP.B -> -50
        CommandCard.NP.C -> 414
    }.let { x -> Region(x, 290, 115, 65) }.xFromCenter()

    fun servantCropRegion(card: CommandCard) = when (card) {
        is CommandCard.Face -> servantCropRegion(card)
        is CommandCard.NP -> servantCropRegion(card)
    }

    fun supportCheckRegion(card: CommandCard) = when (card) {
        is CommandCard.Face -> affinityRegion(card) + Location(-50, 100)
        is CommandCard.NP -> (servantMatchRegion(card) + Location(110, -30)).copy(height = 170)
    }

    fun servantOpenDetailsClick(slot: ServantSlot) =
        when (slot) {
            ServantSlot.A -> Skill.Servant.A2
            ServantSlot.B -> Skill.Servant.B2
            ServantSlot.C -> Skill.Servant.C2
        }.let {
            Location(locate(it).x, 810)
        }

    fun servantChangeCheckRegion(slot: ServantSlot) =
        when (slot) {
            ServantSlot.A -> Skill.Servant.A2
            ServantSlot.B -> Skill.Servant.B2
            ServantSlot.C -> Skill.Servant.C2
        }.let {
            val x = locate(it).x

            Region(x + 20, 930, 40, 80)
        }

    fun servantChangeSupportCheckRegion(slot: ServantSlot) =
        when (slot) {
            ServantSlot.A -> Skill.Servant.A2
            ServantSlot.B -> Skill.Servant.B2
            ServantSlot.C -> Skill.Servant.C2
        }.let {
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