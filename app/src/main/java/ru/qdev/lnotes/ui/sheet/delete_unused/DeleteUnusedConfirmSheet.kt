package ru.qdev.lnotes.ui.sheet.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import ru.qdev.lnotes.ui.sheet.delete_unused.DeleteUnusedConfirmSheetController
import ru.qdev.lnotes.ui.sheet.delete_unused.DeleteUnusedConfirmSheetController.Companion.makeDefStatuses
import ru.qdev.lnotes.ui.sheet.delete_unused.DeleteUnusedConfirmSheetControllerListener
import ru.qdev.lnotes.ui.sheet.delete_unused.model.SelectedStatus
import ru.qdev.lnotes.ui.theme.dp1
import ru.qdev.lnotes.ui.theme.dp14
import ru.qdev.lnotes.ui.theme.dp40
import ru.qdev.lnotes.ui.theme.dp8
import ru.qdev.lnotes.ui.theme.sp16
import ru.qdev.lnotes.ui.view.button.MainButtonContent
import ru.qdev.lnotes.ui.view.button.SButton
import ru.qdev.lnotes.ui.view.spacer.HSpacer
import ru.qdev.lnotes.ui.view.spacer.VSpacer
import ru.qdev.lnotes.ui.view.text.SText
import ru.qdev.lnotes.ui.view.text.STextField
import ru.qdev.lnotes.ui.view.text.TextFieldBorderedModifier
import src.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteUnusedConfirmSheet(controller: DeleteUnusedConfirmSheetController) {
    val sheetState = rememberModalBottomSheetState()

    val isVisible = controller.isShowedS.value
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                controller.hide()
            },
            sheetState = sheetState
        ) {
            Content(
                listener = controller,
                statuses = controller.statusListS,
                monthInputValue = controller.monthInputValueS.value
            )
        }
    }
}

@Composable
private fun Content(listener: DeleteUnusedConfirmSheetControllerListener?,
                    statuses: List<SelectedStatus>,
                    monthInputValue: TextFieldValue) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(dp14),
        horizontalAlignment = Alignment.Start
    ) {
        SText(
            text = stringResource(R.string.clearing_outdated_title),
            fontSize = sp16
        )
        VSpacer(dp14)

        SText(
            text = stringResource(R.string.statuses_select),
            fontWeight = W600
        )
        VSpacer(dp8)

        statuses.forEachIndexed { index, status ->
            if (index > 0) {
                HorizontalDivider()
            }

            Row(modifier = Modifier
                .heightIn(min = dp40)
                .clip(RoundedCornerShape(dp8))
                .clickable {
                    listener?.onStatusClick(status)
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = status.isSelected,
                    onCheckedChange = {
                        listener?.onStatusClick(status)
                    }
                )
                HSpacer(dp1)
                SText(
                    text = status.status.getTitle(context),
                )
            }
        }

        VSpacer(dp14)

        SText(
            text = stringResource(R.string.month_count_for_delete_notes_title),
            fontWeight = W600
        )
        VSpacer(dp8)

        STextField(
            modifier = TextFieldBorderedModifier(
                Modifier
            ),
            value = monthInputValue,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            onValueChange = {
                listener?.onMonthValueChange(it)
            },
        )

        VSpacer(dp14)

        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
            horizontalArrangement = Arrangement.End
        ) {
            SButton(
                onClick = {
                    listener?.onCancelClick()
                },
                content = MainButtonContent(stringResource(R.string.cancel))
            )
            HSpacer(dp14)

            SButton(
                onClick = {
                    listener?.onConfirmClick()
                },
                content = MainButtonContent(
                    stringResource(R.string.show_and_select_records)
                )
            )
        }

        HSpacer(dp14)
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Content(
        listener = null,
        statuses = makeDefStatuses(),
        monthInputValue = TextFieldValue("36")
    )
}