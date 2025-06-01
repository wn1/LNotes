package ru.qdev.lnotes.ui.screen.base

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reply.ui.theme.AppTheme
import ru.qdev.lnotes.db.entity.NotesEntry
import ru.qdev.lnotes.ui.screen.note.NoteListScreenViewModel

@ExperimentalMaterial3Api
@Composable
fun NoteListScreen(viewModel: NoteListScreenViewModel = hiltViewModel()) {
    BaseScreen(baseViewModel = viewModel) {
        ScreenContent(
            test = viewModel.testS.value
        )
    }
}

@ExperimentalMaterial3Api
@Composable
private fun ScreenContent(test: List<NotesEntry>) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Small Top App Bar")
                }
            )
        },
    ) { innerPadding ->
        Column (modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
        ){
            NotesList(modifier = Modifier.fillMaxSize(), test = test)
        }
    }
}

@Composable
private fun NotesList(modifier: Modifier,
                      test: List<NotesEntry>) {
    test.forEach {
        Text(text = it.content ?: "")
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
    AppTheme {
        ScreenContent(
            test = listOf()
        )
    }
}