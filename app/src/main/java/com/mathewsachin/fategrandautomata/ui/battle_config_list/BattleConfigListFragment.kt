package com.mathewsachin.fategrandautomata.ui.battle_config_list

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.battle_config_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mva3.adapter.ListSection
import mva3.adapter.MultiViewAdapter
import mva3.adapter.util.Mode
import timber.log.Timber
import timber.log.error
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BattleConfigListFragment : Fragment(R.layout.battle_config_list) {
    @Inject
    lateinit var preferences: IPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    val vm: BattleConfigListViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        battle_config_add_btn.setOnClickListener {
            addOnBtnClick()
        }

        initView()

        vm.battleConfigItems.observe(viewLifecycleOwner) { items ->
            listSection.set(items)

            battle_config_no_items.visibility =
                if (items.isEmpty()) View.VISIBLE
                else View.GONE
        }
    }

    lateinit var adapter: MultiViewAdapter
    lateinit var listSection: ListSection<IBattleConfig>

    private fun initView() {
        adapter = MultiViewAdapter()

        adapter.registerItemBinders(BattleConfigListBinder({
            editItem(it.id)
        }) { enterActionMode() })

        listSection = ListSection<IBattleConfig>()
        listSection.setOnSelectionChangedListener { _, _, selectedItems ->
            val count = selectedItems.size
            if (count == 0) {
                actionMode?.finish()
            } else actionMode?.let {
                it.title = requireActivity().title
                it.subtitle = getString(R.string.battle_config_list_selected_count, count)
            }
        }

        adapter.addSection(listSection)

        battle_config_list_view.adapter = adapter
        battle_config_list_view.layoutManager = LinearLayoutManager(requireContext())
        battle_config_list_view.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
    }

    private fun newConfig(): String {
        val guid = UUID.randomUUID().toString()

        preferences.addBattleConfig(guid)

        return guid
    }

    private fun addOnBtnClick() {
        val id = newConfig()

        editItem(id)
    }

    private fun editItem(Id: String) {
        val action = BattleConfigListFragmentDirections
            .actionBattleConfigListFragmentToBattleConfigItemSettingsFragment(Id)

        nav(action)
    }

    val battleConfigsExportAll = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { dirUri ->
        if (dirUri != null) {
            val gson = Gson()
            val resolver = requireContext().contentResolver
            val dir = DocumentFile.fromTreeUri(requireContext(), dirUri)

            var failed = 0

            vm.battleConfigItems.value?.forEach { battleConfig ->
                val values = battleConfig.export()
                val json = gson.toJson(values)

                try {
                    dir?.createFile("*/*", "${battleConfig.name}.fga")
                        ?.uri
                        ?.let { uri ->
                            resolver.openOutputStream(uri)?.use { outStream ->
                                outStream.writer().use { it.write(json) }
                            }
                        }
                } catch (e: Exception) {
                    Timber.error(e) { "Failed to export" }
                    ++failed
                }
            }

            if (failed > 0) {
                val msg = getString(R.string.battle_config_list_export_failed, failed)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val battleConfigImport = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        var failed = 0

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                uris.forEach { uri ->
                    try {
                        val json = requireContext().contentResolver.openInputStream(uri)?.use { inStream ->
                            inStream.use {
                                it.reader().readText()
                            }
                        }

                        if (json != null) {
                            val gson = Gson()
                            val map = gson.fromJson(json, Map::class.java)
                                .map { (k, v) -> k.toString() to v }
                                .toMap()

                            val id = newConfig()
                            preferences.forBattleConfig(id).import(map)
                        }
                    } catch (e: Exception) {
                        ++failed
                        Timber.error(e) { "Import Failed" }
                    }
                }
            }

            if (failed > 0) {
                val msg = getString(R.string.battle_config_list_import_failed, failed)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.battle_config_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_battle_config_import -> {
                battleConfigImport.launch("*/*")
                true
            }
            R.id.action_battle_config_export_all -> {
                battleConfigsExportAll.launch(Uri.EMPTY)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    var actionMode: ActionMode? = null

    fun enterActionMode() {
        adapter.startActionMode()
        adapter.setSelectionMode(Mode.MULTIPLE)
        actionMode = requireActivity().startActionMode(actionModeCallback)
    }

    val actionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem) =
            when (item.itemId) {
                R.id.action_battle_config_delete -> {
                    val toDelete = listSection.selectedItems

                    AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.battle_config_list_delete_confirm_message, toDelete.size))
                        .setTitle(R.string.battle_config_list_delete_confirm_title)
                        .setPositiveButton(R.string.battle_config_list_delete_confirm_ok) { _, _ ->
                            toDelete.forEach {
                                preferences.removeBattleConfig(it.id)
                            }

                            mode.finish()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                else -> false
            }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.battle_config_list_multi_menu, menu)
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
