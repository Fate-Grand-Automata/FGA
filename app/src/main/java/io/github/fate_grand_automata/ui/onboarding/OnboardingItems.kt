package io.github.fate_grand_automata.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.BuildConfig
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.openLinkIntent
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.OpenDocTreePersistable
import io.github.fate_grand_automata.util.SupportImageExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

class PickDirectory(vm: OnboardingViewModel) : OnboardingItem(vm) {
    override fun shouldSkip(): Boolean {
        return vm.prefsCore.dirRoot.get().isNotBlank()
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
                        SupportImageExtractor(context, vm.storageProvider).extract()
                    }
                }

                onFinished()
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

class SkillConfirmation(vm: OnboardingViewModel) : OnboardingItem(vm, true) {
    override fun shouldSkip(): Boolean {
        // only show on first installation
        return vm.prefsCore.onboardingCompletedVersion.get() > 0
    }

    @Composable
    override fun UI(onFinished: () -> Unit) {
        Heading(stringResource(R.string.p_skill_confirmation))

        Text(
            text = stringResource(R.string.p_skill_confirmation_onboarding_description),
            style = MaterialTheme.typography.bodyLarge
        )

        var state by vm.prefsCore.skillConfirmation.remember()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(vertical = 15.dp)
        ) {
            Text(stringResource(R.string.p_off))
            Switch(
                checked = state,
                onCheckedChange = { state = it }
            )
            Text(stringResource(R.string.p_on))
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

        OutlinedButton(
            onClick = {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
                onFinished()
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