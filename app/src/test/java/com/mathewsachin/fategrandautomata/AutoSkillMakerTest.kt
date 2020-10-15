package com.mathewsachin.fategrandautomata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.mathewsachin.fategrandautomata.ui.skill_maker.AutoSkillMakerActivityArgs
import com.mathewsachin.fategrandautomata.ui.skill_maker.SkillMakerViewModel
import io.mockk.mockk
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class AutoSkillMakerTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    fun getViewModel() = SkillMakerViewModel(
        mockk(relaxed = true),
        SavedStateHandle(mapOf(AutoSkillMakerActivityArgs::key.name to "foo"))
    )

    @Test
    fun test_empty() {
        val vm = getViewModel()

        assert(vm.finish().isEmpty())
    }

    fun test_skill(block: (SkillMakerViewModel, Char) -> Unit) {
        for (c in 'a'..'l') {
            val vm = getViewModel()

            vm.initSkill(c)
            block(vm, c)
        }
    }

    @Test
    fun test_skill_non_targeted() {
        test_skill { vm, c ->
            vm.targetSkill(null)

            Assert.assertEquals("$c", vm.finish())
        }
    }

    @Test
    fun test_skill_targeted() {
        for (target in '1'..'3') {
            test_skill { vm, c ->
                vm.targetSkill(target)

                Assert.assertEquals("${c}${target}", vm.finish())
            }
        }
    }

    @Test
    fun test_np() {
        for (np in '4'..'6') {
            val vm = getViewModel()

            vm.onNpClick(np)
            vm.nextStage()

            Assert.assertEquals("$np", vm.finish())
        }
    }

    @Test
    fun test_order_change() {
        for (starting in 1..3) {
            for (sub in 1..3) {
                val vm = getViewModel()

                vm.initOrderChange()
                vm.setOrderChangePartyMember(starting)
                vm.setOrderChangeSubMember(sub)
                vm.commitOrderChange()

                Assert.assertEquals("x${starting}${sub}", vm.finish())
            }
        }
    }
}