package com.mathewsachin.fategrandautomata.prefs.core

import android.content.SharedPreferences
import com.tfcporciuncula.flow.FlowSharedPreferences
import com.tfcporciuncula.flow.Serializer

class PrefMaker(
    val prefs: SharedPreferences
) {
    val flowPrefs = FlowSharedPreferences(prefs)

    fun int(key: String, default: Int = 0) =
        Pref(flowPrefs.getInt(key, default))

    fun bool(key: String, default: Boolean = false) =
        Pref(flowPrefs.getBoolean(key, default))

    fun string(key: String, default: String = "") =
        Pref(flowPrefs.getString(key, default))

    private fun stringAsIntSerializer(default: Int) =
        object : Serializer<Int> {
            override fun deserialize(serialized: String) = serialized.toIntOrNull() ?: default
            override fun serialize(value: Int) = value.toString()
        }

    fun stringAsInt(key: String, default: Int = 0) =
        Pref(flowPrefs.getObject(key, stringAsIntSerializer(default), default))

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

        return Pref(flowPrefs.getObject(key, serializer, default))
    }

    fun stringSet(key: String) =
        Pref(flowPrefs.getStringSet(key))
}