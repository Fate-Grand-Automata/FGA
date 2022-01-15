package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.SupportPrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.enums.canAlsoCheckAll
import com.mathewsachin.fategrandautomata.ui.DimmedIcon
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.*
import com.mathewsachin.fategrandautomata.util.stringRes
import java.io.File

@Composable
fun SupportGroup(
    config: SupportPrefsCore,
    maxSkillText: String,
    goToPreferred: () -> Unit
) {
    val supportMode by config.selectionMode.remember()

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            PreferenceGroupHeader(
                title = stringResource(R.string.p_battle_config_support)
            )

            var supportClass by config.supportClass.remember()

            SupportClassPicker(
                selected = supportClass,
                onSelectedChange = { supportClass = it }
            )

            val canAlsoCheckAll = supportClass.canAlsoCheckAll && supportMode != SupportSelectionModeEnum.Manual

            AnimatedVisibility(canAlsoCheckAll) {
                config.alsoCheckAll.SwitchPreference(
                    title = stringResource(R.string.p_battle_config_support_also_check_all)
                )
            }

            val preferredMode = supportMode == SupportSelectionModeEnum.Preferred

            Row {
                config.selectionMode.ListPreference(
                    title = stringResource(R.string.p_battle_config_support_selection_mode),
                    entries = SupportSelectionModeEnum.values()
                        .associateWith { stringResource(it.stringRes) },
                    modifier = Modifier.weight(1f)
                )

                if (preferredMode) {
                    config.fallbackTo.SingleSelectChipPreference(
                        title = stringResource(R.string.p_battle_config_support_fallback_selection_mode),
                        entries = listOf(
                            SupportSelectionModeEnum.First,
                            SupportSelectionModeEnum.Manual
                        ).associateWith { stringResource(it.stringRes) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedVisibility (preferredMode) {
                Column {
                    Preference(
                        title = { Text(stringResource(R.string.p_support_mode_preferred)) },
                        summary = {
                            val servants by config.preferredServants.remember()
                            val ces by config.preferredCEs.remember()
                            val cesFormatted by derivedStateOf {
                                ces
                                    .map { File(it).nameWithoutExtension }
                                    .toSet()
                            }
                            val friendNames by config.friendNames.remember()
                            val friendNamesFormatted by derivedStateOf {
                                friendNames
                                    .map { File(it).nameWithoutExtension }
                                    .toSet()
                            }

                            PreferredSummary(
                                config = config,
                                maxSkillText = maxSkillText,
                                servants = servants,
                                ces = cesFormatted,
                                friendNames = friendNamesFormatted
                            )
                        },
                        onClick = goToPreferred
                    )
                }
            }
        }
    }
}

@Composable
fun SupportClassPicker(
    selected: SupportClass,
    onSelectedChange: (SupportClass) -> Unit
) {
    Card(
        elevation = 2.dp,
        modifier = Modifier
            .padding(16.dp, 5.dp)
            .fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(SupportClass.values().drop(1)) {
                val isSelected = selected == it
                val transition = updateTransition(isSelected, label = "Selected")
                val alpha by transition.animateFloat(label = "alpha") { selected ->
                    if (selected) 1f else 0.4f
                }
                val borderClass by transition.animateColor(label = "border color") { selected ->
                    if (selected)
                        MaterialTheme.colors.secondary
                    else Color.Transparent
                }

                Image(
                    painterResource(it.drawable),
                    contentDescription = it.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(25.dp)
                        .alpha(alpha)
                        .border(2.dp, borderClass, DiamondShape)
                        .clip(DiamondShape)
                        .clickable {
                            onSelectedChange(
                                if (isSelected)
                                    SupportClass.None
                                else it
                            )
                        }
                )
            }
        }
    }
}

val SupportClass.drawable @DrawableRes get() = when (this) {
    SupportClass.None -> R.drawable.ic_dots_horizontal
    SupportClass.All -> R.drawable.support_all
    SupportClass.Saber -> R.drawable.support_saber
    SupportClass.Archer -> R.drawable.support_archer
    SupportClass.Lancer -> R.drawable.support_lancer
    SupportClass.Rider -> R.drawable.support_rider
    SupportClass.Caster -> R.drawable.support_caster
    SupportClass.Assassin -> R.drawable.support_assassin
    SupportClass.Berserker -> R.drawable.support_berserker
    SupportClass.Extra -> R.drawable.support_extra
    SupportClass.Mix -> R.drawable.support_mix
}

val DiamondShape = CutCornerShape(50)

@Composable
fun PreferredSummary(
    config: SupportPrefsCore,
    maxSkillText: String,
    servants: Set<String>,
    ces: Set<String>,
    friendNames: Set<String>
) {
    Column(
        modifier = Modifier
            .padding(vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 2.dp)
        ) {
            DimmedIcon(
                icon(R.drawable.ic_crown),
                contentDescription = "crown"
            )

            val text = if (servants.isNotEmpty())
                servants.joinToString()
            else stringResource(R.string.battle_config_support_any)

            Text(
                text,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            if (servants.isNotEmpty()) {
                Card(
                    elevation = 2.dp
                ) {
                    Text(
                        maxSkillText,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier
                            .padding(5.dp, 1.dp)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 2.dp)
        ) {
            DimmedIcon(
                icon(R.drawable.ic_card),
                contentDescription = "card"
            )

            val text = if (ces.isNotEmpty())
                ces.joinToString()
            else stringResource(R.string.battle_config_support_any)

            Text(
                text,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )

            if (ces.isNotEmpty()) {
                val mlb by config.mlb.remember()

                if (mlb) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "MLB",
                        tint = MaterialTheme.colors.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        val requireFriends by config.requireFriends.remember()

        if (requireFriends) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 2.dp)
            ) {
                DimmedIcon(
                    icon(R.drawable.ic_friend),
                    contentDescription = "friend"
                )

                val text = if (friendNames.isNotEmpty())
                    friendNames.joinToString()
                else stringResource(R.string.battle_config_support_any)

                Text(
                    text,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
            }
        }
    }
}