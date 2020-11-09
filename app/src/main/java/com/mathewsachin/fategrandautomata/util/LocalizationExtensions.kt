package com.mathewsachin.fategrandautomata.util

import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.*

val RefillResourceEnum.stringRes
    get() = when (this) {
        RefillResourceEnum.SQ -> R.string.p_refill_type_sq
        RefillResourceEnum.Gold -> R.string.p_refill_type_gold
        RefillResourceEnum.Silver -> R.string.p_refill_type_silver
        RefillResourceEnum.Bronze -> R.string.p_refill_type_bronze
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
        SupportSelectionModeEnum.Friend -> R.string.p_support_mode_friend
        SupportSelectionModeEnum.Preferred -> R.string.p_support_mode_preferred
    }

val ShuffleCardsEnum.stringRes
    get() = when (this) {
        ShuffleCardsEnum.None -> R.string.p_shuffle_cards_when_none
        ShuffleCardsEnum.NoEffective -> R.string.p_shuffle_cards_when_no_effective
        ShuffleCardsEnum.NoNPMatching -> R.string.p_shuffle_cards_when_no_np_matching
    }

val MaterialEnum.stringRes
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
    }