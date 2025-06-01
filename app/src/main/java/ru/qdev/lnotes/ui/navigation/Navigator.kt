package ru.qdev.lnotes.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.qdev.lnotes.ui.navigation.route.base.BaseRoute
import ru.qdev.lnotes.ui.navigation.route.note.NoteListRoute
import ru.qdev.lnotes.ui.screen.base.NoteListScreen

class Navigator (val context: Context) {

    @Composable
    fun NavigationView(startDestination: BaseRoute) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = startDestination) {
            composable<NoteListRoute> { NoteListScreen() }
        }
    }
}