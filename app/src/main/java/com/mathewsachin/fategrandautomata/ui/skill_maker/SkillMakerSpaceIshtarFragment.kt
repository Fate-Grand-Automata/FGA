package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.ServantTarget
import com.mathewsachin.fategrandautomata.ui.prefs.compose.FgaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerSpaceIshtarFragment : Fragment() {
    val vm: SkillMakerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    SpaceIshtarType(onSkillTarget = ::onSkillTarget)
                }
            }
        }

    fun onSkillTarget(target: ServantTarget) {
        vm.targetSkill(target)

        findNavController().popBackStack()
    }
}

@Composable
fun SpaceIshtarType(
    onSkillTarget: (ServantTarget) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.skill_maker_space_ishtar),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TargetButton(
                onClick = { onSkillTarget(ServantTarget.A) },
                color = colorResource(R.color.colorQuickResist),
                text = stringResource(R.string.skill_maker_quick)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.B) },
                color = colorResource(R.color.colorArtsResist),
                text = stringResource(R.string.skill_maker_arts)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.C) },
                color = colorResource(R.color.colorBuster),
                text = stringResource(R.string.skill_maker_buster)
            )
        }
    }
}

@Preview(widthDp = 600, heightDp = 300)
@Composable
fun TestSpaceIshtar() {
    SpaceIshtarType(onSkillTarget = { })
}