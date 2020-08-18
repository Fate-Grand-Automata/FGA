package com.mathewsachin.fategrandautomata.prefs.core

import com.tfcporciuncula.flow.Preference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Pref<T>(private val pref: Preference<T>) :
    ReadWriteProperty<Any, T>,
    Preference<T> by pref {
    override fun getValue(thisRef: Any, property: KProperty<*>) =
        pref.get()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        pref.set(value)

    fun resetToDefault() = pref.set(pref.defaultValue)
}