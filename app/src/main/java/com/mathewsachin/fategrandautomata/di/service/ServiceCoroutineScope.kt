package com.mathewsachin.fategrandautomata.di.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ServiceCoroutineScope

@Module
@InstallIn(ServiceComponent::class)
class ServiceCoroutineModule {
    @Provides
    @ServiceScoped
    @ServiceCoroutineScope
    fun provideServiceCoroutineScope() = CoroutineScope(Dispatchers.Default)
}