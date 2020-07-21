package com.mathewsachin.fategrandautomata.dagger.app

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppContextModule(val app: Application) {
    @Singleton
    @Provides
    fun provideContext(): Context = app
}