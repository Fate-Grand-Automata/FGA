package io.github.fate_grand_automata.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import io.github.fate_grand_automata.BuildConfig
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.openLinkIntent
import io.github.fate_grand_automata.ui.prefs.LanguagePref
import io.github.fate_grand_automata.util.OpenDocTreePersistable
import io.github.fate_grand_automata.util.SupportImageExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

abstract class OnboardingItem(val vm: OnboardingViewModel, val canSkip: Boolean = false) {
    abstract fun shouldSkip(): Boolean

    @Composable
    abstract fun UI(onFinished: () -> Unit)
}

class WelcomeScreen(vm: OnboardingViewModel) : OnboardingItem(vm, true) {
    override fun shouldSkip() = false

    @Composable
    override fun UI(onFinished: () -> Unit) {
        Heading(stringResource(R.string.p_welcome))

        Text(
            text = stringResource(R.string.p_welcome_description),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

class PickLanguage(vm: OnboardingViewModel) : OnboardingItem(vm, true) {
    override fun shouldSkip() = vm.prefsCore.onboardingCompletedVersion.get() > 1

    @Composable
    override fun UI(onFinished: () -> Unit) {
        Heading(text = stringResource(R.string.p_choose_app_language))

        LocaleDropdownMenu()
    }
}

@Composable
fun LocaleDropdownMenu() {

    val locales = LanguagePref.availableLanguages()
        .mapKeys { Locale.forLanguageTag(it.key) }

    // boilerplate: https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#ExposedDropdownMenuBox(kotlin.Boolean,kotlin.Function1,androidx.compose.ui.Modifier,kotlin.Function1)
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        val selectedLocales = AppCompatDelegate.getApplicationLocales()
        val locale: String = if (!selectedLocales.isEmpty) {
            locales[selectedLocales.get(0)!!]!!
        } else {
            val currentLocale = Locale.getDefault()
            locales.filterKeys {
                // set correct default if system language matches one of the offered languages
                it.language == currentLocale.language && (it.country.isEmpty() || it.country == currentLocale.country)
            }.values.firstOrNull() ?: locales.values.first()
        }
        TextField(
            readOnly = true,
            value = locale,
            onValueChange = { },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            locales.forEach { selectionLocale ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // set app locale given the user's selected locale
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(
                                selectionLocale.key.toLanguageTag()
                            )
                        )
                    },
                    text = { Text(selectionLocale.value) }
                )
            }
        }
    }
}

class PickDirectory(vm: OnboardingViewModel) : OnboardingItem(vm) {
    override fun shouldSkip(): Boolean {
        return vm.prefsCore.dirRoot.get().isNotBlank() && !vm.storageProvider.shouldExtractSupportImages
    }

    @Composable
    override fun UI(onFinished: () -> Unit) {
        Heading(text = stringResource(R.string.p_choose_folder_title))

        Text(
            text = stringResource(R.string.p_choose_folder_message),
            style = MaterialTheme.typography.bodyLarge
        )

        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val dirPicker = rememberLauncherForActivityResult(OpenDocTreePersistable()) {
            if (it != null) {
                vm.storageProvider.setRoot(it)
                scope.launch(Dispatchers.IO) {
                    if (vm.storageProvider.shouldExtractSupportImages) {
                        scope.launch(Dispatchers.Main) {
                            // Toast needs to happen in the UI thread
                            val msg = context.getString(R.string.support_imgs_extracting)
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                        SupportImageExtractor(context, vm.storageProvider).extract()
                    }
                    onFinished()
                }
            }
        }
        OutlinedButton(
            onClick = { dirPicker.launch(Uri.EMPTY) },
            modifier = Modifier.padding(vertical = 15.dp)
        ) {
            Text(
                text = stringResource(R.string.p_choose_folder_action),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

class DisableBatteryOptimization(vm: OnboardingViewModel) : OnboardingItem(vm) {
    override fun shouldSkip(): Boolean =
        vm.powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)

    @Composable
    override fun UI(onFinished: () -> Unit) {
        Heading(text = stringResource(R.string.p_battery_optimization))

        Text(
            text = stringResource(R.string.p_battery_optimization_description),
            style = MaterialTheme.typography.bodyLarge
        )

        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onFinished()
        }

        OutlinedButton(
            onClick = {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                launcher.launch(intent)
            },
            modifier = Modifier.padding(vertical = 15.dp)
        ) {
            Text(
                text = stringResource(R.string.p_battery_optimization_action),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        HighlightedText(
            text = String.format(
                stringResource(R.string.p_battery_optimization_dontkillmyapp),
                stringResource(R.string.link_dontkillmyapp)
            ),
            highlights = listOf(
                Highlight(
                    text = "dontkillmyapp.com",
                    data = stringResource(R.string.link_dontkillmyapp),
                    onClick = { link ->
                        context.openLinkIntent(link)
                    }
                )
            ),
            style = MaterialTheme.typography.bodyLarge
        )
    }

}

class YoutubeVideo(vm: OnboardingViewModel) : OnboardingItem(vm, true) {
    override fun shouldSkip(): Boolean {
        // only show on first installation
        return vm.prefsCore.onboardingCompletedVersion.get() > 0
    }

    @Composable
    override fun UI(onFinished: () -> Unit) {
        Heading(text = stringResource(R.string.p_youtube_guide))

        Text(
            text = stringResource(R.string.p_youtube_guide_description),
            style = MaterialTheme.typography.bodyLarge
        )

        val context = LocalContext.current

        OutlinedButton(
            onClick = {
                context.openLinkIntent(R.string.link_youtube)
            },
            modifier = Modifier.padding(vertical = 15.dp)
        ) {
            Text(
                text = stringResource(R.string.p_youtube_guide_action),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

}