package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.FGAListItemColors
import io.github.fate_grand_automata.ui.drag_sort.DragSort
import io.github.fate_grand_automata.ui.drag_sort.DragSortItemStyle
import io.github.fate_grand_automata.ui.prefs.SwitchPreference
import io.github.fate_grand_automata.ui.prefs.listDialog
import io.github.fate_grand_automata.ui.prefs.remember
import kotlinx.coroutines.launch

@Composable
fun CardPriorityView(
    items: SnapshotStateList<CardPriorityListItem>,
    useServantPriority: Pref<Boolean>,
    useMightyChain: Pref<Boolean>,
    npCardTypes: Pref<List<CardTypeEnum>>,
    mightyChainTurns: List<List<MightyChainAttack>>,
    mightyChainOrders: List<SnapshotStateList<CardTypeEnum>>
) {
    val pagerState = rememberPagerState(pageCount = {items.size},)
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Heading(stringResource(R.string.card_priority))

            val servantPriority by useServantPriority.remember()
            useServantPriority.SwitchPreference(
                title = stringResource(R.string.p_battle_config_use_servant_priority),
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )

            val mightyChain by useMightyChain.remember()
            useMightyChain.SwitchPreference(
                title = stringResource(R.string.p_battle_config_use_mighty_chain),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            CardPriorityWaveSelector(
                items = items,
                selectedWave = pagerState.currentPage,
                onSelectedWaveChange = { scope.launch { pagerState.animateScrollToPage(it) } },
            )

            HorizontalDivider()

            if (mightyChain) {
                val waveAttacks = mightyChainTurns
                    .getOrNull(pagerState.currentPage)
                    .orEmpty()
                MightyChainSettings(
                    npCardTypes = npCardTypes,
                    selectedNpSlots = waveAttacks
                        .flatMap { it.npSlots }
                        .distinct()
                        .sorted(),
                    attack = waveAttacks.firstOrNull(),
                    order = mightyChainOrders.getOrNull(pagerState.currentPage)
                )
                HorizontalDivider()
            }

            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column {
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

                    items.getOrNull(it)?.Render(
                        useServantPriority = servantPriority,
                        useMightyChain = mightyChain
                    )
                }
            }
        }
    }
}

@Composable
private fun MightyChainSettings(
    npCardTypes: Pref<List<CardTypeEnum>>,
    selectedNpSlots: List<Int>,
    attack: MightyChainAttack?,
    order: SnapshotStateList<CardTypeEnum>?
) {
    var types by npCardTypes.remember()
    val entries = linkedMapOf(
        CardTypeEnum.Buster to stringResource(R.string.p_chain_type_buster),
        CardTypeEnum.Arts to stringResource(R.string.p_chain_type_arts),
        CardTypeEnum.Quick to stringResource(R.string.p_chain_type_quick),
        CardTypeEnum.Unknown to stringResource(R.string.p_mighty_chain_np_type_unknown),
    )

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        if (selectedNpSlots.isNotEmpty()) {
            Text(
                text = stringResource(R.string.p_mighty_chain_np_types),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    selectedNpSlots.forEach { index ->
                        val selected = types.getOrElse(index) { CardTypeEnum.Unknown }
                        val dialog = listDialog(
                            selected = selected,
                            onSelectedChange = { selectedType ->
                                types = List(3) { slot ->
                                    if (slot == index) selectedType
                                    else types.getOrElse(slot) { CardTypeEnum.Unknown }
                                }
                            },
                            entries = entries,
                            title = stringResource(R.string.p_mighty_chain_servant_np_type, index + 1)
                        )

                        ListItem(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dialog.show() },
                            headlineContent = {
                                Text(stringResource(R.string.p_mighty_chain_servant_np, index + 1))
                            },
                            supportingContent = { Text(entries[selected].orEmpty()) },
                            colors = FGAListItemColors()
                        )
                    }
                }
            }
        }

        if (order != null) {
            MightyChainOrder(
                order = order,
                attack = attack,
                npTypes = types
            )
        }
    }
}

