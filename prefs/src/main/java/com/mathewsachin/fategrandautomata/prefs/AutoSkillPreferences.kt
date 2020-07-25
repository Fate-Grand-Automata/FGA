package com.mathewsachin.fategrandautomata.prefs

import android.content.Context
import androidx.core.content.edit
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.helpers.SharedPreferenceDelegation
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences

const val defaultCardPriority = "WB, WA, WQ, B, A, Q, RB, RA, RQ"

internal class AutoSkillPreferences(
    override val id: String,
    val context: Context,
    val storageDirs: StorageDirs
) : IAutoSkillPreferences {
    private val prefs =
        SharedPreferenceDelegation(
            context.getSharedPreferences(
                id,
                Context.MODE_PRIVATE
            ),
            context
        )

    override val name by prefs.string(R.string.pref_autoskill_name, "--")

    override var skillCommand by prefs.string(R.string.pref_autoskill_cmd)

    override var cardPriority by prefs.string(
        R.string.pref_card_priority,
        defaultCardPriority
    )

    override val party by prefs.stringAsInt(R.string.pref_autoskill_party, -1)

    override val support =
        SupportPreferences(
            prefs,
            storageDirs
        )

    override fun export(): Map<String, *> =
        prefs.prefs.export()

    override fun import(map: Map<String, *>) =
        prefs.prefs.edit(commit = true) {
            import(map)
        }

    override fun equals(other: Any?): Boolean {
        if (other is IAutoSkillPreferences) {
            return other.id == id
        }

        return super.equals(other)
    }
}