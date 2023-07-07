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
                NavConstants.onboarding
            } else {
                NavConstants.home
            }
        ) {
            fun battleConfigComposable(
                route: String,
                content: @Composable (NavBackStackEntry, id: String) -> Unit
            ) {
                composable(
                    route = "$route/{${NavConstants.battleConfigIdKey}}",
                    arguments = listOf(navArgument(NavConstants.battleConfigIdKey) { type = NavType.StringType })
                ) {
                    content(it, it.arguments?.getString(NavConstants.battleConfigIdKey) ?: "")
                }
            }

            fun navigate(route: String, id: String, builder: NavOptionsBuilder.() -> Unit = { }) {
                navController.navigate("$route/$id", builder)
            }

            composable(NavConstants.home) {
                MainScreen(
                    vm = vm,
                    navigate = {
                        when (it) {
                            MainScreenDestinations.AccessibilitySettings -> {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            }

                            MainScreenDestinations.BattleConfigs -> {
                                navController.navigate(NavConstants.battleConfigs)
                            }

                            MainScreenDestinations.MoreOptions -> {
                                navController.navigate(NavConstants.moreOptions)
                            }

                            MainScreenDestinations.OverlaySettings -> {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )

                                context.startActivity(intent)
                            }

                            MainScreenDestinations.Releases -> {
                                openLinkIntent(context, R.string.link_releases)
                            }

                            MainScreenDestinations.TroubleshootingGuide -> {
                                openLinkIntent(context, R.string.link_troubleshoot)
                            }

                            MainScreenDestinations.Discord -> {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(context.getString(R.string.link_discord))
                                )

                                context.startActivity(intent)
                            }
                        }
                    }
                )
            }
            composable(NavConstants.onboarding) {
                OnboardingScreen(
                    vm = hiltViewModel(),
                    navigateToHome = { navController.navigate(NavConstants.home) }
                )
            }
            composable(NavConstants.battleConfigs) {
                BattleConfigListScreen(
                    vm = hiltViewModel(),
                    navigate = { navigate(NavConstants.battleConfigItem, it) }
                )
            }
            composable(NavConstants.moreOptions) {
                MoreOptionsScreen(
                    vm = hiltViewModel(),
                    navigateToFineTune = { navController.navigate(NavConstants.fineTune) }
                )
            }
            composable(NavConstants.fineTune) {
                FineTuneScreen(
                    vm = hiltViewModel()
                )
            }
            battleConfigComposable(NavConstants.battleConfigItem) { _, id ->
                BattleConfigScreen(
                    vm = hiltViewModel(),
                    supportVm = supportVm,
                    navigate = {
                        when (it) {
                            BattleConfigDestination.Back -> navController.popBackStack()
                            BattleConfigDestination.CardPriority -> navigate(NavConstants.cardPriority, id)
                            is BattleConfigDestination.Other -> {
                                navigate(NavConstants.battleConfigItem, it.id) {
                                    popUpTo(NavConstants.battleConfigs)
                                }
                            }

                            BattleConfigDestination.PreferredSupport -> navigate(NavConstants.preferredSupport, id)
                            BattleConfigDestination.SkillMaker -> {
                                val intent = Intent(context, SkillMakerActivity::class.java).apply {
                                    putExtra(NavConstants.battleConfigIdKey, id)
                                }

                                context.startActivity(intent)
                            }

                            BattleConfigDestination.Spam -> navigate(NavConstants.spam, id)
                        }
                    }
                )
            }
            battleConfigComposable(NavConstants.cardPriority) { _, _ ->
                CardPriorityScreen(
                    vm = hiltViewModel()
                )
            }
            battleConfigComposable(NavConstants.preferredSupport) { _, _ ->
                PreferredSupportScreen(
                    vm = hiltViewModel(),
                    supportVm = supportVm
                )
            }
            battleConfigComposable(NavConstants.spam) { _, _ ->
                SpamScreen(
                    vm = hiltViewModel()
                )
            }
        }
    }
}

object NavConstants {
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