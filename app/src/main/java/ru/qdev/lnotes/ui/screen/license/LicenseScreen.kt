package ru.qdev.lnotes.ui.screen.license

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reply.ui.theme.AppTheme
import ru.qdev.lnotes.ui.screen.base.BaseScreen
import ru.qdev.lnotes.ui.theme.contentHPaddingDp
import ru.qdev.lnotes.ui.theme.dp8
import ru.qdev.lnotes.ui.view.button.MainButtonContent
import ru.qdev.lnotes.ui.view.button.SButton
import ru.qdev.lnotes.ui.view.button.SecondaryButtonColors
import ru.qdev.lnotes.ui.view.text.SText
import src.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(viewModel: LicenseScreenViewModel = hiltViewModel(),
                  onAccept: () -> Unit) {
    BaseScreen(baseViewModel = viewModel) {
        ScreenContent(
            listener = viewModel,
            licenseText = viewModel.licenseText.value,
            onAccept = onAccept
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
private fun ScreenContent(listener: LicenseScreenViewModelListener?,
                          licenseText: String?,
                          onAccept: () -> Unit) {
    Scaffold() {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Spacer(Modifier.height(dp8))

            SText(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = contentHPaddingDp)
                ,
                text = licenseText ?: ""
            )

            Spacer(Modifier.height(dp8))

            Row(Modifier.padding(horizontal = contentHPaddingDp).navigationBarsPadding()) {
                SButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        listener?.onCancelClick()
                    },
                    content = MainButtonContent(
                        text = stringResource(R.string.cancel),
                    ),
                    colors = SecondaryButtonColors()
                )

                Spacer(Modifier.width(dp8))

                SButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        listener?.onAcceptClick()
                        onAccept()
                    },
                    content = MainButtonContent(
                        text = stringResource(R.string.accept_btn),
                    )
                )
            }

            Spacer(Modifier.height(dp8))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark",
    showBackground = true
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight",
    showBackground = true
)
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(
            listener = null,
            licenseText = "License text",
            onAccept = {}
        )
    }
}