package io.github.fate_grand_automata.ui.support_img_namer

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.net.toUri
import coil.compose.AsyncImage
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.dialog.ThemedDialog
import io.github.fate_grand_automata.ui.padding
import io.github.fate_grand_automata.util.StorageProvider
import timber.log.Timber
import java.io.File
import java.io.IOException

class SupportImgEntry(
    val imgPath: File,
    val kind: SupportImageKind,
    val index: Int = 0,
    val options: List<String> = emptyList(),
) {

    companion object {
        // *, ?, \, |, / are special characters in Regex and need to be escaped using \
        private const val INVALID_CHARS = """<>"\|:\*\?\\\/"""
        private const val FILE_NAME_REGEX = """[^\.\s$INVALID_CHARS][^$INVALID_CHARS]*"""

        val regex = Regex("""$FILE_NAME_REGEX(/$FILE_NAME_REGEX)?""")
    }

    private var isCheckBoxChecked by mutableStateOf(false)

    val checkBoxStatus
        get() = isCheckBoxChecked

    private var newFileNameValue by mutableStateOf("")

    private var currentServantName by mutableStateOf("")

    private var currentImageName by mutableStateOf("")

    private var onErrorDialog by mutableStateOf(false)

    private var onErrorDialogText by mutableStateOf("")

    private var servantName
        get() = currentServantName
        set(value) {
            currentServantName = value
            if (kind == SupportImageKind.Servant) {
                newFileNameValue = "$currentServantName/$currentImageName"
            }
        }

    private var servantImageName
        get() = currentImageName
        set(value) {
            currentImageName = value
            if (kind == SupportImageKind.Servant) {
                newFileNameValue = "$currentServantName/$currentImageName"
            }
        }


    @Composable
    fun Entry() {
        if (imgPath.exists()) {
            if (kind == SupportImageKind.Servant) {
                ServantSupportEntry()
            } else {
                supportEntry()
            }

            if (onErrorDialog) {
                ErrorDialog()
            }
        }
    }

    @Composable
    private fun ErrorDialog() {
        ThemedDialog(
            onDismiss = {
                hideDialog()
            },
        ) {
            Text(
                text = onErrorDialogText
            )
        }
    }

    private fun showDialog() {
        onErrorDialog = true
    }

    private fun hideDialog() {
        onErrorDialog = false
    }

    private fun checkIfNameError(
        value: String = newFileNameValue
    ): Boolean {
        if (!isCheckBoxChecked) return false

        return if (value.isEmpty()) {
            true
        } else {
            !regex.containsMatchIn(value)
        }
    }

    @Composable
    private fun NameErrorTextBox(
        value: String = newFileNameValue
    ) {
        if (!isCheckBoxChecked) return

        when {
            value.isEmpty() -> {
                Text(
                    text = stringResource(id = R.string.support_img_namer_text_blank),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            !regex.containsMatchIn(value) -> {
                Text(
                    text = stringResource(id = R.string.support_img_namer_invalid_name),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    @Composable
    private fun ServantSupportEntry() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.padding.small,
                    vertical = MaterialTheme.padding.medium
                ),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        isCheckBoxChecked = !isCheckBoxChecked
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isCheckBoxChecked,
                    onCheckedChange = {
                        isCheckBoxChecked = it
                    }
                )
                AsyncImage(
                    model = imgPath.toUri(),
                    contentDescription = null,
                    alpha = if (isCheckBoxChecked) 1f else 0.4f,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.heightIn(
                        min = 40.dp
                    )
                )
            }
            CustomTextField(
                modifier = Modifier.fillMaxWidth(),
                value = servantName,
                placeHolderText = stringResource(id = R.string.p_support_image_maker_servant),
                options = options,
                onValueChange = {
                    servantName = it
                }
            )
            CustomTextField(
                modifier = Modifier.fillMaxWidth(),
                value = servantImageName,
                placeHolderText = stringResource(id = R.string.p_support_image_maker_servant_ascension),
                enableOptions = false,
                onValueChange = {
                    servantImageName = it
                }
            )
        }
    }

    @Composable
    private fun CustomTextField(
        modifier: Modifier = Modifier,
        placeHolderText: String,
        options: List<String> = emptyList(),
        enableOptions: Boolean = true,
        value: String,
        onValueChange: (String) -> Unit
    ) {
        val leadingIcon: @Composable() (() -> Unit)? = if (value.isNotEmpty()) {
            {
                AnimatedVisibility(
                    visible = value.isNotEmpty(),
                    enter = fadeIn() + expandHorizontally { fullSize -> fullSize },
                    exit = fadeOut() + shrinkVertically { fullSize -> -fullSize }
                ) {
                    IconButton(
                        onClick = {
                            onValueChange("")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            }
        } else {
            null
        }

        var expanded by remember {
            mutableStateOf(false)
        }
        val rotate by animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            label = "Rotation of the arrow"
        )
        val (focusRequester) = FocusRequester.createRefs()
        val focusManager = LocalFocusManager.current

        val trailingIcon: @Composable() (() -> Unit)? = if (enableOptions) {
            {
                IconButton(
                    onClick = {
                        expanded = !expanded
                        if (expanded) {
                            focusRequester.requestFocus()
                        }
                    },
                    enabled = isCheckBoxChecked
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.rotate(
                            degrees = rotate
                        )
                    )
                }
            }
        } else {
            null
        }

        var filteredOptions by remember {
            mutableStateOf(options)
        }

        LaunchedEffect(key1 = value) {
            filteredOptions = options
                .filter { option ->
                    option.contains(value, ignoreCase = true)
                }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            },
            modifier = modifier,
        ) {
            TextField(
                value = value,
                onValueChange = {
                    expanded = true
                    onValueChange(it)
                },
                enabled = isCheckBoxChecked,
                leadingIcon = leadingIcon,
                readOnly = false,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        expanded = state.isFocused
                    }
                    .menuAnchor(),
                placeholder = {
                    Text(
                        text = placeHolderText
                    )
                },
                trailingIcon = trailingIcon,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                    disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                    errorContainerColor = when (value.isEmpty()) {
                        true -> MaterialTheme.colorScheme.background
                        false -> MaterialTheme.colorScheme.error
                    },
                    errorTextColor = MaterialTheme.colorScheme.onError,
                    errorLeadingIconColor = MaterialTheme.colorScheme.onError,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                ),
                isError = checkIfNameError(value = value),
                supportingText = {
                    NameErrorTextBox(value = value)
                }
            )

            if (filteredOptions.isNotEmpty()) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                        focusManager.clearFocus()
                    },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier
                        .exposedDropdownSize(true)
                ) {
                    filteredOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(option)
                            },
                            onClick = {
                                expanded = false
                                focusManager.clearFocus()
                                onValueChange(option)
                            }
                        )
                    }
                }
            }
        }

    }

    @Composable
    private fun supportEntry() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.padding.small,
                    vertical = MaterialTheme.padding.medium
                ),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        isCheckBoxChecked = !isCheckBoxChecked
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isCheckBoxChecked,
                    onCheckedChange = {
                        isCheckBoxChecked = it
                    }
                )
                AsyncImage(
                    model = imgPath.toUri(),
                    contentDescription = null,
                    alpha = if (isCheckBoxChecked) 1f else 0.4f,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.heightIn(
                        min = 40.dp
                    )
                )
            }
            CustomTextField(
                placeHolderText = when (kind) {
                    SupportImageKind.Servant -> stringResource(id = R.string.p_support_image_maker_servant)
                    SupportImageKind.CE -> stringResource(id = R.string.p_support_image_maker_ce)
                    SupportImageKind.Friend -> stringResource(id = R.string.p_support_image_maker_friend)
                },
                value = newFileNameValue,
                onValueChange = {
                    newFileNameValue = it
                },
                options = options,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }


    fun isValid(): Boolean {
        if (!isCheckBoxChecked) return false


        val oldPath = imgPath
        val newFileName = newFileNameValue

        // Either the file was deleted or not generated in the first place.
        if (!oldPath.exists()) return false

        if (newFileName.isBlank()) return false


//        if (regex.matches(newFileName)) {
//            return false
//        }

        return true
    }

    fun rename(storageProvider: StorageProvider): Boolean {
        if (!isCheckBoxChecked) return false


        val oldPath = imgPath
        val newFileName = newFileNameValue

        // Either the file was deleted or not generated in the first place.
        if (!oldPath.exists()) return false


        return try {
            storageProvider.writeSupportImage(kind, "$newFileName.png").use { outStream ->
                oldPath.inputStream().use { inStream ->
                    inStream.copyTo(outStream)
                }
            }
            oldPath.delete()
        } catch (e: IOException) {
            Timber.e(e)
            onErrorDialogText = "$e"
            showDialog()
            false
        } catch (e: SecurityException) {
            Timber.e(e)
            onErrorDialogText = "$e"
            showDialog()
            false
        } catch (e: Exception) {
            Timber.e(e)
            onErrorDialogText = "$e"
            showDialog()
            false
        }
    }

}


