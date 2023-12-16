package io.github.fate_grand_automata.ui.prefs

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import io.github.fate_grand_automata.prefs.core.Pref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.reflect.KProperty

class LanguagePref : Pref<String> {
    override val defaultValue: String = get()
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
        return flow {
            get()
        }
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
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
        set(value)
    }

}