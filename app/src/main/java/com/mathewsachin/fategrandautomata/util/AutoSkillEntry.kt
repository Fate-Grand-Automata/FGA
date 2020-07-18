package com.mathewsachin.fategrandautomata.util

import com.mathewsachin.fategrandautomata.prefs.AutoSkillPreferences
import com.mathewsachin.fategrandautomata.prefs.Preferences

fun getAutoSkillEntries() = Preferences.autoSkillList.map {
    AutoSkillPreferences(
        it,
        AutomataApplication.Instance
    )
}
    .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })