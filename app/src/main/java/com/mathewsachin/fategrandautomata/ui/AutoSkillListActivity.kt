package com.mathewsachin.fategrandautomata.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.appComponent
import kotlinx.android.synthetic.main.autoskill_list.*
import java.util.*
import javax.inject.Inject

const val AUTO_SKILL_IMPORT = 2047

class AutoSkillListActivity : AppCompatActivity() {
    private lateinit var autoSkillItems: Array<IAutoSkillPreferences>

    @Inject
    lateinit var preferences: IPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.autoskill_list)

        appComponent.inject(this)

        autoskill_add_btn.setOnClickListener {
            addOnBtnClick()
        }

        autoskill_listview.setOnItemClickListener { _, _, position, _ ->
            val guid = autoSkillItems[position].id

            editItem(guid)
        }

        initView()
    }

    override fun onRestart() {
        super.onRestart()

        initView()
    }

    private fun initView() {
        autoSkillItems = preferences.autoSkillPreferences
            .toTypedArray()

        val autoSkillNames = autoSkillItems
            .map { it.name }

        val adapter = ArrayAdapter(this, R.layout.autoskill_item, autoSkillNames)
        autoskill_listview.adapter = adapter
        autoskill_listview.emptyView = auto_skill_no_items
    }

    private fun newConfig(): String {
        val guid = UUID.randomUUID().toString()

        val autoSkillItems = preferences.autoSkillList
            .toMutableSet()
            .apply { add(guid) }

        preferences.autoSkillList = autoSkillItems

        return guid
    }

    private fun addOnBtnClick() {
        val id = newConfig()

        editItem(id)
    }

    private fun editItem(Id: String) {
        val intent = Intent(this, AutoSkillItemActivity::class.java)
        intent.putExtra(AutoSkillItemActivity::autoSkillItemKey.name, Id)

        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTO_SKILL_IMPORT && resultCode == Activity.RESULT_OK) {
            val json = data?.data?.let { uri ->
                contentResolver.openInputStream(uri)?.use { inStream ->
                    inStream.use {
                        it.reader().readText()
                    }
                }
            }

            val id = newConfig()
            val gson = Gson()
            val map = gson.fromJson(json, Map::class.java)
                .map { (k, v) -> k.toString() to v }
                .toMap()
            preferences.forAutoSkillConfig(id).import(map)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.autoskill_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_auto_skill_import -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(intent, AUTO_SKILL_IMPORT)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
