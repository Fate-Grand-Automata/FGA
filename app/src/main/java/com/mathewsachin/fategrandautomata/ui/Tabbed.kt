package com.mathewsachin.fategrandautomata.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun <T> Tabbed(
    items: List<T>,
    heading: @Composable (T) -> Unit,
    content: @Composable (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = items.size)
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
    ) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            },
            edgePadding = 0.dp,
            backgroundColor = MaterialTheme.colors.surface
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
            content = { content(items[it]) },
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .weight(1f)
        )
    }
}