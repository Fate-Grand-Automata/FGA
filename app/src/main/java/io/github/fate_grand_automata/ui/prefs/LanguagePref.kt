package io.github.fate_grand_automata.ui.prefs

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.Pref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KProperty

class LanguagePref : Pref<String> {
    companion object {
        @Composable
        fun availableLanguages() = mapOf(
            "en" to stringResource(R.string.language_en),
            "ja" to stringResource(R.string.language_ja),
            "zh-CN" to stringResource(R.string.language_zhCN),
            "zh-TW" to stringResource(R.string.language_zhTW),
            "ko" to stringResource(R.string.language_ko),
            "vi" to stringResource(R.string.language_vi)
        )
    }

    private val locale = MutableStateFlow(get())

    override val defaultValue: String = "en"
    override val key: String = "language"

    override fun resetToDefault() {
        // do nothing
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): String {
        return get()
    }

    override fun asCollector(): FlowCollector<String> {
        TODO("Not yet implemented")
    }

    override fun asFlow(): Flow<String> {
        return locale
    }

    override fun asSyncCollector(throwOnFailure: Boolean): FlowCollector<String> {
        TODO("Not yet implemented")
    }

    override fun delete() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAndCommit(): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(): String {
        return AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag() ?: "en"
    }

    override fun isNotSet(): Boolean {
        return false
    }

    override fun isSet(): Boolean {
        return true
    }

    override suspend fun setAndCommit(value: String): Boolean {
        set(value)
        return true
    }

    override fun set(value: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(value)
        )
        locale.value = value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
        set(value)
    }

}