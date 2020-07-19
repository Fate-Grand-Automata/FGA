package com.mathewsachin.fategrandautomata.ui.prefs

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.extractSupportImgs
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import com.mathewsachin.fategrandautomata.util.shouldExtractSupportImages
import com.mathewsachin.fategrandautomata.ui.AutoSkillItemActivity
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.AutoSkillCommandKey
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.AutoSkillMakerActivity
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.RequestAutoSkillMaker
import com.mathewsachin.fategrandautomata.ui.card_priority.CardPriorityActivity
import com.mathewsachin.fategrandautomata.util.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AutoSkillItemSettingsFragment : PreferenceFragmentCompat() {
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
        val autoSkillItemKey = arguments?.getString(AutoSkillItemActivity::autoSkillItemKey.name)
            ?: throw IllegalArgumentException("Arguments should not be null")

        preferenceManager.sharedPreferencesName = autoSkillItemKey
        autoSkillPrefs = Preferences.forAutoSkillConfig(autoSkillItemKey)

        setPreferencesFromResource(R.xml.autoskill_item_preferences, rootKey)

        preferredSupportOnCreate()

        findPreference<Preference>(getString(R.string.pref_autoskill_cmd))?.let {
            it.setOnPreferenceClickListener {
                onSkillCmdClick()
                true
            }
        }

        findPreference<Preference>(getString(R.string.pref_card_priority))?.let {
            it.setOnPreferenceClickListener {
                val intent = Intent(activity, CardPriorityActivity::class.java)
                intent.putExtra(AutoSkillItemActivity::autoSkillItemKey.name, autoSkillItemKey)
                startActivity(intent)
                true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestAutoSkillMaker -> {
                if (resultCode == RESULT_OK) {
                    setAutoSkillCommand(data?.getStringExtra(AutoSkillCommandKey) ?: "")
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
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
            .setNeutralButton("Maker") { _, _ -> openAutoSkillMaker() }
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

    private fun openAutoSkillMaker() {
        val intent = Intent(requireActivity(), AutoSkillMakerActivity::class.java)
        startActivityForResult(
            intent,
            RequestAutoSkillMaker
        )
    }

    private fun updateSkillCmdSummary() {
        findPreference<Preference>(getString(R.string.pref_autoskill_cmd))?.let {
            it.summary = autoSkillPrefs.skillCommand
        }
    }

    override fun onResume() {
        super.onResume()

        updateSkillCmdSummary()

        if (shouldExtractSupportImages) {
            performSupportImageExtraction()
        } else preferredSupportOnResume()

        // Update Card Priority
        findPreference<Preference>(getString(R.string.pref_card_priority))?.let {
            it.summary = autoSkillPrefs.cardPriority
        }
    }

    private fun performSupportImageExtraction() {
        scope.launch {
            extractSupportImgs()
            Toast.makeText(activity, "Support Images Extracted Successfully", Toast.LENGTH_SHORT)
                .show()
            preferredSupportOnResume()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}
