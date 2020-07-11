package com.mathewsachin.fategrandautomata.util

import android.accessibilityservice.AccessibilityService
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.mathewsachin.fategrandautomata.accessibility.AccessibilityGestures
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoFriendGacha
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoLottery
import com.mathewsachin.fategrandautomata.scripts.entrypoints.SupportImageMaker
import com.mathewsachin.fategrandautomata.scripts.modules.*
import com.mathewsachin.libautomata.*
import org.koin.dsl.module
import org.opencv.android.OpenCVLoader

class AutomataApplication : Application() {
    companion object{
        lateinit var Instance: Application
    }

    fun forceDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    override fun onCreate() {
        super.onCreate()

        Instance = this
        OpenCVLoader.initDebug()

        // forceDarkMode()
    }
}

fun makeKoinModule(
    platformImpl: IPlatformImpl,
    screenshotService: IScreenshotService,
    accessibilityService: AccessibilityService
) = module {
    single { platformImpl }
    single { screenshotService }
    single<IGestureService> { AccessibilityGestures(accessibilityService, get()) }

    single { ScreenshotManager(get(), get(), get(), get()) }
    single { GameAreaManager(get()) }
    single { ExitManager() }

    single<IDurationExtensions> { DurationExtensions(get()) }
    single<IHighlightExtensions> { HighlightExtensions(get(), get(), get()) }
    single<IImageMatchingExtensions> { ImageMatchingExtensions(get(), get(), get(), get(), get(), get()) }
    single<IGestureExtensions> { GestureExtensions(get(), get(), get()) }
    single<ITransformationExtensions> { TransformationExtensions(get()) }

    single<IAutomataExtensions> { AutomataExtensions(get(), get(), get(), get(), get(), get()) }

    single { AutoBattle(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { AutoFriendGacha(get(), get(), get(), get()) }
    single { AutoLottery(get(), get(), get(), get(), get()) }
    single { (callback: () -> Unit) -> SupportImageMaker(callback, get(), get(), get(), get()) }

    single { AutoSkill(get()) }
    single { Battle(get(), get()) }
    single { Card(get(), get(), get()) }
    single { Scaling(get()) }
    single { Support(get(), get(), get()) }
}