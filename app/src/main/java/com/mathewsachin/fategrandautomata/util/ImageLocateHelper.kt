package com.mathewsachin.fategrandautomata.util

import android.os.Environment
import java.io.File

val storageDirs = StorageDirs(
    File(
        Environment.getExternalStorageDirectory(),
        "Fate-Grand-Automata"
    )
)