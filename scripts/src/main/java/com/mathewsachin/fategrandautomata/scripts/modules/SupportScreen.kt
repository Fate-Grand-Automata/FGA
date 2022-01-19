package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.libautomata.Swiper
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface SupportScreen {
    fun scrollDown()
    fun scrollToTop()
    fun click(supportClass: SupportClass)
    fun delay(duration: Duration)
    fun refresh()
    fun isAnyDialogOpen(): Boolean
    fun noSupportsPresent(): Boolean
    fun someSupportsPresent(): Boolean
    fun isListLoaded(): Boolean
}

@ScriptScope
class RealSupportScreen @Inject constructor(
    api: IFgoAutomataApi,
    private val swipe: Swiper
) : IFgoAutomataApi by api, SupportScreen {
    override fun scrollDown() {
        swipe(
            locations.support.listSwipeStart,
            locations.support.listSwipeEnd
        )
    }

    override fun scrollToTop() {
        locations.support.listTopClick.click()
    }

    override fun click(supportClass: SupportClass) =
        locations.support.locate(supportClass).click()

    override fun delay(duration: Duration) = duration.wait()

    override fun refresh() {
        locations.support.updateClick.click()
        1.seconds.wait()

        locations.support.updateYesClick.click()
    }

    override fun isAnyDialogOpen() =
        images[Images.SupportExtra] !in locations.support.extraRegion

    override fun noSupportsPresent() =
        images[Images.SupportNotFound] in locations.support.notFoundRegion

    override fun someSupportsPresent() =
        locations.support.confirmSetupButtonRegion.exists(
            images[Images.SupportConfirmSetupButton],
            similarity = Support.supportRegionToolSimilarity
        ) || images[Images.Guest] in locations.support.friendRegion

    override fun isListLoaded() =
        useSameSnapIn { noSupportsPresent() || someSupportsPresent() }
}