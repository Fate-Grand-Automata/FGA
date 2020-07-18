package com.mathewsachin.fategrandautomata.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class DelegatedPref<T>(
    val prefs: SharedPreferences,
    val key: String,
    val default: T,
    val getter: SharedPreferences.(String, T) -> T,
    val setter: SharedPreferences.Editor.(String, T) -> Unit
) : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>) =
        prefs.getter(key, default)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        prefs.edit(commit = true) { setter(key, value) }
}

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
}