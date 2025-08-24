package io.github.fate_grand_automata.ui.skill_maker.utils

import io.github.fate_grand_automata.R

val ChangeNp2Type.stringRes
    get() = when (this) {
        ChangeNp2Type.Generic -> R.string.skill_maker_change_np_type_2
        ChangeNp2Type.Emiya -> R.string.skill_maker_emiya
        ChangeNp2Type.BBDubai -> R.string.skill_maker_bb_dubai
    }

val ChangeNp2Type.targetAStringRes
    get() = when (this) {
        ChangeNp2Type.Generic -> R.string.skill_maker_option_1
        ChangeNp2Type.Emiya -> R.string.skill_maker_arts
        ChangeNp2Type.BBDubai -> R.string.skill_maker_bb_dubai_target_1
    }

val ChangeNp2Type.targetBStringRes
    get() = when (this) {
        ChangeNp2Type.Generic -> R.string.skill_maker_option_2
        ChangeNp2Type.Emiya -> R.string.skill_maker_buster
        ChangeNp2Type.BBDubai -> R.string.skill_maker_bb_dubai_target_2
    }


val ChangeNp3Type.stringRes
    get() = when (this) {
        ChangeNp3Type.Generic -> R.string.skill_maker_change_np_type_3
        ChangeNp3Type.SpaceIshtar -> R.string.skill_maker_space_ishtar
    }

val ChangeNp3Type.targetAStringRes
    get() = when (this) {
        ChangeNp3Type.Generic -> R.string.skill_maker_option_1
        ChangeNp3Type.SpaceIshtar -> R.string.skill_maker_quick
    }

val ChangeNp3Type.targetBStringRes
    get() = when (this) {
        ChangeNp3Type.Generic -> R.string.skill_maker_option_2
        ChangeNp3Type.SpaceIshtar -> R.string.skill_maker_arts
    }

val ChangeNp3Type.targetCStringRes
    get() = when (this) {
        ChangeNp3Type.Generic -> R.string.skill_maker_option_2
        ChangeNp3Type.SpaceIshtar -> R.string.skill_maker_buster
    }


val Choice3Type.stringRes
    get() = when (this) {
        Choice3Type.Generic -> R.string.skill_maker_choices_3
        Choice3Type.Hakuno -> R.string.skill_maker_hakuno
        Choice3Type.Soujuurou -> R.string.skill_maker_soujuurou
        Choice3Type.Charlotte -> R.string.skill_maker_charlotte
        Choice3Type.VanGogh -> R.string.skill_maker_van_gogh
    }

val Choice3Type.choice1StringRes
    get() = when (this) {
        Choice3Type.Generic -> R.string.skill_maker_option_1
        Choice3Type.Hakuno -> R.string.skill_maker_hakuno_choice_1
        Choice3Type.Soujuurou, Choice3Type.VanGogh -> R.string.skill_maker_quick
        Choice3Type.Charlotte -> R.string.skill_maker_arts
    }

val Choice3Type.choice2StringRes
    get() = when (this) {
        Choice3Type.Generic -> R.string.skill_maker_option_2
        Choice3Type.Hakuno -> R.string.skill_maker_hakuno_choice_2
        Choice3Type.Soujuurou, Choice3Type.VanGogh -> R.string.skill_maker_arts
        Choice3Type.Charlotte -> R.string.skill_maker_charlotte_choice_2
    }

val Choice3Type.choice3StringRes
    get() = when (this) {
        Choice3Type.Generic -> R.string.skill_maker_option_3
        Choice3Type.Hakuno -> R.string.skill_maker_hakuno_choice_3
        Choice3Type.Soujuurou, Choice3Type.VanGogh -> R.string.skill_maker_buster
        Choice3Type.Charlotte -> R.string.skill_maker_charlotte_choice_3
    }


val Choice2Type.stringRes
    get() = when (this) {
        Choice2Type.Generic -> R.string.skill_maker_choices_2
        Choice2Type.Kukulkan -> R.string.skill_maker_kukulkan
        Choice2Type.SummerShiki -> R.string.skill_maker_summer_shiki
        Choice2Type.UDKBarghest -> R.string.skill_maker_udk_barghest
        Choice2Type.Dante -> R.string.skill_maker_dante_alighieri
    }

val Choice2Type.targetAStringRes
    get() = when (this) {
        Choice2Type.Generic -> R.string.skill_maker_option_1
        Choice2Type.Kukulkan -> R.string.skill_maker_kukulkan_choice_1
        Choice2Type.SummerShiki -> R.string.skill_maker_summer_shiki_choice_1
        Choice2Type.UDKBarghest -> R.string.skill_maker_udk_barghest_choice_1
        Choice2Type.Dante -> R.string.skill_maker_dante_alighieri_choice_1
    }

val Choice2Type.targetBStringRes
    get() = when (this) {
        Choice2Type.Generic -> R.string.skill_maker_option_2
        Choice2Type.Kukulkan -> R.string.skill_maker_kukulkan_choice_2
        Choice2Type.SummerShiki -> R.string.skill_maker_summer_shiki_choice_2
        Choice2Type.UDKBarghest -> R.string.skill_maker_udk_barghest_choice_2
        Choice2Type.Dante -> R.string.skill_maker_dante_alighieri_choice_1
    }

val TransformType.stringRes
    get() = when (this) {
        TransformType.Melusine -> R.string.skill_maker_melusine
        TransformType.Ptolemy -> R.string.skill_maker_ptolemy
    }