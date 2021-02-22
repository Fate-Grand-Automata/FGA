package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.ServantTarget
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerTargetFragment : Fragment() {
    val viewModel: SkillMakerViewModel by activityViewModels()
    val args: SkillMakerTargetFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        skillMakerScaffold {
            SkillTarget(
                onSkillTarget = ::onSkillTarget,
                showEmiya = args.showEmiya,
                onEmiya = ::onEmiya,
                showSpaceIshtar = args.showSpaceIshtar,
                onSpaceIshtar = ::onSpaceIshtar
            )
        }

    fun onSkillTarget(target: ServantTarget?) {
        viewModel.targetSkill(target)

        findNavController().popBackStack()
    }

    fun onSpaceIshtar() {
        val action = SkillMakerTargetFragmentDirections
            .actionTargetSkillMakerToSkillMakerSpaceIshtarFragment()

        nav(action)
    }

    fun onEmiya() {
        val action = SkillMakerTargetFragmentDirections
            .actionTargetSkillMakerToSkillMakerEmiyaFragment()

        nav(action)
    }
}

@Composable
fun SkillTarget(
    onSkillTarget: (ServantTarget?) -> Unit,
    showEmiya: Boolean,
    onEmiya: () -> Unit,
    showSpaceIshtar: Boolean,
    onSpaceIshtar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.skill_maker_target_header),
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
                color = colorResource(R.color.colorServant1),
                text = stringResource(R.string.skill_maker_target_servant, 1)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.B) },
                color = colorResource(R.color.colorServant2),
                text = stringResource(R.string.skill_maker_target_servant, 2)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.C) },
                color = colorResource(R.color.colorServant3),
                text = stringResource(R.string.skill_maker_target_servant, 3)
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (showEmiya) {
                Button(onClick = onEmiya) {
                    Text(stringResource(R.string.skill_maker_emiya))
                }
            }

            if (showSpaceIshtar) {
                Button(onClick = onSpaceIshtar) {
                    Text(stringResource(R.string.skill_maker_space_ishtar))
                }
            }

            Button(onClick = { onSkillTarget(null) }) {
                Text(stringResource(R.string.skill_maker_target_none))
            }
        }
    }
}