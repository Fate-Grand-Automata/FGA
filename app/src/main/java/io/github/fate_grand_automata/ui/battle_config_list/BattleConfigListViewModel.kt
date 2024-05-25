package io.github.fate_grand_automata.ui.battle_config_list

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.enums.BattleConfigListSortEnum
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.util.toggle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BattleConfigListViewModel @Inject constructor(
    prefsCore: PrefsCore,
    val prefs: IPreferences
) : ViewModel() {

    val configListSort = prefsCore
        .configListSort
        .asFlow()


    val battleConfigItems = prefsCore
        .battleConfigList
        .asFlow()
        .map {
            it.map { key -> prefsCore.forBattleConfig(key) }
        }
        .combine(configListSort) { battleConfigs, sort ->
            val nameGetter = { battleConfigCore: BattleConfigCore ->
                battleConfigCore.name.get()
            }
            val usageCountGetter = { battleConfigCore: BattleConfigCore ->
                battleConfigCore.usageCount.get()
            }
            val lastUsageGetter = { battleConfigCore: BattleConfigCore ->
                if (battleConfigCore.usageCount.get() > 0) {
                    battleConfigCore.lastUsage.get().toInstant(TimeZone.UTC).epochSeconds
                } else {
                    0L
                }
            }
            
            sortBattleConfigs(
                battleConfigList = battleConfigs,
                sort = sort,
                nameGetter = nameGetter,
                usageCountGetter = usageCountGetter,
                lastUsageGetter = lastUsageGetter
            )

        }
    
    private fun sortBattleConfigs(
        battleConfigList: List<BattleConfigCore>,
        sort: BattleConfigListSortEnum,
        nameGetter: (BattleConfigCore) -> String,
        usageCountGetter: (BattleConfigCore) -> Int,
        lastUsageGetter: (BattleConfigCore) -> Long
    ): List<BattleConfigCore> {
        val serverIndexGetter = { battleConfigCore: BattleConfigCore ->
            battleConfigCore.server.get().asGameServer()?.let { server ->
                GameServer.values.indexOf(server)
            }
        }

        return when (sort) {
            BattleConfigListSortEnum.DEFAULT_SORT -> battleConfigList.sortedWith(
                compareBy(nullsFirst(), serverIndexGetter)
                    .thenBy(String.CASE_INSENSITIVE_ORDER, nameGetter)
            )

            BattleConfigListSortEnum.SORT_BY_NAME_DESC -> battleConfigList.sortedWith(
                compareByDescending(nullsLast(), serverIndexGetter)
                    .thenByDescending(String.CASE_INSENSITIVE_ORDER, nameGetter)
            )

            BattleConfigListSortEnum.SORT_BY_USAGE_COUNT_ASC -> battleConfigList.sortedWith(
                compareBy(usageCountGetter)
                    .thenBy(nullsFirst(), serverIndexGetter)
                    .thenBy(String.CASE_INSENSITIVE_ORDER, nameGetter)
            )

            BattleConfigListSortEnum.SORT_BY_USAGE_COUNT_DESC -> battleConfigList.sortedWith(
                compareByDescending(usageCountGetter)
                    .thenBy(nullsLast(), serverIndexGetter)
                    .thenBy(String.CASE_INSENSITIVE_ORDER, nameGetter)
            )

            BattleConfigListSortEnum.SORT_BY_LAST_USAGE_TIME_ASC -> battleConfigList.sortedWith(
                compareBy(lastUsageGetter)
                    .thenBy(nullsFirst(), serverIndexGetter)
                    .thenBy(String.CASE_INSENSITIVE_ORDER, nameGetter)
            )

            BattleConfigListSortEnum.SORT_BY_LAST_USAGE_TIME_DESC -> battleConfigList.sortedWith(
                compareByDescending(lastUsageGetter)
                    .thenBy(nullsLast(), serverIndexGetter)
                    .thenBy(String.CASE_INSENSITIVE_ORDER, nameGetter)
            )
        }
    }
    
    fun setConfigListSort(sort: BattleConfigListSortEnum) {
        viewModelScope.launch {
            prefs.configListSort = sort
        }
    }


    private val _selectedConfigs = MutableStateFlow(emptySet<String>())
    val selectedConfigs: StateFlow<Set<String>> = _selectedConfigs

    fun toggleSelected(id: String) {
        _selectedConfigs.value = selectedConfigs.value.toggle(id)

        if (selectedConfigs.value.isEmpty()) {
            endSelection()
        }
    }

    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode

    fun startSelection(id: String) {
        _selectedConfigs.value = setOf(id)
        _selectionMode.value = true
    }

    fun endSelection() {
        _selectionMode.value = false
    }

    private fun configsToExport() =
        if (selectionMode.value) {
            selectedConfigs.value.map { prefs.forBattleConfig(it) }
        } else prefs.battleConfigs

    fun newConfig(): IBattleConfig {
        val guid = UUID.randomUUID().toString()

        return prefs.addBattleConfig(guid)
    }

    data class ImportExportResult(val failureCount: Int)

    private suspend fun exportAsync(dirUri: Uri, context: Context): ImportExportResult {
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
                    Timber.e(e, "Failed to export")
                    ++failed
                }
            }
        }

        return ImportExportResult(failed)
    }

    fun exportBattleConfigs(context: Context, dirUri: Uri?) {
        if (dirUri != null) {
            viewModelScope.launch {
                val result = exportAsync(dirUri, context)

                if (result.failureCount > 0) {
                    val msg = context.getString(R.string.battle_config_list_export_failed, result.failureCount)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun importAsync(uris: List<Uri>, context: Context): ImportExportResult {
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
                    Timber.e(e, "Import Failed")
                }
            }
        }

        return ImportExportResult(failed)
    }

    fun importBattleConfigs(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            val result = importAsync(uris, context)

            if (result.failureCount > 0) {
                val msg = context.getString(R.string.battle_config_list_import_failed, result.failureCount)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun deleteSelected() {
        selectedConfigs.value.forEach {
            prefs.removeBattleConfig(it)
        }

        endSelection()
    }
}