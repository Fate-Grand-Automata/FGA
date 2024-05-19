package io.github.fate_grand_automata.util

import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.GameAreaMode
import io.github.fate_grand_automata.scripts.enums.BattleConfigListSortEnum
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.scripts.enums.ShuffleCardsEnum
import io.github.fate_grand_automata.scripts.enums.SpamEnum
import io.github.fate_grand_automata.scripts.enums.SupportClass
import io.github.fate_grand_automata.scripts.enums.SupportSelectionModeEnum

val RefillResourceEnum.stringRes
    get() = when (this) {
        RefillResourceEnum.SQ -> R.string.p_refill_type_sq
        RefillResourceEnum.Gold -> R.string.p_refill_type_gold
        RefillResourceEnum.Silver -> R.string.p_refill_type_silver
        RefillResourceEnum.Bronze -> R.string.p_refill_type_bronze
        RefillResourceEnum.Copper -> R.string.p_refill_type_copper
    }

val BraveChainEnum.stringRes
    get() = when (this) {
        BraveChainEnum.None -> R.string.p_brave_chains_don_t_care
        BraveChainEnum.WithNP -> R.string.p_brave_chains_with_np
        BraveChainEnum.Avoid -> R.string.p_brave_chains_avoid
    }

val SpamEnum.stringRes
    get() = when (this) {
        SpamEnum.None -> R.string.p_spam_none
        SpamEnum.Spam -> R.string.p_spam_spam
        SpamEnum.Danger -> R.string.p_spam_danger
    }

val SupportClass.stringRes
    get() = when (this) {
        SupportClass.None -> R.string.p_support_class_none
        SupportClass.All -> R.string.p_support_class_all
        SupportClass.Saber -> R.string.p_support_class_saber
        SupportClass.Archer -> R.string.p_support_class_archer
        SupportClass.Lancer -> R.string.p_support_class_lancer
        SupportClass.Rider -> R.string.p_support_class_rider
        SupportClass.Caster -> R.string.p_support_class_caster
        SupportClass.Assassin -> R.string.p_support_class_assassin
        SupportClass.Berserker -> R.string.p_support_class_berserker
        SupportClass.Extra -> R.string.p_support_class_extra
        SupportClass.Mix -> R.string.p_support_class_mix
    }

val SupportSelectionModeEnum.stringRes
    get() = when (this) {
        SupportSelectionModeEnum.First -> R.string.p_support_mode_first
        SupportSelectionModeEnum.Manual -> R.string.p_support_mode_manual
        SupportSelectionModeEnum.Preferred -> R.string.p_support_mode_preferred
    }

val ShuffleCardsEnum.stringRes
    get() = when (this) {
        ShuffleCardsEnum.None -> R.string.p_shuffle_cards_when_none
        ShuffleCardsEnum.NoEffective -> R.string.p_shuffle_cards_when_no_effective
        ShuffleCardsEnum.NoNPMatching -> R.string.p_shuffle_cards_when_no_np_matching
    }

