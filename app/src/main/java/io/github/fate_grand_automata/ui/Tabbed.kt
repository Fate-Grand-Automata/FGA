package io.github.fate_grand_automata.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun <T> Tabbed(
    items: List<T>,
    heading: @Composable (T) -> Unit,
    content: @Composable (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = {items.size})
    val scope = rememberCoroutineScope()

    LaunchedEffect(items.size) {
        snapshotFlow { items.size }.collectLatest {
            pagerState.scrollToPage(0)
        }
    }

    Column(
        modifier = modifier
    ) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            // Add tabs for all of our pages
            items.forEachIndexed { index, it ->
                Tab(
                    text = { heading(it) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            pageContent = { content(items[it]) },
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .weight(1f)
        )
    }
}