package com.mathewsachin.fategrandautomata.prefs.core

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import com.tfcporciuncula.flow.FlowSharedPreferences
import com.tfcporciuncula.flow.Serializer

class PrefMaker(
    val prefs: SharedPreferences,
    val context: Context
) {
    val flowPrefs = FlowSharedPreferences(prefs)

    fun k(@StringRes key: Int) = context.getString(key)

    fun int(key: String, default: Int = 0) =
        Pref(flowPrefs.getInt(key, default))

    fun int(@StringRes key: Int, default: Int = 0) =
        int(k(key), default)

    fun bool(key: String, default: Boolean = false) =
        Pref(flowPrefs.getBoolean(key, default))

    fun bool(@StringRes key: Int, default: Boolean = false) =
        bool(k(key), default)

    fun string(key: String, default: String = "") =
        Pref(flowPrefs.getString(key, default))

    fun string(@StringRes key: Int, default: String = "") =
        string(k(key), default)

    private fun stringAsIntSerializer(default: Int) =
        object : Serializer<Int> {
            override fun deserialize(serialized: String) = serialized.toIntOrNull() ?: default
            override fun serialize(value: Int) = value.toString()
        }

    fun stringAsInt(key: String, default: Int = 0) =
        Pref(flowPrefs.getObject(key, stringAsIntSerializer(default), default))

    fun stringAsInt(@StringRes key: Int, default: Int = 0) =
        stringAsInt(k(key), default)

    inline fun <reified T : Enum<T>> enum(
        @StringRes key: Int,
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

        return Pref(flowPrefs.getObject(k(key), serializer, default))
    }

    fun stringSet(key: String) =
        Pref(flowPrefs.getStringSet(key))

    fun stringSet(@StringRes key: Int) =
        stringSet(k(key))
}