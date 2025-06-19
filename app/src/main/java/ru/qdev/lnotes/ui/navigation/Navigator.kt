package ru.qdev.lnotes.ui.navigation

import android.content.Context
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.qdev.lnotes.ui.navigation.route.base.BaseRoute
import ru.qdev.lnotes.ui.navigation.route.note.NoteEditScreenRoute
import ru.qdev.lnotes.ui.navigation.route.note.NoteListScreenRoute
import ru.qdev.lnotes.ui.screen.base.NoteListScreen
import ru.qdev.lnotes.ui.screen.note_edit.NoteEditScreen

class Navigator (val context: Context) {
    lateinit var navController: NavHostController

    @ExperimentalMaterial3Api
    @Composable
    fun NavigationView(modifier: Modifier, startDestination: BaseRoute) {
        navController = rememberNavController()

        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination) {

            composable<NoteListScreenRoute> { NoteListScreen() }
            composable<NoteEditScreenRoute> { NoteEditScreen() }
        }
    }

    fun navigate(destination: BaseRoute) {
        Log.i(TAG, "navigate: ${destination.screenUid}")
        navController.navigate(destination)
    }

    fun goBack() {
        Log.i(TAG, "goBack")
        navController.popBackStack()
    }

    companion object {
        private const val TAG = "Navigator"
    }
}