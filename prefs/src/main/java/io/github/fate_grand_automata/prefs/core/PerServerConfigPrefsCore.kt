package io.github.fate_grand_automata.prefs.core

import android.content.Context
import android.content.SharedPreferences
import com.fredporciuncula.flow.preferences.Serializer
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum

class PerServerConfigPrefsCore(
    val server: GameServer,
    val context: Context
) {
    val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        server.simple,
        Context.MODE_PRIVATE
    )

    private val maker = PrefMaker(sharedPrefs)

    val selectedAutoSkillConfig = maker.string("autoskill_selected")

    val rainbowAppleCount = maker.stringAsInt("rainbowApple", 0)
    val goldAppleCount = maker.stringAsInt("goldApple", 0)
    val silverAppleCount = maker.stringAsInt("silverApple", 0)
    val blueAppleCount = maker.stringAsInt("blueApple", 0)
    val copperAppleCount = maker.stringAsInt("copperApple", 0)

    val waitAPRegen = maker.bool("wait_for_ap_regeneration")


    val selectedApple = maker.serialized(
        "selected_apple",
        serializer = object : Serializer<RefillResourceEnum> {
            override fun deserialize(serialized: String): RefillResourceEnum = try {
                RefillResourceEnum.valueOf(serialized)
            } catch (e: IllegalArgumentException) {
                RefillResourceEnum.Copper
            }

            override fun serialize(value: RefillResourceEnum): String = value.toString()

        },
        default = RefillResourceEnum.Copper
    )


    val refill = RefillPrefsCore(maker)

    val exitOnPresetQuest = maker.bool("exit_on_preset_quest")
}