package ru.q_dev.lnotes;

import android.app.Application;
import android.content.Context;

/**
 * Created by Vladimir Kudashov on 13.04.17.
 */

public class ThisApp extends Application {
    public static Context getContext() {
        return thisApp.getApplicationContext();
    }

    private static ThisApp thisApp = null;

    @Override
    public void onCreate() {
        super.onCreate();
        thisApp = this;

        //TODO Need delete after QDVMyBaseQueryHelper remove
        new QDVDbDatabase(getContext()).getWritableDatabase();
    }
}
