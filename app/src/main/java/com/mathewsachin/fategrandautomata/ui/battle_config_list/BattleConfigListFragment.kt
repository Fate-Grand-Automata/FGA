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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import com.mathewsachin.fategrandautomata.ui.BackHandler
import com.mathewsachin.fategrandautomata.ui.FgaScaffold
import com.mathewsachin.fategrandautomata.ui.HeadingButton
import com.mathewsachin.fategrandautomata.ui.battle_config_item.Material
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint
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

                val configs by vm.battleConfigItems.collectAsState(emptyList())

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
                                        icon = icon(Icons.Default.Delete)
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
                            items(
                                configs,
                                key = { it.id }
                            ) {
                                BattleConfigListItem(
                                    it,
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
                                    isSelectionMode = selectionMode,
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
fun BattleConfigItemSelected(
    isSelectionMode: Boolean,
    isSelected: Boolean
) {
    AnimatedVisibility(isSelectionMode) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(end = 16.dp)
                .border(
                    1.dp,
                    if (isSelected) Color.Transparent else MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
                    CircleShape
                )
                .background(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colors.secondary else Color.Transparent
                )
                .size(15.dp)
        ) {
            AnimatedVisibility(isSelected) {
                Icon(
                    rememberVectorPainter(Icons.Default.Check),
                    contentDescription = "Select",
                    tint = MaterialTheme.colors.onSecondary,
                    modifier = Modifier
                        .size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun BattleConfigListItem(
    it: BattleConfigCore,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val name by it.name.remember()
    val materialsSet by it.materials.remember()
    val mats = materialsSet.take(3)

    val shape = RoundedCornerShape(50)

    // Without this, holding a list item would leave it highlighted because of recomposition happening before ripple ending
    val longClickState = rememberUpdatedState(onLongClick)

    Card(
        shape = shape,
        elevation = if (isSelected) 50.dp else 1.dp,
        modifier = Modifier
            .padding(5.dp)
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { longClickState.value.invoke() }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp, 5.dp)
        ) {
            BattleConfigItemSelected(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected
            )

            Text(
                name,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(1f)
            )

            mats.forEach {
                Material(it)
            }
        }
    }
}