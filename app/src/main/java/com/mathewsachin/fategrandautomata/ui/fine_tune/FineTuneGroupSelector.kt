package com.mathewsachin.fategrandautomata.ui.fine_tune

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mathewsachin.fategrandautomata.ui.GroupSelector

@Composable
fun FineTuneGroupSelector(
    groups: List<FineTuneGroup>,
    selected: FineTuneGroup,
    onSelectedChange: (FineTuneGroup) -> Unit
) {
    GroupSelector(
        groups = groups,
        selected = selected,
        onSelectedChange = onSelectedChange,
        stringify = { stringResource(it.name) }
    )
}