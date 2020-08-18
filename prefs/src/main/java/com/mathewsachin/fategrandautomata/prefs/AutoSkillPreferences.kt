package com.mathewsachin.fategrandautomata.prefs

import android.content.Context
import androidx.core.content.edit
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.core.AutoSkillPrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences

const val defaultCardPriority = "WB, WA, WQ, B, A, Q, RB, RA, RQ"

internal class AutoSkillPreferences(
    override val id: String,
    val context: Context,
    val storageDirs: StorageDirs
) : IAutoSkillPreferences {
    private val prefs = AutoSkillPrefsCore(id, context, storageDirs)

    override var name by prefs.name

    override var skillCommand by prefs.skillCommand

    override var cardPriority by prefs.cardPriority

    override val party by prefs.party

    override val support = SupportPreferences(prefs.support, storageDirs)

    override val skill1Max by prefs.skill1Max
    override val skill2Max by prefs.skill2Max
    override val skill3Max by prefs.skill3Max

    override fun export(): Map<String, *> = prefs.sharedPrefs.all

    override fun import(map: Map<String, *>) =
        prefs.sharedPrefs.edit {
            import(map)
        }

    override fun equals(other: Any?): Boolean {
        if (other is IAutoSkillPreferences) {
            return other.id == id
        }

        return super.equals(other)
    }

    override fun hashCode() = id.hashCode()
}