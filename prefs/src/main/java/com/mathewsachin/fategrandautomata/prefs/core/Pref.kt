package com.mathewsachin.fategrandautomata.prefs.core

import com.fredporciuncula.flow.preferences.Preference
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.map
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Pref<T> : ReadWriteProperty<Any, T>, Preference<T> {
    fun resetToDefault()
}

internal class PrefImpl<T>(private val pref: Preference<T>) :
    Pref<T>,
    Preference<T> by pref {
    override fun getValue(thisRef: Any, property: KProperty<*>) =
        pref.get()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        pref.set(value)

    override fun resetToDefault() = pref.set(pref.defaultValue)
}

internal fun <T, V> ReadWriteProperty<Any, T>.map(func: (T) -> V): ReadOnlyProperty<Any, V> =
    ReadOnlyProperty { thisRef, property -> func(this@map.getValue(thisRef, property)) }

fun <T, R> Pref<T>.map(
    defaultValue: R,
    convert: (T) -> R,
    reverse: (R) -> T
): Pref<R> =
    MappedPref(
        this,
        defaultValue,
        convert,
        reverse
    )

internal class MappedPref<T, R>(
    private val pref: Pref<T>,
    override val defaultValue: R,
    private val convert: (T) -> R,
    private val reverse: (R) -> T
) : Pref<R> {
    override fun getValue(thisRef: Any, property: KProperty<*>) = get()
    override fun setValue(thisRef: Any, property: KProperty<*>, value: R) = set(value)
    override val key get() = pref.key
    override fun delete() = pref.delete()
    override suspend fun deleteAndCommit() = pref.deleteAndCommit()
    override fun isNotSet() = pref.isNotSet()
    override fun isSet() = pref.isSet()
    override fun resetToDefault() = pref.resetToDefault()

    override fun asCollector() =
        object : FlowCollector<R> {
            val baseCollector = pref.asCollector()

            override suspend fun emit(value: R) =
                baseCollector.emit(reverse(value))
        }

    override fun asFlow() =
        pref.asFlow().map { convert(it) }

    override fun asSyncCollector(throwOnFailure: Boolean) =
        object : FlowCollector<R> {
            val baseCollector = pref.asSyncCollector()

            override suspend fun emit(value: R) =
                baseCollector.emit(reverse(value))
        }

    override fun get() = convert(pref.get())
    override fun set(value: R) = pref.set(reverse(value))
    override suspend fun setAndCommit(value: R) = pref.setAndCommit(reverse(value))
}