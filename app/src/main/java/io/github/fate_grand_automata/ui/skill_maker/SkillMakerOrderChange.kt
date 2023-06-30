package io.github.fate_grand_automata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.OrderChangeMember
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.FGATitle

@Composable
fun SkillMakerOrderChange(
    onCommit: (starting: OrderChangeMember.Starting, sub: OrderChangeMember.Sub) -> Unit,
    onCancel: () -> Unit
) {
    var starting by rememberSaveable { mutableStateOf(1) }
    var sub by rememberSaveable { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_order_change_header)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column {
                Text(stringResource(R.string.skill_maker_order_change_starting_member))

                OrderChangeSide(selected = starting, onSelectedChange = { starting = it })
            }

            Column {
                Text(stringResource(R.string.skill_maker_order_change_sub_member))

                OrderChangeSide(selected = sub, onSelectedChange = { sub = it })
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(onClick = onCancel) {
                Text(stringResource(android.R.string.cancel))
            }

            Button(
                onClick = {
                    onCommit(
                        OrderChangeMember.Starting.list[starting - 1],
                        OrderChangeMember.Sub.list[sub - 1]
                    )
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        }
    }
}

@Composable
fun OrderChangeSide(
    selected: Int,
    onSelectedChange: (Int) -> Unit
) {
    Row {
        (1..3).map {
            val isSelected = selected == it

            val selectedColor = when (it) {
                1 -> R.color.colorServant1
                2 -> R.color.colorServant2
                3 -> R.color.colorServant3
                else -> R.color.colorAccent
            }

            Surface(
                tonalElevation = 5.dp,
                shape = MaterialTheme.shapes.medium,
                color =
                if (isSelected)
                    colorResource(selectedColor)
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(5.dp),
                onClick = { onSelectedChange(it) }
            ) {
                Text(
                    stringResource(R.string.skill_maker_order_change_servant, it),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    color =
                    if (isSelected)
                        Color.White
                    else Color.Unspecified
                )
            }
        }
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestOrderChange() {
    FGATheme {
        SkillMakerOrderChange(onCommit = { _, _ -> }, onCancel = { })
    }
}