package com.mathewsachin.fategrandautomata.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.prefs.AutoSkillItemSettingsFragment
import com.mathewsachin.fategrandautomata.util.appComponent
import kotlinx.android.synthetic.main.settings.*
import javax.inject.Inject

const val AUTO_SKILL_EXPORT = 2303

class AutoSkillItemActivity : AppCompatActivity() {

    var autoSkillItemKey = ""
        private set

    @Inject
    lateinit var prefs: IPreferences

    lateinit var autoSkillPrefs: IAutoSkillPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        appComponent.inject(this)

        setSupportActionBar(toolbar_settings)

        autoSkillItemKey = intent.getStringExtra(::autoSkillItemKey.name)
            ?: throw IllegalArgumentException("Missing AutoSkill item key in intent")

        autoSkillPrefs = prefs.forAutoSkillConfig(autoSkillItemKey)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTO_SKILL_EXPORT && resultCode == Activity.RESULT_OK) {
            val values = prefs.forAutoSkillConfig(autoSkillItemKey).export()
            val gson = Gson()
            val json = gson.toJson(values)

            data?.data?.let { uri ->
                contentResolver.openOutputStream(uri)?.use { outStream ->
                    outStream.writer().use { it.write(json) }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
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
            R.id.action_auto_skill_export -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, "auto_skill_${autoSkillPrefs.name}.json")
                }
                startActivityForResult(intent, AUTO_SKILL_EXPORT)
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