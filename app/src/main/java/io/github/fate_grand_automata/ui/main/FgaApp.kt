package io.github.fate_grand_automata.ui.main

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
import androidx.core.net.toUri

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
                Route.Onboarding
            } else {
                Route.Home
            }
        ) {
            composable<Route.Home> {
                MainScreen(
                    vm = vm,
                    navigate = {
                        when (it) {
                            MainScreenDestinations.AccessibilitySettings -> {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            }

                            MainScreenDestinations.BattleConfigs -> {
                                navController.navigate(Route.BattleConfigList)
                            }

                            MainScreenDestinations.MoreOptions -> {
                                navController.navigate(Route.MoreOptions)
                            }

                            MainScreenDestinations.OverlaySettings -> {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri()
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
            composable<Route.Onboarding> {
                OnboardingScreen(
                    vm = hiltViewModel(),
                    navigateToHome = {
                        navController.navigate(Route.Home) {
                            // disables going back to onboarding from home screen
                            popUpTo<Route.Onboarding> {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable<Route.BattleConfigList> {
                BattleConfigListScreen(
                    vm = hiltViewModel(),
                    navigate = { navController.navigate(Route.BattleConfigItem(it)) }
                )
            }
            composable<Route.MoreOptions> {
                MoreOptionsScreen(
                    vm = hiltViewModel(),
                    navigateToFineTune = { navController.navigate(Route.FineTune) }
                )
            }
            composable<Route.FineTune> {
                FineTuneScreen(
                    vm = hiltViewModel()
                )
            }
            composable<Route.BattleConfigItem> { backStackEntry ->
                val id = backStackEntry.toRoute<Route.BattleConfigItem>().id

                BattleConfigScreen(
                    vm = hiltViewModel(),
                    navigate = {
                        when (it) {
                            BattleConfigDestination.Back -> navController.popBackStack()
                            BattleConfigDestination.CardPriority ->
                                navController.navigate(Route.CardPriority(id))

                            is BattleConfigDestination.Other -> {
                                navController.navigate(Route.BattleConfigItem(it.id)) {
                                    popUpTo<Route.BattleConfigList>()
                                }
                            }

                            BattleConfigDestination.PreferredSupport ->
                                navController.navigate(Route.PreferredSupport(id))

                            BattleConfigDestination.SkillMaker -> {
                                val intent = Intent(context, SkillMakerActivity::class.java).apply {
                                    putExtra(Route.BattleConfig.idArg, id)
                                }

                                context.startActivity(intent)
                            }

                            BattleConfigDestination.Spam -> navController.navigate(Route.Spam(id))
                        }
                    }
                )
            }
            composable<Route.CardPriority> {
                CardPriorityScreen(
                    vm = hiltViewModel()
                )
            }
            composable<Route.PreferredSupport> {
                PreferredSupportScreen(
                    vm = hiltViewModel(),
                    supportVm = supportVm
                )
            }
            composable<Route.Spam> {
                SpamScreen(
                    vm = hiltViewModel()
                )
            }
        }
    }
}
