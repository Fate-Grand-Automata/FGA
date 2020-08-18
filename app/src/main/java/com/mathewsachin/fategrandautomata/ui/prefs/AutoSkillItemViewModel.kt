package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class AutoSkillItemViewModel @Inject constructor(
    val preferences: IPreferences,
    val prefsCore: PrefsCore
) : ViewModel() {
    var key: String = ""

    private val prefs by lazy { prefsCore.forAutoSkillConfig(key) }

    val cardPriority by lazy {
        prefs
            .cardPriority
            .asFlow()
            .asLiveData()
    }

    val skillCommand by lazy {
        prefs
            .skillCommand
            .asFlow()
            .asLiveData()
    }

    val skillLevels by lazy {
        combine(
            prefs.skill1Max.asFlow(),
            prefs.skill2Max.asFlow(),
            prefs.skill3Max.asFlow()
        ) { s1, s2, s3 ->
            listOf(s1, s2, s3)
                .joinToString("/") {
                    if (it) "10" else "x"
                }
        }
            .asLiveData()
    }
}