val MaterialEnum.stringRes: Int
    get() = when (this) {
        MaterialEnum.Proof -> R.string.mat_proof
        MaterialEnum.Bone -> R.string.mat_bone
        MaterialEnum.Fang -> R.string.mat_fang
        MaterialEnum.Dust -> R.string.mat_dust
        MaterialEnum.Chain -> R.string.mat_chain
        MaterialEnum.Fluid -> R.string.mat_fluid
        MaterialEnum.Seed -> R.string.mat_seed
        MaterialEnum.GhostLantern -> R.string.mat_ghost_lantern
        MaterialEnum.Feather -> R.string.mat_feather
        MaterialEnum.Page -> R.string.mat_page
        MaterialEnum.Magatama -> R.string.mat_magatama
        MaterialEnum.GiantRing -> R.string.mat_giant_ring
        MaterialEnum.Claw -> R.string.mat_claw
        MaterialEnum.Heart -> R.string.mat_heart
        MaterialEnum.SpiritRoot -> R.string.mat_spirit_root
        MaterialEnum.Scarab -> R.string.mat_scarab
        MaterialEnum.OctupletCrystal -> R.string.mat_octuplet
        MaterialEnum.SerpentJewel -> R.string.mat_serpent_jewel
        MaterialEnum.Gear -> R.string.mat_gear
        MaterialEnum.HomunculusBaby -> R.string.mat_homunculus
        MaterialEnum.Horseshoe -> R.string.mat_horseshoe
        MaterialEnum.ShellOfReminiscence -> R.string.mat_shell
        MaterialEnum.DragonScale -> R.string.mat_scale
        MaterialEnum.YoungHorn -> R.string.mat_young_horn
        MaterialEnum.TearStone -> R.string.mat_tear_stone
        MaterialEnum.Grease -> R.string.mat_grease
        MaterialEnum.Lanugo -> R.string.mat_lanugo
        MaterialEnum.Gallstone -> R.string.mat_gallstone
        MaterialEnum.MysteriousWine -> R.string.mat_wine
        MaterialEnum.Stake -> R.string.mat_stake
        MaterialEnum.Gunpowder -> R.string.mat_gunpowder
        MaterialEnum.Medal -> R.string.mat_medal
        MaterialEnum.LampOfEvilSealing -> R.string.mat_lamp_evil_sealing
        MaterialEnum.Stinger -> R.string.mat_stinger
        MaterialEnum.EternalIce -> R.string.mat_eternal_ice
        MaterialEnum.AuroraSteel -> R.string.mat_steel
        MaterialEnum.ReactorCore -> R.string.mat_core
        MaterialEnum.SoundlessBell -> R.string.mat_bell
        MaterialEnum.Arrowhead -> R.string.mat_arrow
        MaterialEnum.Tiara -> R.string.mat_tiara
        MaterialEnum.DivineSpiritParticle -> R.string.mat_particle
        MaterialEnum.RainbowThreadBall -> R.string.mat_thread
        MaterialEnum.TsukumoMirror -> R.string.mat_mirror
        MaterialEnum.EggOfTruth -> R.string.mat_egg
        MaterialEnum.StarShard -> R.string.mat_star_shard
        MaterialEnum.FruitOfEternity -> R.string.mat_fruit
        MaterialEnum.DemonFlameLantern -> R.string.mat_demon_lantern
        MaterialEnum.AmnestyBell -> R.string.mat_amnesty_bell
        MaterialEnum.FantasyScales -> R.string.mat_fantasy_scales
        MaterialEnum.CeremonialBlade -> R.string.mat_ceremonial_blade
        MaterialEnum.UnforgettableAshes -> R.string.mat_ashes
        MaterialEnum.ObsidianEdge -> R.string.mat_obsidian_edge
        MaterialEnum.VestigeOfMadness -> R.string.mat_vestige_of_madness
        MaterialEnum.Sunscale -> R.string.mat_sunscale
        MaterialEnum.Converger -> R.string.mat_converger

        MaterialEnum.MonumentSaber -> R.string.mat_monument_saber
        MaterialEnum.MonumentArcher -> R.string.mat_monument_archer
        MaterialEnum.MonumentLancer -> R.string.mat_monument_lancer
        MaterialEnum.MonumentRider -> R.string.mat_monument_rider
        MaterialEnum.MonumentCaster -> R.string.mat_monument_caster
        MaterialEnum.MonumentAssassin -> R.string.mat_monument_asssassin
        MaterialEnum.MonumentBerserker -> R.string.mat_monument_berserker

        MaterialEnum.PieceSaber -> R.string.mat_piece_saber
        MaterialEnum.PieceArcher -> R.string.mat_piece_archer
        MaterialEnum.PieceLancer -> R.string.mat_piece_lancer
        MaterialEnum.PieceRider -> R.string.mat_piece_rider
        MaterialEnum.PieceCaster -> R.string.mat_piece_caster
        MaterialEnum.PieceAssassin -> R.string.mat_piece_asssassin
        MaterialEnum.PieceBerserker -> R.string.mat_piece_berserker

        MaterialEnum.SkillGoldSaber -> R.string.mat_skill_gold_saber
        MaterialEnum.SkillGoldArcher -> R.string.mat_skill_gold_archer
        MaterialEnum.SkillGoldLancer -> R.string.mat_skill_gold_lancer
        MaterialEnum.SkillGoldRider -> R.string.mat_skill_gold_rider
        MaterialEnum.SkillGoldCaster -> R.string.mat_skill_gold_caster
        MaterialEnum.SkillGoldAssassin -> R.string.mat_skill_gold_asssassin
        MaterialEnum.SkillGoldBerserker -> R.string.mat_skill_gold_berserker

        MaterialEnum.SkillRedSaber -> R.string.mat_skill_red_saber
        MaterialEnum.SkillRedArcher -> R.string.mat_skill_red_archer
        MaterialEnum.SkillRedLancer -> R.string.mat_skill_red_lancer
        MaterialEnum.SkillRedRider -> R.string.mat_skill_red_rider
        MaterialEnum.SkillRedCaster -> R.string.mat_skill_red_caster
        MaterialEnum.SkillRedAssassin -> R.string.mat_skill_red_asssassin
        MaterialEnum.SkillRedBerserker -> R.string.mat_skill_red_berserker

        MaterialEnum.SkillBlueSaber -> R.string.mat_skill_blue_saber
        MaterialEnum.SkillBlueArcher -> R.string.mat_skill_blue_archer
        MaterialEnum.SkillBlueLancer -> R.string.mat_skill_blue_lancer
        MaterialEnum.SkillBlueRider -> R.string.mat_skill_blue_rider
        MaterialEnum.SkillBlueCaster -> R.string.mat_skill_blue_caster
        MaterialEnum.SkillBlueAssassin -> R.string.mat_skill_blue_asssassin
        MaterialEnum.SkillBlueBerserker -> R.string.mat_skill_blue_berserker
    }

