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

    fun int(@StringRes key: Int, default: Int = 0) =
        Pref(flowPrefs.getInt(k(key), default))

    fun bool(@StringRes key: Int, default: Boolean = false) =
        Pref(flowPrefs.getBoolean(k(key), default))

    fun string(@StringRes key: Int, default: String = "") =
        Pref(flowPrefs.getString(k(key), default))

    private fun stringAsIntSerializer(default: Int) =
        object : Serializer<Int> {
            override fun deserialize(serialized: String) = serialized.toIntOrNull() ?: default
            override fun serialize(value: Int) = value.toString()
        }

    fun stringAsInt(@StringRes key: Int, default: Int = 0) =
        Pref(flowPrefs.getObject(k(key), stringAsIntSerializer(default), default))

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

    fun stringSet(@StringRes key: Int) =
        Pref(flowPrefs.getStringSet(k(key)))
}