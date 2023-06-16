package io.github.fate_grand_automata.ui.skill_maker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SkillMakerSavedState(
    val skillString: String? = null,
    val enemyTarget: Int? = null,
    val stage: Int = 1,
    val currentSkill: Char = '0',
    val currentIndex: Int = 0
) : Parcelable