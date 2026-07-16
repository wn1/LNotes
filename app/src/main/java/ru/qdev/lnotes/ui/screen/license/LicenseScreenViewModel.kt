package ru.qdev.lnotes.ui.screen.license

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.qdev.lnotes.core.pref.NotesPreferenceHelper
import ru.qdev.lnotes.ui.screen.base.BaseScreenViewModel
import src.R
import javax.inject.Inject

interface LicenseScreenViewModelListener {
    fun onCancelClick()
    fun onAcceptClick()
}

@HiltViewModel
class LicenseScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val preferenceHelper: NotesPreferenceHelper
): LicenseScreenViewModelListener, BaseScreenViewModel() {

    private val TAG = javaClass.simpleName

    val licenseText = mutableStateOf("")

    override fun provideContext(): Context {
        return context
    }

    override fun provideSavedStateHandle(): SavedStateHandle {
        return savedStateHandle
    }

    init {
        fillLicenseText()
    }

    private fun fillLicenseText(){
        val rawFile = if (context.getString(R.string.license_lang) == "ru") {
            R.raw.license_ru
        }
        else {
            R.raw.license_en
        }

        val text = context.resources.openRawResource(rawFile).bufferedReader().use {
            it.readText()
        }

        licenseText.value = text
    }

    override fun onCancelClick() {
        val logFn = "onCancelClick"
        Log.i(TAG, logFn)

        activityRef?.get()?.let {
            it.finishAffinity()
        }
    }

    override fun onAcceptClick() {
        val logFn = "onAcceptClick"
        Log.i(TAG, logFn)

        preferenceHelper.isLicenseAccepted = true
    }
}