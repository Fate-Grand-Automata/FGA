package com.mathewsachin.fategrandautomata.prefs.core

import android.content.SharedPreferences
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.fredporciuncula.flow.preferences.Serializer

class PrefMaker(
    val prefs: SharedPreferences
) {
    val flowPrefs = FlowSharedPreferences(prefs)

    fun int(key: String, default: Int = 0): Pref<Int> =
        PrefImpl(flowPrefs.getInt(key, default))

    fun bool(key: String, default: Boolean = false): Pref<Boolean> =
        PrefImpl(flowPrefs.getBoolean(key, default))

    fun string(key: String, default: String = ""): Pref<String> =
        PrefImpl(flowPrefs.getString(key, default))

    fun <T : Any> serialized(key: String, serializer: Serializer<T>, default: T): Pref<T> =
        PrefImpl(flowPrefs.getObject(key, serializer, default))

    private fun stringAsIntSerializer(default: Int) =
        object : Serializer<Int> {
            override fun deserialize(serialized: String) = serialized.toIntOrNull() ?: default
            override fun serialize(value: Int) = value.toString()
        }

    fun stringAsInt(key: String, default: Int = 0) =
        serialized(key, stringAsIntSerializer(default), default)

    inline fun <reified T : Enum<T>> enum(
        key: String,
        default: T
    ): Pref<T> {
        val serializer = object : Serializer<T> {
            override fun deserialize(serialized: String) =
                try {
                    enumValueOf(serialized)
                } catch (e: Exception) {
                    default
                }

            override fun serialize(value: T) = value.name
        }

        return serialized(key, serializer, default)
    }

    fun stringSet(key: String): Pref<Set<String>> =
        PrefImpl(flowPrefs.getStringSet(key))
}