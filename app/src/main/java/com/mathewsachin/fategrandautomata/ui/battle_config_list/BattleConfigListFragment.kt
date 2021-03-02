package com.mathewsachin.fategrandautomata.ui.battle_config_list

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.prefs.FgaTheme
import com.mathewsachin.fategrandautomata.ui.prefs.collect
import com.mathewsachin.fategrandautomata.ui.prefs.editTextDialog
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BattleConfigListFragment : Fragment() {
    @Inject
    lateinit var preferences: IPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    val vm: BattleConfigListViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    Box {
                        // TODO: This hack feels bad
                        val configsStateList = mutableStateListOf<BattleConfigCore>()
                        val configs by vm.battleConfigItems
                            .onEach {
                                configsStateList.clear()
                                configsStateList.addAll(it)
                            }
                            .collectAsState(emptyList())

                        if (configs.isEmpty()) {
                            Text(
                                stringResource(R.string.battle_config_list_no_items),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            val selectedConfigs by vm.selectedConfigs.collectAsState(emptySet())

                            LazyColumn {
                                items(configsStateList) {
                                    val name by it.name.collect()

                                    ListItem(
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    if (vm.selectionMode) {
                                                        vm.selectedConfigs.value = if (it.id in selectedConfigs) {
                                                            selectedConfigs - it.id
                                                        } else selectedConfigs + it.id
                                                    } else {
                                                        editItem(vm.prefs.forBattleConfig(it.id))
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!vm.selectionMode) {
                                                        enterActionMode()
                                                        vm.selectedConfigs.value = setOf(it.id)
                                                    }
                                                }
                                            ),
                                        trailing = {
                                            val selected = vm.selectionMode && it.id in selectedConfigs

                                            if (selected) {
                                                Icon(
                                                    painterResource(R.drawable.ic_check),
                                                    contentDescription = "Select",
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .padding(7.dp)
                                                )
                                            }
                                        }
                                    ) {
                                        Text(
                                            name,
                                            style = MaterialTheme.typography.body2
                                        )
                                    }

                                    Divider()
                                }
                            }
                        }

                        if (!vm.selectionMode) {
                            val newDialog = editTextDialog(
                                title = stringResource(R.string.p_battle_config_name),
                                value = "",
                                valueChange = { addNewConfig(it) },
                                validate = { it.isNotBlank() }
                            )

                            FloatingActionButton(
                                onClick = { newDialog.show() },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_plus),
                                    contentDescription = "Create new config",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(7.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            vm.selectedConfigs.collect { set ->
                val count = set.size
                if (count == 0) {
                    actionMode?.finish()
                } else actionMode?.let {
                    it.title = requireActivity().title
                    it.subtitle = getString(R.string.battle_config_list_selected_count, count)
                }
            }
        }
    }

    private fun addNewConfig(name: String) =
        editItem(vm.newConfig().apply {
            this.name = name
        })

    private fun editItem(config: IBattleConfig) {
        val action = BattleConfigListFragmentDirections
            .actionBattleConfigListFragmentToBattleConfigItemSettingsFragment(config.id)

        nav(action)
    }

    private fun exportBattleConfigs(dirUri: Uri?) {
        if (dirUri != null) {
            lifecycleScope.launch {
                val result = vm.exportAsync(dirUri).await()

                if (result.failureCount > 0) {
                    val msg = getString(R.string.battle_config_list_export_failed, result.failureCount)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }

                actionMode?.finish()
            }
        }
    }

    val battleConfigsExport = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { dirUri ->
        exportBattleConfigs(dirUri)
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
                battleConfigsExport.launch(Uri.EMPTY)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    var actionMode: ActionMode? = null

    fun enterActionMode() {
        actionMode = requireActivity().startActionMode(actionModeCallback)
        vm.selectionMode = true
    }

    val actionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem) =
            when (item.itemId) {
                R.id.action_battle_config_delete -> {
                    val toDelete = vm.selectedConfigs.value

                    AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.battle_config_list_delete_confirm_message, toDelete.size))
                        .setTitle(R.string.battle_config_list_delete_confirm_title)
                        .setPositiveButton(R.string.battle_config_list_delete_confirm_ok) { _, _ ->
                            toDelete.forEach {
                                preferences.removeBattleConfig(it)
                            }

                            mode.finish()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                R.id.action_battle_config_export_selected -> {
                    battleConfigsExport.launch(Uri.EMPTY)
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
            vm.selectionMode = false
            actionMode = null
        }
    }
}
