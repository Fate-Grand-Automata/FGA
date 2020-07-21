package com.mathewsachin.fategrandautomata.util

import android.icu.util.VersionInfo
import com.mathewsachin.fategrandautomata.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

const val githubApiReleasesLink =
    "https://api.github.com/repos/MathewSachin/Fate-Grand-Automata/releases"
const val websiteLink =
    "https://mathewsachin.github.io/Fate-Grand-Automata"

val isDevelopmentBuild get() = !BuildConfig.VERSION_NAME.startsWith('v')
val currentVersion: VersionInfo
    get() = VersionInfo.getInstance(BuildConfig.VERSION_NAME.substring(1))

suspend fun getLatestReleaseTag() = withContext(Dispatchers.IO) {
    val releases = URL(githubApiReleasesLink).readText()
    val latestRelease = JSONArray(releases).getJSONObject(0)
    latestRelease.getString("tag_name") ?: ""
}