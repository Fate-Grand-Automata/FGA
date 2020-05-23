package com.mathewsachin.fategrandautomata.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.defaultPrefs
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringSetPref
import com.mathewsachin.fategrandautomata.util.AutoSkillEntry
import com.mathewsachin.fategrandautomata.util.getAutoSkillEntries
import kotlinx.android.synthetic.main.autoskill_list.*
import java.util.*

class AutoSkillListActivity : AppCompatActivity() {
    private lateinit var autoSkillItems: Array<AutoSkillEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.autoskill_list)

        autoskill_add_btn.setOnClickListener {
            addOnBtnClick()
        }

        autoskill_listview.setOnItemClickListener { _, _, position, _ ->
            val guid = autoSkillItems[position].Id

            editItem(guid)
        }

        initView()
    }

    override fun onRestart() {
        super.onRestart()

        initView()
    }

    private fun initView() {
        autoSkillItems = getAutoSkillEntries()
            .toTypedArray()

        val autoSkillNames = autoSkillItems
            .map { it.Name }

        val adapter = ArrayAdapter(this, R.layout.autoskill_item, autoSkillNames)
        autoskill_listview.adapter = adapter
        autoskill_listview.emptyView = auto_skill_no_items
    }

    private fun addOnBtnClick() {
        val guid = UUID.randomUUID().toString()

        val autoSkillItems = getStringSetPref(R.string.pref_autoskill_list)
            .toMutableSet()

        autoSkillItems.add(guid)

        val key = getString(R.string.pref_autoskill_list)

        defaultPrefs.edit(commit = true) {
            putStringSet(key, autoSkillItems)
        }

        editItem(guid)
    }

    private fun editItem(Id: String) {
        val intent = Intent(this, AutoSkillItemActivity::class.java)
        intent.putExtra(AutoSkillItemActivity::autoSkillItemKey.name, Id)

        startActivity(intent)
    }
}
