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
import com.mathewsachin.fategrandautomata.ui.AutoSkillCommandKey
import com.mathewsachin.fategrandautomata.ui.AutoSkillItemActivity
import com.mathewsachin.fategrandautomata.ui.AutoSkillMakerActivity
import com.mathewsachin.fategrandautomata.ui.RequestAutoSkillMaker
import com.mathewsachin.fategrandautomata.util.preferredSupportOnCreate

class AutoSkillItemSettingsFragment : SupportSettingsBaseFragment() {
    private var autoSkillItemKey = ""

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

        val editText = EditText(requireActivity()).apply {
            setText(getSavedSkillCmd())

            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            val margin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
            lp.setMargins(margin, 0, margin, 0)
            layoutParams = lp
        }

        layout.addView(editText)

        AlertDialog.Builder(requireActivity())
            .setTitle("Skill Command")
            .setView(layout)
            .setPositiveButton(android.R.string.yes) { _, _ -> setAutoSkillCommand(editText.text.toString()) }
            .setNegativeButton(android.R.string.no, null)
            .setNeutralButton("Maker") { _, _ -> openAutoSkillMaker() }
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
        startActivityForResult(intent, RequestAutoSkillMaker)
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
