package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.*
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.error
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BattleConfigItemSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferences: IPreferences

    @Inject
    lateinit var storageProvider: StorageProvider

    @Inject
    lateinit var prefsCore: PrefsCore

    val args: BattleConfigItemSettingsFragmentArgs by navArgs()

    val battleConfigExport = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) {
            try {
                val values = preferences.forBattleConfig(args.key).export()
                val gson = Gson()
                val json = gson.toJson(values)

                requireContext().contentResolver.openOutputStream(uri)?.use { outStream ->
                    outStream.writer().use { it.write(json) }
                }
            } catch (e: Exception) {
                Timber.error(e) { "Failed to export" }

                val msg = getString(R.string.battle_config_item_export_failed)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var battleConfig: IBattleConfig

    private lateinit var friendNamesList: MultiSelectListPreference
    private lateinit var fallbackMode: ListPreference
    private lateinit var prefCmd: EditTextPreference
    private lateinit var navCardPriority: Preference
    private lateinit var navPreferred: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = args.key
        battleConfig = preferences.forBattleConfig(args.key)

        setHasOptionsMenu(true)

        val config = prefsCore.forBattleConfig(args.key)

        prefScreen {
            config.name.text {
                title = R.string.p_battle_config_name
                icon = R.drawable.ic_text_short
            }

            prefCmd = config.skillCommand.text {
                title = R.string.p_battle_config_cmd
                icon = R.drawable.ic_terminal
            }.also {
                it.setOnPreferenceClickListener {
                    if (!prefsCore.showTextBoxForSkillCmd.get()) {
                        val action = BattleConfigItemSettingsFragmentDirections
                            .actionBattleConfigItemSettingsFragmentToBattleConfigMakerActivity(args.key)

                        nav(action)
                    }

                    true
                }
            }

            config.notes.text {
                title = R.string.p_battle_config_notes
                icon = R.drawable.ic_note
            }.also {
                it.makeMultiLine()
            }

            config.party.list {
                title = R.string.p_battle_config_party
                icon = R.drawable.ic_flag
            }.apply {
                entries = arrayOf(getString(R.string.p_not_set)) +
                        (1..10).map {
                            getString(R.string.p_party_number, it)
                        }

                entryValues = (-1..9)
                    .map { it.toString() }
                    .toTypedArray()
            }

            navCardPriority = blank {
                title = R.string.p_battle_config_card_priority
                icon = R.drawable.ic_sort
            }.also {
                it.setOnPreferenceClickListener {
                    val action = BattleConfigItemSettingsFragmentDirections
                        .actionBattleConfigItemSettingsFragmentToCardPriorityFragment(args.key)

                    nav(action)

                    true
                }
            }

            config.materials.multiSelect {
                title = R.string.p_mats
                icon = R.drawable.ic_fang
            }.apply {
                initWith<MaterialEnum> { it.stringRes }
                summaryProvider = MultiSelectSummaryProvider()
            }

            category {
                key = "support_category"
                title = R.string.p_battle_config_support

                config.support.supportClass.list {
                    title = R.string.p_battle_config_support_class
                    icon = R.drawable.ic_diamond
                }.initWith<SupportClass> { it.stringRes }

                config.support.selectionMode.list {
                    title = R.string.p_battle_config_support_selection_mode
                    icon = R.drawable.ic_dots_vertical
                }.initWith<SupportSelectionModeEnum> { it.stringRes }

                navPreferred = blank {
                    title = R.string.p_support_mode_preferred
                    icon = R.drawable.ic_card
                }.also {
                    it.setOnPreferenceClickListener {
                        val action = BattleConfigItemSettingsFragmentDirections
                            .actionBattleConfigItemSettingsFragmentToPreferredSupportSettingsFragment(args.key)

                        nav(action)

                        true
                    }
                }

                friendNamesList = config.support.friendNames.multiSelect {
                    title = R.string.p_battle_config_support_friend_names
                    icon = R.drawable.ic_friend
                }.also {
                    it.summaryProvider = SupportMultiSelectSummaryProvider()
                }

                fallbackMode = config.support.fallbackTo.list {
                    title = R.string.p_battle_config_support_fallback_selection_mode
                    icon = R.drawable.ic_dots_vertical
                }.apply {
                    val values = listOf(
                        SupportSelectionModeEnum.First,
                        SupportSelectionModeEnum.Manual
                    )

                    entryValues = values
                        .map { it.toString() }
                        .toTypedArray()

                    entries = values
                        .map { getString(it.stringRes) }
                        .toTypedArray()
                }
            }

            category {
                key = "spam_category"
                title = R.string.p_spam_spam
                summary = R.string.p_spam_summary

                config.autoChooseTarget.switch {
                    title = R.string.p_auto_choose_target
                    icon = R.drawable.ic_target
                }

                config.npSpam.list {
                    title = R.string.p_spam_np
                    icon = R.drawable.ic_star
                }.initWith<SpamEnum> { it.stringRes }

                config.skillSpam.list {
                    title = R.string.p_spam_skill
                    icon = R.drawable.ic_wand
                }.initWith<SpamEnum> { it.stringRes }
            }

            category {
                key = "shuffle_category"
                title = R.string.p_shuffle_cards

                config.shuffleCardsWave.seekBar {
                    title = R.string.p_shuffle_cards_wave
                    min = 1
                    max = 3
                    icon = R.drawable.ic_counter
                }

                config.shuffleCards.list {
                    title = R.string.p_shuffle_cards_when
                    icon = R.drawable.ic_refresh
                }.initWith<ShuffleCardsEnum> { it.stringRes }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: BattleConfigItemViewModel by viewModels()

        vm.cardPriority.observe(viewLifecycleOwner) {
            navCardPriority.summary = it
        }

        vm.skillCommand.observe(viewLifecycleOwner) {
            prefCmd.text = it
        }

        vm.preferredMessage.observe(viewLifecycleOwner) {
            navPreferred.summary = it
        }

        vm.supportSelectionMode.observe(viewLifecycleOwner) {
            val preferred = it == SupportSelectionModeEnum.Preferred
            val friend = it == SupportSelectionModeEnum.Friend

            friendNamesList.isVisible = friend
            fallbackMode.isVisible = preferred || friend
            navPreferred.isVisible = preferred
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            if (storageProvider.shouldExtractSupportImages) {
                performSupportImageExtraction()
            } else populateFriendNames()
        }
    }

    private suspend fun populateFriendNames() {
        friendNamesList.apply {
            populateFriendOrCe(storageProvider, SupportImageKind.Friend)

            this.dialogMessage = if (entries.isEmpty()) {
                getString(R.string.p_battle_config_support_friend_name_hint)
            } else null
        }
    }

    private suspend fun performSupportImageExtraction() {
        val msg = try {
            SupportImageExtractor(requireContext(), storageProvider).extract()
            populateFriendNames()

            getString(R.string.support_imgs_extracted)
        } catch (e: Exception) {
            getString(R.string.support_imgs_extract_failed).also { msg ->
                Timber.error(e) { msg }
            }
        }

        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.battle_config_item_menu, menu)
        inflater.inflate(R.menu.support_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_support_extract_defaults -> {
                lifecycleScope.launch {
                    performSupportImageExtraction()
                }
                true
            }
            R.id.action_battle_config_delete -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.battle_config_item_delete_confirm_message)
                    .setTitle(R.string.battle_config_item_delete_confirm_title)
                    .setPositiveButton(R.string.battle_config_item_delete_confirm_ok) { _, _ -> deleteItem(args.key) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }
            R.id.action_battle_config_export -> {
                battleConfigExport.launch("${battleConfig.name}.fga")
                true
            }
            R.id.action_battle_config_copy -> {
                val guid = UUID.randomUUID().toString()
                preferences.addBattleConfig(guid)
                val newConfig = preferences.forBattleConfig(guid)

                val map = battleConfig.export()
                newConfig.import(map)
                newConfig.name = getString(R.string.battle_config_item_copy_name, newConfig.name)

                val action = BattleConfigItemSettingsFragmentDirections
                    .actionBattleConfigItemSettingsFragmentSelf(guid)

                nav(action)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun deleteItem(battleConfigKey: String) {
        preferences.removeBattleConfig(battleConfigKey)

        findNavController().popBackStack()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        fun prepare(dialogFragment: PreferenceDialogFragmentCompat) {
            @Suppress("DEPRECATION")
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, null)
        }

        when (preference.key) {
            friendNamesList.key -> {
                ClearMultiSelectListPreferenceDialog().apply {
                    setKey(preference.key)
                    prepare(this)
                }
            }
            prefCmd.key -> {
                if (prefsCore.showTextBoxForSkillCmd.get()) {
                    SkillCmdPreferenceDialogFragment(prefCmd.key).apply {
                        battleConfigKey = args.key
                        prepare(this)
                    }
                }
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }
}