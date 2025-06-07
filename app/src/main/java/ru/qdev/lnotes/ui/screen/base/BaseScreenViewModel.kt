package ru.qdev.lnotes.ui.screen.base

import android.content.Context
import androidx.lifecycle.ViewModel

abstract class BaseScreenViewModel : ViewModel() {
    abstract fun provideContext(): Context
}