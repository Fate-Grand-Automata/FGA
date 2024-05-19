package io.github.fate_grand_automata.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.fate_grand_automata.IStorageProvider
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.imaging.DroidCvPattern
import io.github.fate_grand_automata.scripts.IImageLoader
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.lib_automata.ColorManager
import io.github.lib_automata.Pattern
import org.opencv.android.Utils
import org.opencv.imgcodecs.Imgcodecs
import javax.inject.Inject

class ImageLoader @Inject constructor(
    val storageProvider: IStorageProvider,
    val prefs: IPreferences,
    @ApplicationContext val context: Context,
    private val colorManager: ColorManager
) : IImageLoader {
    private fun createPattern(gameServer: GameServer, FileName: String): Pattern {
        val gameServerPath = gameServer.simple
        val filePath = "$gameServerPath/$FileName"

        val assets = context.assets

        // load image from En by default or from current game server if a custom image exists
        val gameServerWithImage = if (assets.list(gameServerPath)?.contains(FileName) == true) {
            gameServerPath
        } else GameServer.default.simple

        val inputStream = assets.open("$gameServerWithImage/${FileName}")

        inputStream.use {
            return DroidCvPattern(it, colorManager.isColor, filePath)
        }
    }

    private data class CacheKey(val name: String, val gameServer: GameServer?, val isColor: Boolean)

    private fun key(name: String, gameServer: GameServer? = null) = CacheKey(name, gameServer, colorManager.isColor)

    private var currentGameServer: GameServer = GameServer.default
    private var regionCachedPatterns = mutableMapOf<CacheKey, Pattern>()

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
        Images.SupportConfirmSetupButton -> "support_region_tool.png"
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
        Images.LotteryTransition -> "lottery_transition.png"
        Images.PresentBoxFull -> "StopGifts.png"
        Images.MasterExp -> "master_exp.png"
        Images.MasterLevelUp -> "master_lvl_up.png"
        Images.MatRewards -> "mat_rewards.png"
        Images.InventoryFull -> "inven_full.png"
        Images.FPSummonContinue -> "fp_continue.png"
        Images.SkillTen -> "skill_ten.png"
        Images.Stun -> "stun.png"
        Images.StunBuster -> "stun_buster.png"
        Images.StunArts -> "stun_arts.png"
        Images.StunQuick -> "stun_quick.png"
        Images.Immobilized -> "immobilized.png"
        Images.SelectedParty -> "selected_party.png"
        Images.SilverXP -> "SilverXP.png"
        Images.GoldXP -> "GoldXP.png"
        Images.Gold5StarXP -> "Gold5StarXP.png"
        Images.GiftBoxCheck -> "gift_box_check.png"
        Images.GiftBoxScrollEnd -> "gift_box_scroll_end.png"
        Images.DropCE -> "drop_ce.png"
        Images.DropCEStars -> "drop_ce_star.png"
        Images.FriendSummon -> "friend_summon.png"
        Images.DropScrollbar -> "drop_scrollbar.png"
        Images.SupportExtra -> "support_extra.png"
        Images.SupportNotFound -> "support_not_found.png"
        Images.Support -> "support.png"
        Images.SupportScrollBarTop -> "support_scrollbar_top.png"
        Images.SupportScrollBarMoved -> "support_scrollbar_moved.png"
        Images.SupportScrollBarBottom -> "support_scrollbar_bottom.png"
        Images.SupportRefresh -> "support_refresh.png"
        Images.ServantCheckSupport -> "servant_check_support.png"
        Images.BattleMenu -> "battle_menu.png"
        Images.EmptyEnhance -> "empty_enhance.png"
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
        Images.SkillUse -> "skill_use.png"
        Images.RankUp -> "rank_up.png"
        Images.Close -> "close.png"
        Images.ServantAutoSelect -> "servant_auto_select.png"
        Images.ServantAutoSelectOff -> "servant_auto_select_off.png"
        Images.ServantMaxLevel -> "servant_max_level.png"
        Images.ServantGrailRedirectFromMenu -> "servant_palingenesis_redirect_from_menu.png"
        Images.ServantAscensionRedirectFromMenu -> "servant_ascension_redirect_from_menu.png"
        Images.ServantGrailBanner -> "servant_palingenesis_banner.png"
        Images.ServantAscensionBanner -> "servant_ascension_banner.png"
        Images.ServantAscensionReturnToLevel -> "servant_ascension_return_to_level.png"
        Images.Ok -> "ok.png"
        Images.OkKR -> "ok-kr.png"
        Images.Execute -> "execute.png"
    }

    override operator fun get(img: Images, gameServer: GameServer?): Pattern = synchronized(regionCachedPatterns) {
        val path = img.fileName()

        val server = prefs.gameServer

        // Reload Patterns on Server change
        if (currentGameServer != server) {
            clearImageCache()

            currentGameServer = server
        }

        return regionCachedPatterns.getOrPut(key(path, gameServer)) {
            loadPatternWithFallback(path, gameServer)
        }
    }

    /**
     * When image is not available for the current server, use the image from NA server.
     */
    private fun loadPatternWithFallback(path: String, gameServer: GameServer?): Pattern {
        return createPattern(gameServer ?: currentGameServer, path)
    }

    override fun clearImageCache() = synchronized(regionCachedPatterns) {
        for (pattern in regionCachedPatterns.values) {
            pattern.close()
        }

        regionCachedPatterns.clear()

        clearSupportCache()
    }

    private var supportCachedPatterns = mutableMapOf<CacheKey, List<Pattern>>()

    override fun clearSupportCache() = synchronized(supportCachedPatterns) {
        for (patterns in supportCachedPatterns.values) {
            patterns.forEach { it.close() }
        }

        supportCachedPatterns.clear()
    }

    private fun fileLoader(kind: SupportImageKind, name: String): List<Pattern> {
        val inputStreams = storageProvider.readSupportImage(kind, name)
        return inputStreams.withIndex().map { (i, stream) ->
            stream.use {
                DroidCvPattern(it, colorManager.isColor, "$name:$i")
            }
        }
    }

    override fun loadSupportPattern(kind: SupportImageKind, name: String): List<Pattern> = synchronized(supportCachedPatterns) {
        return supportCachedPatterns.getOrPut(key("$kind:$name")) {
            fileLoader(kind, name)
        }
    }

    override fun loadMaterial(material: MaterialEnum) =
        regionCachedPatterns.getOrPut(key("materials/$material")) {
            DroidCvPattern(
                Utils.loadResource(context, material.drawable, Imgcodecs.IMREAD_GRAYSCALE),
                tag = "MAT:$material"
            )
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
        MaterialEnum.CeremonialBlade -> R.drawable.mat_ceremonial_blade
        MaterialEnum.UnforgettableAshes -> R.drawable.mat_ashes

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
        MaterialEnum.ObsidianEdge -> R.drawable.mat_obsidian_edge
        MaterialEnum.VestigeOfMadness -> R.drawable.mat_vestige
        MaterialEnum.Sunscale -> R.drawable.mat_sunscale
        MaterialEnum.Converger -> R.drawable.mat_converger

        MaterialEnum.MonumentSaber -> R.drawable.mat_monument_saber
        MaterialEnum.MonumentArcher -> R.drawable.mat_monument_archer
        MaterialEnum.MonumentLancer -> R.drawable.mat_monument_lancer
        MaterialEnum.MonumentRider -> R.drawable.mat_monument_rider
        MaterialEnum.MonumentCaster -> R.drawable.mat_monument_caster
        MaterialEnum.MonumentAssassin -> R.drawable.mat_monument_assassin
        MaterialEnum.MonumentBerserker -> R.drawable.mat_monument_berserker

        MaterialEnum.PieceSaber -> R.drawable.mat_piece_saber
        MaterialEnum.PieceArcher -> R.drawable.mat_piece_archer
        MaterialEnum.PieceLancer -> R.drawable.mat_piece_lancer
        MaterialEnum.PieceRider -> R.drawable.mat_piece_rider
        MaterialEnum.PieceCaster -> R.drawable.mat_piece_caster
        MaterialEnum.PieceAssassin -> R.drawable.mat_piece_assassin
        MaterialEnum.PieceBerserker -> R.drawable.mat_piece_berserker

        MaterialEnum.SkillGoldSaber -> R.drawable.mat_skill_gold_saber
        MaterialEnum.SkillGoldArcher -> R.drawable.mat_skill_gold_archer
        MaterialEnum.SkillGoldLancer -> R.drawable.mat_skill_gold_lancer
        MaterialEnum.SkillGoldRider -> R.drawable.mat_skill_gold_rider
        MaterialEnum.SkillGoldCaster -> R.drawable.mat_skill_gold_caster
        MaterialEnum.SkillGoldAssassin -> R.drawable.mat_skill_gold_assassin
        MaterialEnum.SkillGoldBerserker -> R.drawable.mat_skill_gold_berserker

        MaterialEnum.SkillRedSaber -> R.drawable.mat_skill_red_saber
        MaterialEnum.SkillRedArcher -> R.drawable.mat_skill_red_archer
        MaterialEnum.SkillRedLancer -> R.drawable.mat_skill_red_lancer
        MaterialEnum.SkillRedRider -> R.drawable.mat_skill_red_rider
        MaterialEnum.SkillRedCaster -> R.drawable.mat_skill_red_caster
        MaterialEnum.SkillRedAssassin -> R.drawable.mat_skill_red_assassin
        MaterialEnum.SkillRedBerserker -> R.drawable.mat_skill_red_berserker

        MaterialEnum.SkillBlueSaber -> R.drawable.mat_skill_blue_saber
        MaterialEnum.SkillBlueArcher -> R.drawable.mat_skill_blue_archer
        MaterialEnum.SkillBlueLancer -> R.drawable.mat_skill_blue_lancer
        MaterialEnum.SkillBlueRider -> R.drawable.mat_skill_blue_rider
        MaterialEnum.SkillBlueCaster -> R.drawable.mat_skill_blue_caster
        MaterialEnum.SkillBlueAssassin -> R.drawable.mat_skill_blue_assassin
        MaterialEnum.SkillBlueBerserker -> R.drawable.mat_skill_blue_berserker
    }