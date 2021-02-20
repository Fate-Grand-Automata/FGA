package com.mathewsachin.fategrandautomata.ui.card_priority

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
        var selectedWave by remember { mutableStateOf(0) }

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
            ) {
                itemsIndexed(items) { index, _ ->
                    val isSelected = selectedWave == index

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colors.secondary else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { selectedWave = index }
                    ) {
                        Text(
                            stringResource(R.string.card_priority_wave_number, index + 1),
                            color = if (isSelected) MaterialTheme.colors.onSecondary else Color.Unspecified,
                            modifier = Modifier.padding(5.dp, 2.dp)
                        )

                        if (index > 0 && index == items.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        if (items.size > 1) {
                                            if (selectedWave == items.lastIndex) {
                                                selectedWave = items.lastIndex - 1
                                            }
                                            items.removeLast()
                                        }
                                    }
                            ) {
                                Icon(
                                    vectorResource(R.drawable.ic_close),
                                    tint = MaterialTheme.colors.error
                                )
                            }
                        }
                    }
                }
            }

            if (items.size < 3) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colors.secondary,
                            shape = CircleShape
                        )
                        .clickable {
                            if (items.size < 3) {
                                items.add(
                                    CardPriorityListItem(
                                        items[0].scores.toMutableList(),
                                        mutableStateOf(false),
                                        mutableStateOf(BraveChainEnum.None)
                                    )
                                )
                            }
                        }
                ) {
                    Icon(
                        vectorResource(R.drawable.ic_plus),
                        tint = MaterialTheme.colors.onSecondary
                    )
                }
            }
        }

        Divider()

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 5.dp)
                .padding(top = 11.dp)
        ) {
            Text(stringResource(R.string.card_priority_high))
            Text(stringResource(R.string.card_priority_low))
        }

        items[selectedWave].Render()
    }
}

@Composable
fun CardPriorityListItem.Render() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardPriorityDragSort(scores)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 5.dp)
        ) {
            var braveChains by braveChains

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

            var rearrange by rearrangeCards

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