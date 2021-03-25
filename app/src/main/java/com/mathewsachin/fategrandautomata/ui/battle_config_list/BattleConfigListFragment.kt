package com.mathewsachin.fategrandautomata.ui.battle_config_list

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.*
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.activity.compose.registerForActivityResult as activityResult

@AndroidEntryPoint
class BattleConfigListFragment : Fragment() {
    @Inject
    lateinit var preferences: IPreferences

    val vm: BattleConfigListViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                val selectionMode by vm.selectionMode.collectAsState()
                val selectedConfigs by vm.selectedConfigs.collectAsState()

                BackHandler(
                    backDispatcher = requireActivity().onBackPressedDispatcher,
                    enabled = selectionMode
                ) {
                    vm.endSelection()
                }

                val battleConfigsExport = activityResult(
                    ActivityResultContracts.OpenDocumentTree()
                ) { dirUri ->
                    exportBattleConfigs(dirUri)
                }

                val battleConfigImport = activityResult(
                    ActivityResultContracts.GetMultipleContents()
                ) { uris ->
                    importBattleConfigs(uris)
                }

                // TODO: This hack feels bad
                val configsStateList = mutableStateListOf<BattleConfigCore>()
                val configs by vm.battleConfigItems
                    .onEach {
                        configsStateList.clear()
                        configsStateList.addAll(it)
                    }
                    .collectAsState(emptyList())

                FgaScaffold(
                    stringResource(R.string.p_battle_config),
                    subheading = {
                        item {
                            HeadingButton(
                                text = stringResource(
                                    if (selectionMode)
                                        R.string.battle_config_item_export
                                    else R.string.battle_config_list_export_all
                                ),
                                onClick = {
                                    battleConfigsExport.launch(Uri.EMPTY)
                                }
                            )
                        }

                        item {
                            Crossfade(selectionMode) {
                                if (it) {
                                    HeadingButton(
                                        text = stringResource(R.string.battle_config_list_delete),
                                        onClick = {
                                            deleteSelectedConfigs()
                                        },
                                        color = MaterialTheme.colors.error,
                                        icon = icon(R.drawable.ic_delete)
                                    )
                                }
                                else {
                                    HeadingButton(
                                        text = stringResource(R.string.battle_config_list_import),
                                        onClick = {
                                            battleConfigImport.launch("*/*")
                                        }
                                    )
                                }
                            }
                        }
                    },
                    content = {
                        if (configs.isEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.battle_config_list_no_items),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        else {
                            itemsIndexed(
                                configsStateList,
                                key = { _, it -> it.id }
                            ) { index, it ->
                                BattleConfigListItem(
                                    it,
                                    index,
                                    onClick = {
                                        if (selectionMode) {
                                            vm.toggleSelected(it.id)
                                        } else {
                                            editItem(vm.prefs.forBattleConfig(it.id))
                                        }
                                    },
                                    onLongClick = {
                                        if (!selectionMode) {
                                            vm.startSelection(it.id)
                                        }
                                    },
                                    isSelected = selectionMode && it.id in selectedConfigs
                                )
                            }
                        }
                    },
                    fab = {
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
                )
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

    private fun importBattleConfigs(uris: List<Uri>) {
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

                vm.endSelection()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}

@Composable
private fun BattleConfigListItem(
    it: BattleConfigCore,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val name by it.name.remember()

    if (index != 0) {
        Divider()
    }

    ListItem(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        trailing = {
            AnimatedVisibility (isSelected) {
                DimmedIcon(
                    icon(Icons.Default.Check),
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