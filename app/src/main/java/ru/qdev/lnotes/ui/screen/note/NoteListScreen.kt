package ru.qdev.lnotes.ui.screen.base

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import ru.qdev.lnotes.ui.theme.contentHPaddingDp
import ru.qdev.lnotes.ui.theme.dp40
import ru.qdev.lnotes.ui.theme.dp8
import ru.qdev.lnotes.ui.view.spacer.HSpacer
import ru.qdev.lnotes.ui.view.spacer.VSpacer
import ru.qdev.lnotes.ui.view.text.SText
import ru.qdev.lnotes.utils.live_data.LiveEvent
import src.R

@ExperimentalMaterial3Api
@Composable
fun NoteListScreen(viewModel: NoteListScreenViewModel = hiltViewModel()) {
    BaseScreen(baseViewModel = viewModel) {
        ScreenContent(
            listener = viewModel,
            folderList = viewModel.folderListS.value,
            selectedFolder = viewModel.selectedFolderS.value,
            notesFlow = viewModel.notesPagingFlow,
            reloadNotesAndGoToFirstEvent = viewModel.reloadNotesAndGoToFirstEvent.value,
            drawerHideEvent = viewModel.drawerHideEvent.value
        )
    }
}

@ExperimentalMaterial3Api
@Composable
private fun ScreenContent(
    listener: NoteListScreenListener?,
    folderList: List<Folder>,
    selectedFolder: Folder?,
    notesFlow: Flow<PagingData<NotesEntry>>,
    reloadNotesAndGoToFirstEvent: LiveEvent<Boolean>? = null,
    drawerHideEvent: LiveEvent<Boolean>? = null,
    drawerShowEvent: LiveEvent<Boolean>? = null
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val notes = notesFlow.collectAsLazyPagingItems()
    val notesColumnState = rememberLazyListState()

    reloadNotesAndGoToFirstEvent?.getEventAndReset()?.let {
        scope.launch {
            notesColumnState.scrollToItem(0)
            notes.refresh()
        }
    }

    drawerHideEvent?.getEventAndReset()?.let {
        scope.launch {
            drawerState.close()
        }
    }

    drawerShowEvent?.getEventAndReset()?.let {
        scope.launch {
            drawerState.open()
        }
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

                        HSpacer(dp8)

                        SText(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = selectedFolder?.title ?: "",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
    ) { innerPadding ->

        ModalNavigationDrawer (
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            drawerState = drawerState,
            drawerContent = {
                FolderListDrawer(
                    modifier = Modifier,
                    folders = folderList,
                    selectedFolder = selectedFolder,
                    onFolderClick = {
                        listener?.onSelectFolder(it)
                    }
                )
            },
        ) {
            Column (modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer)
            ){

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = notesColumnState
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
    SText(text = note.content ?: "")
}

@Composable
private fun FolderListDrawer(modifier: Modifier,
                             folders: List<Folder>,
                             selectedFolder: Folder?,
                             onFolderClick: (Folder) -> Unit) {
    ModalDrawerSheet {
        Column {
            folders.forEach {
                val textColor: Color
                val selectedM = if (selectedFolder?.id == it.id && selectedFolder?.id != null) {
                    textColor = MaterialTheme.colorScheme.primary
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                }
                else {
                    textColor = MaterialTheme.colorScheme.secondary
                    Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                }

                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = dp40)
                        .clickable {
                            onFolderClick(it)
                        }
                        .then(selectedM)
                        .padding(contentHPaddingDp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    SText(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = it.title,
                        color = textColor
                    )
                }
            }
        }
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
    val folders = Folder.makeTestList(context)
    AppTheme {
        ScreenContent(
            listener = null,
            selectedFolder = folders.getOrNull(1),
            folderList = folders,
            notesFlow = flowOf(PagingData.from(NotesEntry.makeTestList()))
        )
    }
}

@ExperimentalMaterial3Api
@Composable
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
private fun ScreenContentPreviewDrawer() {
    val context = LocalContext.current
    val folders = Folder.makeTestList(context)
    AppTheme {
        FolderListDrawer(
            modifier = Modifier,
            folders = folders,
            selectedFolder = folders.getOrNull(1)
        ) {

        }
    }
}