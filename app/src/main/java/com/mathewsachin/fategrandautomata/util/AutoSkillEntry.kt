package com.mathewsachin.fategrandautomata.util

import androidx.appcompat.app.AppCompatActivity
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringPref
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringSetPref

data class AutoSkillEntry(val Id: String, val Name: String)

fun getAutoSkillEntries() = getStringSetPref(R.string.pref_autoskill_list)
    .map {
        val sharedPrefs = AutomataApplication.Instance
            .getSharedPreferences(it, AppCompatActivity.MODE_PRIVATE)

        AutoSkillEntry(
            it,
            getStringPref(R.string.pref_autoskill_name, "--", Prefs = sharedPrefs)
        )
    }
    .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.Name })