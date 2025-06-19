package ru.qdev.lnotes.ui.screen.note_edit

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import com.example.reply.ui.theme.AppTheme
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import ru.qdev.lnotes.db.entity.NotesEntry
import ru.qdev.lnotes.model.Folder
import ru.qdev.lnotes.ui.screen.base.BaseScreen
import ru.qdev.lnotes.ui.theme.dp40
import ru.qdev.lnotes.ui.theme.dp8
import ru.qdev.lnotes.ui.view.spacer.HSpacer
import ru.qdev.lnotes.ui.view.text.SText
import ru.qdev.lnotes.ui.view.text.STextField
import src.R

@ExperimentalMaterial3Api
@Composable
fun NoteEditScreen(viewModel: NoteEditScreenViewModel = hiltViewModel()) {
    BaseScreen(baseViewModel = viewModel) {
        ScreenContent(
            listener = viewModel,
            text = viewModel.textS.value
        )
    }

    BackHandler {
        viewModel.onBackClick()
    }
}

@ExperimentalMaterial3Api
@Composable
private fun ScreenContent(listener: NoteEditScreenViewModelListener?,
                          text: String) {
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
                    SText(
                        modifier = Modifier.clip(RoundedCornerShape(dp8)).clickable {
                            listener?.onSaveClick()
                        },
                        text = stringResource(R.string.action_ok)
                    )
                }
            )
        },
    ) { innerPadding ->
        Column (modifier = Modifier.padding(innerPadding)){
            STextField(
                modifier = Modifier.fillMaxSize(),
                value = text,
                onValueChange = {
                    listener?.onTextChange(it)
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
            text = "Text"
        )
    }
}