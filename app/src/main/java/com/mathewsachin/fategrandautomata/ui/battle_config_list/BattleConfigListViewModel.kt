package com.mathewsachin.fategrandautomata.ui.battle_config_list

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import timber.log.error
import java.util.*

class BattleConfigListViewModel @ViewModelInject constructor(
    prefsCore: PrefsCore,
    val prefs: IPreferences,
    @ApplicationContext val context: Context
) : ViewModel() {
    val battleConfigItems = prefsCore
        .battleConfigList
        .asFlow()
        .map { prefs.battleConfigs }
        .asLiveData()

    fun newConfig(): IBattleConfig {
        val guid = UUID.randomUUID().toString()

        return prefs.addBattleConfig(guid)
    }

    data class ImportExportResult(val failureCount: Int)

    fun exportAsync(dirUri: Uri, configs: List<IBattleConfig>): Deferred<ImportExportResult> =
        viewModelScope.async {
            var failed = 0

            withContext(Dispatchers.IO) {
                val gson = Gson()
                val resolver = context.contentResolver
                val dir = DocumentFile.fromTreeUri(context, dirUri)

                configs.forEach { battleConfig ->
                    val values = battleConfig.export()
                    val json = gson.toJson(values)

                    try {
                        dir?.createFile("*/*", "${battleConfig.name}.fga")
                            ?.uri
                            ?.let { uri ->
                                resolver.openOutputStream(uri)?.use { outStream ->
                                    outStream.writer().use { it.write(json) }
                                }
                            }
                    } catch (e: Exception) {
                        Timber.error(e) { "Failed to export" }
                        ++failed
                    }
                }
            }

            ImportExportResult(failed)
        }

    fun importAsync(uris: List<Uri>): Deferred<ImportExportResult> =
        viewModelScope.async {
            var failed = 0

            withContext(Dispatchers.IO) {
                val gson = Gson()

                uris.forEach { uri ->
                    try {
                        val json = context.contentResolver.openInputStream(uri)?.use { inStream ->
                            inStream.use {
                                it.reader().readText()
                            }
                        }

                        if (json != null) {
                            val map = gson.fromJson(json, Map::class.java)
                                .map { (k, v) -> k.toString() to v }
                                .toMap()

                            newConfig().import(map)
                        }
                    } catch (e: Exception) {
                        ++failed
                        Timber.error(e) { "Import Failed" }
                    }
                }
            }

            ImportExportResult(failed)
        }
}