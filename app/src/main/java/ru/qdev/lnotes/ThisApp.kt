package ru.qdev.lnotes

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import src.BuildConfig
import src.R

/**
 * Created by Vladimir Kudashov on 13.04.17.
 */

@HiltAndroidApp
class ThisApp : Application() {
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
    }

    companion object {
        fun getContext() : Context {
            return thisApp!!.getApplicationContext()
        }

        private var thisApp: ThisApp? = null
    }
}
