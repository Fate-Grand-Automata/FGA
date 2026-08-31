package io.github.fate_grand_automata.ui.main

import kotlinx.serialization.Serializable

/** The destinations of the [FgaApp] navigation graph. */
sealed interface Route {
    /**
     * The screens that edit one battle config.
     *
     * Navigation stores each route field in the destination's `SavedStateHandle` under its own
     * property name. `ViewModelProvidesModule` reads the config id from there by name, which is
     * what lets it serve all four of these screens without knowing which one is on top - so the
     * property has to keep the name [BattleConfig.idArg].
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
