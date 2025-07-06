package ru.qdev.lnotes.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.qdev.lnotes.core.events.AppEvents
import ru.qdev.lnotes.db.QDVDbManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ManagerModule {

    @Provides
    @Singleton
    fun provideDbManager(@ApplicationContext context: Context) : QDVDbManager {
        return QDVDbManager(context)
    }

    @Provides
    @Singleton
    fun provideAppEvents(@ApplicationContext context: Context) : AppEvents {
        return AppEvents(context)
    }
}