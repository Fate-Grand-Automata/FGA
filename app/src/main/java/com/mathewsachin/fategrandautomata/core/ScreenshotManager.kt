package com.mathewsachin.fategrandautomata.core

class ScreenshotManager {
    companion object {
        private var impl: IScreenshotService? = null

        fun register(Impl: IScreenshotService) {
            impl = Impl
        }

        var usePreviousSnap = false

        private var previousPattern: IPattern? = null
        private var resizeTarget: IPattern? = null

        private fun getScaledScreenshot(): IPattern {
            val sshot = impl!!.takeScreenshot()
                .crop(GameAreaManager.GameArea)

            val scale = screenToImageScale()

            if (scale != null) {
                if (resizeTarget == null) {
                    resizeTarget = AutomataApi.getResizableBlankPattern()
                }

                sshot.resize(resizeTarget!!, sshot.Size * scale)

                return resizeTarget!!
            }

            return sshot
        }

        fun snapshot() {
            previousPattern = getScaledScreenshot()
            usePreviousSnap = true
        }

        fun getScreenshot(): IPattern? {
            if (usePreviousSnap) {
                return previousPattern
            }

            previousPattern = getScaledScreenshot()
            return previousPattern
        }

        fun releaseMemory() {
            previousPattern?.close()
            previousPattern = null

            resizeTarget?.close()
            resizeTarget = null
        }
    }
}