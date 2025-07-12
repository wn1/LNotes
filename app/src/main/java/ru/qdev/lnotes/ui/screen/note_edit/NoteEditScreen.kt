@file:OptIn(ExperimentalLayoutApi::class)

package ru.qdev.lnotes.ui.screen.note_edit

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reply.ui.theme.AppTheme
import ru.qdev.lnotes.model.Folder
import ru.qdev.lnotes.ui.screen.base.BaseScreen
import ru.qdev.lnotes.ui.theme.contentHPaddingDp
import ru.qdev.lnotes.ui.theme.dp1
import ru.qdev.lnotes.ui.theme.dp10
import ru.qdev.lnotes.ui.theme.dp14
import ru.qdev.lnotes.ui.theme.dp4
import ru.qdev.lnotes.ui.theme.dp40
import ru.qdev.lnotes.ui.theme.dp44
import ru.qdev.lnotes.ui.theme.dp8
import ru.qdev.lnotes.ui.theme.sp12
import ru.qdev.lnotes.ui.theme.sp16
import ru.qdev.lnotes.ui.view.spacer.HSpacer
import ru.qdev.lnotes.ui.view.spacer.VSpacer
import ru.qdev.lnotes.ui.view.text.SText
import ru.qdev.lnotes.ui.view.text.STextField
import ru.qdev.lnotes.utils.compose.toDp
import src.R

@ExperimentalMaterial3Api
@Composable
fun NoteEditScreen(viewModel: NoteEditScreenViewModel = hiltViewModel()) {
    BaseScreen(baseViewModel = viewModel) {
        ScreenContent(
            listener = viewModel,
            text = viewModel.textS.value,
            checkedSwitchEnabled = viewModel.checkedSwitchEnabledS.value
        )
    }
}

@ExperimentalMaterial3Api
@Composable
private fun ScreenContent(listener: NoteEditScreenViewModelListener?,
                          text: TextFieldValue,
                          checkedSwitchEnabled: Boolean) {
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val menuExpandedS = remember { mutableStateOf(false) }

    LaunchedEffect("focus")
    {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row {
                        Icon(
                            modifier = Modifier.size(dp40).clip(RoundedCornerShape(dp8)).clickable {
                                listener?.onBackClick()
                            },
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = stringResource(R.string.folder_listview_description)
                        )

//                        HSpacer(dp8)
//
//                        SText(
//                            modifier = Modifier.align(Alignment.CenterVertically),
//                            text = selectedFolderTitle ?: "",
//                            color = MaterialTheme.colorScheme.primary
//                        )
                    }
                },
                actions = {
                    MainDropdownMenu(
                        modifier = Modifier,
                        listener = listener,
                        expandedS = menuExpandedS
                    )

                    Box (
                        modifier = Modifier.defaultMinSize(minWidth = dp44, minHeight = dp40)
                            .clip(RoundedCornerShape(dp8))
                            .clickable {
                                listener?.onSaveClick()
                            }
                    ) {
                        SText(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.action_ok),
                            fontSize = sp16,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HSpacer(dp14)
                }
            )
        },
    ) { innerPadding ->

        @Composable fun ToolbarButton(
            text: String,
            onClick: () -> Unit,
            fontSize: TextUnit = sp16,
            enabled: Boolean = true
        ) {
            Box(modifier = Modifier
                .defaultMinSize(dp40)
                .clickable {
                    onClick()
                }
            ) {
                Column (
                    modifier = Modifier
                        .align(Alignment.Center)
                ){
                    VSpacer(dp10)
                    SText(
                        modifier = Modifier.padding(horizontal = dp10),
                        text = text,
                        color = if (enabled) MaterialTheme.colorScheme.tertiary else Color.LightGray,
                        fontSize = fontSize
                    )
                    VSpacer(dp10)
                }
            }
        }

        @Composable fun RowScope.ToolbarDivider() {
            HSpacer(dp4)
            Spacer(modifier = Modifier.width(dp1).background(Color.LightGray).fillMaxHeight())
            HSpacer(dp4)
        }

        val screenH = remember { mutableFloatStateOf(0f) }
        val imeHInDp = (screenH.floatValue * 0.5f).toDp()

        Box(modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(innerPadding)
            .fillMaxSize()
            .onGloballyPositioned {
                screenH.floatValue = it.size.height.toFloat()
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .height(intrinsicSize = IntrinsicSize.Max)
                        .fillMaxWidth()
                        .padding(horizontal = contentHPaddingDp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolbarDivider()

                    ToolbarButton(
                        text = stringResource(R.string.add_time_text),
                        onClick = {
                            listener?.onAddTimeClick()
                        }
                    )

                    ToolbarDivider()

                    ToolbarButton(
                        text = stringResource(R.string.option_checked_char),
                        onClick = {
                            listener?.onInsertCheckedCharClick()
                        }
                    )

                    ToolbarDivider()

                    ToolbarButton(
                        text = stringResource(R.string.option_unchecked_char),
                        onClick = {
                            listener?.onInsertUncheckedCharClick()
                        }
                    )

                    ToolbarDivider()

                    ToolbarButton(
                        text = stringResource(R.string.option_checked_char)
                                +"\n"+stringResource(R.string.option_unchecked_char),
                        onClick = {
                            listener?.onCheckedSwitchClick()
                        },
                        fontSize = sp12,
                        enabled = checkedSwitchEnabled
                    )

                    ToolbarDivider()
                }
                HorizontalDivider()

                Column (Modifier.weight(1f).verticalScroll(rememberScrollState())){
                    STextField(
                        modifier = Modifier
                            .height(screenH.floatValue.toDp()),
                        textFieldModifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester),
                        value = text,
                        onValueChange = {
                            listener?.onTextChange(it)
                        },
                    )

                    VSpacer(dp8)
                    VSpacer(imeHInDp)
                }
            }
        }
    }

    BackHandler {
        listener?.onBackClick()
    }
}

@Composable
fun MainDropdownMenu(modifier: Modifier,
                     listener: NoteEditScreenViewModelListener?,
                     expandedS: MutableState<Boolean>
) {
    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expandedS.value = !expandedS.value }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.menu_cd),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        DropdownMenu(
            expanded = expandedS.value,
            onDismissRequest = { expandedS.value = false }
        ) {
            DropdownMenuItem(
                text = {
                    SText(
                        text = stringResource(R.string.action_send_text)
                    )
                },
                onClick = {
                    expandedS.value = false
                    listener?.onSendNoteClick()
                }
            )
        }
    }
}


@ExperimentalMaterial3Api
@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight"
)
private fun ScreenContentPreview() {
    val context = LocalContext.current
    val folders = Folder.makeTestList(context)
    AppTheme {
        ScreenContent(
            listener = null,
            text = TextFieldValue("Text"),
            checkedSwitchEnabled = true
        )
    }
}

@ExperimentalMaterial3Api
@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight"
)
private fun ScreenContentPreview2() {
    val context = LocalContext.current
    val folders = Folder.makeTestList(context)
    AppTheme {
        ScreenContent(
            listener = null,
            text = TextFieldValue("Text"),
            checkedSwitchEnabled = false
        )
    }
}