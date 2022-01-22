package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import kotlinx.coroutines.launch

@Composable
fun CardPriorityView(
    items: SnapshotStateList<CardPriorityListItem>,
    useServantPriority: Pref<Boolean>
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Heading(stringResource(R.string.p_nav_card_priority))

        val servantPriority by useServantPriority.remember()
        useServantPriority.SwitchPreference(
            title = "Use Servant Priority",
            modifier = Modifier
                .padding(bottom = 16.dp)
        )

        CardPriorityWaveSelector(
            items = items,
            selectedWave = pagerState.currentPage,
            onSelectedWaveChange = { scope.launch { pagerState.animateScrollToPage(it) } },
        )

        Divider()

        HorizontalPager(
            state = pagerState,
            count = items.size,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .weight(1f)
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
                    useServantPriority = servantPriority
                )
            }
        }
    }
}