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
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.*
import javax.inject.Inject
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

private val logger = KotlinLogging.logger {}

@AndroidEntryPoint
class AutoSkillItemSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferences: IPreferences

    @Inject
    lateinit var storageDirs: StorageDirs

    @Inject
    lateinit var prefsCore: PrefsCore

    val args: AutoSkillItemSettingsFragmentArgs by navArgs()

    val autoSkillExport = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) {
            try {
                val values = preferences.forAutoSkillConfig(args.key).export()
                val gson = Gson()
                val json = gson.toJson(values)

                requireContext().contentResolver.openOutputStream(uri)?.use { outStream ->
                    outStream.writer().use { it.write(json) }
                }
            } catch (e: Exception) {
                logger.error("Failed to export", e)

                val msg = getString(R.string.auto_skill_item_export_failed)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var autoSkillPrefs: IAutoSkillPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = args.key
        autoSkillPrefs = preferences.forAutoSkillConfig(args.key)

        setHasOptionsMenu(true)

        setPreferencesFromResource(R.xml.autoskill_item_preferences, rootKey)

        preferredSupportOnCreate()

        findPreference<EditTextPreference>(getString(R.string.pref_autoskill_notes))?.makeMultiLine()

        findPreference<Preference>(getString(prefKeys.pref_card_priority))?.let {
            it.setOnPreferenceClickListener {
                val action = AutoSkillItemSettingsFragmentDirections
                    .actionAutoSkillItemSettingsFragmentToCardPriorityFragment(args.key)

                findNavController().navigate(action)

                true
            }
        }

        findPreference<EditTextPreference>(getString(prefKeys.pref_autoskill_cmd))?.let {
            it.setOnPreferenceClickListener {
                if (!prefsCore.showTextBoxForAutoSkillCmd.get()) {
                    val action = AutoSkillItemSettingsFragmentDirections
                        .actionAutoSkillItemSettingsFragmentToAutoSkillMakerActivity(args.key)

                    findNavController().navigate(action)
                }

                true
            }
        }

        findPreference<Preference>(getString(R.string.pref_nav_skill_lvl))?.let {
            it.setOnPreferenceClickListener {
                val action = AutoSkillItemSettingsFragmentDirections
                    .actionAutoSkillItemSettingsFragmentToSkillLevelSettingsFragment(args.key)

                findNavController().navigate(action)

                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: AutoSkillItemViewModel by viewModels()

        findPreference<Preference>(getString(prefKeys.pref_card_priority))?.let {
            vm.cardPriority.observe(viewLifecycleOwner) { priority ->
                it.summary = priority
            }
        }

        findPreference<EditTextPreference>(getString(R.string.pref_autoskill_cmd))?.let {
            vm.skillCommand.observe(viewLifecycleOwner) { cmd ->
                it.text = cmd
            }
        }

        findPreference<Preference>(getString(R.string.pref_nav_skill_lvl))?.let {
            vm.skillLevels.observe(viewLifecycleOwner) { levels ->
                it.summary = levels
            }

            val maxAscended = findPreference<Preference>(getString(R.string.pref_support_max_ascended))
                ?: return

            vm.areServantsSelected.observe(viewLifecycleOwner) { visible ->
                it.isVisible = visible
                maxAscended.isVisible = visible
            }
        } ?: return

        val servants = findServantList() ?: return
        val ces = findCeList() ?: return
        val friendNames = findFriendNamesList() ?: return
        val friendsOnly =
            findPreference<Preference>(getString(prefKeys.pref_support_friends_only)) ?: return
        val fallback = findPreference<Preference>(getString(prefKeys.pref_support_fallback)) ?: return

        vm.supportSelectionMode.observe(viewLifecycleOwner) {
            val preferred = it == SupportSelectionModeEnum.Preferred
            val friend = it == SupportSelectionModeEnum.Friend

            servants.isVisible = preferred
            ces.isVisible = preferred
            friendsOnly.isVisible = preferred

            friendNames.isVisible = friend

            fallback.isVisible = preferred || friend
        }

        findPreference<Preference>(getString(prefKeys.pref_support_pref_ce_mlb))?.let {
            vm.areCEsSelected.observe(viewLifecycleOwner) { visible ->
                it.isVisible = visible
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (storageDirs.shouldExtractSupportImages) {
            performSupportImageExtraction()
        } else preferredSupportOnResume(storageDirs)
    }

    private fun performSupportImageExtraction() {
        lifecycleScope.launch {
            val msg = try {
                SupportImageExtractor(requireContext(), storageDirs).extract()
                preferredSupportOnResume(storageDirs)

                getString(R.string.support_imgs_extracted)
            } catch (e: Exception) {
                getString(R.string.support_imgs_extract_failed).also { msg ->
                    logger.error(msg, e)
                }
            }

            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.autoskill_item_menu, menu)
        inflater.inflate(R.menu.support_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_support_extract_defaults -> {
                performSupportImageExtraction()
                true
            }
            R.id.action_auto_skill_delete -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.auto_skill_item_delete_confirm_message)
                    .setTitle(R.string.auto_skill_item_delete_confirm_title)
                    .setPositiveButton(R.string.auto_skill_item_delete_confirm_ok) { _, _ -> deleteItem(args.key) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }
            R.id.action_auto_skill_export -> {
                autoSkillExport.launch("auto_skill_${autoSkillPrefs.name}.json")
                true
            }
            R.id.action_auto_skill_copy -> {
                val guid = UUID.randomUUID().toString()
                preferences.addAutoSkillConfig(guid)
                val newConfig = preferences.forAutoSkillConfig(guid)

                val map = autoSkillPrefs.export()
                newConfig.import(map)
                newConfig.name = getString(R.string.auto_skill_item_copy_name, newConfig.name)

                val action = AutoSkillItemSettingsFragmentDirections
                    .actionAutoSkillItemSettingsFragmentSelf(guid)

                findNavController().navigate(action)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteItem(AutoSkillItemKey: String) {
        preferences.removeAutoSkillConfig(AutoSkillItemKey)

        findNavController().popBackStack()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        fun prepare(dialogFragment: PreferenceDialogFragmentCompat) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, null)
        }

        when (preference.key) {
            getString(R.string.pref_support_pref_ce),
            getString(R.string.pref_support_pref_servant),
            getString(R.string.pref_support_friend_names) -> {
                ClearMultiSelectListPreferenceDialog().apply {
                    setKey(preference.key)
                    prepare(this)
                }
            }
            getString(R.string.pref_autoskill_cmd) -> {
                if (prefsCore.showTextBoxForAutoSkillCmd.get()) {
                    SkillCmdPreferenceDialogFragment().apply {
                        autoSkillKey = args.key
                        prepare(this)
                    }
                }
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }
}