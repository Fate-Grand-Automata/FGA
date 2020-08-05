package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.SupportImageExtractor
import com.mathewsachin.fategrandautomata.util.appComponent
import com.mathewsachin.fategrandautomata.util.preferredSupportOnCreate
import com.mathewsachin.fategrandautomata.util.preferredSupportOnResume
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

class AutoSkillItemSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferences: IPreferences

    @Inject
    lateinit var storageDirs: StorageDirs

    val args: AutoSkillItemSettingsFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    val autoSkillExport = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) {
            val values = preferences.forAutoSkillConfig(args.key).export()
            val gson = Gson()
            val json = gson.toJson(values)

            requireContext().contentResolver.openOutputStream(uri)?.use { outStream ->
                outStream.writer().use { it.write(json) }
            }
        }
    }

    private lateinit var autoSkillPrefs: IAutoSkillPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = args.key
        autoSkillPrefs = preferences.forAutoSkillConfig(args.key)

        setPreferencesFromResource(R.xml.autoskill_item_preferences, rootKey)

        preferredSupportOnCreate()

        findPreference<Preference>(getString(prefKeys.pref_card_priority))?.let {
            it.setOnPreferenceClickListener {
                val action = AutoSkillItemSettingsFragmentDirections
                    .actionAutoSkillItemSettingsFragmentToCardPriorityFragment(args.key)

                findNavController().navigate(action)

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

    override fun onResume() {
        super.onResume()

        if (storageDirs.shouldExtractSupportImages) {
            performSupportImageExtraction()
        } else preferredSupportOnResume(storageDirs)

        // Update Card Priority
        findPreference<Preference>(getString(prefKeys.pref_card_priority))?.let {
            lifecycleScope.launch {
                // If not using a delay, this code runs before CardPriorityFragment's onPause
                delay(300)
                it.summary = autoSkillPrefs.cardPriority
            }
        }

        findPreference<EditTextPreference>(getString(R.string.pref_autoskill_cmd))?.let {
            it.text = autoSkillPrefs.skillCommand
        }

        findPreference<Preference>(getString(R.string.pref_nav_skill_lvl))?.let {
            it.summary = listOf(
                autoSkillPrefs.skill1Max,
                autoSkillPrefs.skill2Max,
                autoSkillPrefs.skill3Max
            ).joinToString("/") { m -> if (m) "10" else "x" }
        }
    }

    private fun performSupportImageExtraction() {
        lifecycleScope.launch {
            SupportImageExtractor(requireContext(), storageDirs).extract()
            Toast.makeText(activity, "Support Images Extracted Successfully", Toast.LENGTH_SHORT)
                .show()
            preferredSupportOnResume(storageDirs)
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
                    .setMessage("Are you sure you want to delete this configuration?")
                    .setTitle("Confirm Deletion")
                    .setPositiveButton("Delete") { _, _ -> deleteItem(args.key) }
                    .setNegativeButton("Cancel", null)
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
                newConfig.name += " (Copy)"

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
                SkillCmdPreferenceDialogFragment().apply {
                    autoSkillKey = args.key
                    prepare(this)
                }
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }
}