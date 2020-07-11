package com.mathewsachin.fategrandautomata

import com.mathewsachin.fategrandautomata.scripts.entrypoints.SupportImageMaker
import com.mathewsachin.fategrandautomata.util.makeKoinModule
import io.mockk.mockk
import org.junit.Test

import org.junit.Assert.*
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class ExampleUnitTest: KoinTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun kointest() {
        koinApplication {
            modules(makeKoinModule(
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true)
            ))
        }.checkModules {
            create<SupportImageMaker> { parametersOf({ }) }
        }
    }
}
