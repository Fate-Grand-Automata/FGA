package com.mathewsachin.fategrandautomata.scripts.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.AutomataApplication

private val context get(): Context = AutomataApplication.Instance

val defaultPrefs: SharedPreferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(context)
}

fun applyDefaults() {
    if (!defaultPrefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
        val prefFiles = arrayOf(
            R.xml.main_preferences,
            R.xml.app_preferences,
            R.xml.refill_preferences,
            R.xml.fine_tune_preferences
        )

        for (prefFile in prefFiles) {
            PreferenceManager.setDefaultValues(context, prefFile, true)
        }
    }
}

private fun k(KeyId: Int) = context.getString(KeyId)

fun getBoolPref(
    Key: Int,
    Default: Boolean = false,
    Prefs: SharedPreferences = defaultPrefs
): Boolean {
    return Prefs.getBoolean(k(Key), Default)
}

fun getStringPref(Key: Int, Default: String = "", Prefs: SharedPreferences = defaultPrefs): String {
    return Prefs.getString(k(Key), Default) ?: Default
}

fun getStringSetPref(Key: Int, Prefs: SharedPreferences = defaultPrefs): Set<String> {
    return Prefs.getStringSet(k(Key), emptySet()) ?: emptySet()
}

fun getIntPref(Key: Int, Default: Int = 0, Prefs: SharedPreferences = defaultPrefs): Int {
    return Prefs.getInt(k(Key), Default)
}

fun getStringAsIntPref(Key: Int, Default: Int = 0, Prefs: SharedPreferences = defaultPrefs): Int {
    val s = getStringPref(Key, Prefs = Prefs)

    return s.toIntOrNull() ?: Default
}

inline fun <reified T : Enum<T>> getEnumPref(
    Key: Int,
    Default: T,
    Prefs: SharedPreferences = defaultPrefs
): T {
    val s = getStringPref(Key, Prefs = Prefs)

    return try {
        enumValueOf(s)
    } catch (e: IllegalArgumentException) {
        Default
    }
}

fun getPrefsForSelectedAutoSkill(): SharedPreferences? {
    val selectedConfig = Preferences.SelectedAutoSkillConfig

    return if (selectedConfig.isNotBlank()) {
        context.getSharedPreferences(selectedConfig, Context.MODE_PRIVATE)
    } else null
}

const val defaultCardPriority = "WB, WA, WQ, B, A, Q, RB, RA, RQ"