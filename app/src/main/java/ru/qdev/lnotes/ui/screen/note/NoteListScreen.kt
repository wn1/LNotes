package ru.qdev.lnotes.ui.screen.base

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.qdev.lnotes.ui.screen.note.NoteListScreenViewModel

@Composable
fun NoteListScreen(viewModel: NoteListScreenViewModel = viewModel()) {
    Text(text = "Hello world")
}