val GameAreaMode.stringRes
    get() = when (this) {
        GameAreaMode.Default -> R.string.p_game_area_default
        GameAreaMode.Duo -> R.string.p_game_area_duo
        GameAreaMode.Custom -> R.string.p_game_area_custom
    }

val GameServer.stringRes
    get() = when (this) {
        is GameServer.En -> if (this.betterFgo) R.string.game_server_na_bfgo else R.string.game_server_na
        is GameServer.Jp -> if (this.betterFgo) R.string.game_server_jp_bfgo else R.string.game_server_jp
        else -> this.simpleStringRes
    }

val GameServer.simpleStringRes
    get() = when (this) {
        GameServer.Cn -> R.string.game_server_cn
        GameServer.Tw -> R.string.game_server_tw
        GameServer.Kr -> R.string.game_server_kr
        is GameServer.En -> R.string.game_server_na
        is GameServer.Jp -> R.string.game_server_jp
    }


val BattleConfigListSortEnum.stringRes
    get() = when (this) {
        BattleConfigListSortEnum.DEFAULT_SORT -> R.string.p_battle_config_sort_default
        BattleConfigListSortEnum.SORT_BY_NAME_DESC -> R.string.p_battle_config_sort_by_name_desc
        BattleConfigListSortEnum.SORT_BY_USAGE_COUNT_ASC -> R.string.p_battle_config_sort_by_usage_count_asc
        BattleConfigListSortEnum.SORT_BY_USAGE_COUNT_DESC -> R.string.p_battle_config_sort_by_usage_count_desc
        BattleConfigListSortEnum.SORT_BY_LAST_USAGE_TIME_ASC -> R.string.p_battle_config_sort_by_last_usage_time_asc
        BattleConfigListSortEnum.SORT_BY_LAST_USAGE_TIME_DESC -> R.string.p_battle_config_sort_by_last_usage_time_desc
    }