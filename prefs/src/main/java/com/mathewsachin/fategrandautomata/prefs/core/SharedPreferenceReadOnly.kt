package com.mathewsachin.fategrandautomata.prefs.core

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun <T, V> ReadWriteProperty<Any, T>.map(func: (T) -> V): ReadOnlyProperty<Any, V> =
    ReadOnlyProperty { thisRef, property -> func(this@map.getValue(thisRef, property)) }

internal fun <T, V> ReadWriteProperty<Any, T>.map(reader: (T) -> V, writer: (V) -> T): ReadWriteProperty<Any, V> =
    object : ReadWriteProperty<Any, V> {
        override fun getValue(thisRef: Any, property: KProperty<*>): V =
            reader(this@map.getValue(thisRef, property))

        override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
            this@map.setValue(thisRef, property, writer(value))
        }
    }