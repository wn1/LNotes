package ru.q_dev.lnotes;

import android.app.Application;
import android.content.Context;

/**
 * Created by Vladimir Kudashov on 13.04.17.
 */

public class ThisApp extends Application {
    public static Context getContext() {
        return sContext;
    }

    private static Context sContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        //TODO Need delete after QDVMyBaseQueryHelper remove
        new QDVDbDatabase(sContext);
    }
}
