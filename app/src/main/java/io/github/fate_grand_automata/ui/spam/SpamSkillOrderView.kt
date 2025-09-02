package io.github.fate_grand_automata.ui.spam

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.NpGaugeEnum
import io.github.fate_grand_automata.scripts.enums.SpamEnum
import io.github.fate_grand_automata.scripts.enums.StarConditionEnum
import io.github.fate_grand_automata.scripts.models.AutoSkillCommand
import io.github.fate_grand_automata.scripts.models.SkillSpamTarget
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.spam.SpamScreenViewModel.SkillSpamState
import io.github.fate_grand_automata.util.ItemTouchHelperCallback

data class SkillWithServantRef(
    val servantIndex: Int,
    val skillIndex: Int,
    val state: SkillSpamState
)

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

        val context = LocalContext.current

        val priorityAdapter = remember {
            createAdapter(context, priority, onSkillDragged)
        }

        AndroidView(
            factory = { context ->
                RecyclerView(context).apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = priorityAdapter.also { adapter ->
                        val callback = ItemTouchHelperCallback(adapter)
                        val itemTouchHelper = ItemTouchHelper(callback)
                        itemTouchHelper.attachToRecyclerView(this)
                        adapter.itemTouchHelper = itemTouchHelper
                    }
                }
            },
            update = { recycler ->
                priorityAdapter.refreshIfDataChanged()
            },
            modifier = Modifier.wrapContentWidth()
        )
    }
}

private fun createAdapter(
    context: Context,
    items: MutableList<SkillWithServantRef>,
    onSkillDragged: (List<SkillWithServantRef>) -> Unit
): DragSortSkillPriorityAdapter<SkillWithServantRef> {
    return DragSortSkillPriorityAdapter(items) { item ->
        val teamSlot = (item.servantIndex + 1).toString()
        val skillSlot = when (item.skillIndex) {
            0 -> "L"
            1 -> "M"
            else -> "R"
        }
        DragSortSkillPriorityAdapter.ItemViewConfig(
            teamForegroundColor = ContextCompat.getColor(
                context, when(item.servantIndex) {
                    1 -> R.color.servant1_primary_text
                    2 -> R.color.servant2_primary_text
                    3 -> R.color.servant3_primary_text
                    4 -> R.color.servant4_primary_text
                    5 -> R.color.servant5_primary_text
                    else -> R.color.servant6_primary_text
                }
            ),
            skillForegroundColor = ContextCompat.getColor(
                context, when(item.servantIndex) {
                    1 -> R.color.servant1_secondary_text
                    2 -> R.color.servant2_secondary_text
                    3 -> R.color.servant3_secondary_text
                    4 -> R.color.servant4_secondary_text
                    5 -> R.color.servant5_secondary_text
                    else -> R.color.servant6_secondary_text
                }
            ),
            backgroundColor = ContextCompat.getColor(
                context, when(item.servantIndex) {
                    1 -> R.color.servant1_bg
                    2 -> R.color.servant2_bg
                    3 -> R.color.servant3_bg
                    4 -> R.color.servant4_bg
                    5 -> R.color.servant5_bg
                    else -> R.color.servant6_bg
                }
            ),
            teamSlot = teamSlot,
            skillSlot = skillSlot
        )
    }.apply {
        this.onSkillDragged = onSkillDragged
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