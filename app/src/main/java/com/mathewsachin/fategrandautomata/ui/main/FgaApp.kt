package com.mathewsachin.fategrandautomata.ui.main

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
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.FgaScreen
import com.mathewsachin.fategrandautomata.ui.battle_config_item.BattleConfigDestination
import com.mathewsachin.fategrandautomata.ui.battle_config_item.BattleConfigScreen
import com.mathewsachin.fategrandautomata.ui.battle_config_list.BattleConfigListScreen
import com.mathewsachin.fategrandautomata.ui.card_priority.CardPriorityScreen
import com.mathewsachin.fategrandautomata.ui.fine_tune.FineTuneScreen
import com.mathewsachin.fategrandautomata.ui.more.MoreOptionsScreen
import com.mathewsachin.fategrandautomata.ui.pref_support.PreferredSupportScreen
import com.mathewsachin.fategrandautomata.ui.pref_support.SupportViewModel
import com.mathewsachin.fategrandautomata.ui.skill_maker.SkillMakerActivity
import com.mathewsachin.fategrandautomata.ui.spam.SpamScreen

@Composable
fun FgaApp(
    vm: MainScreenViewModel,
    supportVm: SupportViewModel
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    FgaScreen {
        NavHost(navController = navController, startDestination = NavConstants.home) {
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
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(context.getString(R.string.link_releases))
                                )

                                context.startActivity(intent)
                            }
                            MainScreenDestinations.TroubleshootingGuide -> {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(context.getString(R.string.link_troubleshoot))
                                )

                                context.startActivity(intent)
                            }
                        }
                    }
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
}