package ru.qdev.lnotes;

/**
 * Created by Vladimir Kudashov on 31.03.18.
 */

import android.support.v7.app.AppCompatActivity;

public class QDVVersionDifference extends QDVVersion {
    static public boolean isFreeVersion() {  return false; }
    static public boolean adsPresent () {
        return isFreeVersion();
    }
    static public void loadAd (AppCompatActivity activity) { }
}
