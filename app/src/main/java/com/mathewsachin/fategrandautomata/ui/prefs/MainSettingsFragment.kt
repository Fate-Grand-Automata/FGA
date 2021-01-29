package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.MainFragmentDirections
import com.mathewsachin.fategrandautomata.ui.prefs.compose.ComposePreferencesTheme
import com.mathewsachin.fategrandautomata.ui.prefs.compose.Preference
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.nav
import com.mathewsachin.fategrandautomata.util.registerPersistableDirPicker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainSettingsFragment : Fragment() {
    @Inject
    lateinit var storageProvider: StorageProvider

    val vm: MainSettingsViewModel by activityViewModels()

    fun goToBattleConfigList() {
        val action = MainFragmentDirections
            .actionMainFragmentToBattleConfigListFragment()

        nav(action)
    }

    private val pickDir = registerPersistableDirPicker {
        storageProvider.setRoot(it)

        goToBattleConfigList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                ComposePreferencesTheme {
                    Surface {
                        ScrollableColumn {
                            val refillMsg by vm.refillMessage.collectAsState("")

                            Preference(
                                title = stringResource(R.string.p_refill),
                                summary = refillMsg,
                                icon = vectorResource(R.drawable.ic_apple),
                                onClick = {
                                    val action = MainFragmentDirections
                                        .actionMainFragmentToRefillSettingsFragment()

                                    nav(action)
                                }
                            )

                            Preference(
                                title = stringResource(R.string.p_battle_config),
                                summary = stringResource(R.string.p_battle_config_summary),
                                icon = vectorResource(R.drawable.ic_formation),
                                onClick = {
                                    if (vm.ensureRootDir(pickDir, requireContext())) {
                                        goToBattleConfigList()
                                    }
                                }
                            )

                            Preference(
                                title = stringResource(R.string.p_nav_troubleshoot),
                                icon = vectorResource(R.drawable.ic_troubleshooting),
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(getString(R.string.link_troubleshoot))
                                    )

                                    startActivity(intent)
                                }
                            )

                            Preference(
                                title = stringResource(R.string.p_more_options),
                                icon = vectorResource(R.drawable.ic_dots_horizontal),
                                onClick = {
                                    val action = MainFragmentDirections
                                        .actionMainFragmentToMoreSettingsFragment()

                                    nav(action)
                                }
                            )
                        }
                    }
                }
            }
        }
}
