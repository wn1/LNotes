package ru.qdev.lnotes;

import android.app.Application;
import android.content.Context;
import androidx.annotation.AnyThread;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Created by Vladimir Kudashov on 13.04.17.
 */

@HiltAndroidApp
public class ThisApp extends Application {
    public static Context getContext() {
        return thisApp.getApplicationContext();
    }

    private static ThisApp thisApp = null;

    @Override
    public void onCreate() {
        super.onCreate();
        thisApp = this;
    }
}
