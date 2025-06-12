package ru.qdev.lnotes.ui.view.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.qdev.lnotes.ui.theme.dp14
import ru.qdev.lnotes.ui.theme.dp40
import ru.qdev.lnotes.ui.theme.dp8
import ru.qdev.lnotes.ui.view.menu.DialogMenuItem
import ru.qdev.lnotes.ui.view.spacer.VSpacer
import ru.qdev.lnotes.ui.view.text.SText

@Composable
fun DialogView (dialog: Dialog,
                onDismiss: () -> Unit,
                onMenuItemClick: (DialogMenuItem) -> Unit,
                onButtonClick: (DialogButton, String) -> Unit) {
    Dialog(
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
                        if (dialog.title.isNotEmpty()) {
                            SText(
                                modifier = Modifier.fillMaxWidth(),
                                text = dialog.title,
                                textAlign = TextAlign.Center
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
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
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

                        dialog.buttons.forEach {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onButtonClick(it, inputV.value)
                                }
                            ) {
                                SText(text = it.title)
                            }

                            VSpacer(dp8)
                        }
                    }
                }
            }
        }
    )
}