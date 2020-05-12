package com.mathewsachin.fategrandautomata.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.prefs.AutoSkillItemKey
import com.mathewsachin.fategrandautomata.ui.prefs.AutoSkillSettingsFragment

class AutoSkillItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        // Add the fragment only on first launch
        if (savedInstanceState == null) {
            val fragment = AutoSkillSettingsFragment()
            val args = Bundle()
            args.putString(AutoSkillItemKey, intent.getStringExtra(AutoSkillItemKey))

            fragment.arguments = args

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, fragment)
                .commit()
        }
    }
}
