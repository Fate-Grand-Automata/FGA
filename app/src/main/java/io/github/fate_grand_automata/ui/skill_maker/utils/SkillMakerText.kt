package io.github.fate_grand_automata.ui.skill_maker.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource

@ReadOnlyComposable
@Composable
fun changeNp2TypeSlot3Text(): String {
    val servants = ChangeNp2Type.slot3.map {
        stringResource(it.stringRes)
    }
    return servants.joinToString(COMBINE_SERVANT_ARRAY)
}

@ReadOnlyComposable
@Composable
fun changeNp3TypeSlot2Text(): String {
    val servants = ChangeNp3Type.slot2.map {
        stringResource(it.stringRes)
    }
    return servants.joinToString(COMBINE_SERVANT_ARRAY)
}

@ReadOnlyComposable
@Composable
fun choice2Slot1Text(): String {
    val servants = Choice2Type.slot1.map {
        stringResource(it.stringRes)
    }
    return servants.joinToString(COMBINE_SERVANT_ARRAY)
}

@ReadOnlyComposable
@Composable
fun choice2Slot2Text(): String {
    val servants = Choice2Type.slot2.map {
        stringResource(it.stringRes)
    }
    return servants.joinToString(COMBINE_SERVANT_ARRAY)
}

@ReadOnlyComposable
@Composable
fun choice2Slot3Text(): String {
    val servants = Choice2Type.slot3.map {
        stringResource(it.stringRes)
    }
    return servants.joinToString(COMBINE_SERVANT_ARRAY)
}

@ReadOnlyComposable
@Composable
fun choice3Slot1Text(): String {
    val servants = Choice3Type.slot1.map {
        stringResource(it.stringRes)
    }
    return servants.joinToString(COMBINE_SERVANT_ARRAY)
}

@ReadOnlyComposable
@Composable
fun choice3Slot3Text(): String {
    val servants = Choice3Type.slot3.map {
        stringResource(it.stringRes)
    }
    return servants.joinToString(COMBINE_SERVANT_ARRAY)
}

@ReadOnlyComposable
@Composable
fun transformSlot3Text(): String {
    val servants = TransformType.slot3.map {
        stringResource(it.stringRes)
    }
    return servants.joinToString(COMBINE_SERVANT_ARRAY)
}

private const val COMBINE_SERVANT_ARRAY = "\n"