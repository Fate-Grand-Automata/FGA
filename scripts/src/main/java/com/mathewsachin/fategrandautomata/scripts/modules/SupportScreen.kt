package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.libautomata.Swiper
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class SupportScreen @Inject constructor(
    api: IFgoAutomataApi,
    private val swipe: Swiper
) : IFgoAutomataApi by api {
    fun scrollDown() {
        swipe(
            locations.support.listSwipeStart,
            locations.support.listSwipeEnd
        )
    }

    fun scrollToTop() {
        locations.support.listTopClick.click()
    }

    fun click(supportClass: SupportClass) =
        locations.support.locate(supportClass).click()

    fun delay(duration: Duration) = duration.wait()

    fun refresh() {
        locations.support.updateClick.click()
        Duration.seconds(1).wait()

        locations.support.updateYesClick.click()
    }

    fun isAnyDialogOpen() =
        images[Images.SupportExtra] !in locations.support.extraRegion

    fun noSupportsPresent() =
        images[Images.SupportNotFound] in locations.support.notFoundRegion

    private fun someSupportsPresent() =
        locations.support.confirmSetupButtonRegion.exists(
            images[Images.SupportConfirmSetupButton],
            similarity = Support.supportRegionToolSimilarity
        ) || images[Images.Guest] in locations.support.friendRegion

    fun isListLoaded() =
        useSameSnapIn { noSupportsPresent() || someSupportsPresent() }
}