@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewSupportEntry() {
    var servantFile by remember {
        mutableStateOf(File(""))
    }
    var ceFile by remember {
        mutableStateOf(File(""))
    }
    var output by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        kotlin.runCatching {
            val items = listOf(
                SupportImageKind.Servant to "Support/servant/Artoria (Caster)/Artoria Caster1.png",
                SupportImageKind.CE to "Support/ce/Kaleidoscope.png"
            )
            items.forEachIndexed { index, (kind, path) ->
                val inputStream = context.assets.open(path)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                val tempFile = File(context.cacheDir, "$index.png")
                tempFile.writeBytes(buffer)
                when (kind) {
                    SupportImageKind.Servant -> servantFile = tempFile
                    SupportImageKind.CE -> ceFile = tempFile
                    else -> {
                        //
                    }
                }
            }
        }.onSuccess {
            Timber.d("Success")
        }.onFailure {
            output += "Failed"
        }
    }
    FGATheme {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            if (output.isNotEmpty()) {
                Text(
                    text = output
                )
            }
            SupportImgEntry(
                imgPath = servantFile,
                kind = SupportImageKind.Servant,
                options = (0..5).map { "Servant $it" },
            ).Entry()

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp)
            )

            SupportImgEntry(
                imgPath = ceFile,
                kind = SupportImageKind.CE,
                options = (0..5).map { "CE $it" }
            ).Entry()
        }
    }
}
