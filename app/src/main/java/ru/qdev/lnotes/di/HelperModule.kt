package ru.qdev.lnotes.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.qdev.lnotes.core.pref.NotesPreferenceHelper
import ru.qdev.lnotes.ui.navigation.QDVNavigator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HelperModule {

    @Provides
    @Singleton
    fun provideNavigator(@ApplicationContext context: Context) : QDVNavigator {
        return QDVNavigator(context)
    }

    @Provides
    @Singleton
    fun provideNotesPreferenceHelper(@ApplicationContext context: Context) : NotesPreferenceHelper {
        return NotesPreferenceHelper(context)
    }
}