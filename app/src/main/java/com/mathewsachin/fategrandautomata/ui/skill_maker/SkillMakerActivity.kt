package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.ServantTarget
import com.mathewsachin.fategrandautomata.scripts.models.Skill
import com.mathewsachin.fategrandautomata.ui.FgaDialog
import com.mathewsachin.fategrandautomata.ui.FgaScreen
import com.mathewsachin.fategrandautomata.ui.OnPause
import com.mathewsachin.fategrandautomata.ui.PreventRtl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerActivity : ComponentActivity() {
    val vm: SkillMakerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FgaScreen {
                PreventRtl {
                    SkillMakerUI(
                        vm = vm,
                        exit = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun SkillMakerUI(
    vm: SkillMakerViewModel,
    exit: () -> Unit
) {
    OnPause {
        vm.saveState()
    }

    val exitConfirmDialog = FgaDialog()
    exitConfirmDialog.build {
        title(stringResource(R.string.skill_maker_confirm_exit_title))
        message(stringResource(R.string.skill_maker_confirm_exit_message))

        buttons(
            onSubmit = exit
        )
    }

    val clearConfirmDialog = FgaDialog()
    clearConfirmDialog.build {
        title(stringResource(R.string.skill_maker_confirm_clear_title))
        message(stringResource(R.string.skill_maker_confirm_clear_message))

        buttons(
            onSubmit = { vm.clearAll() }
        )
    }

    BackHandler {
        if (vm.navigation.value is SkillMakerNav.Main) {
            exitConfirmDialog.show()
        } else vm.navigation.value = SkillMakerNav.Main
    }

    val (current, navigate) = vm.navigation

    Crossfade(
        current,
        animationSpec = spring()
    ) { nav ->
        when (nav) {
            SkillMakerNav.Atk -> {
                SkillMakerAtk(
                    onNextWave = { vm.nextStage(it) },
                    onNextTurn = { vm.nextTurn(it) }
                )
            }
            is SkillMakerNav.Emiya -> {
                SkillMakerEmiya(
                    onArts = { vm.targetSkill(ServantTarget.Left) },
                    onBuster = { vm.targetSkill(ServantTarget.Right) }
                )
            }
            SkillMakerNav.Main -> {
                SkillMakerMain(
                    vm = vm,
                    onMasterSkills = { navigate(SkillMakerNav.MasterSkills) },
                    onAtk = { navigate(SkillMakerNav.Atk) },
                    onSkill = { vm.initSkill(it) },
                    onClear = { clearConfirmDialog.show() },
                    onDone = {
                        vm.battleConfig.skillCommand = vm.finish()
                        exit()
                    }
                )
            }
            SkillMakerNav.MasterSkills -> {
                SkillMakerMasterSkills(
                    onMasterSkill = { vm.initSkill(it) },
                    onOrderChange = { navigate(SkillMakerNav.OrderChange) }
                )
            }
            SkillMakerNav.OrderChange -> {
                SkillMakerOrderChange(
                    onCommit = { starting, sub ->
                        vm.commitOrderChange(starting, sub)
                    },
                    onCancel = { vm.back() }
                )
            }
            is SkillMakerNav.SkillTarget -> {
                SkillMakerTarget(
                    onSkillTarget = { vm.targetSkill(it) },
                    showEmiya = nav.skill in listOf(
                        Skill.Servant.A3,
                        Skill.Servant.B3,
                        Skill.Servant.C3
                    ),
                    onEmiya = {
                        navigate(SkillMakerNav.Emiya(nav.skill))
                    },
                    showSpaceIshtar = nav.skill in listOf(
                        Skill.Servant.A2,
                        Skill.Servant.B2,
                        Skill.Servant.C2
                    ),
                    onSpaceIshtar = {
                        navigate(SkillMakerNav.SpaceIshtar(nav.skill))
                    },
                    onKukulcan = {
                        navigate(SkillMakerNav.Kukulcan(nav.skill))
                    }
                )
            }
            is SkillMakerNav.SpaceIshtar -> {
                SkillMakerSpaceIshtar(
                    onSkillTarget = { vm.targetSkill(it) }
                )
            }
            is SkillMakerNav.Kukulcan -> {
                SkillMakerKukulcan(
                    onOption1 = { vm.targetSkill(ServantTarget.Option1) },
                    onOption2 = { vm.targetSkill(ServantTarget.Option2) },
                    goToTarget = nav.skill in listOf(
                        Skill.Servant.A2,
                        Skill.Servant.B2,
                        Skill.Servant.C2
                    ),
                    onTarget = { firstTarget -> navigate(SkillMakerNav.KukulcanTarget(nav.skill, firstTarget)) }
                )
            }
            is SkillMakerNav.KukulcanTarget -> {
                SkillMakerKukulcanTarget(onSkillTarget = { vm.targetSkill(listOf(nav.firstTarget, it)) })
            }
        }
    }
}