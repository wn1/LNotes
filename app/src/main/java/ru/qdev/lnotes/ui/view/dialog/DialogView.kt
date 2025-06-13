package ru.qdev.lnotes.ui.view.dialog

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.reply.ui.theme.AppTheme
import ru.qdev.lnotes.ui.theme.contentHPaddingDp
import ru.qdev.lnotes.ui.theme.dp14
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
                        modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        VSpacer(dp8)

                        if (dialog.title.isNotEmpty()) {
                            SText(
                                modifier = Modifier.fillMaxWidth(),
                                text = dialog.title,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = sp16
                            )
                        }
                        if (dialog.message.isNotEmpty()) {
                            SText(
                                modifier = Modifier.fillMaxWidth(),
                                text = dialog.message,
                                textAlign = TextAlign.Center
                            )
                        }

                        VSpacer(dp8)

                        dialog.menuList.forEach {
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = dp40)
                                    .clickable {
                                        onMenuItemClick(it)
                                    }
                                    .padding(dp14),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SText(
                                    color = MaterialTheme.colorScheme.secondary,
                                    text = it.title
                                )
                            }
                        }
                    }
                }

                DialogType.InputText, DialogType.Dialog -> {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .width(intrinsicSize = IntrinsicSize.Max),
                    ) {
                        VSpacer(dp8)

                        if (dialog.title.isNotEmpty()) {
                            SText(
                                modifier = Modifier.fillMaxWidth(),
                                text = dialog.title,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = sp16
                            )
                        }
                        if (dialog.message.isNotEmpty()) {
                            SText(
                                modifier = Modifier.fillMaxWidth(),
                                text = dialog.message,
                                textAlign = TextAlign.Center
                            )
                        }

                        VSpacer(dp8)

                        val inputV = remember { mutableStateOf("") }
                        if (dialog.dialogType == DialogType.InputText) {
                            STextField(
                                modifier = TextFieldBorderedModifier(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = contentHPaddingDp)
                                ),
                                value = inputV.value,
                                onValueChange = {
                                    val str = if (dialog.inputMaxLen != null) {
                                        it.take(dialog.inputMaxLen)
                                    } else {
                                        it
                                    }

                                    inputV.value = str
                                },
                            )

                            VSpacer(dp8)
                        }

                        DialogButtons(
                            dialog = dialog,
                            inputText = inputV.value,
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