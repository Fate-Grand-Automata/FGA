package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SkillMakerSavedState(
    val skillString: String? = null,
    val enemyTarget: Int = -1,
    val stage: Int = 1,
    val currentSkill: Char = '0',
    val npSequence: List<Char> = emptyList(),
    val cardsBeforeNp: Int = 0,
    val xSelectedParty: Int = 1,
    val xSelectedSub: Int = 1,
    val currentIndex: Int = 0
) : Parcelable