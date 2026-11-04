package io.github.fate_grand_automata.atlas

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

/**
 * The window is measured from the images shipped in `assets/Support/servant`, which were cut from
 * the game's support list. The markers below pin it to the middle of the art rather than its top
 * or right edge, so a change to the offsets can't pass unnoticed.
 */
class SupportServantImageTest {
    private val faceSize = 128

    @Test
    fun `a support image has the size the support list shows`() {
        val image = SupportServantImage.render(blankFace(), faceSize, faceSize)

        assertThat(image.size).isEqualTo(SupportServantImage.WIDTH * SupportServantImage.HEIGHT)
    }

    @Test
    fun `the window covers the face and not the top of the art`() {
        val overTheFace = SupportServantImage.render(faceWithBlockAt(50, 50), faceSize, faceSize)
        val aboveTheFace = SupportServantImage.render(faceWithBlockAt(50, 5), faceSize, faceSize)

        assertThat(overTheFace.any { red(it) > 200 }).isTrue()
        assertThat(aboveTheFace.any { red(it) > 200 }).isFalse()
    }

    @Test
    fun `the window keeps the left of the art and cuts the right`() {
        val onTheLeft = SupportServantImage.render(faceWithBlockAt(5, 50), faceSize, faceSize)
        val onTheRight = SupportServantImage.render(faceWithBlockAt(115, 50), faceSize, faceSize)

        assertThat(onTheLeft.any { red(it) > 200 }).isTrue()
        assertThat(onTheRight.any { red(it) > 200 }).isFalse()
    }

    @Test
    fun `the window starts below the top of the art and ends above its bottom`() {
        // The art is scaled up to 157x157 first, so source row 38 is where the window begins and
        // row 74 is where it ends.
        val aboveTheWindow = SupportServantImage.render(faceWithBlockAt(50, 10, block = 20), faceSize, faceSize)
        val belowTheWindow = SupportServantImage.render(faceWithBlockAt(50, 90, block = 20), faceSize, faceSize)
        val insideTheWindow = SupportServantImage.render(faceWithBlockAt(50, 40, block = 8), faceSize, faceSize)

        assertThat(aboveTheWindow.any { red(it) > 200 }).isFalse()
        assertThat(insideTheWindow.any { red(it) > 200 }).isTrue()
        assertThat(belowTheWindow.any { red(it) > 200 }).isFalse()
    }

    @Test
    fun `a gradient keeps its direction`() {
        val gradient = IntArray(faceSize * faceSize) { index ->
            val x = index % faceSize

            0xFF000000.toInt() or (x shl 16) or (x shl 8) or x
        }

        val image = SupportServantImage.render(gradient, faceSize, faceSize)

        assertThat(red(image.first()) < red(image[SupportServantImage.WIDTH - 1])).isTrue()
    }

    @Test
    fun `the alpha channel survives the scaling`() {
        val image = SupportServantImage.render(blankFace(), faceSize, faceSize)

        assertThat(image.all { (it ushr 24) == 0xFF }).isTrue()
    }

    private fun blankFace() = IntArray(faceSize * faceSize) { 0xFF000000.toInt() }

    private fun faceWithBlockAt(x: Int, y: Int, block: Int = 10) = blankFace().also { pixels ->
        for (blockY in y until y + block) {
            for (blockX in x until x + block) {
                pixels[blockY * faceSize + blockX] = 0xFFFFFFFF.toInt()
            }
        }
    }

    private fun red(pixel: Int) = (pixel shr 16) and 0xFF
}
