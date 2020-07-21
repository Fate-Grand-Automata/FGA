package com.mathewsachin.fategrandautomata.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.prefs.AutoSkillItemSettingsFragment
import com.mathewsachin.fategrandautomata.util.appComponent
import kotlinx.android.synthetic.main.settings.*
import javax.inject.Inject

class AutoSkillItemActivity : AppCompatActivity() {

    var autoSkillItemKey = ""
        private set

    @Inject
    lateinit var prefs: IPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        appComponent.inject(this)

        setSupportActionBar(toolbar_settings)

        autoSkillItemKey = intent.getStringExtra(::autoSkillItemKey.name)
            ?: throw IllegalArgumentException("Missing AutoSkill item key in intent")

        // Add the fragment only on first launch
        if (savedInstanceState == null) {
            val fragment = AutoSkillItemSettingsFragment()
            val args = Bundle()
            args.putString(::autoSkillItemKey.name, autoSkillItemKey)

            fragment.arguments = args

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, fragment)
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.autoskill_item_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_auto_skill_delete -> {
                AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to delete this configuration?")
                    .setTitle("Confirm Deletion")
                    .setPositiveButton("Delete") { _, _ -> deleteItem(autoSkillItemKey) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteItem(AutoSkillItemKey: String) {
        prefs.removeAutoSkillConfig(AutoSkillItemKey)

        // We opened a separate activity for AutoSkill item
        finish()
    }
}
