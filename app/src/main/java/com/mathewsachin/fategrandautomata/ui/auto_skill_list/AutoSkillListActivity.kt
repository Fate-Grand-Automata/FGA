package com.mathewsachin.fategrandautomata.ui.auto_skill_list

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.AutoSkillItemActivity
import com.mathewsachin.fategrandautomata.util.appComponent
import kotlinx.android.synthetic.main.autoskill_list.*
import mu.KotlinLogging
import mva3.adapter.ListSection
import mva3.adapter.MultiViewAdapter
import mva3.adapter.util.Mode
import java.util.*
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

class AutoSkillListActivity : AppCompatActivity() {
    @Inject
    lateinit var preferences: IPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.autoskill_list)

        appComponent.inject(this)

        autoskill_add_btn.setOnClickListener {
            addOnBtnClick()
        }

        initView()
    }

    override fun onRestart() {
        super.onRestart()

        refresh()
    }

    lateinit var adapter: MultiViewAdapter
    lateinit var listSection: ListSection<IAutoSkillPreferences>

    private fun initView() {
        adapter = MultiViewAdapter()

        adapter.registerItemBinders(AutoSkillListBinder({
            editItem(it.id)
        }) { enterActionMode() })

        listSection = ListSection<IAutoSkillPreferences>()
        listSection.setOnSelectionChangedListener { _, _, selectedItems ->
            val count = selectedItems.size
            if (count == 0) {
                actionMode?.finish()
            } else actionMode?.let {
                it.title = title
                it.subtitle = "$count selected"
            }
        }

        adapter.addSection(listSection)

        auto_skill_list_view.adapter = adapter
        auto_skill_list_view.layoutManager = LinearLayoutManager(this)
        auto_skill_list_view.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        refresh()
    }

    private fun refresh() {
        val autoSkillItems = preferences.autoSkillPreferences
        listSection.set(autoSkillItems)

        auto_skill_no_items.visibility =
            if (autoSkillItems.isEmpty()) View.VISIBLE
            else View.GONE
    }

    private fun newConfig(): String {
        val guid = UUID.randomUUID().toString()

        preferences.addAutoSkillConfig(guid)

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

    val autoSkillImport = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        var failed = 0

        uris.forEach { uri ->
            val json = contentResolver.openInputStream(uri)?.use { inStream ->
                inStream.use {
                    it.reader().readText()
                }
            }

            if (json != null) {
                try {
                    val gson = Gson()
                    val map = gson.fromJson(json, Map::class.java)
                        .map { (k, v) -> k.toString() to v }
                        .toMap()

                    val id = newConfig()
                    preferences.forAutoSkillConfig(id).import(map)
                } catch (e: Exception) {
                    ++failed
                    logger.error("Import Failed", e)
                }
            }
        }

        if (failed > 0) {
            Toast.makeText(this, "Import Failed for $failed item(s)", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.autoskill_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_auto_skill_import -> {
                autoSkillImport.launch("*/*")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    var actionMode: ActionMode? = null

    fun enterActionMode() {
        adapter.startActionMode()
        adapter.setSelectionMode(Mode.MULTIPLE)
        actionMode = startActionMode(actionModeCallback)
    }

    val actionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem) =
            when (item.itemId) {
                R.id.action_auto_skill_delete -> {
                    val toDelete = listSection.selectedItems

                    AlertDialog.Builder(this@AutoSkillListActivity)
                        .setMessage("Are you sure you want to delete ${toDelete.size} configuration(s)?")
                        .setTitle("Confirm Deletion")
                        .setPositiveButton("Delete") { _, _ ->
                            toDelete.forEach {
                                preferences.removeAutoSkillConfig(it.id)
                            }

                            mode.finish()
                            refresh()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
                else -> false
            }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.autoskill_list_multi_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        override fun onDestroyActionMode(mode: ActionMode) {
            listSection.clearSelections()
            adapter.stopActionMode()
            adapter.setSelectionMode(Mode.NONE)
            actionMode = null
        }
    }
}
