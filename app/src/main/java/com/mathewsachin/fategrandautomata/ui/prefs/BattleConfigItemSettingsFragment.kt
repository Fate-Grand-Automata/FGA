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
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.error
import java.util.*
import javax.inject.Inject
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

@AndroidEntryPoint
class BattleConfigItemSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferences: IPreferences

    @Inject
    lateinit var storageDirs: StorageDirs

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

    private fun findFriendNamesList(): MultiSelectListPreference? =
        findPreference(getString(prefKeys.pref_support_friend_names))

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = args.key
        battleConfig = preferences.forBattleConfig(args.key)

        setHasOptionsMenu(true)

        setPreferencesFromResource(R.xml.battle_config_preferences, rootKey)

        findFriendNamesList()?.summaryProvider = SupportMultiSelectSummaryProvider()

        findPreference<EditTextPreference>(getString(R.string.pref_battle_config_notes))?.makeMultiLine()

        findPreference<Preference>(getString(prefKeys.pref_card_priority))?.let {
            it.setOnPreferenceClickListener {
                val action = BattleConfigItemSettingsFragmentDirections
                    .actionBattleConfigItemSettingsFragmentToCardPriorityFragment(args.key)

                nav(action)

                true
            }
        }

        findPreference<EditTextPreference>(getString(prefKeys.pref_battle_config_cmd))?.let {
            it.setOnPreferenceClickListener {
                if (!prefsCore.showTextBoxForSkillCmd.get()) {
                    val action = BattleConfigItemSettingsFragmentDirections
                        .actionBattleConfigItemSettingsFragmentToBattleConfigMakerActivity(args.key)

                    nav(action)
                }

                true
            }
        }

        findPreference<Preference>(getString(R.string.pref_nav_preferred_support))?.let {
            it.setOnPreferenceClickListener {
                val action = BattleConfigItemSettingsFragmentDirections
                    .actionBattleConfigItemSettingsFragmentToPreferredSupportSettingsFragment(args.key)

                nav(action)

                true
            }
        }

        listOf(R.string.pref_spam_np, R.string.pref_spam_skill)
            .mapNotNull { findPreference<ListPreference>(getString(it)) }
            .forEach { pref ->
                pref.initWith<SpamEnum> { it.stringRes }
            }

        findPreference<ListPreference>(getString(R.string.pref_battle_config_support_class))
            ?.initWith<SupportClass> { it.stringRes }

        findPreference<ListPreference>(getString(R.string.pref_battle_config_party))?.apply {
            entries = arrayOf(getString(R.string.p_not_set)) +
                    (1..10).map {
                        context.getString(R.string.p_party_number, it)
                    }

            entryValues = (-1..9)
                .map { it.toString() }
                .toTypedArray()
        }

        findPreference<ListPreference>(getString(R.string.pref_support_mode))
            ?.initWith<SupportSelectionModeEnum> { it.stringRes }

        findPreference<ListPreference>(getString(R.string.pref_support_fallback))?.apply {
            val values = listOf(
                SupportSelectionModeEnum.First,
                SupportSelectionModeEnum.Manual
            )

            entryValues = values
                .map { it.toString() }
                .toTypedArray()

            entries = values
                .map { context.getString(it.stringRes) }
                .toTypedArray()
        }

        findPreference<MultiSelectListPreference>(getString(R.string.pref_battle_config_mat))?.apply {
            initWith<MaterialEnum> { it.stringRes }
            summaryProvider = MultiSelectSummaryProvider()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: BattleConfigItemViewModel by viewModels()

        findPreference<Preference>(getString(prefKeys.pref_card_priority))?.let {
            vm.cardPriority.observe(viewLifecycleOwner) { priority ->
                it.summary = priority
            }
        }

        findPreference<EditTextPreference>(getString(R.string.pref_battle_config_cmd))?.let {
            vm.skillCommand.observe(viewLifecycleOwner) { cmd ->
                it.text = cmd
            }
        }

        val navPreferred = findPreference<Preference>(getString(R.string.pref_nav_preferred_support))
        val friendNames = findFriendNamesList()
        val fallback = findPreference<Preference>(getString(prefKeys.pref_support_fallback))

        vm.preferredMessage.observe(viewLifecycleOwner) { msg ->
            navPreferred?.summary = msg
        }

        vm.supportSelectionMode.observe(viewLifecycleOwner) {
            val preferred = it == SupportSelectionModeEnum.Preferred
            val friend = it == SupportSelectionModeEnum.Friend

            friendNames?.isVisible = friend
            fallback?.isVisible = preferred || friend
            navPreferred?.isVisible = preferred
        }
    }

    override fun onResume() {
        super.onResume()

        if (storageDirs.shouldExtractSupportImages) {
            performSupportImageExtraction()
        } else populateFriendNames()
    }

    private fun populateFriendNames() {
        findFriendNamesList()?.apply {
            populateFriendOrCe(storageDirs.supportFriendFolder)
        }
    }

    private fun performSupportImageExtraction() {
        lifecycleScope.launch {
            val msg = try {
                SupportImageExtractor(requireContext(), storageDirs).extract()
                populateFriendNames()

                getString(R.string.support_imgs_extracted)
            } catch (e: Exception) {
                getString(R.string.support_imgs_extract_failed).also { msg ->
                    Timber.error(e) { msg }
                }
            }

            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.battle_config_item_menu, menu)
        inflater.inflate(R.menu.support_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_support_extract_defaults -> {
                performSupportImageExtraction()
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
    }

    private fun deleteItem(battleConfigKey: String) {
        preferences.removeBattleConfig(battleConfigKey)

        findNavController().popBackStack()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        fun prepare(dialogFragment: PreferenceDialogFragmentCompat) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, null)
        }

        when (preference.key) {
            getString(R.string.pref_support_friend_names) -> {
                ClearMultiSelectListPreferenceDialog().apply {
                    setKey(preference.key)
                    prepare(this)
                }
            }
            getString(R.string.pref_battle_config_cmd) -> {
                if (prefsCore.showTextBoxForSkillCmd.get()) {
                    SkillCmdPreferenceDialogFragment().apply {
                        battleConfigKey = args.key
                        prepare(this)
                    }
                }
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }
}