package io.github.fate_grand_automata.ui.main

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The destinations of the [FgaApp] navigation graph. */
sealed interface Route : NavKey {
    /**
     * The screens that edit one battle config.
     *
     * [BattleConfig.idArg] also names the `Intent` extra `SkillMakerActivity` is launched with,
     * since that's a separate Activity outside the nav graph.
     */
    sealed interface BattleConfig : Route {
        val id: String

        companion object {
            const val idArg = "id"
        }
    }

    @Serializable
    data object Home : Route

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object BattleConfigList : Route

    @Serializable
    data object MoreOptions : Route

    @Serializable
    data object FineTune : Route

    @Serializable
    data class BattleConfigItem(override val id: String) : BattleConfig

    @Serializable
    data class CardPriority(override val id: String) : BattleConfig

    @Serializable
    data class PreferredSupport(override val id: String) : BattleConfig

    @Serializable
    data class Spam(override val id: String) : BattleConfig
}
