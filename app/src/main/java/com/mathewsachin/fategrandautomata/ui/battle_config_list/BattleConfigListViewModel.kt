package com.mathewsachin.fategrandautomata.ui.battle_config_list

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import timber.log.error
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BattleConfigListViewModel @Inject constructor(
    prefsCore: PrefsCore,
    val prefs: IPreferences
) : ViewModel() {
    val battleConfigItems = prefsCore
        .battleConfigList
        .asFlow()
        .map { list ->
            list
                .map { key -> prefsCore.forBattleConfig(key) }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name.get() })
        }

    var selectedConfigs = MutableStateFlow(emptySet<String>())

    var selectionMode = mutableStateOf(false)

    private fun configsToExport() =
        if (selectionMode.value) {
            selectedConfigs.value.map { prefs.forBattleConfig(it) }
        }
        else prefs.battleConfigs

    fun newConfig(): IBattleConfig {
        val guid = UUID.randomUUID().toString()

        return prefs.addBattleConfig(guid)
    }

    data class ImportExportResult(val failureCount: Int)

    fun exportAsync(dirUri: Uri, context: Context): Deferred<ImportExportResult> =
        viewModelScope.async {
            var failed = 0

            withContext(Dispatchers.IO) {
                val gson = Gson()
                val resolver = context.contentResolver
                val dir = DocumentFile.fromTreeUri(context, dirUri)

                val configs = configsToExport()

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

    fun importAsync(uris: List<Uri>, context: Context): Deferred<ImportExportResult> =
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