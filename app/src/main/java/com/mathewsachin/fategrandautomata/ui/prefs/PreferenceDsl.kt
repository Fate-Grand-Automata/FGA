package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.util.makeNumeric

@DslMarker
annotation class PreferenceDsl

@PreferenceDsl
open class PreferenceBuilder {
    @StringRes
    var title: Int? = null

    @StringRes
    var summary: Int? = null

    @DrawableRes
    var icon: Int? = null

    var dependency: Pref<Boolean>? = null

    open fun build(pref: Preference) {
        val context = pref.context

        title?.also {
            pref.title = context.getString(it)
        }

        summary?.also {
            pref.summary = context.getString(it)
        }

        icon?.also {
            pref.icon = ContextCompat.getDrawable(context, it)
        }

        dependency?.also {
            pref.dependency = it.key
        }
    }
}

@PreferenceDsl
class SeekBarPreferenceBuilder : PreferenceBuilder() {
    var max = 100
    var min = 0
    var increment = 1
    var showValue = true

    override fun build(pref: Preference) {
        super.build(pref)

        if (pref is SeekBarPreference) {
            pref.seekBarIncrement = increment
            pref.showSeekBarValue = showValue
        }
    }
}

@PreferenceDsl
class GroupPreferenceBuilder(val group: PreferenceGroup) : PreferenceBuilder() {
    var key: String? = null

    fun Pref<Boolean>.switch(
        block: PreferenceBuilder.() -> Unit
    ) = SwitchPreferenceCompat(group.context).also {
        it.setDefaultValue(defaultValue)
        it.key = key
        group.addPreference(it)

        PreferenceBuilder().apply(block).build(it)
    }

    fun Pref<Int>.seekBar(
        block: SeekBarPreferenceBuilder.() -> Unit
    ) = SeekBarPreference(group.context).also {
        val builder = SeekBarPreferenceBuilder().apply(block)

        it.min = builder.min
        it.max = builder.max
        it.setDefaultValue(defaultValue)
        it.key = key

        group.addPreference(it)

        builder.build(it)
    }

    fun Pref<String>.text(
        block: PreferenceBuilder.() -> Unit
    ) = EditTextPreference(group.context).also {
        it.setDefaultValue(defaultValue)
        it.key = key
        group.addPreference(it)

        it.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        PreferenceBuilder().apply(block).build(it)
        it.dialogTitle = it.title
    }

    fun <T> Pref<T>.list(
        block: PreferenceBuilder.() -> Unit
    ) = ListPreference(group.context).also {
        it.setDefaultValue(defaultValue.toString())
        it.key = key
        group.addPreference(it)

        it.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        PreferenceBuilder().apply(block).build(it)
        it.dialogTitle = it.title
    }

    fun Pref<Int>.numeric(
        block: PreferenceBuilder.() -> Unit
    ) = EditTextPreference(group.context).also {
        it.setDefaultValue(defaultValue.toString())
        it.key = key
        group.addPreference(it)

        it.makeNumeric()
        it.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        PreferenceBuilder().apply(block).build(it)
        it.dialogTitle = it.title
    }

    fun Pref<Set<String>>.multiSelect(
        block: PreferenceBuilder.() -> Unit
    ) = MultiSelectListPreference(group.context).also {
        it.key = key
        group.addPreference(it)

        PreferenceBuilder().apply(block).build(it)
        it.dialogTitle = it.title
    }

    fun category(
        block: GroupPreferenceBuilder.() -> Unit
    ) = PreferenceCategory(group.context).also {
        group.addPreference(it)
        GroupPreferenceBuilder(it).apply(block).build(it)
    }

    fun blank(
        block: PreferenceBuilder.() -> Unit
    ) = Preference(group.context).also {
        group.addPreference(it)

        PreferenceBuilder().apply(block).build(it)
    }

    override fun build(pref: Preference) {
        super.build(pref)

        key?.also {
            pref.key = it
        }
    }
}

fun PreferenceFragmentCompat.prefScreen(block: GroupPreferenceBuilder.() -> Unit) {
    preferenceScreen = preferenceManager.createPreferenceScreen(context)
    GroupPreferenceBuilder(preferenceScreen).apply(block)
}