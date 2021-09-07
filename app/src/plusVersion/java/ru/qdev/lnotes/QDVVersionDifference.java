package ru.qdev.lnotes;

/**
 * Created by Vladimir Kudashov on 31.03.18.
 */

import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

public class QDVVersionDifference {
    static public boolean isFreeVersion() { return false; }
    static public boolean adsPresent () {
        return isFreeVersion();
    }
    static public void loadAd (AppCompatActivity activity) { }

    @Nullable
    static public TextView getLabelBuyPlusVersion (ViewGroup rootLayout) {
        return null;
    }
}
