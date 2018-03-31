package ru.q_dev.lnotes;

/**
 * Created by user_vladimir on 31.03.18.
 */

import android.app.Activity;

public class QDVVersionDifference {
    static public boolean isFreeVersion() { return false; }
    static public boolean adsPresent () {
        return isFreeVersion();
    }
    static public void loadAd (Activity activity) { }
}
