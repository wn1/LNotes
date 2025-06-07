package ru.qdev.lnotes.ui.screen.base

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.reply.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import ru.qdev.lnotes.db.entity.NotesEntry
import ru.qdev.lnotes.model.Folder
import ru.qdev.lnotes.ui.screen.note.NoteListScreenListener
import ru.qdev.lnotes.ui.screen.note.NoteListScreenViewModel
import ru.qdev.lnotes.ui.theme.dp40
import ru.qdev.lnotes.ui.theme.dp8
import src.R

@ExperimentalMaterial3Api
@Composable
fun NoteListScreen(viewModel: NoteListScreenViewModel = hiltViewModel()) {
    BaseScreen(baseViewModel = viewModel) {
        ScreenContent(
            listener = viewModel,
            folderList = viewModel.folderListS.value,
            notesFlow = viewModel.notesPagingFlow
        )
    }
}

@ExperimentalMaterial3Api
@Composable
private fun ScreenContent(
    listener: NoteListScreenListener?,
    folderList: List<Folder>,
    notesFlow: Flow<PagingData<NotesEntry>>,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val notes = notesFlow.collectAsLazyPagingItems()

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
                                scope.launch {
                                    if (drawerState.isOpen) {
                                        drawerState.close()
                                    } else {
                                        drawerState.open()
                                    }
                                    listener?.onFolderMenuClick()
                                }
                            },
                            painter = painterResource(R.drawable.ic_menu_24dp),
                            contentDescription = stringResource(R.string.folder_listview_description)
                        )
                        Text("Small Top App Bar")
                    }
                }
            )
        },
    ) { innerPadding ->

        ModalNavigationDrawer (
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    FolderList(
                        modifier = Modifier,
                        folders = folderList
                    )
                }
            },
        ) {
            Column (modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer)
            ){

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = notes.itemCount,
                        key = { index -> notes[index]?.uid ?: index }
                    ) { index ->
                        notes[index]?.let {
                            NotesItem(modifier = Modifier.fillMaxSize(), note = it)
                        }
                    }

                }


            }

//            Text(text = "test 2")
//            Scaffold(
//                floatingActionButton = {
//                    ExtendedFloatingActionButton(
//                        text = { Text("Show drawer") },
//                        icon = { Icon(Icons.Filled.Add, contentDescription = "") },
//                        onClick = {
//                            scope.launch {
//                                drawerState.apply {
//                                    if (isClosed) open() else close()
//                                }
//                            }
//                        }
//                    )
//                }
//            ) { contentPadding ->
//                // Screen content
//            }
        }
    }
}

@Composable
private fun NotesItem(modifier: Modifier,
                      note: NotesEntry) {
    Text(text = note.content ?: "")
}

@Composable
private fun FolderList(modifier: Modifier,
                      folders: List<Folder>) {
    folders.forEach {
        Text(text = it.title)
    }
//    Button(onClick = {}) { }
//    Text(text = "test", color = Color.Blue, fontSize = 14.sp)
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
    AppTheme {
        ScreenContent(
            listener = null,
            folderList = Folder.makeTestList(context),
            notesFlow = flowOf(PagingData.from(NotesEntry.makeTestList()))
        )
    }
}