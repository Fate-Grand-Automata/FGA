package com.mathewsachin.fategrandautomata.prefs.helpers

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class DelegatedPref<T>(
    val prefs: SharedPreferences,
    val key: String,
    val default: T,
    val getter: SharedPreferences.(String, T) -> T,
    val setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
) : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>) =
        prefs.getter(key, default)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        prefs.edit { setter(key, value) }
}