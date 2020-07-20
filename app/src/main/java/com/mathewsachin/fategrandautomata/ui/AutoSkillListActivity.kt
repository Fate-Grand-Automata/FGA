package com.mathewsachin.fategrandautomata.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.appComponent
import kotlinx.android.synthetic.main.autoskill_list.*
import java.util.*
import javax.inject.Inject

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

    private fun addOnBtnClick() {
        val guid = UUID.randomUUID().toString()

        val autoSkillItems = preferences.autoSkillList
            .toMutableSet()
            .apply { add(guid) }

        preferences.autoSkillList = autoSkillItems

        editItem(guid)
    }

    private fun editItem(Id: String) {
        val intent = Intent(this, AutoSkillItemActivity::class.java)
        intent.putExtra(AutoSkillItemActivity::autoSkillItemKey.name, Id)

        startActivity(intent)
    }
}
