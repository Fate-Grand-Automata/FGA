package com.mathewsachin.fategrandautomata.ui.battle_config_list

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.databinding.BattleConfigListBinding
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import mva3.adapter.ListSection
import mva3.adapter.MultiViewAdapter
import mva3.adapter.util.Mode
import javax.inject.Inject

@AndroidEntryPoint
class BattleConfigListFragment : Fragment() {
    @Inject
    lateinit var preferences: IPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    val vm: BattleConfigListViewModel by activityViewModels()

    lateinit var binding: BattleConfigListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        BattleConfigListBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.battleConfigAddBtn.setOnClickListener {
            addOnBtnClick()
        }

        initView()

        vm.battleConfigItems.observe(viewLifecycleOwner) { items ->
            listSection.set(items)

            binding.battleConfigNoItems.visibility =
                if (items.isEmpty()) View.VISIBLE
                else View.GONE
        }
    }

    lateinit var adapter: MultiViewAdapter
    lateinit var listSection: ListSection<IBattleConfig>

    private fun initView() {
        adapter = MultiViewAdapter()

        adapter.registerItemBinders(BattleConfigListBinder({
            editItem(it)
        }) { enterActionMode() })

        listSection = ListSection()
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

        binding.battleConfigListView.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(requireContext())
            it.addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            )
        }
    }

    private fun addOnBtnClick() =
        editItem(vm.newConfig())

    private fun editItem(config: IBattleConfig) {
        val action = BattleConfigListFragmentDirections
            .actionBattleConfigListFragmentToBattleConfigItemSettingsFragment(config.id)

        nav(action)
    }

    private fun exportBattleConfigs(dirUri: Uri?, configs: () -> List<IBattleConfig>) {
        if (dirUri != null) {
            lifecycleScope.launch {
                val result = vm.exportAsync(dirUri, configs()).await()

                if (result.failureCount > 0) {
                    val msg = getString(R.string.battle_config_list_export_failed, result.failureCount)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val battleConfigsExportAll = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { dirUri ->
        exportBattleConfigs(dirUri) {
            vm.battleConfigItems.value ?: emptyList()
        }
    }

    val battleConfigsExportSelected = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { dirUri ->
        exportBattleConfigs(dirUri) {
            listSection.selectedItems ?: emptyList()
        }
    }

    val battleConfigImport = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        lifecycleScope.launch {
            val result = vm.importAsync(uris).await()

            if (result.failureCount > 0) {
                val msg = getString(R.string.battle_config_list_import_failed, result.failureCount)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT)
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
                R.id.action_battle_config_export_selected -> {
                    battleConfigsExportSelected.launch(Uri.EMPTY)
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
