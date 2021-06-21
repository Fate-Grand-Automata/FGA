package com.mathewsachin.fategrandautomata.util

import android.content.Context
import com.mathewsachin.fategrandautomata.IStorageProvider
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.scripts.IImageLoader
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.IPattern
import dagger.hilt.android.qualifiers.ApplicationContext
import org.opencv.android.Utils
import org.opencv.imgcodecs.Imgcodecs
import java.io.FileNotFoundException
import javax.inject.Inject

class ImageLoader @Inject constructor(
    val storageProvider: IStorageProvider,
    val prefs: IPreferences,
    @ApplicationContext val context: Context
) : IImageLoader {
    private fun createPattern(gameServer: GameServerEnum, FileName: String): IPattern {
        val filePath = "$gameServer/${FileName}"

        val assets = context.assets

        val inputStream = assets.open(filePath)

        inputStream.use {
            return DroidCvPattern(it).tag(filePath)
        }
    }

    private var currentGameServer: GameServerEnum =
        GameServerEnum.En
    private var regionCachedPatterns = mutableMapOf<String, IPattern>()

    fun Images.fileName(): String = when (this) {
        Images.BattleScreen -> "battle.png"
        Images.ServantExist -> "servant_exist.png"
        Images.TargetDanger -> "target_danger.png"
        Images.TargetServant -> "target_servant.png"
        Images.Buster -> "buster.png"
        Images.Arts -> "art.png"
        Images.Quick -> "quick.png"
        Images.Weak -> "weak.png"
        Images.Resist -> "resist.png"
        Images.Friend -> "friend.png"
        Images.Guest -> "guest.png"
        Images.Follow -> "follow.png"
        Images.LimitBroken -> "limitbroken.png"
        Images.SupportScreen -> "support_screen.png"
        Images.SupportRegionTool -> "support_region_tool.png"
        Images.StorySkip -> "storyskip.png"
        Images.Menu -> "menu.png"
        Images.Stamina -> "stamina.png"
        Images.Result -> "result.png"
        Images.Bond -> "bond.png"
        Images.Bond10Reward -> "bond10.png"
        Images.CEDetails -> "ce_details.png"
        Images.Repeat -> "confirm.png"
        Images.QuestReward -> "questreward.png"
        Images.Retry -> "retry.png"
        Images.Withdraw -> "withdraw.png"
        Images.LotteryBoxFinished -> "lottery.png"
        Images.PresentBoxFull -> "StopGifts.png"
        Images.MasterExp -> "master_exp.png"
        Images.MasterLevelUp -> "master_lvl_up.png"
        Images.MatRewards -> "mat_rewards.png"
        Images.InventoryFull -> "inven_full.png"
        Images.FPSummonContinue -> "fp_continue.png"
        Images.SkillTen -> "skill_ten.png"
        Images.Stun -> "stun.png"
        Images.SelectedParty -> "selected_party.png"
        Images.SilverXP -> "SilverXP.png"
        Images.GoldXP -> "GoldXP.png"
        Images.GiftBoxCheck -> "gift_box_check.png"
        Images.GiftBoxScrollEnd -> "gift_box_scroll_end.png"
        Images.DropCE -> "drop_ce.png"
        Images.DropCEStars -> "drop_ce_star.png"
        Images.FriendSummon -> "friend_summon.png"
        Images.DropScrollbar -> "drop_scrollbar.png"
        Images.SupportExtra -> "support_extra.png"
        Images.SupportNotFound -> "support_not_found.png"
        Images.Support -> "support.png"
        Images.ServantCheckSupport -> "servant_check_support.png"
        Images.BattleMenu -> "battle_menu.png"
        Images.ExpX1 -> "x1.png"
        Images.ExpX2 -> "x2.png"
        Images.ExpX3 -> "x3.png"
        Images.ExpX4 -> "x4.png"
        Images.CEEnhance -> "ce_enhance.png"
        Images.CEGloomLv1 -> "gloom_0.png"
        Images.CEStarvationLv1 -> "starvation_0.png"
        Images.CEAwakeningLv1 -> "awakening_0.png"
        Images.CEBarrierLv1 -> "barrier_0.png"
        Images.CECombatLv1 -> "combat_0.png"
        Images.CEDeceptionLv1 -> "deception_0.png"
        Images.CELinkageLv1 -> "linkage_0.png"
        Images.CEMercyLv1 -> "mercy_0.png"
        Images.CEProsperityLv1 -> "prosperity_0.png"
        Images.CESynchronizationLv1 -> "synchronization_0.png"
    }

    override operator fun get(img: Images): IPattern = synchronized(regionCachedPatterns) {
        val path = img.fileName()

        val server = prefs.gameServer

        // Reload Patterns on Server change
        if (currentGameServer != server) {
            clearImageCache()

            currentGameServer = server
        }

        return regionCachedPatterns.getOrPut(path) {
            loadPatternWithFallback(path)
        }
    }

    /**
     * When image is not available for the current server, use the image from NA server.
     */
    private fun loadPatternWithFallback(path: String): IPattern {
        if (currentGameServer != GameServerEnum.En) {
            return try {
                createPattern(currentGameServer, path)
            } catch (e: FileNotFoundException) {
                createPattern(GameServerEnum.En, path)
            }
        }

        return createPattern(currentGameServer, path)
    }

    override fun clearImageCache() = synchronized(regionCachedPatterns) {
        for (pattern in regionCachedPatterns.values) {
            pattern.close()
        }

        regionCachedPatterns.clear()

        clearSupportCache()
    }

    private var supportCachedPatterns = mutableMapOf<String, List<IPattern>>()

    override fun clearSupportCache() = synchronized(supportCachedPatterns) {
        for (patterns in supportCachedPatterns.values) {
            patterns.forEach { it.close() }
        }

        supportCachedPatterns.clear()
    }

    private fun fileLoader(kind: SupportImageKind, name: String): List<IPattern> {
        val inputStreams = storageProvider.readSupportImage(kind, name)
        return inputStreams.withIndex().map { (i, stream) ->
            DroidCvPattern(stream).tag("$name:$i")
        }
    }

    override fun loadSupportPattern(kind: SupportImageKind, name: String): List<IPattern> = synchronized(supportCachedPatterns) {
        return supportCachedPatterns.getOrPut("$kind:$name") {
            fileLoader(kind, name)
        }
    }

    override fun loadMaterial(material: MaterialEnum) =
        regionCachedPatterns.getOrPut("materials/$material") {
            DroidCvPattern(
                Utils.loadResource(context, material.drawable, Imgcodecs.IMREAD_GRAYSCALE)
            ).tag("MAT:$material")
        }
}

