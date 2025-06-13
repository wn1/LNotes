package ru.qdev.lnotes.ui.view.dialog

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.reply.ui.theme.AppTheme
import ru.qdev.lnotes.ui.theme.contentHPaddingDp
import ru.qdev.lnotes.ui.theme.dp10
import ru.qdev.lnotes.ui.theme.dp14
import ru.qdev.lnotes.ui.theme.dp4
import ru.qdev.lnotes.ui.theme.dp40
import ru.qdev.lnotes.ui.theme.dp8
import ru.qdev.lnotes.ui.theme.sp16
import ru.qdev.lnotes.ui.view.button.MainButtonContent
import ru.qdev.lnotes.ui.view.button.SButton
import ru.qdev.lnotes.ui.view.menu.DialogMenuItem
import ru.qdev.lnotes.ui.view.spacer.HSpacer
import ru.qdev.lnotes.ui.view.spacer.HSpacerWeight
import ru.qdev.lnotes.ui.view.spacer.VSpacer
import ru.qdev.lnotes.ui.view.text.SText
import ru.qdev.lnotes.ui.view.text.STextField
import ru.qdev.lnotes.ui.view.text.TextFieldBorderedModifier

@Composable
fun DialogView (dialog: Dialog,
                onDismiss: () -> Unit,
                onMenuItemClick: (DialogMenuItem) -> Unit,
                onButtonClick: (DialogButton, String) -> Unit) {
    Dialog (
        onDismissRequest = {
            onDismiss()
        },
        properties = DialogProperties(),
        content = {
            when (dialog.dialogType) {
                DialogType.Menu -> {
                    Column(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(dp10)
                            )
                            .verticalScroll(rememberScrollState())
                    ) {
                        VSpacer(dp8)

                        DialogHeader(dialog)

                        dialog.menuList.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = dp40)
                                    .clickable {
                                        onMenuItemClick(item)
                                    }
                                    .padding(dp14),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VSpacer(dp4)
                                SText(
                                    color = MaterialTheme.colorScheme.secondary,
                                    text = item.title
                                )
                                VSpacer(dp4)
                            }
                        }
                    }
                }

                DialogType.InputText, DialogType.Dialog -> {
                    Column(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(dp10)
                            )
                            .width(intrinsicSize = IntrinsicSize.Max)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        VSpacer(dp8)

                        DialogHeader(dialog)

                        val textV = dialog.inputText ?: ""
                        val inputV = remember { mutableStateOf(
                            TextFieldValue(
                                textV,
                                selection = TextRange(textV.length)
                            )
                        )}
                        if (dialog.dialogType == DialogType.InputText) {
                            val focusRequester = remember { FocusRequester() }

                            STextField(
                                modifier = TextFieldBorderedModifier(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = contentHPaddingDp)
                                        .focusRequester(focusRequester)
                                ),
                                value = inputV.value,
                                onValueChange = {
                                    val str = if (dialog.inputMaxLen != null) {
                                        it.text.take(dialog.inputMaxLen)
                                    } else {
                                        it.text
                                    }

                                    var start = it.selection.start
                                    var end = it.selection.end
                                    if (start > str.length) start = str.length
                                    if (end > str.length) end = str.length

                                    val selection = TextRange(start = start, end= end)
                                    inputV.value = it.copy(text = str, selection = selection)
                                },
                            )

                            LaunchedEffect ("Input text"){
                                focusRequester.requestFocus()
                            }

                            VSpacer(dp8)
                        }

                        DialogButtons(
                            dialog = dialog,
                            inputText = inputV.value.text,
                            onButtonClick = { button, inputText ->
                                onButtonClick(button, inputText)
                            }
                        )

                        VSpacer(dp8)
                    }
                }
            }
        }
    )
}

@Composable
private fun DialogHeader(dialog: Dialog) {
    var withText = false
    Column {
        if (dialog.title.isNotEmpty()) {
            withText = true
            SText(
                modifier = Modifier.fillMaxWidth().padding(horizontal = contentHPaddingDp),
                text = dialog.title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = sp16
            )
        }
        if (dialog.message.isNotEmpty()) {
            withText = true
            SText(
                modifier = Modifier.fillMaxWidth().padding(horizontal = contentHPaddingDp),
                text = dialog.message,
                textAlign = TextAlign.Center,
                maxLines = dialog.messageMaxLines,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (withText) {
            VSpacer(dp8)
        }
    }
}

@Composable
private fun DialogButtons(dialog: Dialog,
                          inputText: String,
                          onButtonClick: (DialogButton, String) -> Unit) {
    when (dialog.dialogButtonLayoutType) {
        DialogButtonLayoutType.Vertical -> {
            Column {
                dialog.buttons.forEachIndexed { index, button ->
                    if (index > 0) VSpacer(dp8)

                    SButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentHPaddingDp),
                        onClick = {
                            onButtonClick(button, inputText)
                        },
                        content = MainButtonContent(button.title)
                    )
                }
            }
        }

        DialogButtonLayoutType.Horizontal -> {
            Row (
                modifier = Modifier
                    .padding(horizontal = contentHPaddingDp),
            ){
                dialog.buttons.forEachIndexed { index, button ->
                    if (index > 0) {
                        HSpacer(dp8)
                    }
                    else {
                        HSpacerWeight()
                    }

                    SButton(
                        modifier = Modifier,
                        onClick = {
                            onButtonClick(button, inputText)
                        },
                        content = MainButtonContent(button.title)
                    )
                }
            }
        }
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
private fun DialogViewPreviewInputText() {
    val context = LocalContext.current
    AppTheme {
        DialogView(
            dialog = Dialog.makeTestInputText(context),
            onDismiss = {

            },
            onButtonClick = { dialogButton, input ->

            },
            onMenuItemClick = {

            }
        )
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
private fun DialogViewPreview() {
    val context = LocalContext.current
    AppTheme {
        DialogView(
            dialog = Dialog.makeDialogLongTextTest(context),
            onDismiss = {

            },
            onButtonClick = { dialogButton, input ->

            },
            onMenuItemClick = {

            }
        )
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
private fun DialogViewPreviewMenu() {
    val context = LocalContext.current
    AppTheme {
        DialogView(
            dialog = Dialog.makeMenuTest(context),
            onDismiss = {

            },
            onButtonClick = { dialogButton, input ->

            },
            onMenuItemClick = {

            }
        )
    }
}