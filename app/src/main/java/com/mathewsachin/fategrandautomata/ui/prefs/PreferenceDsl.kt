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
    var max: Int? = null
    var min = 0
    var increment = 1
    var showValue = true

    override fun build(pref: Preference) {
        super.build(pref)

        if (pref is SeekBarPreference) {
            pref.min = min
            max?.also { pref.max = it }
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
        it.key = key
        group.addPreference(it)

        it.setDefaultValue(defaultValue)
        PreferenceBuilder().apply(block).build(it)
    }

    fun Pref<Int>.seekBar(
        block: SeekBarPreferenceBuilder.() -> Unit
    ) = SeekBarPreference(group.context).also {
        it.key = key
        group.addPreference(it)

        it.setDefaultValue(defaultValue)
        SeekBarPreferenceBuilder().apply(block).build(it)
    }

    fun Pref<Int>.numeric(
        block: PreferenceBuilder.() -> Unit
    ) = EditTextPreference(group.context).also {
        it.key = key
        group.addPreference(it)

        it.setDefaultValue(defaultValue.toString())
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