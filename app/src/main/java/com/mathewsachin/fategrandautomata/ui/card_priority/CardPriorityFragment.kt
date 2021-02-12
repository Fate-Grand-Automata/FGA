package com.mathewsachin.fategrandautomata.ui.card_priority

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.ui.prefs.compose.FgaTheme
import com.mathewsachin.fategrandautomata.ui.prefs.compose.listDialog
import com.mathewsachin.fategrandautomata.util.ItemTouchHelperCallback
import com.mathewsachin.fategrandautomata.util.stringRes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardPriorityFragment : Fragment() {
    val vm: CardPriorityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    CardPriorityView(items = vm.cardPriorityItems)
                }
            }
        }

    override fun onPause() {
        super.onPause()

        vm.save()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.card_priority_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_card_priority_info -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.card_priority_info_content)
                    .setTitle(R.string.card_priority_info_title)
                    .setPositiveButton(android.R.string.yes, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

@Composable
fun CardPriorityView(
    items: SnapshotStateList<CardPriorityListItem>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(stringResource(R.string.card_priority_high))
            Text(stringResource(R.string.card_priority_low))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(items) { index, item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.card_priority_wave_number, index + 1),
                        modifier = Modifier.padding(vertical = 5.dp)
                    )

                    CardPriorityDragSort(item.scores)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 5.dp)
                    ) {
                        var braveChains by item.braveChains

                        val braveChainDialog = listDialog(
                            selected = braveChains,
                            selectedChange = { braveChains = it },
                            entries = BraveChainEnum.values()
                                .associateWith { stringResource(it.stringRes) },
                            title = stringResource(R.string.p_brave_chains)
                        )

                        ListItem(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { braveChainDialog.show() },
                            text = { Text(stringResource(R.string.p_brave_chains)) },
                            secondaryText = { Text(stringResource(braveChains.stringRes)) }
                        )

                        var rearrange by item.rearrangeCards

                        ListItem(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { rearrange = !rearrange },
                            text = { Text(stringResource(R.string.p_rearrange_cards)) },
                            trailing = {
                                Checkbox(
                                    checked = rearrange,
                                    onCheckedChange = { rearrange = it }
                                )
                            }
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = {
                    if (items.size > 1) {
                        items.removeLast()
                    }
                },
                enabled = items.size > 0
            ) {
                Text(stringResource(R.string.card_priority_remove_last_wave))
            }

            TextButton(
                onClick = {
                    items.add(
                        CardPriorityListItem(
                            items[0].scores.toMutableList(),
                            mutableStateOf(false),
                            mutableStateOf(BraveChainEnum.None)
                        )
                    )
                }
            ) {
                Text(stringResource(R.string.card_priority_add_wave))
            }
        }
    }
}

@Composable
fun CardPriorityDragSort(scores: MutableList<CardScore>) {
    AndroidView(
        viewBlock = { context ->
            val adapter = CardPriorityAdapter(scores)

            val recyclerView = RecyclerView(context).apply {
                setHasFixedSize(true)
                this.adapter = adapter
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            val callback = ItemTouchHelperCallback(adapter)
            val itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper.attachToRecyclerView(recyclerView)

            adapter.itemTouchHelper = itemTouchHelper

            recyclerView
        }
    )
}