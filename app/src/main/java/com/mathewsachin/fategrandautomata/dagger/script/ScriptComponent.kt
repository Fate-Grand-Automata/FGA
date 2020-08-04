package com.mathewsachin.fategrandautomata.dagger.script

import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoFriendGacha
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoLottery
import com.mathewsachin.fategrandautomata.scripts.entrypoints.SupportImageMaker
import com.mathewsachin.libautomata.dagger.ScriptScope
import dagger.Subcomponent

@ScriptScope
@Subcomponent(modules = [ScriptModule::class, ScreenshotModule::class])
interface ScriptComponent {
    @Subcomponent.Builder
    interface Builder {
        fun screenshotModule(module: ScreenshotModule): Builder

        fun build(): ScriptComponent
    }

    fun battle(): AutoBattle

    fun friendGacha(): AutoFriendGacha

    fun lottery(): AutoLottery

    fun supportImageMaker(): SupportImageMaker
}