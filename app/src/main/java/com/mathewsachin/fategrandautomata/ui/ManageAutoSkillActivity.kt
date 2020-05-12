package com.mathewsachin.fategrandautomata.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.content.edit
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.defaultPrefs
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringPref
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringSetPref
import com.mathewsachin.fategrandautomata.ui.prefs.AutoSkillItemKey
import com.mathewsachin.fategrandautomata.util.AutoSkillEntry
import kotlinx.android.synthetic.main.autoskill_list.*
import java.util.*

class ManageAutoSkillActivity : AppCompatActivity() {
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
        autoSkillItems = getStringSetPref(R.string.pref_autoskill_list)
            .map {
                val sharedPrefs = getSharedPreferences(it, MODE_PRIVATE)

                AutoSkillEntry(
                    it,
                    getStringPref(R.string.pref_autoskill_name, "--", Prefs = sharedPrefs)
                )
            }
            .sortedBy{ it.Name }
            .toTypedArray()

        val autoSkillNames = autoSkillItems
            .map { it.Name }

        val adapter = ArrayAdapter(this, R.layout.autoskill_item, autoSkillNames)
        autoskill_listview.adapter = adapter
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
        intent.putExtra(AutoSkillItemKey, Id)

        startActivity(intent)
    }
}
