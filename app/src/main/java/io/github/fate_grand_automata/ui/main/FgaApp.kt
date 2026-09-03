package io.github.fate_grand_automata.ui.main

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.FgaScreen
import io.github.fate_grand_automata.ui.battle_config_item.BattleConfigDestination
import io.github.fate_grand_automata.ui.battle_config_item.BattleConfigScreen
import io.github.fate_grand_automata.ui.battle_config_item.BattleConfigScreenViewModel
import io.github.fate_grand_automata.ui.battle_config_list.BattleConfigListScreen
import io.github.fate_grand_automata.ui.card_priority.CardPriorityScreen
import io.github.fate_grand_automata.ui.card_priority.CardPriorityViewModel
import io.github.fate_grand_automata.ui.fine_tune.FineTuneScreen
import io.github.fate_grand_automata.ui.more.MoreOptionsScreen
import io.github.fate_grand_automata.ui.onboarding.OnboardingScreen
import io.github.fate_grand_automata.ui.openLinkIntent
import io.github.fate_grand_automata.ui.pref_support.PreferredSupportScreen
import io.github.fate_grand_automata.ui.pref_support.PreferredSupportViewModel
import io.github.fate_grand_automata.ui.pref_support.SupportViewModel
import io.github.fate_grand_automata.ui.skill_maker.SkillMakerActivity
import io.github.fate_grand_automata.ui.spam.SpamScreen
import io.github.fate_grand_automata.ui.spam.SpamScreenViewModel
import androidx.core.net.toUri

@Composable
fun FgaApp(
    vm: MainScreenViewModel,
    supportVm: SupportViewModel
) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(
        if (vm.prefs.isOnboardingRequired()) {
            Route.Onboarding
        } else {
            Route.Home
        }
    )

    FgaScreen {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = {
                // slide in from right
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                // slide out to right
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                // slide out to right
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
            },
            entryProvider = entryProvider {
                entry<Route.Home> {
                    MainScreen(
                        vm = vm,
                        navigate = {
                            when (it) {
                                MainScreenDestinations.AccessibilitySettings -> {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                }

                                MainScreenDestinations.BattleConfigs -> {
                                    backStack.add(Route.BattleConfigList)
                                }

                                MainScreenDestinations.MoreOptions -> {
                                    backStack.add(Route.MoreOptions)
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
                entry<Route.Onboarding> {
                    OnboardingScreen(
                        vm = hiltViewModel(),
                        navigateToHome = {
                            // disables going back to onboarding from home screen
                            backStack.clear()
                            backStack.add(Route.Home)
                        }
                    )
                }
                entry<Route.BattleConfigList> {
                    BattleConfigListScreen(
                        vm = hiltViewModel(),
                        navigate = { backStack.add(Route.BattleConfigItem(it)) }
                    )
                }
                entry<Route.MoreOptions> {
                    MoreOptionsScreen(
                        vm = hiltViewModel(),
                        navigateToFineTune = { backStack.add(Route.FineTune) }
                    )
                }
                entry<Route.FineTune> {
                    FineTuneScreen(
                        vm = hiltViewModel()
                    )
                }
                entry<Route.BattleConfigItem> { key ->
                    val id = key.id

                    BattleConfigScreen(
                        vm = hiltViewModel<BattleConfigScreenViewModel, BattleConfigScreenViewModel.Factory>(
                            creationCallback = { it.create(id) }
                        ),
                        navigate = {
                            when (it) {
                                BattleConfigDestination.Back -> backStack.removeLastOrNull()
                                BattleConfigDestination.CardPriority ->
                                    backStack.add(Route.CardPriority(id))

                                is BattleConfigDestination.Other -> {
                                    // replaces the current battle config screen with another one,
                                    // discarding any of its child screens (Card Priority, etc.)
                                    val listIndex =
                                        backStack.indexOfLast { it is Route.BattleConfigList }
                                    check(listIndex >= 0) {
                                        "Route.BattleConfigList not found below $it on the back stack"
                                    }
                                    backStack.subList(listIndex + 1, backStack.size).clear()
                                    backStack.add(Route.BattleConfigItem(it.id))
                                }

                                BattleConfigDestination.PreferredSupport ->
                                    backStack.add(Route.PreferredSupport(id))

                                BattleConfigDestination.SkillMaker -> {
                                    val intent = Intent(context, SkillMakerActivity::class.java).apply {
                                        putExtra(Route.BattleConfig.idArg, id)
                                    }

                                    context.startActivity(intent)
                                }

                                BattleConfigDestination.Spam -> backStack.add(Route.Spam(id))
                            }
                        }
                    )
                }
                entry<Route.CardPriority> { key ->
                    CardPriorityScreen(
                        vm = hiltViewModel<CardPriorityViewModel, CardPriorityViewModel.Factory>(
                            creationCallback = { it.create(key.id) }
                        )
                    )
                }
                entry<Route.PreferredSupport> { key ->
                    PreferredSupportScreen(
                        vm = hiltViewModel<PreferredSupportViewModel, PreferredSupportViewModel.Factory>(
                            creationCallback = { it.create(key.id) }
                        ),
                        supportVm = supportVm
                    )
                }
                entry<Route.Spam> { key ->
                    SpamScreen(
                        vm = hiltViewModel<SpamScreenViewModel, SpamScreenViewModel.Factory>(
                            creationCallback = { it.create(key.id) }
                        )
                    )
                }
            }
        )
    }
}
