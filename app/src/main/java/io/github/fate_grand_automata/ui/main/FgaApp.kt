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
                NavConstants.ONBOARDING
            } else {
                NavConstants.HOME
            },
        ) {
            fun battleConfigComposable(
                route: String,
                content: @Composable (NavBackStackEntry, id: String) -> Unit
            ) {
                composable(
                    route = "$route/{${NavConstants.BATTLE_CONFIG_ID_KEY}}",
                    arguments = listOf(navArgument(NavConstants.BATTLE_CONFIG_ID_KEY) { type = NavType.StringType }),
                ) {
                    content(it, it.arguments?.getString(NavConstants.BATTLE_CONFIG_ID_KEY) ?: "")
                }
            }

            fun navigate(route: String, id: String, builder: NavOptionsBuilder.() -> Unit = { }) {
                navController.navigate("$route/$id", builder)
            }

            composable(NavConstants.HOME) {
                MainScreen(
                    vm = vm,
                    navigate = {
                        when (it) {
                            MainScreenDestinations.AccessibilitySettings -> {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            }

                            MainScreenDestinations.BattleConfigs -> {
                                navController.navigate(NavConstants.BATTLE_CONFIGS)
                            }

                            MainScreenDestinations.MoreOptions -> {
                                navController.navigate(NavConstants.MORE_OPTIONS)
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
                    },
                )
            }
            composable(NavConstants.ONBOARDING) {
                OnboardingScreen(
                    vm = hiltViewModel(),
                    navigateToHome = {
                        navController.navigate(NavConstants.HOME) {
                            // disables going back to onboarding from home screen
                            popUpTo(NavConstants.ONBOARDING) {
                                inclusive = true
                            }
                        }
                    },
                )
            }
            composable(NavConstants.BATTLE_CONFIGS) {
                BattleConfigListScreen(
                    vm = hiltViewModel(),
                    navigate = { navigate(NavConstants.BATTLE_CONFIG_ITEM, it) },
                )
            }
            composable(NavConstants.MORE_OPTIONS) {
                MoreOptionsScreen(
                    vm = hiltViewModel(),
                    navigateToFineTune = { navController.navigate(NavConstants.FINE_TUNE) },
                )
            }
            composable(NavConstants.FINE_TUNE) {
                FineTuneScreen(
                    vm = hiltViewModel()
                )
            }
            battleConfigComposable(NavConstants.BATTLE_CONFIG_ITEM) { _, id ->
                BattleConfigScreen(
                    vm = hiltViewModel(),
                    navigate = {
                        when (it) {
                            BattleConfigDestination.Back -> navController.popBackStack()
                            BattleConfigDestination.CardPriority -> navigate(NavConstants.CARD_PRIORITY, id)
                            is BattleConfigDestination.Other -> {
                                navigate(NavConstants.BATTLE_CONFIG_ITEM, it.id) {
                                    popUpTo(NavConstants.BATTLE_CONFIGS)
                                }
                            }

                            BattleConfigDestination.PreferredSupport -> navigate(NavConstants.PREFERRED_SUPPORT, id)
                            BattleConfigDestination.SkillMaker -> {
                                val intent = Intent(context, SkillMakerActivity::class.java).apply {
                                    putExtra(NavConstants.BATTLE_CONFIG_ID_KEY, id)
                                }

                                context.startActivity(intent)
                            }

                            BattleConfigDestination.Spam -> navigate(NavConstants.SPAM, id)
                        }
                    },
                )
            }
            battleConfigComposable(NavConstants.CARD_PRIORITY) { _, _ ->
                CardPriorityScreen(
                    vm = hiltViewModel()
                )
            }
            battleConfigComposable(NavConstants.PREFERRED_SUPPORT) { _, _ ->
                PreferredSupportScreen(
                    vm = hiltViewModel(),
                    supportVm = supportVm
                )
            }
            battleConfigComposable(NavConstants.SPAM) { _, _ ->
                SpamScreen(
                    vm = hiltViewModel()
                )
            }
        }
    }
}

object NavConstants {
    const val HOME = "home"
    const val BATTLE_CONFIGS = "configs"
    const val BATTLE_CONFIG_ITEM = "configItem"
    const val BATTLE_CONFIG_ID_KEY = "id"
    const val MORE_OPTIONS = "more"
    const val FINE_TUNE = "fineTune"
    const val CARD_PRIORITY = "cardPriority"
    const val PREFERRED_SUPPORT = "preferredSupport"
    const val SPAM = "spam"
    const val ONBOARDING = "onboarding"
}
