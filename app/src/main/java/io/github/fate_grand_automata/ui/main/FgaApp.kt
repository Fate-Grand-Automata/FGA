package io.github.fate_grand_automata.ui.main

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.FgaScreen
import io.github.fate_grand_automata.ui.battle_config_item.BattleConfigDestination
import io.github.fate_grand_automata.ui.battle_config_item.BattleConfigScreen
import io.github.fate_grand_automata.ui.battle_config_list.BattleConfigListScreen
import io.github.fate_grand_automata.ui.card_priority.CardPriorityScreen
import io.github.fate_grand_automata.ui.fine_tune.FineTuneScreen
import io.github.fate_grand_automata.ui.more.MoreOptionsScreen
import io.github.fate_grand_automata.ui.onboarding.OnboardingScreen
import io.github.fate_grand_automata.ui.openLinkIntent
import io.github.fate_grand_automata.ui.pref_support.PreferredSupportScreen
import io.github.fate_grand_automata.ui.pref_support.SupportViewModel
import io.github.fate_grand_automata.ui.skill_maker.SkillMakerActivity
import io.github.fate_grand_automata.ui.spam.SpamScreen

@Composable
fun FgaApp(
    vm: MainScreenViewModel,
    supportVm: SupportViewModel
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    FgaScreen {
        NavHost(
            navController = navController,
            startDestination = if (vm.prefs.isOnboardingRequired()) {
                NAV_CONSTANTS.onboarding
            } else {
                NAV_CONSTANTS.home
            }
        ) {
            fun battleConfigComposable(
                route: String,
                content: @Composable (NavBackStackEntry, id: String) -> Unit
            ) {
                composable(
                    route = "$route/{${NAV_CONSTANTS.battleConfigIdKey}}",
                    arguments = listOf(navArgument(NAV_CONSTANTS.battleConfigIdKey) { type = NavType.StringType })
                ) {
                    content(it, it.arguments?.getString(NAV_CONSTANTS.battleConfigIdKey) ?: "")
                }
            }

            fun navigate(route: String, id: String, builder: NavOptionsBuilder.() -> Unit = { }) {
                navController.navigate("$route/$id", builder)
            }

            composable(NAV_CONSTANTS.home) {
                MainScreen(
                    vm = vm,
                    navigate = {
                        when (it) {
                            MainScreenDestinations.AccessibilitySettings -> {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            }

                            MainScreenDestinations.BattleConfigs -> {
                                navController.navigate(NAV_CONSTANTS.battleConfigs)
                            }

                            MainScreenDestinations.MoreOptions -> {
                                navController.navigate(NAV_CONSTANTS.moreOptions)
                            }

                            MainScreenDestinations.OverlaySettings -> {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )

                                context.startActivity(intent)
                            }

                            MainScreenDestinations.Releases -> {
                                context.openLinkIntent(R.string.link_releases)
                            }

                            MainScreenDestinations.TroubleshootingGuide -> {
                                context.openLinkIntent(R.string.link_troubleshoot)
                            }

                            MainScreenDestinations.Discord -> {
                                context.openLinkIntent(R.string.link_discord)
                            }

                            MainScreenDestinations.Donate -> {
                                context.openLinkIntent(R.string.link_donate)
                            }
                        }
                    }
                )
            }
            composable(NAV_CONSTANTS.onboarding) {
                OnboardingScreen(
                    vm = hiltViewModel(),
                    navigateToHome = {
                        navController.navigate(NAV_CONSTANTS.home) {
                            // disables going back to onboarding from home screen
                            popUpTo(NAV_CONSTANTS.onboarding) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(NAV_CONSTANTS.battleConfigs) {
                BattleConfigListScreen(
                    vm = hiltViewModel(),
                    navigate = { navigate(NAV_CONSTANTS.battleConfigItem, it) }
                )
            }
            composable(NAV_CONSTANTS.moreOptions) {
                MoreOptionsScreen(
                    vm = hiltViewModel(),
                    navigateToFineTune = { navController.navigate(NAV_CONSTANTS.fineTune) }
                )
            }
            composable(NAV_CONSTANTS.fineTune) {
                FineTuneScreen(
                    vm = hiltViewModel()
                )
            }
            battleConfigComposable(NAV_CONSTANTS.battleConfigItem) { _, id ->
                BattleConfigScreen(
                    vm = hiltViewModel(),
                    navigate = {
                        when (it) {
                            BattleConfigDestination.Back -> navController.popBackStack()
                            BattleConfigDestination.CardPriority -> navigate(NAV_CONSTANTS.cardPriority, id)
                            is BattleConfigDestination.Other -> {
                                navigate(NAV_CONSTANTS.battleConfigItem, it.id) {
                                    popUpTo(NAV_CONSTANTS.battleConfigs)
                                }
                            }

                            BattleConfigDestination.PreferredSupport -> navigate(NAV_CONSTANTS.preferredSupport, id)
                            BattleConfigDestination.SkillMaker -> {
                                val intent = Intent(context, SkillMakerActivity::class.java).apply {
                                    putExtra(NAV_CONSTANTS.battleConfigIdKey, id)
                                }

                                context.startActivity(intent)
                            }

                            BattleConfigDestination.Spam -> navigate(NAV_CONSTANTS.spam, id)
                        }
                    }
                )
            }
            battleConfigComposable(NAV_CONSTANTS.cardPriority) { _, _ ->
                CardPriorityScreen(
                    vm = hiltViewModel()
                )
            }
            battleConfigComposable(NAV_CONSTANTS.preferredSupport) { _, _ ->
                PreferredSupportScreen(
                    vm = hiltViewModel(),
                    supportVm = supportVm
                )
            }
            battleConfigComposable(NAV_CONSTANTS.spam) { _, _ ->
                SpamScreen(
                    vm = hiltViewModel()
                )
            }
        }
    }
}

object NAV_CONSTANTS {
    const val home = "home"
    const val battleConfigs = "configs"
    const val battleConfigItem = "configItem"
    const val battleConfigIdKey = "id"
    const val moreOptions = "more"
    const val fineTune = "fineTune"
    const val cardPriority = "cardPriority"
    const val preferredSupport = "preferredSupport"
    const val spam = "spam"
    const val onboarding = "onboarding"
}
