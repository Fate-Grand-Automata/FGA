package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.MainFragmentDirections
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.nav
import com.mathewsachin.fategrandautomata.util.registerPersistableDirPicker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainSettingsFragment : PreferenceFragmentCompat() {
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

    private lateinit var navRefill: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        prefScreen {
            navRefill = blank {
                title = R.string.p_refill
                summary = R.string.p_refill_summary
                icon = R.drawable.ic_apple
            }.also {
                it.setOnPreferenceClickListener {
                    val action = MainFragmentDirections
                        .actionMainFragmentToRefillSettingsFragment()

                    nav(action)

                    true
                }
            }

            blank {
                title = R.string.p_battle_config
                summary = R.string.p_battle_config_summary
                icon = R.drawable.ic_formation
            }.setOnPreferenceClickListener {
                if (vm.ensureRootDir(pickDir, requireContext())) {
                    goToBattleConfigList()
                }

                true
            }

            blank {
                title = R.string.p_nav_troubleshoot
                icon = R.drawable.ic_troubleshooting
            }.intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_troubleshoot)))

            blank {
                title = R.string.p_more_options
                icon = R.drawable.ic_dots_horizontal
            }.setOnPreferenceClickListener {
                val action = MainFragmentDirections
                    .actionMainFragmentToMoreSettingsFragment()

                nav(action)

                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.refillMessage.observe(viewLifecycleOwner) {
            navRefill.summary = it
        }
    }
}
