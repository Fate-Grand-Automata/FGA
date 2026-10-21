package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.ui.FGAListItemColors
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.prefs.SwitchPreference
import io.github.fate_grand_automata.ui.prefs.listDialog
import io.github.fate_grand_automata.ui.prefs.remember
import kotlinx.coroutines.launch

@Composable
fun CardPriorityView(
    items: SnapshotStateList<CardPriorityListItem>,
    useServantPriority: Pref<Boolean>,
    useCardSequence: Pref<Boolean>,
    npCardTypes: Pref<List<CardTypeEnum>>,
    cardSequenceTurns: List<List<CardSequenceAttack>>,
    cardSequenceOrders: List<SnapshotStateList<CardTypeEnum>>,
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Heading(stringResource(R.string.card_priority))

            val servantPriority by useServantPriority.remember()
            useServantPriority.SwitchPreference(
                title = stringResource(R.string.p_battle_config_use_servant_priority),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            val sequenceEnabled by useCardSequence.remember()
            useCardSequence.SwitchPreference(
                title = stringResource(R.string.p_battle_config_use_card_sequence),
                modifier = Modifier.padding(bottom = 4.dp),
            )

            CardPriorityWaveSelector(
                items = items,
                selectedWave = pagerState.currentPage,
                onSelectedWaveChange = { scope.launch { pagerState.animateScrollToPage(it) } },
            )

            HorizontalDivider()

            if (sequenceEnabled) {
                val waveAttacks = cardSequenceTurns
                    .getOrNull(pagerState.currentPage)
                    .orEmpty()
                CardSequenceSettings(
                    npCardTypes = npCardTypes,
                    selectedNpSlots = waveAttacks.flatMap { it.npSlots }.distinct().sorted(),
                    attack = waveAttacks.firstOrNull(),
                    order = cardSequenceOrders.getOrNull(pagerState.currentPage),
                )
                HorizontalDivider()
            }

            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 5.dp)
                            .padding(top = 11.dp),
                    ) {
                        Text(stringResource(R.string.card_priority_high))
                        Text(stringResource(R.string.card_priority_low))
                    }

                    items.getOrNull(it)?.Render(
                        useServantPriority = servantPriority,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardSequenceSettings(
    npCardTypes: Pref<List<CardTypeEnum>>,
    selectedNpSlots: List<Int>,
    attack: CardSequenceAttack?,
    order: SnapshotStateList<CardTypeEnum>?,
) {
    var types by npCardTypes.remember()
    val entries = cardTypeEntries(includeUnknown = true)

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        if (selectedNpSlots.isNotEmpty()) {
            Text(
                text = stringResource(R.string.p_card_sequence_np_types),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
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
                            title = stringResource(R.string.p_card_sequence_servant_np_type, index + 1),
                        )

                        ListItem(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dialog.show() },
                            headlineContent = {
                                Text(stringResource(R.string.p_card_sequence_servant_np, index + 1))
                            },
                            supportingContent = { Text(entries[selected].orEmpty()) },
                            colors = FGAListItemColors(),
                        )
                    }
                }
            }
        }

        if (order != null) {
            CardSequenceOrder(order = order, attack = attack, npTypes = types)
        }
    }
}

@Composable
private fun CardSequenceOrder(
    order: SnapshotStateList<CardTypeEnum>,
    attack: CardSequenceAttack?,
    npTypes: List<CardTypeEnum>,
) {
    val entries = cardTypeEntries(includeUnknown = false)
    val npLabel = stringResource(R.string.p_card_sequence_np)
    val npSlots = attack?.npSlots.orEmpty().take(3)
    val faceCardCount = (3 - npSlots.size).coerceAtLeast(0)
    val cardsBeforeNp = attack?.cardsBeforeNp?.coerceIn(0, faceCardCount) ?: 0

    Text(
        text = stringResource(R.string.p_card_sequence_order),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(3) { position ->
            val npIndex = position - cardsBeforeNp
            val servantSlot = npSlots.getOrNull(npIndex)
            val isNp = servantSlot != null && position in cardsBeforeNp until cardsBeforeNp + npSlots.size
            val type = if (isNp) {
                npTypes.getOrElse(servantSlot) { CardTypeEnum.Unknown }
            } else {
                order.getOrElse(position) { CardTypeEnum.Buster }
            }

            if (isNp) {
                SequenceCard(label = npLabel, type = type, modifier = Modifier.weight(1f))
            } else {
                val dialog = listDialog(
                    selected = type,
                    onSelectedChange = { order[position] = it },
                    entries = entries,
                    title = stringResource(R.string.p_card_sequence_order),
                )
                SequenceCard(
                    label = entries[type].orEmpty(),
                    type = type,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { dialog.show() },
                )
            }

            if (position < 2) Text("→")
        }
    }

    Text(
        text = stringResource(R.string.p_card_sequence_hint),
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )
}

@Composable
private fun SequenceCard(
    label: String,
    type: CardTypeEnum,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorResource(
                when (type) {
                    CardTypeEnum.Buster -> R.color.colorBuster
                    CardTypeEnum.Arts -> R.color.colorArts
                    CardTypeEnum.Quick -> R.color.colorQuick
                    CardTypeEnum.Unknown -> R.color.colorPrimaryDark
                }
            ),
            contentColor = Color.White,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label)
        }
    }
}

@Composable
private fun cardTypeEntries(includeUnknown: Boolean): Map<CardTypeEnum, String> {
    val buster = stringResource(R.string.p_chain_type_buster)
    val arts = stringResource(R.string.p_chain_type_arts)
    val quick = stringResource(R.string.p_chain_type_quick)
    val unknown = stringResource(R.string.p_card_sequence_np_type_unknown)

    return buildMap {
        put(CardTypeEnum.Buster, buster)
        put(CardTypeEnum.Arts, arts)
        put(CardTypeEnum.Quick, quick)
        if (includeUnknown) put(CardTypeEnum.Unknown, unknown)
    }
}
