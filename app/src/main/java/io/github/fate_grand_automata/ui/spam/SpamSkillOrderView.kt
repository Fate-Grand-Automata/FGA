package io.github.fate_grand_automata.ui.spam

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.scripts.enums.NpGaugeEnum
import io.github.fate_grand_automata.scripts.enums.SpamEnum
import io.github.fate_grand_automata.scripts.enums.StarConditionEnum
import io.github.fate_grand_automata.scripts.models.AutoSkillCommand
import io.github.fate_grand_automata.scripts.models.SkillSpamTarget
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.spam.SpamScreenViewModel.SkillSpamState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

data class SkillWithServantRef(
    val servantIndex: Int,
    val skillIndex: Int,
    val state: SkillSpamState
) {
    val key = servantIndex * 10 + skillIndex
}

@Composable
fun SpamSkillPriorityView(
    priority: MutableList<SkillWithServantRef>,
    onSkillDragged: (List<SkillWithServantRef>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 5.dp)
                .padding(top = 11.dp)
        ) {
            Text("HIGH")
            Text("SKILL PRIORITY")
            Text("LOW")
        }

        val listState = rememberLazyListState()

        suspend fun updateList(from: Int, to: Int) {
            priority.apply {
                add(to, removeAt(from))
            }
            onSkillDragged(priority)
        }

        val reorderableLazyRowState = rememberReorderableLazyListState(
            lazyListState = listState,
            onMove = { from, to ->
                updateList(from.index, to.index)
            }
        )

        LazyRow(state = listState) {
            items(priority, key = { it.key }) {
                ReorderableItem(
                    state = reorderableLazyRowState,
                    key = it.key
                ) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                    Surface(
                        shadowElevation = elevation,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        SkillPriorityItem(
                            it.servantIndex,
                            it.skillIndex,
                            modifier = Modifier.draggableHandle())
                    }
                }
            }
        }
    }
}

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
private fun Preview_Test() {
    val items = remember {
        mutableStateListOf<SkillWithServantRef>().apply {
            for (servant in 0 until 6) {
                for (skill in 0 until 3) {
                    add(
                        SkillWithServantRef(
                            servantIndex = servant,
                            skillIndex = skill,
                            state = SkillSpamState(
                                spamMode = mutableStateOf(SpamEnum.Spam),
                                npMode = mutableStateOf(NpGaugeEnum.Low),
                                starCond = mutableStateOf(StarConditionEnum.None),
                                target = mutableStateOf(SkillSpamTarget.Slot1),
                                waves = mutableStateOf(setOf(1)),
                                act = mutableStateOf(AutoSkillCommand.parse("").stages.flatten().flatten().firstOrNull()),
                                priority = mutableIntStateOf(1),
                                repeatLimit = mutableIntStateOf(1)
                            )
                        )
                    )
                }
            }
        }
    }

    val index = remember { mutableStateOf(items.size) }

    FGATheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Button(
                onClick = {
                    val servantIndex = index.value++
                    val skillIndex = items.size % 3
                    items.add(
                        SkillWithServantRef(
                            servantIndex = servantIndex,
                            skillIndex = skillIndex,
                            state = SkillSpamState(
                                spamMode = mutableStateOf(SpamEnum.Spam),
                                npMode = mutableStateOf(NpGaugeEnum.Low),
                                starCond = mutableStateOf(StarConditionEnum.None),
                                target = mutableStateOf(SkillSpamTarget.Slot1),
                                waves = mutableStateOf(setOf(1)),
                                act = mutableStateOf(AutoSkillCommand.parse("").stages.flatten().flatten().firstOrNull()),
                                priority = mutableIntStateOf(items.size + 1),
                                repeatLimit = mutableIntStateOf(1)
                            )
                        )
                    )
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Add Skill")
            }

            SpamSkillPriorityView(
                items,
                onSkillDragged = { _ -> /* do nothing */ }
            )
        }
    }
}