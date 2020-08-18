package com.mathewsachin.fategrandautomata.ui.auto_skill_list

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.appComponent
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

class AutoSkillListFragment : Fragment() {
    @Inject
    lateinit var preferences: IPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.autoskill_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoskill_add_btn.setOnClickListener {
            addOnBtnClick()
        }

        initView()

        val vm: AutoSkillListViewModel by activityViewModels { viewModelFactory }

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
                it.subtitle = "$count selected"
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

        findNavController().navigate(action)
    }

    val autoSkillImport = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        var failed = 0

        Toast.makeText(context, "Importing ...", Toast.LENGTH_SHORT).show()

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
                Toast.makeText(requireContext(), "Import Failed for $failed item(s)", Toast.LENGTH_SHORT)
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
                        .setMessage("Are you sure you want to delete ${toDelete.size} configuration(s)?")
                        .setTitle("Confirm Deletion")
                        .setPositiveButton("Delete") { _, _ ->
                            toDelete.forEach {
                                preferences.removeAutoSkillConfig(it.id)
                            }

                            mode.finish()
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
