package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mathewsachin.fategrandautomata.prefs.R.string as prefKeys

class AutoSkillItemSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var preferences: IPreferences

    @Inject
    lateinit var storageDirs: StorageDirs

    val args: AutoSkillItemSettingsFragmentArgs by navArgs()

    val startAutoSkillMaker = registerForActivityResult(StartAutoSkillMaker()) { cmd ->
        if (cmd != null) {
            setAutoSkillCommand(cmd)
        }
    }

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

    private val scope = MainScope()

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private lateinit var autoSkillPrefs: IAutoSkillPreferences
    private var editTextVisibleKey = ""
    private var restoredEditTextContent: String? = null

    private var editText: EditText? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (editText != null) {
            outState.putString(::editText.name, editText?.text.toString())
            outState.putBoolean(::editTextVisibleKey.name, true)
        }
    }

    // We don't want the user to lose what they typed on a screen rotation
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(::editTextVisibleKey.name, false)) {
                restoredEditTextContent = savedInstanceState.getString(::editText.name, "")

                onSkillCmdClick()
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = args.key
        autoSkillPrefs = preferences.forAutoSkillConfig(args.key)

        setPreferencesFromResource(R.xml.autoskill_item_preferences, rootKey)

        preferredSupportOnCreate()

        findPreference<Preference>(getString(prefKeys.pref_autoskill_cmd))?.let {
            it.setOnPreferenceClickListener {
                onSkillCmdClick()
                true
            }
        }

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

    private fun onSkillCmdClick() {
        val layout = FrameLayout(requireActivity())

        editText = EditText(requireActivity()).apply {
            setText(restoredEditTextContent ?: autoSkillPrefs.skillCommand)
            setSelection(text.length)

            val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            val margin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
            lp.setMargins(margin, 0, margin, 0)
            layoutParams = lp
        }

        layout.addView(editText)

        AlertDialog.Builder(requireActivity())
            .setTitle("Skill Command")
            .setView(layout)
            .setPositiveButton(android.R.string.yes) { _, _ -> setAutoSkillCommand(editText?.text.toString()) }
            .setNegativeButton(android.R.string.no, null)
            .setNeutralButton("Maker") { _, _ ->
                startAutoSkillMaker.launch()
            }
            .setOnDismissListener {
                // Clear the persisted information
                editText = null
                restoredEditTextContent = null
            }
            .show()
    }

    private fun setAutoSkillCommand(Cmd: String) {
        autoSkillPrefs.skillCommand = Cmd

        updateSkillCmdSummary()
    }

    private fun updateSkillCmdSummary() {
        findPreference<Preference>(getString(prefKeys.pref_autoskill_cmd))?.let {
            it.summary = autoSkillPrefs.skillCommand
        }
    }

    override fun onResume() {
        super.onResume()

        updateSkillCmdSummary()

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

        findPreference<Preference>(getString(R.string.pref_nav_skill_lvl))?.let {
            it.summary = listOf(
                autoSkillPrefs.skill1Max,
                autoSkillPrefs.skill2Max,
                autoSkillPrefs.skill3Max
            ).joinToString("/") { m -> if (m) "10" else "x" }
        }
    }

    private fun performSupportImageExtraction() {
        scope.launch {
            SupportImageExtractor(requireContext(), storageDirs).extract()
            Toast.makeText(activity, "Support Images Extracted Successfully", Toast.LENGTH_SHORT)
                .show()
            preferredSupportOnResume(storageDirs)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.autoskill_item_menu, menu)
        inflater.inflate(R.menu.support_menu, menu)
        inflater.inflate(R.menu.support_common_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_support_extract_defaults -> {
                performSupportImageExtraction()
                true
            }
            R.id.action_clear_support_servants -> {
                findServantList()?.values = emptySet()
                true
            }
            R.id.action_clear_support_ces -> {
                findCeList()?.values = emptySet()
                true
            }
            R.id.action_clear_support_friends -> {
                findFriendNamesList()?.values = emptySet()
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteItem(AutoSkillItemKey: String) {
        preferences.removeAutoSkillConfig(AutoSkillItemKey)

        findNavController().popBackStack()
    }
}
