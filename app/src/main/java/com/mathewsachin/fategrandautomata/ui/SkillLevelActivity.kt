package com.mathewsachin.fategrandautomata.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.prefs.SkillLevelSettingsFragment
import kotlinx.android.synthetic.main.settings.*

class SkillLevelActivity : AppCompatActivity() {

    var autoSkillItemKey = ""
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        setSupportActionBar(toolbar_settings)

        autoSkillItemKey = intent.getStringExtra(AutoSkillItemActivity::autoSkillItemKey.name)
            ?: throw IllegalArgumentException("Missing AutoSkill item key in intent")

        // Add the fragment only on first launch
        if (savedInstanceState == null) {
            val fragment = SkillLevelSettingsFragment()
            val args = Bundle()
            args.putString(AutoSkillItemActivity::autoSkillItemKey.name, autoSkillItemKey)

            fragment.arguments = args

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, fragment)
                .commit()
        }
    }

}