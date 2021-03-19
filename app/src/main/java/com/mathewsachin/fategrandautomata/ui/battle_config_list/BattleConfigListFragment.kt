package com.mathewsachin.fategrandautomata.ui.battle_config_list

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import com.mathewsachin.fategrandautomata.ui.DimmedIcon
import com.mathewsachin.fategrandautomata.ui.FgaTheme
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.ui.prefs.collect
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

    val vm: BattleConfigListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Back button exits selection mode
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            vm.selectionMode.value = false
        }

        lifecycleScope.launchWhenStarted {
            vm.selectionMode.collect {
                callback.isEnabled = it
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    val selectionMode by vm.selectionMode.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Heading(stringResource(R.string.p_battle_config)) {
                            item {
                                Button(
                                    onClick = {
                                        battleConfigsExport.launch(Uri.EMPTY)
                                    },
                                    modifier = Modifier
                                        .padding(end = 5.dp)
                                        .animateContentSize()
                                ) {
                                    Text(
                                        stringResource(
                                            if (selectionMode)
                                                R.string.battle_config_item_export
                                            else R.string.battle_config_list_export_all
                                        )
                                    )
                                }
                            }

                            item {
                                Crossfade(selectionMode) {
                                    if (it) {
                                        Button(
                                            onClick = {
                                                deleteSelectedConfigs()
                                            }
                                        ) {
                                            Text(
                                                stringResource(R.string.battle_config_list_delete)
                                            )
                                        }
                                    }
                                    else {
                                        Button(
                                            onClick = {
                                                battleConfigImport.launch("*/*")
                                            }
                                        ) {
                                            Text(
                                                stringResource(R.string.battle_config_list_import)
                                            )
                                        }
                                    }
                                }
                            }
                        }

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
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        else {
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                BattleConfigList(
                                    vm = vm,
                                    configs = configsStateList,
                                    editItem = { editItem(it) }
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        AnimatedVisibility(
                            !selectionMode,
                            enter = slideInVertically(initialOffsetY = { it / 2 }),
                            exit = slideOutVertically(
                                targetOffsetY = { it * 2 }
                            )
                        ) {
                            FloatingActionButton(
                                onClick = { addNewConfig() }
                            ) {
                                Icon(
                                    Icons.Default.Add,
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
                    vm.selectionMode.value = false
                }
            }
        }
    }

    private fun addNewConfig() =
        editItem(vm.newConfig())

    private fun editItem(config: IBattleConfig) {
        val action = BattleConfigListFragmentDirections
            .actionBattleConfigListFragmentToBattleConfigItemSettingsFragment(config.id)

        nav(action)
    }

    private fun exportBattleConfigs(dirUri: Uri?) {
        if (dirUri != null) {
            lifecycleScope.launch {
                val result = vm.exportAsync(dirUri, requireContext()).await()

                if (result.failureCount > 0) {
                    val msg = getString(R.string.battle_config_list_export_failed, result.failureCount)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val battleConfigsExport = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { dirUri ->
        exportBattleConfigs(dirUri)
    }

    val battleConfigImport = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        lifecycleScope.launch {
            val result = vm.importAsync(uris, requireContext()).await()

            if (result.failureCount > 0) {
                val msg = getString(R.string.battle_config_list_import_failed, result.failureCount)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun deleteSelectedConfigs() {
        val toDelete = vm.selectedConfigs.value

        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.battle_config_list_delete_confirm_message, toDelete.size))
            .setTitle(R.string.battle_config_list_delete_confirm_title)
            .setPositiveButton(R.string.battle_config_list_delete_confirm_ok) { _, _ ->
                toDelete.forEach {
                    preferences.removeBattleConfig(it)
                }

                vm.selectionMode.value = false
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}

@Composable
fun BattleConfigList(
    vm: BattleConfigListViewModel,
    configs: SnapshotStateList<BattleConfigCore>,
    editItem: (IBattleConfig) -> Unit
) {
    val selectedConfigs by vm.selectedConfigs.collectAsState()
    val selectionMode by vm.selectionMode.collectAsState()

    LazyColumn {
        itemsIndexed(configs) { index, it ->
            val name by it.name.collect()

            if (index != 0) {
                Divider()
            }

            ListItem(
                modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            if (selectionMode) {
                                vm.selectedConfigs.value = if (it.id in selectedConfigs) {
                                    selectedConfigs - it.id
                                } else selectedConfigs + it.id
                            } else {
                                editItem(vm.prefs.forBattleConfig(it.id))
                            }
                        },
                        onLongClick = {
                            if (!selectionMode) {
                                vm.selectionMode.value = true
                                vm.selectedConfigs.value = setOf(it.id)
                            }
                        }
                    ),
                trailing = {
                    val selected = selectionMode && it.id in selectedConfigs

                    if (selected) {
                        DimmedIcon(
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
        }
    }
}