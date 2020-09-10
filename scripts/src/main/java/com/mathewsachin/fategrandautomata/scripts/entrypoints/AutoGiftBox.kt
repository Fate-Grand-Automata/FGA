package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.libautomata.*
import javax.inject.Inject

class AutoGiftBox @Inject constructor(
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager, platformImpl, fgAutomataApi.messages), IFgoAutomataApi by fgAutomataApi {

    companion object {
        const val goldThreshold = 3
        const val maxClickCount = 99
        val checkRegion = Region(820, 225, 60, 1050)
    }

    private var clickCount = 0

    override fun script(): Nothing {
        while (clickCount < maxClickCount) {
            checkGifts()

            swipe(Location(700, 700), Location(700, 175))
        }

        throw ScriptExitException("Picked $clickCount EXP stacks")
    }

    private val countRegionX
        get() = when (prefs.gameServer) {
            GameServerEnum.Jp -> 355
            GameServerEnum.En -> 420
            else -> 420
        }

    sealed class OcrResult {
        object Failed : OcrResult()
        data class Success(val number: Int) : OcrResult()
    }

    private fun Region.ocr(): OcrResult =
        if (images.art in this)
            OcrResult.Success(0)
        else OcrResult.Failed

    private fun checkGifts() {
        for (gift in checkRegion.findAll(images.giftBoxCheck)) {
            val countRegion = Region(countRegionX, gift.Region.Y - 50, 100, 30)
            val iconRegion = Region(95, gift.Region.Y - 58, 115, 120)
            val clickSpot = Location(850, gift.Region.Y + 25)

            if (iconRegion.exists(images.goldXP)) {
                val ocrResult = countRegion.ocr()

                if (ocrResult is OcrResult.Failed) {
                    continue
                } else if (ocrResult is OcrResult.Success) {
                    if (ocrResult.number > goldThreshold) {
                        continue
                    }
                }

                clickSpot.click()
                ++clickCount
            }
        }
    }
}