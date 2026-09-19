package ru.qdev.lnotes

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import ru.qdev.lnotes.core.QDVAppConst
import ru.qdev.lnotes.core.pref.NotesPreferenceHelper
import src.BuildConfig
import src.R
import java.util.Date
import javax.inject.Inject

/**
 * Created by Vladimir Kudashov on 13.04.17.
 */

@HiltAndroidApp
class ThisApp : Application() {
    @Inject
    lateinit var notesPreferenceHelper: NotesPreferenceHelper


    override fun onCreate() {
        super.onCreate()

        thisApp = this

        if (!BuildConfig.DEBUG) {
            val config = AppMetricaConfig.newConfigBuilder(
                getString(R.string.app_matrika_key)
            )
                .build()

            // Initializing the AppMetrica SDK.
            AppMetrica.activate(this, config)
        }

        if (notesPreferenceHelper.tipsNextShowTime == 0L) {
            notesPreferenceHelper.tipsNextShowTime = Date().time + QDVAppConst.NextShowTimeLaterDelay
        }
    }

    companion object {
        fun getContext() : Context {
            return thisApp!!.getApplicationContext()
        }

        private var thisApp: ThisApp? = null
    }
}