val MaterialEnum.drawable
    get() = when (this) {
        MaterialEnum.Proof -> R.drawable.mat_proof
        MaterialEnum.Bone -> R.drawable.mat_bone
        MaterialEnum.Fang -> R.drawable.mat_fang
        MaterialEnum.Dust -> R.drawable.mat_dust
        MaterialEnum.Chain -> R.drawable.mat_chain
        MaterialEnum.Stinger -> R.drawable.mat_stinger
        MaterialEnum.Fluid -> R.drawable.mat_fluid
        MaterialEnum.Stake -> R.drawable.mat_stake
        MaterialEnum.Gunpowder -> R.drawable.mat_gunpowder
        MaterialEnum.AmnestyBell -> R.drawable.mat_amnesty_bell
        MaterialEnum.Seed -> R.drawable.mat_seed
        MaterialEnum.GhostLantern -> R.drawable.mat_ghost_lantern
        MaterialEnum.OctupletCrystal -> R.drawable.mat_octuplet_crystal
        MaterialEnum.SerpentJewel -> R.drawable.mat_serpent_jewel
        MaterialEnum.Feather -> R.drawable.mat_feather
        MaterialEnum.Gear -> R.drawable.mat_gear
        MaterialEnum.Page -> R.drawable.mat_page
        MaterialEnum.HomunculusBaby -> R.drawable.mat_homunculus_baby
        MaterialEnum.Horseshoe -> R.drawable.mat_horseshoe
        MaterialEnum.Medal -> R.drawable.mat_medal
        MaterialEnum.ShellOfReminiscence -> R.drawable.mat_shell_of_reminiscence
        MaterialEnum.Magatama -> R.drawable.mat_magatama
        MaterialEnum.EternalIce -> R.drawable.mat_ice
        MaterialEnum.GiantRing -> R.drawable.mat_giant_ring
        MaterialEnum.AuroraSteel -> R.drawable.mat_steel
        MaterialEnum.SoundlessBell -> R.drawable.mat_bell
        MaterialEnum.Arrowhead -> R.drawable.mat_arrow
        MaterialEnum.Tiara -> R.drawable.mat_tiara
        MaterialEnum.DivineSpiritParticle -> R.drawable.mat_particle
        MaterialEnum.RainbowThreadBall -> R.drawable.mat_thread
        MaterialEnum.FantasyScales -> R.drawable.mat_fantasy_scales
        MaterialEnum.Claw -> R.drawable.mat_claw
        MaterialEnum.Heart -> R.drawable.mat_heart
        MaterialEnum.DragonScale -> R.drawable.mat_scale
        MaterialEnum.SpiritRoot -> R.drawable.mat_spirit_root
        MaterialEnum.YoungHorn -> R.drawable.mat_young_horn
        MaterialEnum.TearStone -> R.drawable.mat_tear_stone
        MaterialEnum.Grease -> R.drawable.mat_grease
        MaterialEnum.LampOfEvilSealing -> R.drawable.mat_lamp_of_evil_sealing
        MaterialEnum.Scarab -> R.drawable.mat_scarab
        MaterialEnum.Lanugo -> R.drawable.mat_lanugo
        MaterialEnum.Gallstone -> R.drawable.mat_gallstone
        MaterialEnum.MysteriousWine -> R.drawable.mat_mysterious_wine
        MaterialEnum.ReactorCore -> R.drawable.mat_core
        MaterialEnum.TsukumoMirror -> R.drawable.mat_mirror
        MaterialEnum.EggOfTruth -> R.drawable.mat_egg
        MaterialEnum.StarShard -> R.drawable.mat_star_shard
        MaterialEnum.FruitOfEternity -> R.drawable.mat_fruit
        MaterialEnum.DemonFlameLantern -> R.drawable.mat_demon_lantern
    }