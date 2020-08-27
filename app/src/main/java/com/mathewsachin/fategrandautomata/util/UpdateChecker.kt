package com.mathewsachin.fategrandautomata.util

import android.icu.util.VersionInfo
import com.mathewsachin.fategrandautomata.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import javax.inject.Inject

class UpdateChecker @Inject constructor() {
    val githubApiReleasesLink =
        "https://api.github.com/repos/MathewSachin/Fate-Grand-Automata/releases"

    val isDevelopmentBuild get() = !BuildConfig.VERSION_NAME.startsWith('v')
    val currentVersion: VersionInfo
        get() = VersionInfo.getInstance(BuildConfig.VERSION_NAME.substring(1))

    suspend fun getLatestReleaseTag() = withContext(Dispatchers.IO) {
        val releases = URL(githubApiReleasesLink).readText()
        val latestRelease = JSONArray(releases).getJSONObject(0)
        latestRelease.getString("tag_name") ?: ""
    }

    suspend fun check(): UpdateCheckResult {
        return try {
            if (isDevelopmentBuild) {
                return UpdateCheckResult.NotAvailable
            }

            val latestTag = getLatestReleaseTag()

            val latestVersion = VersionInfo
                .getInstance(latestTag.substring(1))

            if (latestVersion > currentVersion) {
                UpdateCheckResult.Available(latestTag)
            } else UpdateCheckResult.NotAvailable
        } catch (e: Exception) {
            UpdateCheckResult.Failed(e)
        }
    }
}