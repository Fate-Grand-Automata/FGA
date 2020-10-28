package com.mathewsachin.fategrandautomata.prefs

import androidx.core.content.edit
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.prefs.core.map
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig

const val defaultCardPriority = "WB, WA, WQ, B, A, Q, RB, RA, RQ"

internal class BattleConfig(
    override val id: String,
    prefsCore: PrefsCore,
    val storageDirs: StorageDirs
) : IBattleConfig {
    val prefs = prefsCore.forBattleConfig(id)

    override var name by prefs.name

    override var skillCommand by prefs.skillCommand

    override var cardPriority by prefs.cardPriority

    override val rearrangeCards get() = prefs.rearrangeCards

    override val braveChains get() = prefs.braveChains

    override val party by prefs.party

    override val materials by prefs.materials.map {
        it.mapNotNull { mat ->
            try {
                enumValueOf<MaterialEnum>(mat)
            } catch (e: Exception) {
                null
            }
        }
    }

    override val support = SupportPreferences(prefs.support, storageDirs)

    override val npSpam by prefs.npSpam

    override val skillSpam by prefs.skillSpam

    override val autoChooseTarget by prefs.autoChooseTarget

    override fun export(): Map<String, *> = prefs.sharedPrefs.all

    override fun import(map: Map<String, *>) =
        prefs.sharedPrefs.edit {
            import(map)
        }

    override fun equals(other: Any?): Boolean {
        if (other is IBattleConfig) {
            return other.id == id
        }

        return super.equals(other)
    }

    override fun hashCode() = id.hashCode()
}