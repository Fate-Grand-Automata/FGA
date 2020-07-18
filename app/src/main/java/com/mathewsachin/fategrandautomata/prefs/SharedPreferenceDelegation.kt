package com.mathewsachin.fategrandautomata.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import kotlin.properties.ReadWriteProperty

class SharedPreferenceDelegation(
    val prefs: SharedPreferences,
    val context: Context
) {
    fun int(@StringRes key: Int, default: Int = 0): ReadWriteProperty<Any, Int> =
        DelegatedPref(
            prefs,
            context.getString(key),
            default,
            SharedPreferences::getInt,
            SharedPreferences.Editor::putInt
        )

    fun bool(@StringRes key: Int, default: Boolean = false): ReadWriteProperty<Any, Boolean> =
        DelegatedPref(
            prefs,
            context.getString(key),
            default,
            SharedPreferences::getBoolean,
            SharedPreferences.Editor::putBoolean
        )

    fun string(@StringRes key: Int, default: String = ""): ReadWriteProperty<Any, String> =
        DelegatedPref(
            prefs,
            context.getString(key),
            default,
            { k, d -> getString(k, d) ?: d },
            SharedPreferences.Editor::putString
        )

    fun stringAsInt(@StringRes key: Int, default: Int = 0): ReadWriteProperty<Any, Int> =
        DelegatedPref(
            prefs,
            context.getString(key),
            default,
            { k, d -> getString(k, d.toString())?.toIntOrNull() ?: d },
            { k, v -> putString(k, v.toString()) }
        )

    inline fun <reified T : Enum<T>> enum(
        @StringRes key: Int,
        default: T
    ): ReadWriteProperty<Any, T> =
        DelegatedPref(
            prefs,
            context.getString(key),
            default,
            { k, d ->
                val defaultString = d.toString()

                try {
                    enumValueOf(getString(k, defaultString) ?: defaultString)
                } catch (e: IllegalArgumentException) {
                    d
                }
            },
            { k, v -> putString(k, v.toString()) }
        )

    fun stringSet(@StringRes key: Int): ReadWriteProperty<Any, Set<String>> =
        DelegatedPref<Set<String>>(
            prefs,
            context.getString(key),
            emptySet(),
            { k, d -> getStringSet(k, d) ?: d },
            SharedPreferences.Editor::putStringSet
        )
}