@Composable
private fun MightyChainOrder(
    order: SnapshotStateList<CardTypeEnum>,
    attack: MightyChainAttack?,
    npTypes: List<CardTypeEnum>
) {
    val labels = mapOf(
        CardTypeEnum.Buster to stringResource(R.string.p_chain_type_buster),
        CardTypeEnum.Arts to stringResource(R.string.p_chain_type_arts),
        CardTypeEnum.Quick to stringResource(R.string.p_chain_type_quick)
    )
    val npLabel = stringResource(R.string.p_mighty_chain_np)
    val cardLabel = stringResource(R.string.p_mighty_chain_card)

    val npCards = attack?.npSlots.orEmpty().take(3).map { slot ->
        MightyChainSlot.Np(
            servantSlot = slot,
            type = npTypes.getOrElse(slot) { CardTypeEnum.Unknown },
        )
    }
    val knownNpTypes = npCards.map { it.type }
    val canMakeMighty = knownNpTypes.none { it == CardTypeEnum.Unknown }
        && knownNpTypes.distinct().size == knownNpTypes.size
    val faceCardCount = (3 - npCards.size).coerceAtLeast(0)
    val faceCards = if (canMakeMighty) {
        order.filterNot { it in knownNpTypes }
            .take(faceCardCount)
            .map { MightyChainSlot.Face(it) }
    } else {
        List(faceCardCount) { MightyChainSlot.Face(null) }
    }
    val cardsBeforeNp = attack?.cardsBeforeNp
        ?.coerceIn(0, faceCardCount)
        ?: 0
    val displaySignature = listOf(cardsBeforeNp) + npCards.map { it.servantSlot to it.type } +
        faceCards.map { it.type }
    val displayItems = remember(displaySignature) {
        (
            faceCards.take(cardsBeforeNp) +
                npCards +
                faceCards.drop(cardsBeforeNp)
            ).toMutableStateList()
    }

    DragSort(
        items = displayItems,
        key = { item ->
            when (item) {
                is MightyChainSlot.Face -> "face-${item.type?.name ?: "unknown"}"
                is MightyChainSlot.Np -> "np-${item.servantSlot}"
            }
        },
        canDrag = { it is MightyChainSlot.Face && it.type != null },
        onMove = { fromIndex, toIndex ->
            val from = displayItems.getOrNull(fromIndex) as? MightyChainSlot.Face
            val to = displayItems.getOrNull(toIndex) as? MightyChainSlot.Face
            if (from?.type != null && to?.type != null) {
                val fromOrderIndex = order.indexOf(from.type)
                val toOrderIndex = order.indexOf(to.type)
                if (fromOrderIndex >= 0 && toOrderIndex >= 0) {
                    order[fromOrderIndex] = to.type
                    order[toOrderIndex] = from.type
                    displayItems[fromIndex] = to
                    displayItems[toIndex] = from
                }
            }
        },
        style = { item ->
            val cardType = item.type
            DragSortItemStyle(
                backgroundColor = colorResource(
                    when (cardType) {
                        CardTypeEnum.Buster -> R.color.colorBuster
                        CardTypeEnum.Arts -> R.color.colorArts
                        CardTypeEnum.Quick -> R.color.colorQuick
                        CardTypeEnum.Unknown -> R.color.colorPrimaryDark
                        null -> R.color.colorPrimaryDark
                    }
                ),
                contentColor = Color.White,
                text = when (item) {
                    is MightyChainSlot.Np -> npLabel
                    is MightyChainSlot.Face -> item.type?.let { labels[it] }.orEmpty().ifEmpty { cardLabel }
                }
            )
        },
        modifier = Modifier.fillMaxWidth()
    )

    Text(
        stringResource(R.string.card_priority_hint_move_item),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    if (
        npCards.size > 1
        && knownNpTypes.none { it == CardTypeEnum.Unknown }
        && knownNpTypes.distinct().size != knownNpTypes.size
    ) {
        Text(
            stringResource(R.string.p_mighty_chain_same_np_type_unavailable),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

private sealed interface MightyChainSlot {
    val type: CardTypeEnum?

    data class Face(override val type: CardTypeEnum?) : MightyChainSlot

    data class Np(
        val servantSlot: Int,
        override val type: CardTypeEnum,
    ) : MightyChainSlot
}
