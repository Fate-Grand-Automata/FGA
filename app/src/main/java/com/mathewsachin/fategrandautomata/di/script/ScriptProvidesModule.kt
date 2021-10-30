package com.mathewsachin.fategrandautomata.di.script

import com.mathewsachin.fategrandautomata.scripts.FgoGameAreaManager
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillCommand
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.ServantPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.SpamConfigPerTeamSlot
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.isNewUI
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.dagger.ScriptScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

@Module
@InstallIn(ScriptComponent::class)
class ScriptProvidesModule {
    @ScriptScope
    @Provides
    fun provideExitManager() = ExitManager()

    @ScriptScope
    @Provides
    fun provideGameAreaManager(platformImpl: IPlatformImpl, prefs: IPreferences): GameAreaManager =
        FgoGameAreaManager(
            gameSizeWithBorders = platformImpl.windowRegion.size,
            offset = { platformImpl.windowRegion.location },
            isNewUI = prefs.isNewUI
        )

    @ScriptScope
    @Provides
    fun provideBattleConfig(prefs: IPreferences): IBattleConfig =
        prefs.selectedBattleConfig

    @ScriptScope
    @Provides
    fun provideSupportPrefs(battleConfig: IBattleConfig): ISupportPreferences =
        battleConfig.support

    @ScriptScope
    @Provides
    fun provideSpamConfig(battleConfig: IBattleConfig): SpamConfigPerTeamSlot =
        SpamConfigPerTeamSlot(battleConfig.spam)

    @ScriptScope
    @Provides
    fun provideSkillCommand(battleConfig: IBattleConfig): AutoSkillCommand =
        AutoSkillCommand.parse(battleConfig.skillCommand)

    @ScriptScope
    @Provides
    fun provideCardPriority(battleConfig: IBattleConfig): CardPriorityPerWave =
        battleConfig.cardPriority

    @ScriptScope
    @Provides
    fun provideServantPriority(battleConfig: IBattleConfig): ServantPriorityPerWave? =
        if (battleConfig.useServantPriority) battleConfig.servantPriority else null
}