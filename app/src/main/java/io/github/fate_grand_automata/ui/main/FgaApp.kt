package io.github.fate_grand_automata.ui.main

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import io.github.fate_grand_automata.ui.material.MaterialScreen
import io.github.fate_grand_automata.ui.more.MoreOptionsScreen
import io.github.fate_grand_automata.ui.onboarding.OnboardingScreen
import io.github.fate_grand_automata.ui.openLinkIntent
import io.github.fate_grand_automata.ui.pref_support.PreferredSupportScreen
import io.github.fate_grand_automata.ui.pref_support.SupportViewModel
import io.github.fate_grand_automata.ui.release_notes.ReleaseNotesScreen
import io.github.fate_grand_automata.ui.skill_maker.SkillMakerActivity
import io.github.fate_grand_automata.ui.spam.SpamScreen

@Composable
fun FgaApp(
    windowSizeClass: WindowSizeClass,
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

                            MainScreenDestinations.ReleaseNotes -> {
                                navController.navigate(NavConstants.releaseNotes)
                            }
                        }
                    }
                )
            }
            composable(NavConstants.releaseNotes) {
                ReleaseNotesScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(NavConstants.onboarding) {
                OnboardingScreen(
                    vm = hiltViewModel(),
                    navigateToHome = {
                        navController.navigate(NavConstants.home) {
                            // disables going back to onboarding from home screen
                            popUpTo(NavConstants.onboarding) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(NavConstants.battleConfigs) {
                BattleConfigListScreen(
                    vm = hiltViewModel(),
                    windowSizeClass = windowSizeClass,
                    navigate = { navigate(NavConstants.battleConfigItem, it) },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
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
                    windowSizeClass = windowSizeClass,
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

                            BattleConfigDestination.Material -> navigate(NavConstants.materials, id)
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
            battleConfigComposable(NavConstants.materials){ _, _ ->
                MaterialScreen(
                    windowSizeClass = windowSizeClass,
                    vm = hiltViewModel(),
                    onNavigateBack = {
                        navController.popBackStack()
                    }
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
    const val releaseNotes = "releaseNotes"
    const val fineTune = "fineTune"
    const val cardPriority = "cardPriority"
    const val preferredSupport = "preferredSupport"
    const val spam = "spam"
    const val materials = "materials"
    const val onboarding = "onboarding"
}