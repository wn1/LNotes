package ru.qdev.lnotes.ui.navigation

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.qdev.lnotes.ui.navigation.route.base.BaseRoute
import ru.qdev.lnotes.ui.navigation.route.note.NoteListRoute
import ru.qdev.lnotes.ui.screen.base.NoteListScreen

class Navigator (val context: Context) {

    @ExperimentalMaterial3Api
    @Composable
    fun NavigationView(modifier: Modifier, startDestination: BaseRoute) {
        val navController = rememberNavController()

        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination) {
            composable<NoteListRoute> { NoteListScreen() }
        }
    }
}