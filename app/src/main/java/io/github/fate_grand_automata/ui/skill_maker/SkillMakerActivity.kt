package io.github.fate_grand_automata.ui.skill_maker

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.scripts.models.Skill
import io.github.fate_grand_automata.ui.FgaScreen
import io.github.fate_grand_automata.ui.OnPause
import io.github.fate_grand_automata.ui.PreventRtl
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChoice3
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChoice2
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChoice2Target
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerSpaceIshtar
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerTwoTargets

@AndroidEntryPoint
class SkillMakerActivity : AppCompatActivity() {
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

    val turn by vm.turn
    val wave by vm.wave

    Crossfade(
        current,
        animationSpec = spring()
    ) { nav ->
        when (nav) {
            SkillMakerNav.Atk -> {
                SkillMakerAtk(
                    wave = wave,
                    turn = turn,
                    onNextWave = { vm.nextWave(it) },
                    onNextTurn = { vm.nextTurn(it) }
                )
            }

            is SkillMakerNav.TwoTargets -> {
                SkillMakerTwoTargets(
                    onTargetLeft = { vm.targetSkill(ServantTarget.Left) },
                    onTargetRight = { vm.targetSkill(ServantTarget.Right) }
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
                    showTwoTargets = nav.skill in Skill.Servant.skill3,
                    onTwoTargets = {
                        navigate(SkillMakerNav.TwoTargets(nav.skill))
                    },
                    showSpaceIshtar = nav.skill in Skill.Servant.skill2,
                    onSpaceIshtar = {
                        navigate(SkillMakerNav.SpaceIshtar(nav.skill))
                    },
                    showChoice2 = nav.skill in Skill.Servant.list,
                    onChoice2 = {
                        navigate(SkillMakerNav.Choice2(nav.skill))
                    },
                    showTransform = nav.skill in Skill.Servant.skill3,
                    onTransform = {
                        vm.targetSkill(ServantTarget.Transform)
                    },
                    showChoice3Slot1 = nav.skill in Skill.Servant.skill1,
                    showChoice3Slot3 = nav.skill in Skill.Servant.skill3,
                    onChoice3 = { slot ->
                        navigate(SkillMakerNav.Choice3(nav.skill, slot))
                    }
                )
            }

            is SkillMakerNav.SpaceIshtar -> {
                SkillMakerSpaceIshtar(
                    onSkillTarget = { vm.targetSkill(it) }
                )
            }

            is SkillMakerNav.Choice2 -> {
                SkillMakerChoice2(
                    onOption1 = { vm.targetSkill(ServantTarget.Option1) },
                    onOption2 = { vm.targetSkill(ServantTarget.Option2) },
                    goToTarget = nav.skill in Skill.Servant.skill2,
                    onTarget = { firstTarget -> navigate(SkillMakerNav.Choice2Target(nav.skill, firstTarget)) }
                )
            }

            is SkillMakerNav.Choice2Target -> {
                SkillMakerChoice2Target(onSkillTarget = { vm.targetSkill(listOf(nav.firstTarget, it)) })
            }
            is SkillMakerNav.Choice3 -> {
                SkillMakerChoice3(
                    slot = nav.slot,
                    onSkillTarget = { servantTarget ->
                        vm.targetSkill(servantTarget)
                    },
                )
            }
        }
    }
}