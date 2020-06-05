package com.mathewsachin.fategrandautomata.ui.prefs

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.Preference
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringPref
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.AutoSkillCommandKey
import com.mathewsachin.fategrandautomata.ui.AutoSkillItemActivity
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.AutoSkillMakerActivity
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.RequestAutoSkillMaker
import com.mathewsachin.fategrandautomata.util.preferredSupportOnCreate

class AutoSkillItemSettingsFragment : SupportSettingsBaseFragment() {
    private var autoSkillItemKey = ""
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
        autoSkillItemKey = arguments?.getString(AutoSkillItemActivity::autoSkillItemKey.name)
            ?: throw IllegalArgumentException("Arguments should not be null")

        preferenceManager.sharedPreferencesName = autoSkillItemKey

        setPreferencesFromResource(R.xml.autoskill_item_preferences, rootKey)

        preferredSupportOnCreate()

        findPreference<Preference>(getString(R.string.pref_autoskill_cmd))?.let {
            it.setOnPreferenceClickListener {
                onSkillCmdClick()
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
            setText(restoredEditTextContent ?: getSavedSkillCmd())
            setSelection(text.length)

            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
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
        val prefs = requireContext().getSharedPreferences(autoSkillItemKey, Context.MODE_PRIVATE)
        prefs.edit(commit = true) {
            putString(getString(R.string.pref_autoskill_cmd), Cmd)
        }

        updateSkillCmdSummary()
    }

    private fun openAutoSkillMaker() {
        val intent = Intent(requireActivity(), AutoSkillMakerActivity::class.java)
        startActivityForResult(intent,
            RequestAutoSkillMaker
        )
    }

    private fun getSavedSkillCmd(): String {
        val prefs = requireContext().getSharedPreferences(autoSkillItemKey, Context.MODE_PRIVATE)
        return getStringPref(R.string.pref_autoskill_cmd, Prefs = prefs)
    }

    private fun updateSkillCmdSummary() {
        findPreference<Preference>(getString(R.string.pref_autoskill_cmd))?.let {
            it.summary = getSavedSkillCmd()
        }
    }

    override fun onResume() {
        super.onResume()

        updateSkillCmdSummary()
    }
}
