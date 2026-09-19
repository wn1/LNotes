package ru.qdev.lnotes.ui.sheet.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.reply.ui.theme.linkColor
import ru.qdev.lnotes.ui.sheet.tips.TipsSheetController
import ru.qdev.lnotes.ui.sheet.tips.TipsSheetControllerListener
import ru.qdev.lnotes.ui.theme.contentHPaddingDp
import ru.qdev.lnotes.ui.theme.dp14
import ru.qdev.lnotes.ui.theme.dp8
import ru.qdev.lnotes.ui.theme.sp16
import ru.qdev.lnotes.ui.view.button.MainButtonContent
import ru.qdev.lnotes.ui.view.button.SButton
import ru.qdev.lnotes.ui.view.spacer.HSpacer
import ru.qdev.lnotes.ui.view.spacer.VSpacer
import ru.qdev.lnotes.ui.view.text.SText
import src.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsSheet(controller: TipsSheetController) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

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
                init = controller.initS.value,
            )
        }
    }
}

@Composable
private fun Content(listener: TipsSheetControllerListener?,
                    init: TipsSheetController.InitData?) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dp14)
            .padding(bottom = dp14),
        horizontalAlignment = Alignment.Start
    ) {
        SText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.help_the_project_title),
            fontSize = sp16,
            fontWeight = W600
        )
        VSpacer(dp8)

        SText(
            modifier = Modifier.padding(horizontal = contentHPaddingDp),
            text = stringResource(R.string.help_the_project_title_desc),
            fontWeight = W500
        )

        VSpacer(dp8)

        SText(
            modifier = Modifier.align(Alignment.CenterHorizontally).clickable {
                listener?.onTipsLinkClick()
            },
            text = stringResource(R.string.tips_link),
            fontWeight = W600,
            color = linkColor()
        )
//        VSpacer(dp8)

        Image(
            modifier = Modifier.align(Alignment.CenterHorizontally).size(200.dp),
            painter = painterResource(R.drawable.tips_qr_code),
            contentDescription = stringResource(R.string.qr_code_cd)
        )

        VSpacer(dp8)

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.End
        ) {
            SButton(
                modifier = Modifier.fillMaxHeight(),
                onClick = {
                    listener?.onIHelpedClick()
                },
                content = MainButtonContent(stringResource(R.string.i_helped))
            )

            HSpacer(dp14)

            SButton(
                modifier = Modifier.fillMaxHeight(),
                onClick = {
                    listener?.onNotWantClick()
                },
                content = MainButtonContent(stringResource(R.string.i_do_not_want))
            )
        }

        VSpacer(dp8)

        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.End
        ) {
            SButton(
                modifier = Modifier.fillMaxHeight(),
                onClick = {
                    listener?.onCancelClick()
                },
                content = MainButtonContent(stringResource(R.string.remind_later))
            )
            HSpacer(dp14)

            SButton(
                modifier = Modifier.fillMaxHeight(),
                onClick = {
                    listener?.onTipsLinkClick()
                },
                content = MainButtonContent(
                    stringResource(R.string.open_in_browser)
                )
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    val context = LocalContext.current
    Content(
        listener = null,
        init = TipsSheetController.InitData.makeTest(context)
    )
}