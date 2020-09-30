package com.mathewsachin.fategrandautomata.ui.auto_skill_list

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
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.autoskill_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import mva3.adapter.ListSection
import mva3.adapter.MultiViewAdapter
import mva3.adapter.util.Mode
import java.util.*
import javax.inject.Inject

private val logger = KotlinLogging.logger {}

@AndroidEntryPoint
class AutoSkillListFragment : Fragment(R.layout.autoskill_list) {
    @Inject
    lateinit var preferences: IPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    val vm: AutoSkillListViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoskill_add_btn.setOnClickListener {
            addOnBtnClick()
        }

        initView()

        vm.autoSkillItems.observe(viewLifecycleOwner) { items ->
            listSection.set(items)

            auto_skill_no_items.visibility =
                if (items.isEmpty()) View.VISIBLE
                else View.GONE
        }
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
                it.title = requireActivity().title
                it.subtitle = getString(R.string.auto_skill_list_selected_count, count)
            }
        }

        adapter.addSection(listSection)

        auto_skill_list_view.adapter = adapter
        auto_skill_list_view.layoutManager = LinearLayoutManager(requireContext())
        auto_skill_list_view.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
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
        val action = AutoSkillListFragmentDirections
            .actionAutoSkillListFragmentToAutoSkillItemSettingsFragment(Id)

        nav(action)
    }

    val autoSkillExportAll = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { dirUri ->
        if (dirUri != null) {
            val gson = Gson()
            val resolver = requireContext().contentResolver
            val dir = DocumentFile.fromTreeUri(requireContext(), dirUri)

            var failed = 0

            vm.autoSkillItems.value?.forEach { autoSkillItem ->
                val values = autoSkillItem.export()
                val json = gson.toJson(values)

                try {
                    dir?.createFile("*/*", "auto_skill_${autoSkillItem.name}.json")
                        ?.uri
                        ?.let { uri ->
                            resolver.openOutputStream(uri)?.use { outStream ->
                                outStream.writer().use { it.write(json) }
                            }
                        }
                } catch (e: Exception) {
                    logger.error("Failed to export", e)
                    ++failed
                }
            }

            if (failed > 0) {
                val msg = getString(R.string.auto_skill_list_export_failed, failed)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val autoSkillImport = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
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
                            preferences.forAutoSkillConfig(id).import(map)
                        }
                    } catch (e: Exception) {
                        ++failed
                        logger.error("Import Failed", e)
                    }
                }
            }

            if (failed > 0) {
                val msg = getString(R.string.auto_skill_list_import_failed, failed)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.autoskill_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_auto_skill_import -> {
                autoSkillImport.launch("*/*")
                true
            }
            R.id.action_auto_skill_export_all -> {
                autoSkillExportAll.launch(Uri.EMPTY)
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
                R.id.action_auto_skill_delete -> {
                    val toDelete = listSection.selectedItems

                    AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.auto_skill_list_delete_confirm_message, toDelete.size))
                        .setTitle(R.string.auto_skill_list_delete_confirm_title)
                        .setPositiveButton(R.string.auto_skill_list_delete_confirm_ok) { _, _ ->
                            toDelete.forEach {
                                preferences.removeAutoSkillConfig(it.id)
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
