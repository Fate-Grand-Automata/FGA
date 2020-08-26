package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AutoSkillMakerSavedState(
    val skillCommand: MutableList<String> = mutableListOf(),
    val enemyTarget: Int = -1,
    val stage: Int = 1,
    val turn: Int = 1,
    val currentSkill: Char = '0',
    val npSequence: List<Char> = emptyList(),
    val cardsBeforeNp: Int = 0,
    val xSelectedParty: Int = 1,
    val xSelectedSub: Int = 1
) : Parcelable