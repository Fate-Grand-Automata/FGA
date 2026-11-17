package io.github.fate_grand_automata.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource

sealed class VectorIcon {
    @Composable
    abstract fun asPainter(): Painter

    class Drawable(@param:DrawableRes val res: Int): VectorIcon() {
        @Composable
        override fun asPainter() = painterResource(res)
    }

    class Vector(val vector: ImageVector): VectorIcon() {
        @Composable
        override fun asPainter() =
            rememberVectorPainter(vector)
    }
}

fun icon(@DrawableRes res: Int) =
    VectorIcon.Drawable(res)

fun icon(vector: ImageVector) =
    VectorIcon.Vector(vector)