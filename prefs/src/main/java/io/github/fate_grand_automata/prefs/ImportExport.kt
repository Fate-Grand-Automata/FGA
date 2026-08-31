package io.github.fate_grand_automata.prefs

import android.content.SharedPreferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

fun SharedPreferences.Editor.import(map: Map<String, *>) {
    for ((key, value) in map) {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Collection<*> -> putStringSet(key, value.map { it.toString() }.toSet())
        }
    }
}

/**
 * The `.fga` file format: one battle config's whole [SharedPreferences] map as a flat JSON object.
 *
 * [PrefMaker][io.github.fate_grand_automata.prefs.core.PrefMaker] only ever stores strings, ints,
 * booleans and string sets, and those are the types [import] puts back, so those are the types
 * that survive a round trip. Other values are still written out, but importing skips them.
 */
object BattleConfigFile {
    private val json = Json

    fun encode(values: Map<String, *>): String {
        val entries = values.mapNotNull { (key, value) ->
            value.toJson()?.let { key to it }
        }

        return json.encodeToString(JsonObject.serializer(), JsonObject(entries.toMap()))
    }

    /** @throws kotlinx.serialization.SerializationException if [text] is not a JSON object. */
    fun decode(text: String): Map<String, Any> =
        json.parseToJsonElement(text).jsonObject
            .mapNotNull { (key, element) ->
                element.toPrefValue()?.let { key to it }
            }
            .toMap()

    private fun Any?.toJson(): JsonElement? = when (this) {
        is String -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Collection<*> -> JsonArray(map { JsonPrimitive(it.toString()) })
        else -> null
    }

    /*
     * JSON has one number type, so the whole-number cases have to be picked apart by hand:
     * reading them all back as doubles - which is what an untyped parse gives you - would make
     * every int-valued preference unimportable.
     */
    private fun JsonElement.toPrefValue(): Any? = when {
        this is JsonArray -> mapNotNull { (it as? JsonPrimitive)?.content }
        this !is JsonPrimitive || this is JsonNull -> null
        isString -> content
        else -> booleanOrNull ?: intOrNull ?: longOrNull ?: doubleOrNull
    }
}
